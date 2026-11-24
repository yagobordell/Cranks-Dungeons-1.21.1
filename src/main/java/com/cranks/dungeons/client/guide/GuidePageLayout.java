package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import java.util.*;

/** Shared wiki layout for both the full Compendium and contextual reader. */
public final class GuidePageLayout {
    public record Line(OrderedText text,int x,int y,int color){}
    private record Fill(int x,int y,int width,int height,int color){}
    private record Picture(String key,String caption,int x,int y){}
    private final List<Line> lines=new ArrayList<>();private final List<Fill> fills=new ArrayList<>();private final List<Picture> pictures=new ArrayList<>();
    private final Map<String,Integer> anchors=new LinkedHashMap<>();private final GuideTarget target;private final int width;private int y;
    public final int height;
    private GuidePageLayout(GuideDocument document,GuideTarget target,int width){
        this.target=GuideTopics.root(GuideTopics.resolve(target));this.width=Math.max(70,width);y=4;
        text(document.title(),0,this.width,CompendiumStyle.TEXT);y+=3;fills.add(new Fill(0,y,this.width,1,0xFF5A5A5A));y+=12;
        var headings=document.blocks().stream().filter(b->b.type().equals("heading")).toList();boolean contents=false;
        for(var block:document.blocks()){
            if(!contents&&block.type().equals("heading")){contents=true;if(headings.size()>=3)contents(headings);}
            switch(block.type()){
                case "item_image"->{pictures.add(new Picture(block.value(),GuideIllustrations.caption(block),0,y));y+=48;}
                case "spacer"->y+=8;
                case "heading","subheading"->{y+=7;String anchor=anchor(block);anchors.putIfAbsent(anchor,y);text(block.value(),0,this.width,CompendiumStyle.TEXT);y+=10;
                    if(block.type().equals("heading")){fills.add(new Fill(0,y-6,this.width,1,0xFF343434));}}
                case "table"->{table(block.value());y+=9;}
                case "stat_catalog"->{try{for(var row:GuideRuneArticles.catalogue(com.cranks.dungeons.equipment.EquipmentType.valueOf(block.value().toUpperCase(Locale.ROOT)))){
                    if(row.type().equals("table"))table(row.value());else{text(row.value(),0,this.width,CompendiumStyle.TEXT);y+=4;}}}catch(IllegalArgumentException ignored){}}
                default->{for(String paragraph:block.value().split("\\\\n|\\n",-1)){if(paragraph.isBlank()){y+=4;continue;}text((block.type().equals("list")?"• ":"")+paragraph,0,this.width,CompendiumStyle.TEXT_MUTED);}y+=7;}
            }
        }height=y+5;
    }
    public static GuidePageLayout build(GuideDocument doc,GuideTarget target,int width){return new GuidePageLayout(doc,target,width);}
    public static String anchor(GuideDocument.Block block){return block.style()!=null&&block.style().startsWith("anchor=")?block.style().substring(7):GuideReferences.slug(GuideText.plain(block.value()));}
    public int anchorY(String anchor){if(anchor==null||anchor.isBlank())return 0;return anchors.getOrDefault(anchor,anchors.entrySet().stream().filter(e->e.getKey().contains(anchor)).map(Map.Entry::getValue).findFirst().orElse(0));}
    private void text(String text,int x,int room,int color){for(var line:MinecraftClient.getInstance().textRenderer.wrapLines(GuideText.parse(text),Math.max(12,room))){lines.add(new Line(line,x,y,color));y+=11;}}
    private void contents(List<GuideDocument.Block> headings){
        int start=y;y+=7;text("Contents",7,width-14,CompendiumStyle.TEXT);y+=5;
        int columns=width>=300?2:1,cell=(width-20)/columns;int top=y;
        for(int i=0;i<headings.size();i++){
            var block=headings.get(i);y=top+(i/columns)*14;
            String title=GuideText.plain(block.value());String shortTitle=MinecraftClient.getInstance().textRenderer.trimToWidth(title,cell-8);
            text(GuideText.reference(shortTitle,new GuideTarget(target.kind(),target.key()+"#"+anchor(block))),7+(i%columns)*cell,cell-8,CompendiumStyle.TEXT_MUTED);
        }
        y=top+((headings.size()+columns-1)/columns)*14+7;
        fills.add(new Fill(0,start,width,y-start,0xFF141414));fills.add(new Fill(0,start,width,1,0xFF454545));fills.add(new Fill(0,y-1,width,1,0xFF454545));y+=10;
    }
    private void table(String json){
        var data=JsonParser.parseString(json).getAsJsonObject();var headers=data.getAsJsonArray("headers");var rows=data.getAsJsonArray("rows");int count=headers.size();if(count==0)return;
        if(width<250&&count>2){for(var row:rows){var cells=row.getAsJsonArray();var title=new JsonArray();title.add(cells.get(0));drawRow(title,new int[]{width},true,0);
            for(int i=1;i<count;i++){var pair=new JsonArray();pair.add(headers.get(i));pair.add(i<cells.size()?cells.get(i).getAsString():"");drawRow(pair,new int[]{width/3,width-width/3},false,i);}
        }return;}

        double[] weights=new double[count];double sum=0;var configured=data.getAsJsonArray("weights");
        for(int i=0;i<count;i++){weights[i]=configured!=null&&i<configured.size()?Math.max(.1,configured.get(i).getAsDouble()):1;sum+=weights[i];}
        int[] widths=new int[count];int used=0;for(int i=0;i<count;i++){widths[i]=i==count-1?width-used:(int)(width*weights[i]/sum);used+=widths[i];}
        drawRow(headers,widths,true,0);int index=0;for(var row:rows)drawRow(row.getAsJsonArray(),widths,false,index++);
    }
    private void drawRow(JsonArray cells,int[] widths,boolean header,int index){
        var font=MinecraftClient.getInstance().textRenderer;int rowHeight=0,x=0,start=y;
        for(int col=0;col<widths.length;col++){
            String value=col<cells.size()?cells.get(col).getAsString():"";var wrapped=font.wrapLines(GuideText.parse(value),Math.max(12,widths[col]-10));
            for(int line=0;line<wrapped.size();line++)lines.add(new Line(wrapped.get(line),x+5,start+5+line*11,header?CompendiumStyle.TEXT:CompendiumStyle.TEXT_MUTED));
            rowHeight=Math.max(rowHeight,wrapped.size()*11+10);x+=widths[col];
        }
        rowHeight=Math.max(20,rowHeight);fills.add(new Fill(0,start,width,rowHeight,header?0xFF282828:index%2==0?0xFF151515:0xFF101010));
        fills.add(new Fill(0,start,width,1,0xFF424242));x=0;for(int cell:widths){fills.add(new Fill(x,start,1,rowHeight,0xFF353535));x+=cell;}fills.add(new Fill(width-1,start,1,rowHeight,0xFF353535));fills.add(new Fill(0,start+rowHeight-1,width,1,0xFF353535));y+=rowHeight;
    }
    public void draw(DrawContext context,int x,int y,int top,int bottom,List<GuideHit> hits){
        var font=MinecraftClient.getInstance().textRenderer;
        for(var fill:fills){int fy=y+fill.y();if(fy+fill.height()>top&&fy<bottom)context.fill(x+fill.x(),fy,x+fill.x()+fill.width(),fy+fill.height(),fill.color());}
        for(var pic:pictures){int py=y+pic.y();if(py+40>top&&py<bottom)GuideIllustrations.draw(context,pic.key(),x+pic.x(),py,width,pic.caption());}
        for(var line:lines){int ly=y+line.y();if(ly+10<=top||ly>=bottom)continue;
            if(hits==null)GuideInteraction.drawLinked(context,font,line.text(),x+line.x(),ly,line.color());
            else{context.drawText(font,line.text(),x+line.x(),ly,line.color(),false);GuideInteraction.collect(line.text(),font,x+line.x(),ly,hits,context);}
        }
    }
}

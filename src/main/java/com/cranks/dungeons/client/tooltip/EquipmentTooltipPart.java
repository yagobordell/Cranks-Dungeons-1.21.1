package com.cranks.dungeons.client.tooltip;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentTooltipPart implements TooltipComponent {
    public final String kind, itemId;
    public final Text left, right;
    public final CustomRarity rarity;
    private EquipmentTooltipPart(String kind,String itemId,Text left,Text right,CustomRarity rarity) {
        this.kind=kind;this.itemId=itemId;this.left=left;this.right=right;this.rarity=rarity;
    }
    private record Glyph(int codepoint,Style style,boolean detail) {}

    public static EquipmentTooltipPart of(OrderedText ordered) {
        var glyphs=new ArrayList<Glyph>();
        String[] marker={null};
        ordered.accept((index,style,codepoint)->{
            String insertion=style.getInsertion();
            if(marker[0]==null && insertion!=null && insertion.startsWith(TooltipLines.MARKER))marker[0]=insertion;
            glyphs.add(new Glyph(codepoint,readable(style.withInsertion(null)),(TooltipLines.MARKER+"detail").equals(insertion)));
            return true;
        });
        if(marker[0]==null)return null;
        String[] metadata=marker[0].substring(TooltipLines.MARKER.length()).split("\\|",3);
        String kind=metadata[0];
        CustomRarity rarity=CustomRarity.NORMAL;
        if(metadata.length>1)try{rarity=CustomRarity.valueOf(metadata[1]);}catch(IllegalArgumentException ignored){}
        Text left, right=Text.empty();
        if(kind.startsWith("rune")) {
            left=compose(glyphs.stream().filter(g->!g.detail()).toList());
            right=compose(glyphs.stream().filter(Glyph::detail).toList());
        } else if(kind.equals("aligned")||kind.equals("section")) {
            int split=-1;for(int i=0;i<glyphs.size();i++)if(glyphs.get(i).codepoint()==':'){split=i;break;}
            if(split>=0){left=compose(glyphs.subList(0,split));int start=split+1;
                while(start<glyphs.size()&&Character.isWhitespace(glyphs.get(start).codepoint()))start++;
                right=compose(glyphs.subList(start,glyphs.size()));}
            else left=compose(glyphs);
        } else left=compose(glyphs);
        return new EquipmentTooltipPart(kind,metadata.length>2?metadata[2]:"",left,right,rarity);
    }

    private static Style readable(Style style) {
        if(style.getColor()==null)return style;
        return switch(style.getColor().getRgb()) {
            case 0x555555 -> style.withColor(0x93959D).withItalic(false);
            case 0x5555FF -> style.withColor(0x93B0DF);
            case 0x00AAAA,0x55FFFF -> style.withColor(0x8DC6C9);
            case 0x00AA00 -> style.withColor(0x75AD83);
            default -> style;
        };
    }
    private static Text compose(List<Glyph> glyphs) {
        var result=Text.empty();
        var run=new StringBuilder();Style current=null;
        for(var glyph:glyphs){
            if(current!=null&&!current.equals(glyph.style())){result.append(Text.literal(run.toString()).setStyle(current));run.setLength(0);}
            current=glyph.style();run.appendCodePoint(glyph.codepoint());
        }
        if(!run.isEmpty())result.append(Text.literal(run.toString()).setStyle(current));
        return result;
    }
    public boolean groupedRune(){return kind.startsWith("rune_");}
    @Override public int getHeight(){return kind.equals("separator")?11:12;}
    @Override public int getWidth(TextRenderer renderer){return renderer.getWidth(left)+renderer.getWidth(right)+(right.getString().isBlank()?0:12)
            +(kind.equals("title")||kind.equals("rarity")?22:groupedRune()?8:0);}
    @Override public void drawText(TextRenderer renderer,int x,int y,Matrix4f matrix,VertexConsumerProvider.Immediate consumers) {
        renderer.draw(left.asOrderedText(),x,y,0xFFFFFFFF,true,matrix,consumers,TextRenderer.TextLayerType.NORMAL,0,15728880);
        if(!right.getString().isBlank())renderer.draw(right.asOrderedText(),x+getWidth(renderer)-renderer.getWidth(right),y,
                0xFFFFFFFF,true,matrix,consumers,TextRenderer.TextLayerType.NORMAL,0,15728880);
    }
}

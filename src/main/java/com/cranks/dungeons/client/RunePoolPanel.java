package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.inventory.RunePool;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler;
import com.cranks.dungeons.stat.CustomStat;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.inventory.RunicAltarRules.text;

/** Two independently scrollable columns for rune families and exact source/tier ranges. */
final class RunePoolPanel {
    private record Row(RunePool.Entry entry, CustomStat.AffixType heading, int top, int height) {}
    private record Detail(net.minecraft.text.OrderedText text, int color) {}
    private final RunicEnhancementAltarScreenHandler altar;
    private final Supplier<ItemStack> tome;
    private TextRenderer font;
    private TextFieldWidget search;
    private int x,y,width,height,divider,leftScroll,rightScroll,leftHeight,grabbed;
    private double grab;
    private boolean selectedOnly;
    private ItemStack lastGear=ItemStack.EMPTY,lastTome=ItemStack.EMPTY;
    private List<RunePool.Entry> catalogue=List.of(),filtered=List.of();
    private List<Row> rows=List.of();
    private List<Detail> details=List.of();
    private RunePool.Entry selected;
    private boolean dirty=true;

    RunePoolPanel(RunicEnhancementAltarScreenHandler altar,Supplier<ItemStack> tome){this.altar=altar;this.tome=tome;}
    void init(TextRenderer font,int x,int y,int width,int height){
        this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;divider=x+width*44/100;
        String query=search==null?"":search.getText();
        search=new TextFieldWidget(font,x+3,y+3,divider-x-12,10,text("pool_search"));search.setDrawsBackground(false);search.setMaxLength(80);search.setPlaceholder(text("pool_search"));search.setText(query);
        search.setChangedListener(value->filter(true));dirty=true;grabbed=0;refresh();
    }
    private int top(){return y+22;}
    private int bodyHeight(){return height-22;}
    private int right(){return divider+6;}
    private int rightWidth(){return x+width-right()-7;}
    private StatsScrollbar leftBar(){return new StatsScrollbar(top(),bodyHeight(),leftHeight);}
    private StatsScrollbar rightBar(){return new StatsScrollbar(top(),bodyHeight(),details.size()*10);}
    private void refresh(){
        ItemStack gear=altar.equipment(),currency=tome.get();
        if(!dirty&&ItemStack.areEqual(gear,lastGear)&&(!selectedOnly||ItemStack.areEqual(currency,lastTome)))return;
        lastGear=gear.copy();lastTome=currency.copy();catalogue=RunePool.entries(gear,currency,selectedOnly);dirty=false;filter(false);
    }
    private void filter(boolean reset){
        if(search==null)return;
        String query=search.getText().toLowerCase(Locale.ROOT).strip();String[] terms=query.isEmpty()?new String[0]:query.split("\\s+");
        filtered=catalogue.stream().filter(entry->{String key=entry.stat().getId()+" "+entry.stat().getComponents().stream().map(CustomStat.StatComponent::getDisplayName).reduce("",(a,b)->a+" "+b);return Arrays.stream(terms).allMatch(key.toLowerCase(Locale.ROOT)::contains);}).toList();
        if(selected!=null){String id=selected.stat().getId();selected=filtered.stream().filter(entry->entry.stat().getId().equals(id)).findFirst().orElse(null);}
        if(selected==null&&!filtered.isEmpty())selected=filtered.getFirst();
        var next=new ArrayList<Row>();CustomStat.AffixType heading=null;int offset=0;
        for(var entry:filtered){if(entry.stat().getAffixType()!=heading){heading=entry.stat().getAffixType();next.add(new Row(null,heading,offset,14));offset+=14;}next.add(new Row(entry,null,offset,25));offset+=25;}
        rows=List.copyOf(next);leftHeight=offset;if(reset)leftScroll=0;leftScroll=Math.clamp(leftScroll,0,leftBar().maxScroll());rebuildDetails();
    }
    private String state(RunePool.Entry entry){
        if(entry.present())return text("pool_present").getString();
        if(entry.full())return text(CustomRarity.getRarity(altar.equipment())==CustomRarity.NORMAL?"pool_upgrade":"pool_full").getString();
        return text(entry.targetedOnly()?"pool_targeted_only":"pool_family").getString();
    }
    private void addDetail(List<Detail> out,Text value,int color){for(var line:font.wrapLines(value,Math.max(25,rightWidth()-3)))out.add(new Detail(line,color));}
    private void rebuildDetails(){
        rightScroll=0;var next=new ArrayList<Detail>();
        if(selected!=null){
            addDetail(next,com.cranks.dungeons.client.guide.GuideText.link(selected.name(),com.cranks.dungeons.client.guide.GuideTarget.rune(selected.stat().getId())),CompendiumStyle.TEXT);
            addDetail(next,text("pool_level",selected.stat().getWeight()),CompendiumStyle.TEXT_MUTED);
            for(var route:selected.routes()){
                next.add(new Detail(Text.literal(" ").asOrderedText(),CompendiumStyle.TEXT_MUTED));
                Text source=route.kind()==RunePool.Kind.RANDOM?route.tome().isEmpty()?text("pool_random"):com.cranks.dungeons.client.guide.GuideText.itemName(route.tome())
                        :route.kind()==RunePool.Kind.ARTIFICIAL?text("pool_artificial"):route.kind()==RunePool.Kind.REROLL?text("pool_reroll"):com.cranks.dungeons.client.guide.GuideText.itemName(route.tome());
                addDetail(next,source,CompendiumStyle.TEXT_MUTED);
                if(route.kind()==RunePool.Kind.TARGETED)addDetail(next,text("pool_guaranteed"),CompendiumStyle.TEXT_MUTED);
                for(int tier:route.tiers()){
                    addDetail(next,Text.literal("T"+tier),CompendiumStyle.TEXT);
                    for(var component:selected.stat().getComponents())addDetail(next,
                            Text.literal(component.formatTierRange(tier,selected.stat().getEquipmentType())),CompendiumStyle.TEXT);
                }
            }
        }
        details=List.copyOf(next);
    }
    void render(DrawContext context,int mx,int my,float delta){
        refresh();
        CompendiumStyle.drawTextFieldFrame(context,x,y,divider-x-6,15,search.isFocused());search.render(context,mx,my,delta);
        CompendiumButtonWidget.drawButton(context,right(),y,x+width-right(),15,inside(mx,my,right(),y,x+width-right(),15),true,selectedOnly,text("pool_selected_tome").getString());
        if(filtered.isEmpty()){
            String key=altar.equipment().isEmpty()?"equipment_hint":!RunePool.supports(altar.equipment())?"pool_unsupported"
                    :selectedOnly&&tome.get().isEmpty()?"pool_choose_tome":selectedOnly&&tome.get().getItem() instanceof RunicTomeItem item
                    &&item.getTomeType()==RunicTomeItem.TomeType.ANNULMENT?"pool_removal_only":"pool_empty";
            int ty=top()+3;for(var line:font.wrapLines(text(key),width-10)){if(ty+9>y+height)break;context.drawText(font,line,x+3,ty,CompendiumStyle.TEXT_MUTED,false);ty+=10;}return;
        }
        context.enableScissor(x,top(),divider-5,top()+bodyHeight());
        for(var row:rows){int ty=top()+row.top-leftScroll;if(ty+row.height<=top()||ty>=top()+bodyHeight())continue;
            if(row.entry==null)context.drawText(font,text(row.heading==CustomStat.AffixType.PREFIX?"pool_prefixes":"pool_suffixes"),x+2,ty+3,CompendiumStyle.TEXT_MUTED,false);
            else{
                CompendiumStyle.drawRailPlaque(context,x,ty,divider-x-8,24,row.entry==selected,inside(mx,my,x,Math.max(top(),ty),divider-x-8,Math.min(ty+24,top()+bodyHeight())-Math.max(top(),ty)));
                line(context,row.entry.name(),x+3,ty+4,divider-x-14,CompendiumStyle.TEXT);
                line(context,state(row.entry),x+3,ty+14,divider-x-14,CompendiumStyle.TEXT_MUTED);
            }
        }context.disableScissor();drawBar(context,leftBar(),divider-5,leftScroll);
        context.enableScissor(right(),top(),x+width-5,top()+bodyHeight());
        for(int i=rightScroll/10;i<details.size()&&i*10-rightScroll<bodyHeight();i++)com.cranks.dungeons.client.guide.GuideInteraction.drawLinked(context,font,details.get(i).text,right(),top()+i*10-rightScroll,details.get(i).color);
        context.disableScissor();drawBar(context,rightBar(),x+width-3,rightScroll);
    }
    private void drawBar(DrawContext context,StatsScrollbar bar,int sx,int scroll){if(bar.maxScroll()==0)return;context.fill(sx,bar.top(),sx+3,bar.top()+bar.height(),0xff090909);int sy=(int)Math.round(bar.thumbTop(scroll));context.fill(sx,sy,sx+3,sy+bar.thumbHeight(),0xff8c8c8c);}
    void tooltip(DrawContext context,int mx,int my){
        if(inside(mx,my,right(),y,x+width-right(),15))context.drawOrderedTooltip(font,font.wrapLines(text("pool_filter_hint"),230),mx,my);
    }
    void click(double mx,double my,int button,Consumer<TextFieldWidget> focus){
        if(button!=0)return;refresh();
        if(inside(mx,my,x,y,divider-x-6,15)){focus.accept(search);search.mouseClicked(mx,my,button);return;}focus.accept(null);
        if(inside(mx,my,right(),y,x+width-right(),15)){selectedOnly=!selectedOnly;dirty=true;leftScroll=0;refresh();return;}
        if(inside(mx,my,divider-7,top(),7,bodyHeight())&&leftBar().maxScroll()>0){grabbed=1;grab=leftBar().grabOffset(my,leftScroll);leftScroll=leftBar().scrollAt(my,grab);return;}
        if(inside(mx,my,x+width-5,top(),5,bodyHeight())&&rightBar().maxScroll()>0){grabbed=2;grab=rightBar().grabOffset(my,rightScroll);rightScroll=rightBar().scrollAt(my,grab);return;}
        if(inside(mx,my,x,top(),divider-x-8,bodyHeight())){int offset=(int)my-top()+leftScroll;for(var row:rows)if(row.entry!=null&&offset>=row.top&&offset<row.top+row.height){selected=row.entry;rebuildDetails();return;}}
    }
    void scroll(double mx,double amount){if(mx<divider)leftScroll=Math.clamp(leftScroll-(int)(amount*20),0,leftBar().maxScroll());else rightScroll=Math.clamp(rightScroll-(int)(amount*20),0,rightBar().maxScroll());}
    boolean drag(double mx,double my,int button,double dx,double dy,Element focused){if(button!=0)return false;if(grabbed==1){leftScroll=leftBar().scrollAt(my,grab);return true;}if(grabbed==2){rightScroll=rightBar().scrollAt(my,grab);return true;}return focused==search&&search.mouseDragged(mx,my,button,dx,dy);}
    boolean release(int button){boolean result=button==0&&grabbed!=0;if(button==0)grabbed=0;return result;}
    private void line(DrawContext context,String value,int sx,int sy,int w,int color){if(font.getWidth(value)>w)value=font.trimToWidth(value,Math.max(0,w-6))+"…";context.drawText(font,value,sx,sy,color,false);}
}

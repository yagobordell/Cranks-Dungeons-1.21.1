package com.cranks.dungeons.client;

import com.cranks.dungeons.block.ModBlocks;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.inventory.ArtificialRunes;
import com.cranks.dungeons.network.ArtificialAltarPayload;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.screen.ArtificialEnhancementAltarScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.inventory.ArtificialRunes.text;

/** Exact stored choices, with no client-side random generation or immediate apply clicks. */
final class ArtificialAltarPanel {
    static final int WIDTH = 300, HEIGHT = 320;
    private record Line(OrderedText text, int color) {}
    private record Card(ArtificialRunes.Option option, List<Line> lines, int top, int height) {}
    private final ArtificialEnhancementAltarScreenHandler altar;
    private final Consumer<ArtificialAltarPayload> reveal;
    private TextRenderer font;
    private int x,y,width,height,scroll,contentHeight,selected=-1,pending;
    private double grab;
    private boolean grabbed;
    private ItemStack lastGear=ItemStack.EMPTY,preview=ItemStack.EMPTY;
    private List<Card> cards=List.of();
    private List<Line> resultLines=List.of();
    private Text feedback;
    private int feedbackColor;
    private long feedbackUntil;
    ArtificialAltarPanel(ArtificialEnhancementAltarScreenHandler altar,Consumer<ArtificialAltarPayload> reveal){this.altar=altar;this.reveal=reveal;}
    void init(TextRenderer font,int x,int y,int width,int height){this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;grabbed=false;rebuild();}
    int top(){return y;}
    int slotX(){return x+12;}
    int slotY(){return y+36;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    private int bodyTop(){return y+78;}
    private StatsScrollbar bar(){return new StatsScrollbar(bodyTop(),height-151,contentHeight);}
    private void refresh(){if(!ItemStack.areEqual(lastGear,altar.equipment())){lastGear=altar.equipment().copy();selected=-1;preview=ItemStack.EMPTY;scroll=0;rebuild();}}
    private void add(List<Line> out,Text message,int color){for(var line:font.wrapLines(message,width-37))out.add(new Line(line,color));}
    private List<Line> choiceLines(ArtificialRunes.Option option){
        var lines=new ArrayList<Line>();
        add(lines,text(option.stat().getAffixType()==com.cranks.dungeons.stat.CustomStat.AffixType.PREFIX?"prefix_tier":"suffix_tier",option.tier()),CompendiumStyle.TEXT_MUTED);
        for(int i=0;i<option.stat().getComponents().size();i++){
            var component=option.stat().getComponents().get(i);
            add(lines,Text.literal(component.formatValue(option.values()[i],option.stat().getEquipmentType())),CompendiumStyle.TEXT);
            add(lines,text("range",component.getFormattedMinMax(option.tier())),CompendiumStyle.TEXT_MUTED);
        }
        return List.copyOf(lines);
    }
    private void rebuild(){
        var next=new ArrayList<Card>();int top=0;
        for(var option:ArtificialRunes.options(altar.equipment())){
            var lines=choiceLines(option);
            int cardHeight=lines.size()*10+12;
            next.add(new Card(option,List.copyOf(lines),top,cardHeight));top+=cardHeight+5;
        }
        cards=List.copyOf(next);resultLines=List.of();
        if(cards.isEmpty())for(var roll:ItemStatManager.getStats(altar.equipment())){
            var stat=com.cranks.dungeons.stat.StatRegistry.getStat(roll.statId);
            if(roll.isArtificial&&stat!=null&&roll.tier>=1&&roll.tier<=10&&roll.values.length==stat.getComponents().size()){
                resultLines=choiceLines(new ArtificialRunes.Option(-1,stat,roll.tier,roll.values));break;
            }
        }
        contentHeight=resultLines.isEmpty()?Math.max(0,top-5):resultLines.size()*10+12;scroll=Math.clamp(scroll,0,bar().maxScroll());
    }
    private int previewY(){return y+height-64;}
    private int actionY(){return y+height-27;}
    void render(DrawContext context,int mx,int my,float delta){
        refresh();CompendiumStyle.drawPanel(context,x,y,width,height);
        context.drawItem(ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR.asItem().getDefaultStack(),x+8,y+7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR.getName(),x+29,y+11,width-110,CompendiumStyle.TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("artificial_tome"));
        InventoryStyle.drawDivider(context,x+8,x+width-8,y+24);
        InventoryStyle.drawSocket(context,slotX()-3,slotY()-3,22,22,inside(mx,my,slotX(),slotY(),16,16));
        var gear=altar.equipment();
        if(gear.isEmpty())StationSlotHints.draw(context,StationSlotHints.Kind.EQUIPMENT,slotX(),slotY());
        line(context,gear.isEmpty()?text("equipment"):gear.getName(),x+38,y+35,width-49,CompendiumStyle.TEXT);
        line(context,ItemStatManager.hasCrypticStat(gear)?text(ItemStatManager.getCrypticAffixType(gear)==com.cranks.dungeons.stat.CustomStat.AffixType.PREFIX?"reserved_prefix":"reserved_suffix"):text(resultLines.isEmpty()?"equipment_hint":"revealed_state"),x+38,y+48,width-49,CompendiumStyle.TEXT_MUTED);
        line(context,text(resultLines.isEmpty()?"choose":"result"),x+10,y+65,width-20,CompendiumStyle.TEXT_MUTED);
        var bar=bar();
        context.enableScissor(x+8,bar.top(),x+width-9,bar.top()+bar.height());
        for(var card:cards){
            int cy=bar.top()+card.top-scroll;
            if(cy+card.height<=bar.top()||cy>=bar.top()+bar.height())continue;
            CompendiumStyle.drawRailPlaque(context,x+9,cy,width-23,card.height,selected==card.option.index(),inside(mx,my,x+9,Math.max(cy,bar.top()),width-23,Math.min(cy+card.height,bar.top()+bar.height())-Math.max(cy,bar.top())));
            int ty=cy+6;for(var row:card.lines){context.drawText(font,row.text,x+15,ty,row.color,false);ty+=10;}
        }
        if(!resultLines.isEmpty()){
            int cy=bar.top()-scroll;
            CompendiumStyle.drawRailPlaque(context,x+9,cy,width-23,contentHeight,true,false);
            for(var row:resultLines){context.drawText(font,row.text,x+15,cy+6,row.color,false);cy+=10;}
        }
        context.disableScissor();
        if(cards.isEmpty()&&resultLines.isEmpty()){
            String reason=ArtificialRunes.reason(gear);
            int ty=bar.top()+5;for(var row:font.wrapLines(text(reason==null?"no_options":reason),width-24)){if(ty+9>bar.top()+bar.height())break;context.drawText(font,row,x+12,ty,CompendiumStyle.TEXT_MUTED,false);ty+=10;}
        }
        if(bar.maxScroll()>0){context.fill(x+width-7,bar.top(),x+width-4,bar.top()+bar.height(),0xff090909);int sy=(int)Math.round(bar.thumbTop(scroll));context.fill(x+width-7,sy,x+width-4,sy+bar.thumbHeight(),0xff939393);}
        InventoryStyle.drawDivider(context,x+8,x+width-8,previewY()-8);
        if(!preview.isEmpty()){
            context.drawItem(preview,x+12,previewY()+1);
            line(context,text("preview"),x+35,previewY(),width-48,CompendiumStyle.TEXT_MUTED);
            var chosen=cards.stream().filter(c->c.option.index()==selected).findFirst().orElse(null);
            if(chosen!=null)line(context,Text.literal(chosen.option.name()),x+35,previewY()+11,width-48,CompendiumStyle.TEXT);
        }else line(context,feedback!=null&&System.currentTimeMillis()<feedbackUntil?feedback:text(resultLines.isEmpty()?"preview_hint":"finished_hint"),x+11,previewY()+7,width-22,feedback!=null&&System.currentTimeMillis()<feedbackUntil?feedbackColor:CompendiumStyle.TEXT_MUTED);
        drawButton(context,text(pending==0?"reveal":"working"),x+10,actionY(),width-20,19,mx,my,pending==0&&!preview.isEmpty());
    }
    void tooltip(DrawContext context,int mx,int my){if(!preview.isEmpty()&&inside(mx,my,x+12,previewY()+1,16,16))context.drawItemTooltip(font,preview,mx,my);}
    void click(double mx,double my,int button){
        if(button!=0||pending!=0)return;refresh();
        if(inside(mx,my,x+10,actionY(),width-20,19)){if(!preview.isEmpty())reveal.accept(new ArtificialAltarPayload(altar.syncId,MaterialCraftingPanel.nextRequest(),altar.revision(),altar.equipment().copy(),selected,false));return;}
        var bar=bar();
        if(inside(mx,my,x+width-9,bar.top(),7,bar.height())&&bar.maxScroll()>0){grabbed=true;grab=bar.grabOffset(my,scroll);scroll=bar.scrollAt(my,grab);return;}
        if(!inside(mx,my,x+9,bar.top(),width-23,bar.height()))return;
        int offset=(int)my-bar.top()+scroll;
        for(var card:cards)if(offset>=card.top&&offset<card.top+card.height){selected=card.option.index();preview=ArtificialRunes.preview(altar.equipment(),selected);feedback=null;return;}
    }
    void scroll(double amount){scroll=Math.clamp(scroll-(int)(amount*20),0,bar().maxScroll());}
    boolean drag(double my,int button){if(button!=0||!grabbed)return false;scroll=bar().scrollAt(my,grab);return true;}
    boolean release(int button){boolean result=button==0&&grabbed;if(button==0)grabbed=false;return result;}
    void send(ArtificialAltarPayload request){if(pending!=0)return;pending=request.request();ClientPlayNetworking.send(request);}
    void receive(CraftMaterialsResultPayload result){
        if(pending==0||pending!=result.request())return;
        pending=0;selected=-1;preview=ItemStack.EMPTY;
        if(result.result()>0)MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(ModSounds.ARTIFICIAL_ALTAR_CLICK,1.0F));
        feedback=text(result.result()>0?"success":result.result()==CraftMaterialsResultPayload.INVENTORY_CHANGED?"stale":"unavailable");
        feedbackColor=result.result()>0?CompendiumStyle.TEXT_SUCCESS:CompendiumStyle.TEXT_WARNING;
        feedbackUntil=System.currentTimeMillis()+5000;refresh();
    }
    private void line(DrawContext context,Text message,int left,int top,int w,int color){String value=message.getString();if(font.getWidth(value)>w)value=font.trimToWidth(value,Math.max(0,w-6))+"…";context.drawText(font,value,left,top,color,false);}
}

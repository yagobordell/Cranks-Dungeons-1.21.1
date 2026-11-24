package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.horse.HorseBreedingFocus;
import com.cranks.dungeons.mount.*;
import com.cranks.dungeons.network.*;
import com.cranks.dungeons.screen.MountCargoScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.text.Text;
import java.util.*;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.HorseInformation.*;

/** Mount gear/cargo and scrollable training, genetics and breeding pages. */
final class MountPanel {
    static final int WIDTH=300,HEIGHT=340;
    private final MountCargoScreenHandler handler;
    private TextRenderer font;
    private int x,y,width,height,tab,cargoRow,infoScroll;
    private boolean grabbed;
    private String menu="";
    private int menuScroll;
    private int menuSelection;
    private List<net.minecraft.entity.player.PlayerEntity> recipients=List.of();
    private double grab;
    private List<net.minecraft.entity.LivingEntity> mates=List.of();
    private UUID mateId;
    private long nextMates,confirmUntil;
    private String confirm="";
    private long blockedAttachmentUntil;
    private record Line(StatLine value,int top,int height){}
    private List<Line> lines=List.of();
    private int contentHeight;
    MountPanel(MountCargoScreenHandler handler){this.handler=handler;}
    static Text text(String key,Object... args){return Text.translatable("mount.inventory.cranks-dungeons."+key,args);}
    void init(TextRenderer font,int x,int y,int width,int height){this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;grabbed=false;}
    int top(){return y;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    private int columns(){return Math.max(1,Math.min(9,handler.capacity()));}
    private int visibleRows(){return Math.max(1,Math.min(5,(height-53-(cargoTop()-y))/18));}
    private int portraitHeight(){return Math.min(87,Math.max(45,height-173));}
    private int cargoTop(){return y+87+portraitHeight();}
    private int gridLeft(){return x+(width-columns()*18)/2;}
    private StatsScrollbar cargoBar(){return new StatsScrollbar(cargoTop(),visibleRows()*18,Math.ceilDiv(handler.activeCapacity(),columns())*18);}
    private StatsScrollbar infoBar(){return new StatsScrollbar(infoTop(),Math.max(18,y+height-(tab==3?83:62)-infoTop()),contentHeight);}
    private int infoTop(){return y+(hasChoiceRow()?78:57);}
    boolean cargoVisible(int index){return tab==0&&handler.hasCargoAccess()&&index<handler.activeCapacity()&&index/columns()>=cargoRow&&index/columns()<cargoRow+visibleRows();}
    int cargoX(int index){return gridLeft()+index%columns()*18;}
    int cargoY(int index){return cargoTop()+(index/columns()-cargoRow)*18;}
    boolean equipmentVisible(int index){var slot=handler.getSlot(index);return tab==0&&(slot.isEnabled()||slot.hasStack());}
    int equipmentX(){return x+width-40;}
    int equipmentY(int index){int rank=0;for(int i=handler.saddleSlotIndex();i<index;i++)if(equipmentVisible(i))rank++;return y+61+rank*27;}
    private String hint(int index){return index==handler.saddleSlotIndex()?"saddle":index==handler.chestSlotIndex()?"chest":handler.mount() instanceof LlamaEntity?"carpet":"armor";}
    private boolean canManage(){var p=MinecraftClient.getInstance().player;var owner=owner();return p!=null&&(p.isCreative()||owner!=null&&owner.equals(p.getUuid()));}
    private boolean isOwner(){var p=MinecraftClient.getInstance().player;return p!=null&&owner()!=null&&owner().equals(p.getUuid());}
    private UUID owner(){var mount=handler.mount();if(mount==null)return null;if(mount instanceof HorseEntity){var d=HorseGeneticsClientData.get(mount.getId());return d==null?null:d.ownerUuid();}var d=MountClientData.get(mount.getId());return d==null?null:d.ownerUuid();}
    private String ownerName(){var mount=handler.mount();if(mount==null)return "Unowned";
        if(mount instanceof HorseEntity){var d=HorseGeneticsClientData.get(mount.getId());return d==null||d.ownerUuid()==null?"Unowned":d.ownerName().isBlank()?"Unknown":d.ownerName();}
        var d=MountClientData.get(mount.getId());return d==null||d.ownerUuid()==null?"Unowned":d.ownerName().isBlank()?"Unknown":d.ownerName();}
    boolean blockAttachmentRemoval(int slot,net.minecraft.screen.slot.SlotActionType action){
        if(slot!=handler.chestSlotIndex()||action==net.minecraft.screen.slot.SlotActionType.CLONE
                ||action==net.minecraft.screen.slot.SlotActionType.QUICK_CRAFT
                ||!handler.getSlot(slot).hasStack()||!handler.attachmentHasContents())return false;
        blockedAttachmentUntil=System.currentTimeMillis()+4000;
        for(int i=0;i<handler.capacity();i++)if(handler.attachmentOwnsCargo(i)&&handler.getSlot(handler.cargoStartIndex()+i).hasStack()){
            cargoRow=i/columns();break;
        }
        return true;
    }
    private MountAccessMode access(boolean storage){var mount=handler.mount();if(mount instanceof HorseEntity){var d=HorseGeneticsClientData.get(mount.getId());return d==null?MountAccessMode.OWNER_ONLY:MountAccessMode.fromId(storage?d.storageAccessMode():d.accessMode());}var d=MountClientData.get(mount.getId());return d==null?MountAccessMode.OWNER_ONLY:MountAccessMode.fromId(storage?d.storageAccessMode():d.accessMode());}
    private boolean hasChoiceRow(){return handler.mount() instanceof HorseEntity&&tab==2
            ||tab==3&&MountBreedingPreview.breedable(MountKind.of(handler.mount()))
            ||handler.mount() instanceof SkeletonHorseEntity&&tab==2&&canSpecialize();}
    private List<Integer> tabs(){return switch(MountKind.of(handler.mount())){
        case PIG -> List.of(0,1);
        case MULE,SKELETON_HORSE -> List.of(0,1,2);
        default -> List.of(0,1,2,3);
    };}
    private Text tabTitle(int value){return text(value==0?"equipment":value==1?"training":value==2&&handler.mount() instanceof SkeletonHorseEntity?"soul_bond":value==2?"genetics":"breeding");}
    private boolean canSpecialize(){var mount=handler.mount();var d=mount==null?null:MountClientData.get(mount.getId());return d!=null&&d.soulBond()>=10&&"None".equals(d.specialization())&&canManage();}
    private HorseBreedingFocus focus(){var mount=handler.mount();var d=mount==null?null:HorseGeneticsClientData.get(mount.getId());return d==null?HorseBreedingFocus.NONE:HorseBreedingFocus.fromId(d.breedingFocus());}
    private net.minecraft.entity.LivingEntity mate(){if(mates.isEmpty())return null;for(var candidate:mates)if(candidate.getUuid().equals(mateId))return candidate;mateId=mates.getFirst().getUuid();return mates.getFirst();}
    private void rebuildInfo(){
        if(handler.mount()==null)return;var values=new ArrayList<StatLine>();
        if(handler.mount() instanceof HorseEntity horse){var d=HorseGeneticsClientData.get(horse.getId());
            if(tab==1)values.addAll(buildStats(horse,d));
            else if(tab==2)values.addAll(buildGenes(d,focus()).stream().filter(line->!line.name.equals("Breeding Focus")).toList());
            else{
                values.addAll(buildFamily(horse,d));
            }
        }else values.addAll(MountInformation.lines(handler,tab));
        if(tab==3&&MountBreedingPreview.breedable(MountKind.of(handler.mount()))){
            if(System.currentTimeMillis()>=nextMates){mates=MountBreedingPreview.nearby(handler.mount());nextMates=System.currentTimeMillis()+1000;}
            var partner=mate();
            if(partner==null)values.add(new StatLine("Pairing Preview","No nearby adult, compatible mounts within 8 blocks",0xff999999));
            else values.addAll(MountBreedingPreview.lines(handler.mount(),partner,mates.indexOf(partner),mates.size()));
        }
        var rows=new ArrayList<Line>();int top=0;for(var value:values){int h=font.wrapLines(Text.literal(value.name+(value.value.isEmpty()?"":": "+value.value)),width-38).size()*10+5+(value.progress>=0?5:0);rows.add(new Line(value,top,h));top+=h;}lines=List.copyOf(rows);contentHeight=top;infoScroll=Math.clamp(infoScroll,0,infoBar().maxScroll());
    }
    void render(DrawContext c,int mx,int my,float delta){
        var mount=handler.mount();CompendiumStyle.drawPanel(c,x,y,width,height);
        line(c,mount==null?text("title"):mount.getDisplayName(),x+11,y+11,width-96,CompendiumStyle.TEXT);
        InventoryStyle.drawDivider(c,x+8,x+width-8,y+25);
        var tabs=tabs();int tabWidth=(width-20)/tabs.size();
        for(int i=0;i<tabs.size();i++)drawButton(c,tabTitle(tabs.get(i)),x+10+i*tabWidth,y+33,tabWidth-2,16,mx,my,true);
        int selected=tabs.indexOf(tab);c.fill(x+12+selected*tabWidth,y+49,x+8+(selected+1)*tabWidth,y+50,0xff999999);
        if(mount!=null){
            if(tab==0){
                InventoryStyle.drawPanel(c,x+12,y+59,width-79,portraitHeight(),false,InventoryStyle.Panel.PORTRAIT);
                c.enableScissor(x+17,y+63,x+width-72,y+portraitHeight()+55);
                InventoryScreen.drawEntity(c,x+20,y+61,x+width-85,y+portraitHeight()+52,Math.max(10,(mount instanceof CamelEntity?22:25)*portraitHeight()/87),0.1f,mx,my,mount);
                c.disableScissor();
                for(int i=handler.saddleSlotIndex();i<handler.cargoStartIndex();i++)if(equipmentVisible(i)){
                    int sy=equipmentY(i);InventoryStyle.drawSocket(c,equipmentX()-3,sy-3,23,23,inside(mx,my,equipmentX(),sy,17,17));if(i!=handler.chestSlotIndex()&&!handler.getSlot(i).hasStack())MountSlotHints.draw(c,hint(i),equipmentX(),sy);
                }
                line(c,text(mount instanceof LlamaEntity?"pack_cargo":"cargo"),x+12,cargoTop()-17,width-24,CompendiumStyle.TEXT_MUTED);
                cargoRow=Math.clamp(cargoRow,0,Math.ceilDiv(cargoBar().maxScroll(),18));
                if(handler.hasCargoAccess()){
                    InventoryStyle.drawPanel(c,gridLeft()-4,cargoTop()-4,columns()*18+7,visibleRows()*18+7,false,InventoryStyle.Panel.STORAGE);
                    for(int i=0;i<handler.capacity();i++)if(cargoVisible(i)){
                        InventoryStyle.drawCell(c,cargoX(i),cargoY(i),true);
                        if(System.currentTimeMillis()<blockedAttachmentUntil&&handler.attachmentOwnsCargo(i)&&handler.getSlot(handler.cargoStartIndex()+i).hasStack())
                            c.drawBorder(cargoX(i)-1,cargoY(i)-1,18,18,CompendiumStyle.TEXT_WARNING);
                    }
                    drawBar(c,cargoBar(),gridLeft()+columns()*18+6,cargoRow*18);
                }else wrapped(c,text("equip_chest"),x+12,cargoTop()+6,width-24,CompendiumStyle.TEXT_MUTED);
                if(System.currentTimeMillis()<blockedAttachmentUntil&&handler.attachmentHasContents())
                    wrapped(c,text("empty_attachment"),x+12,y+height-71,width-24,CompendiumStyle.TEXT_WARNING);
            }else{
                rebuildInfo();
                if(hasChoiceRow()){
                    if(mount instanceof HorseEntity&&tab==2)drawButton(c,text("focus",focus().displayName()),x+11,y+56,width-22,16,mx,my,canManage());
                    else if(tab==3)drawButton(c,text("mate",mate()==null?text("none"):mate().getName()),x+11,y+56,width-22,16,mx,my,mates.size()>1);
                    else for(int i=0;i<3;i++)drawButton(c,Text.literal(confirm.equals(new String[]{"Tidebound","Graveguard","Stormrunner"}[i])&&System.currentTimeMillis()<confirmUntil?"Confirm":new String[]{"Tide","Guard","Storm"}[i]),x+11+i*((width-22)/3),y+56,(width-22)/3-3,16,mx,my,true);
                }
                var bar=infoBar();c.enableScissor(x+11,bar.top(),x+width-14,bar.top()+bar.height());
                for(var row:lines){int sy=bar.top()+row.top-infoScroll;if(sy+row.height<bar.top()||sy>=bar.top()+bar.height())continue;var value=row.value;int textY=sy;for(var text:font.wrapLines(Text.literal(value.name+(value.value.isEmpty()?"":": "+value.value)),width-38)){c.drawText(font,text,x+13,textY,value.color,false);textY+=10;}if(value.progress>=0){c.fill(x+13,textY+1,x+width-26,textY+4,0xff292929);c.fill(x+13,textY+1,x+13+(int)((width-39)*Math.clamp(value.progress,0,1)),textY+4,0xff888888);}}
                c.disableScissor();drawBar(c,bar,x+width-13,infoScroll);
            }
            int permissionWidth=(width-24)/2;
            drawButton(c,text("ride_access",access(false).displayName()),x+11,y+height-46,permissionWidth,16,mx,my,canManage());
            drawButton(c,text("storage_access",access(true).displayName()),x+13+permissionWidth,y+height-46,permissionWidth,16,mx,my,canManage());
            line(c,text("owner",ownerName()),x+12,y+height-22,width-170,CompendiumStyle.TEXT_MUTED);
            if(tab==3){
                var d=mount instanceof HorseEntity?HorseGeneticsClientData.get(mount.getId()):null;var other=d==null?MountClientData.get(mount.getId()):null;
                var mode=MountAccessMode.fromId(d!=null?d.breedingAccessMode():other!=null?other.breedingAccessMode():"");
                drawButton(c,Text.literal("Breeding: "+mode.displayName()),x+11,y+height-67,width-22,16,mx,my,canManage());
            }
            drawButton(c,Text.literal("Transfer"),x+width-146,y+height-27,67,16,mx,my,isOwner());
            drawButton(c,text(confirm.equals("release")&&System.currentTimeMillis()<confirmUntil?"confirm_release":"release"),x+width-77,y+height-27,66,16,mx,my,isOwner());
        }
    }
    void tooltip(DrawContext c,int mx,int my){
        if(tab==0)for(int i=handler.saddleSlotIndex();i<handler.cargoStartIndex();i++)if(equipmentVisible(i)&&!handler.getSlot(i).hasStack()&&inside(mx,my,equipmentX(),equipmentY(i),17,17)){c.drawTooltip(font,text("slot_"+hint(i)),mx,my);return;}
        if(inside(mx,my,x+width-77,y+height-27,66,16))c.drawOrderedTooltip(font,font.wrapLines(text("release_hint"),210),mx,my);
        else if(inside(mx,my,x+width-146,y+height-27,67,16))c.drawOrderedTooltip(font,font.wrapLines(Text.literal("Offer ownership to a nearby player. They must accept. Gear, cargo, training and genes stay with the mount."),230),mx,my);
        else if(inside(mx,my,x+11,y+height-46,width-22,16))c.drawOrderedTooltip(font,font.wrapLines(text("access_hint"),240),mx,my);
        else if(inside(mx,my,x+12,y+height-25,width-126,16))c.drawTooltip(font,text("owner",ownerName()),mx,my);
        else if(handler.mount()!=null&&inside(mx,my,x+11,y+6,width-96,16))c.drawTooltip(font,handler.mount().getDisplayName(),mx,my);
    }
    void click(double mx,double my,int button){
        if(button!=0||handler.mount()==null)return;
        var tabs=tabs();int tabWidth=(width-20)/tabs.size();
        for(int i=0;i<tabs.size();i++)if(inside(mx,my,x+10+i*tabWidth,y+33,tabWidth-2,16)){tab=tabs.get(i);infoScroll=0;grabbed=false;confirm="";return;}
        if(inside(mx,my,x+11,y+height-46,width-22,16)&&canManage()){
            boolean storage=mx>=x+13+(width-24)/2;
            ClientPlayNetworking.send(new SetMountAccessPayload(handler.mount().getId(),access(storage).cycle(1).name(),storage));return;
        }
        if(tab==3&&inside(mx,my,x+11,y+height-67,width-22,16)&&canManage()){
            var mount=handler.mount();var d=mount instanceof HorseEntity?HorseGeneticsClientData.get(mount.getId()):null;var other=d==null?MountClientData.get(mount.getId()):null;
            var mode=MountAccessMode.fromId(d!=null?d.breedingAccessMode():other!=null?other.breedingAccessMode():"");
            ClientPlayNetworking.send(new SetMountAccessPayload(mount.getId(),mode.cycle(1).name(),false,true));return;
        }
        if(inside(mx,my,x+width-146,y+height-27,67,16)&&isOwner()){
            recipients=handler.mount().getWorld().getPlayers().stream().filter(p->p!=MinecraftClient.getInstance().player&&p.isAlive()&&p.squaredDistanceTo(handler.mount())<=64).sorted(Comparator.comparing(p->p.getName().getString())).map(p->(net.minecraft.entity.player.PlayerEntity)p).toList();
            menu="transfer";menuScroll=menuSelection=0;return;
        }
        if(inside(mx,my,x+width-77,y+height-27,66,16)&&isOwner()){
            if(confirm.equals("release")&&System.currentTimeMillis()<confirmUntil){ClientPlayNetworking.send(new ReleaseMountPayload(handler.mount().getId()));confirm="";}else{confirm="release";confirmUntil=System.currentTimeMillis()+4000;}return;
        }
        if(hasChoiceRow()&&inside(mx,my,x+11,y+56,width-22,16)){
            if(tab==3){if(mates.size()>1){var current=mate();mateId=mates.get((mates.indexOf(current)+1)%mates.size()).getUuid();}}
            else if(handler.mount() instanceof HorseEntity horse){
                if(tab==2&&canManage()){menu="focus";menuSelection=focus().ordinal();menuScroll=Math.max(0,menuSelection-menuRows()+1);}
            }else if(canSpecialize()){
                int index=Math.clamp((int)(mx-x-11)/((width-22)/3),0,2);String choice=new String[]{"Tidebound","Graveguard","Stormrunner"}[index];
                if(confirm.equals(choice)&&System.currentTimeMillis()<confirmUntil){ClientPlayNetworking.send(new SetMountSpecializationPayload(handler.mount().getId(),choice));confirm="";}else{confirm=choice;confirmUntil=System.currentTimeMillis()+4000;}
            }return;
        }
        var bar=tab==0?cargoBar():infoBar();int sx=tab==0?gridLeft()+columns()*18+5:x+width-14;
        if(bar.maxScroll()>0&&inside(mx,my,sx,bar.top(),7,bar.height())){grabbed=true;grab=bar.grabOffset(my,tab==0?cargoRow*18:infoScroll);updateDrag(my);}
    }
    private void updateDrag(double my){if(tab==0)cargoRow=Math.clamp((int)Math.round(cargoBar().scrollAt(my,grab)/18.0),0,Math.ceilDiv(cargoBar().maxScroll(),18));else infoScroll=infoBar().scrollAt(my,grab);}
    boolean drag(double my,int button){if(button!=0||!grabbed)return false;updateDrag(my);return true;}
    boolean release(int button){if(button!=0)return false;boolean used=grabbed;grabbed=false;return used;}
    void scroll(double amount){if(tab==0)cargoRow=Math.clamp(cargoRow-(int)amount,0,Math.ceilDiv(cargoBar().maxScroll(),18));else infoScroll=Math.clamp(infoScroll-(int)(amount*24),0,infoBar().maxScroll());}
    private void drawBar(DrawContext c,StatsScrollbar bar,int sx,int scroll){if(bar.maxScroll()>0)CompendiumStyle.drawGuideScrollbar(c,sx,bar.top(),bar.height(),(int)Math.round(bar.thumbTop(scroll)),bar.thumbHeight());}
    private void drawButton(DrawContext c,Text label,int sx,int sy,int w,int h,int mx,int my,boolean active){
        String value=label.getString();if(font.getWidth(value)>w-10)value=font.trimToWidth(value,Math.max(0,w-16))+"…";
        MaterialCraftingPanel.drawButton(c,Text.literal(value),sx,sy,w,h,mx,my,active);
    }
    private void line(DrawContext c,Text text,int sx,int sy,int max,int color){var value=text.getString();if(font.getWidth(value)>max)value=font.trimToWidth(value,Math.max(0,max-6))+"…";c.drawText(font,value,sx,sy,color,false);}
    private void wrapped(DrawContext c,Text text,int sx,int sy,int max,int color){for(var line:font.wrapLines(text,max)){c.drawText(font,line,sx,sy,color,false);sy+=11;}}

    boolean modalOpen(){return !menu.isEmpty();}
    void closeModal(){menu="";}
    private int menuCount(){return menu.equals("focus")?HorseBreedingFocus.values().length:recipients.size();}
    private int menuRows(){return Math.max(1,(height-180)/19);}
    void modalScroll(double amount){menuScroll=Math.clamp(menuScroll-(int)amount,0,Math.max(0,menuCount()-menuRows()));}
    void renderModal(DrawContext c,int mx,int my){
        c.getMatrices().push();c.getMatrices().translate(0,0,700);
        c.fill(x,y,x+width,y+height,0xe6000000);InventoryStyle.drawPanel(c,x+7,y+45,width-14,height-95,false,InventoryStyle.Panel.STORAGE);
        line(c,Text.literal(menu.equals("focus")?"Breeding Focus":"Transfer Ownership"),x+19,y+57,width-38,CompendiumStyle.TEXT);
        wrapped(c,Text.literal(menu.equals("focus")?"Favor a gene in future offspring.":"Choose a player within 8 blocks."),x+19,y+73,width-38,CompendiumStyle.TEXT_MUTED);
        int count=menuCount();if(count==0)wrapped(c,Text.literal("No nearby players."),x+19,y+101,width-38,CompendiumStyle.TEXT_MUTED);
        for(int row=0;row<menuRows()&&row+menuScroll<count;row++){
            int i=row+menuScroll;
            String label=menu.equals("focus")?HorseBreedingFocus.values()[i].displayName()+(HorseBreedingFocus.values()[i]==focus()?"  ✓":""):recipients.get(i).getName().getString();
            drawButton(c,Text.literal(label),x+18,y+96+row*19,width-43,17,mx,my,true);
            if(i==menuSelection)c.drawBorder(x+18,y+96+row*19,width-43,17,CompendiumStyle.TEXT_MUTED);
        }
        var bar=new StatsScrollbar(y+96,menuRows()*19,count*19);drawBar(c,bar,x+width-20,menuScroll*19);
        drawButton(c,Text.literal("Cancel"),x+width-83,y+height-72,65,16,mx,my,true);
        c.getMatrices().pop();
    }
    void modalClick(double mx,double my,int button){
        if(button!=0)return;
        if(inside(mx,my,x+width-83,y+height-72,65,16)){closeModal();return;}
        for(int row=0;row<menuRows()&&row+menuScroll<menuCount();row++)if(inside(mx,my,x+18,y+96+row*19,width-43,17)){
            chooseMenu(row+menuScroll);return;
        }
    }
    private void chooseMenu(int i){
        if(i<0||i>=menuCount())return;
        if(menu.equals("focus")&&canManage())ClientPlayNetworking.send(new SetHorseBreedingFocusPayload(handler.mount().getId(),HorseBreedingFocus.values()[i].name()));
        else if(menu.equals("transfer")&&isOwner())ClientPlayNetworking.send(new MountTransferPayload(handler.mount().getId(),recipients.get(i).getUuid(),MountTransferPayload.OFFER));
        closeModal();
    }
    void modalKey(int key){
        if(key==256){closeModal();return;}
        if(key==257||key==335){chooseMenu(menuSelection);return;}
        if(key==264||key==265){
            menuSelection=Math.clamp(menuSelection+(key==264?1:-1),0,Math.max(0,menuCount()-1));
            menuScroll=Math.clamp(menuScroll,Math.max(0,menuSelection-menuRows()+1),menuSelection);
        }
    }
}

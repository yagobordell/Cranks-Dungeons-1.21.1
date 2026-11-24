package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.compendium.*;
import com.cranks.dungeons.inventory.SupportedContent;
import com.cranks.dungeons.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;

/** The creative catalogue is a dock beside the same player inventory used by every station. */
public final class CreativeCatalogPanel {
    static final int WIDTH=200;
    private static final int COLUMNS=9,TAB_PAGE_SIZE=5,DISCARD_TOP=4;
    private record SearchMatch(ItemStack stack,int rank) {}
    private record Browsing(net.minecraft.util.Identifier group,String query,int scroll,int page,int preset) {}
    private static Browsing browsing;
    private static long generation;
    private final long sessionGeneration=generation;
    private final MinecraftClient client=MinecraftClient.getInstance();
    private TextRenderer font;
    private StyledTextFieldWidget search;
    private List<ItemGroup> groups=List.of();
    private ItemGroup selected;
    private List<ItemStack> items=List.of();
    private CreativeHotbarPresets presets;
    private int preset;
    private boolean loadingPreset;
    private int x,y,width,height,rows,scroll,tabPage,pending,lastMouseX,lastMouseY;
    private boolean dragging,compact,cursorDragged;
    private double grab;
    private Text feedback;
    private long feedbackUntil;
    public static void clearSession(){browsing=null;generation++;CreativeHotbarPresets.clearPending();}
    void remember(){
        if(sessionGeneration==generation&&selected!=null&&search!=null)
            browsing=new Browsing(Registries.ITEM_GROUP.getId(selected),search.getText(),scroll,tabPage,preset);
    }
    void init(TextRenderer font,int x,int y,int width,int height,boolean compact){
        this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;this.compact=compact;
        ItemGroups.updateDisplayContext(client.world.getEnabledFeatures(),client.player.hasPermissionLevel(2),client.world.getRegistryManager());
        groups=ItemGroups.getGroups().stream()
                .filter(g->g.getType()!=ItemGroup.Type.INVENTORY&&(g.shouldDisplay()||g.getType()==ItemGroup.Type.HOTBAR))
                .sorted(Comparator.comparingInt(g->g.getType()==ItemGroup.Type.HOTBAR?2:
                        net.minecraft.util.Identifier.ofVanilla("op_blocks").equals(Registries.ITEM_GROUP.getId(g))?1:0))
                .toList();
        boolean first=search==null;
        if(first&&browsing!=null){selected=Registries.ITEM_GROUP.get(browsing.group());preset=browsing.preset();}
        if(selected==null||!groups.contains(selected))selected=ItemGroups.getDefaultTab();
        String query=first?(browsing==null?"":browsing.query()):search.getText();
        int savedScroll=first?(browsing==null?0:browsing.scroll()):scroll;
        int savedPage=first?(browsing==null?Math.max(0,groups.indexOf(selected)/TAB_PAGE_SIZE):browsing.page()):tabPage;
        if(presets==null)presets=new CreativeHotbarPresets(client);
        rows=Math.max(1,(height-118)/InventoryStyle.SLOT_PITCH);
        search=new StyledTextFieldWidget(font,x+10,y+31,width-20,19,Text.literal("Search creative items"));
        search.setMaxLength(80);search.setPlaceholder(Text.literal("Search items or #tags..."));
        search.setChangedListener(value->{scroll=0;rebuild();});search.setText(query);
        rebuild();scroll=Math.clamp(savedScroll,0,bar().maxScroll());
        tabPage=Math.clamp(savedPage,0,Math.max(0,(groups.size()-1)/TAB_PAGE_SIZE));
    }
    private void rebuild(){
        if(selected==null)return;
        String query=search==null?"":search.getText().strip();
        var source=new ArrayList<ItemStack>();
        if(selected.getType()==ItemGroup.Type.HOTBAR&&query.isEmpty()){
            // Presets have their own ten-slot preview and Save/Load controls.
        }else if(!query.isEmpty())source.addAll(ItemGroups.getSearchGroup().getSearchTabStacks());
        else source.addAll(selected.getDisplayStacks());
        String normalized=MaterialCraftingPanel.searchKey(query.startsWith("#")?query.substring(1):query);
        var terms=normalized.isEmpty()?new String[0]:normalized.split("\\s+");
        var candidates=source.stream().filter(stack->!stack.isEmpty()&&SupportedContent.isSupported(stack)&&!CranksDungeons.shouldRemoveFromCreative(stack));
        if(!normalized.isEmpty()){
            boolean tags=query.startsWith("#"),qualified=normalized.contains(":");
            candidates=candidates.map(stack->new SearchMatch(stack,searchRank(stack,normalized,terms,tags,qualified)))
                    .filter(match->match.rank()!=Integer.MAX_VALUE)
                    .sorted(Comparator.comparingInt(SearchMatch::rank))
                    .map(SearchMatch::stack);
        }
        items=candidates.map(ItemStack::copy).toList();
        scroll=Math.clamp(scroll,0,bar().maxScroll());
    }
    private int searchRank(ItemStack stack,String query,String[] terms,boolean tags,boolean qualified){
        if(tags){
            String tagKeys=stack.streamTags().map(tag->qualified?tag.id().toString():tag.id().getPath())
                    .collect(java.util.stream.Collectors.joining(" "));
            return MaterialCraftingPanel.searchRank("",query,terms,MaterialCraftingPanel.searchKey(tagKeys));
        }
        var id=Registries.ITEM.getId(stack.getItem());
        return MaterialCraftingPanel.searchRank(MaterialCraftingPanel.searchKey(stack.getName().getString()),query,terms,
                MaterialCraftingPanel.searchKey(qualified?id.toString():id.getPath()));
    }
    int top(){return y;}
    int height(){return height;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    private int gridX(){return x+(width-COLUMNS*InventoryStyle.SLOT_PITCH)/2-2;}
    private int gridY(){return y+100;}
    private StatsScrollbar bar(){return new StatsScrollbar(gridY(),Math.max(1,rows)*InventoryStyle.SLOT_PITCH,((items.size()+COLUMNS-1)/COLUMNS)*InventoryStyle.SLOT_PITCH);}
    private int tabX(int index){return x+34+(index%TAB_PAGE_SIZE)*25;}
    private boolean presetView(){return selected.getType()==ItemGroup.Type.HOTBAR&&search.getText().isBlank();}
    private int gridCell(double mx,double my){
        if(presetView())return -1;
        int gx=(int)Math.floor(mx-gridX()),gy=(int)Math.floor(my-gridY());
        if(gx<0||gy<0||gx>=COLUMNS*18||gy>=rows*18||gx%18>=InventoryStyle.CELL_SIZE||gy%18>=InventoryStyle.CELL_SIZE)return -1;
        return gy/18*COLUMNS+gx/18;
    }
    private int hovered(double mx,double my){
        int cell=gridCell(mx,my);if(cell<0)return -1;
        int index=(scroll/18)*COLUMNS+cell;return index<items.size()?index:-1;
    }
    void render(DrawContext context,int mx,int my,float delta){
        lastMouseX=mx;lastMouseY=my;
        CompendiumStyle.drawPanel(context,x,y,width,height);
        context.drawText(font,"Creative Inventory",x+10,y+11,CompendiumStyle.TEXT,false);
        CompendiumButtonWidget.drawButton(context,x+width-28,y+DISCARD_TOP,18,18,inside(mx,my,x+width-28,y+DISCARD_TOP,18,18),pending==0&&!client.player.currentScreenHandler.getCursorStack().isEmpty(),"x");
        InventoryStyle.drawDivider(context,x+8,x+width-8,y+25);
        search.render(context,mx,my,delta);
        CompendiumButtonWidget.drawButton(context,x+9,y+57,18,22,inside(mx,my,x+9,y+57,18,22),tabPage>0,"<");
        CompendiumButtonWidget.drawButton(context,x+width-27,y+57,18,22,inside(mx,my,x+width-27,y+57,18,22),(tabPage+1)*TAB_PAGE_SIZE<groups.size(),">");
        for(int i=tabPage*TAB_PAGE_SIZE;i<Math.min(groups.size(),(tabPage+1)*TAB_PAGE_SIZE);i++){
            int tx=tabX(i);var group=groups.get(i);
            CompendiumStyle.drawRailPlaque(context,tx,y+57,22,22,group==selected,inside(mx,my,tx,y+57,22,22));context.drawItem(group.getIcon(),tx+3,y+60);
        }
        Text caption=search.getText().isBlank()?selected.getDisplayName():Text.literal("Search results");
        context.drawText(font,font.trimToWidth(caption.getString(),width-22),x+10,y+86,CompendiumStyle.TEXT_MUTED,false);
        if(presetView())renderPresets(context,mx,my);
        else {
        int hovered=gridCell(mx,my),first=(scroll/18)*COLUMNS;
        for(int i=0;i<rows*COLUMNS;i++){
            int sx=gridX()+(i%COLUMNS)*18,sy=gridY()+(i/COLUMNS)*18,index=first+i;
            InventoryStyle.drawCell(context,sx,sy,true);
            if(i==hovered)context.fill(sx,sy,sx+InventoryStyle.CELL_SIZE,sy+InventoryStyle.CELL_SIZE,0xFF454545);
            if(index<items.size()){context.drawItem(items.get(index),sx,sy);context.drawItemInSlot(font,items.get(index),sx,sy);}
        }
        if(bar().maxScroll()>0)CompendiumStyle.drawGuideScrollbar(context,x+width-9,gridY(),rows*18,(int)Math.round(bar().thumbTop(scroll)),bar().thumbHeight());
        if(items.isEmpty())context.drawText(font,"No matching items",gridX()+5,gridY()+10,CompendiumStyle.TEXT_MUTED,false);
        }
        String hint=pending!=0?"Working...":feedback!=null&&System.currentTimeMillis()<feedbackUntil?feedback.getString():presetView()?"Ten slots per preset":!client.player.currentScreenHandler.getCursorStack().isEmpty()?"Click a slot to discard held item":"Click: item · Shift: stack";
        context.drawText(font,font.trimToWidth(hint,width-(compact?89:20)),x+10,y+height-17,CompendiumStyle.TEXT_MUTED,false);
    }
    private void renderPresets(DrawContext context,int mx,int my){
        for(int i=0;i<CreativeHotbarPresets.COUNT;i++){
            int px=x+14+i*19;
            CompendiumStyle.drawRailPlaque(context,px,y+100,17,17,i==preset,inside(mx,my,px,y+100,17,17));
            context.drawCenteredTextWithShadow(font,Integer.toString(i+1),px+8,y+105,presets.has(i)?CompendiumStyle.TEXT:CompendiumStyle.TEXT_DISABLED);
        }
        var stacks=presets.get(preset);
        for(int i=0;i<stacks.size();i++){
            int px=x+9+i*18;
            InventoryStyle.drawCell(context,px,y+126,true);
            if(!stacks.get(i).isEmpty()){context.drawItem(stacks.get(i),px,y+126);context.drawItemInSlot(font,stacks.get(i),px,y+126);}
            context.drawCenteredTextWithShadow(font,Integer.toString((i+1)%10),px+8,y+147,CompendiumStyle.TEXT_DISABLED);
        }
        CompendiumButtonWidget.drawButton(context,x+10,y+166,85,18,inside(mx,my,x+10,y+166,85,18),pending==0,"Save");
        CompendiumButtonWidget.drawButton(context,x+width-95,y+166,85,18,inside(mx,my,x+width-95,y+166,85,18),canLoadPreset(),"Load");
        context.drawText(font,"Preset "+(preset+1)+(presets.has(preset)?"":" · Empty"),x+10,y+190,CompendiumStyle.TEXT_MUTED,false);
    }
    private boolean canLoadPreset(){return pending==0&&presets.has(preset)&&client.player.currentScreenHandler.getCursorStack().isEmpty();}
    private void loadPreset(){
        if(!canLoadPreset())return;
        var expected=new ArrayList<ItemStack>();
        for(int i=0;i<com.cranks.dungeons.inventory.PlayerLoadout.HOTBAR_COUNT;i++)expected.add(client.player.getInventory().getStack(com.cranks.dungeons.inventory.PlayerLoadout.getHotbarSlot(i)).copy());
        pending=MaterialCraftingPanel.nextRequest();loadingPreset=true;
        ClientPlayNetworking.send(new CreativeHotbarPayload(pending,presets.get(preset),expected));
    }
    private void feedback(String text){feedback=Text.literal(text);feedbackUntil=System.currentTimeMillis()+3000;}
    void tooltip(DrawContext context,int mx,int my){
        if(presetView()){
            if(inside(mx,my,x+10,y+166,85,18))context.drawTooltip(font,Text.literal("Save all ten hotbar slots to preset "+(preset+1)+". Replaces its previous contents."),mx,my);
            if(inside(mx,my,x+width-95,y+166,85,18))context.drawTooltip(font,Text.literal(!client.player.currentScreenHandler.getCursorStack().isEmpty()?"Put down the held item first":"Replace all ten hotbar slots with this preset."),mx,my);
            if(inside(mx,my,x+9,y+126,180,17)){
                var stack=presets.get(preset).get((mx-x-9)/18);
                if(!stack.isEmpty())context.drawTooltip(font,net.minecraft.client.gui.screen.Screen.getTooltipFromItem(client,stack),mx,my);
            }
        }
        int item=hovered(mx,my);
        if(item>=0){var lines=new ArrayList<>(net.minecraft.client.gui.screen.Screen.getTooltipFromItem(client,items.get(item)));lines.add(Text.literal("Click: take one · Middle-click: full stack").formatted(net.minecraft.util.Formatting.GRAY));lines.add(Text.literal("Shift-click: add a stack to inventory").formatted(net.minecraft.util.Formatting.GRAY));context.drawTooltip(font,lines,mx,my);return;}
        for(int i=tabPage*TAB_PAGE_SIZE;i<Math.min(groups.size(),(tabPage+1)*TAB_PAGE_SIZE);i++)if(inside(mx,my,tabX(i),y+57,22,22))context.drawTooltip(font,groups.get(i).getDisplayName(),mx,my);
        if(inside(mx,my,x+width-28,y+DISCARD_TOP,18,18))context.drawTooltip(font,Text.literal("Discard held item"),mx,my);
    }
    void click(double mx,double my,int button,Consumer<TextFieldWidget> focus){
        if(inside(mx,my,search.frameX(),search.frameY(),search.frameWidth(),search.frameHeight())){focus.accept(search);search.mouseClicked(mx,my,button);return;}
        focus.accept(null);
        if(button==0){
            if(inside(mx,my,x+9,y+57,18,22)&&tabPage>0){tabPage--;return;}
            if(inside(mx,my,x+width-27,y+57,18,22)&&(tabPage+1)*TAB_PAGE_SIZE<groups.size()){tabPage++;return;}
            for(int i=tabPage*TAB_PAGE_SIZE;i<Math.min(groups.size(),(tabPage+1)*TAB_PAGE_SIZE);i++)if(inside(mx,my,tabX(i),y+57,22,22)){selected=groups.get(i);search.setText("");scroll=0;rebuild();return;}
            if(inside(mx,my,x+width-28,y+DISCARD_TOP,18,18)){send(CreativeCatalogPayload.DELETE_CURSOR,ItemStack.EMPTY);return;}
            if(presetView()){
                for(int i=0;i<CreativeHotbarPresets.COUNT;i++)if(inside(mx,my,x+14+i*19,y+100,17,17)){preset=i;return;}
                if(inside(mx,my,x+10,y+166,85,18)&&pending==0)feedback(presets.save(preset)?"Preset "+(preset+1)+" saved":"Could not save preset");
                else if(inside(mx,my,x+width-95,y+166,85,18))loadPreset();
                return;
            }
            if(inside(mx,my,x+width-12,gridY(),9,rows*18)&&bar().maxScroll()>0){dragging=true;grab=bar().grabOffset(my,scroll);scroll=18*(int)Math.round(bar().scrollAt(my,grab)/18.0);return;}
        }
        if(button==0&&gridCell(mx,my)>=0&&!client.player.currentScreenHandler.getCursorStack().isEmpty()){
            send(CreativeCatalogPayload.DELETE_CURSOR,ItemStack.EMPTY);return;
        }
        int item=hovered(mx,my);if(item<0||button!=0&&button!=1&&!client.options.pickItemKey.matchesMouse(button))return;
        send(net.minecraft.client.gui.screen.Screen.hasShiftDown()?CreativeCatalogPayload.GIVE_STACK:client.options.pickItemKey.matchesMouse(button)?CreativeCatalogPayload.TAKE_STACK:button==1&&!client.player.currentScreenHandler.getCursorStack().isEmpty()?CreativeCatalogPayload.REDUCE_CURSOR:CreativeCatalogPayload.TAKE_ONE,items.get(item));
    }
    boolean keyPressed(int key,int scan){
        for(int i=0;i<CreativeHotbarPresets.COUNT;i++){
            if(client.options.hotbarKeys[i].matchesKey(key,scan)&&(client.options.loadToolbarActivatorKey.isPressed()||client.options.saveToolbarActivatorKey.isPressed())){
                preset=i;
                if(client.options.loadToolbarActivatorKey.isPressed())loadPreset();
                else if(pending==0)feedback(presets.save(preset)?"Preset "+(preset+1)+" saved":"Could not save preset");
                return true;
            }
        }
        int index=hovered(lastMouseX,lastMouseY);if(index<0)return false;
        for(int i=0;i<client.options.hotbarKeys.length;i++)if(client.options.hotbarKeys[i].matchesKey(key,scan)){send(10+i,items.get(index));return true;}
        if(ModKeybinds.TENTH_HOTBAR.matchesKey(key,scan)){send(19,items.get(index));return true;}
        if(client.options.pickItemKey.matchesKey(key,scan)){send(CreativeCatalogPayload.TAKE_STACK,items.get(index));return true;}
        return false;
    }
    private void send(int action,ItemStack stack){
        if(pending!=0||!client.player.isCreative())return;
        pending=MaterialCraftingPanel.nextRequest();ClientPlayNetworking.send(new CreativeCatalogPayload(pending,action,stack.copy(),client.player.currentScreenHandler.getCursorStack().copy()));
    }
    boolean drag(double mx,double my,int button,double dx,double dy,Element focused){
        if(button==0&&!dragging&&focused!=search&&!client.player.currentScreenHandler.getCursorStack().isEmpty())cursorDragged=true;
        if(dragging&&button==0){scroll=18*(int)Math.round(bar().scrollAt(my,grab)/18.0);return true;}
        return focused==search&&search.mouseDragged(mx,my,button,dx,dy);
    }
    boolean release(int button){boolean used=dragging;dragging=false;return used;}
    boolean release(double mx,double my,int button){
        if(button==0&&!dragging&&cursorDragged&&gridCell(mx,my)>=0&&!client.player.currentScreenHandler.getCursorStack().isEmpty())send(CreativeCatalogPayload.DELETE_CURSOR,ItemStack.EMPTY);
        cursorDragged=false;return release(button);
    }
    void scroll(double amount){scroll=Math.clamp(scroll-(int)(amount*18),0,bar().maxScroll());}
    void receive(CraftMaterialsResultPayload result){if(result.request()!=pending)return;pending=0;feedback=result.result()>0?(loadingPreset?Text.literal("Preset loaded"):null):Text.literal(result.result()==CraftMaterialsResultPayload.LOCKED?"Put down the held item first":result.result()==CraftMaterialsResultPayload.INVENTORY_CHANGED?"Inventory changed; try again":loadingPreset?"Preset contains unavailable items":"Unavailable or inventory full");loadingPreset=false;feedbackUntil=System.currentTimeMillis()+2500;}
}

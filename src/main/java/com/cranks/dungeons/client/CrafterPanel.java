package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.*;
import com.cranks.dungeons.network.*;
import com.cranks.dungeons.screen.LoadoutCrafterHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;

/** Recipe programming is separate from material storage and never performs a manual craft. */
final class CrafterPanel {
    static final int WIDTH=300,HEIGHT=340;
    private record Row(RecipeEntry<CraftingRecipe> entry,Text name,ItemStack icon,StationRecipeCategories.Category category){}
    private final LoadoutCrafterHandler crafter;
    private final PlayerInventory player;
    private final List<Row> recipes;
    private final RecipeCategoryTabs categories=new RecipeCategoryTabs(StationRecipeCategories.CRAFTING_TABLE);
    private List<Row> filtered=List.of();
    private Row selected;
    private TextRenderer font;
    private TextFieldWidget search;
    private int x,y,width,height,scroll,detailScroll,pending,discoveryRevision=-1;
    private boolean craftable,materialSearch,dirty=true,grabbed,detailGrabbed;
    private double grab;
    private List<ItemStack> last=List.of();
    private Text feedback;
    private long feedbackUntil;
    CrafterPanel(LoadoutCrafterHandler crafter,PlayerInventory player){
        this.crafter=crafter;this.player=player;
        recipes=AutomaticCrafting.recipes(player.player.getWorld()).stream().map(e->new Row(e,MaterialCraftingPanel.recipeName(e,player.player.getWorld()),MaterialCraftingPanel.recipeIcon(e,player.player.getWorld()),StationRecipeCategories.CRAFTING_TABLE.classify(e.value(),MaterialCraftingPanel.recipeIcon(e,player.player.getWorld()))))
                .sorted(Comparator.comparing(r->r.name.getString(),String.CASE_INSENSITIVE_ORDER)).toList();
    }
    static Text text(String key,Object... args){return Text.translatable("crafter.cranks-dungeons."+key,args);}
    void init(TextRenderer font,int x,int y,int width,int height){
        this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;grabbed=false;detailGrabbed=false;
        String query=search==null?"":search.getText();
        search=new TextFieldWidget(font,x+13,y+34,width-26,10,MaterialCraftingPanel.message("search"));search.setDrawsBackground(false);search.setMaxLength(80);search.setChangedListener(v->{dirty=true;scroll=0;});search.setText(query);dirty=true;
    }
    int top(){return y;}
    int slotX(int index){return x+(width-162)/2+index*18;}
    int slotY(){return y+height-78;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    private int listTop(){return y+72+categories.height(width-20);}
    private int listHeight(){return Math.max(18,y+height-123-listTop());}
    private int split(){return x+width/2;}
    private StatsScrollbar listBar(){return new StatsScrollbar(listTop(),listHeight(),filtered.size()*19);}
    private StatsScrollbar detailBar(int count){return new StatsScrollbar(listTop()+31,Math.max(12,listHeight()-31),count*17);}
    private List<ItemStack> sample(Row row){return MaterialCrafting.usesChoices(row.entry.value())?AutomaticCrafting.sample(crafter.materials()):List.of();}
    private MaterialCrafting.Selection selection(Row row){return AutomaticCrafting.selection(crafter.materials(),row.entry,player.player.getWorld(),sample(row));}
    private void refresh(){
        var discoveries=ModComponents.ITEM_DISCOVERIES.get(player.player);
        if(discoveryRevision!=discoveries.revision()){discoveryRevision=discoveries.revision();dirty=true;}
        var next=crafter.snapshot();if(last.size()!=9||java.util.stream.IntStream.range(0,9).anyMatch(i->!ItemStack.areEqual(last.get(i),next.get(i)))){last=next;dirty=true;}
        if(!dirty)return;dirty=false;
        var known=recipes.stream().filter(r->RecipeDiscovery.isDiscovered(r.entry.value(),discoveries)).toList();
        categories.setDiscovered(known.stream().map(Row::category));
        String query=MaterialCraftingPanel.searchKey(search.getText());String[] terms=query.split("\\s+");
        filtered=known.stream().filter(r->categories.includes(r.category)).filter(r->{
            if(query.isEmpty())return true;
            if(!materialSearch)return MaterialCraftingPanel.searchRank(MaterialCraftingPanel.searchKey(r.name.getString()),query,terms,MaterialCraftingPanel.searchKey(r.entry.id().getPath()))!=Integer.MAX_VALUE;
            var test=RecipeDiscovery.materialTest(r.entry.value());
            for(var item:Registries.ITEM)if(discoveries.contains(item)&&test.test(item)&&MaterialCraftingPanel.searchRank(MaterialCraftingPanel.searchKey(item.getName().getString()),query,terms,MaterialCraftingPanel.searchKey(Registries.ITEM.getId(item).getPath()))!=Integer.MAX_VALUE)return true;
            return false;
        }).filter(r->!craftable||AutomaticCrafting.plan(crafter.materials(),r.entry,player.player.getWorld(),sample(r))!=null).toList();
        if(selected==null||!filtered.contains(selected)){selected=filtered.isEmpty()?null:filtered.getFirst();detailScroll=0;}
        scroll=Math.clamp(scroll,0,listBar().maxScroll());
    }
    void render(DrawContext c,int mx,int my,float delta){
        refresh();CompendiumStyle.drawPanel(c,x,y,width,height);
        c.drawItem(Items.CRAFTER.getDefaultStack(),x+8,y+7);com.cranks.dungeons.client.guide.GuideWidgets.header(c,font,Items.CRAFTER.getName(),x+29,y+11,width-110,CompendiumStyle.TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("automation"));
        InventoryStyle.drawDivider(c,x+8,x+width-8,y+24);
        CompendiumStyle.drawTextFieldFrame(c,x+10,y+31,width-20,16,search.isFocused());search.render(c,mx,my,delta);
        drawButton(c,MaterialCraftingPanel.message("available_only"),x+10,y+51,76,15,mx,my,true);
        if(craftable)c.drawBorder(x+11,y+52,74,13,0xff999999);
        drawButton(c,text(materialSearch?"search_materials":"search_items"),x+90,y+51,width-100,15,mx,my,true);
        categories.render(c,x+10,y+70,width-20,mx,my);
        c.fill(split()-1,listTop(),split(),listTop()+listHeight(),0xff444444);
        c.enableScissor(x+9,listTop(),split()-6,listTop()+listHeight());
        for(int i=0;i<filtered.size();i++){
            int top=listTop()+i*19-scroll;if(top+19<=listTop()||top>=listTop()+listHeight())continue;
            var row=filtered.get(i);if(row==selected||inside(mx,my,x+10,top,width/2-18,18))c.fill(x+10,top,split()-7,top+18,row==selected?0xff303030:0xff202020);
            c.drawItem(row.icon,x+12,top+1);line(c,row.name,x+31,top+5,width/2-42,CompendiumStyle.TEXT);
        }
        if(filtered.isEmpty())line(c,text("no_recipes"),x+12,listTop()+7,width/2-24,CompendiumStyle.TEXT_MUTED);
        c.disableScissor();drawBar(c,listBar(),split()-6,scroll);
        drawDetails(c,mx,my);
        var programmed=crafter.programmed();
        line(c,text("programmed",programmed==null?text(crafter.status()==1?"unavailable":"none"):MaterialCraftingPanel.recipeName(programmed,player.player.getWorld())),x+11,y+height-112,width-22,CompendiumStyle.TEXT_MUTED);
        InventoryStyle.drawDivider(c,x+9,x+width-9,y+height-100);
        line(c,text("materials"),x+11,y+height-91,width-22,CompendiumStyle.TEXT_MUTED);
        InventoryStyle.drawPanel(c,slotX(0)-4,slotY()-4,169,25,true,InventoryStyle.Panel.STORAGE);
        for(int i=0;i<9;i++)InventoryStyle.drawCell(c,slotX(i),slotY(),true);
        drawButton(c,text(pending!=0?"programming":"program"),x+11,y+height-52,width-91,17,mx,my,canProgram());
        drawButton(c,text("clear"),x+width-74,y+height-52,63,17,mx,my,pending==0&&crafter.status()!=0);
        Text status=feedback!=null&&System.currentTimeMillis()<feedbackUntil?feedback:text("state_"+crafter.status());
        line(c,status,x+11,y+height-27,width-22,CompendiumStyle.TEXT_MUTED);
        line(c,text(crafter.powered()?"powered":"pulse"),x+11,y+height-14,width-22,CompendiumStyle.TEXT_MUTED);
    }
    private boolean canProgram(){return pending==0&&selected!=null&&(!MaterialCrafting.usesChoices(selected.entry.value())||AutomaticCrafting.plan(crafter.materials(),selected.entry,player.player.getWorld(),sample(selected))!=null);}
    private List<MaterialCrafting.Material> materials(){
        if(selected==null)return List.of();var selection=selection(selected);
        return selection==null?List.of():selection.materials();
    }
    private void drawDetails(DrawContext c,int mx,int my){
        if(selected==null)return;
        int left=split()+7,w=x+width-left-14;
        var selection=selection(selected);ItemStack preview=selection==null?ItemStack.EMPTY:selection.preview();if(preview.isEmpty())preview=selected.icon;
        c.drawItem(preview,left,listTop()+1);line(c,selected.name,left+21,listTop()+2,w-21,CompendiumStyle.TEXT);
        line(c,text("per_pulse",preview.getCount()),left+21,listTop()+13,w-21,CompendiumStyle.TEXT_MUTED);
        var materials=materials();var bar=detailBar(materials.size());detailScroll=Math.clamp(detailScroll,0,bar.maxScroll());
        c.enableScissor(left,bar.top(),x+width-10,bar.top()+bar.height());
        if(materials.isEmpty()){
            var message=text(MaterialCrafting.usesChoices(selected.entry.value())?"variant_hint":"no_materials");int sy=bar.top()+2;
            for(var line:font.wrapLines(message,w-4)){c.drawText(font,line,left,sy,CompendiumStyle.TEXT_MUTED,false);sy+=10;}
        }
        for(int i=0;i<materials.size();i++){
            var m=materials.get(i);int top=bar.top()+i*17-detailScroll;
            if(top+17<=bar.top()||top>=bar.top()+bar.height())continue;
            var icon=m.examples().getFirst();c.drawItem(icon,left,top);
            line(c,Text.literal(m.available()+" / "+m.perCraft()),left+20,top+4,w-26,m.available()>=m.perCraft()?CompendiumStyle.TEXT:CompendiumStyle.TEXT_WARNING);
        }
        c.disableScissor();drawBar(c,bar,x+width-12,detailScroll);
    }
    private void drawBar(DrawContext c,StatsScrollbar bar,int sx,int value){if(bar.maxScroll()>0)CompendiumStyle.drawGuideScrollbar(c,sx,bar.top(),bar.height(),(int)Math.round(bar.thumbTop(value)),bar.thumbHeight());}
    void tooltip(DrawContext c,int mx,int my){
        if(categories.tooltip(c,font,x+10,y+70,width-20,mx,my))return;
        if(inside(mx,my,x+10,listTop(),width/2-18,listHeight())){int i=(my-listTop()+scroll)/19;if(i>=0&&i<filtered.size())c.drawTooltip(font,filtered.get(i).name,mx,my);return;}
        var materials=materials();var bar=detailBar(materials.size());
        if(inside(mx,my,split()+7,bar.top(),width/2-19,bar.height())){int i=(my-bar.top()+detailScroll)/17;if(i>=0&&i<materials.size())c.drawItemTooltip(font,materials.get(i).examples().getFirst(),mx,my);return;}
        if(selected!=null&&MaterialCrafting.usesChoices(selected.entry.value())&&inside(mx,my,x+11,y+height-52,width-91,17))c.drawOrderedTooltip(font,font.wrapLines(text("variant_tooltip"),210),mx,my);
    }
    void click(double mx,double my,int button,Consumer<TextFieldWidget> focus){
        if(button!=0)return;refresh();
        if(inside(mx,my,x+10,y+31,width-20,16)){focus.accept(search);search.mouseClicked(mx,my,button);return;}focus.accept(null);
        if(inside(mx,my,x+10,y+51,76,15)){craftable=!craftable;dirty=true;scroll=0;return;}
        if(inside(mx,my,x+90,y+51,width-100,15)){materialSearch=!materialSearch;dirty=true;scroll=0;return;}
        if(categories.click(x+10,y+70,width-20,mx,my)){dirty=true;scroll=0;return;}
        var bar=listBar();if(bar.maxScroll()>0&&inside(mx,my,split()-7,bar.top(),6,bar.height())){grabbed=true;grab=bar.grabOffset(my,scroll);scroll=bar.scrollAt(my,grab);return;}
        var detail=detailBar(materials().size());if(detail.maxScroll()>0&&inside(mx,my,x+width-14,detail.top(),7,detail.height())){detailGrabbed=true;grab=detail.grabOffset(my,detailScroll);detailScroll=detail.scrollAt(my,grab);return;}
        if(inside(mx,my,x+10,listTop(),width/2-18,listHeight())){int i=(int)(my-listTop()+scroll)/19;if(i<filtered.size()){selected=filtered.get(i);detailScroll=0;feedback=null;}return;}
        if(inside(mx,my,x+11,y+height-52,width-91,17)&&canProgram())send(selected.entry.id().toString());
        else if(inside(mx,my,x+width-74,y+height-52,63,17)&&pending==0&&crafter.status()!=0)send("");
    }
    private void send(String id){pending=MaterialCraftingPanel.nextRequest();feedback=null;ClientPlayNetworking.send(new ProgramCrafterPayload(crafter.syncId,pending,crafter.revision(),id,crafter.snapshot()));}
    void receive(CraftMaterialsResultPayload result){if(pending!=result.request())return;pending=0;feedback=text(result.result()>0?"saved":result.result()==CraftMaterialsResultPayload.LOCKED?"locked":result.result()==CraftMaterialsResultPayload.INVENTORY_CHANGED?"changed":"invalid");feedbackUntil=System.currentTimeMillis()+3500;}
    boolean drag(double mx,double my,int button,double dx,double dy,Element focus){if(button!=0)return false;if(grabbed){scroll=listBar().scrollAt(my,grab);return true;}if(detailGrabbed){detailScroll=detailBar(materials().size()).scrollAt(my,grab);return true;}return focus==search&&search.mouseDragged(mx,my,button,dx,dy);}
    boolean release(int button){if(button!=0)return false;boolean used=grabbed||detailGrabbed;grabbed=detailGrabbed=false;return used;}
    void scroll(double mx,double amount){if(mx>=split())detailScroll=Math.clamp(detailScroll-(int)(amount*20),0,detailBar(materials().size()).maxScroll());else scroll=Math.clamp(scroll-(int)(amount*25),0,listBar().maxScroll());}
    private void line(DrawContext c,Text text,int sx,int sy,int max,int color){String value=text.getString();if(font.getWidth(value)>max)value=font.trimToWidth(value,Math.max(0,max-6))+"…";c.drawText(font,value,sx,sy,color,false);}
}

package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.StorageSortPayload;
import com.cranks.dungeons.screen.LoadoutStorageHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;

final class StoragePanel {
    static final int WIDTH=200;
    private final LoadoutStorageHandler storage;
    private final Text title;
    private TextRenderer font;
    private TextFieldWidget search;
    private String[] terms=new String[0];
    private int x,y,width,height,pending;
    private boolean compact;
    private Text feedback;
    private long feedbackUntil;
    StoragePanel(LoadoutStorageHandler storage,Text title){this.storage=storage;this.title=title;}
    static Text text(String key,Object... args){return Text.translatable("storage.cranks-dungeons."+key,args);}
    int desiredHeight(){return storage.rows()*InventoryStyle.SLOT_PITCH+92;}
    void init(TextRenderer font,int x,int y,int width,boolean compact){
        this.font=font;this.x=x;this.y=y;this.width=width;this.height=desiredHeight();this.compact=compact;
        String query=search==null?"":search.getText();
        search=new TextFieldWidget(font,x+13,y+34,width-54,10,text("search"));search.setDrawsBackground(false);search.setMaxLength(80);search.setPlaceholder(text("search"));
        search.setChangedListener(value->{String normalized=MaterialCraftingPanel.searchKey(value);terms=normalized.isEmpty()?new String[0]:normalized.split("\\s+");});search.setText(query);
    }
    int top(){return y;}
    int slotX(int index){return x+(width-storage.columns()*InventoryStyle.SLOT_PITCH)/2+(index%storage.columns())*InventoryStyle.SLOT_PITCH;}
    int slotY(int index){return y+58+(index/storage.columns())*InventoryStyle.SLOT_PITCH;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    boolean matches(ItemStack stack){if(stack.isEmpty())return false;String key=MaterialCraftingPanel.searchKey(stack.getName().getString()+" "+Registries.ITEM.getId(stack.getItem()));return Arrays.stream(terms).allMatch(key::contains);}
    private boolean canSort(){return pending==0&&storage.getCursorStack().isEmpty();}
    void render(DrawContext context,int mx,int my,float delta){
        CompendiumStyle.drawPanel(context,x,y,width,height);
        context.drawItem(storage.icon(),x+8,y+7);line(context,title,x+29,y+11,width-(compact?110:40),CompendiumStyle.TEXT);
        InventoryStyle.drawDivider(context,x+8,x+width-8,y+24);
        CompendiumStyle.drawTextFieldFrame(context,x+10,y+31,width-46,16,search.isFocused());search.render(context,mx,my,delta);
        int bx=x+width-32,by=y+31;
        CompendiumButtonWidget.drawButton(context,bx,by,22,16,inside(mx,my,bx,by,22,16),canSort(),false,"");
        int color=canSort()?CompendiumStyle.TEXT_MUTED:CompendiumStyle.TEXT_DISABLED;
        context.fill(bx+5,by+4,bx+12,by+5,color);context.fill(bx+5,by+7,bx+10,by+8,color);context.fill(bx+5,by+10,bx+8,by+11,color);
        context.fill(bx+15,by+4,bx+16,by+11,color);context.fill(bx+13,by+9,bx+18,by+10,color);context.fill(bx+14,by+10,bx+17,by+11,color);context.fill(bx+15,by+11,bx+16,by+12,color);
        int matched=0;
        for(int i=0;i<storage.storageSize();i++){
            int sx=slotX(i),sy=slotY(i);boolean match=terms.length>0&&matches(storage.stack(i));if(match)matched++;
            InventoryStyle.drawCell(context,sx,sy,true);
            if(match){context.fill(sx,sy,sx+InventoryStyle.CELL_SIZE,sy+InventoryStyle.CELL_SIZE,0xff3b3b3b);context.drawBorder(sx,sy,InventoryStyle.CELL_SIZE,InventoryStyle.CELL_SIZE,0xff969696);}
        }
        Text status=pending!=0?text("sorting"):feedback!=null&&System.currentTimeMillis()<feedbackUntil?feedback:terms.length>0?text("matches",matched):Text.empty();
        line(context,status,x+11,y+height-19,width-22,CompendiumStyle.TEXT_MUTED);
    }
    void tooltip(DrawContext context,int mx,int my){
        if(inside(mx,my,x+width-32,y+31,22,16))context.drawTooltip(font,text(storage.getCursorStack().isEmpty()?"sort":"cursor"),mx,my);
        else if(inside(mx,my,x+29,y+7,width-40,16))context.drawTooltip(font,title,mx,my);
    }
    void click(double mx,double my,int button,Consumer<TextFieldWidget> focus){
        if(button!=0)return;
        if(inside(mx,my,x+10,y+31,width-46,16)){focus.accept(search);search.mouseClicked(mx,my,button);return;}
        focus.accept(null);
        if(inside(mx,my,x+width-32,y+31,22,16)&&canSort()){
            pending=MaterialCraftingPanel.nextRequest();feedback=null;ClientPlayNetworking.send(new StorageSortPayload(storage.syncId,pending,storage.snapshot()));
        }
    }
    boolean drag(double mx,double my,int button,double dx,double dy,Element focused){return focused==search&&search.mouseDragged(mx,my,button,dx,dy);}
    void receive(CraftMaterialsResultPayload result){if(pending==0||pending!=result.request())return;pending=0;feedback=text(result.result()>0?"sorted":result.result()==CraftMaterialsResultPayload.INVENTORY_CHANGED?"changed":result.result()==CraftMaterialsResultPayload.LOCKED?"cursor":"unavailable");feedbackUntil=System.currentTimeMillis()+3000;}
    private void line(DrawContext context,Text message,int sx,int sy,int w,int color){String value=message.getString();if(font.getWidth(value)>w)value=font.trimToWidth(value,Math.max(0,w-6))+"…";context.drawText(font,value,sx,sy,color,false);}
}

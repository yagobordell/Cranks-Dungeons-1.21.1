package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.client.InventoryStyle;
import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.name.NameTagManager;
import com.cranks.dungeons.network.ApplyNameTagPayload;
import com.cranks.dungeons.network.OpenNameTagScreenPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.UUID;

/** Shared journal-style naming surface, with a server-issued one-use tame invitation. */
public final class NameTagScreen extends Screen {
    private static final int PANEL_WIDTH=300, PANEL_HEIGHT=168;
    private final int entityId;
    private final String initialName;
    private final boolean offHand;
    private final UUID token;
    private final long deadline=System.nanoTime()+300_000_000_000L;
    // Declare the base widget type so InvMove recognizes focused text entry.
    private TextFieldWidget nameField;
    private CompendiumButtonWidget confirmButton;
    private boolean replied;

    public NameTagScreen(int entityId,String initialName,boolean offHand){
        this(entityId,initialName,offHand,OpenNameTagScreenPayload.PAID);
    }
    public NameTagScreen(int entityId,String initialName,boolean offHand,UUID token){
        super(Text.literal(OpenNameTagScreenPayload.PAID.equals(token)?"Name Tag":"Name your companion"));
        this.entityId=entityId;this.initialName=initialName==null?"":initialName;this.offHand=offHand;this.token=token;
    }
    private boolean free(){return !OpenNameTagScreenPayload.PAID.equals(token);}
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext context,int mx,int my,float delta){}
    @Override protected void init(){
        int left=(width-PANEL_WIDTH)/2,top=(height-PANEL_HEIGHT)/2;
        String value=nameField==null?initialName:nameField.getText();
        nameField=new NamingField(textRenderer,left+16,top+70,PANEL_WIDTH-32,22);
        nameField.setMaxLength(NameTagManager.MAX_NAME_LENGTH);
        nameField.setPlaceholder(Text.literal("Enter a name"));
        nameField.setTextPredicate(text->text!=null&&text.codePoints().noneMatch(Character::isISOControl));
        nameField.setChangedListener(ignored->updateConfirmState());
        nameField.setText(value);
        addDrawableChild(nameField);
        addDrawableChild(new CompendiumButtonWidget(left+16,top+130,128,22,
                Text.literal(free()?"Skip":"Cancel"),button->close()));
        confirmButton=addDrawableChild(new CompendiumButtonWidget(left+156,top+130,128,22,
                Text.literal("Confirm"),button->confirm()));
        updateConfirmState();nameField.setCursorToEnd(false);
    }
    @Override protected void setInitialFocus(){if(nameField!=null)setFocused(nameField);else super.setInitialFocus();}
    @Override public void tick(){
        if(client.player==null||!client.player.isAlive()){close();return;}
        if(free()){
            var animal=client.world==null?null:client.world.getEntityById(entityId);
            if(System.nanoTime()>deadline||animal==null||!animal.isAlive()||client.player.squaredDistanceTo(animal)>256)close();
        }
    }
    @Override public void render(DrawContext context,int mx,int my,float delta){
        int left=(width-PANEL_WIDTH)/2,top=(height-PANEL_HEIGHT)/2;
        context.fill(0,0,width,height,0x99000000);
        CompendiumStyle.drawPanel(context,left,top,PANEL_WIDTH,PANEL_HEIGHT);
        context.drawCenteredTextWithShadow(textRenderer,title,width/2,top+14,CompendiumStyle.TEXT);
        InventoryStyle.drawDivider(context,left+16,left+PANEL_WIDTH-16,top+31);
        var animal=client.world==null?null:client.world.getEntityById(entityId);
        if(animal!=null)context.drawCenteredTextWithShadow(textRenderer,
                textRenderer.trimToWidth(animal.getType().getName().getString(),PANEL_WIDTH-32),width/2,top+43,CompendiumStyle.TEXT_MUTED);
        context.drawText(textRenderer,"Name",left+16,top+58,CompendiumStyle.TEXT,false);
        String count=nameField.getText().length()+" / "+NameTagManager.MAX_NAME_LENGTH;
        context.drawText(textRenderer,count,left+PANEL_WIDTH-16-textRenderer.getWidth(count),top+58,CompendiumStyle.TEXT_MUTED,false);
        String hint=free()?"First name · no name tag needed":client.player!=null&&client.player.isCreative()?"Creative naming":"Uses one name tag when confirmed";
        context.drawCenteredTextWithShadow(textRenderer,Text.literal(hint),width/2,top+104,CompendiumStyle.TEXT_MUTED);
        if(free())context.drawCenteredTextWithShadow(textRenderer,Text.literal("Naming later will need a name tag"),width/2,top+116,CompendiumStyle.TEXT_DISABLED);
        super.render(context,mx,my,delta);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if((key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_KP_ENTER)&&confirmButton!=null&&confirmButton.active){confirm();return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    private void updateConfirmState(){if(confirmButton!=null)confirmButton.active=!nameField.getText().strip().isEmpty();}
    private void confirm(){
        if(replied||nameField==null||nameField.getText().strip().isEmpty())return;
        if(ClientPlayNetworking.canSend(ApplyNameTagPayload.ID)){
            replied=true;ClientPlayNetworking.send(new ApplyNameTagPayload(entityId,nameField.getText().strip(),offHand,token));
        }
        close();
    }
    private void cancelFreeName(){
        if(free()&&!replied&&ClientPlayNetworking.canSend(ApplyNameTagPayload.ID)){
            replied=true;ClientPlayNetworking.send(new ApplyNameTagPayload(entityId,"",offHand,token));
        }
    }
    @Override public void close(){cancelFreeName();super.close();}
    @Override public void removed(){cancelFreeName();super.removed();}

    private static final class NamingField extends TextFieldWidget {
        NamingField(TextRenderer renderer,int x,int y,int width,int height){
            super(renderer,x,y,width,height,Text.literal("Name"));setDrawsBackground(false);setEditableColor(CompendiumStyle.TEXT);
        }
        @Override public int getInnerWidth(){return getWidth()-12;}
        @Override public void renderWidget(DrawContext context,int mx,int my,float delta){
            CompendiumStyle.drawTextFieldFrame(context,getX(),getY(),getWidth(),getHeight(),isFocused());
            context.getMatrices().push();context.getMatrices().translate(6,(getHeight()-8)/2f,0);
            super.renderWidget(context,mx,my,delta);context.getMatrices().pop();
        }
        @Override public boolean mouseClicked(double mx,double my,int button){
            if(mx<getX()||mx>=getX()+getWidth()||my<getY()||my>=getY()+getHeight())return false;
            return super.mouseClicked(Math.clamp(mx-6,getX(),getX()+getWidth()-1),Math.clamp(my-(getHeight()-8)/2.0,getY(),getY()+getHeight()-1),button);
        }
    }
}

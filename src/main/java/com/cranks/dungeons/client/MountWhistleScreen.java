package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.network.MountWhistleListPayload;
import com.cranks.dungeons.network.MountWhistlePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import java.util.*;

/** Hold the whistle key, choose an owned mount, then release over a command. */
public final class MountWhistleScreen extends Screen {
    private final InputUtil.Key trigger;
    private List<MountWhistleListPayload.Entry> mounts=List.of();
    private UUID selected;
    private int scroll,highlighted,ticks;
    private boolean received,keyboardChoice;
    private boolean pointerArmed;
    private double initialMouseX,initialMouseY;
    private int left,top,panelWidth,panelHeight,listWidth,cx,cy,radius;
    private MountWhistleWheel wheel;
    public MountWhistleScreen(InputUtil.Key trigger){super(Text.literal("Mount Whistle"));this.trigger=trigger;}
    @Override protected void init(){
        panelWidth=Math.min(410,width-16);panelHeight=Math.min(270,height-16);left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        listWidth=Math.min(145,panelWidth*36/100);cx=left+listWidth+(panelWidth-listWidth)/2;cy=top+panelHeight/2+7;
        radius=Math.max(30,Math.min((panelWidth-listWidth)/2-12,(panelHeight-100)/2));
        wheel=new MountWhistleWheel(radius);
        initialMouseX=client.mouse.getX()*width/client.getWindow().getWidth();initialMouseY=client.mouse.getY()*height/client.getWindow().getHeight();request();
    }
    private void request(){ClientPlayNetworking.send(new MountWhistlePayload(new UUID(0,0),-1));}
    @Override public void tick(){if(++ticks%20==0)request();if(client.player==null||!client.player.isAlive())close();}
    @Override public boolean shouldPause(){return false;}
    public void update(MountWhistleListPayload data){
        mounts=data.mounts();received=true;
        if(mounts.stream().noneMatch(m->m.uuid().equals(selected)))selected=mounts.isEmpty()?null:mounts.getFirst().uuid();
        scroll=Math.clamp(scroll,0,Math.max(0,mounts.size()-rows()));
    }
    private int rows(){return Math.max(1,(panelHeight-90)/24);}
    private MountWhistleListPayload.Entry selected(){return mounts.stream().filter(m->m.uuid().equals(selected)).findFirst().orElse(null);}
    public boolean highlights(Entity entity){var m=selected();return m!=null&&m.entityId()==entity.getId()&&m.uuid().equals(entity.getUuid());}
    private int sector(double mx,double my){
        return wheel.hit(mx-cx,my-cy);
    }
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,0x66000000);CompendiumStyle.drawPanel(c,left,top,panelWidth,panelHeight);
        c.drawCenteredTextWithShadow(textRenderer,title,left+panelWidth/2,top+12,CompendiumStyle.TEXT);
        InventoryStyle.drawDivider(c,left+10,left+panelWidth-10,top+28);
        MaterialCraftingPanel.drawButton(c,Text.literal("×"),left+panelWidth-29,top+7,18,17,mx,my,true);
        c.drawText(textRenderer,"Your mounts",left+13,top+39,CompendiumStyle.TEXT_MUTED,false);
        if(mounts.isEmpty()){
            int y=top+60;for(var line:textRenderer.wrapLines(Text.literal(received?"No owned mounts within 48 blocks.":"Listening…"),listWidth-25)){c.drawText(textRenderer,line,left+13,y,CompendiumStyle.TEXT_MUTED,false);y+=11;}
        }
        for(int row=0;row<rows()&&row+scroll<mounts.size();row++){
            var m=mounts.get(row+scroll);int y=top+54+row*24;boolean chosen=m.uuid().equals(selected);
            c.fill(left+10,y,left+listWidth-7,y+22,chosen?0xff292929:0xff141414);
            if(chosen)c.drawBorder(left+10,y,listWidth-17,22,0xff999999);
            c.drawText(textRenderer,textRenderer.trimToWidth(m.name(),listWidth-27),left+14,y+3,CompendiumStyle.TEXT,false);
            c.drawText(textRenderer,textRenderer.trimToWidth(m.kind()+" · "+Math.round(m.distance())+"m",listWidth-27),left+14,y+13,CompendiumStyle.TEXT_MUTED,false);
            if(MaterialCraftingPanel.inside(mx,my,left+10,y,listWidth-17,22))c.drawTooltip(textRenderer,Text.literal(m.name()),mx,my);
        }
        var bar=new StatsScrollbar(top+54,rows()*24,mounts.size()*24);
        if(bar.maxScroll()>0)CompendiumStyle.drawGuideScrollbar(c,left+listWidth-4,bar.top(),bar.height(),(int)Math.round(bar.thumbTop(scroll*24)),bar.thumbHeight());
        int hover=pointerArmed?sector(mx,my):0;if(!keyboardChoice||hover!=0)highlighted=hover;
        wheel.draw(c,cx,cy,selected()!=null?highlighted:0);
        for(int i=1;i<=3;i++){
            boolean active=selected()!=null;
            double angle=Math.toRadians(-90+(i-1)*120);int x=cx+(int)(Math.cos(angle)*radius*.64),y=cy+(int)(Math.sin(angle)*radius*.64);
            if(i==1){center(c,"Come",x,y-9,active);center(c,"here",x,y+1,active);}else center(c,i==2?"Follow":"Stay",x,y-4,active);
        }
        var selected=selected();
        if(selected!=null){center(c,textRenderer.trimToWidth(selected.name(),panelWidth-listWidth-25),cx,top+42,true);String mode=switch(selected.order()){case "COME" -> "Coming here";case "FOLLOW" -> "Following";case "STAY" -> "Staying";default -> "Roaming";};center(c,mode,cx,cy+radius+8,false);}
        center(c,"Release "+trigger.getLocalizedText().getString()+" over a command",left+panelWidth/2,top+panelHeight-20,false);
        super.render(c,mx,my,delta);
    }
    private void center(DrawContext c,String text,int x,int y,boolean active){c.drawCenteredTextWithShadow(textRenderer,Text.literal(text),x,y,active?CompendiumStyle.TEXT:CompendiumStyle.TEXT_MUTED);}
    private void submit(int action){var m=selected();if(m!=null&&action>0)ClientPlayNetworking.send(new MountWhistlePayload(m.uuid(),action));close();}
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0)return true;
        if(MaterialCraftingPanel.inside(mx,my,left+panelWidth-29,top+7,18,17)){close();return true;}
        for(int row=0;row<rows()&&row+scroll<mounts.size();row++)if(MaterialCraftingPanel.inside(mx,my,left+10,top+54+row*24,listWidth-17,22)){selected=mounts.get(row+scroll).uuid();return true;}
        int action=sector(mx,my);if(action>0)submit(action);return true;
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){scroll=Math.clamp(scroll-(int)vertical,0,Math.max(0,mounts.size()-rows()));return true;}
    @Override public void mouseMoved(double mx,double my){
        if(Math.hypot(mx-initialMouseX,my-initialMouseY)>3)pointerArmed=true;
        int hover=pointerArmed?sector(mx,my):0;if(hover!=0)keyboardChoice=false;if(!keyboardChoice)highlighted=hover;
    }
    @Override public boolean keyReleased(int key,int scan,int modifiers){if(InputUtil.fromKeyCode(key,scan).equals(trigger)){submit(highlighted);return true;}return super.keyReleased(key,scan,modifiers);}
    @Override public boolean mouseReleased(double mx,double my,int button){if(trigger.getCategory()==InputUtil.Type.MOUSE&&trigger.getCode()==button){submit(sector(mx,my));return true;}return true;}
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key==256){close();return true;}
        if(key==257||key==335){submit(highlighted==0?1:highlighted);return true;}
        if(key==263||key==262){keyboardChoice=true;highlighted=1+Math.floorMod((highlighted==0?0:highlighted-1)+(key==262?1:-1),3);return true;}
        if((key==264||key==265)&&!mounts.isEmpty()){
            int at=0;for(int i=0;i<mounts.size();i++)if(mounts.get(i).uuid().equals(selected))at=i;
            at=Math.clamp(at+(key==264?1:-1),0,mounts.size()-1);selected=mounts.get(at).uuid();scroll=Math.clamp(scroll,Math.max(0,at-rows()+1),at);return true;
        }
        return true;
    }
}

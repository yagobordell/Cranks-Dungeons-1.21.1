package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.network.MountTransferOfferPayload;
import com.cranks.dungeons.network.MountTransferPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Waits for existing menus to close instead of interrupting an inventory transaction. */
public final class MountTransferOfferScreen extends Screen {
    private static MountTransferOfferPayload pending;
    private static long deadline;
    private final MountTransferOfferPayload offer;
    private boolean replied;
    private int panelWidth, panelHeight, left, top;
    private static final String DETAILS = "Equipment, cargo, genes and training stay with the mount. Access becomes owner-only. Breeder imprint stays with its breeder.";
    private MountTransferOfferScreen(MountTransferOfferPayload offer){super(Text.literal("Mount ownership"));this.offer=offer;}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(MountTransferOfferPayload.ID,(p,c)->c.client().execute(()->{
            pending=p;deadline=System.nanoTime()+30_000_000_000L;
            if(c.client().currentScreen!=null&&c.client().player!=null)c.client().player.sendMessage(Text.literal(p.ownerName()+" offers you "+p.mountName()+". Close this menu to review the offer within 30 seconds."),true);
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(pending!=null&&(client.player==null||System.nanoTime()>deadline))pending=null;
            if(pending!=null&&client.currentScreen==null){var offer=pending;pending=null;client.setScreen(new MountTransferOfferScreen(offer));}
        });
    }
    @Override protected void init(){
        panelWidth=Math.min(280,width-24);
        int contentHeight=textRenderer.wrapLines(offerText(),panelWidth-30).size()*11
                +textRenderer.wrapLines(Text.literal(DETAILS),panelWidth-30).size()*11;
        panelHeight=Math.min(height-16,Math.max(174,37+contentHeight+8+46));
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        int buttonWidth=(panelWidth-40)/2;
        addDrawableChild(new CompendiumButtonWidget(left+14,top+panelHeight-33,buttonWidth,20,Text.literal("Accept"),b->reply(true)));
        addDrawableChild(new CompendiumButtonWidget(left+26+buttonWidth,top+panelHeight-33,buttonWidth,20,Text.literal("Decline"),b->reply(false)));
    }
    private Text offerText(){return Text.literal(offer.ownerName()+" offers you ownership of "+offer.mountName()+".");}
    private void reply(boolean accept){
        if(!replied){replied=true;ClientPlayNetworking.send(new MountTransferPayload(offer.entityId(),offer.token(),accept?MountTransferPayload.ACCEPT:MountTransferPayload.DECLINE));}
        client.setScreen(null);
    }
    @Override public void close(){reply(false);}
    @Override public boolean shouldPause(){return false;}
    @Override public void tick(){if(System.nanoTime()>deadline)close();}
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,0x99000000);
        CompendiumStyle.drawPanel(c,left,top,panelWidth,panelHeight);
        c.drawCenteredTextWithShadow(textRenderer,title,width/2,top+13,CompendiumStyle.TEXT);
        int y=top+37;
        for(var line:textRenderer.wrapLines(offerText(),panelWidth-30)){c.drawText(textRenderer,line,left+15,y,CompendiumStyle.TEXT,false);y+=11;}
        y+=8;
        for(var line:textRenderer.wrapLines(Text.literal(DETAILS),panelWidth-30)){c.drawText(textRenderer,line,left+15,y,CompendiumStyle.TEXT_MUTED,false);y+=11;}
        super.render(c,mx,my,delta);
    }
}

package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.*;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.economy.EconomyState;
import com.cranks.dungeons.network.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;
import java.util.function.Consumer;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;

/** A station-style dock. Item offers are server-owned references, never client-side transfers. */
final class TradePanel {
    static final int WIDTH=200;
    private final MinecraftClient client=MinecraftClient.getInstance();
    private TextRenderer font;
    private StyledTextFieldWidget amount;
    private int x,y,width,height,scroll;
    private long shownCoins=Long.MIN_VALUE;
    void init(TextRenderer font,int x,int y,int width,int height){
        this.font=font;this.x=x;this.y=y;this.width=width;this.height=height;
        String entered=amount==null?Long.toString(TradeClient.state().yourCoins()):amount.getText();
        amount=new StyledTextFieldWidget(font,x+10,y+129,width-54,18,Text.literal("Coins offered"));amount.setMaxLength(16);amount.setTextPredicate(value->value.matches("[0-9]*"));
        amount.setText(entered);shownCoins=TradeClient.state().yourCoins();
    }
    int top(){return y;}
    int height(){return height;}
    boolean contains(double mx,double my){return inside(mx,my,x,y,width,height);}
    private boolean button(double mx,double my,int px,int py,int w,int h){return inside(mx,my,x+px,y+py,w,h);}
    private List<PlayerEntity> nearby(){
        if(client.player==null||client.world==null)return List.of();
        return client.world.getPlayers().stream().filter(player->player!=client.player&&player.isAlive()&&!player.isSpectator()
            &&player.squaredDistanceTo(client.player)<=Math.pow(ModComponents.WALLET.get(client.player).tradeDistance(),2)&&client.player.canSee(player)).map(player->(PlayerEntity)player).sorted(Comparator.comparing(p->p.getName().getString())).toList();
    }
    private long coins(){try{long coins=Long.parseLong(amount.getText());return coins>=0&&coins<=EconomyState.MAX_COINS?coins:-1;}catch(NumberFormatException error){return -1;}}
    private boolean validCoins(){return coins()>=0&&coins()<=ModComponents.WALLET.get(client.player).balance();}
    private void drawButton(DrawContext context,int px,int py,int w,String text,boolean enabled,int mx,int my){CompendiumButtonWidget.drawButton(context,x+px,y+py,w,18,button(mx,my,px,py,w,18),enabled,text);}
    private void drawCoinOffer(DrawContext context,String label,long coins,int offset){
        String value=WalletDisplay.full(coins);
        int numberX=x+width-10-font.getWidth(value);
        context.drawText(font,font.trimToWidth(label,Math.max(0,numberX-x-18)),x+10,y+offset,CompendiumStyle.TEXT_MUTED,false);
        context.drawText(font,value,numberX,y+offset,CompendiumStyle.TEXT,false);
    }
    void render(DrawContext context,int mx,int my,float delta){
        var state=TradeClient.state();
        CompendiumStyle.drawPanel(context,x,y,width,height);context.drawText(font,"Trade",x+10,y+11,CompendiumStyle.TEXT,false);
        InventoryStyle.drawDivider(context,x+8,x+width-8,y+27);
        if(state.phase()==TradeStatePayload.ACTIVE){
            context.drawText(font,"Click items to offer",x+53,y+12,CompendiumStyle.TEXT_MUTED,false);
            if(!amount.isFocused()&&shownCoins!=state.yourCoins()){amount.setText(Long.toString(state.yourCoins()));shownCoins=state.yourCoins();}
            amount.active=!state.yourReady()&&!TradeClient.pending();amount.setEditableColor(validCoins()?CompendiumStyle.TEXT:CompendiumStyle.TEXT_WARNING);
            context.drawText(font,"You",x+10,y+36,CompendiumStyle.TEXT,false);
            context.drawText(font,font.trimToWidth(state.name(),80),x+110,y+36,CompendiumStyle.TEXT,false);
            for(int side=0;side<2;side++)for(int slot=0;slot<9;slot++){
                int px=x+23+side*100+(slot%3)*18,py=y+52+(slot/3)*18;
                InventoryStyle.drawCell(context,px,py,true);
                var items=side==0?state.yours():state.theirs();var stack=slot<items.size()?items.get(slot):ItemStack.EMPTY;
                if(inside(mx,my,px,py,17,17))context.fill(px,py,px+17,py+17,0xFF454545);
                if(!stack.isEmpty()){context.drawItem(stack,px,py);context.drawItemInSlot(font,stack,px,py);}
            }
            drawCoinOffer(context,"Your coins",state.yourCoins(),115);
            amount.render(context,mx,my,delta);drawButton(context,width-34,129,24,"Set",!state.yourReady()&&!TradeClient.pending()&&validCoins(),mx,my);
            drawCoinOffer(context,"Their coins",state.theirCoins(),152);
            context.drawText(font,status(state.yourReady(),state.yourConfirmed()),x+10,y+168,CompendiumStyle.TEXT_MUTED,false);
            context.drawText(font,status(state.theirReady(),state.theirConfirmed()),x+110,y+168,CompendiumStyle.TEXT_MUTED,false);
            drawButton(context,10,182,85,state.yourReady()?"Edit":"Ready",!TradeClient.pending()&&(state.yourReady()||validCoins()),mx,my);
            drawButton(context,105,182,85,"Confirm",!TradeClient.pending()&&state.yourReady()&&state.theirReady()&&!state.yourConfirmed(),mx,my);
        }else if(state.phase()==TradeStatePayload.INCOMING||state.phase()==TradeStatePayload.OUTGOING){
            context.drawText(font,font.trimToWidth(state.name(),width-20),x+10,y+68,CompendiumStyle.TEXT,false);
            context.drawText(font,state.phase()==TradeStatePayload.INCOMING?"Wants to trade":"Waiting for a response...",x+10,y+87,CompendiumStyle.TEXT_MUTED,false);
            context.drawText(font,"Stay nearby and in sight",x+10,y+111,CompendiumStyle.TEXT_MUTED,false);
            if(state.phase()==TradeStatePayload.INCOMING)drawButton(context,10,139,width-20,"Accept",!TradeClient.pending(),mx,my);
        }else{
            context.drawText(font,"Nearby players",x+10,y+67,CompendiumStyle.TEXT,false);
            var players=nearby();int visible=Math.max(1,(height-128)/25);scroll=Math.clamp(scroll,0,Math.max(0,players.size()-visible));
            for(int i=scroll;i<Math.min(players.size(),scroll+visible);i++)drawButton(context,10,87+(i-scroll)*25,width-20,font.trimToWidth(players.get(i).getName().getString(),width-35),!TradeClient.pending(),mx,my);
            if(players.isEmpty()){context.drawText(font,"Stand near another player",x+10,y+94,CompendiumStyle.TEXT_MUTED,false);context.drawText(font,"with a clear line of sight",x+10,y+109,CompendiumStyle.TEXT_MUTED,false);}
        }
        if(state.phase()!=TradeStatePayload.ACTIVE)drawButton(context,10,40,width-20,"Incoming requests: "+(ModComponents.WALLET.get(client.player).incomingRequests()?"On":"Off"),!TradeClient.pending(),mx,my);
        drawButton(context,10,height-24,70,state.phase()==0?"Back":state.phase()==TradeStatePayload.INCOMING?"Decline":"Cancel",true,mx,my);
    }
    private String status(boolean ready,boolean confirmed){return confirmed?"Confirmed":ready?"Ready":"Editing";}
    void tooltip(DrawContext context,int mx,int my){
        var state=TradeClient.state();
        if(state.phase()!=TradeStatePayload.ACTIVE){
            if(button(mx,my,10,40,width-20,18))context.drawTooltip(font,Text.literal("Allow nearby players to send trade requests"),mx,my);
            return;
        }
        if(inside(mx,my,amount.frameX(),amount.frameY(),amount.frameWidth(),amount.frameHeight())){
            context.drawTooltip(font,Text.literal("Available: "+WalletDisplay.full(ModComponents.WALLET.get(client.player).balance())+" coins"),mx,my);return;
        }
        for(int side=0;side<2;side++)for(int slot=0;slot<9;slot++){
            int px=x+23+side*100+(slot%3)*18,py=y+52+(slot/3)*18;
            var items=side==0?state.yours():state.theirs();if(slot>=items.size()||items.get(slot).isEmpty()||!inside(mx,my,px,py,17,17))continue;
            var lines=new ArrayList<>(net.minecraft.client.gui.screen.Screen.getTooltipFromItem(client,items.get(slot)));
            if(side==0)lines.add(Text.literal("Click: remove offer · Right-click: remove one").formatted(net.minecraft.util.Formatting.GRAY));
            context.drawTooltip(font,lines,mx,my);
        }
    }
    void click(double mx,double my,int button,Consumer<TextFieldWidget> focus){
        var state=TradeClient.state();
        if(state.phase()==TradeStatePayload.ACTIVE&&inside(mx,my,amount.frameX(),amount.frameY(),amount.frameWidth(),amount.frameHeight())&&!state.yourReady()){
            focus.accept(amount);amount.mouseClicked(mx,my,button);return;
        }
        focus.accept(null);
        if(button==0&&button(mx,my,10,height-24,70,18)){TradeClient.close();client.setScreen(new PlayerLoadoutScreen(client.player));return;}
        if(TradeClient.pending())return;
        if(state.phase()!=TradeStatePayload.ACTIVE&&button==0&&button(mx,my,10,40,width-20,18)){TradeClient.send(TradeActionPayload.REQUESTS,0,0,ModComponents.WALLET.get(client.player).incomingRequests()?0:1);return;}
        if(state.phase()==0&&button==0){var players=nearby();int row=((int)my-y-87)/25;int visible=Math.max(1,(height-128)/25);if(my>=y+87&&row>=0&&row<visible&&row+scroll<players.size()&&button(mx,my,10,87+row*25,width-20,18))TradeClient.request(players.get(row+scroll).getUuid());return;}
        if(state.phase()==TradeStatePayload.INCOMING&&button==0&&button(mx,my,10,139,width-20,18)){TradeClient.send(TradeActionPayload.ACCEPT,0,0,0);return;}
        if(state.phase()!=TradeStatePayload.ACTIVE)return;
        if(!state.yourReady()&&(button==0||button==1))for(int slot=0;slot<9;slot++)if(button(mx,my,23+(slot%3)*18,52+(slot/3)*18,17,17)){
            if(slot<state.yours().size()&&!state.yours().get(slot).isEmpty())TradeClient.send(TradeActionPayload.REMOVE,slot,button==1?1:0,0);return;
        }
        if(button!=0)return;
        if(button(mx,my,width-34,129,24,18))submitCoins();
        else if(button(mx,my,10,182,85,18)){if(state.yourReady())TradeClient.send(TradeActionPayload.EDIT,0,0,0);else if(validCoins())TradeClient.send(TradeActionPayload.READY,0,0,coins());}
        else if(button(mx,my,105,182,85,18)&&state.yourReady()&&state.theirReady())TradeClient.send(TradeActionPayload.CONFIRM,0,0,0);
    }
    void submitCoins(){if(!TradeClient.state().yourReady()&&validCoins())TradeClient.send(TradeActionPayload.COINS,0,0,coins());}
    boolean amountFocused(TextFieldWidget field){return field==amount;}
    boolean drag(double mx,double my,int button,double dx,double dy,net.minecraft.client.gui.Element focused){return focused==amount&&amount.mouseDragged(mx,my,button,dx,dy);}
    void scroll(double amount){scroll=Math.max(0,scroll-(int)amount);}
}

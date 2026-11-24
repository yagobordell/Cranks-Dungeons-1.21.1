package com.cranks.dungeons.client;
import com.cranks.dungeons.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class TradeClient {
    private static TradeStatePayload state=TradeStatePayload.idle("");
    private static boolean browsing,pending,cancelOnResponse;
    public static TradeStatePayload state(){return state;}
    public static boolean visible(){return browsing||state.phase()!=0;}
    public static boolean active(){return state.phase()==TradeStatePayload.ACTIVE;}
    public static boolean pending(){return pending;}
    public static void clear(){state=TradeStatePayload.idle("");browsing=pending=cancelOnResponse=false;}
    public static void open(){
        var client=MinecraftClient.getInstance();if(client.player==null)return;
        if(!client.player.currentScreenHandler.getCursorStack().isEmpty()){NotificationHudOverlay.addNotification(net.minecraft.text.Text.literal("Put down the held item before trading"),60);return;}
        browsing=true;
        if(client.player.currentScreenHandler!=client.player.playerScreenHandler)client.player.closeHandledScreen();
        client.setScreen(new PlayerLoadoutScreen(client.player));
    }
    public static void close(){
        if(pending&&state.phase()==0)cancelOnResponse=true;
        if(state.phase()!=0)send(TradeActionPayload.CANCEL,0,0,0);
        browsing=false;
    }
    public static void send(int action,int slot,int count,long coins){
        if(pending&&action!=TradeActionPayload.CANCEL)return;
        pending=true;
        ClientPlayNetworking.send(new TradeActionPayload(action,state.partner(),state.session(),state.revision(),slot,count,coins));
    }
    public static void request(java.util.UUID player){
        if(pending)return;pending=true;
        ClientPlayNetworking.send(new TradeActionPayload(TradeActionPayload.REQUEST,player,TradeActionPayload.NONE,0,0,0,0));
    }
    public static void receive(TradeStatePayload next){
        var client=MinecraftClient.getInstance();var previous=state;
        state=next;pending=false;
        if(cancelOnResponse){
            if(next.phase()!=0){send(TradeActionPayload.CANCEL,0,0,0);browsing=false;return;}
            cancelOnResponse=false;
        }
        if(!next.message().isBlank())NotificationHudOverlay.addNotification(net.minecraft.text.Text.literal(next.message()),60);
        if(next.phase()==TradeStatePayload.INCOMING&&previous.phase()!=TradeStatePayload.INCOMING){
            browsing=true;NotificationHudOverlay.addNotification(net.minecraft.text.Text.literal(next.name()+" wants to trade. Open your inventory to respond."),120);
        }
        if(next.phase()==TradeStatePayload.IDLE&&previous.phase()!=TradeStatePayload.IDLE)browsing=false;
        if(client.player!=null&&client.currentScreen instanceof PlayerLoadoutScreen screen&&screen.getScreenHandler()==client.player.playerScreenHandler
                &&((previous.phase()==0)!=(next.phase()==0)||previous.phase()!=next.phase()))client.setScreen(new PlayerLoadoutScreen(client.player));
    }
}

package com.cranks.dungeons.client;

import com.cranks.dungeons.network.RangedFirePayload;
import com.cranks.dungeons.ranged.RangedRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;

/** Input prediction only. Ammunition, draw validation and projectile creation remain on the server. */
public final class RangedClient {
    private static boolean suppressAttack, wasReady;
    private static boolean reloadUseHeld, crossbowShotPlaying;
    private static Hand pendingCrossbowShot;
    private static int crossbowShotExpires;
    private static int ticks, rearmAt, selectedSlot;
    private static Hand rearmHand;
    private static Item rearmItem;
    private static long shotTime, readyTime;
    private static float kick;
    private RangedClient() {}
    public static void registerTooltips() {
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack,context,type,lines) -> {
            var client=MinecraftClient.getInstance();
            String prefix=stack.getItem() instanceof BowItem ? "ranged.cranks-dungeons.bow" :
                    stack.getItem() instanceof CrossbowItem ? "ranged.cranks-dungeons.crossbow" : null;
            if(prefix==null)return;
            lines.add(net.minecraft.text.Text.translatable(prefix+".aim",client.options.useKey.getBoundKeyLocalizedText()).formatted(net.minecraft.util.Formatting.DARK_GRAY));
            lines.add(net.minecraft.text.Text.translatable(prefix+".fire",client.options.attackKey.getBoundKeyLocalizedText()).formatted(net.minecraft.util.Formatting.DARK_GRAY));
        });
    }
    public static void reset() {
        suppressAttack=false; wasReady=false; rearmHand=null; shotTime=0;
        reloadUseHeld=false; crossbowShotPlaying=false; pendingCrossbowShot=null;
    }
    private static void updateAimLatch(MinecraftClient client) {
        if (!client.options.useKey.isPressed()) reloadUseHeld=false;
        else if (client.player!=null && client.player.isUsingItem()
                && client.player.getActiveItem().getItem() instanceof CrossbowItem
                && !CrossbowItem.isCharged(client.player.getActiveItem())) reloadUseHeld=true;
    }
    /** Optional animation integration consumes actual shots, never aim presses. */
    public static boolean consumeCrossbowShot(Hand hand) {
        if (pendingCrossbowShot!=hand || ticks>crossbowShotExpires) return false;
        pendingCrossbowShot=null;
        return true;
    }
    public static void crossbowShotPlaying(boolean playing) { crossbowShotPlaying=playing; }
    public static Hand firingHand(MinecraftClient client) {
        if(client.player==null || client.currentScreen!=null || client.player.isSpectator())return null;
        var player=client.player;
        if(player.isUsingItem()) {
            var active=player.getActiveItem();
            if(active.getItem() instanceof BowItem || active.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(active)) return player.getActiveHand();
            return null;
        }
        return player.getMainHandStack().getItem() instanceof CrossbowItem && CrossbowItem.isCharged(player.getMainHandStack()) ? Hand.MAIN_HAND : null;
    }
    public static boolean beforeInput(MinecraftClient client) {
        updateAimLatch(client);
        if(!client.options.attackKey.isPressed())suppressAttack=false;
        if(client.player!=null && client.currentScreen==null && client.player.isUsingItem()
                && client.player.getActiveItem().getItem() instanceof CrossbowItem && !CrossbowItem.isCharged(client.player.getActiveItem())) {
            boolean bash=false; while(client.options.attackKey.wasPressed())bash=true;
            if(bash){
                var hand=client.player.getActiveHand();var weapon=client.player.getActiveItem();
                client.interactionManager.stopUsingItem(client.player);queueRearm(client,hand,weapon);
                suppressAttack=false;return true;
            }
        }
        if(firingHand(client)==null)return false;
        boolean pressed=false;
        while(client.options.attackKey.wasPressed())pressed=true;
        if(pressed){suppressAttack=true;fire(client);}
        return false;
    }
    public static boolean suppressAttack() { return suppressAttack; }
    public static boolean deferUse() { return rearmHand!=null; }
    private static void queueRearm(MinecraftClient client,Hand hand,ItemStack weapon) {
        if(client.options.useKey.isPressed()) {
            rearmHand=hand;rearmItem=weapon.getItem();selectedSlot=client.player.getInventory().selectedSlot;rearmAt=ticks+2;
        }
    }
    public static boolean fire(MinecraftClient client) {
        var hand=firingHand(client);
        if(hand==null || client.interactionManager==null)return false;
        suppressAttack=true;
        var player=client.player;
        var weapon=player.getStackInHand(hand);
        if(player.getItemCooldownManager().isCoolingDown(weapon.getItem()))return true;
        boolean bow=weapon.getItem() instanceof BowItem;
        if(bow && player.getItemUseTime()<RangedRules.MIN_BOW_DRAW)return true;
        ClientPlayNetworking.send(new RangedFirePayload(hand,player.getInventory().selectedSlot));
        player.stopUsingItem();
        if(!bow) {
            weapon.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
            pendingCrossbowShot=hand; crossbowShotExpires=ticks+10;
        }
        shotTime=Util.getMeasuringTimeMs(); kick=bow?0.45f:0.75f; wasReady=false;
        queueRearm(client,hand,weapon);
        return true;
    }
    public static void tick(MinecraftClient client) {
        ticks++;
        if(client.player==null || client.world==null){reset();return;}
        updateAimLatch(client);
        if(rearmHand!=null) {
            if(client.currentScreen!=null || !client.options.useKey.isPressed() || client.player.getInventory().selectedSlot!=selectedSlot
                    || client.player.getStackInHand(rearmHand).getItem()!=rearmItem)rearmHand=null;
            else if(ticks>=rearmAt && !crossbowShotPlaying && !client.player.getItemCooldownManager().isCoolingDown(rearmItem)) {
                if(!client.player.isUsingItem() && client.interactionManager!=null)client.interactionManager.interactItem(client.player,rearmHand);
                rearmHand=null;
            }
        }
        boolean ready=client.player.isUsingItem() && client.player.getActiveItem().getItem() instanceof BowItem && client.player.getItemUseTime()>=20;
        if(ready&&!wasReady){readyTime=Util.getMeasuringTimeMs();client.player.playSound(SoundEvents.BLOCK_TRIPWIRE_CLICK_ON,0.13f,1.6f);}
        wasReady=ready;
    }
    public static float recoil() {
        double t=(Util.getMeasuringTimeMs()-shotTime)/190.0;
        return t>=0&&t<1 ? (float)(kick*Math.sin(Math.PI*t)*(1-t)) : 0;
    }
    public static boolean readyFlash() { return Util.getMeasuringTimeMs()-readyTime<180; }
    public static boolean aimingLoadedCrossbow(MinecraftClient client) {
        return !reloadUseHeld && client.player!=null && client.currentScreen==null && client.options.useKey.isPressed()
                && client.player.isUsingItem() && client.player.getActiveItem().getItem() instanceof CrossbowItem
                && CrossbowItem.isCharged(client.player.getActiveItem());
    }
}

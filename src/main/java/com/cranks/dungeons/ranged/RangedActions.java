package com.cranks.dungeons.ranged;

import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public final class RangedActions {
    private static final ThreadLocal<ServerPlayerEntity> FIRING_BOW = new ThreadLocal<>();
    private RangedActions() {}
    public static boolean firingBow(Object user) { return FIRING_BOW.get() == user; }
    public static boolean fire(ServerPlayerEntity player, Hand hand, int selectedSlot) {
        if (!player.isAlive() || player.isSpectator() || player.currentScreenHandler != player.playerScreenHandler
                || player.getInventory().selectedSlot != selectedSlot) return false;
        var weapon = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(weapon.getItem())) return false;
        if (weapon.getItem() instanceof BowItem) {
            if (!player.isUsingItem() || player.getActiveHand() != hand || player.getActiveItem() != weapon
                    || !((RangedUse) player).cranks$matchesRangedUse(weapon, hand, selectedSlot)
                    || player.getItemUseTime() < RangedRules.MIN_BOW_DRAW) return false;
            if (!player.isCreative() && player.getProjectileType(weapon).isEmpty()) return false;
            var previous = FIRING_BOW.get(); FIRING_BOW.set(player);
            try { player.stopUsingItem(); }
            finally { if (previous == null) FIRING_BOW.remove(); else FIRING_BOW.set(previous); }
        } else if (weapon.getItem() instanceof CrossbowItem crossbow && CrossbowItem.isCharged(weapon)) {
            if (player.isUsingItem() && player.getActiveHand() != hand) return false;
            player.stopUsingItem();
            var ammunition = weapon.get(net.minecraft.component.DataComponentTypes.CHARGED_PROJECTILES);
            float velocity = ammunition != null && ammunition.contains(net.minecraft.item.Items.FIREWORK_ROCKET) ? 1.6f : 3.15f;
            crossbow.shootAll(player.getWorld(), player, hand, weapon, velocity, 1.0f, null);
        } else return false;
        player.getItemCooldownManager().set(weapon.getItem(), 2);
        player.getInventory().markDirty(); player.playerScreenHandler.sendContentUpdates();
        return true;
    }
}

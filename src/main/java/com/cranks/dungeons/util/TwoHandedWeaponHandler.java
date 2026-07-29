package com.cranks.dungeons.util;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class TwoHandedWeaponHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (hand == Hand.OFF_HAND) {
                if (CombatUtility.isTwoHandedWeapon(player.getMainHandStack()) || CombatUtility.isTwoHandedWeapon(player.getStackInHand(Hand.OFF_HAND))) {
                    return net.minecraft.util.TypedActionResult.fail(player.getStackInHand(hand));
                }
            }
            return net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand == Hand.OFF_HAND) {
                if (CombatUtility.isTwoHandedWeapon(player.getMainHandStack()) || CombatUtility.isTwoHandedWeapon(player.getStackInHand(Hand.OFF_HAND))) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.OFF_HAND) {
                if (CombatUtility.isTwoHandedWeapon(player.getMainHandStack()) || CombatUtility.isTwoHandedWeapon(player.getStackInHand(Hand.OFF_HAND))) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
    }
}

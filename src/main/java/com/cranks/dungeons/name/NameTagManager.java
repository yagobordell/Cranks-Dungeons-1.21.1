package com.cranks.dungeons.name;

import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.network.ApplyNameTagPayload;
import com.cranks.dungeons.network.OpenNameTagScreenPayload;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** Coordinates the client naming screen and validates every name-tag action on the server. */
public final class NameTagManager {
    public static final int MAX_NAME_LENGTH = 64;

    private NameTagManager() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!isNameableTarget(target) || !player.getStackInHand(hand).isOf(Items.NAME_TAG)) {
                return ActionResult.PASS;
            }

            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                String currentName = target.getCustomName() == null
                        ? "" : limit(target.getCustomName().getString());
                ServerPlayNetworking.send(serverPlayer,
                        new OpenNameTagScreenPayload(target.getId(), currentName, hand == Hand.OFF_HAND));
            }
            return ActionResult.SUCCESS;
        });
    }

    public static void applyName(ServerPlayerEntity player, ApplyNameTagPayload payload) {
        String name = normalize(payload.name());
        if (!isValidName(name)) return;

        Entity entity = player.getEntityWorld().getEntityById(payload.entityId());
        if (!isNameableTarget(entity)
                || !player.canInteractWithEntity(entity, 4.0)) return;

        Hand hand = payload.offHand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack held = player.getStackInHand(hand);
        if (!held.isOf(Items.NAME_TAG)) return;

        LivingEntity living = (LivingEntity) entity;
        living.setCustomName(Text.literal(name));
        if (entity instanceof MobEntity mob) mob.setPersistent();

        // Naming is also the ownership claim for camels, pigs, and striders.
        MountManager.claimNamedMount(player, living);
        if (!player.isCreative()) held.decrement(1);
    }

    private static boolean isNameableTarget(Entity entity) {
        return entity instanceof LivingEntity living
                && !(entity instanceof PlayerEntity)
                && living.isAlive();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private static boolean isValidName(String name) {
        return !name.isEmpty()
                && name.length() <= MAX_NAME_LENGTH
                && name.codePoints().noneMatch(Character::isISOControl);
    }

    private static String limit(String value) {
        return value.length() <= MAX_NAME_LENGTH
                ? value : value.substring(0, MAX_NAME_LENGTH);
    }
}

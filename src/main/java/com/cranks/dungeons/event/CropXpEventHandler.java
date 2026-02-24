package com.cranks.dungeons.event;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CropXpEventHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return;

            // Only vanilla CropBlock (wheat, carrots, potatoes, beetroot)
            if (!(state.getBlock() instanceof CropBlock crop)) return;

            // Only if fully grown
            if (!crop.isMature(state)) return;

            // Only when breaking with a hoe
            ItemStack tool = player.getMainHandStack();
            if (!tool.isIn(ItemTags.HOES)) return;

            // Read Crop XP flat value directly from the hoe's rune stats
            double xpAmount = EquipmentStatApplier.getItemStatValue(tool, "crop_experience");
            if (xpAmount <= 0.0) return;

            // Integer part = guaranteed XP
            int baseXp = (int) Math.floor(xpAmount);
            double frac = xpAmount - baseXp;

            int xp = baseXp;
            // Fractional part = chance for +1 XP
            if (frac > 0.0 && player.getRandom().nextDouble() < frac) {
                xp++;
            }

            if (xp <= 0) return;

            ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(pos), xp);
        });
    }
}
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MiningEfficiencyMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void applyMiningEfficiency(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Get mining efficiency from the tool being used
        ItemStack tool = player.getMainHandStack();
        
        // Total mining efficiency is now the absolute speed (e.g., 8.0 for Diamond)
        double efficiency = player.getAttributeValue(ModAttributes.MINING_EFFICIENCY);
        
        // Add rune value from the item itself (multiplied as percentage)
        double runePercent = com.cranks.dungeons.equipment.ItemStatManager.getTotalStatValue(tool, "mining_efficiency");
        float finalSpeed = (float) (efficiency * (1.0 + runePercent));

        if (finalSpeed > 0) {
            // Only apply if the tool is actually valid for the block (vanilla speed > 1.0)
            if (cir.getReturnValue() > 1.0f) {
                cir.setReturnValue(finalSpeed);
            }
        }
    }
}
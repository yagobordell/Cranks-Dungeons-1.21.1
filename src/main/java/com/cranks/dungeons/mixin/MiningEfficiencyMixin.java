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
        double efficiencyBonus = ToolStatHelper.getMiningEfficiency(tool);
        efficiencyBonus += player.getAttributeValue(ModAttributes.MINING_EFFICIENCY);

        if (efficiencyBonus > 0) {
            float currentSpeed = cir.getReturnValue();
            float newSpeed = currentSpeed * (float) (1.0 + efficiencyBonus);
            cir.setReturnValue(newSpeed);
        }
    }
}
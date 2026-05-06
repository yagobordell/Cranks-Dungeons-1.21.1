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
        
        // Final attribute value includes base (1.0), item base (e.g. 11.0), 
        // and all multipliers from runes/enchantments (e.g. 250% = 3.5x).
        double efficiency = player.getAttributeValue(ModAttributes.MINING_EFFICIENCY);
        float vanillaSpeed = cir.getReturnValue();

        // If vanilla thinks it's not a valid tool for the block, it returns 1.0 or lower.
        // We only want to override if it's a valid mining operation.
        if (vanillaSpeed > 1.0f) {
            float finalSpeed = (float) efficiency;
            
            // Uncomment for debugging if speed still feels wrong
            // System.out.println("DEBUG: [Mining] Vanilla: " + vanillaSpeed + " -> RPG: " + finalSpeed + " (Attr: " + efficiency + ")");
            
            cir.setReturnValue(finalSpeed);
        }
    }
}
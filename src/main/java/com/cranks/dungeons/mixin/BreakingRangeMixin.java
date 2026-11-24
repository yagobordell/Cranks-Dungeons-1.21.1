package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class BreakingRangeMixin {

    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void modifyBreakingRange(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        double baseRange = cir.getReturnValue();

        ItemStack tool = player.getMainHandStack();

        // 1) Prefer the attribute value (already includes modifiers from runes, etc.)
        double rangeBonus = 0.0;
        var inst = player.getAttributeInstance(ModAttributes.BREAKING_RANGE);
        if (inst != null) {
            rangeBonus = inst.getValue();
        }

        // 2) If attribute isn't wired for some reason, fall back to the raw tool stat
        if (rangeBonus <= 0.0) {
            rangeBonus = ToolStatHelper.getBreakingRange(tool);
        }

        if (rangeBonus != 0.0) {
            cir.setReturnValue(baseRange + com.cranks.dungeons.gathering.GatheringRules.bounded(rangeBonus,2));
        }
    }
}
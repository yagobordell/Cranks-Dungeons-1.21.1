package com.cranks.dungeons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class MiningEfficiencyMixin {
    @Redirect(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean statusHasteUsesMiningAttribute(LivingEntity entity) {
        return false;
    }

    @Redirect(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean statusFatigueUsesMiningAttribute(PlayerEntity player, RegistryEntry<StatusEffect> effect) {
        return !effect.equals(StatusEffects.MINING_FATIGUE) && player.hasStatusEffect(effect);
    }

    @WrapMethod(method = "getBlockBreakingSpeed")
    private float applyMiningEfficiency(BlockState block, Operation<Float> original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Final attribute value includes base (1.0), item base (e.g. 11.0), 
        // and all multipliers from runes/enchantments (e.g. 250% = 3.5x).
        double efficiency = player.getAttributeValue(ModAttributes.MINING_EFFICIENCY);
        float vanillaSpeed = original.call(block);

        // If vanilla thinks it's not a valid tool for the block, it returns 1.0 or lower.
        // We only want to override if it's a valid mining operation.
        float toolSpeed = player.getInventory().getBlockBreakingSpeed(block);
        if (toolSpeed > 1.0f) {
            return (float) (efficiency * vanillaSpeed / toolSpeed);
        } else {
            var haste = player.getAttributeInstance(ModAttributes.MINING_EFFICIENCY)
                    .getModifier(com.cranks.dungeons.effect.StatusEffectRules.HASTE.modifierId);
            var fatigue = player.getAttributeInstance(ModAttributes.MINING_EFFICIENCY)
                    .getModifier(com.cranks.dungeons.effect.StatusEffectRules.MINING_FATIGUE.modifierId);
            double multiplier = (haste == null ? 1 : 1 + haste.value()) * (fatigue == null ? 1 : 1 + fatigue.value());
            return (float) (vanillaSpeed * multiplier);
        }
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class WitherResistanceReductionMixin {

    @Unique
    private static final Identifier WITHER_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "wither_resistance_reduction");

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyWitherResistanceReduction(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getEntityWorld().isClient()) return;

        StatusEffectInstance witherEffect = entity.getStatusEffect(StatusEffects.WITHER);
        int currentWitherLevel = witherEffect != null ? witherEffect.getAmplifier() + 1 : 0;
        int previousWitherLevel = com.cranks.dungeons.util.WitherResistanceTracker.getLevel(entity.getUuid());

        // Only update if wither level changed
        if (currentWitherLevel != previousWitherLevel) {
            updateResistances(entity, currentWitherLevel);
            com.cranks.dungeons.util.WitherResistanceTracker.setLevel(entity.getUuid(), currentWitherLevel);
        }

        // Clear tracking when wither ends
        if (currentWitherLevel == 0 && previousWitherLevel > 0) {
            com.cranks.dungeons.util.WitherResistanceTracker.clearEntity(entity.getUuid());
        }
    }

    @Unique
    private void updateResistances(LivingEntity entity, int witherLevel) {
        // Wither I (level 1) = -35%, Wither II (level 2) = -50%
        double resistanceReduction;
        if (witherLevel == 1) {
            resistanceReduction = -0.35;
        } else if (witherLevel >= 2) {
            resistanceReduction = -0.50;
        } else {
            resistanceReduction = 0.0;
        }

        // Apply to all elemental resistances
        applyResistanceModifier(entity, ModAttributes.FIRE_RESISTANCE, resistanceReduction);
        applyResistanceModifier(entity, ModAttributes.COLD_RESISTANCE, resistanceReduction);
        applyResistanceModifier(entity, ModAttributes.LIGHTNING_RESISTANCE, resistanceReduction);
        applyResistanceModifier(entity, ModAttributes.VOID_RESISTANCE, resistanceReduction);
    }

    @Unique
    private void applyResistanceModifier(LivingEntity entity, 
                                         net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                         double modifierValue) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            // Remove existing wither modifier if present
            instance.removeModifier(WITHER_MODIFIER_ID);

            // Apply new modifier if wither is active
            if (modifierValue < 0) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                    WITHER_MODIFIER_ID,
                    modifierValue,
                    EntityAttributeModifier.Operation.ADD_VALUE
                );
                instance.addTemporaryModifier(modifier);
            }
        }
    }
}

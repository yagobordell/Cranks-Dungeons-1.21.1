package com.cranks.dungeons.effect;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

public class StarvingStatusEffect extends StatusEffect {
    public StarvingStatusEffect() {
        // Dark green/brown color for Starving
        super(StatusEffectCategory.HARMFUL, 0x4B5320);
        this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(CranksDungeons.MOD_ID, "starving_movement_speed"), -0.10, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Identifier.of(CranksDungeons.MOD_ID, "starving_attack_speed"), -0.10, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}

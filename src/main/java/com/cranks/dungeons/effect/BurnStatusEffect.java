package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BurnStatusEffect extends StatusEffect {
    public BurnStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF4500);
    }
}

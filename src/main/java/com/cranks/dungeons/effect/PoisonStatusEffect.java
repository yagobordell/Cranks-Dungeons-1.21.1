package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.text.Text;

public class PoisonStatusEffect extends StatusEffect {
    public PoisonStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x006400);
    }
}

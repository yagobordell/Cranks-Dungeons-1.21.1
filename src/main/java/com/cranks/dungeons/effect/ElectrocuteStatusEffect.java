package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ElectrocuteStatusEffect extends StatusEffect {
    public ElectrocuteStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFFF00); // Yellow/gold color
    }
}

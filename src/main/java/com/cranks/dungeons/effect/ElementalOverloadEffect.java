package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ElementalOverloadEffect extends StatusEffect {
    public ElementalOverloadEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00FFFF);
    }
}

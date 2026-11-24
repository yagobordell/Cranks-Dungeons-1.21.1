package com.cranks.dungeons.util;

import com.cranks.dungeons.effect.StatusEffectRules;
import com.cranks.dungeons.effect.PotionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public final class StatusEffectStatManager {
    private StatusEffectStatManager() {}

    public static void updateEntityStatusEffectModifiers(LivingEntity entity) {
        if (entity == null || entity.getWorld().isClient()) return;
        for (var rule : StatusEffectRules.values()) {
            // Clean native modifiers retained in worlds saved before the rework.
            var nativeAttribute = rule.nativeAttribute();
            if (nativeAttribute != null) {
                var nativeInstance = entity.getAttributeInstance(nativeAttribute);
                if (nativeInstance != null) nativeInstance.removeModifier(rule.nativeModifierId());
            }
            var attribute = entity.getAttributeInstance(rule.attribute);
            if (attribute == null) continue;
            StatusEffectInstance effect = entity.getStatusEffect(rule.effect);
            if (rule == StatusEffectRules.HASTE) {
                var conduit = entity.getStatusEffect(StatusEffects.CONDUIT_POWER);
                if (conduit != null && (effect == null || rule.amount(conduit.getAmplifier()) > rule.amount(effect.getAmplifier()))) {
                    effect = conduit;
                }
            }
            double value = effect == null ? 0 : rule.modifier(effect.getAmplifier());
            EntityAttributeModifier existing = attribute.getModifier(rule.modifierId);
            if (existing != null && existing.value() == value && existing.operation() == rule.operation) continue;
            attribute.removeModifier(rule.modifierId);
            if (value != 0) attribute.addTemporaryModifier(new EntityAttributeModifier(rule.modifierId, value, rule.operation));
        }
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    public static void applyInstantHealth(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (target == null) return;

        double effectiveProximity = Math.clamp(proximity, 0.0, 1.0);
        if (effectiveProximity <= 0) return;

        if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
            // Damages undead with Magic/Void damage
            float baseDamage = StatusEffectRules.instantHealth(amplifier);
            float actualDamage = (float) (baseDamage * effectiveProximity);
            DamageSource dmgSource = PotionRules.damageSource(target, source, attacker);
            target.damage(dmgSource, actualDamage);
        } else {
            // Heals living entity with flat HP
            float baseHeal = StatusEffectRules.instantHealth(amplifier);
            float actualHeal = (float) (baseHeal * effectiveProximity);
            float before=target.getHealth();
            target.heal(actualHeal);
            if(target.getHealth()>before && attacker instanceof net.minecraft.entity.player.PlayerEntity helper)
                com.cranks.dungeons.progression.CombatExperience.support(helper,target);
        }
    }

    /**
     * Handles Instant Damage (Harming) application with ~15 flat Void damage.
     */
    public static void applyInstantDamage(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (target == null) return;

        double effectiveProximity = Math.clamp(proximity, 0.0, 1.0);
        if (effectiveProximity <= 0) return;

        if (target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) {
            // Heals undead
            float baseHeal = StatusEffectRules.instantDamage(amplifier);
            float actualHeal = (float) (baseHeal * effectiveProximity);
            float before=target.getHealth();
            target.heal(actualHeal);
            if(target.getHealth()>before && attacker instanceof net.minecraft.entity.player.PlayerEntity helper)
                com.cranks.dungeons.progression.CombatExperience.support(helper,target);
        } else {
            // Deals flat Void Damage to living entities
            float baseDamage = StatusEffectRules.instantDamage(amplifier);
            float actualDamage = (float) (baseDamage * effectiveProximity);
            DamageSource dmgSource = PotionRules.damageSource(target, source, attacker);
            target.damage(dmgSource, actualDamage);
        }
    }
}

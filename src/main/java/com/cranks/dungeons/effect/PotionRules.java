package com.cranks.dungeons.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Map;

public final class PotionRules {
    public static final int CLOUD_DURATION = 160;
    public static final int CLOUD_WAIT = 10;
    public static final float CLOUD_DURATION_MULTIPLIER = 0.25f;
    public static final float CLOUD_INSTANT_MULTIPLIER = 0.5f;

    private static final Map<String, Integer> DURATIONS = Map.ofEntries(
            Map.entry("regeneration", 200), Map.entry("long_regeneration", 400), Map.entry("strong_regeneration", 100),
            Map.entry("poison", 160), Map.entry("long_poison", 320), Map.entry("strong_poison", 80),
            Map.entry("strength", 1200), Map.entry("long_strength", 2400), Map.entry("strong_strength", 600),
            Map.entry("swiftness", 1200), Map.entry("long_swiftness", 2400), Map.entry("strong_swiftness", 600),
            Map.entry("fire_resistance", 2400), Map.entry("long_fire_resistance", 4800),
            Map.entry("slowness", 400), Map.entry("long_slowness", 800), Map.entry("strong_slowness", 200),
            Map.entry("weakness", 400), Map.entry("long_weakness", 800),
            Map.entry("turtle_master", 400), Map.entry("long_turtle_master", 800), Map.entry("strong_turtle_master", 200));

    private PotionRules() {}

    public static Potion balance(String id, Potion original) {
        Integer duration = DURATIONS.get(id);
        if (duration == null) return original;
        String baseName = id.replaceFirst("^(long_|strong_)", "");
        var effects = original.getEffects().stream().map(effect -> new StatusEffectInstance(effect.getEffectType(),
                duration, effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()))
                .toArray(StatusEffectInstance[]::new);
        return new Potion(baseName, effects);
    }

    public static Entity effectOwner(Entity source) {
        if (source instanceof AreaEffectCloudEntity cloud && cloud.getOwner() != null) return cloud.getOwner();
        if (source instanceof ProjectileEntity projectile && projectile.getOwner() != null) return projectile.getOwner();
        return source;
    }

    public static DamageSource damageSource(LivingEntity target, Entity source, Entity attacker) {
        DamageSource original = attacker == null ? target.getDamageSources().magic()
                : target.getDamageSources().indirectMagic(source != null ? source : attacker, attacker);
        return new PotionDamageSource(original);
    }

    public static boolean isPotionDamage(DamageSource source) {
        return source instanceof PotionDamageSource;
    }

    private static final class PotionDamageSource extends DamageSource {
        private PotionDamageSource(DamageSource original) {
            super(original.getTypeRegistryEntry(), original.getSource(), original.getAttacker());
        }
    }
}

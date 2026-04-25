package com.cranks.dungeons.ailment;

import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.network.ReactionEffectPayload;
import com.cranks.dungeons.util.ReactionCooldownProvider;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import java.util.List;

public class AilmentReactionManager {
    private static final ThreadLocal<Boolean> cranks$reactionInProgress = ThreadLocal.withInitial(() -> false);
    private static DamageElement currentReactionElement = null;

    public static boolean isReactionInProgress() {
        return cranks$reactionInProgress.get();
    }

    public static DamageElement getCurrentReactionElement() {
        return currentReactionElement;
    }

    /**
     * Triggers an elemental reaction if conditions are met.
     * @return A damage multiplier (e.g. 1.4f for 40% more) to be applied to the PRIMARY hit.
     */
    public static float triggerReaction(LivingEntity target, DamageElement element, float damage, Entity attacker) {
        if (target.getEntityWorld().isClient()) return 1.0f;
        ServerWorld world = (ServerWorld) target.getEntityWorld();

        // 1. FREEZE + PHYSICAL = Shatter
        if (element == DamageElement.PHYSICAL && AilmentManager.hasAilment(target, AilmentType.FREEZE)) {
            if (canTrigger(target, "shatter")) {
                applyShatter(target, world, damage, attacker);
                setCooldown(target, "shatter", getScaledCooldown(attacker, 40)); 
            }
        }

        // 2. BLEED + FIRE = Cauterize
        if (element == DamageElement.FIRE && AilmentManager.hasAilment(target, AilmentType.BLEED)) {
            if (canTrigger(target, "cauterize")) {
                applyCauterize(target, world, attacker);
                setCooldown(target, "cauterize", getScaledCooldown(attacker, 60)); 
            }
        }

        // 3. BURN + COLD = Thermal Shock
        if (element == DamageElement.COLD && AilmentManager.hasAilment(target, AilmentType.BURN)) {
            if (canTrigger(target, "thermal_shock")) {
                applyThermalShock(target, world, attacker);
                setCooldown(target, "thermal_shock", getScaledCooldown(attacker, 80)); 
            }
        }

        // 4. SHOCK + LIGHTNING = Supercharge
        if (element == DamageElement.LIGHTNING && AilmentManager.hasAilment(target, AilmentType.SHOCK)) {
            if (canTrigger(target, "supercharge")) {
                applySupercharge(target, world);
                setCooldown(target, "supercharge", getScaledCooldown(attacker, 40)); 
                return 1.4f * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_DAMAGE_PERCENT)); 
            }
        }

        // 5. POISON + VOID = Miasma
        if (element == DamageElement.VOID && AilmentManager.hasAilment(target, AilmentType.POISON)) {
            Ailment poison = AilmentManager.getAilments(target).stream()
                    .filter(a -> a.getType() == AilmentType.POISON)
                    .findFirst().orElse(null);
            if (poison != null && poison.getStacks() >= 5) {
                if (canTrigger(target, "miasma")) {
                    applyMiasma(target, world, attacker, poison.getDamagePerStack());
                    setCooldown(target, "miasma", getScaledCooldown(attacker, 100)); 
                }
            }
        }

        return 1.0f;
    }

    private static boolean canTrigger(LivingEntity target, String reaction) {
        return ((ReactionCooldownProvider) target).cranks$canTriggerReaction(reaction);
    }

    private static void setCooldown(LivingEntity target, String reaction, int ticks) {
        ((ReactionCooldownProvider) target).cranks$setReactionCooldown(reaction, ticks);
    }

    private static void applyShatter(LivingEntity target, ServerWorld world, float damage, Entity attacker) {
        float shatterDamage = damage * 1.5f * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_DAMAGE_PERCENT));
        float radius = 3.5f * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_RADIUS_PERCENT));
        Box box = target.getBoundingBox().expand(radius);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box, e -> e != target && e.isAlive());

        try {
            cranks$reactionInProgress.set(true);
            currentReactionElement = DamageElement.COLD;
            for (LivingEntity nearby : targets) {
                com.cranks.dungeons.util.ElementalDamageHelper.applyDamage(nearby, world, shatterDamage, DamageElement.COLD, attacker, true, true);
            }
        } finally {
            cranks$reactionInProgress.set(false);
            currentReactionElement = null;
        }
        syncReaction(world, target, "shatter");
    }

    private static void applyCauterize(LivingEntity target, ServerWorld world, Entity attacker) {
        Ailment bleed = AilmentManager.getAilments(target).stream()
                .filter(a -> a.getType() == AilmentType.BLEED)
                .findFirst().orElse(null);
        if (bleed == null) return;

        float burstDamage = bleed.getStoredDamage() * 0.8f * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_DAMAGE_PERCENT));
        if (burstDamage > 0) {
            try {
                cranks$reactionInProgress.set(true);
                currentReactionElement = DamageElement.FIRE;
                com.cranks.dungeons.util.ElementalDamageHelper.applyDamage(target, world, burstDamage, DamageElement.FIRE, attacker, true, true);
            } finally {
                cranks$reactionInProgress.set(false);
                currentReactionElement = null;
            }
        }
        syncReaction(world, target, "cauterize");
    }

    private static void applyThermalShock(LivingEntity target, ServerWorld world, Entity attacker) {
        float missingHealth = target.getMaxHealth() - target.getHealth();
        float burstDamage = Math.min(100f, missingHealth * 0.5f) * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_DAMAGE_PERCENT));
        
        if (burstDamage > 0) {
            try {
                cranks$reactionInProgress.set(true);
                currentReactionElement = DamageElement.COLD;
                com.cranks.dungeons.util.ElementalDamageHelper.applyDamage(target, world, burstDamage, DamageElement.COLD, attacker, true, true);
            } finally {
                cranks$reactionInProgress.set(false);
                currentReactionElement = null;
            }
        }
        syncReaction(world, target, "thermal_shock");
    }

    private static void applySupercharge(LivingEntity target, ServerWorld world) {
        AilmentManager.refreshAilmentDuration(target, AilmentType.SHOCK);
        syncReaction(world, target, "supercharge");
    }

    private static void applyMiasma(LivingEntity target, ServerWorld world, Entity attacker, float damagePerStack) {
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, target.getX(), target.getY(), target.getZ());
        float radius = 3.0f * (float)(1.0 + getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_RADIUS_PERCENT));
        cloud.setRadius(radius);
        cloud.setDuration(80); // 4 seconds
        cloud.setWaitTime(0);
        cloud.setRadiusOnUse(-0.5f);
        cloud.setRadiusGrowth(-0.01f);
        cloud.setParticleType(ParticleTypes.WITCH);
        
        // Apply Vanilla Poison effect - this will be caught by VanillaPoisonConversionMixin 
        // and converted into the stacking modded poison ailment.
        cloud.addEffect(new StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.POISON, 60, 0));
        
        world.spawnEntity(cloud);
        syncReaction(world, target, "miasma");
    }

    private static void syncReaction(ServerWorld world, LivingEntity target, String type) {
        ReactionEffectPayload payload = new ReactionEffectPayload(target.getBlockPos(), type);
        for (ServerPlayerEntity player : PlayerLookup.tracking(target)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static double getAttribute(Entity entity, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
        if (entity instanceof LivingEntity le) {
            return le.getAttributeValue(attribute);
        }
        return 0;
    }

    private static int getScaledCooldown(Entity attacker, int baseTicks) {
        double reduction = getAttribute(attacker, com.cranks.dungeons.registry.ModAttributes.REACTION_COOLDOWN_PERCENT);
        return (int) (baseTicks * (1.0 - reduction));
    }
}

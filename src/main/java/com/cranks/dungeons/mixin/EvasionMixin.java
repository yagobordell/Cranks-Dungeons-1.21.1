package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 600)
public abstract class EvasionMixin {

    @Unique
    private float evasionEntropy = -1.0f; // -1 indicates unseeded / out-of-combat
    @Unique private long cranks$lastEvasionAttempt = Long.MIN_VALUE;
    @Unique
    private double cranks$lastGlobalEvasionPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void resetEvasionEntropy(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (cranks$lastEvasionAttempt != Long.MIN_VALUE
                && entity.getWorld().getTime() - cranks$lastEvasionAttempt > 120) evasionEntropy = -1;

        // Apply Global Evasion Percent
        var evasionAttr = entity.getAttributeInstance(ModAttributes.EVASION_RATING);
        if (evasionAttr != null) {
            double globalEvasionPercent = entity.getAttributeValue(ModAttributes.EVASION_RATING_PERCENT);
            net.minecraft.util.Identifier percentModifierId = net.minecraft.util.Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID, "global_evasion_percent");
            if (Double.isNaN(cranks$lastGlobalEvasionPercentBonus) || globalEvasionPercent != cranks$lastGlobalEvasionPercentBonus) {
                evasionAttr.removeModifier(percentModifierId);
                if (globalEvasionPercent > 0) {
                    evasionAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                            percentModifierId,
                            globalEvasionPercent,
                            net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
                cranks$lastGlobalEvasionPercentBonus = globalEvasionPercent;
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void handleEvasion(DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        net.minecraft.world.World world = entity.getWorld();

        if (world.isClient() || amount <= 0) return;

        if (com.cranks.dungeons.ailment.AilmentManager.isSecondaryDamage(entity)) return;
        // Evasion ONLY applies to hits originating from an entity (melee, projectile, mob/player attack)
        if (source.getAttacker() == null && source.getSource() == null) {
            return;
        }

        // Exclude environmental / non-attack damage types
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_ARMOR)
                || source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FALL)
                || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_DROWNING)
                || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)
                || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FREEZING)) {
            return;
        }

        // Resolute Technique prevents evasion
        if (source.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE) > 0) {
                return;
            }
        }

        // Iron Reflexes converts evasion to armor, so we can't evade
        if (entity.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0) {
            return;
        }

        double rating = entity.getAttributeValue(ModAttributes.EVASION_RATING);
        if (rating <= 0)
            return;

        // Path of Exile 2 inspired Evasion scaling based on incoming damage (hit threat/accuracy)
        // Dodge Chance = Evasion / (Evasion + 8 * Damage), capped at 75%
        double dodgeChance = com.cranks.dungeons.stat.CombatStatRules.evasionChance(rating, amount);
        cranks$lastEvasionAttempt = world.getTime();

        // Roll a random starting entropy seed on combat start
        if (evasionEntropy < 0.0f) {
            evasionEntropy = entity.getRandom().nextFloat();
        }

        evasionEntropy += (float) dodgeChance;

        if (evasionEntropy >= 1.0f) {
            evasionEntropy -= 1.0f;

            world.playSound(null, entity.getBlockPos(),
                    net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    entity instanceof PlayerEntity ? net.minecraft.sound.SoundCategory.PLAYERS : net.minecraft.sound.SoundCategory.HOSTILE, 0.3f, 1.8f);

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        net.minecraft.particle.ParticleTypes.POOF,
                        entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                        3, 0.15, 0.2, 0.15, 0.02);
            }
            if (entity instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                        new com.cranks.dungeons.network.ShowNotificationPayload(Text.literal("Evaded").formatted(Formatting.GRAY), 12));
            }

            cir.setReturnValue(false);
        }
    }
}

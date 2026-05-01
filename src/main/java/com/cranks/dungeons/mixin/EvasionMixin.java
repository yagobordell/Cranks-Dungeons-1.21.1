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

@Mixin(LivingEntity.class)
public abstract class EvasionMixin {

    @Unique
    private float evasionEntropy = 0.5f; // Initial entropy state

    @Inject(method = "tick", at = @At("HEAD"))
    private void resetEvasionEntropy(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof com.cranks.dungeons.EnergyShieldProvider provider) {
            long currentTime = entity.getEntityWorld().getTime();
            long timeSinceLastHit = currentTime - provider.cranksdungeons$getLastDamageTime();

            // If it's been 6 seconds (120 ticks) since last hit, reset entropy to 0.5
            // This ensures a fresh fight starts with a clean roll
            if (timeSinceLastHit > 120L && evasionEntropy != 0.5f) {
                evasionEntropy = 0.5f;
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void handleEvasion(ServerWorld world, DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // All living entities can evade; exclude void/fall damage
        if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }

        double rating = entity.getAttributeValue(ModAttributes.EVASION_RATING);
        if (rating <= 0)
            return;

        double armor = entity.getAttributeValue(EntityAttributes.ARMOR);

        double k = 400.0;
        double dodgeChance = rating / (rating + k);

        evasionEntropy += (float) dodgeChance;

        if (evasionEntropy >= 1.0f) {
            evasionEntropy -= 1.0f;

            world.playSound(null, entity.getBlockPos(),
                    net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    entity instanceof PlayerEntity ? net.minecraft.sound.SoundCategory.PLAYERS : net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.5f);

            world.spawnParticles(
                    net.minecraft.particle.ParticleTypes.CLOUD,
                    entity.getX(),
                    entity.getY() + entity.getHeight() / 2.0,
                    entity.getZ(),
                    10,
                    0.5, 0.5, 0.5,
                    0.1);

            cir.setReturnValue(false);
        } else {
            if (evasionEntropy < 0.1f)
                evasionEntropy += 0.05f;
        }
    }
}
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class EnergyShieldDamageMixin {

    // Time between shield hits in ticks (10 = half a second at 20 TPS)
    private static final long SHIELD_COOLDOWN_TICKS = 10L;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void absorbDamageWithEnergyShield(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!(self instanceof ServerPlayerEntity serverPlayer)) return;

        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        float currentShield = provider.cranksdungeons$getCurrentEnergyShield();

        long currentTime = world.getTime();
        long timeSinceLastHit = currentTime - provider.cranksdungeons$getLastDamageTime();

        // Bypass shield if player is invulnerable (Creative, etc.) or if it's friendly fire between party members
        if (self.isInvulnerableTo(world, source)) return;

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (com.cranks.dungeons.party.PartyManager.areInSameParty(self.getUuid(), attacker.getUuid())) {
                return;
            }
        }

        // Bypass shield for specific damage types that should ignore it entirely (Void, etc.)
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isOf(DamageTypes.DROWN)) {
            return;
        }

        // No shield available: just update time and let vanilla handle health
        if (currentShield <= 0 || maxShield <= 0) {
            provider.cranksdungeons$setLastDamageTime(currentTime);
            return;
        }

        // Shield cooldown (i-frames for shield)
        if (timeSinceLastHit < SHIELD_COOLDOWN_TICKS) {
            // Do NOT update lastDamageTime here – so cooldown can actually expire
            cir.setReturnValue(false);
            return;
        }

        float prevShield = currentShield;

        // Compute shield damage
        float damageToShield = amount;
        if (source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            damageToShield *= 2.0f;
        }
        // Ensure a hit always costs at least 1 shield unit
        if (damageToShield < 1.0f) {
            damageToShield = 1.0f;
        }

        if (currentShield >= damageToShield) {
            // Shield fully absorbs this hit
            float newShield = currentShield - damageToShield;

            provider.cranksdungeons$setCurrentEnergyShield(newShield);
            provider.cranksdungeons$setLastDamageTime(currentTime);

            // Sync shield to client
            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(newShield));

            // Hit sound
            world.playSoundFromEntity(
                    null,
                    self,
                    ModSounds.ENERGY_SHIELD_HIT,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            // BREAK if hearts go from >=1 to <1
            if (prevShield >= 1.0f && newShield < 1.0f) {
                world.playSoundFromEntity(
                        null,
                        self,
                        ModSounds.ENERGY_SHIELD_BREAK,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );

                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        self.getX(),
                        self.getY() + 1.0,
                        self.getZ(),
                        30,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }

            // Cancel vanilla damage: no health lost when shield fully absorbs
            cir.setReturnValue(false);
        } else {
            // Shield breaks but absorbs ALL damage from this hit (no overflow to health)
            provider.cranksdungeons$setCurrentEnergyShield(0.0f);
            provider.cranksdungeons$setLastDamageTime(currentTime);
            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0.0f));

            // Only play break if we actually had at least 1 heart before
            if (prevShield >= 1.0f) {
                world.playSoundFromEntity(
                        null,
                        self,
                        ModSounds.ENERGY_SHIELD_BREAK,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );

                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        self.getX(),
                        self.getY() + 1.0,
                        self.getZ(),
                        30,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }

            // Cancel damage - shield broke but absorbed the entire hit (no overflow damage)
            cir.setReturnValue(false);
        }
    }
}
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.DamageConversionHelper;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.network.DamageNumberSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class EnergyShieldDamageMixin {

    private static final long SHIELD_COOLDOWN_TICKS = 10L;

    /**
     * Overhaul: We use ModifyVariable to subtract damage from the shield and return 
     * the remaining damage that should deal to health.
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float applyEnergyShieldMitigation(float amount, ServerWorld world, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (world.isClient()) return amount;
        if (amount <= 0) return 0.0f;

        // Bypass shield if entity is invulnerable
        if (self.isInvulnerableTo(world, source)) return 0.0f;

        // Party friendly fire check (only if target is player)
        if (self instanceof PlayerEntity player && source.getAttacker() instanceof PlayerEntity attacker) {
            if (com.cranks.dungeons.party.PartyManager.areInSameParty(player.getUuid(), attacker.getUuid())) {
                return 0.0f;
            }
        }

        // Specific bypasses
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isOf(DamageTypes.DROWN)) {
            return amount;
        }

        EnergyShieldProvider provider = (EnergyShieldProvider) self;
        float currentShield = provider.cranksdungeons$getCurrentEnergyShield();
        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);

        // No shield at all
        if (currentShield <= 0 || maxShield <= 0) {
            provider.cranksdungeons$setLastDamageTime(world.getTime());
            return amount;
        }

        // I-frame check
        long currentTime = world.getTime();
        long timeSinceLastHit = currentTime - provider.cranksdungeons$getLastDamageTime();
        if (timeSinceLastHit < SHIELD_COOLDOWN_TICKS) {
            return 0.0f; 
        }

        // Ailment Bypass check (Poison/Bleed ignore ES in PoE 2)
        if (AilmentManager.isPoisonDamageInProgress(self.getUuid()) || AilmentManager.isBleedDamageInProgress(self.getUuid())) {
            return amount;
        }

        // Calculate weighted shield damage (Void deals 2x)
        DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, amount, self);
        float voidDmg = breakdown.get(DamageElement.VOID);
        float otherDmg = amount - voidDmg;
        float weightedShieldDmg = otherDmg + (voidDmg * 2.0f);
        
        if (weightedShieldDmg <= 0) return amount;

        if (currentShield >= weightedShieldDmg) {
            // Full absorption
            float newShield = currentShield - weightedShieldDmg;
            provider.cranksdungeons$setCurrentEnergyShield(newShield);
            provider.cranksdungeons$setLastDamageTime(currentTime);
            
            triggerShieldVisuals(world, self, currentShield, newShield);
            triggerDamageNumbers(world, self, source, amount);
            
            // Sync to owner player if applicable
            float finalShield = newShield;
            if (self instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(finalShield, (float) maxShield));
            } else {
                com.cranks.dungeons.network.EntityShieldSyncPacket packet = new com.cranks.dungeons.network.EntityShieldSyncPacket(self.getId(), finalShield, (float) maxShield);
                net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(self).forEach(p -> ServerPlayNetworking.send(p, packet));
            }

            // Manual Knockback for fully absorbed hits
            if (!source.isIn(net.minecraft.registry.tag.DamageTypeTags.NO_KNOCKBACK)) {
                if (source.getAttacker() instanceof net.minecraft.entity.LivingEntity livingAttacker) {
                    self.setAttacker(livingAttacker);
                    
                    double diffX = livingAttacker.getX() - self.getX();
                    double diffZ = livingAttacker.getZ() - self.getZ();
                    
                    if (diffX * diffX + diffZ * diffZ < 1.0E-4D) {
                        diffX = -Math.sin(livingAttacker.getYaw() * 0.017453292F);
                        diffZ = Math.cos(livingAttacker.getYaw() * 0.017453292F);
                    }
                    
                    // Reverted to simple base knockback (let Mixins handle scaling)
                    self.takeKnockback(0.5, diffX, diffZ);
                } else if (source.getPosition() != null) {
                    self.takeKnockback(0.5, source.getPosition().x - self.getX(), source.getPosition().z - self.getZ());
                }
                
                // SYNC: Send velocity update packet to all tracking players
                net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket velocityPacket = new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(self);
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(velocityPacket);
                }
                world.getChunkManager().sendToNearbyPlayers(self, velocityPacket);
            }
            
            return 0.0f; 
        } else {
            // Overflow logic
            float absorptionRatio = currentShield / weightedShieldDmg;
            float absorbedAmount = amount * absorptionRatio;
            float remainingAmount = amount - absorbedAmount;

            provider.cranksdungeons$setCurrentEnergyShield(0.0f);
            provider.cranksdungeons$setLastDamageTime(currentTime);
            
            triggerShieldVisuals(world, self, currentShield, 0.0f);
            triggerDamageNumbers(world, self, source, absorbedAmount);
            
            if (self instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0.0f, (float) maxShield));
            } else {
                com.cranks.dungeons.network.EntityShieldSyncPacket packet = new com.cranks.dungeons.network.EntityShieldSyncPacket(self.getId(), 0.0f, (float) maxShield);
                net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(self).forEach(p -> ServerPlayNetworking.send(p, packet));
            }

            // Force velocity sync on shield break
            if (!source.isIn(net.minecraft.registry.tag.DamageTypeTags.NO_KNOCKBACK)) {
                if (self instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(self));
                } else {
                    world.getChunkManager().sendToNearbyPlayers(self, new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(self));
                }
            }

            return remainingAmount;
        }
    }

    /**
     * Because ModifyVariable changed amount to 0 when absorbed, we cancel the damage method here.
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelShieldedDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0.0f) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private void triggerDamageNumbers(ServerWorld world, LivingEntity self, DamageSource source, float actualAbsorbedAmount) {
        if (actualAbsorbedAmount <= 0.01f) return;
        
        DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, actualAbsorbedAmount, self);
        
        DamageNumberSender.broadcast(
            world, 
            self, 
            breakdown.get(DamageElement.PHYSICAL),
            breakdown.get(DamageElement.FIRE),
            breakdown.get(DamageElement.COLD),
            breakdown.get(DamageElement.LIGHTNING),
            breakdown.get(DamageElement.VOID),
            source.getAttacker() != null ? source.getAttacker().getUuid() : null,
            false // ES hits don't show as crits for now to keep it clean
        );
    }

    @Unique
    private void triggerShieldVisuals(ServerWorld world, LivingEntity self, float prevShield, float newShield) {
        SoundCategory category = self instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
        world.playSoundFromEntity(null, self, ModSounds.ENERGY_SHIELD_HIT, category, 1.0f, 1.0f);

        if (prevShield >= 1.0f && newShield < 1.0f) {
            world.playSoundFromEntity(null, self, ModSounds.ENERGY_SHIELD_BREAK, category, 1.0f, 1.0f);
            world.spawnParticles(ParticleTypes.END_ROD, self.getX(), self.getY() + self.getHeight() / 2.0, self.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
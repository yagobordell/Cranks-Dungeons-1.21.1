package com.cranks.dungeons.mixin;

import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mixin to make drowning damage percentage-based for PLAYERS only.
 * - Players: 12% max health per damage tick, cyan-blue numbers, bypasses respiration RNG
 * - Non-players (mobs/animals): Vanilla drowning damage, cyan-blue numbers visible to all
 */
@Mixin(LivingEntity.class)
public class DrowningDamageMixin {

    private boolean cranks_dungeons$isDrowningDamage = false;
    private boolean cranks_dungeons$applyingCustomDrown = false;
    private boolean cranks_dungeons$shouldShowDrownNumbers = false;
    private float cranks_dungeons$pendingDrownDamage = 0.0f;
    private boolean cranks_dungeons$isPlayer = false;

    // Track last tick we processed for each entity to prevent duplicate processing
    @Unique
    private static final Map<UUID, Long> LAST_DROWN_TICK = new HashMap<>();

    /**
     * Hook into tick to detect when drowning damage WOULD occur.
     * Works for ALL living entities but handles players and mobs differently.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void detectDrowningTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity.getEntityWorld() instanceof ServerWorld world)) return;
        
        // Prevent duplicate processing in the same tick
        long currentTick = world.getTime();
        UUID entityUuid = entity.getUuid();
        Long lastTick = LAST_DROWN_TICK.get(entityUuid);
        if (lastTick != null && lastTick == currentTick) {
            return;
        }
        
        // Check if entity is drowning (air depleted and underwater)
        if (entity.getAir() > 0 || !entity.isSubmergedInWater()) {
            return;
        }
        
        // Mark this tick as processed
        LAST_DROWN_TICK.put(entityUuid, currentTick);
        
        // Calculate damage based on entity type
        boolean isPlayer = entity instanceof PlayerEntity;
        float damage;
        
        if (isPlayer) {
            // Players: 12% max HP
            damage = entity.getMaxHealth() * 0.12f;
            damage = Math.max(damage, 2.0f);
        } else {
            // Non-players: Use vanilla damage amount (2.0)
            damage = 2.0f;
        }
        
        // Flag that we should show damage numbers this tick
        cranks_dungeons$shouldShowDrownNumbers = true;
        cranks_dungeons$pendingDrownDamage = damage;
        cranks_dungeons$isPlayer = isPlayer;
        
        // Set damage breakdown for display
        if (entity instanceof DamageBreakdownProvider provider) {
            DamageSource source = world.getDamageSources().drown();
            DamageConversionHelper.DamageBreakdown breakdown = DamageConversionHelper.getBreakdown(source, damage, entity);
            provider.cranks$setLastDamageBreakdown(breakdown);
        }
    }

    /**
     * Handle drowning damage - players get percentage damage, mobs get vanilla.
     */
    @ModifyVariable(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private float handleDrowningDamage(float amount, ServerWorld world, DamageSource source) {
        // Skip if we're applying our own custom drowning damage (prevent recursion)
        if (cranks_dungeons$applyingCustomDrown) {
            return amount;
        }
        
        if (!source.isOf(DamageTypes.DROWN)) {
            return amount;
        }

        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isPlayer = entity instanceof PlayerEntity;
        
        // Show cyan-blue damage numbers to all nearby players
        if (cranks_dungeons$shouldShowDrownNumbers) {
            broadcastDrownNumbersToNearbyPlayers(world, entity, cranks_dungeons$pendingDrownDamage);
            cranks_dungeons$shouldShowDrownNumbers = false;
        }
        
        // For players: cancel vanilla and apply custom percentage damage
        if (isPlayer) {
            cranks_dungeons$isDrowningDamage = true;
            return 0.0f; // Cancel vanilla damage
        }
        
        // For non-players: let vanilla handle the damage, just show our numbers
        return amount;
    }

    /**
     * Apply custom drowning damage to players after vanilla method runs.
     */
    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("TAIL")
    )
    private void applyPlayerDrowningDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cranks_dungeons$isDrowningDamage) {
            return;
        }
        
        cranks_dungeons$isDrowningDamage = false;

        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity instanceof PlayerEntity)) return;

        // Use the pending damage we calculated in tick()
        float damage = cranks_dungeons$pendingDrownDamage;
        if (damage <= 0) {
            damage = Math.max(entity.getMaxHealth() * 0.12f, 2.0f);
        }
        
        // Apply damage using proper damage system so death handling works
        cranks_dungeons$applyingCustomDrown = true;
        entity.damage(world, source, damage);
        cranks_dungeons$applyingCustomDrown = false;
    }

    /**
     * Broadcast drowning damage numbers to all nearby players with CYAN-BLUE color.
     */
    @Unique
    private void broadcastDrownNumbersToNearbyPlayers(ServerWorld world, LivingEntity entity, float damage) {
        // Send to all players within 32 blocks
        double range = 32.0;
        
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(entity) < range * range) {
                // Use broadcastDrown to get the cyan-blue color (DROWN damage element)
                DamageNumberSender.broadcastDrown(world, entity, damage);
            }
        }
    }
}

package com.cranks.dungeons.util;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ElementalDamageHelper {

    /**
     * Applies elemental damage to a target, bypassing i-frames (hurtTime) if requested.
     */
    public static void applyDamage(LivingEntity target, ServerWorld world, float amount, DamageElement element, Entity attacker, boolean bypassIFrames, boolean shouldBroadcast) {
        if (amount <= 0) return;

        // Reset hurtTime to bypass i-frames for reactions occurring in the same tick
        if (bypassIFrames) {
            target.hurtTime = 0;
            target.timeUntilRegen = 0;
        }

        // Apply health reduction
        // We use setHealth instead of damage() to avoid recursive loops and further i-frame issues
        // But we check for death to trigger proper death logic
        if (target.getHealth() - amount <= 0) {
            DamageSource source = getElementalDamageSource(world, element, attacker);
            boolean wasDamaged = target.damage(source, amount);
        } else {
            target.setHealth(target.getHealth() - amount);
            // Broadcast damage number since setHealth doesn't trigger our mixins
            if (shouldBroadcast) {
                DamageNumberSender.broadcast(world, target, 
                    element == DamageElement.PHYSICAL ? amount : 0,
                    element == DamageElement.FIRE ? amount : 0,
                    element == DamageElement.COLD ? amount : 0,
                    element == DamageElement.LIGHTNING ? amount : 0,
                    element == DamageElement.VOID ? amount : 0,
                    attacker != null ? attacker.getUuid() : null,
                    false
                );
            }
        }
    }

    private static DamageSource getElementalDamageSource(ServerWorld world, DamageElement element, Entity attacker) {
        if (element == DamageElement.VOID || element == DamageElement.PHYSICAL) {
            if (attacker instanceof net.minecraft.entity.player.PlayerEntity playerAttacker) {
                return world.getDamageSources().playerAttack(playerAttacker);
            } else if (attacker instanceof LivingEntity livingAttacker) {
                return world.getDamageSources().mobAttack(livingAttacker);
            }
        }
        
        return switch (element) {
            case PHYSICAL -> world.getDamageSources().generic(); // Fallback if no attacker
            case FIRE -> world.getDamageSources().onFire();
            case COLD -> world.getDamageSources().freeze();
            case LIGHTNING -> world.getDamageSources().lightningBolt();
            case VOID -> world.getDamageSources().generic(); // Fallback if no attacker
            default -> world.getDamageSources().generic();
        };
    }
}

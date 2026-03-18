package com.cranks.dungeons.network;

import com.cranks.dungeons.damage.DamageElement;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DamageNumberSender {
    private static final Logger LOGGER = LoggerFactory.getLogger("DamageNumbers");

    public static void send(ServerPlayerEntity player, LivingEntity target,
                            double physicalDamage,
                            double fireDamage,
                            double coldDamage,
                            double lightningDamage,
                            double voidDamage) {

        List<DamageNumberPayload.DamageEntry> entries = new ArrayList<>();
        if (physicalDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(physicalDamage, DamageElement.PHYSICAL.color, false));
        if (fireDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(fireDamage, DamageElement.FIRE.color, false));
        if (coldDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(coldDamage, DamageElement.COLD.color, false));
        if (lightningDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(lightningDamage, DamageElement.LIGHTNING.color, false));
        if (voidDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(voidDamage, DamageElement.VOID.color, false));

        if (entries.isEmpty()) return;

        Vec3d hitPos = getHitPosition(player, target);
        DamageNumberPayload payload = new DamageNumberPayload(hitPos.x, hitPos.y, hitPos.z, entries, true);
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastHeal(ServerWorld world, LivingEntity target, double healAmount) {
        if (healAmount <= 0) return;
        List<DamageNumberPayload.DamageEntry> entries = new ArrayList<>();
        entries.add(new DamageNumberPayload.DamageEntry(healAmount, 0x55FF55, true)); // Green color

        Vec3d pos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() * 0.5,
                target.getZ()
        );

        DamageNumberPayload payload = new DamageNumberPayload(pos.x, pos.y, pos.z, entries, false);
        double range = 32.0;
        List<ServerPlayerEntity> players = world.getPlayers(p -> p.squaredDistanceTo(pos) < range * range);
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcast(ServerWorld world, LivingEntity target,
                                 double physicalDamage,
                                 double fireDamage,
                                 double coldDamage,
                                 double lightningDamage,
                                 double voidDamage) {
        
        List<DamageNumberPayload.DamageEntry> entries = new ArrayList<>();
        if (physicalDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(physicalDamage, DamageElement.PHYSICAL.color, false));
        if (fireDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(fireDamage, DamageElement.FIRE.color, false));
        if (coldDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(coldDamage, DamageElement.COLD.color, false));
        if (lightningDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(lightningDamage, DamageElement.LIGHTNING.color, false));
        if (voidDamage > 0) entries.add(new DamageNumberPayload.DamageEntry(voidDamage, DamageElement.VOID.color, false));

        if (entries.isEmpty()) return;

        // Fallback for hit position when there's no specific attacker
        Vec3d hitPos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() * 0.5,
                target.getZ()
        );

        DamageNumberPayload payload = new DamageNumberPayload(hitPos.x, hitPos.y, hitPos.z, entries, false);
        
        // Find players within range
        double range = 32.0;
        List<ServerPlayerEntity> players = world.getPlayers(p -> p.squaredDistanceTo(hitPos) < range * range);
        
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    /**
     * Compute where the player's line of sight hits the target's bounding box.
     * Falls back to entity center if the raycast misses.
     */
    private static Vec3d getHitPosition(ServerPlayerEntity player, LivingEntity target) {
        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F); // server-side rotation

        // Max distance for the ray (a bit more than melee range)
        double maxDist = 6.0D;
        Vec3d end = eyePos.add(look.multiply(maxDist));

        // Slightly expanded box like normal hit detection
        Box box = target.getBoundingBox().expand(0.1D);

        Optional<Vec3d> optHit = box.raycast(eyePos, end);

        Vec3d hit;
        if (optHit.isPresent()) {
            Vec3d rawHit = optHit.get();

            // Clamp Y so we don't go exactly on the very feet or above the very top
            double minY = target.getY() + target.getHeight() * 0.05;
            double maxY = target.getY() + target.getHeight() * 0.95;
            double clampedY = MathHelper.clamp(rawHit.y, minY, maxY);

            hit = new Vec3d(rawHit.x, clampedY, rawHit.z);
        } else {
            // Fallback: center of the entity
            hit = new Vec3d(
                    target.getX(),
                    target.getY() + target.getHeight() * 0.5,
                    target.getZ()
            );
        }

        // Push slightly toward the player so the text is not "inside" the model
        Vec3d toPlayer = eyePos.subtract(hit);
        if (toPlayer.lengthSquared() > 1.0e-6) {
            toPlayer = toPlayer.normalize().multiply(0.1D);
            hit = hit.add(toPlayer);
        }

        return hit;
    }
}

package com.cranks.dungeons.network;

import com.cranks.dungeons.damage.DamageElement;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

        if (physicalDamage > 0) {
            entries.add(new DamageNumberPayload.DamageEntry(physicalDamage, DamageElement.PHYSICAL.color));
        }
        if (fireDamage > 0) {
            entries.add(new DamageNumberPayload.DamageEntry(fireDamage, DamageElement.FIRE.color));
        }
        if (coldDamage > 0) {
            entries.add(new DamageNumberPayload.DamageEntry(coldDamage, DamageElement.COLD.color));
        }
        if (lightningDamage > 0) {
            entries.add(new DamageNumberPayload.DamageEntry(lightningDamage, DamageElement.LIGHTNING.color));
        }
        if (voidDamage > 0) {
            entries.add(new DamageNumberPayload.DamageEntry(voidDamage, DamageElement.VOID.color));
        }

        if (entries.isEmpty()) {
            LOGGER.debug("[DamageNumbers] No entries to send - all damages are 0");
            return;
        }

        Vec3d hitPos = getHitPosition(player, target);
        double x = hitPos.x;
        double y = hitPos.y;
        double z = hitPos.z;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[DamageNumbers] Sending {} entries to {} at ({}, {}, {})",
                    entries.size(), player.getName().getString(),
                    Math.round(x * 100.0) / 100.0,
                    Math.round(y * 100.0) / 100.0,
                    Math.round(z * 100.0) / 100.0);
        }

        DamageNumberPayload payload = new DamageNumberPayload(x, y, z, entries);
        ServerPlayNetworking.send(player, payload);
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
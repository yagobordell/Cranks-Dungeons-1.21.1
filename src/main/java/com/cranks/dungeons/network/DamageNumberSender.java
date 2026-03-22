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
import java.util.UUID;

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
        DamageNumberPayload payload = new DamageNumberPayload(hitPos.x, hitPos.y, hitPos.z, entries, true, player.getUuid(), target.getUuid());
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

        DamageNumberPayload payload = new DamageNumberPayload(pos.x, pos.y, pos.z, entries, false, null, target.getUuid());
        double range = 32.0;
        List<ServerPlayerEntity> players = world.getPlayers(p -> p.squaredDistanceTo(pos) < range * range);
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcastPoison(ServerWorld world, LivingEntity target, double poisonDamage, UUID attackerUuid) {
        if (poisonDamage <= 0) return;
        List<DamageNumberPayload.DamageEntry> entries = new ArrayList<>();
        entries.add(new DamageNumberPayload.DamageEntry(poisonDamage, DamageElement.POISON.color, false));

        Vec3d pos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() * 0.5,
                target.getZ()
        );

        DamageNumberPayload payload = new DamageNumberPayload(pos.x, pos.y, pos.z, entries, false, attackerUuid, target.getUuid());
        double range = 32.0;
        List<ServerPlayerEntity> players = world.getPlayers(p -> p.squaredDistanceTo(pos) < range * range);
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcastBleed(ServerWorld world, LivingEntity target, double bleedDamage, UUID attackerUuid) {
        if (bleedDamage <= 0) return;
        List<DamageNumberPayload.DamageEntry> entries = new ArrayList<>();
        // Dark red color for bleed burst damage
        int bleedColor = 0x8B0000;
        entries.add(new DamageNumberPayload.DamageEntry(bleedDamage, bleedColor, false));

        Vec3d pos = new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() * 0.5,
                target.getZ()
        );

        DamageNumberPayload payload = new DamageNumberPayload(pos.x, pos.y, pos.z, entries, false, attackerUuid, target.getUuid());
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
                                 double voidDamage,
                                 UUID attackerUuid) {
        
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

        DamageNumberPayload payload = new DamageNumberPayload(hitPos.x, hitPos.y, hitPos.z, entries, false, attackerUuid, target.getUuid());
        
        // Find players within range
        double range = 32.0;
        List<ServerPlayerEntity> players = world.getPlayers(p -> p.squaredDistanceTo(hitPos) < range * range);
        
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
        UUID attackerUuid = (target.getRecentDamageSource() != null) ? (target.getRecentDamageSource().getAttacker() != null ? target.getRecentDamageSource().getAttacker().getUuid() : null) : null;
        broadcast(world, target, physicalDamage, fireDamage, coldDamage, lightningDamage, voidDamage, attackerUuid);
    }

    private static Vec3d getHitPosition(ServerPlayerEntity player, LivingEntity target) {
        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F); 

        double maxDist = 6.0D;
        Vec3d end = eyePos.add(look.multiply(maxDist));

        Box box = target.getBoundingBox().expand(0.1D);
        Optional<Vec3d> optHit = box.raycast(eyePos, end);

        Vec3d hit;
        if (optHit.isPresent()) {
            Vec3d rawHit = optHit.get();
            double minY = target.getY() + target.getHeight() * 0.05;
            double maxY = target.getY() + target.getHeight() * 0.95;
            double clampedY = MathHelper.clamp(rawHit.y, minY, maxY);
            hit = new Vec3d(rawHit.x, clampedY, rawHit.z);
        } else {
            hit = new Vec3d(
                    target.getX(),
                    target.getY() + target.getHeight() * 0.5,
                    target.getZ()
            );
        }

        Vec3d toPlayer = eyePos.subtract(hit);
        if (toPlayer.lengthSquared() > 1.0e-6) {
            toPlayer = toPlayer.normalize().multiply(0.1D);
            hit = hit.add(toPlayer);
        }

        return hit;
    }
}

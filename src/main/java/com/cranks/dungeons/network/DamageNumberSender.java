package com.cranks.dungeons.network;

import com.cranks.dungeons.damage.CombatNumberKind;
import com.cranks.dungeons.damage.CombatNumberStyle;
import com.cranks.dungeons.damage.DamageElement;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DamageNumberSender {
    private DamageNumberSender() {}
    private static void deliver(ServerWorld world, LivingEntity target, List<DamageNumberPayload.DamageEntry> entries,
                                UUID attacker, CombatNumberKind kind) {
        if (entries.isEmpty()) return;
        var pos = target.getPos().add(0, target.getHeight() * 0.65, 0);
        var payload = new DamageNumberPayload(pos.x, pos.y, pos.z, List.copyOf(entries),
                kind == CombatNumberKind.HIT, attacker, target.getId(), target.getUuid(), kind);
        // Include the attacker for long-range arrows without broadcasting to the whole server.
        for (var player : world.getPlayers()) {
            if (player.squaredDistanceTo(pos) <= 32 * 32 || player.getUuid().equals(attacker))
                ServerPlayNetworking.send(player, payload);
        }
    }
    public static void broadcastHeal(ServerWorld world, LivingEntity target, double amount) {
        broadcastHeal(world, target, amount, false);
    }
    public static void broadcastHeal(ServerWorld world, LivingEntity target, double amount, boolean gradual) {
        if (!CombatNumberStyle.valid(amount)) return;
        deliver(world, target, List.of(new DamageNumberPayload.DamageEntry(amount, 0x8EED9C, true,
                DamageElement.PHYSICAL.ordinal(), false)), null, gradual ? CombatNumberKind.RECOVERY : CombatNumberKind.HEAL);
    }
    public static void broadcastPoison(ServerWorld world, LivingEntity target, double amount, UUID attacker) {
        single(world, target, amount, DamageElement.POISON, attacker, CombatNumberKind.PERIODIC, 0xA4DC65);
    }
    public static void broadcastBleed(ServerWorld world, LivingEntity target, double amount, UUID attacker) {
        single(world, target, amount, DamageElement.PHYSICAL, attacker, CombatNumberKind.BURST, 0xF87883);
    }
    public static void broadcastDrown(ServerWorld world, LivingEntity target, double amount) {
        single(world, target, amount, DamageElement.DROWN, null, CombatNumberKind.PERIODIC, 0x75CFE5);
    }
    private static void single(ServerWorld world, LivingEntity target, double amount, DamageElement element,
                               UUID attacker, CombatNumberKind kind, int color) {
        if (CombatNumberStyle.valid(amount)) deliver(world, target, List.of(new DamageNumberPayload.DamageEntry(
                amount, color, false, element.ordinal(), false)), attacker, kind);
    }
    public static void broadcastPeriodic(ServerWorld world, LivingEntity target, double amount,
                                         DamageElement element, UUID attacker) {
        single(world, target, amount, element, attacker, CombatNumberKind.PERIODIC, element.color);
    }
    public static void broadcast(ServerWorld world, LivingEntity target, double physical, double fire, double cold,
                                 double lightning, double voidDamage, UUID attacker, boolean critical) {
        var entries = new ArrayList<DamageNumberPayload.DamageEntry>();
        double[] amounts = {physical, fire, cold, lightning, voidDamage};
        for (int i = 0; i < amounts.length; i++) {
            if (CombatNumberStyle.valid(amounts[i])) entries.add(new DamageNumberPayload.DamageEntry(amounts[i],
                    DamageElement.values()[i].color, false, i, critical));
        }
        deliver(world, target, entries, attacker, CombatNumberKind.HIT);
    }
    public static void broadcast(ServerWorld world, LivingEntity target, double physical, double fire, double cold,
                                 double lightning, double voidDamage) {
        var source = target.getRecentDamageSource();
        broadcast(world, target, physical, fire, cold, lightning, voidDamage,
                source != null && source.getAttacker() != null ? source.getAttacker().getUuid() : null, false);
    }
}

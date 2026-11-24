package com.cranks.dungeons.damage;

import com.cranks.dungeons.network.DamageNumberPayload;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** One hit/recovery event. Element values stay separate, but share placement and lifetime. */
public final class DamageNumber {
    public final int victimEntityId;
    public final UUID victimUuid, attackerUuid;
    public final CombatNumberKind kind;
    public final boolean important, critical;
    public final long spawnTime;
    private final List<DamageNumberPayload.DamageEntry> entries;
    public Vec3d position;
    public boolean detached;
    public int revision;
    public boolean placed;
    public float offsetX, offsetY;
    public long nextPlacementTime;
    public float waitingX, waitingY;

    public DamageNumber(DamageNumberPayload payload, List<DamageNumberPayload.DamageEntry> entries,
                        boolean important, long now) {
        this.entries = new ArrayList<>(entries);
        this.entries.sort(Comparator.comparingInt(DamageNumberPayload.DamageEntry::elementId));
        victimEntityId = payload.victimEntityId(); victimUuid = payload.victimUuid(); attackerUuid = payload.attackerUuid();
        kind = payload.kind(); this.important = important;
        critical = entries.stream().anyMatch(DamageNumberPayload.DamageEntry::isCrit);
        position = new Vec3d(payload.x(), payload.y(), payload.z());
        spawnTime = now;
    }
    public List<DamageNumberPayload.DamageEntry> entries() { return java.util.Collections.unmodifiableList(entries); }
    public double total() { return entries.stream().mapToDouble(DamageNumberPayload.DamageEntry::damage).sum(); }
    public long age(long now) { return Math.max(0, now - spawnTime); }
    public long lifetime() { return CombatNumberStyle.lifetime(total(), critical, kind.periodic()); }
    public boolean expired(long now) { return age(now) >= lifetime(); }
    public boolean sameVictim(DamageNumber other) {
        return victimUuid != null ? victimUuid.equals(other.victimUuid)
                : other.victimUuid == null && victimEntityId == other.victimEntityId;
    }
    public boolean compatible(DamageNumber other) {
        if (!sameVictim(other) || !Objects.equals(attackerUuid, other.attackerUuid) || kind != other.kind
                || critical != other.critical || entries.size() != other.entries.size()) return false;
        for (int i = 0; i < entries.size(); i++) {
            var a = entries.get(i); var b = other.entries.get(i);
            if (a.elementId() != b.elementId() || a.color() != b.color() || a.isHeal() != b.isHeal()) return false;
        }
        return true;
    }
    public void merge(DamageNumber other) {
        for (int i = 0; i < entries.size(); i++) {
            var a = entries.get(i);
            double sum = a.damage() + other.entries.get(i).damage();
            entries.set(i, new DamageNumberPayload.DamageEntry(Math.min(Double.MAX_VALUE, sum), a.color(), a.isHeal(), a.elementId(), a.isCrit()));
        }
        revision++;
    }
}

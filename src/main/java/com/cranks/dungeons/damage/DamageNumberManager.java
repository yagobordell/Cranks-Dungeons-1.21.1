package com.cranks.dungeons.damage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DamageNumberManager {
    public static final int MAX_GROUPS = 128, MAX_PER_TARGET = 8;
    private static final List<DamageNumber> ACTIVE = new ArrayList<>();
    private static final List<DamageNumber> VIEW = Collections.unmodifiableList(ACTIVE);
    private static Object world;
    private static long clock, previousRealTime = Util.getMeasuringTimeMs();
    private DamageNumberManager() {}
    public static long now() {
        var client = MinecraftClient.getInstance();
        long real = Util.getMeasuringTimeMs();
        if (client.world != world) { clear(); world = client.world; }
        if (!client.isPaused()) clock += Math.max(0, real - previousRealTime);
        previousRealTime = real;
        return clock;
    }
    public static void addDamageNumber(DamageNumber number) {
        long time = now();
        ACTIVE.removeIf(n -> n.expired(time));
        int targetCount = 0;
        DamageNumber compatible = null;
        for (var existing : ACTIVE) {
            if (existing.sameVictim(number)) targetCount++;
            if (existing.compatible(number) && existing.age(time) < (number.kind.periodic() ? 400 : 240)) compatible = existing;
        }
        // Accumulate periodic ticks; direct hits only combine during actual overflow.
        if (compatible != null && (number.kind.periodic() || targetCount >= MAX_PER_TARGET || ACTIVE.size() >= MAX_GROUPS)) {
            compatible.merge(number); return;
        }
        if (targetCount >= MAX_PER_TARGET) {
            for (var iterator = ACTIVE.iterator(); iterator.hasNext();) {
                if (iterator.next().sameVictim(number)) { iterator.remove(); break; }
            }
        }
        if (ACTIVE.size() >= MAX_GROUPS) {
            int discard = 0;
            for (int i = 0; i < ACTIVE.size(); i++) if (!ACTIVE.get(i).important) { discard = i; break; }
            if (!number.important && ACTIVE.stream().allMatch(n -> n.important)) return;
            ACTIVE.remove(discard);
        }
        ACTIVE.add(number);
    }
    public static List<DamageNumber> getActiveNumbers() { return VIEW; }
    public static void tick() { long time = now(); ACTIVE.removeIf(n -> n.expired(time)); }
    public static void clear() { ACTIVE.clear(); }
}

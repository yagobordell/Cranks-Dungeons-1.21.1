package com.cranks.dungeons.client.render;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class LightningArcManager {
    private static final List<ArcInstance> ARCS = new ArrayList<>();

    public static record ArcInstance(Vec3d start, Vec3d end, int color, long startTime, long endTime) {
        public boolean isExpired() {
            return System.currentTimeMillis() > endTime;
        }
    }

    public static void addArc(Vec3d start, Vec3d end, int color) {
        long now = System.currentTimeMillis();
        // Snappy duration: 150ms
        ARCS.add(new ArcInstance(start, end, color, now, now + 150));
    }

    public static void tick() {
        ARCS.removeIf(ArcInstance::isExpired);
    }

    public static List<ArcInstance> getActiveArcs() {
        return new ArrayList<>(ARCS);
    }
}

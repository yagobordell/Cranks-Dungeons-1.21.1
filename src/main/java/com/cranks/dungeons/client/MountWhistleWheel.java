package com.cranks.dungeons.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** A circle rasterized once per size, with constant-width gaps and a shared hit mask. */
final class MountWhistleWheel {
    private record Span(int x, int y, int width, int region, boolean border) {}
    private final int radius;
    private final int size;
    private final byte[] regions;
    private final List<Span> spans = new ArrayList<>();

    MountWhistleWheel(int radius) {
        this.radius = radius;
        size = radius * 2 + 5;
        regions = new byte[size * size];
        double inner = radius * .30;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                double distance = Math.hypot(x, y);
                int region = 0;
                if (distance <= inner - 4) region = 4;
                // Half-pixel inset avoids lone protruding pixels at the four cardinal points.
                else if (distance <= radius - .5 && distance >= inner && !divider(x, y)) {
                    double angle = Math.toDegrees(Math.atan2(y, x));
                    region = angle >= -150 && angle < -30 ? 1 : angle >= -30 && angle < 90 ? 2 : 3;
                }
                regions[offset(x, y)] = (byte) region;
            }
        }
        // Coalesce horizontal spans. Rendering performs no trigonometry or allocations.
        for (int y = -radius - 1; y <= radius + 1; y++) {
            for (int x = -radius - 1; x <= radius + 1;) {
                int material = material(x, y), end = x + 1;
                while (end <= radius + 1 && material(end, y) == material) end++;
                if (material != 0) spans.add(new Span(x, y, end - x, material / 2, (material & 1) != 0));
                x = end;
            }
        }
    }

    private static boolean divider(int x, int y) {
        // Three rays at -150, -30 and 90 degrees; distance is in pixels, not degrees.
        double cos = Math.sqrt(3) / 2;
        return (x * -cos + y * -.5 >= 0 && Math.abs(x * -.5 - y * -cos) < 1.5)
                || (x * cos + y * -.5 >= 0 && Math.abs(x * -.5 - y * cos) < 1.5)
                || (y >= 0 && Math.abs(x) < 1.5);
    }

    private int offset(int x, int y) { return (y + radius + 2) * size + x + radius + 2; }
    private int region(int x, int y) { return regions[offset(x, y)]; }
    private int material(int x, int y) {
        int region = region(x, y);
        if (region == 0) {
            return region(x - 1, y) != 0 || region(x + 1, y) != 0
                    || region(x, y - 1) != 0 || region(x, y + 1) != 0 ? 1 : 0;
        }
        boolean border = region(x - 1, y) != region || region(x + 1, y) != region
                || region(x, y - 1) != region || region(x, y + 1) != region;
        return region * 2 + (border ? 1 : 0);
    }

    int hit(double x, double y) {
        int px = (int) Math.floor(x + .5), py = (int) Math.floor(y + .5);
        if (Math.abs(px) > radius || Math.abs(py) > radius) return 0;
        int region = region(px, py);
        return region <= 3 ? region : 0;
    }

    void draw(DrawContext context, int cx, int cy, int highlighted) {
        for (var span : spans) {
            boolean selected = span.region == highlighted && highlighted != 0;
            int color = span.region == 0 ? 0xff000000 : span.border
                    ? selected ? 0xffcccccc : 0xff888888
                    : selected ? 0xff292929 : 0xff111111;
            context.fill(cx + span.x, cy + span.y, cx + span.x + span.width, cy + span.y + 1, color);
        }
    }
}

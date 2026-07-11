/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 */
package punchy.client.render;

import java.util.EnumMap;
import net.minecraft.class_1268;
import net.minecraft.class_3532;
import net.minecraft.class_746;

public final class InHandFallPhysics {
    private static final float FLOAT_MAX = 0.65f;
    private static final float RISE_LERP = 0.1f;
    private static final float FALL_LERP = 0.12f;
    private static final EnumMap<class_1268, Float> OFFSETS = new EnumMap(class_1268.class);

    private InHandFallPhysics() {
    }

    public static float update(class_746 player, class_1268 hand) {
        if (player == null || hand == null) {
            return 0.0f;
        }
        float current = OFFSETS.getOrDefault(hand, Float.valueOf(0.0f)).floatValue();
        float target = 0.0f;
        if (!player.method_24828()) {
            float fallSpeed = class_3532.method_15363((float)((float)(-player.method_18798().field_1351)), (float)0.0f, (float)0.3f);
            float speedFactor = fallSpeed / 0.3f;
            float distanceFactor = class_3532.method_15363((float)(player.field_6017 / 2.0f), (float)0.0f, (float)1.0f);
            target = 0.65f * class_3532.method_15363((float)(speedFactor + distanceFactor * 0.75f), (float)0.0f, (float)1.0f);
        }
        float lerp = target > current ? 0.1f : 0.12f;
        float next = class_3532.method_16439((float)lerp, (float)current, (float)target);
        OFFSETS.put(hand, Float.valueOf(next));
        return next;
    }

    public static void reset(class_1268 hand) {
        if (hand == null) {
            return;
        }
        OFFSETS.put(hand, Float.valueOf(0.0f));
    }
}


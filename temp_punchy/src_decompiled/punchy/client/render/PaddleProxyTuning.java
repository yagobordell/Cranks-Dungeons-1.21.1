/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.render;

import java.util.EnumMap;

public final class PaddleProxyTuning {
    private static final EnumMap<Kind, Profile> RIGHT = new EnumMap(Kind.class);
    private static final EnumMap<Kind, Profile> LEFT = new EnumMap(Kind.class);

    private PaddleProxyTuning() {
    }

    public static Profile get(Kind kind, boolean leftHand) {
        return leftHand ? LEFT.get((Object)kind) : RIGHT.get((Object)kind);
    }

    public static void apply(Kind kind, boolean leftHand, Profile profile) {
        if (kind == null || profile == null) {
            return;
        }
        if (leftHand) {
            LEFT.put(kind, profile);
        } else {
            RIGHT.put(kind, profile);
        }
    }

    public static void reset(Kind kind, boolean leftHand) {
        if (kind == null) {
            return;
        }
        Profile defaults = new Profile(0.0f, -0.2f, -0.2f, -10.0f, 20.0f, 0.0f, 0.35f);
        PaddleProxyTuning.apply(kind, leftHand, leftHand ? PaddleProxyTuning.mirror(defaults) : defaults);
    }

    public static Profile mirror(Profile src) {
        return new Profile(-src.offX, src.offY, src.offZ, src.rotX, -src.rotY, -src.rotZ, src.scale);
    }

    static {
        Profile base = new Profile(0.0f, -0.2f, -0.2f, -10.0f, 20.0f, 0.0f, 0.35f);
        RIGHT.put(Kind.BOAT, base);
        RIGHT.put(Kind.RAFT, base);
        LEFT.put(Kind.BOAT, PaddleProxyTuning.mirror(base));
        LEFT.put(Kind.RAFT, PaddleProxyTuning.mirror(base));
    }

    public static final class Profile {
        public float offX;
        public float offY;
        public float offZ;
        public float rotX;
        public float rotY;
        public float rotZ;
        public float scale;

        public Profile(float offX, float offY, float offZ, float rotX, float rotY, float rotZ, float scale) {
            this.offX = offX;
            this.offY = offY;
            this.offZ = offZ;
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
            this.scale = scale;
        }
    }

    public static enum Kind {
        BOAT,
        RAFT;

    }
}


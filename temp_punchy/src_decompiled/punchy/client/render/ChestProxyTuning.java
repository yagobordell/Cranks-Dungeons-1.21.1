/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.render;

import java.util.EnumMap;

public final class ChestProxyTuning {
    private static final EnumMap<Kind, Profile> PROFILES = new EnumMap(Kind.class);

    private ChestProxyTuning() {
    }

    public static Profile get(Kind kind) {
        return PROFILES.get((Object)kind);
    }

    public static void apply(Kind kind, Profile profile) {
        if (kind == null || profile == null) {
            return;
        }
        PROFILES.put(kind, profile);
    }

    public static void reset(Kind kind) {
        if (kind == null) {
            return;
        }
        Profile defaults = switch (kind.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Profile(0.0f, 0.12f, -0.08f, 0.0f, 0.0f, 0.0f, 0.75f);
            case 1 -> new Profile(0.0f, 0.12f, -0.08f, 0.0f, 0.0f, 0.0f, 0.75f);
            case 2 -> new Profile(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f);
        };
        PROFILES.put(kind, defaults);
    }

    static {
        PROFILES.put(Kind.BOAT, new Profile(0.0f, 0.12f, -0.08f, 0.0f, 0.0f, 0.0f, 0.75f));
        PROFILES.put(Kind.RAFT, new Profile(0.0f, 0.12f, -0.08f, 0.0f, 0.0f, 0.0f, 0.75f));
        PROFILES.put(Kind.MINECART, new Profile(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f));
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
        RAFT,
        MINECART;

    }
}


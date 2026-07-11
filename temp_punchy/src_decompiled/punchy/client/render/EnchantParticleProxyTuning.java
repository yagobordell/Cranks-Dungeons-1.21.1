/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.render;

import java.util.EnumMap;

public final class EnchantParticleProxyTuning {
    private static final EnumMap<Kind, Profile> PROFILES_RIGHT = new EnumMap(Kind.class);
    private static final EnumMap<Kind, Profile> PROFILES_LEFT = new EnumMap(Kind.class);

    private EnchantParticleProxyTuning() {
    }

    public static Profile get(Kind kind, boolean left) {
        return (left ? PROFILES_LEFT : PROFILES_RIGHT).get((Object)kind);
    }

    public static void apply(Kind kind, boolean left, Profile profile) {
        if (kind == null || profile == null) {
            return;
        }
        (left ? PROFILES_LEFT : PROFILES_RIGHT).put(kind, profile);
    }

    public static void reset(Kind kind, boolean left) {
        if (kind == null) {
            return;
        }
        switch (kind.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: 
        }
        Profile defaults = new Profile(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        (left ? PROFILES_LEFT : PROFILES_RIGHT).put(kind, defaults);
    }

    static {
        PROFILES_RIGHT.put(Kind.BOOK, new Profile(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
        PROFILES_LEFT.put(Kind.BOOK, new Profile(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
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
        BOOK;

    }
}


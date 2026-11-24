package com.cranks.dungeons.client.render;

/** Presentation arithmetic shared by the nameplate and its regression checks. */
public final class AilmentPresentation {
    public static final int ICON_SIZE = 16;
    public static final int FOOTER_HEIGHT = 8;
    public static final int RESISTED_TICKS = 16;

    private AilmentPresentation() {}

    public static boolean hasTimer(boolean buildup, float snapshotSeconds) {
        return !buildup && Float.isFinite(snapshotSeconds) && snapshotSeconds > 0;
    }

    public static float remaining(float snapshotSeconds, long receivedTick, long currentTick) {
        if (!Float.isFinite(snapshotSeconds) || snapshotSeconds < 0) return snapshotSeconds;
        return Math.max(0, snapshotSeconds - Math.max(0, currentTick - receivedTick) / 20f);
    }

    public static int tileHeight(boolean buildup, int stacks, float snapshotSeconds) {
        return ICON_SIZE + (buildup || stacks > 1 ? FOOTER_HEIGHT : 0)
                + (hasTimer(buildup, snapshotSeconds) ? FOOTER_HEIGHT : 0);
    }

    public static String timer(float seconds) {
        if (!Float.isFinite(seconds) || seconds < 0) return "";
        long tenths = (long) Math.ceil(Math.max(0, seconds) * 10.0 - .00001);
        long roundedSeconds = (tenths + 9) / 10;
        if (roundedSeconds >= 3600) return (roundedSeconds + 3599) / 3600 + "h";
        if (roundedSeconds >= 60) {
            long secondsPart=roundedSeconds%60;
            return roundedSeconds/60+":"+(secondsPart<10?"0":"")+secondsPart;
        }
        if (tenths >= 100) return roundedSeconds + "s";
        return tenths/10+"."+tenths%10+"s";
    }
}

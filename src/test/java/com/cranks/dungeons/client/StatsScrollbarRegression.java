package com.cranks.dungeons.client;

public final class StatsScrollbarRegression {
    private StatsScrollbarRegression() {}

    public static void run() {
        StatsScrollbar bar = new StatsScrollbar(70, 113, 402);
        int originalScroll = 94;
        double mouseY = Math.round(bar.thumbTop(originalScroll)) + 3;
        double grab = bar.grabOffset(mouseY, originalScroll);
        require(bar.scrollAt(mouseY, grab) == originalScroll, "grabbing the thumb changes the current scroll");
        require(bar.scrollAt(mouseY + 5, grab) > originalScroll, "dragging down does not scroll");
        require(bar.scrollAt(0, grab) == 0, "dragging above the track does not reach the first row");
        require(bar.scrollAt(500, grab) == bar.maxScroll(), "dragging below the track does not reach the last row");

        double trackClick = 180;
        double centeredGrab = bar.grabOffset(trackClick, 0);
        require(centeredGrab == bar.thumbHeight() / 2.0, "track click does not center the thumb");
        require(bar.scrollAt(trackClick, centeredGrab) > 0, "clicking the track does not scroll");
        require(new StatsScrollbar(70, 113, 30).scrollAt(180, 5) == 0, "short search results scroll");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

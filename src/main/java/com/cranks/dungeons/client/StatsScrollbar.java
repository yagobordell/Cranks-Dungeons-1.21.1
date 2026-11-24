package com.cranks.dungeons.client;

public record StatsScrollbar(int top, int height, int contentHeight) {
    public int maxScroll() {
        return Math.max(0, contentHeight - height);
    }

    public int thumbHeight() {
        if (maxScroll() == 0) return height;
        return Math.min(height - 1, Math.max(12, height * height / contentHeight));
    }

    public double thumbTop(int scroll) {
        if (maxScroll() == 0) return top;
        return top + (height - thumbHeight()) * (double) scroll / maxScroll();
    }

    public double grabOffset(double mouseY, int scroll) {
        long drawnTop = Math.round(thumbTop(scroll));
        if (mouseY >= drawnTop && mouseY < drawnTop + thumbHeight()) {
            return mouseY - thumbTop(scroll);
        }
        return thumbHeight() / 2.0;
    }

    public int scrollAt(double mouseY, double grabOffset) {
        if (maxScroll() == 0) return 0;
        double position = (mouseY - grabOffset - top) / (height - thumbHeight());
        return (int) Math.round(Math.clamp(position, 0, 1) * maxScroll());
    }
}

package com.cranks.dungeons.config;

public enum StatusEffectOrientation {
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT;

    public boolean isVertical() {
        return this == TOP_TO_BOTTOM || this == BOTTOM_TO_TOP;
    }

    public boolean isHorizontal() {
        return this == LEFT_TO_RIGHT || this == RIGHT_TO_LEFT;
    }
}

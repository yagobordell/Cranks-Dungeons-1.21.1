package com.cranks.dungeons.damage;

public enum DamageElement {
    PHYSICAL(0xFFFFFF),
    FIRE(0xFF5555),
    COLD(0x55FFFF),
    LIGHTNING(0x0000AA),
    VOID(0xAA00AA),
    POISON(0x006400); // Dark Green

    public final int color;
    DamageElement(int color) {
        this.color = color;
    }
}
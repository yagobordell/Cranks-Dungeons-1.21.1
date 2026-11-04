package com.cranks.dungeons.client;

/** A lightweight line used by the mount information panels. */
public final class StatLine {
    public final String name;
    public final String value;
    public final int color;
    public final double progress;

    public StatLine(String name, String value, int color) {
        this(name, value, color, -1.0);
    }

    public StatLine(String name, String value, int color, double progress) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.progress = progress;
    }
}

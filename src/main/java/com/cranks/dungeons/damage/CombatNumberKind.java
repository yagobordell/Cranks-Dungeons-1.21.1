package com.cranks.dungeons.damage;

/** Presentation category, independent of the damage element. */
public enum CombatNumberKind {
    HIT, PERIODIC, BURST, HEAL, RECOVERY;

    public boolean healing() { return this == HEAL || this == RECOVERY; }
    public boolean periodic() { return this == PERIODIC || this == RECOVERY; }
}

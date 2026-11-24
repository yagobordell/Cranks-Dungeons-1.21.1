package com.cranks.dungeons.gathering;

/** Carries provenance through the moving-piston block entity's final state transitions. */
public interface GatheringPistonData {
    boolean cranks$placedResource();
    void cranks$placedResource(boolean value);
}

package com.cranks.dungeons.mount;

public interface MountDataProvider {
    MountData cranksdungeons$getMountData();

    boolean cranksdungeons$hasMountChest();

    void cranksdungeons$setMountChest(boolean equipped);

    default int cranksdungeons$getSyncedCargoCapacity() { return 0; }

    default void cranksdungeons$setSyncedCargoCapacity(int capacity) { }
}

package com.cranks.dungeons.mount;

import net.minecraft.entity.passive.AbstractHorseEntity;

/** Access to the mount associated with vanilla's horse inventory handler. */
public interface HorseScreenHandlerMountAccess {
    AbstractHorseEntity cranks$getMountEntity();
}

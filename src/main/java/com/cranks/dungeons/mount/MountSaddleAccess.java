package com.cranks.dungeons.mount;

/** Server-side saddle state access used by the generic mount inventory. */
public interface MountSaddleAccess {
    boolean cranksdungeons$isSaddled();

    void cranksdungeons$setSaddled(boolean saddled);
}

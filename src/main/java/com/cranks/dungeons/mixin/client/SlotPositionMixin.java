package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.mount.SlotPositionAccess;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;

/** Allows the client to reposition slots as a scrollable mount inventory moves. */
@Mixin(Slot.class)
public abstract class SlotPositionMixin implements SlotPositionAccess {
    @Shadow @Final @Mutable public int x;
    @Shadow @Final @Mutable public int y;

    @Override
    public void cranksdungeons$setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

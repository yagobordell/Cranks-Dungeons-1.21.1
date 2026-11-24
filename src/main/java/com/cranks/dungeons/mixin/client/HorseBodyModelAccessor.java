package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.MountBodyModelAccess;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseEntityModel.class)
public interface HorseBodyModelAccessor extends MountBodyModelAccess {
    @Override @Accessor("body") ModelPart cranksdungeons$getMountBody();
}

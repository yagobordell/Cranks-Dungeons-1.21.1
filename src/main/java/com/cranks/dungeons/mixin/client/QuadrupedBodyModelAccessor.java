package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.MountBodyModelAccess;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(QuadrupedEntityModel.class)
public interface QuadrupedBodyModelAccessor extends MountBodyModelAccess {
    @Override @Accessor("body") ModelPart cranksdungeons$getMountBody();
}

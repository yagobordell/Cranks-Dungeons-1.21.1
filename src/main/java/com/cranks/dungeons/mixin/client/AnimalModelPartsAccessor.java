package com.cranks.dungeons.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnimalModel.class)
public interface AnimalModelPartsAccessor {
    @Invoker("getHeadParts") Iterable<ModelPart> cranks$headParts();
    @Invoker("getBodyParts") Iterable<ModelPart> cranks$bodyParts();
    @Accessor("headScaled") boolean cranks$headScaled();
    @Accessor("childHeadYOffset") float cranks$childHeadY();
    @Accessor("childHeadZOffset") float cranks$childHeadZ();
    @Accessor("invertedChildHeadScale") float cranks$childHeadScale();
    @Accessor("invertedChildBodyScale") float cranks$childBodyScale();
    @Accessor("childBodyYOffset") float cranks$childBodyY();
}

package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.MountModelPartAccess;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface MountModelPartAccessor extends MountModelPartAccess {
    @Override @Accessor("children") Map<String,ModelPart> cranks$getChildren();
    @Override @Accessor("cuboids") List<ModelPart.Cuboid> cranks$getCuboids();
}

package com.cranks.dungeons.client.render;

import net.minecraft.client.model.ModelPart;
import java.util.List;
import java.util.Map;

public interface MountModelPartAccess {
    Map<String,ModelPart> cranks$getChildren();
    List<ModelPart.Cuboid> cranks$getCuboids();
}

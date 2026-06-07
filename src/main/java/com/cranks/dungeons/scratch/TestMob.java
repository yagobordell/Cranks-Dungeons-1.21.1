package com.cranks.dungeons.scratch;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class TestMob extends MobEntity {
    public TestMob(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }
}

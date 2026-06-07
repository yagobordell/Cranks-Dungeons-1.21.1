package com.cranks.dungeons.scratch;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class TestExceptionEntity extends MobEntity {
    public TestExceptionEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        throw new RuntimeException("TICK_STACK_TRACE");
    }
}

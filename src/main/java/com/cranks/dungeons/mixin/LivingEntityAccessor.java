package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastDamageTaken")
    float cranks$getLastDamageTaken();

    @Accessor("lastDamageTaken")
    void cranks$setLastDamageTaken(float lastDamageTaken);

    @Accessor("attackingPlayer")
    net.minecraft.entity.player.PlayerEntity cranks$getAttackingPlayer();
}

package com.cranks.dungeons.mixin;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(MobEntity.class)
public interface MobEntityAccessor { @Invoker("getXpToDrop") int cranks$baseExperience(); }

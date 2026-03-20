package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class FreezeSoundMixin {

    @Unique
    private Vec3d lastPos = Vec3d.ZERO;
    @Unique
    private int tickCounter = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void playFreezeSoundOnMove(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getEntityWorld().isClient()) return;
        if (!entity.hasStatusEffect(ModEffects.FREEZE)) return;
        
        tickCounter++;
        
        // Check if entity moved (only every 5 ticks to avoid spam)
        if (tickCounter % 5 == 0) {
            Vec3d currentPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            double distanceMoved = currentPos.squaredDistanceTo(lastPos);
            
            // If moved more than 0.01 blocks, play sound
            if (distanceMoved > 0.0001) {
                if (entity.getEntityWorld() instanceof ServerWorld world) {
                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            ModSounds.FREEZE_MOVE, SoundCategory.HOSTILE,
                            0.6f, 1.0f);
                }
            }
            
            lastPos = currentPos;
        }
    }

    @Inject(method = "swingHand", at = @At("HEAD"))
    private void playFreezeSoundOnAttack(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getEntityWorld().isClient()) return;
        if (!entity.hasStatusEffect(ModEffects.FREEZE)) return;
        
        if (entity.getEntityWorld() instanceof ServerWorld world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.FREEZE_MOVE, SoundCategory.HOSTILE,
                    0.6f, 1.0f);
        }
    }
}

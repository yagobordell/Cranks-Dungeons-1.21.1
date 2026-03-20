package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
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
    private Vec3d cranks$lastPos = Vec3d.ZERO;
    @Unique
    private int cranks$tickCounter = 0;
    @Unique
    private int cranks$lastSoundTick = -100; // Track when last sound was played
    @Unique
    private boolean cranks$wasUsingRangedWeapon = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void playFreezeSoundOnMove(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getEntityWorld().isClient()) return;
        if (!entity.hasStatusEffect(ModEffects.FREEZE)) {
            cranks$wasUsingRangedWeapon = false;
            return;
        }
        
        cranks$tickCounter++;
        
        // Check if entity moved (only every 5 ticks to avoid spam)
        if (cranks$tickCounter % 5 == 0) {
            Vec3d currentPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            double distanceMoved = currentPos.squaredDistanceTo(cranks$lastPos);
            
            // If moved more than 0.01 blocks, play sound
            if (distanceMoved > 0.0001) {
                tryPlayFreezeSound(entity);
            }
            
            cranks$lastPos = currentPos;
        }
        
        // Check for bow/crossbow usage
        if (entity instanceof PlayerEntity player) {
            ItemStack activeItem = player.getActiveItem();
            boolean isUsingRanged = !activeItem.isEmpty() && 
                (activeItem.getItem() instanceof BowItem || activeItem.getItem() instanceof CrossbowItem);
            
            // Play sound when starting to use bow/crossbow
            if (isUsingRanged && !cranks$wasUsingRangedWeapon) {
                tryPlayFreezeSound(entity);
            }
            
            cranks$wasUsingRangedWeapon = isUsingRanged;
        }
    }

    @Inject(method = "swingHand", at = @At("HEAD"))
    private void playFreezeSoundOnAttack(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getEntityWorld().isClient()) return;
        if (!entity.hasStatusEffect(ModEffects.FREEZE)) return;
        
        tryPlayFreezeSound(entity);
    }
    
    @Unique
    private void tryPlayFreezeSound(LivingEntity entity) {
        // Only play sound if enough time has passed since last sound (15 ticks = 0.75 seconds)
        if (cranks$tickCounter - cranks$lastSoundTick < 15) {
            return;
        }
        
        if (entity.getEntityWorld() instanceof ServerWorld world) {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.FREEZE_MOVE, SoundCategory.HOSTILE,
                    0.6f, 1.0f);
            cranks$lastSoundTick = cranks$tickCounter;
        }
    }
}

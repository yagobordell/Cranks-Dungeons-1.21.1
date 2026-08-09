package com.cranks.dungeons.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntitySpawnMixin {

    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private static void interceptOrbSpawn(ServerWorld world, Vec3d pos, int amount, CallbackInfo ci) {
        if (amount > 0) {
            PlayerEntity nearestPlayer = world.getClosestPlayer(pos.x, pos.y, pos.z, 32.0, false);
            if (nearestPlayer != null) {
                // If it's not combat XP, PlayerEntityXpMixin will intercept this and route it to World Tree
                nearestPlayer.addExperience(amount);
            }
        }
        ci.cancel();
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.Block;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class WorldTreeExperienceMixin {

    @Mixin(Block.class)
    public static class BlockExperienceMixin {
        @Inject(method = "dropExperience", at = @At("HEAD"))
        private void modifyMiningExperience(ServerWorld world, BlockPos pos, int size, CallbackInfo ci) {
            if (size <= 0) return;
            PlayerEntity closestPlayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8.0, false);
            if (closestPlayer != null) {
                double miningBonus = closestPlayer.getAttributeValue(ModAttributes.MINING_EXPERIENCE);
                if (miningBonus > 0) {
                    int extraXp = (int) (size * miningBonus);
                    if (extraXp > 0) {
                        ExperienceOrbEntity.spawn(world, net.minecraft.util.math.Vec3d.ofCenter(pos), extraXp);
                    }
                }
            }
        }
    }

    @Mixin(AnimalEntity.class)
    public static class AnimalBreedingMixin {
        @Inject(method = "breed", at = @At("HEAD"))
        private void modifyBreedingExperience(ServerWorld world, AnimalEntity other, CallbackInfo ci) {
            AnimalEntity self = (AnimalEntity) (Object) this;
            PlayerEntity lovingPlayer = self.getLovingPlayer();
            if (lovingPlayer == null) lovingPlayer = other.getLovingPlayer();
            
            if (lovingPlayer != null) {
                double breedingBonus = lovingPlayer.getAttributeValue(ModAttributes.BREEDING_EXPERIENCE);
                if (breedingBonus > 0) {
                    int extraXp = (int) (7 * breedingBonus); // Default breeding gives 1-7 XP, we'll give max scale
                    if (extraXp > 0) {
                        ExperienceOrbEntity.spawn(world, new net.minecraft.util.math.Vec3d(self.getX(), self.getY(), self.getZ()), extraXp);
                    }
                }
            }
        }
    }
    
    @Mixin(FishingBobberEntity.class)
    public static class FishingExperienceMixin {
        @Inject(method = "use", at = @At("RETURN"))
        private void modifyFishingExperience(net.minecraft.item.ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
            int result = cir.getReturnValue();
            if (result > 0) { // If caught something
                FishingBobberEntity self = (FishingBobberEntity) (Object) this;
                PlayerEntity player = self.getPlayerOwner();
                if (player != null && player.getEntityWorld() instanceof ServerWorld world) {
                    double fishingBonus = player.getAttributeValue(ModAttributes.FISHING_EXPERIENCE);
                    if (fishingBonus > 0) {
                        int extraXp = (int) (result * 3 * fishingBonus); // Fishing usually gives 1-6 xp, we use 3 as base
                        if (extraXp > 0) {
                            ExperienceOrbEntity.spawn(world, new net.minecraft.util.math.Vec3d(player.getX(), player.getY(), player.getZ()), extraXp);
                        }
                    }
                }
            }
        }
    }
}

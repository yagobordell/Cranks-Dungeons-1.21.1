package com.cranks.dungeons.mixin;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityXpMixin extends LivingEntity {

    protected PlayerEntityXpMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    /**
     * Hook into gaining experience levels to grant unused passive points.
     */
    @Inject(method = "addExperienceLevels", at = @At("HEAD"))
    private void grantPassivePoints(int levels, CallbackInfo ci) {
        if (levels > 0) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (!player.getEntityWorld().isClient()) {
                PassiveDataComponent component = ModComponents.PASSIVE_DATA.get(player);
                component.addUnspentPoints(levels);
                
                player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                    ModSounds.PASSIVE_LEVEL_UP, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    
                com.cranks.dungeons.entity.LevelUpVfxEntity vfx = new com.cranks.dungeons.entity.LevelUpVfxEntity(com.cranks.dungeons.registry.ModEntities.LEVEL_UP_VFX, player.getEntityWorld());
                vfx.setPosition(player.getX(), player.getY(), player.getZ());
                vfx.setWorldTree(false); // False means Gold for Passive Tree
                player.getEntityWorld().spawnEntity(vfx);
            }
        }
    }

    /**
     * Override Vanilla Minecraft XP curve to use our custom Passive Tree progression (60 hours to max).
     */
    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void overrideNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        int level = player.experienceLevel;
        
        // Prevent going over level 90 (or return high amount)
        if (level >= 90) {
            cir.setReturnValue(9999999);
            return;
        }
        
        int nextXp = (int) (50 + 5.0 * level + 0.2 * Math.pow(level, 2));
        cir.setReturnValue(nextXp);
    }

    /**
     * Intercept all Vanilla XP gain. If it's not from a combat kill, redirect it to the World Tree!
     * This elegantly catches Furnace XP, Breeding, Trading, and any physical XP orbs not dropped by mobs.
     */
    @Inject(method = "addExperience", at = @At("HEAD"), cancellable = true)
    private void redirectNonCombatXp(int experience, CallbackInfo ci) {
        if (!com.cranks.dungeons.util.CombatXpContext.isCombatXp()) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (!player.getEntityWorld().isClient()) {
                // Multiply by 10 to match World Tree gathering curve
                com.cranks.dungeons.component.ModComponents.WORLD_TREE_DATA.get(player).addGatheringXp(experience * 10);
            }
            ci.cancel();
        }
    }
}

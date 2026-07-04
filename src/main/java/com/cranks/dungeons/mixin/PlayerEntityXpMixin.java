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
                // The player could theoretically add many levels at once via commands, loop or add the value directly.
                component.addUnspentPoints(levels);
                
                // Play custom level up sound with correct category for server-side broadcast
                player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), 
                    ModSounds.PASSIVE_LEVEL_UP, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    
                com.cranks.dungeons.entity.LevelUpVfxEntity vfx = new com.cranks.dungeons.entity.LevelUpVfxEntity(com.cranks.dungeons.registry.ModEntities.LEVEL_UP_VFX, player.getEntityWorld());
                vfx.setPosition(player.getX(), player.getY(), player.getZ());
                vfx.setWorldTree(false); // False means Gold for Passive Tree
                player.getEntityWorld().spawnEntity(vfx);
            }
        }
    }
}

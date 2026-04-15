package com.cranks.dungeons.mixin;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class DeathXpPenaltyMixin {

    /**
     * Preserves XP Level and subtracts 30% of XP progress on death instead of losing everything.
     */
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void applyXpDeathPenalty(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive) {
            ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;
            
            // If gamerule KeepInventory is on, vanilla already copies XP.
            // boolean keepInventory = oldPlayer.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            if (true) { // TODO: Find correct GameRules package for 1.21.1 Mojang mappings if keepInventory check is needed
                newPlayer.experienceLevel = oldPlayer.experienceLevel;
                // Penalize 30% of the current level's bar
                newPlayer.experienceProgress = Math.max(0.0f, oldPlayer.experienceProgress - 0.30f);
                
                // Note: The totalExperience field might not be perfectly exact, but it's mainly used for score.
                // We'll leave it as whatever oldPlayer had, or vanilla handled, because level/progress track the important visual state.
                newPlayer.totalExperience = oldPlayer.totalExperience; 
            }
        }
    }
}

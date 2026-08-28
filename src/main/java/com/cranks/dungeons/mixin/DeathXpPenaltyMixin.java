package com.cranks.dungeons.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
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
            
            // With keepInventory enabled, vanilla has already retained the XP exactly.
            boolean keepInventory = oldPlayer.getServerWorld().getGameRules()
                    .getBoolean(GameRules.KEEP_INVENTORY);
            if (!keepInventory) {
                newPlayer.experienceLevel = oldPlayer.experienceLevel;
                // Penalize 30% of the current level's bar
                newPlayer.experienceProgress = Math.max(0.0f, oldPlayer.experienceProgress - 0.30f);

                int xpForLevel = Math.max(1, oldPlayer.getNextLevelExperience());
                int oldProgressXp = Math.round(oldPlayer.experienceProgress * xpForLevel);
                int newProgressXp = Math.round(newPlayer.experienceProgress * xpForLevel);
                newPlayer.totalExperience = Math.max(0,
                        oldPlayer.totalExperience - Math.max(0, oldProgressXp - newProgressXp));
            }
        }
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.alchemy.AlchemyPotions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Normalize before insertion, including when only an existing potion stack has room. */
@Mixin(ItemEntity.class)
abstract class AlchemyLootPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void matchingPotionPickup(PlayerEntity player, CallbackInfo callback) {
        var entity = (ItemEntity) (Object) this;
        if (entity.getWorld().isClient()) return;
        var previous = entity.getStack();
        var canonical = AlchemyPotions.canonicalize(previous);
        if (canonical != previous) entity.setStack(canonical);
    }
}

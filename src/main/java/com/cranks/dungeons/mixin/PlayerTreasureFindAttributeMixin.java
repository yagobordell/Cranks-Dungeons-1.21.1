package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerTreasureFindAttributeMixin {

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"), cancellable = true)
    private static void cranksDungeons$addTreasureFind(
            CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir
    ) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();

        // Attach the attribute to players
        builder.add(ModAttributes.TREASURE_FIND /*, 0.0D */);

        // If your mappings require a default value overload:
        // builder.add(ModAttributes.TREASURE_FIND, 0.0D);

        cir.setReturnValue(builder);
    }
}
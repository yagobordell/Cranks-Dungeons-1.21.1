package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class EnergyShieldInitMixin {

    @Unique
    private boolean shieldInitialized = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void initializeShield(CallbackInfo ci) {
        if (shieldInitialized) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);

        // Initialize shield ONLY ONCE when player first spawns
        if (maxShield > 0) {
            provider.cranksdungeons$setCurrentEnergyShield((float) maxShield);
            shieldInitialized = true;
        }
    }
}
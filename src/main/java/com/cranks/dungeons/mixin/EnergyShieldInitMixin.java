package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class EnergyShieldInitMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void initializeShield(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        float currentShield = provider.cranksdungeons$getCurrentEnergyShield();

        // If max shield exists but current is 0, initialize it
        if (maxShield > 0 && currentShield == 0 && provider.cranksdungeons$getLastDamageTime() == 0) {
            provider.cranksdungeons$setCurrentEnergyShield((float) maxShield);
        }
    }
}
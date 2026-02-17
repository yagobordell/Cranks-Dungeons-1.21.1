package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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

        // Initialize shield ONLY ONCE when the player first has Energy Shield
        if (maxShield > 0) {
            // Start at 0, not full
            provider.cranksdungeons$setCurrentEnergyShield(0.0f);

            // Sync to client (so HUD immediately shows 0)
            if (!self.getEntityWorld().isClient() && self instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0.0f));
            }

            shieldInitialized = true;
        }
    }
}
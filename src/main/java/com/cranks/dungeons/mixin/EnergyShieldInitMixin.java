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
            // ONLY run this initialization logic on the SERVER.
            // When run on the client, it was forcefully overwriting the synced value back to 0!
            if (!self.getEntityWorld().isClient() && self instanceof ServerPlayerEntity serverPlayer) {
                // Only start at 0 if we legitimately don't have any shield yet.
                // If we logged in with saved shield > 0, we shouldn't wipe it!
                if (provider.cranksdungeons$getCurrentEnergyShield() <= 0.0f) {
                    provider.cranksdungeons$setCurrentEnergyShield(0.0f);
                    ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0.0f));
                }
            }

            shieldInitialized = true;
        }
    }
}
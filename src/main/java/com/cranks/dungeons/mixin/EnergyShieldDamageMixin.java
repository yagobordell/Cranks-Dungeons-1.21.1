package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class EnergyShieldDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void absorbDamageWithEnergyShield(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!(self instanceof ServerPlayerEntity serverPlayer)) return;

        EnergyShieldProvider provider = (EnergyShieldProvider) self;

        double maxShield = self.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        float currentShield = provider.cranksdungeons$getCurrentEnergyShield();

        // Bypass shield for creative/void damage
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            provider.cranksdungeons$setLastDamageTime(world.getTime());
            return;
        }

        if (currentShield <= 0 || maxShield <= 0) {
            provider.cranksdungeons$setLastDamageTime(world.getTime());
            return;
        }

        // Void damage deals 2x to shields
        float damageToShield = amount;
        if (source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            damageToShield *= 2.0f;
        }

        provider.cranksdungeons$setLastDamageTime(world.getTime());

        if (currentShield >= damageToShield) {
            // Shield absorbs all damage
            float newShield = currentShield - damageToShield;
            provider.cranksdungeons$setCurrentEnergyShield(newShield);

            // SYNC TO CLIENT
            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(newShield));

            world.playSound(
                    null,
                    self.getX(),
                    self.getY(),
                    self.getZ(),
                    SoundEvents.ITEM_SHIELD_BLOCK,
                    SoundCategory.PLAYERS,
                    0.1f,
                    2.0f
            );

            if (newShield <= 0) {
                world.playSound(
                        null,
                        self.getX(),
                        self.getY(),
                        self.getZ(),
                        SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.25f
                );
            }

            cir.setReturnValue(false); // No damage to health
        } else {
            provider.cranksdungeons$setCurrentEnergyShield(0);

            ServerPlayNetworking.send(serverPlayer, new EnergyShieldSyncPacket(0));

            world.playSound(
                    null,
                    self.getX(),
                    self.getY(),
                    self.getZ(),
                    SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.25f
            );

            cir.setReturnValue(false); // No damage to health
        }
    }
}
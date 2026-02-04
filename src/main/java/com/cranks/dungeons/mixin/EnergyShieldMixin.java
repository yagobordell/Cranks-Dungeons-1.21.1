package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class) // TARGET ENTITY INSTEAD OF PLAYER
public abstract class EnergyShieldMixin implements EnergyShieldProvider {

    @Unique
    private float currentEnergyShield = 0.0f;
    @Unique
    private long lastDamageTime = 0;

    @Override
    public float cranks$getShield() {
        return currentEnergyShield;
    }

    @Override
    public double cranks$getMaxShield() {
        if ((Object)this instanceof PlayerEntity player) {
            return (float) player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        }
        return 0.0f;
    }

    // These methods now resolve because they are defined in Entity.class
    @Inject(method = "writeData", at = @At("TAIL"))
    protected void writeShieldData(WriteView view, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity) {
            view.putFloat("EnergyShield", currentEnergyShield);
            view.putLong("EnergyShieldLastDamage", lastDamageTime);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    protected void readShieldData(ReadView view, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity) {
            // Passing 2 arguments: Key and Default Value
            this.currentEnergyShield = view.getFloat("EnergyShield", 0.0f);
            this.lastDamageTime = view.getLong("EnergyShieldLastDamage", 0L);
        }
    }
}
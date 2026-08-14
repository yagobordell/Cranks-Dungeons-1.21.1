package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MaxHealthMixin {

    @Unique
    private double cranks$lastHealthBonus = Double.NaN;
    @Unique
    private double cranks$lastHealthPercentBonus = Double.NaN;
    @Unique
    private float cranks$savedRealHealth = -1;
    @Unique
    private int cranks$loginGraceTicks = 0;

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeRealHealth(net.minecraft.nbt.NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        nbt.putFloat("CranksRealHealth", player.getHealth());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readRealHealth(net.minecraft.nbt.NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("CranksRealHealth")) {
            this.cranks$savedRealHealth = nbt.getFloat("CranksRealHealth");
        } else if (nbt.contains("Health")) {
            this.cranks$savedRealHealth = nbt.getFloat("Health");
        }
        this.cranks$loginGraceTicks = 20;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyMaxHealthBonus(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        Identifier voidInoculationId = Identifier.of(CranksDungeons.MOD_ID, "void_inoculation");
        if (player.getAttributeValue(ModAttributes.KEYSTONE_VOID_INOCULATION) > 0) {
            // Remove existing void inoculation modifier to recalculate
            maxHealthAttr.removeModifier(voidInoculationId);
            double currentMax = maxHealthAttr.getValue();
            if (currentMax > 1.0) {
                maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(
                        voidInoculationId,
                        -(currentMax - 1.0),
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            // Clamp current health if it exceeds max
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            return; // Void inoculation overrides all other max health calculations
        } else {
            maxHealthAttr.removeModifier(voidInoculationId);
        }

        double healthFlatBonus = player.getAttributeValue(ModAttributes.MAX_HEALTH_BONUS);
        double healthPercent = player.getAttributeValue(ModAttributes.MAX_HEALTH_PERCENT);

        Identifier flatModifierId = Identifier.of(CranksDungeons.MOD_ID, "max_health_bonus");
        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "max_health_passive_percent");

        // Apply Flat Bonus
        if (healthFlatBonus != cranks$lastHealthBonus) {
            float healthBefore = player.getHealth();
            maxHealthAttr.removeModifier(flatModifierId);
            if (healthFlatBonus > 0) {
                maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(
                        flatModifierId,
                        healthFlatBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            if (healthBefore > 0 && healthBefore <= player.getMaxHealth()) {
                player.setHealth(healthBefore);
            }
            cranks$lastHealthBonus = healthFlatBonus;
        }

        // Crimson Pact Max Health
        Identifier crimsonPactId = Identifier.of(CranksDungeons.MOD_ID, "crimson_pact_health");
        double crimsonPactHealth = 0;
        if (player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) {
            crimsonPactHealth = player.getAttributeValue(ModAttributes.ENERGY_SHIELD) * 2.0;
        }
        
        var cpMod = maxHealthAttr.getModifier(crimsonPactId);
        double currentCP = cpMod != null ? cpMod.value() : 0;
        
        if (Math.abs(crimsonPactHealth - currentCP) > 0.01) {
            float healthBefore = player.getHealth();
            maxHealthAttr.removeModifier(crimsonPactId);
            if (crimsonPactHealth > 0) {
                maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(
                        crimsonPactId, crimsonPactHealth, EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            if (healthBefore > 0 && healthBefore <= player.getMaxHealth()) {
                player.setHealth(healthBefore);
            }
        }
        
        // Apply Percentage Bonus (calculated as flat value of current total minus previous bonus)
        double currentTotal = maxHealthAttr.getValue();
        double currentPercentModifier = 0;
        var mod = maxHealthAttr.getModifier(percentModifierId);
        if (mod != null) currentPercentModifier = mod.value();

        double totalWithoutThis = currentTotal - currentPercentModifier;
        double newPercentBonus = totalWithoutThis * healthPercent;

        if (Math.abs(newPercentBonus - cranks$lastHealthPercentBonus) > 0.01) {
            float healthBefore = player.getHealth();
            maxHealthAttr.removeModifier(percentModifierId);
            if (newPercentBonus > 0) {
                maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        newPercentBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            if (healthBefore > 0 && healthBefore <= player.getMaxHealth()) {
                player.setHealth(healthBefore);
            }
            cranks$lastHealthPercentBonus = newPercentBonus;
        }

        // Restore saved health from NBT on world join / login across the initial grace ticks
        if (cranks$loginGraceTicks > 0) {
            cranks$loginGraceTicks--;
            if (cranks$savedRealHealth > 0) {
                player.setHealth(Math.min(cranks$savedRealHealth, (float) player.getMaxHealth()));
            }
        }
    }
}
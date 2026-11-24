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

    @Inject(method="damage",at=@At("HEAD"))
    private void stopLoginRestoreOnHit(net.minecraft.entity.damage.DamageSource source,float amount,
                                      org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if(amount>0)cranks$loginGraceTicks=0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyMaxHealthBonus(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        Identifier voidInoculationId = Identifier.of(CranksDungeons.MOD_ID,"void_inoculation");
        maxHealthAttr.removeModifier(voidInoculationId);

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
        
        // A true total multiplier stays correct when equipment or other health modifiers change.
        var percentModifier=maxHealthAttr.getModifier(percentModifierId);
        if(percentModifier==null || percentModifier.operation()!=EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                || Math.abs(percentModifier.value()-healthPercent)>0.000001) {
            float before=player.getHealth();
            maxHealthAttr.removeModifier(percentModifierId);
            if(healthPercent>0)maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(percentModifierId,healthPercent,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if(before>0)player.setHealth(Math.min(before,player.getMaxHealth()));
        }
        if(player.getAttributeValue(ModAttributes.KEYSTONE_VOID_INOCULATION)>0) {
            double current=maxHealthAttr.getValue();
            if(current>1)maxHealthAttr.addTemporaryModifier(new EntityAttributeModifier(voidInoculationId,1/current-1,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
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
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.CriticalHitState;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ElementalDamageMixin {

    @Shadow
    public abstract boolean damage(ServerWorld world, DamageSource source, float amount);

    @Shadow
    public abstract void heal(float amount);

    @Shadow
    public abstract double getAttributeValue(net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Unique
    private boolean cranks$processingElementalDamage = false;

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyElementalDamageOnHit(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (cranks$processingElementalDamage) {
            return;
        }

        LivingEntity target = (LivingEntity) (Object) this;

        // Accumulate bleed damage if target has bleed ailment
        AilmentManager.accumulateBleedDamage(target, amount);

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            handlePlayerAttacker(world, source, amount, attacker, target);
        } else {
            handleNonPlayerDamage(world, source, amount, target);
        }
    }

    @Unique
    private void handlePlayerAttacker(ServerWorld world, DamageSource source, float amount, PlayerEntity attacker, LivingEntity target) {
        ItemStack weapon = attacker.getMainHandStack();
        ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

        // Check if this hit was a critical strike
        boolean isCrit = CriticalHitState.isCriticalHit();
        float critMultiplier = isCrit ? 1.5f : 1.0f;

        // The amount parameter already has crit applied to physical damage
        float physicalDamage = amount;

        try {
            cranks$processingElementalDamage = true;

            // === FIRE DAMAGE ===
            double fireBaseDamage = attacker.getAttributeValue(ModAttributes.FIRE_DAMAGE);
            fireBaseDamage += getEnchantmentValue(weapon, "minecraft:fire_damage", 4.0);

            double firePercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "fire_damage_percent");
            double firePercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "fire_damage_percent");
            double fireTotalPercent = firePercentFromWeapon + firePercentFromChestplate;

            double fireDamage = fireBaseDamage * (1.0 + fireTotalPercent) * critMultiplier;

            // === COLD DAMAGE ===
            double coldBaseDamage = attacker.getAttributeValue(ModAttributes.COLD_DAMAGE);
            coldBaseDamage += getEnchantmentValue(weapon, "minecraft:cold_damage", 4.0);

            double coldPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "cold_damage_percent");
            double coldPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "cold_damage_percent");
            double coldTotalPercent = coldPercentFromWeapon + coldPercentFromChestplate;

            double coldDamage = coldBaseDamage * (1.0 + coldTotalPercent) * critMultiplier;

            // === LIGHTNING DAMAGE ===
            double lightningBaseDamage = attacker.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE);
            lightningBaseDamage += getEnchantmentValue(weapon, "minecraft:lightning_damage", 4.0);

            double lightningPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "lightning_damage_percent");
            double lightningPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "lightning_damage_percent");
            double lightningTotalPercent = lightningPercentFromWeapon + lightningPercentFromChestplate;

            double lightningDamage = lightningBaseDamage * (1.0 + lightningTotalPercent) * critMultiplier;

            // === VOID DAMAGE ===
            double voidBaseDamage = attacker.getAttributeValue(ModAttributes.VOID_DAMAGE);
            voidBaseDamage += getEnchantmentValue(weapon, "minecraft:void_damage", 4.0);

            double voidPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "void_damage_percent");
            double voidPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "void_damage_percent");
            double voidTotalPercent = voidPercentFromWeapon + voidPercentFromChestplate;

            double voidDamage = voidBaseDamage * (1.0 + voidTotalPercent) * critMultiplier;

            double totalElementalDamage = 0;

            double finalFireDamage = 0;
            double finalColdDamage = 0;
            double finalLightningDamage = 0;
            double finalVoidDamage = 0;

            if (fireDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                double finalDamage = fireDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                    finalFireDamage = finalDamage;
                }
            }

            if (coldDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                double finalDamage = coldDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                    finalColdDamage = finalDamage;
                }
            }

            if (lightningDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                double finalDamage = lightningDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                    finalLightningDamage = finalDamage;
                }
            }

            if (voidDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                double finalDamage = voidDamage * (1.0 - resistance);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                    finalVoidDamage = finalDamage;
                }
            }

            // === SEND DAMAGE NUMBERS ===
            if (attacker instanceof ServerPlayerEntity serverPlayer) {
                DamageNumberSender.send(
                        serverPlayer, target,
                        physicalDamage,
                        finalFireDamage,
                        finalColdDamage,
                        finalLightningDamage,
                        finalVoidDamage
                );
            }

            // Play per-element hit sounds
            if (finalFireDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_FIRE_HIT, SoundCategory.PLAYERS, 1.5f, 1.0f);
            }
            if (finalColdDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_COLD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            if (finalLightningDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_LIGHTNING_HIT, SoundCategory.PLAYERS, 0.8f, 1.0f);
            }
            if (finalVoidDamage > 0) {
                world.playSoundFromEntity(null, target, ModSounds.ELEMENTAL_VOID_HIT, SoundCategory.PLAYERS, 0.6f, 1.0f);
            }

            // Life Steal
            double lifeSteal = attacker.getAttributeValue(ModAttributes.LIFE_STEAL) + 
                             EquipmentStatApplier.getItemStatValue(weapon, "life_steal") + 
                             EquipmentStatApplier.getItemStatValue(chestplate, "life_steal") +
                             getEnchantmentValue(weapon, "minecraft:life_steal", 0.06) +
                             getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);

            if (lifeSteal > 0) {
                float totalDamage = physicalDamage + (float) totalElementalDamage;
                attacker.heal((float) (totalDamage * lifeSteal));
            }

            // Burn
            double chanceToBurn = attacker.getAttributeValue(ModAttributes.CHANCE_TO_BURN) + 
                                EquipmentStatApplier.getItemStatValue(weapon, "chance_to_burn") + 
                                EquipmentStatApplier.getItemStatValue(chestplate, "chance_to_burn") +
                                getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.05) +
                                getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.05);

            if (Math.random() < chanceToBurn) {
                float burnBaseDamage = (float) attacker.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE);
                float burnDamagePercent = (float) attacker.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT);
                AilmentManager.applyAilment(target, AilmentType.BURN, burnBaseDamage * burnDamagePercent / 100f, AilmentType.BURN.getBaseDuration(), attacker);
            }

            // Poison
            double chanceToPoison = attacker.getAttributeValue(ModAttributes.CHANCE_TO_POISON) + 
                                  EquipmentStatApplier.getItemStatValue(weapon, "chance_to_poison") + 
                                  EquipmentStatApplier.getItemStatValue(chestplate, "chance_to_poison") +
                                  getEnchantmentValue(weapon, "minecraft:chance_to_poison", 0.05) +
                                  getEnchantmentValue(chestplate, "minecraft:chance_to_poison", 0.05);
            
            if (Math.random() < chanceToPoison) {
                float poisonBaseDamage = (float) attacker.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE);
                float poisonDamagePercent = (float) attacker.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT);
                AilmentManager.applyAilment(target, AilmentType.POISON, poisonBaseDamage * (1.0f + poisonDamagePercent / 100f), AilmentType.POISON.getBaseDuration(), attacker);
            }

            // Freeze
            double chanceToFreeze = attacker.getAttributeValue(ModAttributes.CHANCE_TO_FREEZE) +
                                  EquipmentStatApplier.getItemStatValue(weapon, "chance_to_freeze") +
                                  EquipmentStatApplier.getItemStatValue(chestplate, "chance_to_freeze") +
                                  getEnchantmentValue(weapon, "minecraft:chance_to_freeze", 0.05) +
                                  getEnchantmentValue(chestplate, "minecraft:chance_to_freeze", 0.05);

            if (Math.random() < chanceToFreeze) {
                AilmentManager.applyAilment(target, AilmentType.FREEZE, 0.0f, AilmentType.FREEZE.getBaseDuration(), attacker);
            }

            // Bleed
            double chanceToBleed = attacker.getAttributeValue(ModAttributes.CHANCE_TO_BLEED) +
                                  EquipmentStatApplier.getItemStatValue(weapon, "chance_to_bleed") +
                                  EquipmentStatApplier.getItemStatValue(chestplate, "chance_to_bleed") +
                                  getEnchantmentValue(weapon, "minecraft:chance_to_bleed", 0.05) +
                                  getEnchantmentValue(chestplate, "minecraft:chance_to_bleed", 0.05);

            if (Math.random() < chanceToBleed) {
                AilmentManager.applyAilment(target, AilmentType.BLEED, 0.0f, AilmentType.BLEED.getBaseDuration(), attacker);
            }

        } finally {
            cranks$processingElementalDamage = false;
        }
    }

    @Unique
    private void handleNonPlayerDamage(ServerWorld world, DamageSource source, float amount, LivingEntity target) {
        // For non-player damage, we retrieve the stored breakdown and broadcast to nearby players
        if (target instanceof DamageBreakdownProvider provider) {
            DamageConversionHelper.DamageBreakdown breakdown = provider.cranks$getLastDamageBreakdown();
            if (breakdown == null) {
                breakdown = DamageConversionHelper.getBreakdown(source, amount);
            }
            
            // Apply resistances to get the actual final values
            float physical = breakdown.get(DamageElement.PHYSICAL);
            float fire = (float) (breakdown.get(DamageElement.FIRE) * (1.0 - target.getAttributeValue(ModAttributes.FIRE_RESISTANCE)));
            float cold = (float) (breakdown.get(DamageElement.COLD) * (1.0 - target.getAttributeValue(ModAttributes.COLD_RESISTANCE)));
            float lightning = (float) (breakdown.get(DamageElement.LIGHTNING) * (1.0 - target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE)));
            float voidDamage = (float) (breakdown.get(DamageElement.VOID) * (1.0 - target.getAttributeValue(ModAttributes.VOID_RESISTANCE)));

            DamageNumberSender.broadcast(world, target, physical, fire, cold, lightning, voidDamage);
        }
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                return baseValue;
            }
        }
        return 0.0;
    }
}
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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

    @Unique
    private static final ThreadLocal<Boolean> cranks$processingElementalDamage = ThreadLocal.withInitial(() -> false);

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyElementalDamageOnHit(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (cranks$processingElementalDamage.get()) {
            return;
        }

        if (!(source.getAttacker() instanceof PlayerEntity attacker)) {
            return;
        }

        LivingEntity target = (LivingEntity) (Object) this;
        ItemStack weapon = attacker.getMainHandStack();
        ItemStack chestplate = attacker.getEquippedStack(EquipmentSlot.CHEST);

        try {
            cranks$processingElementalDamage.set(true);

            // === FIRE DAMAGE ===
            // Get base damage from attribute (already includes weapon/chestplate/enchants)
            double fireBaseDamage = attacker.getAttributeValue(ModAttributes.FIRE_DAMAGE);

            // Get percentage multipliers from items
            double firePercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "fire_damage_percent");
            double firePercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "fire_damage_percent");
            double fireTotalPercent = firePercentFromWeapon + firePercentFromChestplate;

            // Apply percentage to base damage
            double fireDamage = fireBaseDamage * (1.0 + fireTotalPercent);

            // === COLD DAMAGE ===
            double coldBaseDamage = attacker.getAttributeValue(ModAttributes.COLD_DAMAGE);

            double coldPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "cold_damage_percent");
            double coldPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "cold_damage_percent");
            double coldTotalPercent = coldPercentFromWeapon + coldPercentFromChestplate;

            double coldDamage = coldBaseDamage * (1.0 + coldTotalPercent);

            // === LIGHTNING DAMAGE ===
            double lightningBaseDamage = attacker.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE);

            double lightningPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "lightning_damage_percent");
            double lightningPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "lightning_damage_percent");
            double lightningTotalPercent = lightningPercentFromWeapon + lightningPercentFromChestplate;

            double lightningDamage = lightningBaseDamage * (1.0 + lightningTotalPercent);

            // === VOID DAMAGE ===
            double voidBaseDamage = attacker.getAttributeValue(ModAttributes.VOID_DAMAGE);

            double voidPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "void_damage_percent");
            double voidPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "void_damage_percent");
            double voidTotalPercent = voidPercentFromWeapon + voidPercentFromChestplate;

            double voidDamage = voidBaseDamage * (1.0 + voidTotalPercent);

            double totalElementalDamage = 0;

            if (fireDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                double finalDamage = fireDamage * (1.0 - resistance);
                System.out.println("Fire: " + fireDamage + " * (1 - " + resistance + ") = " + finalDamage);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                }
            }

            if (coldDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.COLD_RESISTANCE);
                double finalDamage = coldDamage * (1.0 - resistance);
                System.out.println("Cold: " + coldDamage + " * (1 - " + resistance + ") = " + finalDamage);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                }
            }

            if (lightningDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                double finalDamage = lightningDamage * (1.0 - resistance);
                System.out.println("Lightning: " + lightningDamage + " * (1 - " + resistance + ") = " + finalDamage);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                }
            }

            if (voidDamage > 0) {
                double resistance = target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                double finalDamage = voidDamage * (1.0 - resistance);
                System.out.println("Void: " + voidDamage + " * (1 - " + resistance + ") = " + finalDamage);
                if (finalDamage > 0) {
                    target.setHealth(target.getHealth() - (float) finalDamage);
                    totalElementalDamage += finalDamage;
                }
            }


            double lifeStealFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "life_steal");
            double lifeStealFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "life_steal");
            double LifeStealFromEnchantment = attacker.getAttributeValue(ModAttributes.LIFE_STEAL);
            double lifeSteal = lifeStealFromWeapon + lifeStealFromChestplate + LifeStealFromEnchantment;


            if (lifeSteal > 0) {
                float totalDamage = amount + (float) totalElementalDamage;
                float healAmount = (float) (totalDamage * lifeSteal);
                System.out.println("Healing: " + totalDamage + " * " + lifeSteal + " = " + healAmount);
                attacker.heal(healAmount);
            }

            // Chance to burn from weapon + chestplate + Enchantment
            double chanceToBurnFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "chance_to_burn");
            double chanceToBurnFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "chance_to_burn");
            double chanceToBurnFromEnchantment = attacker.getAttributeValue(ModAttributes.CHANCE_TO_BURN);
            double chanceToBurn = chanceToBurnFromWeapon + chanceToBurnFromChestplate + chanceToBurnFromEnchantment;

            if (chanceToBurn > 0) {
                double roll = Math.random();
                if (roll < chanceToBurn) {
                    ((net.minecraft.entity.Entity) target).setOnFireFor(4.0f);
                }
            }


        } finally {
            cranks$processingElementalDamage.set(false);
        }
    }
}
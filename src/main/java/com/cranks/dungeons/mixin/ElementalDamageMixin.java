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

            // Get elemental damage from WEAPON (stats)
            double fireDamageFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "fire_damage");
            double coldDamageFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "cold_damage");
            double lightningDamageFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "lightning_damage");
            double voidDamageFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "void_damage");

            // Get elemental damage from CHESTPLATE (stats)
            double fireDamageFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "fire_damage");
            double coldDamageFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "cold_damage");
            double lightningDamageFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "lightning_damage");
            double voidDamageFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "void_damage");

            // ADD: Get elemental damage from ENCHANTMENTS (attributes on player)
            double fireDamageFromEnchants = attacker.getAttributeValue(ModAttributes.FIRE_DAMAGE);
            double coldDamageFromEnchants = attacker.getAttributeValue(ModAttributes.COLD_DAMAGE);
            double lightningDamageFromEnchants = attacker.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE);
            double voidDamageFromEnchants = attacker.getAttributeValue(ModAttributes.VOID_DAMAGE);

            // Combine all sources
            double fireDamage = fireDamageFromWeapon + fireDamageFromChestplate + fireDamageFromEnchants;
            double coldDamage = coldDamageFromWeapon + coldDamageFromChestplate + coldDamageFromEnchants;
            double lightningDamage = lightningDamageFromWeapon + lightningDamageFromChestplate + lightningDamageFromEnchants;
            double voidDamage = voidDamageFromWeapon + voidDamageFromChestplate + voidDamageFromEnchants;

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
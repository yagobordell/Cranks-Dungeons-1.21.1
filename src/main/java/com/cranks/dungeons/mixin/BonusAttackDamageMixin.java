package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.CombatUtility;
import com.cranks.dungeons.util.CritEntropyProvider;
import com.cranks.dungeons.util.DamageHit;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 500)
public class BonusAttackDamageMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyBonusAttackDamage(float amount, DamageSource source) {
        if (com.cranks.dungeons.effect.PotionRules.isPotionDamage(source)) return amount;
        if (AilmentManager.isSecondaryDamage((LivingEntity) (Object) this)) return amount;
        if (source.getAttacker() instanceof LivingEntity attacker) {
            LivingEntity target = (LivingEntity) (Object) this;
            ItemStack weapon = CombatUtility.getCombatWeapon(source, attacker);

            // 1. Point Blank Keystone (Distance Multiplier for Projectiles)
            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_POINT_BLANK) > 0) {
                if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE) || source.getSource() instanceof net.minecraft.entity.projectile.ProjectileEntity) {
                    double distance = Math.sqrt(attacker.squaredDistanceTo(target));
                    if (distance <= 5.0) {
                        amount *= 1.3f;
                    } else if (distance >= 30.0) {
                        amount *= 0.5f;
                    } else {
                        amount *= (float) (1.3 - ((distance - 5.0) / 25.0 * 0.8));
                    }
                }
            }

            // 2. Flat bonus attack damage
            double bonusDamage = CombatUtility.getAttributeValue(attacker, ModAttributes.BONUS_ATTACK_DAMAGE, weapon,
                    "bonus_attack_damage", source);
            float damageWithBonus = amount + (float) bonusDamage;

            // 3. Percentage multipliers
            double attributePercent = attacker.getAttributeValue(ModAttributes.PHYSICAL_DAMAGE_PERCENT);
            double weaponSpecificPercent = CombatUtility.getWeaponSpecificDamageBonus(attacker, weapon);

            double afflictedBonus = 0;
            if (AilmentManager.isAfflicted(target)) {
                afflictedBonus = attacker.getAttributeValue(ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT);
            }

            double typeBonus = 0;
            if (CombatUtility.isMelee(weapon, source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.MELEE_DAMAGE_PERCENT);
            } else if (CombatUtility.isProjectile(source)) {
                typeBonus = attacker.getAttributeValue(ModAttributes.PROJECTILE_DAMAGE_PERCENT);
                if (CombatUtility.isRanged(weapon, source)) {
                    typeBonus += attacker.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);
                }
            }

            double totalPercent = attributePercent + weaponSpecificPercent + afflictedBonus + typeBonus;

            if (CombatUtility.isTwoHanding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isDualWielding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
            } else if (CombatUtility.isOneHanding(attacker, weapon)) {
                totalPercent += attacker.getAttributeValue(ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
            }

            double damageAfterCrit = attacker.getAttributeValue(ModAttributes.DAMAGE_AFTER_CRIT_PERCENT);
            if (damageAfterCrit > 0 && CombatUtility.hasCritInLast8Seconds(attacker, target.getWorld().getTime())) {
                totalPercent += damageAfterCrit;
            }

            float finalDamage = (float) (damageWithBonus * (1.0 + totalPercent));

            // 4. Critical Strike Roll & Multiplier
            double baseCrit = attacker.getAttributeValue(ModAttributes.CRIT_CHANCE);
            double localCrit = EquipmentStatApplier.getItemStatValue(weapon, "local_crit_chance");
            double globalCrit = attacker.getAttributeValue(ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT);
            double critChance = com.cranks.dungeons.ailment.AilmentRules.criticalChance(baseCrit,localCrit,globalCrit,
                    AilmentManager.isAfflicted(target)?attacker.getAttributeValue(ModAttributes.CRIT_CHANCE_AGAINST_AFFLICTED):0);

            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE) > 0) {
                critChance = 0;
            }

            boolean isCrit = false;
            if (critChance > 0) {
                if (attacker instanceof CritEntropyProvider provider) {
                    isCrit = provider.cranks$rollCriticalStrike((float) critChance, target.getWorld().getTime(), attacker.getRandom());
                } else {
                    isCrit = attacker.getRandom().nextFloat() < critChance;
                }
            }

            if (isCrit) {
                double critMult = CombatUtility.getAttributeValue(attacker, ModAttributes.CRIT_MULTIPLIER, weapon, "crit_multiplier", source);

                // Elemental Overload check
                if (attacker.getAttributeValue(ModAttributes.KEYSTONE_ELEMENTAL_OVERLOAD) > 0) {
                    critMult = 1.0;
                }

                // Armor Crit Mitigation & Reduced Critical Damage Taken
                float extraDamageMult = (float) (critMult - 1.0);
                double protection = com.cranks.dungeons.stat.CombatStatRules.criticalProtection(
                        target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR)
                                +(target.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES)>0?target.getAttributeValue(ModAttributes.EVASION_RATING):0),
                        target.getAttributeValue(ModAttributes.ARMOR_CRIT_MITIGATION),
                        target.getAttributeValue(ModAttributes.REDUCED_CRIT_DAMAGE_TAKEN_PERCENT));
                extraDamageMult *= (float) (1 - protection);

                float effectiveCritMultiplier = 1.0f + extraDamageMult;
                finalDamage *= effectiveCritMultiplier;

                DamageHit hit = DamageHit.current();
                if (hit != null) hit.critical(effectiveCritMultiplier);


            }

            return finalDamage;
        }

        return amount;
    }

}

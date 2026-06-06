package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider, AilmentHistoryProvider {

    @Unique
    private DamageConversionHelper.DamageBreakdown cranks$lastDamageBreakdown;

    @Unique
    private float cranks$accumulatedHeal = 0;
    @Unique
    private int cranks$healTimer = 0;

    @Unique
    private final Set<UUID> cranks$recentBurnChains = new HashSet<>();

    @Override
    public DamageConversionHelper.DamageBreakdown cranks$getLastDamageBreakdown() {
        return cranks$lastDamageBreakdown;
    }

    @Override
    public void cranks$setLastDamageBreakdown(DamageConversionHelper.DamageBreakdown breakdown) {
        this.cranks$lastDamageBreakdown = breakdown;
    }

    @Override
    public void cranks$recordBurnChain(UUID chainUuid) {
        if (chainUuid != null) {
            cranks$recentBurnChains.add(chainUuid);
        }
    }

    @Override
    public boolean cranks$wasHitByChain(UUID chainUuid) {
        return chainUuid != null && cranks$recentBurnChains.contains(chainUuid);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        AilmentManager.onEntityDeath((LivingEntity) (Object) this);
    }

    /**
     * Prevents players from dropping XP on death (Yarn 1.21.1 signature).
     */
    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void preventPlayerXpDrop(ServerWorld world, Entity attacker, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void onHeal(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient() || amount <= 0)
            return;

        float actualHeal = Math.min(amount, entity.getMaxHealth() - entity.getHealth());
        if (actualHeal <= 0)
            return;

        cranks$accumulatedHeal += actualHeal;
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"), cancellable = true)
    private static void addCustomAttributesToAllEntities(
            CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();

        builder
                // Defensive
                .add(ModAttributes.FIRE_RESISTANCE)
                .add(ModAttributes.COLD_RESISTANCE)
                .add(ModAttributes.LIGHTNING_RESISTANCE)
                .add(ModAttributes.VOID_RESISTANCE)
                .add(ModAttributes.KNOCKBACK_RESISTANCE)
                .add(ModAttributes.BONUS_ARMOR)
                .add(ModAttributes.LIFE_REGENERATION)
                .add(ModAttributes.MAX_HEALTH_BONUS)
                .add(ModAttributes.FEATHER_FALLING)
                .add(ModAttributes.ENERGY_SHIELD)
                .add(ModAttributes.EVASION_RATING)
                .add(ModAttributes.ARMOR_CRIT_MITIGATION)
                .add(ModAttributes.THORNS_DAMAGE)
                .add(ModAttributes.ENCHANTMENT_EFFECTIVENESS)

                // Offensive
                .add(ModAttributes.CRIT_CHANCE)
                .add(ModAttributes.CRIT_MULTIPLIER)
                .add(ModAttributes.BONUS_ATTACK_DAMAGE)
                .add(ModAttributes.FIRE_DAMAGE)
                .add(ModAttributes.COLD_DAMAGE)
                .add(ModAttributes.LIGHTNING_DAMAGE)
                .add(ModAttributes.VOID_DAMAGE)
                .add(ModAttributes.LIFE_STEAL)
                .add(ModAttributes.CHANCE_TO_BURN)
                .add(ModAttributes.BURN_BASE_DAMAGE)
                .add(ModAttributes.BURN_DAMAGE_PERCENT)
                .add(ModAttributes.BURN_DURATION)
                .add(ModAttributes.CHANCE_TO_POISON)
                .add(ModAttributes.POISON_BASE_DAMAGE)
                .add(ModAttributes.POISON_DAMAGE_PERCENT)
                .add(ModAttributes.POISON_DURATION)
                .add(ModAttributes.POISON_MAX_STACKS)
                .add(ModAttributes.CHANCE_TO_FREEZE)
                .add(ModAttributes.FREEZE_DURATION)
                .add(ModAttributes.CHANCE_TO_BLEED)
                .add(ModAttributes.BLEED_DURATION)
                .add(ModAttributes.CHANCE_TO_SHOCK)
                .add(ModAttributes.SHOCK_DURATION)
                .add(ModAttributes.ATTACK_RANGE)
                .add(ModAttributes.ATTACK_SPEED)
                .add(ModAttributes.KNOCKBACK)

                // Utility
                .add(ModAttributes.MINING_EFFICIENCY)
                .add(ModAttributes.FORTUNE)
                .add(ModAttributes.DURABILITY_BONUS)
                .add(ModAttributes.EXPERIENCE_BONUS)
                .add(ModAttributes.PRECISION_MINING)
                .add(ModAttributes.BREAKING_RANGE)
                .add(ModAttributes.MOVEMENT_SPEED)
                .add(ModAttributes.FLYING_SPEED)
                .add(ModAttributes.LUCK)
                .add(ModAttributes.CROP_FORTUNE)
                .add(ModAttributes.LOG_FORTUNE)
                .add(ModAttributes.DIG_FORTUNE)
                .add(ModAttributes.CROP_EXPERIENCE)

                // Percentage damage
                .add(ModAttributes.ATTACK_DAMAGE_PERCENT)
                .add(ModAttributes.FIRE_DAMAGE_PERCENT)
                .add(ModAttributes.COLD_DAMAGE_PERCENT)
                .add(ModAttributes.LIGHTNING_DAMAGE_PERCENT)
                .add(ModAttributes.VOID_DAMAGE_PERCENT)
                .add(ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT)
                .add(ModAttributes.DAMAGE_WHILE_AFFLICTED_PERCENT)
                .add(ModAttributes.CRIT_CHANCE_AGAINST_AFFLICTED)
                .add(ModAttributes.ELEMENTAL_DAMAGE_PERCENT)
                .add(ModAttributes.MELEE_DAMAGE_PERCENT)
                .add(ModAttributes.RANGED_DAMAGE_PERCENT)
                .add(ModAttributes.ONE_HANDED_DAMAGE_PERCENT)
                .add(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT)
                .add(ModAttributes.TWO_HANDING_DAMAGE_PERCENT)
                .add(ModAttributes.SWORD_DAMAGE_PERCENT)
                .add(ModAttributes.AXE_DAMAGE_PERCENT)
                .add(ModAttributes.MACE_DAMAGE_PERCENT)
                .add(ModAttributes.SPEAR_DAMAGE_PERCENT)
                .add(ModAttributes.BOW_DAMAGE_PERCENT)
                .add(ModAttributes.CROSSBOW_DAMAGE_PERCENT)
                .add(ModAttributes.TRIDENT_DAMAGE_PERCENT)

                // Extra Percentages
                .add(ModAttributes.PHYSICAL_EXTRA_PERCENT)
                .add(ModAttributes.FIRE_EXTRA_PERCENT)
                .add(ModAttributes.COLD_EXTRA_PERCENT)
                .add(ModAttributes.LIGHTNING_EXTRA_PERCENT)
                .add(ModAttributes.VOID_EXTRA_PERCENT)
                .add(ModAttributes.PHYSICAL_AS_EXTRA_FIRE_PERCENT)
                .add(ModAttributes.PHYSICAL_AS_EXTRA_COLD_PERCENT)
                .add(ModAttributes.PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT)
                .add(ModAttributes.PHYSICAL_AS_EXTRA_VOID_PERCENT)

                // Stats Percentages
                .add(ModAttributes.MAX_HEALTH_PERCENT)
                .add(ModAttributes.ARMOR_PERCENT)
                .add(ModAttributes.ENERGY_SHIELD_PERCENT)
                .add(ModAttributes.EVASION_RATING_PERCENT)

                // Penetration
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT)
                .add(ModAttributes.FIRE_PENETRATION_PERCENT)
                .add(ModAttributes.COLD_PENETRATION_PERCENT)
                .add(ModAttributes.LIGHTNING_PENETRATION_PERCENT)
                .add(ModAttributes.VOID_PENETRATION_PERCENT)
                .add(ModAttributes.ELEMENTAL_PENETRATION_PERCENT)

                // Attributes & Blocking
                .add(ModAttributes.STRENGTH)
                .add(ModAttributes.DEXTERITY)
                .add(ModAttributes.INTELLIGENCE)
                .add(ModAttributes.CHANCE_TO_BLOCK)
                .add(ModAttributes.BLOCK_VALUE)
                .add(ModAttributes.HEALTH_ON_BLOCK)

                .add(ModAttributes.ARMOR_SPEED_PENALTY_REDUCTION)
                .add(ModAttributes.LOW_ARMOR_SPEED_BONUS)
                .add(ModAttributes.RELOAD_SPEED)
                .add(ModAttributes.PROJECTILE_SPEED)
                .add(ModAttributes.THORNS_DAMAGE)
                .add(ModAttributes.ENCHANTMENT_EFFECTIVENESS);

        cir.setReturnValue(builder);
    }

    @Unique
    private double cranks$lastStrength = -1;
    @Unique
    private double cranks$lastDexterity = -1;
    @Unique
    private double cranks$lastIntelligence = -1;

    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_HEALTH_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_health");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_MELEE_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_melee");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_EVASION_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_evasion");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_ES_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_es");

    @Unique
    private void updateModifier(LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, net.minecraft.util.Identifier id, double value, net.minecraft.entity.attribute.EntityAttributeModifier.Operation operation) {
        net.minecraft.entity.attribute.EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            if (value != 0) {
                instance.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(id, value, operation));
            }
        }
    }

    @Unique
    private void updateIntrinsicAttributes(LivingEntity entity) {
        if (entity.getEntityWorld().isClient()) return;

        double str = entity.getAttributeValue(ModAttributes.STRENGTH);
        double dex = entity.getAttributeValue(ModAttributes.DEXTERITY);
        double intel = entity.getAttributeValue(ModAttributes.INTELLIGENCE);

        if (str != cranks$lastStrength) {
            cranks$lastStrength = str;
            double hpBonus = Math.floor(str / 10.0) * 5.0;
            double meleeBonus = Math.floor(str / 10.0) * 0.02;
            updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH, INTRINSIC_HEALTH_MOD_ID, hpBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
            updateModifier(entity, ModAttributes.MELEE_DAMAGE_PERCENT, INTRINSIC_MELEE_MOD_ID, meleeBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }

        if (dex != cranks$lastDexterity) {
            cranks$lastDexterity = dex;
            double evasionBonus = Math.floor(dex / 10.0) * 20.0;
            updateModifier(entity, ModAttributes.EVASION_RATING, INTRINSIC_EVASION_MOD_ID, evasionBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }

        if (intel != cranks$lastIntelligence) {
            cranks$lastIntelligence = intel;
            double esBonus = Math.floor(intel / 10.0) * 5.0;
            updateModifier(entity, ModAttributes.ENERGY_SHIELD, INTRINSIC_ES_MOD_ID, esBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }
    }

    @Unique
    private void updateRequirementPenalties(LivingEntity entity) {
        if (!(entity instanceof net.minecraft.entity.player.PlayerEntity player)) return;

        double attackPenalty = 0;
        double armorPenalty = 0;
        double evasionPenalty = 0;
        double esPenalty = 0;

        net.minecraft.item.ItemStack mainhand = player.getMainHandStack();
        if (!mainhand.isEmpty() && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(player, mainhand)) {
            net.minecraft.component.type.AttributeModifiersComponent mods = mainhand.getOrDefault(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT);
            for (net.minecraft.component.type.AttributeModifiersComponent.Entry entry : mods.modifiers()) {
                if (entry.slot() == net.minecraft.component.type.AttributeModifierSlot.MAINHAND) {
                    if (entry.attribute().equals(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE)) {
                        attackPenalty += entry.modifier().value() * 0.5;
                    }
                }
            }
        }

        for (net.minecraft.entity.EquipmentSlot slot : new net.minecraft.entity.EquipmentSlot[]{net.minecraft.entity.EquipmentSlot.HEAD, net.minecraft.entity.EquipmentSlot.CHEST, net.minecraft.entity.EquipmentSlot.LEGS, net.minecraft.entity.EquipmentSlot.FEET, net.minecraft.entity.EquipmentSlot.OFFHAND}) {
            net.minecraft.item.ItemStack armor = player.getEquippedStack(slot);
            if (!armor.isEmpty() && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(player, armor)) {
                net.minecraft.component.type.AttributeModifiersComponent mods = armor.getOrDefault(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT);
                for (net.minecraft.component.type.AttributeModifiersComponent.Entry entry : mods.modifiers()) {
                    if (entry.attribute().equals(net.minecraft.entity.attribute.EntityAttributes.ARMOR)) {
                        armorPenalty += entry.modifier().value() * 0.5;
                    } else if (entry.attribute().equals(ModAttributes.EVASION_RATING)) {
                        evasionPenalty += entry.modifier().value() * 0.5;
                    } else if (entry.attribute().equals(ModAttributes.ENERGY_SHIELD)) {
                        esPenalty += entry.modifier().value() * 0.5;
                    }
                }
            }
        }

        updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_damage"), -attackPenalty, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.ARMOR, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_armor"), -armorPenalty, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        updateModifier(entity, ModAttributes.EVASION_RATING, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_evasion"), -evasionPenalty, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        updateModifier(entity, ModAttributes.ENERGY_SHIELD, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_es"), -esPenalty, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickLivingEntity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient())
            return;

        AilmentManager.tickAilments(entity);
        updateArmorSpeed(entity);
        updateIntrinsicAttributes(entity);
        updateRequirementPenalties(entity);

        // Show accumulated healing
        if (cranks$accumulatedHeal > 0) {
            cranks$healTimer++;
            if (cranks$accumulatedHeal >= 1.0f || cranks$healTimer >= 10) {
                DamageNumberSender.broadcastHeal((ServerWorld) entity.getEntityWorld(), entity,
                        (double) Math.round(cranks$accumulatedHeal * 10.0) / 10.0);
                cranks$accumulatedHeal = 0;
                cranks$healTimer = 0;
            }
        }
    }

    @Unique
    private static final net.minecraft.util.Identifier ARMOR_SPEED_MOD_ID = net.minecraft.util.Identifier
            .of("cranks-dungeons", "armor_speed_penalty");

    @Inject(method = "damage", at = @At("TAIL"))
    private void applyThorns(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || amount <= 0) return;

        LivingEntity defender = (LivingEntity) (Object) this;
        Entity attacker = source.getAttacker();

        if (attacker instanceof LivingEntity livingAttacker && attacker != defender) {
            double thorns = defender.getAttributeValue(ModAttributes.THORNS_DAMAGE);
            // Only direct hits (not projectiles, not thorns, not fire, etc.)
            if (thorns > 0 && source.getSource() == attacker && !source.isOf(net.minecraft.entity.damage.DamageTypes.THORNS)) {
                livingAttacker.damage(world, defender.getDamageSources().thorns(defender), (float) thorns);
            }
        }
    }

    @Unique
    private void updateArmorSpeed(LivingEntity entity) {
        if (entity.getEntityWorld().isClient())
            return;

        double armor = entity.getArmor();
        double penaltyReduction = entity.getAttributeValue(ModAttributes.ARMOR_SPEED_PENALTY_REDUCTION);
        double lowArmorBonusAttr = entity.getAttributeValue(ModAttributes.LOW_ARMOR_SPEED_BONUS);

        double finalModifier = 0;

        // 1. Calculate Penalty (1% per 100 armor points)
        if (armor >= 100) {
            double penaltySteps = Math.floor(armor / 100.0);
            double basePenalty = penaltySteps * 0.03;
            finalModifier -= basePenalty * (1.0 - penaltyReduction);
        }

        // 2. Calculate Low Armor Bonus (if armor < 150)
        if (armor < 150) {
            double bonus = (150 - armor) * lowArmorBonusAttr;
            finalModifier += bonus;
        }

        // Apply modifier
        net.minecraft.entity.attribute.EntityAttributeInstance speedAttr = entity
                .getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(ARMOR_SPEED_MOD_ID);
            if (finalModifier != 0) {
                speedAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        ARMOR_SPEED_MOD_ID,
                        finalModifier,
                        net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

}

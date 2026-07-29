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
import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider, AilmentHistoryProvider {

    @Inject(method = "getOffHandStack", at = @At("RETURN"), cancellable = true)
    private void hideOffhandWhenTwoHanding(CallbackInfoReturnable<ItemStack> cir) {
        if ((Object)this instanceof PlayerEntity player) {
            ItemStack actualOffhand = cir.getReturnValue();
            if (com.cranks.dungeons.util.CombatUtility.isTwoHandedWeapon(player.getMainHandStack()) ||
                com.cranks.dungeons.util.CombatUtility.isTwoHandedWeapon(actualOffhand)) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }

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
    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    private void preventPlayerXpDrop(net.minecraft.entity.Entity attacker, CallbackInfo ci) {
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

        for (var attr : ModAttributes.ALL_ATTRIBUTES) {
            builder.add(attr);
        }

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
            net.minecraft.entity.attribute.EntityAttributeModifier existing = instance.getModifier(id);
            if (existing != null && existing.value() == value && existing.operation() == operation) {
                return; // Already has the exact modifier, skip to prevent spamming client syncs
            }
            instance.removeModifier(id);
            if (value != 0) {
                instance.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(id, value, operation));
            }
        }
    }

    /**
     * Applies attribute bonuses derived from the player's core stats (Strength, Dexterity, Intelligence).
     * <ul>
     *   <li>Strength: +5 Max Health per 10 points</li>
     *   <li>Dexterity: +20 Evasion Rating per 10 points</li>
     *   <li>Intelligence: Reserved for future Mana system</li>
     * </ul>
     * <p>Only recalculates when the stat value actually changes to avoid unnecessary work.</p>
     */
    @Unique
    private void updateIntrinsicAttributes(LivingEntity entity) {

        double str = entity.getAttributeValue(ModAttributes.STRENGTH);
        double dex = entity.getAttributeValue(ModAttributes.DEXTERITY);
        double intel = entity.getAttributeValue(ModAttributes.INTELLIGENCE);

        if (str != cranks$lastStrength) {
            cranks$lastStrength = str;
            double hpBonus = Math.floor(str / 10.0) * 5.0;
            updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH, INTRINSIC_HEALTH_MOD_ID, hpBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }

        if (dex != cranks$lastDexterity) {
            cranks$lastDexterity = dex;
            double evasionBonus = Math.floor(dex / 10.0) * 20.0;
            updateModifier(entity, ModAttributes.EVASION_RATING, INTRINSIC_EVASION_MOD_ID, evasionBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }

        if (intel != cranks$lastIntelligence) {
            cranks$lastIntelligence = intel;
            // PoE 2: Intelligence gives Mana (Mana system not yet implemented)
        }
    }

    @Unique
    private final Map<String, Double> cranks$activeRequirementPenalties = new HashMap<>();

    /**
     * Applies a 50% penalty to all attribute modifiers from equipped items that don't meet
     * their stat requirements (Strength/Dexterity/Intelligence).
     * <p>The penalty is applied as a negative modifier with the SAME operation type as the
     * original modifier. This ensures that flat stats (like +10 fire resistance) become +5,
     * and multiplicative stats (like attack speed) are also correctly halved.</p>
     * <p>Attack speed is special-cased: since weapons use negative ADD_VALUE modifiers to
     * reduce the player's base 4.0 speed, the penalty halves the final speed (4.0 + modifier)
     * rather than halving the raw modifier value.</p>
     */
    @Unique
    private void updateRequirementPenalties(LivingEntity entity) {
        if (!(entity instanceof net.minecraft.entity.player.PlayerEntity player)) return;

        Map<String, Double> newPenalties = new HashMap<>();

        // Check all equipped items
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            net.minecraft.item.ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty() && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(player, stack)) {
                boolean[] hasAttackSpeedPenalty = {false};
                double[] reloadSlowdown = {0.5}; // fallback if no reload speed attribute exists

                stack.applyAttributeModifiers(slot, (attr, modifier) -> {
                    String penaltyKey = attr.getIdAsString() + "|" + modifier.operation().name();
                    double penaltyAmount = 0;

                    String attrId = attr.getIdAsString();
                    if ((attrId.equals("minecraft:attack_speed") || attrId.equals("minecraft:generic.attack_speed"))
                            && modifier.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                            && modifier.value() < 0
                            && (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND)) {
                        penaltyAmount = (4.0 + modifier.value()) * 0.5;
                        hasAttackSpeedPenalty[0] = true;
                    } else if (attrId.equals("cranks-dungeons:reload_speed")
                            && modifier.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                            && modifier.value() < 0
                            && (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND)) {
                        penaltyAmount = (1.0 + modifier.value()) * 0.5;
                        double finalSpeed = 1.0 + modifier.value() - penaltyAmount;
                        reloadSlowdown[0] = 1.0 - finalSpeed;
                    } else {
                        penaltyAmount = Math.abs(modifier.value()) * 0.5;
                    }

                    newPenalties.merge(penaltyKey, penaltyAmount, Double::sum);
                });

                if (!hasAttackSpeedPenalty[0] && (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND)) {
                    // Force third-party animation mods to play at the EXACT speed of the reload penalty
                    newPenalties.merge("minecraft:attack_speed|ADD_MULTIPLIED_TOTAL", reloadSlowdown[0], Double::sum);
                }
            }
        }

        // Apply new penalties and update tracked ones
        for (Map.Entry<String, Double> entry : newPenalties.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(net.minecraft.util.Identifier.of(parts[0])).orElse(null);
            if (attr == null) continue;
            
            // Extract operation from the second part
            net.minecraft.entity.attribute.EntityAttributeModifier.Operation op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.valueOf(parts[1]);

            updateModifier(entity, attr, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_" + parts[0].replace(':', '_') + "_" + parts[1]), -entry.getValue(), op);
        }

        // Remove any penalties that are no longer active
        for (String oldKey : cranks$activeRequirementPenalties.keySet()) {
            if (!newPenalties.containsKey(oldKey)) {
                String[] parts = oldKey.split("\\|", 2);
                net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(net.minecraft.util.Identifier.of(parts[0])).orElse(null);
                if (attr == null) continue;
                
                net.minecraft.entity.attribute.EntityAttributeModifier.Operation op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.valueOf(parts[1]);

                updateModifier(entity, attr, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_" + parts[0].replace(':', '_') + "_" + parts[1]), 0.0, op);
            }
        }

        cranks$activeRequirementPenalties.clear();
        cranks$activeRequirementPenalties.putAll(newPenalties);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickLivingEntityClientSafe(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        updateArmorSpeed(entity);
        updateIntrinsicAttributes(entity);
        updateRequirementPenalties(entity);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickLivingEntity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient())
            return;

        AilmentManager.tickAilments(entity);
        com.cranks.dungeons.util.LeechManager.tick(entity);

        // Show accumulated healing
        if (cranks$accumulatedHeal > 0) {
            cranks$healTimer++;
            double healAmount = (double) Math.round(cranks$accumulatedHeal * 10.0) / 10.0;
            if (healAmount > 0) {
                if (!entity.getWorld().isClient() && entity.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    DamageNumberSender.broadcastHeal(serverWorld, entity, healAmount);
                }
            }
            cranks$accumulatedHeal = 0;
            cranks$healTimer = 0;
        }
    }

    @Unique
    private static final net.minecraft.util.Identifier ARMOR_SPEED_MOD_ID = net.minecraft.util.Identifier
            .of("cranks-dungeons", "armor_speed_penalty");


    @Unique
    private void updateArmorSpeed(LivingEntity entity) {

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
                .getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED);
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

    @Inject(method = "dropXp", at = @At("HEAD"))
    private void modifyCombatXp(net.minecraft.entity.Entity attacker, CallbackInfo ci) {
        if (attacker instanceof net.minecraft.entity.player.PlayerEntity player) {
            double combatXpBonus = player.getAttributeValue(ModAttributes.COMBAT_EXPERIENCE);
            if (combatXpBonus > 0) {
                LivingEntity entity = (LivingEntity) (Object) this;
                if (entity instanceof net.minecraft.entity.mob.MobEntity mob) {
                    int currentXp = ((MobEntityAccessor) mob).getExperiencePoints();
                    if (currentXp > 0) {
                        ((MobEntityAccessor) mob).setExperiencePoints(currentXp + (int)(currentXp * combatXpBonus));
                    }
                }
            }
        }
    }
}

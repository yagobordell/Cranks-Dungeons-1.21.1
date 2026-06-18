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
            updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH, INTRINSIC_HEALTH_MOD_ID, hpBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
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

    @Unique
    private void updateRequirementPenalties(LivingEntity entity) {
        if (!(entity instanceof net.minecraft.entity.player.PlayerEntity player)) return;

        Map<String, Double> newPenalties = new HashMap<>();

        // Check all equipped items
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            net.minecraft.item.ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty() && !com.cranks.dungeons.equipment.ItemRequirementManager.meetsRequirements(player, stack)) {
                stack.applyAttributeModifiers(slot, (attr, modifier) -> {
                    String penaltyKey = attr.getIdAsString() + "|" + modifier.operation().getId();
                    double penaltyAmount = modifier.value() * 0.5;

                    // Special handling for weapon base attack speed
                    // Weapons use negative modifiers to reduce the player's base 4.0 attack speed.
                    // If we just halve the modifier, it becomes less negative (faster).
                    // Instead, we halve the weapon's final attack speed (4.0 + modifier).
                    String attrId = attr.getIdAsString();
                    if ((attrId.equals("minecraft:attack_speed") || attrId.equals("minecraft:generic.attack_speed"))
                            && modifier.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                            && modifier.value() < 0
                            && (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND)) {
                        penaltyAmount = (4.0 + modifier.value()) * 0.5;
                    }

                    newPenalties.merge(penaltyKey, penaltyAmount, Double::sum);
                });
            }
        }

        // Apply new penalties and update tracked ones
        for (Map.Entry<String, Double> entry : newPenalties.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(net.minecraft.util.Identifier.of(parts[0])).orElse(null);
            if (attr == null) continue;
            
            // Extract operation from the second part (we need to map ID back to Operation)
            net.minecraft.entity.attribute.EntityAttributeModifier.Operation op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE;
            if (parts[1].equals("add_multiplied_base")) op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            else if (parts[1].equals("add_multiplied_total")) op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

            updateModifier(entity, attr, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_" + parts[0].replace(':', '_') + "_" + parts[1]), -entry.getValue(), op);
        }

        // Remove any penalties that are no longer active
        for (String oldKey : cranks$activeRequirementPenalties.keySet()) {
            if (!newPenalties.containsKey(oldKey)) {
                String[] parts = oldKey.split("\\|", 2);
                net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr = net.minecraft.registry.Registries.ATTRIBUTE.getEntry(net.minecraft.util.Identifier.of(parts[0])).orElse(null);
                if (attr == null) continue;
                
                net.minecraft.entity.attribute.EntityAttributeModifier.Operation op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE;
                if (parts[1].equals("add_multiplied_base")) op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                else if (parts[1].equals("add_multiplied_total")) op = net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;

                updateModifier(entity, attr, net.minecraft.util.Identifier.of("cranks-dungeons", "req_penalty_" + parts[0].replace(':', '_') + "_" + parts[1]), 0.0, op);
            }
        }

        cranks$activeRequirementPenalties.clear();
        cranks$activeRequirementPenalties.putAll(newPenalties);
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

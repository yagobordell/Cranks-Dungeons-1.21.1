package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.CritEntropyProvider;
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
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider, AilmentHistoryProvider, CritEntropyProvider {

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

    @Unique
    private float cranks$critEntropy = -1.0f; // -1 = unseeded / out-of-combat
    @Unique
    private long cranks$lastCritAttackTime = 0L;

    @Override
    public float cranks$getCritEntropy() {
        return this.cranks$critEntropy;
    }

    @Override
    public void cranks$setCritEntropy(float entropy) {
        this.cranks$critEntropy = entropy;
    }

    @Override
    public long cranks$getLastCritAttackTime() {
        return this.cranks$lastCritAttackTime;
    }

    @Override
    public void cranks$setLastCritAttackTime(long time) {
        this.cranks$lastCritAttackTime = time;
    }

    @Override
    public boolean cranks$rollCriticalStrike(float critChance, long currentTime, net.minecraft.util.math.random.Random random) {
        if (critChance <= 0.0f) {
            return false;
        }

        // Out-of-combat reset: after 6 seconds (120 ticks) without attacking, reseed entropy
        if (this.cranks$lastCritAttackTime > 0 && (currentTime - this.cranks$lastCritAttackTime) > 120L) {
            this.cranks$critEntropy = -1.0f;
        }
        this.cranks$lastCritAttackTime = currentTime;

        // Seed random starting entropy if unseeded
        if (this.cranks$critEntropy < 0.0f) {
            this.cranks$critEntropy = random.nextFloat();
        }

        this.cranks$critEntropy += critChance;

        if (this.cranks$critEntropy >= 1.0f) {
            this.cranks$critEntropy -= 1.0f;
            if (this.cranks$critEntropy >= 1.0f) {
                this.cranks$critEntropy %= 1.0f;
            }
            return true;
        }

        return false;
    }

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

        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingAttacker) {
            if (livingAttacker instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                double healthOnKill = 0.0;
                double manaOnKill = 0.0;

                if (sp.getAttributes().hasAttribute(ModAttributes.HEALTH_ON_KILL)) {
                    healthOnKill += sp.getAttributeValue(ModAttributes.HEALTH_ON_KILL);
                }
                if (sp.getAttributes().hasAttribute(ModAttributes.MANA_ON_KILL)) {
                    manaOnKill += sp.getAttributeValue(ModAttributes.MANA_ON_KILL);
                }

                if (sp.getAttributes().hasAttribute(ModAttributes.HEALTH_ON_KILL_PERCENT)) {
                    double healthOnKillPercent = sp.getAttributeValue(ModAttributes.HEALTH_ON_KILL_PERCENT);
                    if (healthOnKillPercent > 0) {
                        double maxHealth = sp.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH);
                        healthOnKill += maxHealth * healthOnKillPercent;
                    }
                }

                if (sp.getAttributes().hasAttribute(ModAttributes.KEYSTONE_VAAL_PACT) && sp.getAttributeValue(ModAttributes.KEYSTONE_VAAL_PACT) > 0) {
                    healthOnKill *= 0.5;
                    manaOnKill *= 0.5;
                }
                
                if (sp.getAttributes().hasAttribute(ModAttributes.MANA_RECOVERY_RATE)) {
                    double recoveryRate = sp.getAttributeValue(ModAttributes.MANA_RECOVERY_RATE);
                    manaOnKill *= (1.0 + recoveryRate);
                }

                if (healthOnKill > 0) {
                    sp.heal((float) healthOnKill);
                }

                if (manaOnKill > 0 && sp instanceof com.cranks.dungeons.ManaProvider manaProvider) {
                    float currentMana = manaProvider.cranksdungeons$getCurrentMana();
                    float maxMana = (float) sp.getAttributeValue(ModAttributes.MAX_MANA);
                    if (currentMana < maxMana) {
                        float newMana = Math.min(maxMana, currentMana + (float) manaOnKill);
                        manaProvider.cranksdungeons$setCurrentMana(newMana);
                        if (sp.networkHandler != null) {
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, new com.cranks.dungeons.network.ManaSyncPacket(newMana, maxMana));
                        }
                    }
                }
            }
        }
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

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyMaceFallingDamage(float amount, net.minecraft.entity.damage.DamageSource source) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (player.getMainHandStack().isOf(net.minecraft.item.Items.MACE)) {
                double fallingDamageMult = player.getAttributeValue(ModAttributes.MACE_FALLING_DAMAGE);
                if (fallingDamageMult > 0 && player.fallDistance > 1.5f) {
                    amount *= (1.0f + (player.fallDistance * (float)fallingDamageMult));
                }
            }
        }
        return amount;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void applyMaceBounce(net.minecraft.entity.damage.DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && source.getAttacker() instanceof PlayerEntity player) {
            if (player.getMainHandStack().isOf(net.minecraft.item.Items.MACE)) {
                double bounce = player.getAttributeValue(ModAttributes.MACE_WIND_BOUNCE);
                if (bounce > 0 && player.fallDistance > 1.5f) {
                    // Calculate bounce velocity
                    double bounceVelocity = Math.min(1.5, 0.7 + (player.fallDistance * 0.05));
                    player.setVelocity(player.getVelocity().x, bounceVelocity, player.getVelocity().z);
                    player.velocityModified = true;
                    player.fallDistance = 0.0f; // Reset fall distance to prevent falling damage and allow chained bounces
                    
                    if (!player.getWorld().isClient) {
                        ((net.minecraft.server.world.ServerWorld)player.getWorld()).playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ITEM_MACE_SMASH_GROUND, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void trackDamageContributions(net.minecraft.entity.damage.DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !((LivingEntity) (Object) this).getWorld().isClient()) {
            if ((Object) this instanceof com.cranks.dungeons.util.MobDamageTracker tracker) {
                net.minecraft.entity.player.PlayerEntity attacker = null;
                if (source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity player) {
                    attacker = player;
                } else {
                    attacker = ((LivingEntityAccessor) this).cranks$getAttackingPlayer();
                }
                
                if (attacker != null) {
                    tracker.cranks$addDamageContribution(attacker.getUuid(), amount);
                }
            }
        }
    }

    @Unique
    private double cranks$lastStrength = Double.NaN;
    @Unique
    private double cranks$lastDexterity = Double.NaN;
    @Unique
    private double cranks$lastIntelligence = Double.NaN;
    @Unique
    private int cranks$lastLevel = -1;

    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_HEALTH_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_health");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_MELEE_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_melee");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_EVASION_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_evasion");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_ES_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_es");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_MANA_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_mana");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_LEVEL_HEALTH_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_level_health");
    @Unique
    private static final net.minecraft.util.Identifier INTRINSIC_LEVEL_MANA_MOD_ID = net.minecraft.util.Identifier.of("cranks-dungeons", "intrinsic_level_mana");

    @Unique
    private void updateModifier(LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, net.minecraft.util.Identifier id, double value, net.minecraft.entity.attribute.EntityAttributeModifier.Operation operation) {
        net.minecraft.entity.attribute.EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            net.minecraft.entity.attribute.EntityAttributeModifier existing = instance.getModifier(id);
            if (existing != null && existing.value() == value && existing.operation() == operation) {
                return; // Already has the exact modifier, skip to prevent spamming client syncs
            }
            float healthBefore = entity.getHealth();
            instance.removeModifier(id);
            if (value != 0) {
                instance.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(id, value, operation));
            }
            if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)) {
                if (healthBefore > 0 && healthBefore <= entity.getMaxHealth()) {
                    entity.setHealth(healthBefore);
                }
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
            double oldStr = cranks$lastStrength;
            cranks$lastStrength = str;
            double hpBonus = str * 2.0;
            updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH, INTRINSIC_HEALTH_MOD_ID, hpBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
            if (!Double.isNaN(oldStr) && oldStr >= 0 && str > oldStr) {
                double gainedHealth = (str - oldStr) * 2.0;
                entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + (float) gainedHealth));
            }
        }

        if (dex != cranks$lastDexterity) {
            cranks$lastDexterity = dex;
            double evasionBonus = dex * 2.0;
            updateModifier(entity, ModAttributes.EVASION_RATING, INTRINSIC_EVASION_MOD_ID, evasionBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
        }

        if (intel != cranks$lastIntelligence) {
            double oldIntel = cranks$lastIntelligence;
            cranks$lastIntelligence = intel;
            double manaBonus = intel * 2.0;
            updateModifier(entity, ModAttributes.MAX_MANA, INTRINSIC_MANA_MOD_ID, manaBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
            if (!Double.isNaN(oldIntel) && oldIntel >= 0 && intel > oldIntel && entity instanceof com.cranks.dungeons.ManaProvider mp) {
                double gainedMana = (intel - oldIntel) * 2.0;
                float maxMana = (float) entity.getAttributeValue(ModAttributes.MAX_MANA);
                mp.cranksdungeons$setCurrentMana(Math.min(maxMana, mp.cranksdungeons$getCurrentMana() + (float) gainedMana));
            }
        }

        if (entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            int currentLevel = player.experienceLevel;
            if (currentLevel != cranks$lastLevel) {
                int oldLevel = cranks$lastLevel;
                cranks$lastLevel = currentLevel;
                double levelHealthBonus = currentLevel * 12.0;
                double levelManaBonus = currentLevel * 4.0;

                updateModifier(entity, net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH, INTRINSIC_LEVEL_HEALTH_MOD_ID, levelHealthBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);
                updateModifier(entity, ModAttributes.MAX_MANA, INTRINSIC_LEVEL_MANA_MOD_ID, levelManaBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE);

                if (oldLevel != -1 && oldLevel >= 0 && currentLevel > oldLevel) {
                    double gainedHealth = (currentLevel - oldLevel) * 12.0;
                    double gainedMana = (currentLevel - oldLevel) * 4.0;

                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + (float) gainedHealth));

                    if (player instanceof com.cranks.dungeons.ManaProvider mp) {
                        float maxMana = (float) player.getAttributeValue(ModAttributes.MAX_MANA);
                        mp.cranksdungeons$setCurrentMana(Math.min(maxMana, mp.cranksdungeons$getCurrentMana() + (float) gainedMana));
                    }
                }
            }
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
        updateElytraSpeed(entity);
    }

    @Unique
    private void updateElytraSpeed(LivingEntity entity) {
        if (entity.isFallFlying()) {
            double flyingSpeed = entity.getAttributeValue(ModAttributes.FLYING_SPEED);
            if (flyingSpeed > 0) {
                // flyingSpeed is roughly 0.15 to 0.35
                net.minecraft.util.math.Vec3d look = entity.getRotationVector();
                // Apply a small continuous thrust in the direction the player is looking
                entity.addVelocity(look.x * flyingSpeed * 0.03, look.y * flyingSpeed * 0.03, look.z * flyingSpeed * 0.03);
            }
        }
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

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void awardCombatXp(net.minecraft.entity.damage.DamageSource source, CallbackInfo ci) {
        if (((LivingEntity) (Object) this).getWorld().isClient()) return;
        
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof net.minecraft.entity.mob.MobEntity mob)) return;
        
        int baseXP = ((MobEntityAccessor) mob).getExperiencePoints();
        if (baseXP <= 0) return;

        net.minecraft.server.MinecraftServer server = entity.getServer();
        if (server == null) return;

        // Group damage by Party or Solo UUID
        java.util.Map<Object, Float> teamDamage = new java.util.HashMap<>();
        
        if (entity instanceof com.cranks.dungeons.util.MobDamageTracker tracker) {
            java.util.Map<java.util.UUID, Float> contributions = tracker.cranks$getDamageContributions();
            for (java.util.Map.Entry<java.util.UUID, Float> entry : contributions.entrySet()) {
                java.util.UUID uuid = entry.getKey();
                float damage = entry.getValue();
                
                java.util.Optional<com.cranks.dungeons.party.Party> optParty = com.cranks.dungeons.party.PartyManager.getParty(uuid);
                Object key = optParty.isPresent() ? optParty.get() : uuid;
                teamDamage.merge(key, damage, Float::sum);
            }
        }
        
        Object winner = null;
        float maxDamage = -1;
        
        // Find winner
        for (java.util.Map.Entry<Object, Float> entry : teamDamage.entrySet()) {
            if (entry.getValue() > maxDamage) {
                maxDamage = entry.getValue();
                winner = entry.getKey();
            }
        }
        
        // Fallback to old logic if no damage was tracked but there's an attacking player
        if (winner == null) {
            net.minecraft.entity.player.PlayerEntity attacker = null;
            if (source.getAttacker() instanceof net.minecraft.entity.player.PlayerEntity player) {
                attacker = player;
            } else {
                attacker = ((LivingEntityAccessor) this).cranks$getAttackingPlayer();
            }
            if (attacker != null) {
                java.util.Optional<com.cranks.dungeons.party.Party> optParty = com.cranks.dungeons.party.PartyManager.getParty(attacker.getUuid());
                winner = optParty.isPresent() ? optParty.get() : attacker.getUuid();
            }
        }

        if (winner instanceof com.cranks.dungeons.party.Party party) {
            java.util.List<net.minecraft.server.network.ServerPlayerEntity> eligiblePlayers = new java.util.ArrayList<>();
            double maxBonus = 0;
            for (java.util.UUID memberId : party.getMembers()) {
                net.minecraft.server.network.ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
                if (member != null && member.getWorld() == entity.getWorld() && member.squaredDistanceTo(entity) <= 10000) {
                    eligiblePlayers.add(member);
                    maxBonus = Math.max(maxBonus, member.getAttributeValue(ModAttributes.COMBAT_EXPERIENCE));
                }
            }
            
            if (maxBonus > 0) {
                baseXP += (int)(baseXP * maxBonus);
            }

            int n = eligiblePlayers.size();
            if (n > 1) {
                int sharedXp = (int) Math.ceil((baseXP / (double) n) * 1.2);
                for (net.minecraft.server.network.ServerPlayerEntity member : eligiblePlayers) {
                    com.cranks.dungeons.util.CombatXpContext.setCombatXp(true);
                    member.addExperience(sharedXp);
                    com.cranks.dungeons.util.CombatXpContext.setCombatXp(false);
                }
            } else if (n == 1) {
                com.cranks.dungeons.util.CombatXpContext.setCombatXp(true);
                eligiblePlayers.get(0).addExperience(baseXP);
                com.cranks.dungeons.util.CombatXpContext.setCombatXp(false);
            }
        } else if (winner instanceof java.util.UUID soloUuid) {
            net.minecraft.server.network.ServerPlayerEntity player = server.getPlayerManager().getPlayer(soloUuid);
            if (player != null && player.getWorld() == entity.getWorld() && player.squaredDistanceTo(entity) <= 10000) {
                double bonus = player.getAttributeValue(ModAttributes.COMBAT_EXPERIENCE);
                if (bonus > 0) {
                    baseXP += (int)(baseXP * bonus);
                }
                
                com.cranks.dungeons.util.CombatXpContext.setCombatXp(true);
                player.addExperience(baseXP);
                com.cranks.dungeons.util.CombatXpContext.setCombatXp(false);
            }
        }
    }


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

    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    private void modifyCombatXp(net.minecraft.entity.Entity attacker, CallbackInfo ci) {
        if ((Object) this instanceof net.minecraft.entity.mob.MobEntity) {
            // Unconditionally cancel physical XP drops for mobs.
            // We handle all combat XP distribution manually in onDeath(TAIL).
            ci.cancel();
        }
    }

    /**
     * Safely compares two RegistryEntry<EntityAttribute> references.
     * Fabric's registry can produce different RegistryEntry objects for the same attribute,
     * so we fall back to comparing translation keys.
     */
    private static boolean attributesMatch(RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr1,
                                           RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr2) {
        return com.cranks.dungeons.util.CombatUtility.attributesMatch(attr1, attr2);
    }
}

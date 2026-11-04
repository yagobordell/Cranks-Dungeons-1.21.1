package com.cranks.dungeons.horse;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.mount.MountDurabilityProfile;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class HorseBreedingManager {
    private static final int CURRENT_DATA_VERSION = 4;
    public static final int MAX_LEVEL = 30;
    public static final int MAX_BREEDS = 3;
    public static final long BREEDING_COOLDOWN_TICKS = 36_000L;
    public static final long CARE_COOLDOWN_TICKS = 3_000L;

    private static final double MIN_HEALTH = HorseBreedingRules.MIN_HEALTH;
    private static final double MAX_HEALTH = HorseBreedingRules.MAX_HEALTH;
    private static final double WILD_VANILLA_MIN_HEALTH = 15.0;
    private static final double WILD_VANILLA_MAX_HEALTH = 30.0;
    private static final double LEGACY_MAX_HEALTH = 45.0;
    private static final double PRE_DURABILITY_MIN_HEALTH = 50.0;
    private static final double PRE_DURABILITY_MAX_HEALTH = 90.0;
    private static final double PRE_GENE_EXPANSION_MAX_HEALTH = 40.0;
    private static final double MIN_SPEED = HorseBreedingRules.MIN_SPEED;
    private static final double MAX_SPEED = HorseBreedingRules.MAX_SPEED;
    private static final double MIN_JUMP = HorseBreedingRules.MIN_JUMP;
    private static final double MAX_JUMP = HorseBreedingRules.MAX_JUMP;
    private static final double MIN_EVASION = HorseBreedingRules.MIN_EVASION;
    private static final double MAX_EVASION = HorseBreedingRules.MAX_EVASION;
    private static final double MIN_ENDURANCE = HorseBreedingRules.MIN_ENDURANCE;
    private static final double MAX_ENDURANCE = HorseBreedingRules.MAX_ENDURANCE;
    private static final double BETTER_PARENT_CHANCE = 0.70;
    private static final double MUTATION_CHANCE = 0.05;
    private static final double POSITIVE_MUTATION_CHANCE = 0.80;
    private static final double BASE_GALLOP_BONUS = 0.15;
    private static final double ROAD_SPEED_BONUS = 0.10;
    private static final double MAX_MOBILITY_SPEED = 0.80;
    private static final int STAMINA_REGEN_DELAY_TICKS = 40;
    private static final TagKey<Block> HORSE_ROADS = TagKey.of(
            RegistryKeys.BLOCK, id("horse_roads"));

    private static final Identifier TRAINING_HEALTH = id("horse_training_health");
    private static final Identifier TRAINING_SPEED = id("horse_training_speed");
    private static final Identifier TRAINING_JUMP = id("horse_training_jump");
    private static final Identifier TRAINING_EVASION = id("horse_training_evasion");
    private static final Identifier TRAIT_HEALTH = id("horse_trait_health");
    private static final Identifier TRAIT_SPEED = id("horse_trait_speed");
    private static final Identifier TRAIT_JUMP = id("horse_trait_jump");
    private static final Identifier TRAIT_ARMOR = id("horse_trait_armor");
    private static final Identifier TRAIT_EVASION = id("horse_trait_evasion");
    private static final Identifier TRAIT_KNOCKBACK = id("horse_trait_knockback");
    private static final Identifier IMPRINT_HEALTH = id("horse_imprint_health");
    private static final Identifier BASE_ARMOR = id("horse_base_armor");
    private static final Identifier GENETIC_ARMOR = id("horse_genetic_armor");
    private static final Identifier IMPRINT_SPEED = id("horse_imprint_speed");
    private static final Identifier IMPRINT_JUMP = id("horse_imprint_jump");
    private static final Identifier MOBILITY_SPEED = id("horse_mobility_speed");
    private static final Identifier TRAINING_MOVEMENT_EFFICIENCY = id("horse_training_movement_efficiency");
    private static final Identifier TRAINING_WATER_EFFICIENCY = id("horse_training_water_efficiency");
    private static final Identifier TRAINING_STEP_HEIGHT = id("horse_training_step_height");
    private static final Identifier TRAINING_SAFE_FALL = id("horse_training_safe_fall");
    private static final Identifier TRAINING_FALL_DAMAGE = id("horse_training_fall_damage");
    private static final Identifier TRAIT_STEP_HEIGHT = id("horse_trait_step_height");
    private static final Identifier TRAIT_SAFE_FALL = id("horse_trait_safe_fall");
    private static final Identifier TRAIT_FALL_DAMAGE = id("horse_trait_fall_damage");

    private HorseBreedingManager() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!(target instanceof HorseEntity horse)) {
                return ActionResult.PASS;
            }

            boolean breedingFood = player.getStackInHand(hand).isOf(Items.GOLDEN_CARROT)
                    || player.getStackInHand(hand).isOf(Items.GOLDEN_APPLE);
            if (!world.isClient() && !horse.isBaby() && breedingFood) {
                warnAboutBlockedBreeding(player, horse);
            }

            if (!horse.isBaby() || !player.getStackInHand(hand).isOf(Items.SUGAR)) {
                return ActionResult.PASS;
            }
            if (world.isClient()) return ActionResult.SUCCESS;

            HorseGeneticsData data = ensureInitialized(horse);
            if (data.imprintOwnerId == null || !data.imprintOwnerId.equals(player.getUuid())) {
                player.sendMessage(Text.literal("Only this foal's breeder can imprint it."), true);
                return ActionResult.FAIL;
            }
            if (data.imprintProgress >= 1.0f) {
                player.sendMessage(Text.literal("This foal is already fully imprinted."), true);
                return ActionResult.FAIL;
            }

            long worldTime = world.getTime();
            if (worldTime < data.nextCareTime) {
                player.sendMessage(Text.literal("The foal needs more time before its next care."), true);
                return ActionResult.FAIL;
            }

            if (!player.isCreative()) player.getStackInHand(hand).decrement(1);
            data.imprintProgress = Math.min(1.0f, data.imprintProgress + 0.25f);
            data.nextCareTime = worldTime + CARE_COOLDOWN_TICKS;
            data.modifiersDirty = true;
            player.sendMessage(Text.literal("Foal imprint: " + Math.round(data.imprintProgress * 100.0f) + "%"), true);
            sync(horse);
            return ActionResult.SUCCESS;
        });
    }

    private static void warnAboutBlockedBreeding(PlayerEntity player, HorseEntity horse) {
        HorseGeneticsData data = ensureInitialized(horse);
        long time = horse.getEntityWorld().getTime();
        if (data.breedsRemaining <= 0) {
            player.sendMessage(Text.literal("This horse has no breeding attempts remaining."), true);
            return;
        }
        if (time < data.nextBreedTime) {
            player.sendMessage(Text.literal("This horse is still on breeding cooldown."), true);
            return;
        }

        HorseEntity mate = horse.getEntityWorld().getEntitiesByClass(
                HorseEntity.class, horse.getBoundingBox().expand(8.0),
                candidate -> candidate != horse && candidate.isInLove()).stream().findFirst().orElse(null);
        if (mate == null) return;
        HorseGeneticsData mateData = ensureInitialized(mate);
        if (mateData.breedsRemaining <= 0) {
            player.sendMessage(Text.literal("The nearby mate has no breeding attempts remaining."), true);
        } else if (data.male == mateData.male) {
            player.sendMessage(Text.literal("Horse breeding requires one male and one female."), true);
        } else if (time < mateData.nextBreedTime) {
            player.sendMessage(Text.literal("The nearby mate is still on breeding cooldown."), true);
        } else if (areRelated(horse, data, mate, mateData)) {
            player.sendMessage(Text.literal("These horses are too closely related to breed."), true);
        }
    }

    public static HorseGeneticsData data(HorseEntity horse) {
        return ((HorseGeneticsProvider) horse).cranksdungeons$getHorseGenetics();
    }

    public static HorseGeneticsData ensureInitialized(HorseEntity horse) {
        HorseGeneticsData data = data(horse);
        if (data.initialized) {
            migrateLegacyData(horse, data);
            return data;
        }

        data.dataVersion = CURRENT_DATA_VERSION;
        data.initialized = true;
        data.male = horse.getRandom().nextBoolean();
        double vanillaHealth = horse.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
        double wildHealthQuality = normalized(
                vanillaHealth, WILD_VANILLA_MIN_HEALTH, WILD_VANILLA_MAX_HEALTH);
        data.geneticHealth = MIN_HEALTH
                + (MountGeneticRatings.naturalMaximum(MountKind.HORSE, 0) - MIN_HEALTH)
                * wildHealthQuality;
        data.geneticSpeed = clamp(horse.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
                MIN_SPEED, MAX_SPEED);
        data.geneticJump = clamp(horse.getAttributeBaseValue(EntityAttributes.GENERIC_JUMP_STRENGTH),
                MIN_JUMP, MAX_JUMP);
        data.geneticEvasion = horse.getRandom().nextDouble()
                * MountGeneticRatings.naturalMaximum(MountKind.HORSE, 3);
        data.geneticEndurance = MountGeneticRatings.minimum(MountKind.HORSE, 4)
                + horse.getRandom().nextDouble() * (MountGeneticRatings.naturalMaximum(MountKind.HORSE, 4)
                - MountGeneticRatings.minimum(MountKind.HORSE, 4));
        data.geneticArmor = MountGeneticRatings.minimum(MountKind.HORSE, 5)
                + horse.getRandom().nextDouble() * (MountGeneticRatings.naturalMaximum(MountKind.HORSE, 5)
                - MountGeneticRatings.minimum(MountKind.HORSE, 5));
        data.breedsRemaining = MAX_BREEDS;
        data.trait = rollWildTrait(horse);
        data.currentStamina = getMaxStamina(data);
        applyGeneticBases(horse, data);
        data.modifiersDirty = true;
        applyModifiers(horse, data);
        horse.setHealth(horse.getMaxHealth());
        sync(horse);
        return data;
    }

    public static void inherit(HorseEntity firstParent, HorseEntity secondParent, HorseEntity child) {
        HorseGeneticsData first = ensureInitialized(firstParent);
        HorseGeneticsData second = ensureInitialized(secondParent);
        HorseGeneticsData inherited = data(child);
        if (inherited.initialized) return;

        inherited.initialized = true;
        inherited.dataVersion = CURRENT_DATA_VERSION;
        inherited.male = child.getRandom().nextBoolean();
        inherited.generation = Math.max(first.generation, second.generation) + 1;
        inherited.breedsRemaining = MAX_BREEDS;

        double[] firstGenes = {first.geneticHealth, first.geneticSpeed, first.geneticJump,
                first.geneticEvasion, first.geneticEndurance, first.geneticArmor};
        double[] secondGenes = {second.geneticHealth, second.geneticSpeed, second.geneticJump,
                second.geneticEvasion, second.geneticEndurance, second.geneticArmor};
        double[] minimums = {MIN_HEALTH, MIN_SPEED, MIN_JUMP, MIN_EVASION, MIN_ENDURANCE,
                HorseBreedingRules.MIN_ARMOR};
        double[] maximums = {MAX_HEALTH, MAX_SPEED, MAX_JUMP, MAX_EVASION, MAX_ENDURANCE,
                HorseBreedingRules.MAX_ARMOR};
        boolean[] betterParent = rollInheritanceSources(child, first.breedingFocus, second.breedingFocus);
        MutationResult[] genes = new MutationResult[6];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = inheritStat(child, firstGenes[i], secondGenes[i], minimums[i], maximums[i],
                    betterParent[i], inherited.generation);
        }

        int lineageGene = chooseLineageGene(child, first.breedingFocus, second.breedingFocus);
        double betterLineageValue = Math.max(firstGenes[lineageGene], secondGenes[lineageGene]);
        double lineageQuality = normalized(
                betterLineageValue, minimums[lineageGene], maximums[lineageGene]);
        double lineageAmount = HorseBreedingRules.lineageImprovement(
                first.breedingFocus, second.breedingFocus, lineageQuality);
        MutationResult lineageResult = genes[lineageGene];
        genes[lineageGene] = new MutationResult(clamp(
                Math.max(lineageResult.value, betterLineageValue)
                        + (maximums[lineageGene] - minimums[lineageGene]) * lineageAmount,
                minimums[lineageGene], maximums[lineageGene]), lineageResult.mutations,
                lineageResult.delta, lineageResult.positive);

        inherited.geneticHealth = genes[0].value;
        inherited.geneticSpeed = genes[1].value;
        inherited.geneticJump = genes[2].value;
        inherited.geneticEvasion = genes[3].value;
        inherited.geneticEndurance = genes[4].value;
        inherited.geneticArmor = genes[5].value;
        inherited.personalGeneMutationMask = mutationMask(genes);
        inherited.personalPositiveMutationMask = positiveMutationMask(genes);
        inherited.healthMutation = genes[0].delta;
        inherited.speedMutation = genes[1].delta;
        inherited.jumpMutation = genes[2].delta;
        inherited.evasionMutation = genes[3].delta;
        inherited.enduranceMutation = genes[4].delta;
        inherited.armorMutation = genes[5].delta;
        TraitResult trait = inheritTrait(child, first.trait, second.trait);
        inherited.mutations = first.mutations + second.mutations
                + genes[0].mutations + genes[1].mutations + genes[2].mutations
                + genes[3].mutations + genes[4].mutations + genes[5].mutations
                + trait.mutations;
        inherited.trait = trait.value;
        inherited.traitMutated = trait.mutations > 0;
        inherited.traitMutationFrom = trait.inheritedValue;
        inherited.traitMutationTo = trait.value;
        inherited.breedingFocus = HorseBreedingFocus.NONE;

        HorseGeneticsData sire = first.male ? first : second;
        HorseGeneticsData dam = first.male ? second : first;
        HorseEntity sireHorse = first.male ? firstParent : secondParent;
        HorseEntity damHorse = first.male ? secondParent : firstParent;
        inherited.sireId = sireHorse.getUuid();
        inherited.damId = damHorse.getUuid();
        inherited.sireName = displayName(sireHorse);
        inherited.damName = displayName(damHorse);
        inherited.ancestry.add(sireHorse.getUuid());
        inherited.ancestry.add(damHorse.getUuid());
        inherited.ancestry.addAll(sire.ancestry);
        inherited.ancestry.addAll(dam.ancestry);
        trimAncestry(inherited.ancestry, 16);

        ServerPlayerEntity breeder = firstParent.getLovingPlayer();
        if (breeder == null) breeder = secondParent.getLovingPlayer();
        if (breeder != null) {
            inherited.imprintOwnerId = breeder.getUuid();
            inherited.imprintOwnerName = breeder.getName().getString();
            inherited.nextCareTime = child.getEntityWorld().getTime() + 1_200L;
        }

        inherited.currentStamina = getMaxStamina(inherited);
        applyGeneticBases(child, inherited);
        inherited.modifiersDirty = true;
        applyModifiers(child, inherited);
        child.setHealth(child.getMaxHealth());

        long nextBreedTime = child.getEntityWorld().getTime() + BREEDING_COOLDOWN_TICKS;
        first.breedsRemaining = Math.max(0, first.breedsRemaining - 1);
        second.breedsRemaining = Math.max(0, second.breedsRemaining - 1);
        first.nextBreedTime = nextBreedTime;
        second.nextBreedTime = nextBreedTime;

        // Vanilla already has occasional random coats; this is the rarer explicit mutation roll.
        if (child.getRandom().nextDouble() < 0.02) {
            HorseColor[] colors = HorseColor.values();
            child.setVariant(colors[child.getRandom().nextInt(colors.length)]);
            inherited.mutations++;
            inherited.coatMutated = true;
        }

        sync(firstParent);
        sync(secondParent);
        sync(child);
    }

    public static boolean canMate(HorseEntity first, HorseEntity second) {
        HorseGeneticsData a = ensureInitialized(first);
        HorseGeneticsData b = ensureInitialized(second);
        long time = first.getEntityWorld().getTime();
        return a.male != b.male
                && a.breedsRemaining > 0 && b.breedsRemaining > 0
                && time >= a.nextBreedTime && time >= b.nextBreedTime
                && !areRelated(first, a, second, b);
    }

    public static boolean areRelated(HorseEntity first, HorseGeneticsData firstData,
                                     HorseEntity second, HorseGeneticsData secondData) {
        UUID firstId = first.getUuid();
        UUID secondId = second.getUuid();
        if (firstId.equals(secondId)) return true;
        if (firstData.ancestry.contains(secondId) || secondData.ancestry.contains(firstId)) return true;
        for (UUID ancestor : firstData.ancestry) {
            if (secondData.ancestry.contains(ancestor)) return true;
        }
        return false;
    }

    public static void tick(HorseEntity horse) {
        if (horse.getEntityWorld().isClient()) return;
        HorseGeneticsData data = ensureInitialized(horse);
        applyGeneticBases(horse, data);
        data.currentStamina = Math.min(data.currentStamina, getMaxStamina(data));

        Entity rider = horse.getControllingPassenger();
        UUID imprintRider = rider instanceof PlayerEntity ? rider.getUuid() : null;
        if (!java.util.Objects.equals(data.activeImprintRider, imprintRider)) {
            data.activeImprintRider = imprintRider;
            data.modifiersDirty = true;
        }

        double x = horse.getX();
        double z = horse.getZ();
        if (Double.isNaN(data.lastX)) {
            data.lastX = x;
            data.lastZ = z;
        }
        double distance = Math.sqrt(MathHelper.square(x - data.lastX) + MathHelper.square(z - data.lastZ));
        data.lastX = x;
        data.lastZ = z;

        boolean ridden = rider instanceof PlayerEntity && !horse.isBaby();
        if (!ridden && data.gallopRequested) data.gallopRequested = false;
        boolean onRoad = ridden && isRoadSurface(horse);
        boolean moving = distance >= 0.01 && distance < 8.0;
        boolean galloping = ridden && moving && data.gallopRequested && !data.exhausted;
        if (onRoad != data.onRoad || galloping != data.galloping) {
            data.onRoad = onRoad;
            data.galloping = galloping;
            data.modifiersDirty = true;
        }

        if (ridden && distance < 8.0) {
            data.distanceAccumulator += distance;
            while (data.distanceAccumulator >= 10.0) {
                data.distanceAccumulator -= 10.0;
                addExperience(horse, HorseBreedingRules.travelExperience(data.galloping));
            }
            if (data.wasOnGround && !horse.isOnGround() && horse.getVelocity().y > 0.1) {
                addExperience(horse, 3);
                data.trackingLongJump = true;
                data.longJumpDistance = 0.0;
            }
        }
        if (data.trackingLongJump) {
            if (distance < 8.0) data.longJumpDistance += distance;
            if (horse.isOnGround()) {
                addExperience(horse, HorseBreedingRules.longJumpExperience(data.longJumpDistance));
                data.trackingLongJump = false;
                data.longJumpDistance = 0.0;
            }
        }
        if (data.galloping) {
            spendStamina(data, gallopDrainPerSecond(data) / 20.0);
            if (data.currentStamina <= 0.01) {
                data.galloping = false;
                data.modifiersDirty = true;
            }
        } else if (horse.isOnGround()) {
            regenerateStamina(data, ridden);
        } else if (data.staminaRegenDelay > 0) {
            data.staminaRegenDelay--;
        }
        data.wasOnGround = horse.isOnGround();

        boolean exhausted = data.exhausted
                ? data.currentStamina < getMaxStamina(data) * 0.20
                : data.currentStamina <= 0.01;
        if (exhausted != data.exhausted) {
            data.exhausted = exhausted;
            data.modifiersDirty = true;
        }
        if (data.modifiersDirty) applyModifiers(horse, data);

        UUID ownerUuid = horse.isTame() ? horse.getOwnerUuid() : null;
        boolean ownershipChanged = data.ownershipSyncInitialized
                && (data.lastSyncedTamed != horse.isTame()
                || !java.util.Objects.equals(data.lastSyncedOwnerUuid, ownerUuid));
        data.ownershipSyncInitialized = true;
        data.lastSyncedTamed = horse.isTame();
        data.lastSyncedOwnerUuid = ownerUuid;
        if (ownershipChanged) sync(horse);

        data.syncTicker++;
        int syncInterval = ridden || horse.isBaby() ? 10 : 100;
        if (data.syncTicker >= syncInterval) {
            data.syncTicker = 0;
            sync(horse);
        }
    }

    public static void addCombatExperience(HorseEntity horse, float defeatedMaxHealth) {
        if (horse.getEntityWorld().isClient() || horse.isBaby()) return;
        addExperience(horse, Math.max(5, Math.min(50, Math.round(defeatedMaxHealth))));
    }

    public static void addExperience(HorseEntity horse, int amount) {
        if (amount <= 0) return;
        HorseGeneticsData data = ensureInitialized(horse);
        if (data.level >= MAX_LEVEL) return;
        data.experience += amount;
        boolean leveled = false;
        while (data.level < MAX_LEVEL && data.experience >= experienceForNextLevel(data.level)) {
            data.experience -= experienceForNextLevel(data.level);
            data.level++;
            leveled = true;
        }
        if (leveled) {
            data.modifiersDirty = true;
            Entity rider = horse.getControllingPassenger();
            if (rider instanceof PlayerEntity player) {
                player.sendMessage(Text.literal(displayName(horse) + " reached training level " + data.level + "!"), true);
            }
        }
    }

    public static int experienceForNextLevel(int level) {
        return HorseBreedingRules.experienceForNextLevel(level, MAX_LEVEL);
    }

    public static double getMaxStamina(HorseGeneticsData data) {
        double maximum = data.geneticEndurance + Math.max(0, data.level - 1) * 1.5;
        return data.trait == HorseTrait.HARDY ? maximum * 1.15 : maximum;
    }

    public static double mountedStaminaRegenPerSecond(HorseGeneticsData data) {
        double regeneration = 2.0 + (data.level >= 10 ? 0.5 : 0.0);
        return data.trait == HorseTrait.HARDY ? regeneration * 1.25 : regeneration;
    }

    public static double unmountedStaminaRegenPerSecond(HorseGeneticsData data) {
        double regeneration = 4.0;
        return data.trait == HorseTrait.HARDY ? regeneration * 1.25 : regeneration;
    }

    public static double gallopDrainPerSecond(HorseGeneticsData data) {
        return data.level >= 25 ? 3.0 : 4.0;
    }

    public static double jumpStaminaCost(HorseGeneticsData data) {
        double cost = 8.0;
        if (data.level >= 15) cost -= 2.0;
        if (data.trait == HorseTrait.SURE_FOOTED) cost -= 2.0;
        return Math.max(4.0, cost);
    }

    /** Spends horse jump stamina at the jump itself, independent of movement distance. */
    public static void spendJumpStamina(HorseEntity horse) {
        if (horse.getEntityWorld().isClient()
                || !(horse.getControllingPassenger() instanceof PlayerEntity)) return;
        HorseGeneticsData data = ensureInitialized(horse);
        spendStamina(data, jumpStaminaCost(data));
        sync(horse);
    }

    public static double gallopSpeedBonus(HorseGeneticsData data) {
        double bonus = BASE_GALLOP_BONUS;
        if (data.level >= 30) bonus += 0.05;
        if (data.trait == HorseTrait.SWIFT) bonus += 0.05;
        return bonus;
    }

    public static void setGallopRequested(HorseEntity horse, PlayerEntity rider, boolean requested) {
        if (horse.getControllingPassenger() != rider || horse.isBaby()) return;
        HorseGeneticsData data = ensureInitialized(horse);
        data.gallopRequested = requested;
        if (!requested && data.galloping) {
            data.galloping = false;
            data.modifiersDirty = true;
        }
    }

    public static void setBreedingFocus(HorseEntity horse, PlayerEntity player, HorseBreedingFocus focus) {
        HorseGeneticsData data = ensureInitialized(horse);
        UUID owner = horse.getOwnerUuid();
        boolean allowed = player.isCreative() || owner == null || owner.equals(player.getUuid())
                || player.getUuid().equals(data.imprintOwnerId);
        if (!horse.isTame() || !allowed || player.squaredDistanceTo(horse) > 64.0) return;
        data.breedingFocus = focus == null ? HorseBreedingFocus.NONE : focus;
        sync(horse);
    }

    public static String grade(HorseGeneticsData data) {
        return MountGeneticRatings.overall(MountKind.HORSE,
                new double[]{data.geneticHealth, data.geneticSpeed, data.geneticJump,
                        data.geneticEvasion, data.geneticEndurance, data.geneticArmor}, data.generation);
    }

    public static void sync(HorseEntity horse) {
        if (horse.getEntityWorld().isClient() || !data(horse).initialized) return;
        HorseGeneticsSyncPacket packet = HorseGeneticsSyncPacket.from(horse);
        Set<ServerPlayerEntity> recipients = new HashSet<>(PlayerLookup.tracking(horse));
        if (horse.getControllingPassenger() instanceof ServerPlayerEntity rider) recipients.add(rider);
        for (ServerPlayerEntity recipient : recipients) {
            ServerPlayNetworking.send(recipient, packet);
        }
    }

    /**
     * Sends the complete genetics record to the player opening a horse screen.
     * Tracking packets are normally enough, but a screen can be opened during
     * the same tick in which the horse first becomes visible to the client.
     * Sending directly here prevents the breeding panels from displaying the
     * gray "Syncing..." placeholder in that race.
     */
    public static void syncToPlayer(ServerPlayerEntity player, HorseEntity horse) {
        if (player == null || horse.getEntityWorld().isClient()) return;
        ensureInitialized(horse);
        ServerPlayNetworking.send(player, HorseGeneticsSyncPacket.from(horse));
    }

    private static void migrateLegacyData(HorseEntity horse, HorseGeneticsData data) {
        if (data.dataVersion >= CURRENT_DATA_VERSION) return;

        float previousMaximum = horse.getMaxHealth();
        float healthRatio = previousMaximum > 0.0f
                ? MathHelper.clamp(horse.getHealth() / previousMaximum, 0.0f, 1.0f)
                : 1.0f;
        if (data.dataVersion < 2) {
            double legacyQuality = normalized(
                    data.geneticHealth, WILD_VANILLA_MIN_HEALTH, LEGACY_MAX_HEALTH);
            data.geneticHealth = PRE_DURABILITY_MIN_HEALTH
                    + (PRE_DURABILITY_MAX_HEALTH - PRE_DURABILITY_MIN_HEALTH) * legacyQuality;
            data.dataVersion = 2;
        }
        if (data.dataVersion < 3) {
            double healthQuality = normalized(data.geneticHealth,
                    PRE_DURABILITY_MIN_HEALTH, PRE_DURABILITY_MAX_HEALTH);
            data.geneticHealth = MIN_HEALTH
                    + (PRE_GENE_EXPANSION_MAX_HEALTH - MIN_HEALTH) * healthQuality;
            data.geneticArmor = 0.0;
            data.dataVersion = 3;
        }
        if (data.dataVersion < 4) {
            data.geneticHealth = MathHelper.clamp(data.geneticHealth * 1.25, MIN_HEALTH, MAX_HEALTH);
            data.geneticArmor = MathHelper.clamp(data.geneticArmor * 3.0,
                    HorseBreedingRules.MIN_ARMOR, HorseBreedingRules.MAX_ARMOR);
            data.dataVersion = 4;
        }
        data.modifiersDirty = true;

        applyGeneticBases(horse, data);
        applyModifiers(horse, data);
        horse.setHealth(horse.getMaxHealth() * healthRatio);
        sync(horse);
    }

    private static void applyGeneticBases(HorseEntity horse, HorseGeneticsData data) {
        MountDurabilityProfile durability = MountDurabilityProfile.forKind(MountKind.HORSE);
        setBase(horse, EntityAttributes.GENERIC_MAX_HEALTH,
                durability.baseHealth() + data.geneticHealth);
        setBase(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED, data.geneticSpeed);
        setBase(horse, EntityAttributes.GENERIC_JUMP_STRENGTH, data.geneticJump);
        setBase(horse, ModAttributes.EVASION_RATING, data.geneticEvasion);
        setBase(horse, ModAttributes.HORSE_ENDURANCE, data.geneticEndurance);
    }

    private static void applyModifiers(HorseEntity horse, HorseGeneticsData data) {
        data.modifiersDirty = false;
        int trainedLevels = Math.max(0, data.level - 1);
        setModifier(horse, EntityAttributes.GENERIC_MAX_HEALTH, TRAINING_HEALTH,
                trainedLevels * MountDurabilityProfile.HEALTH_PER_LEVEL,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED, TRAINING_SPEED,
                trainedLevels * 0.005, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, EntityAttributes.GENERIC_JUMP_STRENGTH, TRAINING_JUMP,
                trainedLevels * 0.0035, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, ModAttributes.EVASION_RATING, TRAINING_EVASION,
                trainedLevels * 2.0, EntityAttributeModifier.Operation.ADD_VALUE);

        setModifier(horse, EntityAttributes.GENERIC_MAX_HEALTH, TRAIT_HEALTH,
                data.trait == HorseTrait.HARDY ? 0.10 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED, TRAIT_SPEED,
                data.trait == HorseTrait.SWIFT ? 0.05 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, EntityAttributes.GENERIC_JUMP_STRENGTH, TRAIT_JUMP,
                data.trait == HorseTrait.SURE_FOOTED ? 0.08 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, ModAttributes.KNOCKBACK_RESISTANCE, TRAIT_KNOCKBACK,
                data.trait == HorseTrait.SURE_FOOTED ? 0.15 : data.trait == HorseTrait.WARHORSE ? 0.20 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        MountDurabilityProfile durability = MountDurabilityProfile.forKind(MountKind.HORSE);
        setModifier(horse, EntityAttributes.GENERIC_ARMOR, TRAIT_ARMOR,
                data.trait == HorseTrait.WARHORSE ? durability.baseArmor() * 0.30 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_ARMOR, BASE_ARMOR,
                durability.baseArmor(), EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_ARMOR, GENETIC_ARMOR,
                data.geneticArmor, EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, ModAttributes.EVASION_RATING, TRAIT_EVASION,
                data.trait == HorseTrait.WARHORSE ? 20.0 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);

        boolean imprintedRider = data.imprintOwnerId != null
                && data.imprintOwnerId.equals(data.activeImprintRider);
        double imprintBonus = imprintedRider ? data.imprintProgress * 0.20 : 0.0;
        setModifier(horse, EntityAttributes.GENERIC_MAX_HEALTH, IMPRINT_HEALTH,
                imprintBonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED, IMPRINT_SPEED,
                imprintBonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(horse, EntityAttributes.GENERIC_JUMP_STRENGTH, IMPRINT_JUMP,
                imprintBonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        double movementEfficiency = data.level >= 5 ? 0.20 : 0.0;
        if (data.trait == HorseTrait.SWIFT) movementEfficiency += 0.10;
        setModifier(horse, EntityAttributes.GENERIC_MOVEMENT_EFFICIENCY, TRAINING_MOVEMENT_EFFICIENCY,
                movementEfficiency, EntityAttributeModifier.Operation.ADD_VALUE);
        double waterEfficiency = 0.25 + (data.level >= 20 ? 0.15 : 0.0)
                + (data.trait == HorseTrait.HARDY ? 0.10 : 0.0);
        setModifier(horse, EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, TRAINING_WATER_EFFICIENCY,
                waterEfficiency, EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_STEP_HEIGHT, TRAINING_STEP_HEIGHT,
                data.level >= 20 ? 0.25 : 0.0, EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_STEP_HEIGHT, TRAIT_STEP_HEIGHT,
                data.trait == HorseTrait.SURE_FOOTED ? 0.15 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, TRAINING_SAFE_FALL,
                data.level >= 20 ? 2.0 : 0.0, EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, TRAIT_SAFE_FALL,
                data.trait == HorseTrait.SURE_FOOTED ? 2.0 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(horse, EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, TRAINING_FALL_DAMAGE,
                data.level >= 20 ? -0.20 : 0.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        setModifier(horse, EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, TRAIT_FALL_DAMAGE,
                data.trait == HorseTrait.SURE_FOOTED ? -0.30 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyMobilitySpeed(horse, data);
        horse.setHealth(Math.min(horse.getHealth(), horse.getMaxHealth()));
    }

    private static void setBase(HorseEntity horse, RegistryEntry<EntityAttribute> attribute, double value) {
        EntityAttributeInstance instance = horse.getAttributeInstance(attribute);
        if (instance != null && Math.abs(instance.getBaseValue() - value) > 0.000001) {
            instance.setBaseValue(value);
        }
    }

    private static void setModifier(HorseEntity horse, RegistryEntry<EntityAttribute> attribute,
                                    Identifier id, double value, EntityAttributeModifier.Operation operation) {
        EntityAttributeInstance instance = horse.getAttributeInstance(attribute);
        if (instance == null) return;
        EntityAttributeModifier old = instance.getModifier(id);
        if (old != null && Math.abs(old.value() - value) < 0.000001 && old.operation() == operation) return;
        instance.removeModifier(id);
        if (Math.abs(value) > 0.000001) {
            instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
        }
    }

    private static MutationResult inheritStat(HorseEntity child, double first, double second,
                                              double min, double max, boolean inheritBetter,
                                              int generation) {
        double better = Math.max(first, second);
        double weaker = Math.min(first, second);
        double value = inheritBetter ? better : weaker;
        double selectedValue = value;
        int mutations = 0;
        boolean positive = false;
        if (child.getRandom().nextDouble() < MUTATION_CHANCE) {
            positive = child.getRandom().nextDouble() < POSITIVE_MUTATION_CHANCE;
            double direction = positive ? 1.0 : -1.0;
            double generationBonus = Math.min(0.02, Math.max(0, generation) * 0.001);
            double magnitude = 0.02 + child.getRandom().nextDouble() * 0.03 + generationBonus;
            value += direction * (max - min) * magnitude;
            mutations = 1;
        }
        double clamped = clamp(value, min, max);
        return new MutationResult(clamped, mutations, clamped - selectedValue, positive);
    }

    private static HorseTrait rollWildTrait(HorseEntity horse) {
        if (horse.getRandom().nextDouble() >= 0.40) return HorseTrait.NONE;
        HorseTrait[] traits = {HorseTrait.SWIFT, HorseTrait.HARDY, HorseTrait.SURE_FOOTED, HorseTrait.WARHORSE};
        return traits[horse.getRandom().nextInt(traits.length)];
    }

    private static TraitResult inheritTrait(HorseEntity child, HorseTrait first, HorseTrait second) {
        HorseTrait inherited;
        if (first == second && first != HorseTrait.NONE) {
            inherited = first;
        } else if (first == HorseTrait.NONE ^ second == HorseTrait.NONE) {
            HorseTrait positive = first == HorseTrait.NONE ? second : first;
            inherited = child.getRandom().nextDouble() < 0.70 ? positive : HorseTrait.NONE;
        } else {
            double roll = child.getRandom().nextDouble();
            inherited = roll < 0.45 ? first : roll < 0.90 ? second : HorseTrait.NONE;
        }
        if (child.getRandom().nextDouble() < 0.03) {
            return new TraitResult(rollWildTraitForced(child), 1, inherited);
        }
        return new TraitResult(inherited, 0, inherited);
    }

    private static boolean[] rollInheritanceSources(HorseEntity child, HorseBreedingFocus firstFocus,
                                                    HorseBreedingFocus secondFocus) {
        boolean[] better = new boolean[6];
        int firstIndex = focusIndex(firstFocus);
        int secondIndex = focusIndex(secondFocus);
        int count = 0;
        for (int i = 0; i < better.length; i++) {
            better[i] = child.getRandom().nextDouble() < BETTER_PARENT_CHANCE
                    || i == firstIndex || i == secondIndex;
            if (better[i]) count++;
        }
        while (count < 3) {
            int index = child.getRandom().nextInt(better.length);
            if (!better[index]) {
                better[index] = true;
                count++;
            }
        }
        return better;
    }

    private static int chooseLineageGene(HorseEntity child, HorseBreedingFocus first,
                                         HorseBreedingFocus second) {
        int firstIndex = focusIndex(first);
        int secondIndex = focusIndex(second);
        if (firstIndex >= 0 && secondIndex >= 0) {
            return firstIndex == secondIndex || child.getRandom().nextBoolean() ? firstIndex : secondIndex;
        }
        if (firstIndex >= 0) return firstIndex;
        if (secondIndex >= 0) return secondIndex;
        return child.getRandom().nextInt(6);
    }

    private static int focusIndex(HorseBreedingFocus focus) {
        if (focus == null) return -1;
        return switch (focus) {
            case HEALTH -> 0;
            case SPEED -> 1;
            case JUMP -> 2;
            case EVASION -> 3;
            case ENDURANCE -> 4;
            case ARMOR -> 5;
            case NONE -> -1;
        };
    }

    private static void spendStamina(HorseGeneticsData data, double amount) {
        data.currentStamina = Math.max(0.0, data.currentStamina - amount);
        data.staminaRegenDelay = STAMINA_REGEN_DELAY_TICKS;
    }

    private static void regenerateStamina(HorseGeneticsData data, boolean ridden) {
        if (data.staminaRegenDelay > 0) {
            data.staminaRegenDelay--;
            return;
        }
        double rate = ridden ? mountedStaminaRegenPerSecond(data) : unmountedStaminaRegenPerSecond(data);
        data.currentStamina = Math.min(getMaxStamina(data), data.currentStamina + rate / 20.0);
    }

    private static boolean isRoadSurface(HorseEntity horse) {
        BlockPos position = BlockPos.ofFloored(horse.getX(), horse.getBoundingBox().minY - 0.05, horse.getZ());
        return horse.getEntityWorld().getBlockState(position).isIn(HORSE_ROADS);
    }

    private static void applyMobilitySpeed(HorseEntity horse, HorseGeneticsData data) {
        EntityAttributeInstance instance = horse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (instance == null) return;
        instance.removeModifier(MOBILITY_SPEED);
        double bonus = (data.galloping ? gallopSpeedBonus(data) : 0.0)
                + (data.onRoad ? ROAD_SPEED_BONUS : 0.0);
        if (bonus <= 0.0) return;
        double speedWithoutMobility = instance.getValue();
        if (speedWithoutMobility <= 0.0 || speedWithoutMobility >= MAX_MOBILITY_SPEED) return;
        double target = Math.min(MAX_MOBILITY_SPEED, speedWithoutMobility * (1.0 + bonus));
        double modifier = target / speedWithoutMobility - 1.0;
        instance.addTemporaryModifier(new EntityAttributeModifier(
                MOBILITY_SPEED, modifier, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static HorseTrait rollWildTraitForced(HorseEntity horse) {
        HorseTrait[] traits = {HorseTrait.SWIFT, HorseTrait.HARDY, HorseTrait.SURE_FOOTED, HorseTrait.WARHORSE};
        return traits[horse.getRandom().nextInt(traits.length)];
    }

    private static String displayName(HorseEntity horse) {
        return horse.hasCustomName() ? horse.getName().getString() : "Unnamed Horse";
    }

    private static double normalized(double value, double min, double max) {
        return MathHelper.clamp((value - min) / (max - min), 0.0, 1.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void trimAncestry(Set<UUID> ancestry, int maxSize) {
        while (ancestry.size() > maxSize) {
            UUID last = null;
            for (UUID ancestor : ancestry) last = ancestor;
            if (last == null) return;
            ancestry.remove(last);
        }
    }

    private static Identifier id(String path) {
        return Identifier.of(CranksDungeons.MOD_ID, path);
    }

    private static int mutationMask(MutationResult[] genes) {
        int mask = 0;
        for (int i = 0; i < genes.length; i++) if (genes[i].mutations > 0) mask |= 1 << i;
        return mask;
    }

    private static int positiveMutationMask(MutationResult[] genes) {
        int mask = 0;
        for (int i = 0; i < genes.length; i++) {
            if (genes[i].mutations > 0 && genes[i].positive) mask |= 1 << i;
        }
        return mask;
    }

    private record MutationResult(double value, int mutations, double delta, boolean positive) {
    }

    private record TraitResult(HorseTrait value, int mutations, HorseTrait inheritedValue) {
    }
}

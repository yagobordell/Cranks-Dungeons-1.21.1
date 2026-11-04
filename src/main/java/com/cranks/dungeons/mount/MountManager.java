package com.cranks.dungeons.mount;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.screen.MountCargoScreenHandler;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.cranks.dungeons.network.MountDataSyncPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.AnimalEntity;
import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.horse.HorseGeneticsData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Shared mount roles, genetic baselines, chest interaction, and cargo access. */
public final class MountManager {
    private static final long BREEDING_COOLDOWN = 24_000L;
    private static final int MAX_LEVEL = 30;
    private static final int CURRENT_DATA_VERSION = 4;
    private static final Identifier ACTIVE_SPEED = Identifier.of("cranks-dungeons", "mount_active_speed");
    private static final Identifier TRAINING_HEALTH = Identifier.of("cranks-dungeons", "mount_training_health");
    private static final Identifier BASE_ARMOR = Identifier.of("cranks-dungeons", "mount_base_armor");
    private static final Identifier GENETIC_ARMOR = Identifier.of("cranks-dungeons", "mount_genetic_armor");
    private static final Identifier TRAINING_SPEED = Identifier.of("cranks-dungeons", "mount_training_speed");
    private static final Identifier TRAIL_STEP = Identifier.of("cranks-dungeons", "mount_trail_step");
    private static final Identifier TRAIT_HEALTH = Identifier.of("cranks-dungeons", "mount_trait_health");
    private static final Identifier TRAIT_ARMOR = Identifier.of("cranks-dungeons", "mount_trait_armor");
    private static final Identifier TRAIT_SPEED = Identifier.of("cranks-dungeons", "mount_trait_speed");
    private static final Identifier TRAIT_KNOCKBACK = Identifier.of("cranks-dungeons", "mount_trait_knockback");
    private static final Identifier TRAIT_SAFE_FALL = Identifier.of("cranks-dungeons", "mount_trait_safe_fall");
    private static final Identifier SPECIALIZATION_HEALTH = Identifier.of("cranks-dungeons", "skeleton_specialization_health");
    private static final Identifier SPECIALIZATION_ARMOR = Identifier.of("cranks-dungeons", "skeleton_specialization_armor");
    private static final Identifier SPECIALIZATION_SPEED = Identifier.of("cranks-dungeons", "skeleton_specialization_speed");
    private static final Identifier SPECIALIZATION_WATER = Identifier.of("cranks-dungeons", "skeleton_specialization_water");
    private static final TagKey<Block> ROADS = TagKey.of(
            RegistryKeys.BLOCK, Identifier.of("cranks-dungeons", "horse_roads"));

    private MountManager() {
    }

    public static void register() {
        // Camels do not expose vanilla inventory/drop hooks, so their attached
        // chest and cargo are released from the common death event instead.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof CamelEntity camel && entity instanceof MountDataProvider provider) {
                for (ItemStack stack : provider.cranksdungeons$getMountData().cargo.clearToList()) {
                    camel.dropStack(stack);
                }
                if (provider.cranksdungeons$hasMountChest()) {
                    camel.dropStack(new ItemStack(Items.CHEST));
                }
            }
        });
        UseEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!(target instanceof LivingEntity living)
                    || !(target instanceof MountDataProvider provider)
                    || MountKind.of(target) == MountKind.UNSUPPORTED) {
                return ActionResult.PASS;
            }

            ItemStack held = player.getStackInHand(hand);
            if (!world.isClient() && usesCustomOwnership(living)
                    && held.isOf(Items.NAME_TAG)
                    && held.get(DataComponentTypes.CUSTOM_NAME) != null) {
                claimNamedMount(player, living);
            }
            if (!world.isClient() && living instanceof AnimalEntity animal && !living.isBaby()
                    && animal.isBreedingItem(held) && MountKind.of(living) != MountKind.HORSE
                    && MountKind.of(living) != MountKind.PIG) {
                MountData data = ensureInitialized(living);
                if (data.breedsRemaining <= 0) {
                    player.sendMessage(Text.literal("This " + MountKind.of(living).displayName().toLowerCase()
                            + " has no breeding attempts remaining."), true);
                } else if (world.getTime() < data.nextBreedTime) {
                    player.sendMessage(Text.literal("This mount is still on breeding cooldown."), true);
                }
            }
            if (held.isOf(Items.CHEST)) {
                if (world.isClient()) return ActionResult.SUCCESS;
                return equipChest(player, living, provider, held);
            }

            // Llamas do not use saddles, but vanilla can still send a
            // sneaking saddle interaction through its horse inventory path.
            // Catch it here so the llama always opens the shared cargo UI.
            if (living instanceof LlamaEntity && held.isOf(Items.SADDLE)
                    && player.isSneaking()) {
                if (world.isClient()) return ActionResult.SUCCESS;
                openCargo((ServerPlayerEntity) player, living);
                return ActionResult.SUCCESS;
            }

            // Mounts without a vanilla inventory use sneak + empty hand as
            // their explicit cargo gesture. This keeps normal right-click
            // mounting (especially camel two-rider mounting) intact. A rider
            // can also use an empty hand on a chest-equipped pig directly,
            // which makes pig cargo behave like the other mount inventories
            // without taking away the normal mounting interaction.
            boolean riderOpeningPigCargo = living instanceof PigEntity
                    && provider.cranksdungeons$hasMountChest()
                    && living.getControllingPassenger() == player;
            if (held.isEmpty()
                    && (player.isSneaking() || riderOpeningPigCargo)
                    && usesCustomCargoScreen(living)) {
                if (world.isClient()) return ActionResult.SUCCESS;
                openCargo((ServerPlayerEntity) player, living);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    public static MountData data(LivingEntity mount) {
        return ((MountDataProvider) mount).cranksdungeons$getMountData();
    }

    /**
     * Returns whether the mount uses the shared custom cargo screen instead of
     * Minecraft's vanilla horse inventory screen.  Keep this check centralized
     * so the interaction callback and the inventory-key packet cannot drift
     * apart as more non-vanilla mounts are added.
     */
    public static boolean usesCustomCargoScreen(LivingEntity mount) {
        return mount instanceof PigEntity
                || mount instanceof StriderEntity
                || mount instanceof CamelEntity
                || mount instanceof LlamaEntity;
    }

    /** Mounts that have no vanilla tame-owner field but can be claimed by naming them. */
    public static boolean usesCustomOwnership(LivingEntity mount) {
        return mount instanceof PigEntity
                || mount instanceof StriderEntity
                || mount instanceof CamelEntity;
    }

    /** Claims an unowned camel, pig, or strider after a name has been applied. */
    public static void claimNamedMount(PlayerEntity player, LivingEntity mount) {
        if (!usesCustomOwnership(mount)) return;
        MountData data = ensureInitialized(mount);
        if (data.ownerUuid != null) return;
        data.ownerUuid = player.getUuid();
        data.ownerName = player.getName().getString();
        data.accessMode = MountAccessMode.OWNER_ONLY;
        sync(mount);
    }

    public static MountData ensureInitialized(LivingEntity mount) {
        MountData data = data(mount);
        if (data.initialized) {
            migrateDurabilityData(mount, data);
            return data;
        }
        data.initialized = true;
        data.dataVersion = CURRENT_DATA_VERSION;
        data.male = mount.getRandom().nextBoolean();
        data.breedsRemaining = MountKind.of(mount) == MountKind.STRIDER ? 4 : 3;

        switch (MountKind.of(mount)) {
            case DONKEY -> {
                data.health = randomGene(mount, MountKind.DONKEY, 0);
                data.armor = randomGene(mount, MountKind.DONKEY, 5);
                data.speed = randomGene(mount, MountKind.DONKEY, 1);
                data.power = randomGene(mount, MountKind.DONKEY, 2); // endurance reserve
                data.endurance = randomGene(mount, MountKind.DONKEY, 3); // trail skill
                data.utility = randomGene(mount, MountKind.DONKEY, 4); // pack capacity
                data.currentResource = data.power;
                data.trait = randomTrait(mount, "Packmaster", "Tireless", "Sure-Footed", "Sturdy");
            }
            case MULE -> {
                data.health = randomGene(mount, MountKind.MULE, 0);
                data.armor = randomGene(mount, MountKind.MULE, 5);
                data.speed = randomGene(mount, MountKind.MULE, 1);
                data.power = randomGene(mount, MountKind.MULE, 2); // jump
                data.endurance = randomGene(mount, MountKind.MULE, 3);
                // Vanilla chested mules already expose 15 slots.  Keep the
                // initial roll at or above that floor so loading an existing
                // chested mule never shrinks its inventory and discards items.
                data.utility = randomGene(mount, MountKind.MULE, 4);
                data.currentResource = data.endurance;
                data.trait = randomTrait(mount, "Hybrid Vigor", "Tireless", "Sure-Footed", "Sturdy");
                data.breedsRemaining = 0;
            }
            case CAMEL -> {
                data.health = randomGene(mount, MountKind.CAMEL, 0);
                data.armor = randomGene(mount, MountKind.CAMEL, 5);
                data.speed = randomGene(mount, MountKind.CAMEL, 1); // cruise speed
                data.power = randomGene(mount, MountKind.CAMEL, 2); // dash power
                data.endurance = randomGene(mount, MountKind.CAMEL, 3);
                data.utility = randomGene(mount, MountKind.CAMEL, 4); // dash efficiency
                data.currentResource = data.endurance;
                data.trait = randomTrait(mount, "Long-Haul", "Powerful", "Efficient", "Stalwart");
            }
            case STRIDER -> {
                data.health = randomGene(mount, MountKind.STRIDER, 0);
                data.armor = randomGene(mount, MountKind.STRIDER, 5);
                data.speed = randomGene(mount, MountKind.STRIDER, 1); // lava speed
                data.power = randomGene(mount, MountKind.STRIDER, 2); // heat capacity
                data.endurance = randomGene(mount, MountKind.STRIDER, 3); // heat retention
                data.utility = randomGene(mount, MountKind.STRIDER, 4); // stability
                data.currentResource = data.power;
                data.trait = randomTrait(mount, "Lava-Born", "Heatkeeper", "Steady", "Blazing");
            }
            case LLAMA -> {
                data.health = randomGene(mount, MountKind.LLAMA, 0);
                data.speed = randomGene(mount, MountKind.LLAMA, 1);
                data.power = randomGene(mount, MountKind.LLAMA, 2); // pack speed per companion
                data.endurance = 0.0;
                data.utility = mount instanceof LlamaEntity llama ? llama.getStrength() : 1.0;
                data.armor = randomGene(mount, MountKind.LLAMA, 4);
                data.trait = randomTrait(mount, "Pack Leader", "Sure-Footed", "Swift", "Sturdy");
            }
            case SKELETON_HORSE -> {
                data.health = 0.0;
                data.armor = 0.0;
                data.speed = mount.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                data.power = mount.getAttributeBaseValue(EntityAttributes.GENERIC_JUMP_STRENGTH);
                data.endurance = 100.0;
                data.utility = 0.0;
                data.breedsRemaining = 0;
            }
            case PIG -> {
                data.health = 40.0;
                data.speed = 0.30;
                data.breedsRemaining = 0;
            }
            default -> {
                // Normal horse genetics are managed by HorseBreedingManager.
                data.health = mount.getMaxHealth();
                data.speed = mount.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            }
        }
        if (MountKind.of(mount) != MountKind.HORSE) applyBases(mount, data);
        ((MountDataProvider) mount).cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(mount));
        if (mount instanceof AbstractHorseEntity horse
                && MountKind.of(horse) != MountKind.LLAMA
                && ((MountDataProvider) horse).cranksdungeons$hasMountChest()) {
            AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
        }
        if (MountKind.of(mount) != MountKind.HORSE) mount.setHealth(mount.getMaxHealth());
        return data;
    }

    private static void migrateDurabilityData(LivingEntity mount, MountData data) {
        if (data.dataVersion >= CURRENT_DATA_VERSION) return;

        float previousMaximum = mount.getMaxHealth();
        float healthRatio = previousMaximum > 0.0f
                ? MathHelper.clamp(mount.getHealth() / previousMaximum, 0.0f, 1.0f)
                : 1.0f;
        MountKind kind = MountKind.of(mount);
        if (data.dataVersion < 1) {
            // Version 1 converted the original donkey, mule, camel, llama, and
            // skeleton horse health values to the durability-profile model.
            // Striders had no durability profile yet, so keep their old raw
            // 48..85 health roll for the version 2 conversion below.
            if (kind.hasDurabilityProfile() && kind != MountKind.STRIDER) {
                if (kind == MountKind.SKELETON_HORSE) {
                    data.health = 0.0;
                } else {
                    double legacyMinimum = switch (kind) {
                        case DONKEY -> 55.0;
                        case MULE -> 58.0;
                        case CAMEL -> 65.0;
                        default -> 0.0;
                    };
                    double legacyMaximum = switch (kind) {
                        case DONKEY, MULE -> 90.0;
                        case CAMEL -> 100.0;
                        default -> legacyMinimum;
                    };
                    double quality = legacyMaximum > legacyMinimum && Double.isFinite(data.health)
                            ? MathHelper.clamp((data.health - legacyMinimum)
                            / (legacyMaximum - legacyMinimum), 0.0, 1.0)
                            : 0.0;
                    // Preserve the prior health-gene value before the version 3
                    // migration scales existing genes to the expanded range.
                    double legacyHealthGeneMaximum = switch (kind) {
                        case DONKEY -> 40.0;
                        case MULE -> 45.0;
                        case CAMEL -> 55.0;
                        default -> 0.0;
                    };
                    data.health = quality * legacyHealthGeneMaximum;
                }
                data.armor = 0.0;
            }
            data.dataVersion = 1;
        }

        if (data.dataVersion < 2) {
            switch (kind) {
                case STRIDER -> {
                    double quality = Double.isFinite(data.health)
                            ? MathHelper.clamp((data.health - 48.0) / (85.0 - 48.0), 0.0, 1.0)
                            : 0.0;
                    data.health = quality * 20.0;
                    data.armor = random(mount, 0.0, 10.0);
                }
                case LLAMA -> data.armor = random(mount, 0.0, 10.0);
                case SKELETON_HORSE -> {
                    data.health = 0.0;
                    data.armor = 0.0;
                    data.breedsRemaining = 0;
                }
                default -> { }
            }
            data.dataVersion = 2;
        }

        if (data.dataVersion < 3) {
            // The prior version stored health and armor genes at the old caps.
            // Scale only genetic mounts, leaving skeleton horses and pigs alone.
            if (kind == MountKind.DONKEY || kind == MountKind.MULE || kind == MountKind.CAMEL
                    || kind == MountKind.STRIDER || kind == MountKind.LLAMA) {
                data.health = MathHelper.clamp(data.health * 1.25, 0.0,
                        MountGeneticRatings.breedingMaximum(kind, 0));
                int armorGene = MountGeneticRatings.geneCount(kind) - 1;
                data.armor = MathHelper.clamp(data.armor * 3.0, 0.0,
                        MountGeneticRatings.breedingMaximum(kind, armorGene));
            }
            data.dataVersion = 3;
        }

        if (data.dataVersion < 4) {
            if (kind == MountKind.SKELETON_HORSE) {
                data.health = 0.0;
                data.armor = 0.0;
                data.breedsRemaining = 0;
            }
            data.dataVersion = 4;
        }

        if (kind != MountKind.HORSE) {
            applyBases(mount, data);
            mount.setHealth(mount.getMaxHealth() * healthRatio);
        }
    }

    public static void tick(LivingEntity mount) {
        if (mount.getEntityWorld().isClient()) return;
        MountKind kind = MountKind.of(mount);
        if (kind == MountKind.HORSE || kind == MountKind.UNSUPPORTED) return;
        MountData data = ensureInitialized(mount);
        boolean tamed = mount instanceof AbstractHorseEntity horse && horse.isTame();
        UUID ownerUuid = tamed ? ((AbstractHorseEntity) mount).getOwnerUuid() : null;
        if (usesCustomOwnership(mount)) {
            ownerUuid = data.ownerUuid;
        }
        boolean owned = tamed || ownerUuid != null;
        boolean ownershipChanged = data.ownershipSyncInitialized
                && (data.lastSyncedTamed != owned
                || !java.util.Objects.equals(data.lastSyncedOwnerUuid, ownerUuid));
        data.ownershipSyncInitialized = true;
        data.lastSyncedTamed = owned;
        data.lastSyncedOwnerUuid = ownerUuid;
        if (ownershipChanged) sync(mount);
        applyProgressionModifiers(mount, data);

        double moved = 0.0;
        if (!Double.isNaN(data.lastX)) {
            double dx = mount.getX() - data.lastX;
            double dz = mount.getZ() - data.lastZ;
            moved = Math.sqrt(dx * dx + dz * dz);
        }
        data.lastX = mount.getX();
        data.lastZ = mount.getZ();
        boolean controlled = mount.getControllingPassenger() instanceof PlayerEntity;
        boolean progressingCaravan = kind == MountKind.LLAMA && tamed;
        if ((controlled || progressingCaravan) && moved > 0.001 && moved < 4.0) {
            data.distanceAccumulator += moved;
            while (data.distanceAccumulator >= 10.0) {
                data.distanceAccumulator -= 10.0;
                addExperience(mount, data.abilityActive ? 4 : 2);
                if (kind == MountKind.SKELETON_HORSE && data.soulBond < 100) data.soulBond++;
            }
        }

        double maximum = maximumResource(kind, data);
        boolean moving = moved > 0.015;
        boolean canUseAbility = controlled && moving && data.currentResource > 0.0;
        data.abilityActive = switch (kind) {
            case DONKEY, MULE -> data.abilityRequested && canUseAbility;
            case STRIDER -> data.abilityRequested && canUseAbility && mount.isInLava();
            case CAMEL -> data.abilityRequested && canUseAbility;
            default -> false;
        };

        if (data.abilityActive) {
            double drain = switch (kind) {
                case DONKEY -> "Tireless".equals(data.trait) ? 2.4 : 3.0;
                case MULE -> "Tireless".equals(data.trait) ? 2.56 : 3.2;
                case CAMEL -> "Efficient".equals(data.trait) ? 2.4 : 3.0;
                case STRIDER -> "Heatkeeper".equals(data.trait) ? 3.2 : 4.0;
                default -> 0.0;
            };
            data.currentResource = Math.max(0.0, data.currentResource - drain / 20.0);
            data.resourceRegenDelay = 40;
        } else {
            if (data.resourceRegenDelay > 0) {
                data.resourceRegenDelay--;
            } else if (mount.isOnGround() || kind == MountKind.STRIDER) {
                double regen = 2.5;
                if (kind == MountKind.STRIDER) {
                    regen = mount.isInLava() ? 6.0 : (mount instanceof StriderEntity strider && strider.isCold()
                            ? -1.0 * (1.0 - data.endurance * 0.006) : 0.5);
                }
                data.currentResource = MathHelper.clamp(data.currentResource + regen / 20.0, 0.0, maximum);
            }
        }

        applyTravelSpeed(mount, data, kind);
        if (kind == MountKind.SKELETON_HORSE && "Tidebound".equals(data.specialization)
                && mount.getControllingPassenger() instanceof PlayerEntity rider) {
            rider.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.WATER_BREATHING,
                    40, 0, true, false, true));
        }
        if (++data.syncTicker >= 10) {
            data.syncTicker = 0;
            sync(mount);
        }
    }

    public static void setAbilityRequested(LivingEntity mount, PlayerEntity player, boolean active) {
        if (mount.getControllingPassenger() != player) return;
        MountKind kind = MountKind.of(mount);
        if (kind != MountKind.DONKEY && kind != MountKind.MULE
                && kind != MountKind.CAMEL && kind != MountKind.STRIDER) return;
        MountData data = ensureInitialized(mount);
        data.abilityRequested = active;
        if (!active) data.abilityActive = false;
    }

    /** Charges a camel's dash once when the dash begins, rather than per dash tick. */
    public static boolean trySpendCamelDash(CamelEntity camel) {
        if (camel.getEntityWorld().isClient()
                || !(camel.getControllingPassenger() instanceof PlayerEntity)) return true;

        MountData data = ensureInitialized(camel);
        double cost = camelDashCost(data);
        if (data.currentResource + 1.0e-6 < cost) return false;

        data.currentResource = Math.max(0.0, data.currentResource - cost);
        data.resourceRegenDelay = 40;
        sync(camel);
        return true;
    }

    /** Spends Endurance for a player-controlled donkey or mule jump. */
    public static void spendJumpResource(LivingEntity mount) {
        if (mount.getEntityWorld().isClient()
                || !(mount.getControllingPassenger() instanceof PlayerEntity)) return;
        MountKind kind = MountKind.of(mount);
        if (kind != MountKind.DONKEY && kind != MountKind.MULE) return;

        MountData data = ensureInitialized(mount);
        double cost = "Sure-Footed".equals(data.trait) ? 6.0 : 8.0;
        data.currentResource = Math.max(0.0, data.currentResource - cost);
        data.resourceRegenDelay = 40;
        sync(mount);
    }

    private static double camelDashCost(MountData data) {
        return camelDashCost(maximumResource(MountKind.CAMEL, data), data.utility, data.trait);
    }

    public static double camelDashCost(double maximumResource, double dashEfficiency, String trait) {
        double efficiency = MathHelper.clamp(1.0 - dashEfficiency * 0.004, 0.6, 1.0);
        if ("Efficient".equals(trait)) efficiency *= 0.8;
        return maximumResource * 0.25 * efficiency;
    }

    public static double maximumResource(MountKind kind, MountData data) {
        double base = switch (kind) {
            case DONKEY, STRIDER -> Math.max(1.0, data.power);
            case MULE, CAMEL -> Math.max(1.0, data.endurance);
            default -> 0.0;
        };
        if ("Long-Haul".equals(data.trait) || "Tireless".equals(data.trait)) base *= 1.15;
        if ("Lava-Born".equals(data.trait) || "Hybrid Vigor".equals(data.trait)) base *= 1.10;
        return base;
    }

    public static int experienceForNextLevel(int level) {
        return com.cranks.dungeons.horse.HorseBreedingRules.experienceForNextLevel(level, MAX_LEVEL);
    }

    private static void addExperience(LivingEntity mount, int amount) {
        MountData data = ensureInitialized(mount);
        if (data.level >= MAX_LEVEL || amount <= 0) return;
        data.experience += amount;
        while (data.level < MAX_LEVEL) {
            int required = experienceForNextLevel(data.level);
            if (data.experience < required) break;
            data.experience -= required;
            data.level++;
            mount.getEntityWorld().sendEntityStatus(mount, (byte) 7);
        }
    }

    private static void applyProgressionModifiers(LivingEntity mount, MountData data) {
        int trained = Math.max(0, data.level - 1);
        MountDurabilityProfile durability = MountDurabilityProfile.forKind(MountKind.of(mount));
        setModifier(mount, EntityAttributes.GENERIC_MAX_HEALTH, TRAINING_HEALTH,
                trained * (durability == null ? 0.0 : MountDurabilityProfile.HEALTH_PER_LEVEL),
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_ARMOR, BASE_ARMOR,
                durability == null ? 0.0 : durability.baseArmor(),
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_ARMOR, GENETIC_ARMOR,
                durability != null && MountKind.of(mount).hasArmorGene() ? data.armor : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, TRAINING_SPEED,
                trained * 0.003, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        double trail = (MountKind.of(mount) == MountKind.DONKEY ? data.endurance : 0.0);
        setModifier(mount, EntityAttributes.GENERIC_STEP_HEIGHT, TRAIL_STEP,
                trail >= 70.0 || "Sure-Footed".equals(data.trait) ? 0.25 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_MAX_HEALTH, TRAIT_HEALTH,
                "Hardy".equals(data.trait) || "Stalwart".equals(data.trait) ? 0.10
                        : "Hybrid Vigor".equals(data.trait) ? 0.08 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, TRAIT_SPEED,
                "Swift".equals(data.trait) ? 0.05 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        double armorBonusPercent = "Sturdy".equals(data.trait) ? 0.15
                : "Stalwart".equals(data.trait) ? 0.12
                : "Hybrid Vigor".equals(data.trait) ? 0.08 : 0.0;
        double armorBonus = durability == null ? 0.0 : durability.baseArmor() * armorBonusPercent;
        setModifier(mount, EntityAttributes.GENERIC_ARMOR, TRAIT_ARMOR,
                armorBonus, EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, ModAttributes.KNOCKBACK_RESISTANCE, TRAIT_KNOCKBACK,
                "Steady".equals(data.trait) || "Sure-Footed".equals(data.trait) ? 0.20 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, TRAIT_SAFE_FALL,
                "Sure-Footed".equals(data.trait) ? 2.0 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_MAX_HEALTH, SPECIALIZATION_HEALTH,
                "Graveguard".equals(data.specialization) ? 0.20 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(mount, EntityAttributes.GENERIC_ARMOR, SPECIALIZATION_ARMOR,
                "Graveguard".equals(data.specialization) && durability != null
                        ? durability.baseArmor() * 0.50 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPECIALIZATION_SPEED,
                "Stormrunner".equals(data.specialization) ? 0.15 : 0.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        setModifier(mount, EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, SPECIALIZATION_WATER,
                "Tidebound".equals(data.specialization) ? 0.75 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
        setModifier(mount, ModAttributes.LIGHTNING_RESISTANCE,
                Identifier.of("cranks-dungeons", "skeleton_specialization_lightning"),
                "Stormrunner".equals(data.specialization) ? 0.40 : 0.0,
                EntityAttributeModifier.Operation.ADD_VALUE);
    }

    private static void applyTravelSpeed(LivingEntity mount, MountData data, MountKind kind) {
        double bonus = 0.0;
        if (data.abilityActive) {
            bonus += switch (kind) {
                case DONKEY -> 0.10 + data.endurance * 0.0005;
                case MULE -> 0.12 + ("Hybrid Vigor".equals(data.trait) ? 0.03 : 0.0);
                case CAMEL -> 0.15;
                case STRIDER -> 0.20 + data.utility * 0.0005
                        + ("Blazing".equals(data.trait) ? 0.05 : 0.0);
                default -> 0.0;
            };
        }
        if ((kind == MountKind.DONKEY || kind == MountKind.MULE) && isRoadSurface(mount)) {
            double trail = kind == MountKind.DONKEY ? data.endurance : 50.0;
            bonus += 0.05 + trail * 0.001;
        }
        if (kind == MountKind.LLAMA && mount instanceof LlamaEntity llama) {
            bonus += LlamaPackManager.speedBonus(llama, data.power, data.trait);
        }
        setModifier(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, ACTIVE_SPEED,
                bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static boolean isRoadSurface(LivingEntity mount) {
        var pos = net.minecraft.util.math.BlockPos.ofFloored(
                mount.getX(), mount.getBoundingBox().minY - 0.05, mount.getZ());
        return mount.getEntityWorld().getBlockState(pos).isIn(ROADS);
    }

    private static void setModifier(LivingEntity entity,
                                    net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                    Identifier id, double value, EntityAttributeModifier.Operation operation) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance == null) return;
        EntityAttributeModifier old = instance.getModifier(id);
        if (old != null && Math.abs(old.value() - value) < 0.000001 && old.operation() == operation) return;
        instance.removeModifier(id);
        if (Math.abs(value) > 0.000001) {
            instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
        }
    }

    public static void sync(LivingEntity mount) {
        if (mount.getEntityWorld().isClient()) return;
        MountDataSyncPacket packet = MountDataSyncPacket.from(mount);
        for (ServerPlayerEntity player : PlayerLookup.tracking(mount)) ServerPlayNetworking.send(player, packet);
        if (mount.getControllingPassenger() instanceof ServerPlayerEntity rider) {
            ServerPlayNetworking.send(rider, packet);
        }
    }

    /** Sends mount data directly to the player who just opened its inventory. */
    public static void syncToPlayer(ServerPlayerEntity player, LivingEntity mount) {
        if (player == null || mount.getEntityWorld().isClient()) return;
        ServerPlayNetworking.send(player, MountDataSyncPacket.from(mount));
    }

    public static void chooseSkeletonSpecialization(SkeletonHorseEntity skeleton,
                                                    PlayerEntity player, String requested) {
        MountData data = ensureInitialized(skeleton);
        if (!canManage(player, skeleton) || data.soulBond < 10
                || !"None".equals(data.specialization)) return;
        String selected = switch (requested == null ? "" : requested.toLowerCase(java.util.Locale.ROOT)) {
            case "tidebound" -> "Tidebound";
            case "graveguard" -> "Graveguard";
            case "stormrunner" -> "Stormrunner";
            default -> null;
        };
        if (selected == null) return;
        data.specialization = selected;
        applyProgressionModifiers(skeleton, data);
        skeleton.setHealth(skeleton.getMaxHealth());
        player.sendMessage(Text.literal("Skeleton Horse specialization: " + selected), true);
        sync(skeleton);
    }

    public static int cargoCapacity(LivingEntity mount) {
        if (mount instanceof LlamaEntity llama) return LlamaPackManager.cargoCapacity(llama);
        if (!mount.getEntityWorld().isClient()) {
            int capacity = cargoCapacityUntracked(mount);
            ((MountDataProvider) mount).cranksdungeons$setSyncedCargoCapacity(capacity);
            return capacity;
        }
        int synced = ((MountDataProvider) mount).cranksdungeons$getSyncedCargoCapacity();
        if (synced > 0) return synced;
        return cargoCapacityUntracked(mount);
    }

    /** Rebuilds the tracked capacity after loading or when opening a mount inventory. */
    public static void refreshCargoCapacity(LivingEntity mount) {
        if (mount.getEntityWorld().isClient() || !(mount instanceof MountDataProvider provider)) return;
        provider.cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(mount));
    }

    /** Slot count reserved by a screen while mount equipment can change. */
    public static int cargoScreenCapacity(LivingEntity mount) {
        return mount instanceof LlamaEntity llama
                ? LlamaPackManager.screenCapacity(llama) : cargoCapacity(mount);
    }

    /** Maximum horse-family inventory size reserved up front so chest equipping
     * never has to replace the inventory while a screen is open. */
    public static int maxHorseCargoCapacity(AbstractHorseEntity horse) {
        return MountKind.of(horse).maximumCargoSlots();
    }

    private static int cargoCapacityUntracked(LivingEntity mount) {
        MountKind kind = MountKind.of(mount);
        MountData data = data(mount);
        return switch (kind) {
            case HORSE, SKELETON_HORSE, STRIDER -> 6;
            case PIG -> 3;
            case CAMEL -> 12;
            case LLAMA -> mount instanceof LlamaEntity llama
                    ? LlamaPackManager.cargoCapacity(llama) : 0;
            case DONKEY -> data.initialized
                    ? MathHelper.clamp((int) Math.round(data.utility
                            + ("Packmaster".equals(data.trait) ? 2.0 : 0.0)), 15, 33)
                    : 15;
            case MULE -> data.initialized
                    // Mules use the vanilla horse inventory layout, whose
                    // smallest chest is 15 slots. Keep that floor so a
                    // rounded-down genetic result can never hide or discard
                    // an occupied slot.
                    ? MathHelper.clamp((int) Math.round(data.utility), 15, 24)
                    : 15;
            default -> 0;
        };
    }

    public static boolean cargoIsEmpty(LivingEntity mount) {
        if (mount instanceof LlamaEntity llama) {
            var inventory = AbstractHorseInventoryAccess.of(llama).cranksdungeons$getHorseItems();
            int cargoSlots = ((MountDataProvider) llama).cranksdungeons$hasMountChest()
                    ? Math.min(inventory.size() - 2, llama.getStrength() * 3) : 0;
            for (int slot = 2; slot < 2 + cargoSlots; slot++) {
                if (!inventory.getStack(slot).isEmpty()) return false;
            }
            return true;
        }
        if (mount instanceof CamelEntity || mount instanceof PigEntity || mount instanceof StriderEntity) {
            return data(mount).cargo.isEmpty();
        }
        int capacity = cargoCapacity(mount);
        if (mount instanceof AbstractHorseEntity horse) {
            var inventory = AbstractHorseInventoryAccess.of(horse).cranksdungeons$getHorseItems();
            for (int slot = 1; slot < Math.min(inventory.size(), capacity + 1); slot++) {
                if (!inventory.getStack(slot).isEmpty()) return false;
            }
            return true;
        }
        MountData data = data(mount);
        for (int slot = 0; slot < capacity; slot++) {
            if (!data.cargo.getStack(slot).isEmpty()) return false;
        }
        return true;
    }

    public static boolean canManage(PlayerEntity player, LivingEntity mount) {
        if (usesCustomOwnership(mount)) {
            UUID ownerUuid = ensureInitialized(mount).ownerUuid;
            return player.isCreative() || ownerUuid == null || ownerUuid.equals(player.getUuid());
        }
        if (mount instanceof AbstractHorseEntity horse) {
            return player.isCreative() || (horse.isTame()
                    && (horse.getOwnerUuid() == null || horse.getOwnerUuid().equals(player.getUuid())));
        }
        return true; // Other mounts have no custom ownership layer.
    }

    /** True only for the player UUID stored as this mount's current owner. */
    public static boolean isOwner(PlayerEntity player, LivingEntity mount) {
        if (player == null || mount == null || MountKind.of(mount) == MountKind.UNSUPPORTED) return false;
        if (usesCustomOwnership(mount)) {
            UUID ownerUuid = data(mount).ownerUuid;
            return ownerUuid != null && ownerUuid.equals(player.getUuid());
        }
        if (mount instanceof AbstractHorseEntity horse) {
            UUID ownerUuid = horse.isTame() ? horse.getOwnerUuid() : null;
            return ownerUuid != null && ownerUuid.equals(player.getUuid());
        }
        return false;
    }

    /**
     * Removes ownership after checking the actual owner on the server.
     * The mount's name, inventory, equipment, and progression are retained.
     */
    public static boolean releaseOwnership(ServerPlayerEntity player, LivingEntity mount) {
        if (player == null || mount == null || mount.getEntityWorld().isClient()
                || mount.getEntityWorld() != player.getEntityWorld()
                || !mount.isAlive() || !player.canInteractWithEntity(mount, 4.0)
                || !isOwner(player, mount)) {
            return false;
        }

        if (usesCustomOwnership(mount)) {
            MountData data = ensureInitialized(mount);
            data.ownerUuid = null;
            data.ownerName = "";
            data.accessMode = MountAccessMode.OWNER_ONLY;
            data.ownershipSyncInitialized = true;
            data.lastSyncedTamed = false;
            data.lastSyncedOwnerUuid = null;
            sync(mount);
            syncToPlayer(player, mount);
            return true;
        }

        if (!(mount instanceof AbstractHorseEntity horse)
                || (!(horse instanceof HorseEntity) && !(horse instanceof MountDataProvider))) {
            return false;
        }
        horse.setOwnerUuid(null);
        horse.setTame(false);
        if (horse instanceof HorseEntity normalHorse) {
            HorseGeneticsData data = HorseBreedingManager.ensureInitialized(normalHorse);
            data.accessMode = MountAccessMode.OWNER_ONLY;
            data.ownershipSyncInitialized = true;
            data.lastSyncedTamed = false;
            data.lastSyncedOwnerUuid = null;
            HorseBreedingManager.sync(normalHorse);
            HorseBreedingManager.syncToPlayer(player, normalHorse);
        } else {
            MountData data = ensureInitialized(horse);
            data.accessMode = MountAccessMode.OWNER_ONLY;
            data.ownershipSyncInitialized = true;
            data.lastSyncedTamed = false;
            data.lastSyncedOwnerUuid = null;
            sync(horse);
            syncToPlayer(player, horse);
        }
        return true;
    }

    public static MountAccessMode accessMode(LivingEntity mount) {
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            return HorseBreedingManager.ensureInitialized(horse).accessMode;
        }
        if (mount instanceof MountDataProvider) return ensureInitialized(mount).accessMode;
        return MountAccessMode.OWNER_ONLY;
    }

    public static boolean canManageAccess(PlayerEntity player, LivingEntity mount) {
        if (usesCustomOwnership(mount)) {
            UUID ownerUuid = ensureInitialized(mount).ownerUuid;
            return ownerUuid != null && (player.isCreative() || ownerUuid.equals(player.getUuid()));
        }
        if (!(mount instanceof AbstractHorseEntity horse) || !horse.isTame()) return false;
        return player.isCreative() || (horse.getOwnerUuid() != null
                && horse.getOwnerUuid().equals(player.getUuid()));
    }

    /** Permission to ride and access inventories on owned horse-family or named custom mounts. */
    public static boolean canAccessMount(PlayerEntity player, LivingEntity mount) {
        if (usesCustomOwnership(mount)) {
            MountData data = ensureInitialized(mount);
            if (data.ownerUuid == null) return true;
            if (player.isCreative() || data.ownerUuid.equals(player.getUuid())) return true;
            return switch (data.accessMode) {
                case OWNER_ONLY -> false;
                case PARTY -> com.cranks.dungeons.party.PartyManager.areInSameParty(
                        player.getUuid(), data.ownerUuid);
                case EVERYONE -> true;
            };
        }
        if (!(mount instanceof AbstractHorseEntity horse) || !horse.isTame()) return true;
        if (player.isCreative()) return true;
        UUID owner = horse.getOwnerUuid();
        if (owner != null && owner.equals(player.getUuid())) return true;
        return switch (accessMode(mount)) {
            case OWNER_ONLY -> false;
            case PARTY -> owner != null
                    && com.cranks.dungeons.party.PartyManager.areInSameParty(player.getUuid(), owner);
            case EVERYONE -> true;
        };
    }

    public static boolean setAccessMode(PlayerEntity player, LivingEntity mount, MountAccessMode mode) {
        if (mode == null || !canManageAccess(player, mount)) return false;
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            HorseBreedingManager.ensureInitialized(horse).accessMode = mode;
            HorseBreedingManager.sync(horse);
        } else if (mount instanceof MountDataProvider) {
            ensureInitialized(mount).accessMode = mode;
            sync(mount);
        } else {
            return false;
        }
        return true;
    }

    public static void denyMountAccess(PlayerEntity player) {
        player.sendMessage(Text.literal("This mount's access is restricted."), true);
    }

    public static boolean setChest(PlayerEntity player, LivingEntity mount, boolean equipped) {
        MountDataProvider provider = (MountDataProvider) mount;
        if (!canManage(player, mount) || (equipped && mount.isBaby())) return false;
        if (provider.cranksdungeons$hasMountChest() == equipped) return true;
        if (!equipped) ejectCargo(mount, player);
        provider.cranksdungeons$setMountChest(equipped);
        provider.cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(mount));
        if (mount instanceof AbstractHorseEntity horse) {
            AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
        }
        mount.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0f, 1.0f);
        return true;
    }

    /** Removes a chest after returning its contents to a player or dropping leftovers. */
    public static void removeChestWithoutPlayer(LivingEntity mount) {
        if (mount.getEntityWorld().isClient()
                || !(mount instanceof MountDataProvider provider)
                || !provider.cranksdungeons$hasMountChest()) return;
        ejectCargo(mount, null);
        provider.cranksdungeons$setMountChest(false);
        provider.cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(mount));
        if (mount instanceof AbstractHorseEntity horse) {
            AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
        }
        mount.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0f, 1.0f);
    }

    /** Returns all physical cargo stacks, including slots hidden by changed capacity. */
    public static void ejectCargo(LivingEntity mount, PlayerEntity player) {
        if (mount.getEntityWorld().isClient()) return;
        if (mount instanceof CamelEntity || mount instanceof PigEntity || mount instanceof StriderEntity) {
            MountData data = data(mount);
            for (int slot = 0; slot < data.cargo.size(); slot++) {
                moveCargoStack(mount, player, data.cargo.removeStack(slot));
            }
            data.cargo.markDirty();
            return;
        }
        if (mount instanceof AbstractHorseEntity horse) {
            var inventory = AbstractHorseInventoryAccess.of(horse).cranksdungeons$getHorseItems();
            int cargoStart = mount instanceof LlamaEntity ? 2 : 1;
            for (int slot = cargoStart; slot < inventory.size(); slot++) {
                moveCargoStack(mount, player, inventory.removeStack(slot));
            }
            inventory.markDirty();
            return;
        }
        MountData data = data(mount);
        for (int slot = 0; slot < data.cargo.size(); slot++) {
            moveCargoStack(mount, player, data.cargo.removeStack(slot));
        }
        data.cargo.markDirty();
    }

    private static void moveCargoStack(LivingEntity mount, PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (player != null) player.getInventory().insertStack(stack);
        if (!stack.isEmpty()) mount.dropStack(stack);
    }

    private static ActionResult equipChest(PlayerEntity player, LivingEntity mount,
                                           MountDataProvider provider, ItemStack chest) {
        ensureInitialized(mount);
        if (mount.isBaby()) {
            player.sendMessage(Text.literal("A chest can only be fitted to an adult mount."), true);
            return ActionResult.FAIL;
        }
        if (!canManage(player, mount)) {
            player.sendMessage(Text.literal("Only this mount's owner can change its chest."), true);
            return ActionResult.FAIL;
        }
        if (provider.cranksdungeons$hasMountChest()) {
            player.sendMessage(Text.literal("This mount already has a chest."), true);
            return ActionResult.FAIL;
        }
        if (!setChest(player, mount, true)) return ActionResult.FAIL;
        chest.decrementUnlessCreative(1, player);
        return ActionResult.SUCCESS;
    }

    public static void openCargo(ServerPlayerEntity player, LivingEntity mount) {
        if (!canAccessMount(player, mount)
                || (mount instanceof LlamaEntity llama
                && !LlamaPackManager.canPlayerAccessPack(player, llama))) {
            denyMountAccess(player);
            return;
        }
        ensureInitialized(mount);
        refreshCargoCapacity(mount);
        syncToPlayer(player, mount);
        player.openHandledScreen(new ExtendedScreenHandlerFactory<MountCargoScreenHandler.OpeningData>() {
            @Override
            public MountCargoScreenHandler.OpeningData getScreenOpeningData(ServerPlayerEntity serverPlayer) {
                int packSize = mount instanceof LlamaEntity llama
                        ? LlamaPackManager.members(llama).size() : 0;
                return new MountCargoScreenHandler.OpeningData(mount.getId(),
                        cargoScreenCapacity(mount), packSize, cargoCapacity(mount));
            }

            @Override
            public Text getDisplayName() {
                return mount.getDisplayName();
            }

            @Override
            public ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inventory,
                                            PlayerEntity ignored) {
                return new MountCargoScreenHandler(syncId, inventory, mount);
            }
        });
    }

    public static String[] geneLabels(MountKind kind) {
        return switch (kind) {
            case DONKEY -> new String[]{"Health Bonus", "Travel Speed", "Endurance", "Trail Skill", "Pack Capacity", "Armor Bonus"};
            case MULE -> new String[]{"Health Bonus", "Travel Speed", "Jump", "Endurance", "Pack Capacity", "Armor Bonus"};
            case CAMEL -> new String[]{"Health Bonus", "Cruise Speed", "Dash Power", "Endurance", "Dash Efficiency", "Armor Bonus"};
            case STRIDER -> new String[]{"Health Bonus", "Lava Speed", "Heat Capacity", "Heat Retention", "Stability", "Armor Bonus"};
            case LLAMA -> new String[]{"Health Bonus", "Pack Speed", "Pack Instinct", "Load Strength", "Armor Bonus"};
            default -> new String[]{"Health", "Speed", "Power", "Endurance", "Utility"};
        };
    }

    public static double[] geneValues(MountData data, MountKind kind) {
        if (kind == MountKind.LLAMA) {
            return new double[]{data.health, data.speed, data.power, data.utility, data.armor};
        }
        double[] values = {data.health, data.speed, data.power, data.endurance, data.utility};
        if (!kind.hasArmorGene()) return values;
        return new double[]{data.health, data.speed, data.power, data.endurance, data.utility, data.armor};
    }

    public static boolean canMate(LivingEntity first, LivingEntity second) {
        MountKind firstKind = MountKind.of(first);
        MountKind secondKind = MountKind.of(second);
        boolean hybridPair = (firstKind == MountKind.HORSE && secondKind == MountKind.DONKEY)
                || (firstKind == MountKind.DONKEY && secondKind == MountKind.HORSE);
        if (!hybridPair && firstKind != secondKind) return false;
        if (firstKind == MountKind.MULE || secondKind == MountKind.MULE
                || firstKind == MountKind.SKELETON_HORSE || secondKind == MountKind.SKELETON_HORSE
                || firstKind == MountKind.PIG || secondKind == MountKind.PIG) return false;
        long time = first.getEntityWorld().getTime();

        boolean firstMale = sex(first);
        boolean secondMale = sex(second);
        if (firstMale == secondMale) return false;
        if (remainingBreeds(first) <= 0 || remainingBreeds(second) <= 0) return false;
        return nextBreedTime(first) <= time && nextBreedTime(second) <= time
                && !areRelated(first, second);
    }

    public static void inheritHorseFamily(AbstractHorseEntity first, AbstractHorseEntity second,
                                          PassiveEntity child) {
        if (!(child instanceof LivingEntity livingChild)
                || !(livingChild instanceof MountDataProvider)) return;
        MountKind childKind = MountKind.of(livingChild);
        if (childKind == MountKind.HORSE) return;
        if (childKind == MountKind.MULE) {
            LivingEntity horse = MountKind.of(first) == MountKind.HORSE ? first : second;
            LivingEntity donkey = MountKind.of(first) == MountKind.DONKEY ? first : second;
            if (MountKind.of(horse) != MountKind.HORSE || MountKind.of(donkey) != MountKind.DONKEY) return;
            inheritMule(horse, donkey, livingChild);
            consumeBreeding(first);
            consumeBreeding(second);
            return;
        }
        if (MountKind.of(first) == childKind && MountKind.of(second) == childKind) {
            inheritSameKind(first, second, livingChild);
        }
    }

    public static void inheritSameKind(LivingEntity first, LivingEntity second, LivingEntity child) {
        MountKind kind = MountKind.of(child);
        if (kind == MountKind.HORSE || kind == MountKind.MULE || kind == MountKind.PIG
                || kind == MountKind.SKELETON_HORSE || kind == MountKind.UNSUPPORTED) return;
        MountData a = ensureInitialized(first);
        MountData b = ensureInitialized(second);
        MountData inherited = data(child);
        if (inherited.initialized) return;
        inherited.initialized = true;
        inherited.dataVersion = CURRENT_DATA_VERSION;
        inherited.male = child.getRandom().nextBoolean();
        inherited.generation = Math.max(a.generation, b.generation) + 1;
        inherited.breedsRemaining = kind == MountKind.STRIDER ? 4 : 3;

        double[][] ranges = ranges(kind);
        double[] av = geneValues(a, kind);
        double[] bv = geneValues(b, kind);
        int geneCount = MountGeneticRatings.geneCount(kind);
        double[] result = new double[geneCount];
        int newMutations = 0;
        int betterCount = 0;
        boolean[] better = new boolean[geneCount];
        for (int i = 0; i < geneCount; i++) {
            better[i] = child.getRandom().nextDouble() < 0.70;
            if (better[i]) betterCount++;
        }
        while (betterCount < 3) {
            int index = child.getRandom().nextInt(geneCount);
            if (!better[index]) { better[index] = true; betterCount++; }
        }
        for (int i = 0; i < geneCount; i++) {
            double selected = better[i] ? Math.max(av[i], bv[i]) : Math.min(av[i], bv[i]);
            double span = ranges[i][1] - ranges[i][0];
            selected += span * (0.005 + child.getRandom().nextDouble() * 0.015);
            if (child.getRandom().nextDouble() < 0.05) {
                selected += span * (child.getRandom().nextDouble() < 0.80 ? 1.0 : -1.0)
                        * (0.02 + child.getRandom().nextDouble() * 0.04);
                newMutations++;
            }
            result[i] = MathHelper.clamp(selected, ranges[i][0], ranges[i][1]);
        }
        assignGenes(inherited, kind, result);
        inherited.mutations = a.mutations + b.mutations + newMutations;
        inherited.trait = inheritTrait(child, a.trait, b.trait, kind);
        recordFamily(first, second, child, inherited);
        inherited.currentResource = maximumResource(kind, inherited);
        applyBases(child, inherited);
        if (kind == MountKind.LLAMA && child instanceof LlamaEntity llama) {
            llama.setStrength(MathHelper.clamp((int) Math.round(inherited.utility), 1, 5));
        }
        ((MountDataProvider) child).cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(child));
        child.setHealth(child.getMaxHealth());
        consumeBreeding(first);
        consumeBreeding(second);
        sync(child);
    }

    private static void inheritMule(LivingEntity horse, LivingEntity donkey, LivingEntity child) {
        HorseGeneticsData horseData = HorseBreedingManager.ensureInitialized(
                (net.minecraft.entity.passive.HorseEntity) horse);
        MountData donkeyData = ensureInitialized(donkey);
        MountData mule = data(child);
        mule.initialized = true;
        mule.dataVersion = CURRENT_DATA_VERSION;
        mule.male = child.getRandom().nextBoolean();
        mule.generation = Math.max(horseData.generation, donkeyData.generation) + 1;
        mule.breedsRemaining = 0;
        mule.health = clampMuleGene((horseData.geneticHealth + donkeyData.health) * 0.5 + 3.75, 0);
        mule.armor = clampMuleGene((horseData.geneticArmor + donkeyData.armor) * 0.5 + 6.0, 5);
        mule.speed = clampMuleGene((horseData.geneticSpeed + donkeyData.speed) * 0.5, 1);
        mule.power = clampMuleGene(horseData.geneticJump * 0.92, 2);
        mule.endurance = clampMuleGene((horseData.geneticEndurance + donkeyData.power) * 0.5 + 8.0, 3);
        mule.utility = clampMuleGene(donkeyData.utility * 0.75, 4);
        mule.trait = child.getRandom().nextDouble() < 0.20 ? "Hybrid Vigor"
                : (child.getRandom().nextBoolean() ? donkeyData.trait : horseData.trait.displayName());
        mule.mutations = horseData.mutations + donkeyData.mutations;
        recordFamily(horse, donkey, child, mule);
        mule.currentResource = mule.endurance;
        applyBases(child, mule);
        ((MountDataProvider) child).cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(child));
        child.setHealth(child.getMaxHealth());
        sync(child);
    }

    private static double clampMuleGene(double value, int geneIndex) {
        return MathHelper.clamp(value, MountGeneticRatings.breedingMinimum(MountKind.MULE, geneIndex),
                MountGeneticRatings.breedingMaximum(MountKind.MULE, geneIndex));
    }

    private static double[][] ranges(MountKind kind) {
        return MountGeneticRatings.breedingRanges(kind);
    }

    private static double randomGene(LivingEntity mount, MountKind kind, int index) {
        return random(mount, MountGeneticRatings.minimum(kind, index),
                MountGeneticRatings.naturalMaximum(kind, index));
    }

    private static void assignGenes(MountData data, MountKind kind, double[] values) {
        data.health = values[0]; data.speed = values[1]; data.power = values[2];
        if (kind == MountKind.LLAMA) {
            data.endurance = 0.0;
            data.utility = values[3];
            data.armor = values[4];
        } else {
            data.endurance = values[3]; data.utility = values[4];
            data.armor = values.length > 5 ? values[5] : 0.0;
        }
    }

    private static String inheritTrait(LivingEntity child, String first, String second, MountKind kind) {
        if (first.equals(second) && !first.equals("None")) return first;
        String selected = child.getRandom().nextBoolean() ? first : second;
        if (child.getRandom().nextDouble() < 0.10) selected = "None";
        if (child.getRandom().nextDouble() < 0.03) {
            selected = switch (kind) {
                case DONKEY -> randomTraitForced(child, "Packmaster", "Tireless", "Sure-Footed", "Sturdy");
                case CAMEL -> randomTraitForced(child, "Long-Haul", "Powerful", "Efficient", "Stalwart");
                case STRIDER -> randomTraitForced(child, "Lava-Born", "Heatkeeper", "Steady", "Blazing");
                case LLAMA -> randomTraitForced(child, "Pack Leader", "Sure-Footed", "Swift", "Sturdy");
                default -> selected;
            };
        }
        return selected;
    }

    private static boolean sex(LivingEntity mount) {
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            return HorseBreedingManager.ensureInitialized(horse).male;
        }
        return ensureInitialized(mount).male;
    }

    private static int remainingBreeds(LivingEntity mount) {
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            return HorseBreedingManager.ensureInitialized(horse).breedsRemaining;
        }
        return ensureInitialized(mount).breedsRemaining;
    }

    private static long nextBreedTime(LivingEntity mount) {
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            return HorseBreedingManager.ensureInitialized(horse).nextBreedTime;
        }
        return ensureInitialized(mount).nextBreedTime;
    }

    private static void consumeBreeding(LivingEntity mount) {
        long cooldown = MountKind.of(mount) == MountKind.STRIDER ? BREEDING_COOLDOWN : 36_000L;
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            HorseGeneticsData data = HorseBreedingManager.ensureInitialized(horse);
            data.breedsRemaining = Math.max(0, data.breedsRemaining - 1);
            data.nextBreedTime = mount.getEntityWorld().getTime() + cooldown;
        } else {
            MountData data = ensureInitialized(mount);
            data.breedsRemaining = Math.max(0, data.breedsRemaining - 1);
            data.nextBreedTime = mount.getEntityWorld().getTime() + cooldown;
            sync(mount);
        }
    }

    private static boolean areRelated(LivingEntity first, LivingEntity second) {
        if (first.getUuid().equals(second.getUuid())) return true;
        Set<UUID> firstFamily = familyIds(first);
        Set<UUID> secondFamily = familyIds(second);
        if (firstFamily.contains(second.getUuid()) || secondFamily.contains(first.getUuid())) return true;
        for (UUID id : firstFamily) if (secondFamily.contains(id)) return true;
        return false;
    }

    private static Set<UUID> familyIds(LivingEntity mount) {
        Set<UUID> family = new LinkedHashSet<>();
        if (mount instanceof net.minecraft.entity.passive.HorseEntity horse) {
            HorseGeneticsData data = HorseBreedingManager.ensureInitialized(horse);
            if (data.sireId != null) family.add(data.sireId);
            if (data.damId != null) family.add(data.damId);
            family.addAll(data.ancestry);
        } else {
            MountData data = ensureInitialized(mount);
            if (data.sireId != null) family.add(data.sireId);
            if (data.damId != null) family.add(data.damId);
            family.addAll(data.ancestry);
        }
        return family;
    }

    private static void recordFamily(LivingEntity first, LivingEntity second, LivingEntity child,
                                     MountData inherited) {
        LivingEntity sire = sex(first) ? first : second;
        LivingEntity dam = sex(first) ? second : first;
        inherited.sireId = sire.getUuid();
        inherited.damId = dam.getUuid();
        inherited.sireName = displayName(sire);
        inherited.damName = displayName(dam);
        inherited.ancestry.add(first.getUuid());
        inherited.ancestry.add(second.getUuid());
        inherited.ancestry.addAll(familyIds(first));
        inherited.ancestry.addAll(familyIds(second));
        while (inherited.ancestry.size() > 16) {
            UUID last = null;
            for (UUID id : inherited.ancestry) last = id;
            if (last == null) break;
            inherited.ancestry.remove(last);
        }
    }

    private static String displayName(LivingEntity entity) {
        return entity.hasCustomName() ? entity.getName().getString()
                : MountKind.of(entity).displayName();
    }

    private static void applyBases(LivingEntity mount, MountData data) {
        MountDurabilityProfile durability = MountDurabilityProfile.forKind(MountKind.of(mount));
        setBase(mount, EntityAttributes.GENERIC_MAX_HEALTH,
                durability == null ? data.health : durability.baseHealth() + data.health);
        setBase(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, data.speed);
        if (mount instanceof AbstractHorseEntity && data.power > 0.0
                && (mount instanceof MuleEntity || mount instanceof SkeletonHorseEntity)) {
            setBase(mount, EntityAttributes.GENERIC_JUMP_STRENGTH, data.power);
        }
        applyProgressionModifiers(mount, data);
    }

    private static void setBase(LivingEntity entity,
                                net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                double value) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private static double random(LivingEntity entity, double min, double max) {
        return min + entity.getRandom().nextDouble() * (max - min);
    }

    private static String randomTrait(LivingEntity entity, String... traits) {
        if (entity.getRandom().nextDouble() >= 0.40) return "None";
        return traits[entity.getRandom().nextInt(traits.length)];
    }

    private static String randomTraitForced(LivingEntity entity, String... traits) {
        return traits[entity.getRandom().nextInt(traits.length)];
    }
}

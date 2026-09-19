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
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.AnimalEntity;
import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.horse.HorseGeneticsData;
import net.minecraft.entity.player.PlayerEntity;
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
    private static final Identifier ACTIVE_SPEED = Identifier.of("cranks-dungeons", "mount_active_speed");
    private static final Identifier TRAINING_HEALTH = Identifier.of("cranks-dungeons", "mount_training_health");
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
                || mount instanceof CamelEntity;
    }

    public static MountData ensureInitialized(LivingEntity mount) {
        MountData data = data(mount);
        if (data.initialized) return data;
        data.initialized = true;
        data.male = mount.getRandom().nextBoolean();
        data.breedsRemaining = MountKind.of(mount) == MountKind.STRIDER ? 4 : 3;

        switch (MountKind.of(mount)) {
            case DONKEY -> {
                data.health = random(mount, 55.0, 75.0);
                data.speed = random(mount, 0.15, 0.24);
                data.power = random(mount, 70.0, 120.0); // endurance reserve
                data.endurance = random(mount, 0.0, 100.0); // trail skill
                data.utility = random(mount, 15.0, 21.0); // pack capacity
                data.currentResource = data.power;
                data.trait = randomTrait(mount, "Packmaster", "Tireless", "Sure-Footed", "Sturdy");
            }
            case MULE -> {
                data.health = random(mount, 58.0, 80.0);
                data.speed = random(mount, 0.18, 0.28);
                data.power = random(mount, 0.45, 0.85); // jump
                data.endurance = random(mount, 75.0, 130.0);
                // Vanilla chested mules already expose 15 slots.  Keep the
                // initial roll at or above that floor so loading an existing
                // chested mule never shrinks its inventory and discards items.
                data.utility = random(mount, 15.0, 18.0);
                data.currentResource = data.endurance;
                data.trait = randomTrait(mount, "Hybrid Vigor", "Tireless", "Sure-Footed", "Sturdy");
                data.breedsRemaining = 0;
            }
            case CAMEL -> {
                data.health = random(mount, 65.0, 85.0);
                data.speed = random(mount, 0.14, 0.22); // cruise speed
                data.power = random(mount, 0.45, 0.75); // dash power
                data.endurance = random(mount, 75.0, 135.0);
                data.utility = random(mount, 0.0, 100.0); // dash efficiency
                data.currentResource = data.endurance;
                data.trait = randomTrait(mount, "Long-Haul", "Powerful", "Efficient", "Stalwart");
            }
            case STRIDER -> {
                data.health = random(mount, 48.0, 68.0);
                data.speed = random(mount, 0.18, 0.30); // lava speed
                data.power = random(mount, 75.0, 135.0); // heat capacity
                data.endurance = random(mount, 0.0, 100.0); // heat retention
                data.utility = random(mount, 0.0, 100.0); // stability
                data.currentResource = data.power;
                data.trait = randomTrait(mount, "Lava-Born", "Heatkeeper", "Steady", "Blazing");
            }
            case SKELETON_HORSE -> {
                data.health = 72.0;
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
                && ((MountDataProvider) horse).cranksdungeons$hasMountChest()) {
            AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
        }
        if (MountKind.of(mount) != MountKind.HORSE) mount.setHealth(mount.getMaxHealth());
        return data;
    }

    public static void tick(LivingEntity mount) {
        if (mount.getEntityWorld().isClient()) return;
        MountKind kind = MountKind.of(mount);
        if (kind == MountKind.HORSE || kind == MountKind.UNSUPPORTED) return;
        MountData data = ensureInitialized(mount);
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
        if (controlled && moved > 0.001 && moved < 4.0) {
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
            case CAMEL -> mount instanceof CamelEntity camel && camel.isDashing() && canUseAbility;
            default -> false;
        };

        if (data.abilityActive) {
            double drain = switch (kind) {
                case DONKEY -> "Tireless".equals(data.trait) ? 1.6 : 2.0;
                case MULE -> "Tireless".equals(data.trait) ? 1.8 : 2.2;
                case CAMEL -> 3.0 * (1.0 - data.utility * 0.004)
                        * ("Efficient".equals(data.trait) ? 0.80 : 1.0);
                case STRIDER -> "Heatkeeper".equals(data.trait) ? 2.0 : 2.5;
                default -> 0.0;
            };
            data.currentResource = Math.max(0.0, data.currentResource - drain / 20.0);
            data.resourceRegenDelay = 40;
        } else if (data.resourceRegenDelay > 0) {
            data.resourceRegenDelay--;
        } else {
            double regen = 2.5;
            if (kind == MountKind.STRIDER) {
                regen = mount.isInLava() ? 6.0 : (mount instanceof StriderEntity strider && strider.isCold()
                        ? -1.0 * (1.0 - data.endurance * 0.006) : 0.5);
            }
            data.currentResource = MathHelper.clamp(data.currentResource + regen / 20.0, 0.0, maximum);
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
        if (kind != MountKind.DONKEY && kind != MountKind.MULE && kind != MountKind.STRIDER) return;
        MountData data = ensureInitialized(mount);
        data.abilityRequested = active;
        if (!active) data.abilityActive = false;
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
        setModifier(mount, EntityAttributes.GENERIC_MAX_HEALTH, TRAINING_HEALTH,
                trained * 0.4, EntityAttributeModifier.Operation.ADD_VALUE);
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
        double armor = "Sturdy".equals(data.trait) ? 15.0
                : "Stalwart".equals(data.trait) ? 12.0
                : "Hybrid Vigor".equals(data.trait) ? 8.0 : 0.0;
        setModifier(mount, EntityAttributes.GENERIC_ARMOR, TRAIT_ARMOR,
                armor, EntityAttributeModifier.Operation.ADD_VALUE);
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
                "Graveguard".equals(data.specialization) ? 20.0 : 0.0,
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
                case STRIDER -> 0.20 + data.utility * 0.0005
                        + ("Blazing".equals(data.trait) ? 0.05 : 0.0);
                default -> 0.0;
            };
        }
        if ((kind == MountKind.DONKEY || kind == MountKind.MULE) && isRoadSurface(mount)) {
            double trail = kind == MountKind.DONKEY ? data.endurance : 50.0;
            bonus += 0.05 + trail * 0.001;
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
        int synced = ((MountDataProvider) mount).cranksdungeons$getSyncedCargoCapacity();
        if (synced > 0) return synced;
        return cargoCapacityUntracked(mount);
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
        if (mount instanceof AbstractHorseEntity horse) {
            return player.isCreative() || (horse.isTame()
                    && (horse.getOwnerUuid() == null || horse.getOwnerUuid().equals(player.getUuid())));
        }
        return true; // Pigs and striders have no vanilla ownership system.
    }

    public static boolean setChest(PlayerEntity player, LivingEntity mount, boolean equipped) {
        MountDataProvider provider = (MountDataProvider) mount;
        if (!canManage(player, mount) || mount.isBaby()) return false;
        if (!equipped && !cargoIsEmpty(mount)) return false;
        if (provider.cranksdungeons$hasMountChest() == equipped) return true;
        provider.cranksdungeons$setMountChest(equipped);
        provider.cranksdungeons$setSyncedCargoCapacity(cargoCapacityUntracked(mount));
        if (mount instanceof AbstractHorseEntity horse) {
            AbstractHorseInventoryAccess.of(horse).cranksdungeons$resizeHorseInventory();
        }
        mount.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0f, 1.0f);
        return true;
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
        ensureInitialized(mount);
        syncToPlayer(player, mount);
        player.openHandledScreen(new ExtendedScreenHandlerFactory<MountCargoScreenHandler.OpeningData>() {
            @Override
            public MountCargoScreenHandler.OpeningData getScreenOpeningData(ServerPlayerEntity serverPlayer) {
                return new MountCargoScreenHandler.OpeningData(mount.getId(), cargoCapacity(mount));
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
            case DONKEY -> new String[]{"Health", "Travel Speed", "Endurance", "Trail Skill", "Pack Capacity"};
            case MULE -> new String[]{"Health", "Travel Speed", "Jump", "Endurance", "Pack Capacity"};
            case CAMEL -> new String[]{"Health", "Cruise Speed", "Dash Power", "Endurance", "Dash Efficiency"};
            case STRIDER -> new String[]{"Health", "Lava Speed", "Heat Capacity", "Heat Retention", "Stability"};
            default -> new String[]{"Health", "Speed", "Power", "Endurance", "Utility"};
        };
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
        inherited.male = child.getRandom().nextBoolean();
        inherited.generation = Math.max(a.generation, b.generation) + 1;
        inherited.breedsRemaining = kind == MountKind.STRIDER ? 4 : 3;

        double[][] ranges = ranges(kind);
        double[] av = geneValues(a);
        double[] bv = geneValues(b);
        double[] result = new double[5];
        int newMutations = 0;
        int betterCount = 0;
        boolean[] better = new boolean[5];
        for (int i = 0; i < 5; i++) {
            better[i] = child.getRandom().nextDouble() < 0.70;
            if (better[i]) betterCount++;
        }
        while (betterCount < 3) {
            int index = child.getRandom().nextInt(5);
            if (!better[index]) { better[index] = true; betterCount++; }
        }
        for (int i = 0; i < 5; i++) {
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
        assignGenes(inherited, result);
        inherited.mutations = a.mutations + b.mutations + newMutations;
        inherited.trait = inheritTrait(child, a.trait, b.trait, kind);
        recordFamily(first, second, child, inherited);
        inherited.currentResource = maximumResource(kind, inherited);
        applyBases(child, inherited);
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
        mule.male = child.getRandom().nextBoolean();
        mule.generation = Math.max(horseData.generation, donkeyData.generation) + 1;
        mule.breedsRemaining = 0;
        mule.health = MathHelper.clamp((horseData.geneticHealth + donkeyData.health) * 0.5 + 3.0, 58.0, 90.0);
        mule.speed = MathHelper.clamp((horseData.geneticSpeed + donkeyData.speed) * 0.5, 0.18, 0.34);
        mule.power = MathHelper.clamp(horseData.geneticJump * 0.92, 0.45, 1.05);
        mule.endurance = MathHelper.clamp((horseData.geneticEndurance + donkeyData.power) * 0.5 + 8.0, 75.0, 155.0);
        mule.utility = MathHelper.clamp(donkeyData.utility * 0.75, 15.0, 24.0);
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

    private static double[][] ranges(MountKind kind) {
        return switch (kind) {
            case DONKEY -> new double[][]{{55, 90}, {0.15, 0.30}, {70, 160}, {0, 100}, {15, 33}};
            case CAMEL -> new double[][]{{65, 100}, {0.14, 0.28}, {0.45, 0.90}, {75, 170}, {0, 100}};
            case STRIDER -> new double[][]{{48, 85}, {0.18, 0.38}, {75, 170}, {0, 100}, {0, 100}};
            default -> new double[][]{{0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}};
        };
    }

    private static void assignGenes(MountData data, double[] values) {
        data.health = values[0]; data.speed = values[1]; data.power = values[2];
        data.endurance = values[3]; data.utility = values[4];
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

    public static double[] geneValues(MountData data) {
        return new double[]{data.health, data.speed, data.power, data.endurance, data.utility};
    }

    private static void applyBases(LivingEntity mount, MountData data) {
        setBase(mount, EntityAttributes.GENERIC_MAX_HEALTH, data.health);
        setBase(mount, EntityAttributes.GENERIC_MOVEMENT_SPEED, data.speed);
        if (mount instanceof AbstractHorseEntity && data.power > 0.0
                && (mount instanceof MuleEntity || mount instanceof SkeletonHorseEntity)) {
            setBase(mount, EntityAttributes.GENERIC_JUMP_STRENGTH, data.power);
        }
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

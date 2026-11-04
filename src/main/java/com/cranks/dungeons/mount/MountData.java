package com.cranks.dungeons.mount;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Persistent progression and cargo state used by mounts other than normal horses. */
public final class MountData {
    public static final int MAX_CARGO_SLOTS = 33;
    public int dataVersion;

    public boolean initialized;
    public boolean chestEquipped;
    public boolean male;
    public int level = 1;
    public int experience;
    public int generation;
    public int mutations;
    public int breedsRemaining = 3;
    public long nextBreedTime;
    public UUID sireId;
    public UUID damId;
    public String sireName = "Wild";
    public String damName = "Wild";
    /** Custom ownership used by mounts that vanilla does not tame. */
    public UUID ownerUuid;
    public String ownerName = "";
    public final Set<UUID> ancestry = new LinkedHashSet<>();

    // The labels and ranges of these genes depend on MountKind. Health and armor
    // are bonuses above the species baseline for primary mounts.
    public double health;
    public double speed;
    public double power;
    public double endurance;
    public double utility;
    public double armor;
    public String trait = "None";
    public String specialization = "None";
    public MountAccessMode accessMode = MountAccessMode.OWNER_ONLY;
    public int soulBond;
    public double currentResource;

    // Runtime-only movement/resource bookkeeping.
    public double lastX = Double.NaN;
    public double lastZ = Double.NaN;
    public double distanceAccumulator;
    public boolean abilityRequested;
    public boolean abilityActive;
    public int resourceRegenDelay;
    public int syncTicker;
    public boolean ownershipSyncInitialized;
    public boolean lastSyncedTamed;
    public UUID lastSyncedOwnerUuid;

    // Pig and strider cargo lives here. Horse-family cargo uses the vanilla horse inventory.
    public final SimpleInventory cargo = new SimpleInventory(MAX_CARGO_SLOTS);

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries, boolean includeCargo) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("DataVersion", dataVersion);
        nbt.putBoolean("Initialized", initialized);
        nbt.putBoolean("ChestEquipped", chestEquipped);
        nbt.putBoolean("Male", male);
        nbt.putInt("Level", level);
        nbt.putInt("Experience", experience);
        nbt.putInt("Generation", generation);
        nbt.putInt("Mutations", mutations);
        nbt.putInt("BreedsRemaining", breedsRemaining);
        nbt.putLong("NextBreedTime", nextBreedTime);
        putUuid(nbt, "SireId", sireId);
        putUuid(nbt, "DamId", damId);
        nbt.putString("SireName", sireName);
        nbt.putString("DamName", damName);
        putUuid(nbt, "OwnerUuid", ownerUuid);
        nbt.putString("OwnerName", ownerName);
        nbt.putString("Ancestry", ancestry.stream().map(UUID::toString)
                .collect(java.util.stream.Collectors.joining(",")));
        nbt.putDouble("Health", health);
        nbt.putDouble("Speed", speed);
        nbt.putDouble("Power", power);
        nbt.putDouble("Endurance", endurance);
        nbt.putDouble("Utility", utility);
        nbt.putDouble("Armor", armor);
        nbt.putString("Trait", trait);
        nbt.putString("Specialization", specialization);
        nbt.putString("MountAccess", accessMode.name());
        nbt.putInt("SoulBond", soulBond);
        nbt.putDouble("CurrentResource", currentResource);
        if (includeCargo && !cargo.isEmpty()) {
            nbt.put("Cargo", cargo.toNbtList(registries));
        }
        return nbt;
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries,
                        boolean includeCargo) {
        dataVersion = Math.max(0, nbt.getInt("DataVersion"));
        initialized = nbt.getBoolean("Initialized");
        chestEquipped = nbt.getBoolean("ChestEquipped");
        male = nbt.getBoolean("Male");
        level = Math.max(1, nbt.getInt("Level"));
        experience = Math.max(0, nbt.getInt("Experience"));
        generation = Math.max(0, nbt.getInt("Generation"));
        mutations = Math.max(0, nbt.getInt("Mutations"));
        breedsRemaining = nbt.contains("BreedsRemaining")
                ? Math.max(0, nbt.getInt("BreedsRemaining")) : 3;
        nextBreedTime = nbt.getLong("NextBreedTime");
        sireId = getUuid(nbt, "SireId");
        damId = getUuid(nbt, "DamId");
        sireName = textOr(nbt, "SireName", "Wild");
        damName = textOr(nbt, "DamName", "Wild");
        ownerUuid = getUuid(nbt, "OwnerUuid");
        ownerName = textOr(nbt, "OwnerName", "");
        ancestry.clear();
        String encodedAncestry = nbt.getString("Ancestry");
        if (!encodedAncestry.isBlank()) {
            for (String encoded : encodedAncestry.split(",")) {
                try { ancestry.add(UUID.fromString(encoded)); }
                catch (IllegalArgumentException ignored) { }
            }
        }
        health = nbt.getDouble("Health");
        speed = nbt.getDouble("Speed");
        power = nbt.getDouble("Power");
        endurance = nbt.getDouble("Endurance");
        utility = nbt.getDouble("Utility");
        armor = nbt.getDouble("Armor");
        trait = textOr(nbt, "Trait", "None");
        specialization = textOr(nbt, "Specialization", "None");
        accessMode = MountAccessMode.fromId(nbt.getString("MountAccess"));
        soulBond = Math.max(0, nbt.getInt("SoulBond"));
        currentResource = Math.max(0.0, nbt.getDouble("CurrentResource"));
        if (includeCargo && nbt.contains("Cargo")) {
            cargo.readNbtList(nbt.getList("Cargo", net.minecraft.nbt.NbtElement.COMPOUND_TYPE), registries);
        }
    }

    private static String textOr(NbtCompound nbt, String key, String fallback) {
        String value = nbt.getString(key);
        return value.isBlank() ? fallback : value;
    }

    private static void putUuid(NbtCompound nbt, String key, UUID value) {
        if (value != null) nbt.putUuid(key, value);
    }

    private static UUID getUuid(NbtCompound nbt, String key) {
        return nbt.containsUuid(key) ? nbt.getUuid(key) : null;
    }
}

package com.cranks.dungeons.horse;

import net.minecraft.nbt.NbtCompound;
import com.cranks.dungeons.mount.MountAccessMode;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class HorseGeneticsData {
    public int dataVersion;
    public boolean initialized;
    public boolean male;
    public double geneticHealth;
    public double geneticSpeed;
    public double geneticJump;
    public double geneticEvasion;
    public double geneticEndurance;
    public double geneticArmor;
    public int level = 1;
    public int experience;
    public int generation;
    public int mutations;
    public int personalGeneMutationMask;
    public int personalPositiveMutationMask;
    public double healthMutation;
    public double speedMutation;
    public double jumpMutation;
    public double evasionMutation;
    public double enduranceMutation;
    public double armorMutation;
    public boolean traitMutated;
    public HorseTrait traitMutationFrom = HorseTrait.NONE;
    public HorseTrait traitMutationTo = HorseTrait.NONE;
    public boolean coatMutated;
    public int breedsRemaining = 3;
    public HorseTrait trait = HorseTrait.NONE;
    public HorseBreedingFocus breedingFocus = HorseBreedingFocus.NONE;
    public MountAccessMode accessMode = MountAccessMode.OWNER_ONLY;
    public UUID sireId;
    public UUID damId;
    public String sireName = "Wild";
    public String damName = "Wild";
    public final Set<UUID> ancestry = new LinkedHashSet<>();
    public UUID imprintOwnerId;
    public String imprintOwnerName = "None";
    public float imprintProgress;
    public long nextBreedTime;
    public long nextCareTime;
    public double currentStamina;

    // Runtime-only bookkeeping.
    public double lastX = Double.NaN;
    public double lastZ = Double.NaN;
    public double distanceAccumulator;
    public boolean wasOnGround = true;
    public int syncTicker;
    public boolean ownershipSyncInitialized;
    public boolean lastSyncedTamed;
    public UUID lastSyncedOwnerUuid;
    public boolean modifiersDirty = true;
    public boolean exhausted;
    public boolean gallopRequested;
    public boolean galloping;
    public boolean onRoad;
    public int staminaRegenDelay;
    public UUID activeImprintRider;
    public boolean trackingLongJump;
    public double longJumpDistance;

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("DataVersion", dataVersion);
        nbt.putBoolean("Initialized", initialized);
        nbt.putBoolean("Male", male);
        nbt.putDouble("GeneticHealth", geneticHealth);
        nbt.putDouble("GeneticSpeed", geneticSpeed);
        nbt.putDouble("GeneticJump", geneticJump);
        nbt.putDouble("GeneticEvasion", geneticEvasion);
        nbt.putDouble("GeneticEndurance", geneticEndurance);
        nbt.putDouble("GeneticArmor", geneticArmor);
        nbt.putInt("Level", level);
        nbt.putInt("Experience", experience);
        nbt.putInt("Generation", generation);
        nbt.putInt("Mutations", mutations);
        nbt.putInt("PersonalGeneMutationMask", personalGeneMutationMask);
        nbt.putInt("PersonalPositiveMutationMask", personalPositiveMutationMask);
        nbt.putDouble("HealthMutation", healthMutation);
        nbt.putDouble("SpeedMutation", speedMutation);
        nbt.putDouble("JumpMutation", jumpMutation);
        nbt.putDouble("EvasionMutation", evasionMutation);
        nbt.putDouble("EnduranceMutation", enduranceMutation);
        nbt.putDouble("ArmorMutation", armorMutation);
        nbt.putBoolean("TraitMutated", traitMutated);
        nbt.putString("TraitMutationFrom", traitMutationFrom.name());
        nbt.putString("TraitMutationTo", traitMutationTo.name());
        nbt.putBoolean("CoatMutated", coatMutated);
        nbt.putInt("BreedsRemaining", breedsRemaining);
        nbt.putString("Trait", trait.name());
        nbt.putString("BreedingFocus", breedingFocus.name());
        nbt.putString("MountAccess", accessMode.name());
        putUuid(nbt, "SireId", sireId);
        putUuid(nbt, "DamId", damId);
        nbt.putString("SireName", sireName);
        nbt.putString("DamName", damName);
        putUuid(nbt, "ImprintOwnerId", imprintOwnerId);
        nbt.putString("ImprintOwnerName", imprintOwnerName);
        nbt.putFloat("ImprintProgress", imprintProgress);
        nbt.putLong("NextBreedTime", nextBreedTime);
        nbt.putLong("NextCareTime", nextCareTime);
        nbt.putDouble("CurrentStamina", currentStamina);
        nbt.putString("Ancestry", ancestry.stream().map(UUID::toString)
                .collect(java.util.stream.Collectors.joining(",")));
        return nbt;
    }

    public void readNbt(NbtCompound nbt) {
        dataVersion = Math.max(0, nbt.getInt("DataVersion"));
        initialized = nbt.getBoolean("Initialized");
        male = nbt.getBoolean("Male");
        geneticHealth = nbt.getDouble("GeneticHealth");
        geneticSpeed = nbt.getDouble("GeneticSpeed");
        geneticJump = nbt.getDouble("GeneticJump");
        geneticEvasion = nbt.getDouble("GeneticEvasion");
        geneticEndurance = nbt.getDouble("GeneticEndurance");
        geneticArmor = nbt.getDouble("GeneticArmor");
        level = Math.max(1, nbt.getInt("Level"));
        experience = Math.max(0, nbt.getInt("Experience"));
        generation = Math.max(0, nbt.getInt("Generation"));
        mutations = Math.max(0, nbt.getInt("Mutations"));
        personalGeneMutationMask = Math.max(0, nbt.getInt("PersonalGeneMutationMask"));
        personalPositiveMutationMask = Math.max(0, nbt.getInt("PersonalPositiveMutationMask"));
        healthMutation = nbt.getDouble("HealthMutation");
        speedMutation = nbt.getDouble("SpeedMutation");
        jumpMutation = nbt.getDouble("JumpMutation");
        evasionMutation = nbt.getDouble("EvasionMutation");
        enduranceMutation = nbt.getDouble("EnduranceMutation");
        armorMutation = nbt.getDouble("ArmorMutation");
        traitMutated = nbt.getBoolean("TraitMutated");
        traitMutationFrom = HorseTrait.fromId(nbt.getString("TraitMutationFrom"));
        traitMutationTo = HorseTrait.fromId(nbt.getString("TraitMutationTo"));
        coatMutated = nbt.getBoolean("CoatMutated");
        breedsRemaining = nbt.contains("BreedsRemaining")
                ? Math.max(0, nbt.getInt("BreedsRemaining")) : 3;
        trait = HorseTrait.fromId(nbt.getString("Trait"));
        breedingFocus = HorseBreedingFocus.fromId(nbt.getString("BreedingFocus"));
        accessMode = MountAccessMode.fromId(nbt.getString("MountAccess"));
        sireId = getUuid(nbt, "SireId");
        damId = getUuid(nbt, "DamId");
        sireName = getStringOr(nbt, "SireName", "Wild");
        damName = getStringOr(nbt, "DamName", "Wild");
        imprintOwnerId = getUuid(nbt, "ImprintOwnerId");
        imprintOwnerName = getStringOr(nbt, "ImprintOwnerName", "None");
        imprintProgress = Math.max(0.0f, Math.min(1.0f, nbt.getFloat("ImprintProgress")));
        nextBreedTime = nbt.getLong("NextBreedTime");
        nextCareTime = nbt.getLong("NextCareTime");
        currentStamina = nbt.contains("CurrentStamina")
                ? Math.max(0.0, nbt.getDouble("CurrentStamina")) : geneticEndurance;

        ancestry.clear();
        String encodedAncestry = nbt.getString("Ancestry");
        if (!encodedAncestry.isBlank()) {
            for (String encoded : encodedAncestry.split(",")) {
                try {
                    ancestry.add(UUID.fromString(encoded));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        modifiersDirty = true;
    }

    private static void putUuid(NbtCompound nbt, String key, UUID value) {
        if (value != null) nbt.putUuid(key, value);
    }

    private static UUID getUuid(NbtCompound nbt, String key) {
        return nbt.containsUuid(key) ? nbt.getUuid(key) : null;
    }

    private static String getStringOr(NbtCompound nbt, String key, String fallback) {
        String value = nbt.getString(key);
        return value.isBlank() ? fallback : value;
    }
}

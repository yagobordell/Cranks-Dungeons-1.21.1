package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.horse.HorseBreedingManager;
import com.cranks.dungeons.horse.HorseGeneticsData;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HorseGeneticsSyncPacket(
        int entityId,
        boolean male,
        double geneticHealth,
        double geneticSpeed,
        double geneticJump,
        double geneticEvasion,
        double geneticEndurance,
        int level,
        int experience,
        int experienceRequired,
        int generation,
        int mutations,
        int personalGeneMutationMask,
        int personalPositiveMutationMask,
        double healthMutation,
        double speedMutation,
        double jumpMutation,
        double evasionMutation,
        double enduranceMutation,
        boolean traitMutated,
        String traitMutationFrom,
        String traitMutationTo,
        boolean coatMutated,
        int breedsRemaining,
        String grade,
        String mobilityGrade,
        String survivalGrade,
        String trait,
        String breedingFocus,
        String sireName,
        String damName,
        List<UUID> ancestry,
        String imprintOwnerName,
        float imprintProgress,
        double stamina,
        double maxStamina,
        double mountedStaminaRegen,
        double gallopDrain,
        double jumpStaminaCost,
        double gallopSpeedBonus,
        boolean galloping,
        boolean exhausted,
        boolean onRoad,
        long breedingCooldownTicks,
        long careCooldownTicks
) implements CustomPayload {
    public static final Id<HorseGeneticsSyncPacket> ID =
            new Id<>(Identifier.of(CranksDungeons.MOD_ID, "horse_genetics_sync"));
    public static final PacketCodec<RegistryByteBuf, HorseGeneticsSyncPacket> CODEC =
            PacketCodec.of(HorseGeneticsSyncPacket::write, HorseGeneticsSyncPacket::read);

    public static HorseGeneticsSyncPacket from(HorseEntity horse) {
        HorseGeneticsData data = HorseBreedingManager.data(horse);
        long worldTime = horse.getEntityWorld().getTime();
        return new HorseGeneticsSyncPacket(
                horse.getId(), data.male, data.geneticHealth, data.geneticSpeed, data.geneticJump,
                data.geneticEvasion, data.geneticEndurance, data.level, data.experience,
                HorseBreedingManager.experienceForNextLevel(data.level), data.generation,
                data.mutations, data.personalGeneMutationMask, data.personalPositiveMutationMask,
                data.healthMutation, data.speedMutation, data.jumpMutation,
                data.evasionMutation, data.enduranceMutation,
                data.traitMutated, data.traitMutationFrom.displayName(),
                data.traitMutationTo.displayName(), data.coatMutated,
                data.breedsRemaining, HorseBreedingManager.grade(data),
                HorseBreedingManager.mobilityGrade(data), HorseBreedingManager.survivalGrade(data),
                data.trait.displayName(), data.breedingFocus.displayName(),
                data.sireName, data.damName, List.copyOf(data.ancestry), data.imprintOwnerName,
                data.imprintProgress, data.currentStamina, HorseBreedingManager.getMaxStamina(data),
                HorseBreedingManager.mountedStaminaRegenPerSecond(data),
                HorseBreedingManager.gallopDrainPerSecond(data),
                HorseBreedingManager.jumpStaminaCost(data),
                HorseBreedingManager.gallopSpeedBonus(data),
                data.galloping, data.exhausted, data.onRoad,
                Math.max(0L, data.nextBreedTime - worldTime),
                Math.max(0L, data.nextCareTime - worldTime));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(male);
        buf.writeDouble(geneticHealth);
        buf.writeDouble(geneticSpeed);
        buf.writeDouble(geneticJump);
        buf.writeDouble(geneticEvasion);
        buf.writeDouble(geneticEndurance);
        buf.writeVarInt(level);
        buf.writeVarInt(experience);
        buf.writeVarInt(experienceRequired);
        buf.writeVarInt(generation);
        buf.writeVarInt(mutations);
        buf.writeVarInt(personalGeneMutationMask);
        buf.writeVarInt(personalPositiveMutationMask);
        buf.writeDouble(healthMutation);
        buf.writeDouble(speedMutation);
        buf.writeDouble(jumpMutation);
        buf.writeDouble(evasionMutation);
        buf.writeDouble(enduranceMutation);
        buf.writeBoolean(traitMutated);
        buf.writeString(traitMutationFrom, 32);
        buf.writeString(traitMutationTo, 32);
        buf.writeBoolean(coatMutated);
        buf.writeVarInt(breedsRemaining);
        buf.writeString(grade, 4);
        buf.writeString(mobilityGrade, 4);
        buf.writeString(survivalGrade, 4);
        buf.writeString(trait, 32);
        buf.writeString(breedingFocus, 16);
        buf.writeString(sireName, 64);
        buf.writeString(damName, 64);
        buf.writeVarInt(Math.min(16, ancestry.size()));
        for (int i = 0; i < Math.min(16, ancestry.size()); i++) buf.writeUuid(ancestry.get(i));
        buf.writeString(imprintOwnerName, 64);
        buf.writeFloat(imprintProgress);
        buf.writeDouble(stamina);
        buf.writeDouble(maxStamina);
        buf.writeDouble(mountedStaminaRegen);
        buf.writeDouble(gallopDrain);
        buf.writeDouble(jumpStaminaCost);
        buf.writeDouble(gallopSpeedBonus);
        buf.writeBoolean(galloping);
        buf.writeBoolean(exhausted);
        buf.writeBoolean(onRoad);
        buf.writeLong(breedingCooldownTicks);
        buf.writeLong(careCooldownTicks);
    }

    private static HorseGeneticsSyncPacket read(RegistryByteBuf buf) {
        int entityId = buf.readVarInt();
        boolean male = buf.readBoolean();
        double geneticHealth = buf.readDouble();
        double geneticSpeed = buf.readDouble();
        double geneticJump = buf.readDouble();
        double geneticEvasion = buf.readDouble();
        double geneticEndurance = buf.readDouble();
        int level = buf.readVarInt();
        int experience = buf.readVarInt();
        int experienceRequired = buf.readVarInt();
        int generation = buf.readVarInt();
        int mutations = buf.readVarInt();
        int personalGeneMutationMask = buf.readVarInt();
        int personalPositiveMutationMask = buf.readVarInt();
        double healthMutation = buf.readDouble();
        double speedMutation = buf.readDouble();
        double jumpMutation = buf.readDouble();
        double evasionMutation = buf.readDouble();
        double enduranceMutation = buf.readDouble();
        boolean traitMutated = buf.readBoolean();
        String traitMutationFrom = buf.readString(32);
        String traitMutationTo = buf.readString(32);
        boolean coatMutated = buf.readBoolean();
        int breedsRemaining = buf.readVarInt();
        String grade = buf.readString(4);
        String mobilityGrade = buf.readString(4);
        String survivalGrade = buf.readString(4);
        String trait = buf.readString(32);
        String breedingFocus = buf.readString(16);
        String sireName = buf.readString(64);
        String damName = buf.readString(64);
        int ancestrySize = Math.min(16, Math.max(0, buf.readVarInt()));
        List<UUID> ancestry = new ArrayList<>(ancestrySize);
        for (int i = 0; i < ancestrySize; i++) ancestry.add(buf.readUuid());
        return new HorseGeneticsSyncPacket(
                entityId, male, geneticHealth, geneticSpeed, geneticJump, geneticEvasion,
                geneticEndurance, level, experience, experienceRequired, generation, mutations,
                personalGeneMutationMask, personalPositiveMutationMask, healthMutation,
                speedMutation, jumpMutation, evasionMutation, enduranceMutation,
                traitMutated, traitMutationFrom, traitMutationTo, coatMutated,
                breedsRemaining, grade, mobilityGrade, survivalGrade, trait, breedingFocus,
                sireName, damName, List.copyOf(ancestry), buf.readString(64), buf.readFloat(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readLong(), buf.readLong());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

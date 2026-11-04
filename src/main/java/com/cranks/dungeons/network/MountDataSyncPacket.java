package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.MountOwnership;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record MountDataSyncPacket(
        int entityId, String kind, boolean male,
        double health, double speed, double power, double endurance, double utility, double armor,
        int level, int experience, int experienceRequired, int generation, int mutations,
        int breedsRemaining, long breedingCooldownTicks, String trait, String specialization,
        String sireName, String damName, int soulBond, int cargoCapacity, double resource, double maximumResource,
        boolean abilityActive,
        boolean tamed,
        UUID ownerUuid,
        String ownerName,
        String accessMode
) implements CustomPayload {
    public static final Id<MountDataSyncPacket> ID = new Id<>(
            Identifier.of(CranksDungeons.MOD_ID, "mount_data_sync"));
    public static final PacketCodec<RegistryByteBuf, MountDataSyncPacket> CODEC =
            PacketCodec.of(MountDataSyncPacket::write, MountDataSyncPacket::read);

    public static MountDataSyncPacket from(LivingEntity mount) {
        MountData data = MountManager.ensureInitialized(mount);
        MountKind kind = MountKind.of(mount);
        MountOwnership ownership = MountOwnership.from(mount);
        return new MountDataSyncPacket(mount.getId(), kind.name(), data.male,
                data.health, data.speed, data.power, data.endurance, data.utility, data.armor,
                data.level, data.experience, MountManager.experienceForNextLevel(data.level),
                data.generation, data.mutations, data.breedsRemaining,
                Math.max(0L, data.nextBreedTime - mount.getEntityWorld().getTime()),
                data.trait, data.specialization, data.sireName, data.damName, data.soulBond,
                MountManager.cargoCapacity(mount), data.currentResource,
                MountManager.maximumResource(kind, data), data.abilityActive,
                ownership.tamed(), ownership.ownerUuid(), ownership.ownerName(), data.accessMode.name());
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeString(kind, 24);
        buf.writeBoolean(male);
        buf.writeDouble(health);
        buf.writeDouble(speed);
        buf.writeDouble(power);
        buf.writeDouble(endurance);
        buf.writeDouble(utility);
        buf.writeDouble(armor);
        buf.writeVarInt(level);
        buf.writeVarInt(experience);
        buf.writeVarInt(experienceRequired);
        buf.writeVarInt(generation);
        buf.writeVarInt(mutations);
        buf.writeVarInt(breedsRemaining);
        buf.writeLong(breedingCooldownTicks);
        buf.writeString(trait, 32);
        buf.writeString(specialization, 32);
        buf.writeString(sireName, 64);
        buf.writeString(damName, 64);
        buf.writeVarInt(soulBond);
        buf.writeVarInt(cargoCapacity);
        buf.writeDouble(resource);
        buf.writeDouble(maximumResource);
        buf.writeBoolean(abilityActive);
        buf.writeBoolean(tamed);
        buf.writeBoolean(ownerUuid != null);
        if (ownerUuid != null) buf.writeUuid(ownerUuid);
        buf.writeString(ownerName, 64);
        buf.writeString(accessMode, 16);
    }

    private static MountDataSyncPacket read(RegistryByteBuf buf) {
        return new MountDataSyncPacket(buf.readVarInt(), buf.readString(24), buf.readBoolean(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readLong(), buf.readString(32), buf.readString(32),
                buf.readString(64), buf.readString(64), buf.readVarInt(), buf.readVarInt(),
                buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean() ? buf.readUuid() : null, buf.readString(64), buf.readString(16));
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}

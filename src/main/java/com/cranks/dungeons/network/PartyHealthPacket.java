package com.cranks.dungeons.network;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record PartyHealthPacket(
    UUID playerUuid,
    float health,
    float maxHealth,
    float absorption,
    float energyShield,
    float maxEnergyShield
) implements CustomPayload {
    
    public static final CustomPayload.Id<PartyHealthPacket> ID = 
        new CustomPayload.Id<>(Identifier.of(CranksDungeons.MOD_ID, "party_health"));
    
    public static final PacketCodec<RegistryByteBuf, PartyHealthPacket> CODEC = 
        PacketCodec.of(PartyHealthPacket::write, PartyHealthPacket::read);
    
    private void write(RegistryByteBuf buf) {
        buf.writeUuid(playerUuid);
        buf.writeFloat(health);
        buf.writeFloat(maxHealth);
        buf.writeFloat(absorption);
        buf.writeFloat(energyShield);
        buf.writeFloat(maxEnergyShield);
    }
    
    private static PartyHealthPacket read(RegistryByteBuf buf) {
        UUID uuid = buf.readUuid();
        float health = buf.readFloat();
        float maxHealth = buf.readFloat();
        float absorption = buf.readFloat();
        float energyShield = buf.readFloat();
        float maxEnergyShield = buf.readFloat();
        return new PartyHealthPacket(uuid, health, maxHealth, absorption, energyShield, maxEnergyShield);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

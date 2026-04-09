package com.cranks.dungeons.party;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public record PartyMemberData(
    UUID uuid,
    String name,
    float health,
    float maxHealth,
    float absorption,
    float energyShield,
    float maxEnergyShield
) implements CustomPayload {
    
    public static final CustomPayload.Id<PartyMemberData> ID = 
        new CustomPayload.Id<>(net.minecraft.util.Identifier.of("cranks-dungeons", "party_member_data"));
    
    public static final PacketCodec<RegistryByteBuf, PartyMemberData> CODEC = 
        PacketCodec.of(PartyMemberData::write, PartyMemberData::read);
    
    private void write(RegistryByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeString(name);
        buf.writeFloat(health);
        buf.writeFloat(maxHealth);
        buf.writeFloat(absorption);
        buf.writeFloat(energyShield);
        buf.writeFloat(maxEnergyShield);
    }
    
    private static PartyMemberData read(RegistryByteBuf buf) {
        UUID uuid = buf.readUuid();
        String name = buf.readString();
        float health = buf.readFloat();
        float maxHealth = buf.readFloat();
        float absorption = buf.readFloat();
        float energyShield = buf.readFloat();
        float maxEnergyShield = buf.readFloat();
        return new PartyMemberData(uuid, name, health, maxHealth, absorption, energyShield, maxEnergyShield);
    }
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public float getHealthPercent() {
        if (maxHealth <= 0) return 0;
        return health / maxHealth;
    }
    
    public float getShieldPercent() {
        if (maxEnergyShield <= 0) return 0;
        return energyShield / maxEnergyShield;
    }
}

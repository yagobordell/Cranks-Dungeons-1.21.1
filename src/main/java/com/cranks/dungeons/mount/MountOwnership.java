package com.cranks.dungeons.mount;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/** Server-resolved ownership included in mount scouting and inventory syncs. */
public record MountOwnership(boolean tamed, UUID ownerUuid, String ownerName) {
    public static MountOwnership from(LivingEntity mount) {
        if (MountManager.usesCustomOwnership(mount)
                && mount instanceof MountDataProvider provider) {
            MountData data = provider.cranksdungeons$getMountData();
            return new MountOwnership(data.ownerUuid != null, data.ownerUuid, data.ownerName);
        }
        if (!(mount instanceof AbstractHorseEntity horse) || !horse.isTame()) {
            return new MountOwnership(false, null, "");
        }

        UUID ownerUuid = horse.getOwnerUuid();
        String ownerName = "";
        MinecraftServer server = mount.getEntityWorld().getServer();
        if (ownerUuid != null && server != null) {
            ownerName = server.getUserCache().getByUuid(ownerUuid)
                    .map(profile -> profile.getName())
                    .orElse("");
        }
        return new MountOwnership(true, ownerUuid, ownerName);
    }
}

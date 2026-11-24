package com.cranks.dungeons.client;

import com.cranks.dungeons.network.HorseGallopPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.passive.HorseEntity;
import com.cranks.dungeons.mount.MountKind;

public final class HorseGallopInputHandler {
    private static boolean lastSent;
    private static int lastMount=-1;

    private HorseGallopInputHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean active = client.player != null
                    && client.player.getVehicle() instanceof net.minecraft.entity.LivingEntity mount
                    && mount.getControllingPassenger() == client.player
                    && switch (MountKind.of(mount)) {
                        case HORSE, DONKEY, MULE, CAMEL, STRIDER -> true;
                        default -> false;
                    }
                    && client.options.sprintKey.isPressed()
                    && client.options.forwardKey.isPressed();
            int mountId=client.player!=null&&client.player.getVehicle()!=null?client.player.getVehicle().getId():-1;
            if ((active != lastSent || active && mountId!=lastMount) && ClientPlayNetworking.canSend(HorseGallopPayload.ID)) {
                ClientPlayNetworking.send(new HorseGallopPayload(active));
                lastSent = active;
            }
            lastMount=mountId;
            if (client.player == null) lastSent = false;
        });
    }

    public static void reset() {
        lastSent = false;
        lastMount = -1;
    }
}

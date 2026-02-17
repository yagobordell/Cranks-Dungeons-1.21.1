package com.cranks.dungeons.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class ClientShieldEffects {

    private static int shakeTicks = 0;
    private static final int MAX_SHAKE_TICKS = 6; // short shake
    private static float yawOffset = 0f;
    private static float pitchOffset = 0f;

    public static void init() {

    }

    public static void triggerShake() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        // These fields control the vanilla hurt animation.
        // Setting them client-side triggers the usual camera tilt/shake.
        player.hurtTime = 8;      // duration in ticks (adjust to taste)
        player.maxHurtTime = 8;
    }
}
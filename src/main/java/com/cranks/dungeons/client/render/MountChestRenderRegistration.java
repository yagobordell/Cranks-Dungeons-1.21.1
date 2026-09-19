package com.cranks.dungeons.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.LivingEntityRenderer;

public final class MountChestRenderRegistration {
    private MountChestRenderRegistration() { }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, helper, context) -> {
                    if (!(entityRenderer instanceof LivingEntityRenderer renderer)) return;
                    // The feature performs the supported-mount check itself.
                    // Registering it on every living renderer avoids relying
                    // on EntityType identity during Fabric's renderer setup
                    // (which can differ for wrapped/custom renderer entries).
                    // Unsupported entities return before any model work.
                    helper.register(new MountChestFeatureRenderer(renderer));
                });
    }
}

package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.ailment.Ailment;
import com.cranks.dungeons.ailment.AilmentManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AilmentIconRenderer {
    private static final Map<String, Identifier> ICON_TEXTURES = Map.of(
            "BURN", Identifier.of(CranksDungeons.MOD_ID, "textures/mob_effect/burn.png"),
            "POISON", Identifier.of(CranksDungeons.MOD_ID, "textures/mob_effect/poison.png"),
            "FREEZE", Identifier.of(CranksDungeons.MOD_ID, "textures/mob_effect/freeze.png"),
            "BLEED", Identifier.of(CranksDungeons.MOD_ID, "textures/mob_effect/bleed.png"),
            "SHOCK", Identifier.of(CranksDungeons.MOD_ID, "textures/mob_effect/shock.png")
    );

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.gameRenderer().getCamera().getCameraPos();
        
        // We use a broader search or just iterate tracked entities
        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity living && !living.isInvisibleTo(client.player)) {
                renderIconsForEntity(context, living, cameraPos, matrices);
            }
        }
    }

    private static void renderIconsForEntity(WorldRenderContext context, LivingEntity entity, Vec3d cameraPos, MatrixStack matrices) {
        List<Ailment> ailments = AilmentManager.getAilments(entity);
        if (ailments.isEmpty()) return;

        matrices.push();
        // Position above head
        double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * context.gameRenderer().getCamera().getLastTickProgress() - cameraPos.x;
        double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * context.gameRenderer().getCamera().getLastTickProgress() - cameraPos.y + entity.getStandingEyeHeight() + 0.5;
        double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * context.gameRenderer().getCamera().getLastTickProgress() - cameraPos.z;
        
        matrices.translate(x, y, z);
        
        // Billboard rotation
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-context.gameRenderer().getCamera().getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(context.gameRenderer().getCamera().getPitch()));

        float scale = 0.4f;
        matrices.scale(scale, scale, scale);

        int count = ailments.size();
        float spacing = 1.2f;
        float startX = -(count - 1) * spacing / 2.0f;

        long time = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            Ailment ailment = ailments.get(i);
            Identifier tex = ICON_TEXTURES.get(ailment.getType().name());
            if (tex == null) continue;

            matrices.push();
            matrices.translate(startX + i * spacing, MathHelper.sin((time + i * 200) / 300f) * 0.15f, 0);
            
            drawSprite(matrices, context.consumers(), tex);
            matrices.pop();
        }

        matrices.pop();
    }

    private static void drawSprite(MatrixStack matrices, VertexConsumerProvider consumers, Identifier texture) {
        VertexConsumer buffer = consumers.getBuffer(RenderLayers.entityCutout(texture));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // Face
        buffer.vertex(matrix, -0.5f, -0.5f, 0).color(1f, 1f, 1f, 1f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0, 0, 1);
        buffer.vertex(matrix, 0.5f, -0.5f, 0).color(1f, 1f, 1f, 1f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0, 0, 1);
        buffer.vertex(matrix, 0.5f, 0.5f, 0).color(1f, 1f, 1f, 1f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0, 0, 1);
        buffer.vertex(matrix, -0.5f, 0.5f, 0).color(1f, 1f, 1f, 1f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(0, 0, 1);
    }
}

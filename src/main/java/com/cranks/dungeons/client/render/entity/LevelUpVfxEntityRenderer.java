package com.cranks.dungeons.client.render.entity;

import com.cranks.dungeons.entity.LevelUpVfxEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

/** Vanilla pixel sprites, with no enclosing mesh, beams, shockwaves or lingering particle bursts. */
public class LevelUpVfxEntityRenderer extends EntityRenderer<LevelUpVfxEntity> {
    private static final Identifier MOTE = Identifier.ofVanilla("textures/particle/glow.png");
    private static final int MOTE_COUNT = 12;

    public LevelUpVfxEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override public Identifier getTexture(LevelUpVfxEntity entity) { return MOTE; }

    @Override
    public void render(LevelUpVfxEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertices, int light) {
        float age = entity.age + tickDelta;
        if (age >= LevelUpVfxEntity.DURATION_TICKS) return;
        float r = entity.isWorldTree() ? .66f : .86f;
        float g = entity.isWorldTree() ? .79f : .73f;
        float b = entity.isWorldTree() ? .86f : .46f;
        var consumer = vertices.getBuffer(RenderLayer.getEntityTranslucentEmissive(MOTE));
        for (int i = 0; i < MOTE_COUNT; i++) {
            float delay = sample(i, 0) * 5;
            float duration = 15 + sample(i, 1) * 2;
            float t = (age - delay) / duration;
            if (t <= 0 || t >= 1) continue;
            float fade = MathHelper.sin(t * (float)Math.PI);
            float angle = sample(i, 2) * MathHelper.TAU + entity.getId() * .37f;
            float radius = .28f + sample(i, 3) * .19f;
            float x = MathHelper.cos(angle) * radius;
            float z = MathHelper.sin(angle) * radius;
            float y = .08f + sample(i, 4) * .15f + t * (.65f + sample(i, 5) * .3f);
            float size = (.035f + sample(i, 6) * .02f) * (.8f + fade * .2f);
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(dispatcher.getRotation());
            var matrix = matrices.peek().getPositionMatrix();
            // Faint outer light and a small readable pixel core, both depth-tested against the player.
            quad(consumer, matrix, size * 1.8f, r, g, b, fade * .10f);
            quad(consumer, matrix, size, r, g, b, fade * .65f);
            matrices.pop();
        }
    }

    private static float sample(int index, int channel) {
        int hash = (index + 1) * 0x45d9f3b + channel * 0x119de1f3;
        hash = (hash ^ (hash >>> 16)) * 0x45d9f3b;
        hash ^= hash >>> 16;
        return (hash & 0xffff) / 65535f;
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, float size,
                             float r, float g, float b, float alpha) {
        vertex(consumer, matrix, -size, -size, 0, 1, r, g, b, alpha);
        vertex(consumer, matrix, size, -size, 1, 1, r, g, b, alpha);
        vertex(consumer, matrix, size, size, 1, 0, r, g, b, alpha);
        vertex(consumer, matrix, -size, size, 0, 0, r, g, b, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v,
                               float r, float g, float b, float alpha) {
        consumer.vertex(matrix, x, y, 0).color(r, g, b, alpha).texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
    }
}

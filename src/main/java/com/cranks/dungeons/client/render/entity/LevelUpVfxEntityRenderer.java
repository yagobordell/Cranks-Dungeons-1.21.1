package com.cranks.dungeons.client.render.entity;

import com.cranks.dungeons.entity.LevelUpVfxEntity;
import com.cranks.dungeons.client.render.entity.state.LevelUpVfxEntityRenderState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class LevelUpVfxEntityRenderer extends EntityRenderer<LevelUpVfxEntity, LevelUpVfxEntityRenderState> {
    private static final Identifier BEAM_TEXTURE = Identifier.tryParse("cranks-dungeons:textures/entity/beam.png");

    public LevelUpVfxEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public LevelUpVfxEntityRenderState createRenderState() {
        return new LevelUpVfxEntityRenderState();
    }

    @Override
    public void updateRenderState(LevelUpVfxEntity entity, LevelUpVfxEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.isWorldTree = entity.isWorldTree();
        state.age = entity.age + tickDelta;
    }

    @Override
    public void render(LevelUpVfxEntityRenderState state, MatrixStack matrices, net.minecraft.client.render.command.OrderedRenderCommandQueue vertexConsumers, net.minecraft.client.render.state.CameraRenderState light) {
        float age = state.age;
        float maxAge = 60.0f;
        float progress = age / maxAge;
        if (progress > 1.0f) progress = 1.0f;

        float alpha = 1.0f;
        if (progress < 0.1f) {
            alpha = progress / 0.1f;
        } else if (progress > 0.6f) {
            alpha = 1.0f - ((progress - 0.6f) / 0.4f);
        }

        float r, g, b;
        if (state.isWorldTree) {
            r = 0.2f; g = 1.0f; b = 0.3f;
        } else {
            r = 1.0f; g = 0.8f; b = 0.1f;
        }

        matrices.push();
        float vOffset = -(age * 0.05f);

        final float finalAlpha = alpha;
        final float finalR = r, finalG = g, finalB = b;
        
        vertexConsumers.submitCustom(matrices, RenderLayers.entityTranslucentEmissive(BEAM_TEXTURE), (matrixEntry, consumer) -> {
            float height = 5.0f; // Much shorter, aura-like
            Matrix4f posMatrix = matrixEntry.getPositionMatrix();

            // 1. Draw glowing hemisphere at the base (smaller)
            float sphereRadius = 0.8f + MathHelper.sin(age * 0.2f) * 0.1f;
            MatrixStack localMatrices = new MatrixStack();
            localMatrices.multiplyPositionMatrix(posMatrix);
            localMatrices.translate(0, 0.1f, 0); // slightly above ground
            drawHemisphere(consumer, localMatrices.peek().getPositionMatrix(), sphereRadius, finalR, finalG, finalB, finalAlpha * 0.6f, 16);

            // 2. Draw expanding rings (shockwaves)
            float ringProgress1 = (age % 20.0f) / 20.0f; // loops every 20 ticks
            float ringRadius1 = ringProgress1 * 2.5f;
            float ringAlpha1 = finalAlpha * (1.0f - ringProgress1) * 0.8f;
            localMatrices = new MatrixStack();
            localMatrices.multiplyPositionMatrix(posMatrix);
            localMatrices.translate(0, 0.05f, 0);
            drawRing(consumer, localMatrices.peek().getPositionMatrix(), ringRadius1 - 0.15f, ringRadius1, finalR, finalG, finalB, ringAlpha1, 32);
            
            float ringProgress2 = ((age + 10.0f) % 20.0f) / 20.0f;
            float ringRadius2 = ringProgress2 * 2.5f;
            float ringAlpha2 = finalAlpha * (1.0f - ringProgress2) * 0.8f;
            drawRing(consumer, localMatrices.peek().getPositionMatrix(), ringRadius2 - 0.15f, ringRadius2, finalR, finalG, finalB, ringAlpha2, 32);

            // 3. Draw smooth multi-sided inner cylinder
            localMatrices = new MatrixStack();
            localMatrices.multiplyPositionMatrix(posMatrix);
            localMatrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-age * 4.0f));
            drawCylinder(consumer, localMatrices.peek().getPositionMatrix(), 0.35f, height, finalR, finalG, finalB, finalAlpha, 0.0f, 0, vOffset, 16);

            // 4. Draw smooth multi-sided outer cylinder (pulsing)
            float outerRadius = 0.6f + MathHelper.sin(age * 0.15f) * 0.1f;
            float outerAlphaBase = finalAlpha * 0.5f;
            float outerVOffset = -(age * 0.03f);
            localMatrices = new MatrixStack();
            localMatrices.multiplyPositionMatrix(posMatrix);
            localMatrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 2.5f));
            drawCylinder(consumer, localMatrices.peek().getPositionMatrix(), outerRadius, height + 1.0f, finalR, finalG, finalB, outerAlphaBase, 0.0f, 0, outerVOffset, 16);
        });
        
        matrices.pop();
    }

    @Override
    public boolean shouldRender(com.cranks.dungeons.entity.LevelUpVfxEntity entity, net.minecraft.client.render.Frustum frustum, double x, double y, double z) {
        return true;
    }

    private void drawCylinder(VertexConsumer consumer, Matrix4f posMatrix, float radius, float height, float r, float g, float b, float alphaBase, float alphaTop, float uOffset, float vOffset, int segments) {
        float angleStep = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float x1 = MathHelper.cos(angle1) * radius;
            float z1 = MathHelper.sin(angle1) * radius;
            float x2 = MathHelper.cos(angle2) * radius;
            float z2 = MathHelper.sin(angle2) * radius;

            float u1 = (float) i / segments + uOffset;
            float u2 = (float) (i + 1) / segments + uOffset;

            // Draw quad for this segment (outside) - must be CCW from outside perspective
            // x2 is on the left, x1 is on the right when looking from outside
            drawVertex(consumer, posMatrix, x2, 0, z2, u2, 1 + vOffset, r, g, b, alphaBase);
            drawVertex(consumer, posMatrix, x1, 0, z1, u1, 1 + vOffset, r, g, b, alphaBase);
            drawVertex(consumer, posMatrix, x1, height, z1, u1, 0 + vOffset, r, g, b, alphaTop);
            drawVertex(consumer, posMatrix, x2, height, z2, u2, 0 + vOffset, r, g, b, alphaTop);
            
            // Draw inside face too (reverse winding) - must be CCW from inside perspective
            // x1 is on the left, x2 is on the right when looking from inside
            drawVertex(consumer, posMatrix, x1, 0, z1, u1, 1 + vOffset, r, g, b, alphaBase);
            drawVertex(consumer, posMatrix, x2, 0, z2, u2, 1 + vOffset, r, g, b, alphaBase);
            drawVertex(consumer, posMatrix, x2, height, z2, u2, 0 + vOffset, r, g, b, alphaTop);
            drawVertex(consumer, posMatrix, x1, height, z1, u1, 0 + vOffset, r, g, b, alphaTop);
        }
    }

    private void drawRing(VertexConsumer consumer, Matrix4f posMatrix, float innerRadius, float outerRadius, float r, float g, float b, float alpha, int segments) {
        if (innerRadius < 0) innerRadius = 0;
        float angleStep = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float ix1 = MathHelper.cos(angle1) * innerRadius, iz1 = MathHelper.sin(angle1) * innerRadius;
            float ox1 = MathHelper.cos(angle1) * outerRadius, oz1 = MathHelper.sin(angle1) * outerRadius;
            
            float ix2 = MathHelper.cos(angle2) * innerRadius, iz2 = MathHelper.sin(angle2) * innerRadius;
            float ox2 = MathHelper.cos(angle2) * outerRadius, oz2 = MathHelper.sin(angle2) * outerRadius;

            // Top face
            drawVertex(consumer, posMatrix, ox1, 0, oz1, 0, 0, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ix1, 0, iz1, 0, 1, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ix2, 0, iz2, 1, 1, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ox2, 0, oz2, 1, 0, r, g, b, alpha);
            
            // Bottom face
            drawVertex(consumer, posMatrix, ix1, 0, iz1, 0, 1, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ox1, 0, oz1, 0, 0, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ox2, 0, oz2, 1, 0, r, g, b, alpha);
            drawVertex(consumer, posMatrix, ix2, 0, iz2, 1, 1, r, g, b, alpha);
        }
    }

    private void drawHemisphere(VertexConsumer consumer, Matrix4f posMatrix, float radius, float r, float g, float b, float alpha, int segments) {
        int vSegments = segments / 2;
        for (int i = 0; i < segments; i++) {
            float theta1 = (float) (i * Math.PI * 2.0 / segments);
            float theta2 = (float) ((i + 1) * Math.PI * 2.0 / segments);
            for (int j = 0; j < vSegments / 2; j++) { // Only top half
                float phi1 = (float) (j * Math.PI / vSegments);
                float phi2 = (float) ((j + 1) * Math.PI / vSegments);

                float x1 = radius * MathHelper.sin(phi1) * MathHelper.cos(theta1);
                float y1 = radius * MathHelper.cos(phi1);
                float z1 = radius * MathHelper.sin(phi1) * MathHelper.sin(theta1);
                
                float x2 = radius * MathHelper.sin(phi1) * MathHelper.cos(theta2);
                float y2 = y1;
                float z2 = radius * MathHelper.sin(phi1) * MathHelper.sin(theta2);
                
                float x3 = radius * MathHelper.sin(phi2) * MathHelper.cos(theta2);
                float y3 = radius * MathHelper.cos(phi2);
                float z3 = radius * MathHelper.sin(phi2) * MathHelper.sin(theta2);
                
                float x4 = radius * MathHelper.sin(phi2) * MathHelper.cos(theta1);
                float y4 = y3;
                float z4 = radius * MathHelper.sin(phi2) * MathHelper.sin(theta1);

                drawVertex(consumer, posMatrix, x1, y1, z1, 0, 0, r, g, b, alpha);
                drawVertex(consumer, posMatrix, x4, y4, z4, 0, 1, r, g, b, alpha);
                drawVertex(consumer, posMatrix, x3, y3, z3, 1, 1, r, g, b, alpha);
                drawVertex(consumer, posMatrix, x2, y2, z2, 1, 0, r, g, b, alpha);
            }
        }
    }

    private void drawVertex(VertexConsumer consumer, Matrix4f posMatrix, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        consumer.vertex(posMatrix, x, y, z)
                .color(r, g, b, a)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(15728880)
                .normal(0, 1, 0);
    }
}

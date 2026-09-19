package com.cranks.dungeons.client.render;

import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Renders the same pannier model used by vanilla donkeys and mules. Vanilla
 * does not use a chest block here: the two chest cuboids are part of the
 * donkey model and use the donkey texture atlas.
 */
public final class MountChestFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {
    private static final Identifier DONKEY_TEXTURE =
            Identifier.ofVanilla("textures/entity/horse/donkey.png");
    private static final ModelPart PANNIERS = createPanniers();

    public MountChestFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        renderPanniers(matrices, vertices, light, entity);
    }

    /**
     * Draws panniers from the entity renderer's normal model transform. This
     * is also used by the renderer mixin as a fallback for renderers that are
     * constructed before Fabric's feature-registration callback is dispatched.
     */
    public static void renderPanniers(MatrixStack matrices, VertexConsumerProvider vertices,
                                      int light, LivingEntity entity) {
        if (!(entity instanceof MountDataProvider provider)
                || !provider.cranksdungeons$hasMountChest() || entity.isBaby()) return;

        MountKind kind = MountKind.of(entity);
        if (kind == MountKind.DONKEY || kind == MountKind.MULE
                || kind == MountKind.UNSUPPORTED) return;

        Placement placement = placement(kind);
        matrices.push();
        // The renderer's current model transform maps this positive local
        // translation downward on screen. Eight model pixels = 0.5 blocks.
        matrices.translate(placement.x, placement.y + 0.5f, placement.z);
        matrices.scale(placement.scale, placement.scale, placement.scale);
        PANNIERS.render(matrices,
                vertices.getBuffer(RenderLayer.getEntityCutoutNoCull(DONKEY_TEXTURE)),
                light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
        matrices.pop();
    }

    /** Exact body/chest hierarchy from DonkeyEntityModel, without its body cuboid. */
    private static ModelPart createPanniers() {
        ModelData data = new ModelData();
        ModelPartData body = data.getRoot().addChild("body", ModelPartBuilder.create(),
                ModelTransform.pivot(0.0f, 11.0f, 5.0f));
        ModelPartBuilder chest = ModelPartBuilder.create()
                .uv(26, 21)
                .cuboid(-4.0f, -8.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        body.addChild("left_chest", chest,
                ModelTransform.of(6.0f, -8.0f, 0.0f, 0.0f, -1.5707964f, 0.0f));
        body.addChild("right_chest", chest,
                ModelTransform.of(-6.0f, -8.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        TexturedModelData textured = TexturedModelData.of(data, 64, 64);
        return textured.createModel().getChild("body");
    }

    private static Placement placement(MountKind kind) {
        return switch (kind) {
            // Horse-family model roots already match vanilla donkey geometry.
            case HORSE, SKELETON_HORSE -> new Placement(0.0f, 0.0f, 0.0f, 1.0f);
            // The remaining mounts have different model roots, so move the
            // same panniers onto their body and scale them to fit.
            case CAMEL -> new Placement(0.0f, -0.44f, 0.28f, 0.80f);
            case STRIDER -> new Placement(0.0f, -0.69f, -0.31f, 0.80f);
            case PIG -> new Placement(0.0f, 0.06f, -0.69f, 0.62f);
            default -> new Placement(0.0f, 0.0f, 0.0f, 1.0f);
        };
    }

    private record Placement(float x, float y, float z, float scale) { }
}

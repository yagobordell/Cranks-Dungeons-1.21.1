/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  net.minecraft.class_757
 *  net.minecraft.class_761
 *  net.minecraft.class_765
 *  net.minecraft.class_9779
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_765;
import net.minecraft.class_9779;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.modelparts.ModelPartsParticleSystem;
import punchy.config.PunchyConfig;

@Mixin(value={class_761.class})
public class LevelRendererMixin {
    @Inject(method={"method_22710(Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lnet/minecraft/class_757;Lnet/minecraft/class_765;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"}, at={@At(value="TAIL")})
    private void punchy$renderReleasedItemParticles(class_9779 deltaTracker, boolean renderBlockOutline, class_4184 camera, class_757 gameRenderer, class_765 lightTexture, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!PunchyConfig.isModEnabled()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1687 == null) {
            return;
        }
        Matrix4f particleModelView = new Matrix4f((Matrix4fc)modelViewMatrix);
        particleModelView.setTranslation(0.0f, 0.0f, 0.0f);
        class_4587 worldBasePose = new class_4587();
        worldBasePose.method_23760().method_23761().set((Matrix4fc)particleModelView);
        worldBasePose.method_23760().method_23762().set((Matrix3fc)new Matrix3f((Matrix4fc)particleModelView));
        class_4597.class_4598 buffer = client.method_22940().method_23000();
        ModelPartsParticleSystem.renderReleasedParticlesInWorld(client, (class_4597)buffer, worldBasePose);
        buffer.method_22993();
    }
}


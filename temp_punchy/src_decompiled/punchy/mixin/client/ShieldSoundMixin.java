/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1113
 *  net.minecraft.class_1144
 *  net.minecraft.class_1306
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package punchy.mixin.client;

import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_1306;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.ShieldImpactPhysics;
import punchy.client.state.MovementStateMachine;
import punchy.config.PunchyConfig;

@Mixin(value={class_1144.class})
public class ShieldSoundMixin {
    @Inject(method={"method_4873(Lnet/minecraft/class_1113;)V"}, at={@At(value="HEAD")})
    private void punchy$onPlaySound(class_1113 sound, CallbackInfo ci) {
        if (sound == null) {
            return;
        }
        if (PunchyConfig.disableArmPhysics() || PunchyConfig.disableNativeItemPhysics()) {
            return;
        }
        class_2960 id = sound.method_4775();
        if (id != null && id.method_12832().equals("item.shield.block")) {
            class_1306 arm;
            double distSq;
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 != null && (distSq = mc.field_1724.method_5649(sound.method_4784(), sound.method_4779(), sound.method_4778())) < 4.0 && (arm = ShieldImpactPhysics.resolveImpactArm(mc.field_1724)) != null) {
                ShieldImpactPhysics.get().trigger(arm);
                MovementStateMachine.get().triggerShieldBlockImpact(arm);
            }
        }
    }
}


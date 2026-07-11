/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Implements
 *  org.spongepowered.asm.mixin.Interface
 *  org.spongepowered.asm.mixin.Mixin
 */
package punchy.mixin.client;

import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import punchy.client.modelparts.ModelPartsSystem;
import punchy.compat.iris.IrisItemLightProviderCompat;

@Mixin(value={class_1792.class})
@Implements(value={@Interface(iface=IrisItemLightProviderCompat.class, prefix="punchy$")})
public class ItemIrisLightMixin {
    private static final int MAX_MODEL_EMISSION = 15;
    private static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    public int punchy$getLightEmission(class_1657 player, class_1799 stack) {
        int vanilla = ItemIrisLightMixin.getVanillaEmission(stack);
        int modelEmission = ItemIrisLightMixin.getModelWorldEmission(player, stack);
        return Math.max(vanilla, ItemIrisLightMixin.scaleEmission(modelEmission));
    }

    public Vector3f punchy$getLightColor(class_1657 player, class_1799 stack) {
        return DEFAULT_COLOR;
    }

    private static int getVanillaEmission(class_1799 stack) {
        if (stack == null) {
            return 0;
        }
        class_1792 item = stack.method_7909();
        if (item instanceof class_1747) {
            class_1747 blockItem = (class_1747)item;
            return blockItem.method_7711().method_9564().method_26213();
        }
        return 0;
    }

    private static int getModelWorldEmission(class_1657 player, class_1799 stack) {
        if (player == null || stack == null) {
            return 0;
        }
        return ModelPartsSystem.get().getModelWorldEmission(player, stack);
    }

    private static int scaleEmission(int emission) {
        if (emission <= 0) {
            return 0;
        }
        int clamped = Math.min(emission, 15);
        return Math.round((float)clamped * 1.0f);
    }
}


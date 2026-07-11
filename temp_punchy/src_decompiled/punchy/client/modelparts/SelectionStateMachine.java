/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 */
package punchy.client.modelparts;

import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import punchy.client.model.ItemModelDefinition;
import punchy.client.model.ResourcePackGeoRegistry;
import punchy.client.model.ResourcePackItemModelRegistry;

final class SelectionStateMachine {
    SelectionStateMachine() {
    }

    void onResourcesReload() {
    }

    class_2960 resolveModelId(class_1799 stack, class_1657 player) {
        if (stack == null || stack.method_7960() || player == null) {
            return null;
        }
        return ResourcePackGeoRegistry.getModelForTarget(stack, player);
    }

    ItemModelDefinition getItemModel(class_2960 modelId) {
        return ResourcePackItemModelRegistry.getModel(modelId);
    }

    int getModelWorldEmission(class_1657 player, class_1799 stack) {
        if (player == null || stack == null || stack.method_7960()) {
            return 0;
        }
        class_2960 modelId = this.resolveModelId(stack, player);
        if (modelId == null) {
            return 0;
        }
        ItemModelDefinition model = this.getItemModel(modelId);
        if (model == null) {
            return 0;
        }
        int max = 0;
        for (ItemModelDefinition.Element element : model.getElements()) {
            if (!element.hasWorldLight()) continue;
            max = Math.max(max, element.getLightEmission());
        }
        return max;
    }
}


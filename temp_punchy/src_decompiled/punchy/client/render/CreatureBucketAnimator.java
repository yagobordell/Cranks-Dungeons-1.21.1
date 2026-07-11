/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1058
 *  net.minecraft.class_1087
 *  net.minecraft.class_1268
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2350
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3298
 *  net.minecraft.class_3300
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_5819
 *  net.minecraft.class_746
 *  net.minecraft.class_777
 *  net.minecraft.class_7833
 *  net.minecraft.class_7923
 *  net.minecraft.class_806
 *  net.minecraft.class_809
 *  net.minecraft.class_811
 */
package punchy.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1058;
import net.minecraft.class_1087;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5819;
import net.minecraft.class_746;
import net.minecraft.class_777;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_806;
import net.minecraft.class_809;
import net.minecraft.class_811;
import punchy.client.bedrockitems.BedrockItemSystem;
import punchy.client.render.AxolotlPhysics;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.mixin.client.accessor.ItemRendererInvoker;

public final class CreatureBucketAnimator {
    private static final EnumMap<class_1268, AxolotlPhysics> PHYSICS = new EnumMap(class_1268.class);
    private static class_3300 cachedResourceManager;
    private static final Map<class_2960, Boolean> RESOURCE_PACK_ITEM_DEFINITION_CACHE;
    private static final float AXOLOTL_ROLL_SCALE = 1.4f;

    private CreatureBucketAnimator() {
    }

    public static boolean shouldAnimate(class_1799 stack, class_811 context) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (context != class_811.field_4322 && context != class_811.field_4321 && context != class_811.field_4320 && context != class_811.field_4323) {
            return false;
        }
        if (!CreatureBucketAnimator.isCreatureBucket(stack)) {
            return false;
        }
        boolean hasDefinition = BedrockItemSystem.get().hasDefinition(stack);
        boolean hasResourcePackModel = CreatureBucketAnimator.isResourcePackItemDefinition(stack);
        if (stack.method_31574(class_1802.field_28354) && hasDefinition) {
            return false;
        }
        return hasResourcePackModel;
    }

    private static boolean isResourcePackItemDefinition(class_1799 stack) {
        class_2960 itemId;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1478() == null || stack == null || stack.method_7960()) {
            return false;
        }
        class_3300 manager = mc.method_1478();
        if (manager != cachedResourceManager) {
            cachedResourceManager = manager;
            RESOURCE_PACK_ITEM_DEFINITION_CACHE.clear();
        }
        if ((itemId = class_7923.field_41178.method_10221((Object)stack.method_7909())) == null) {
            return false;
        }
        Boolean cached = RESOURCE_PACK_ITEM_DEFINITION_CACHE.get(itemId);
        if (cached != null) {
            return cached;
        }
        class_2960 defId = class_2960.method_60655((String)itemId.method_12836(), (String)("items/" + itemId.method_12832() + ".json"));
        List resources = manager.method_14489(defId);
        if (resources == null || resources.isEmpty()) {
            RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, false);
            return false;
        }
        for (class_3298 resource : resources) {
            String packId;
            if (resource == null || (packId = resource.method_14480()) == null || "vanilla".equals(packId)) continue;
            RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, true);
            return true;
        }
        RESOURCE_PACK_ITEM_DEFINITION_CACHE.put(itemId, false);
        return false;
    }

    public static void renderSplit(ItemRendererInvoker renderer, class_1087 model, class_1799 stack, int light, int overlay, class_4587 poseStack, class_4588 consumer, boolean leftHand) {
        boolean enableCreaturePhysics;
        if (renderer == null || model == null) {
            return;
        }
        FilteredBakedModel bucketModel = new FilteredBakedModel(model, stack, false);
        FilteredBakedModel creatureModel = new FilteredBakedModel(model, stack, true);
        renderer.punchy$renderModelLists(bucketModel, stack, light, overlay, poseStack, consumer);
        VanillaFirstPersonItemLayer.ToolKind kind = ToolKindResolver.resolveItemKind(stack, VanillaFirstPersonItemLayer.ToolKind.CREATURE_BUCKET);
        ToolTuning.Profile profile = ToolTuning.resolveProfile(stack, kind, leftHand);
        ToolTuning.PhysicsSettings settings = profile != null ? profile.physicsSettings : null;
        boolean bl = enableCreaturePhysics = settings == null || settings.enableCreaturePhysics == null || settings.enableCreaturePhysics != false;
        if (!enableCreaturePhysics) {
            renderer.punchy$renderModelLists(creatureModel, stack, light, overlay, poseStack, consumer);
            return;
        }
        AxolotlPhysics physics = CreatureBucketAnimator.stateFor(leftHand ? class_1268.field_5810 : class_1268.field_5808);
        class_746 player = class_310.method_1551().field_1724;
        physics.update(player);
        float roll = physics.getRoll() * 1.4f;
        float lift = physics.getLift();
        float rollLift = Math.abs(roll) * 0.15f;
        poseStack.method_22903();
        if (lift != 0.0f || rollLift != 0.0f) {
            poseStack.method_46416(0.0f, lift + rollLift, 0.0f);
        }
        if (leftHand) {
            poseStack.method_22907(class_7833.field_40716.rotation((float)Math.PI));
        }
        if (roll != 0.0f) {
            poseStack.method_22907(class_7833.field_40718.rotation(roll));
        }
        if (leftHand) {
            poseStack.method_22907(class_7833.field_40715.rotation((float)Math.PI));
        }
        renderer.punchy$renderModelLists(creatureModel, stack, light, overlay, poseStack, consumer);
        poseStack.method_22909();
    }

    private static boolean isCreatureTexture(class_1799 stack, class_2960 name) {
        boolean hasCreatureToken;
        if (name == null) {
            return false;
        }
        String path = name.method_12832();
        if (path == null) {
            return false;
        }
        String normalized = path.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("item/")) {
            return false;
        }
        if (normalized.contains("_water") || normalized.contains("water")) {
            return false;
        }
        boolean bl = hasCreatureToken = normalized.contains("axolotl") || normalized.contains("axo") || normalized.contains("tropical_fish") || normalized.contains("tropical") || normalized.contains("pufferfish") || normalized.contains("puffer") || normalized.contains("cod") || normalized.contains("salmon") || normalized.contains("tadpole");
        if (normalized.contains("bucket")) {
            return hasCreatureToken;
        }
        if (normalized.contains("balde") || normalized.contains("baldes")) {
            return hasCreatureToken;
        }
        if (hasCreatureToken) {
            return true;
        }
        return stack != null && CreatureBucketAnimator.isCreatureBucket(stack);
    }

    private static boolean isCreatureBucket(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        return stack.method_31574(class_1802.field_28354) || stack.method_31574(class_1802.field_8478) || stack.method_31574(class_1802.field_8108) || stack.method_31574(class_1802.field_8666) || stack.method_31574(class_1802.field_8714) || stack.method_31574(class_1802.field_37533);
    }

    private static AxolotlPhysics stateFor(class_1268 hand) {
        class_1268 resolved = hand == null ? class_1268.field_5808 : hand;
        return PHYSICS.computeIfAbsent(resolved, h -> new AxolotlPhysics());
    }

    static {
        RESOURCE_PACK_ITEM_DEFINITION_CACHE = new HashMap<class_2960, Boolean>();
    }

    private static final class FilteredBakedModel
    implements class_1087 {
        private final class_1087 delegate;
        private final class_1799 stack;
        private final boolean creature;

        private FilteredBakedModel(class_1087 delegate, class_1799 stack, boolean creature) {
            this.delegate = delegate;
            this.stack = stack;
            this.creature = creature;
        }

        public List<class_777> method_4707(class_2680 state, class_2350 side, class_5819 rand) {
            List original = this.delegate.method_4707(state, side, rand);
            if (original == null || original.isEmpty()) {
                return original;
            }
            ArrayList<class_777> filtered = new ArrayList<class_777>(original.size());
            for (class_777 quad : original) {
                boolean isCreature;
                class_1058 sprite;
                if (quad == null || (sprite = quad.method_35788()) == null || sprite.method_45851() == null || (isCreature = CreatureBucketAnimator.isCreatureTexture(this.stack, sprite.method_45851().method_45816())) != this.creature) continue;
                filtered.add(quad);
            }
            return filtered;
        }

        public boolean method_4712() {
            return this.delegate.method_4712();
        }

        public boolean method_4708() {
            return this.delegate.method_4708();
        }

        public boolean method_24304() {
            return this.delegate.method_24304();
        }

        public boolean method_4713() {
            return this.delegate.method_4713();
        }

        public class_1058 method_4711() {
            return this.delegate.method_4711();
        }

        public class_809 method_4709() {
            return this.delegate.method_4709();
        }

        public class_806 method_4710() {
            return this.delegate.method_4710();
        }
    }
}


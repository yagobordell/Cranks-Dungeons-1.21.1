/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3300
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_5321
 *  net.minecraft.class_6862
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 *  net.minecraft.class_7924
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package punchy.client.bedrockitems;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.bedrockitems.BedrockAxolPhysicsStateMachine;
import punchy.client.bedrockitems.BedrockItemAnimationManager;
import punchy.client.bedrockitems.BedrockItemDefinition;
import punchy.client.bedrockitems.BedrockItemRenderer;
import punchy.client.bedrockitems.BedrockPufferPhysicsStateMachine;
import punchy.client.render.ToolTuning;
import punchy.config.PunchyConfig;

public final class BedrockItemSystem {
    private static final BedrockItemSystem INSTANCE = new BedrockItemSystem();
    private static final EnumMap<class_1306, Matrix4f> CURRENT_PHYSICS_DELTA = new EnumMap(class_1306.class);
    private final List<BedrockItemDefinition> definitions = new ArrayList<BedrockItemDefinition>();
    private final BedrockAxolPhysicsStateMachine axolPhysicsState = new BedrockAxolPhysicsStateMachine();
    private final BedrockPufferPhysicsStateMachine pufferPhysicsState = new BedrockPufferPhysicsStateMachine();

    private BedrockItemSystem() {
    }

    public static BedrockItemSystem get() {
        return INSTANCE;
    }

    public static void setCurrentPhysicsDelta(class_1306 arm, Matrix4f delta) {
        if (arm == null) {
            return;
        }
        if (delta == null) {
            CURRENT_PHYSICS_DELTA.remove(arm);
        } else {
            CURRENT_PHYSICS_DELTA.put(arm, new Matrix4f((Matrix4fc)delta));
        }
    }

    public static Matrix4f getCurrentPhysicsDelta(class_1306 arm) {
        if (arm == null) {
            return null;
        }
        Matrix4f stored = CURRENT_PHYSICS_DELTA.get(arm);
        return stored == null ? null : new Matrix4f((Matrix4fc)stored);
    }

    public static void clearCurrentPhysicsDelta(class_1306 arm) {
        if (arm != null) {
            CURRENT_PHYSICS_DELTA.remove(arm);
        }
    }

    public void onResourceReload(class_3300 manager) {
        if (PunchyConfig.disableResourcePackModelParts()) {
            return;
        }
        this.definitions.clear();
        this.definitions.addAll(BedrockItemDefinition.loadAll(manager));
        for (BedrockItemDefinition def : this.definitions) {
            def.loadResources(manager);
        }
    }

    public void tick(class_310 client) {
        if (PunchyConfig.disableResourcePackModelParts()) {
            return;
        }
        PunchyAnimationManager.updateItemOverrides(client);
        BedrockItemAnimationManager.tick(client);
        this.axolPhysicsState.tick(client);
        this.pufferPhysicsState.tick(client);
    }

    BedrockAxolPhysicsStateMachine.FeedbackPose getAxolPhysicsPose(class_1306 arm, float partialTicks) {
        return this.axolPhysicsState.getPose(arm, partialTicks);
    }

    BedrockPufferPhysicsStateMachine.FeedbackPose getPufferPhysicsPose(class_1306 arm, float partialTicks) {
        return this.pufferPhysicsState.getPose(arm, partialTicks);
    }

    public boolean hasSlimeBonePhysics(class_1799 stack) {
        if (PunchyConfig.disableResourcePackModelParts() || stack == null || stack.method_7960()) {
            return false;
        }
        BedrockItemDefinition def = this.resolveDefinition(stack);
        if (def != null && def.hasSlimeBonePhysics()) {
            return true;
        }
        JsonElement tuningBonePhysics = ToolTuning.getSpecificBonePhysics(stack);
        if (tuningBonePhysics == null) {
            return false;
        }
        Map<String, BedrockItemDefinition.BonePhysicsSpec> parsed = BedrockItemDefinition.parseBonePhysics(tuningBonePhysics);
        for (BedrockItemDefinition.BonePhysicsSpec spec : parsed.values()) {
            if (spec == null || !spec.enableSlimePhysics()) continue;
            return true;
        }
        return false;
    }

    public boolean tryRenderItem(class_1799 stack, class_746 player, class_1306 arm, class_4587 poseStack, class_4597 buffer, int packedLight, float partialTicks, boolean applyDisplay) {
        if (PunchyConfig.disableResourcePackModelParts()) {
            return false;
        }
        if (stack == null || stack.method_7960() || player == null || arm == null) {
            return false;
        }
        if (this.isBlockedByCustomName(stack)) {
            return false;
        }
        BedrockItemDefinition def = this.resolveDefinition(stack);
        if (def == null) {
            return false;
        }
        return BedrockItemRenderer.render(def, stack, player, arm, poseStack, buffer, packedLight, partialTicks, applyDisplay);
    }

    public boolean hasDefinition(class_1799 stack) {
        return this.resolveDefinition(stack) != null;
    }

    BedrockItemDefinition resolveDefinition(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return null;
        }
        BedrockItemDefinition fallback = null;
        boolean hasNamedMatch = false;
        for (BedrockItemDefinition def : this.definitions) {
            if (def == null || !BedrockItemSystem.matchesItemList(stack, def.getItems())) continue;
            if (def.hasCustomName()) {
                hasNamedMatch = true;
                if (!def.matchesCustomName(stack)) continue;
                return def;
            }
            if (fallback != null) continue;
            fallback = def;
        }
        if (hasNamedMatch) {
            return null;
        }
        return fallback;
    }

    static boolean matchesItemList(class_1799 stack, List<String> items) {
        if (stack == null || stack.method_7960() || items == null || items.isEmpty()) {
            return false;
        }
        class_2960 itemId = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (itemId == null) {
            return false;
        }
        String current = itemId.toString();
        for (String entry : items) {
            class_2960 parsed;
            class_6862 tagKey;
            class_2960 tagId;
            String trimmed;
            if (entry == null || entry.isBlank() || !((trimmed = entry.trim()).startsWith("#") ? (tagId = class_2960.method_12829((String)trimmed.substring(1))) != null && stack.method_31573(tagKey = class_6862.method_40092((class_5321)class_7924.field_41197, (class_2960)tagId)) : (parsed = class_2960.method_12829((String)trimmed)) != null && current.equals(parsed.toString()))) continue;
            return true;
        }
        return false;
    }

    boolean isBlockedByCustomName(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        boolean namedExists = false;
        for (BedrockItemDefinition def : this.definitions) {
            if (def == null || !BedrockItemSystem.matchesItemList(stack, def.getItems()) || !def.hasCustomName()) continue;
            namedExists = true;
            if (!def.matchesCustomName(stack)) continue;
            return false;
        }
        return namedExists;
    }
}


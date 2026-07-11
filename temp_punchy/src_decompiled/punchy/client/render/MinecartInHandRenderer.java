/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1657
 *  net.minecraft.class_1688
 *  net.minecraft.class_1688$class_1689
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1808
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_7833
 *  net.minecraft.class_7923
 *  net.minecraft.class_824
 *  net.minecraft.class_827
 *  net.minecraft.class_898
 */
package punchy.client.render;

import java.util.EnumMap;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1688;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1808;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_7923;
import net.minecraft.class_824;
import net.minecraft.class_827;
import net.minecraft.class_898;
import punchy.client.render.ChestProxyTuning;
import punchy.client.render.InHandFallPhysics;
import punchy.client.render.InHandLidPhysics;
import punchy.client.render.MinecartInHandContext;
import punchy.compat.EmfEtfCemCompat;
import punchy.mixin.client.accessor.MinecartItemAccessor;

public final class MinecartInHandRenderer {
    private static final EnumMap<class_1268, MinecartCache> CACHE = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, Float> CHEST_LID_OPEN = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, Long> CHEST_LID_LAST_UPDATE = new EnumMap(class_1268.class);
    private static final float SCALE = 0.65f;
    private static final float ROT_Y = (float)Math.PI;

    private MinecartInHandRenderer() {
    }

    public static boolean isMinecartStack(class_1799 stack) {
        if (stack == null || stack.method_7960() || !(stack.method_7909() instanceof class_1808)) {
            return false;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        return id != null && "minecraft".equals(id.method_12836());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean tryRender(class_1799 stack, class_1268 hand, class_4587 matrices, class_4597 buffer, int light) {
        class_898 dispatcher;
        class_1792 class_17922 = stack.method_7909();
        if (!(class_17922 instanceof class_1808)) {
            return false;
        }
        class_1808 minecartItem = (class_1808)class_17922;
        class_2960 id = class_7923.field_41178.method_10221((Object)minecartItem);
        if (id == null || !"minecraft".equals(id.method_12836())) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1687 == null) {
            return false;
        }
        MinecartItemAccessor accessor = (MinecartItemAccessor)minecartItem;
        class_1688.class_1689 type = accessor.punchy$getType();
        MinecartCache cache = CACHE.computeIfAbsent(hand, h -> new MinecartCache());
        class_1688 minecart = cache.resolve(minecartItem, stack, client);
        if (minecart == null) {
            return false;
        }
        double vy = 0.0;
        if (client.field_1724 != null) {
            vy = client.field_1724.method_18798().field_1351;
            minecart.method_18800(0.0, vy, 0.0);
        }
        if ((dispatcher = client.method_1561()) == null) {
            return false;
        }
        float partialTicks = client.method_60646().method_60637(true);
        float fallOffset = InHandFallPhysics.update(client.field_1724, hand) * 0.3f;
        float tntOffset = MinecartInHandRenderer.isTntMinecart(minecartItem) ? fallOffset * 1.7f + 0.0625f : 0.0f;
        MinecartInHandContext.push();
        try {
            MinecartInHandContext.setVy(vy);
            MinecartInHandContext.setTntOffset(tntOffset);
            MinecartInHandContext.setType(type);
            matrices.method_22903();
            if (fallOffset != 0.0f) {
                matrices.method_46416(0.0f, fallOffset, 0.0f);
            }
            matrices.method_22905(0.65f, 0.65f, 0.65f);
            matrices.method_22907(class_7833.field_40716.rotation((float)Math.PI));
            dispatcher.method_3954((class_1297)minecart, 0.0, 0.0, 0.0, minecart.method_36454(), partialTicks, matrices, buffer, light);
            if (type == class_1688.class_1689.field_7678) {
                MinecartInHandRenderer.renderChestInMinecart(hand, matrices, buffer, client, light);
            }
            matrices.method_22909();
        }
        finally {
            MinecartInHandContext.pop();
        }
        return true;
    }

    private static class_1688 createMinecart(class_1808 minecartItem, class_310 client) {
        if (minecartItem == null || client == null || client.field_1687 == null) {
            return null;
        }
        MinecartItemAccessor accessor = (MinecartItemAccessor)minecartItem;
        class_1688.class_1689 type = accessor.punchy$getType();
        class_1299<? extends class_1688> entityType = MinecartInHandRenderer.resolveEntityType(type);
        if (entityType == null) {
            return null;
        }
        class_1688 minecart = (class_1688)entityType.method_5883((class_1937)client.field_1687);
        if (minecart != null) {
            minecart.method_5814(0.0, 0.0, 0.0);
            minecart.method_36456(180.0f);
        }
        return minecart;
    }

    private static boolean isTntMinecart(class_1808 minecartItem) {
        if (minecartItem == null) {
            return false;
        }
        MinecartItemAccessor accessor = (MinecartItemAccessor)minecartItem;
        return accessor.punchy$getType() == class_1688.class_1689.field_7675;
    }

    private static class_1299<? extends class_1688> resolveEntityType(class_1688.class_1689 type) {
        if (type == null) {
            return class_1299.field_6096;
        }
        return switch (type) {
            default -> throw new MatchException(null, null);
            case class_1688.class_1689.field_7678 -> class_1299.field_6126;
            case class_1688.class_1689.field_7679 -> class_1299.field_6080;
            case class_1688.class_1689.field_7675 -> class_1299.field_6053;
            case class_1688.class_1689.field_7680 -> class_1299.field_6142;
            case class_1688.class_1689.field_7677 -> class_1299.field_6058;
            case class_1688.class_1689.field_7681 -> class_1299.field_6136;
            case class_1688.class_1689.field_7674 -> class_1299.field_6096;
        };
    }

    private static void renderChestInMinecart(class_1268 hand, class_4587 matrices, class_4597 buffer, class_310 client, int light) {
        float open;
        if (client == null || client.field_1687 == null) {
            return;
        }
        class_824 dispatcher = client.method_31975();
        if (dispatcher == null) {
            return;
        }
        class_2680 state = class_2246.field_10034.method_9564();
        PunchyChestBlockEntity chest = new PunchyChestBlockEntity(class_2338.field_10980, state);
        chest.method_31662((class_1937)client.field_1687);
        class_827 renderer = dispatcher.method_3550((class_2586)chest);
        if (renderer == null) {
            return;
        }
        ChestProxyTuning.Profile profile = ChestProxyTuning.get(ChestProxyTuning.Kind.MINECART);
        if (profile == null) {
            return;
        }
        boolean disablePhysics = EmfEtfCemCompat.disableProxyPhysics(EmfEtfCemCompat.ProxyPhysicsTarget.CHEST);
        if (disablePhysics) {
            open = 0.0f;
        } else {
            float target = InHandLidPhysics.computeFallTarget((class_1657)client.field_1724);
            open = InHandLidPhysics.updateLid(CHEST_LID_OPEN, CHEST_LID_LAST_UPDATE, hand, target);
        }
        chest.punchy$setOpen(open);
        matrices.method_22903();
        matrices.method_46416(profile.offX, profile.offY, profile.offZ);
        matrices.method_22907(class_7833.field_40714.rotationDegrees(profile.rotX));
        matrices.method_22907(class_7833.field_40716.rotationDegrees(profile.rotY));
        matrices.method_22907(class_7833.field_40718.rotationDegrees(profile.rotZ));
        matrices.method_22905(profile.scale, profile.scale, profile.scale);
        renderer.method_3569((class_2586)chest, 0.0f, matrices, buffer, light, class_4608.field_21444);
        matrices.method_22909();
    }

    private static final class MinecartCache {
        private class_1799 lastStack;
        private class_1688 minecart;

        private MinecartCache() {
        }

        class_1688 resolve(class_1808 minecartItem, class_1799 stack, class_310 client) {
            if (this.minecart == null || this.lastStack == null || this.lastStack.method_7909() != stack.method_7909()) {
                this.lastStack = stack;
                this.minecart = MinecartInHandRenderer.createMinecart(minecartItem, client);
            }
            return this.minecart;
        }
    }

    private static final class PunchyChestBlockEntity
    extends class_2595 {
        private float punchy$open;

        private PunchyChestBlockEntity(class_2338 pos, class_2680 state) {
            super(pos, state);
        }

        private void punchy$setOpen(float open) {
            this.punchy$open = open;
        }

        public float method_11274(float partialTicks) {
            return this.punchy$open;
        }
    }
}


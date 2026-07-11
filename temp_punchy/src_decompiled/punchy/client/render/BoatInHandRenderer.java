/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1690
 *  net.minecraft.class_1690$class_1692
 *  net.minecraft.class_1749
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
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

import java.lang.reflect.Method;
import java.util.EnumMap;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1690;
import net.minecraft.class_1749;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
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
import punchy.client.render.BoatInHandContext;
import punchy.client.render.BoatLidState;
import punchy.client.render.ChestProxyTuning;
import punchy.client.render.InHandFallPhysics;
import punchy.client.render.InHandLidPhysics;
import punchy.compat.EmfEtfCemCompat;
import punchy.mixin.client.accessor.BoatItemAccessor;
import punchy.platform.Services;

public final class BoatInHandRenderer {
    private static final String BERYL_MOD_ID = "beryl";
    private static final EnumMap<class_1268, BoatCache> CACHE = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, Float> CHEST_LID_OPEN = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, Long> CHEST_LID_LAST_UPDATE = new EnumMap(class_1268.class);
    private static final float SCALE = 0.6f;
    private static final float ROT_Y = (float)Math.PI;

    private BoatInHandRenderer() {
    }

    public static boolean isBoatStack(class_1799 stack) {
        return stack != null && !stack.method_7960() && stack.method_7909() instanceof class_1749;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean tryRender(class_1799 stack, class_1268 hand, class_4587 matrices, class_4597 buffer, int light) {
        class_898 dispatcher;
        class_1792 class_17922 = stack.method_7909();
        if (!(class_17922 instanceof class_1749)) {
            return false;
        }
        class_1749 boatItem = (class_1749)class_17922;
        if (BoatInHandRenderer.shouldUseVanillaBoatRender(stack)) {
            return false;
        }
        BoatItemAccessor accessor = (BoatItemAccessor)boatItem;
        boolean hasChest = accessor.punchy$hasChest();
        boolean disablePhysics = false;
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1687 == null) {
            return false;
        }
        BoatCache cache = CACHE.computeIfAbsent(hand, h -> new BoatCache());
        class_1690 boat = cache.resolve(boatItem, stack, client);
        if (boat == null) {
            return false;
        }
        if (client.field_1724 != null) {
            double vy = disablePhysics ? 0.0 : client.field_1724.method_18798().field_1351;
            boat.method_18800(0.0, vy, 0.0);
        }
        BoatInHandRenderer.stabilizeBoat(boat, disablePhysics);
        if (boat instanceof BoatLidState) {
            BoatLidState lidState = (BoatLidState)boat;
            lidState.punchy$setInHand(true);
            lidState.punchy$setVy(boat.method_18798().field_1351);
            lidState.punchy$setChestBoat(hasChest);
            lidState.punchy$setLidApplied(false);
        }
        if ((dispatcher = client.method_1561()) == null) {
            return false;
        }
        float partialTicks = client.method_60646().method_60637(true);
        float fallOffset = disablePhysics ? 0.0f : InHandFallPhysics.update(client.field_1724, hand) * 0.3f;
        matrices.method_22903();
        if (fallOffset != 0.0f) {
            matrices.method_46416(0.0f, fallOffset, 0.0f);
        }
        matrices.method_22905(0.6f, 0.6f, 0.6f);
        matrices.method_22907(class_7833.field_40716.rotation((float)Math.PI));
        BoatInHandContext.push();
        try {
            dispatcher.method_3954((class_1297)boat, 0.0, 0.0, 0.0, boat.method_36454(), partialTicks, matrices, buffer, light);
            if (hasChest) {
                ChestProxyTuning.Kind chestKind = BoatInHandRenderer.resolveChestProxyKind(stack);
                BoatInHandRenderer.renderChestInBoat(hand, boat, matrices, buffer, client, chestKind, light, disablePhysics);
            }
        }
        finally {
            BoatInHandContext.pop();
        }
        matrices.method_22909();
        return true;
    }

    private static class_1690 createBoat(class_1749 boatItem, class_310 client) {
        if (boatItem == null || client == null || client.field_1687 == null) {
            return null;
        }
        BoatItemAccessor accessor = (BoatItemAccessor)boatItem;
        class_1690.class_1692 type = accessor.punchy$getType();
        if (type == null) {
            return null;
        }
        class_1690 boat = new class_1690((class_1937)client.field_1687, 0.0, 0.0, 0.0);
        boat.method_47884(type);
        boat.method_36456(180.0f);
        return boat;
    }

    private static void renderChestInBoat(class_1268 hand, class_1690 boat, class_4587 matrices, class_4597 buffer, class_310 client, ChestProxyTuning.Kind chestKind, int light, boolean disablePhysics) {
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
        ChestProxyTuning.Profile profile = ChestProxyTuning.get(chestKind != null ? chestKind : ChestProxyTuning.Kind.BOAT);
        if (profile == null) {
            return;
        }
        boolean disableProxyPhysics = EmfEtfCemCompat.disableProxyPhysics(EmfEtfCemCompat.ProxyPhysicsTarget.CHEST);
        float target = disablePhysics || disableProxyPhysics ? 0.0f : InHandLidPhysics.computeFallTarget((class_1657)client.field_1724);
        float open = disablePhysics || disableProxyPhysics ? 0.0f : InHandLidPhysics.updateLid(CHEST_LID_OPEN, CHEST_LID_LAST_UPDATE, hand, target);
        chest.punchy$setOpen(open);
        matrices.method_22903();
        BoatInHandRenderer.applyBoatTransforms(boat, matrices, false);
        if (!disablePhysics) {
            BoatInHandRenderer.applyChestPose(boat, matrices);
        }
        matrices.method_46416(profile.offX, profile.offY, profile.offZ);
        matrices.method_22907(class_7833.field_40714.rotationDegrees(profile.rotX));
        matrices.method_22907(class_7833.field_40716.rotationDegrees(profile.rotY));
        matrices.method_22907(class_7833.field_40718.rotationDegrees(profile.rotZ));
        matrices.method_22905(profile.scale, profile.scale, profile.scale);
        renderer.method_3569((class_2586)chest, 0.0f, matrices, buffer, light, class_4608.field_21444);
        matrices.method_22909();
    }

    private static ChestProxyTuning.Kind resolveChestProxyKind(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return ChestProxyTuning.Kind.BOAT;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (id == null) {
            return ChestProxyTuning.Kind.BOAT;
        }
        String path = id.method_12832();
        if (path.endsWith("_chest_raft")) {
            return ChestProxyTuning.Kind.RAFT;
        }
        return ChestProxyTuning.Kind.BOAT;
    }

    private static void applyBoatTransforms(class_1690 boat, class_4587 matrices, boolean flip) {
        float yRot = boat != null ? boat.method_36454() : 0.0f;
        matrices.method_46416(0.0f, 0.375f, 0.0f);
        matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0f - yRot));
        if (flip) {
            matrices.method_22905(-1.0f, -1.0f, 1.0f);
        }
        matrices.method_22907(class_7833.field_40716.rotationDegrees(90.0f));
    }

    private static void applyChestPose(class_1690 boat, class_4587 matrices) {
        BoatLidState lidState;
        if (!(boat instanceof BoatLidState) || !(lidState = (BoatLidState)boat).punchy$isChestPoseValid()) {
            return;
        }
        float x = lidState.punchy$getChestX() / 16.0f;
        float y = lidState.punchy$getChestY() / 16.0f;
        float z = lidState.punchy$getChestZ() / 16.0f;
        matrices.method_46416(x, y, z);
        matrices.method_22907(class_7833.field_40714.rotation(lidState.punchy$getChestRotX()));
        matrices.method_22907(class_7833.field_40716.rotation(lidState.punchy$getChestRotY()));
        matrices.method_22907(class_7833.field_40718.rotation(lidState.punchy$getChestRotZ()));
    }

    private static void stabilizeBoat(class_1690 boat, boolean disablePhysics) {
        if (boat == null) {
            return;
        }
        boat.field_5982 = boat.method_36454();
        boat.field_6004 = boat.method_36455();
        if (disablePhysics) {
            Method method3;
            try {
                method3 = boat.getClass().getMethod("setPaddleState", Boolean.TYPE, Boolean.TYPE);
                method3.invoke((Object)boat, false, false);
            }
            catch (Throwable method2) {
                // empty catch block
            }
            try {
                method3 = boat.getClass().getMethod("setHurtTime", Integer.TYPE);
                method3.invoke((Object)boat, 0);
            }
            catch (Throwable method3) {
                // empty catch block
            }
            try {
                method3 = boat.getClass().getMethod("setDamage", Float.TYPE);
                method3.invoke((Object)boat, Float.valueOf(0.0f));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private static boolean shouldUseVanillaBoatRender(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        if (Services.PLATFORM == null || !Services.PLATFORM.isModLoaded(BERYL_MOD_ID)) {
            return false;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (id == null) {
            return false;
        }
        String path = id.method_12832();
        return path.endsWith("_boat") || path.endsWith("_chest_boat");
    }

    private static final class BoatCache {
        private class_1799 lastStack;
        private class_1690 lastBoat;

        private BoatCache() {
        }

        class_1690 resolve(class_1749 boatItem, class_1799 stack, class_310 client) {
            if (this.lastBoat == null || this.lastStack == null || this.lastStack.method_7909() != stack.method_7909()) {
                this.lastStack = stack;
                this.lastBoat = BoatInHandRenderer.createBoat(boatItem, client);
            }
            return this.lastBoat;
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


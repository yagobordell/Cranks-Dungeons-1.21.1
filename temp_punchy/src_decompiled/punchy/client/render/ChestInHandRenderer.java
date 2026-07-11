/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2336
 *  net.minecraft.class_2338
 *  net.minecraft.class_2343
 *  net.minecraft.class_2531
 *  net.minecraft.class_2586
 *  net.minecraft.class_2595
 *  net.minecraft.class_2611
 *  net.minecraft.class_2646
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_4739
 *  net.minecraft.class_638
 *  net.minecraft.class_824
 *  net.minecraft.class_827
 */
package punchy.client.render;

import java.util.EnumMap;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2343;
import net.minecraft.class_2531;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2611;
import net.minecraft.class_2646;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_4739;
import net.minecraft.class_638;
import net.minecraft.class_824;
import net.minecraft.class_827;
import punchy.client.render.ChestInHandContext;
import punchy.client.render.InHandLidPhysics;
import punchy.compat.EmfEtfCemCompat;

public final class ChestInHandRenderer {
    private static final class_2338 ORIGIN = class_2338.field_10980;
    private static final EnumMap<class_1268, Float> LID_OPEN = new EnumMap(class_1268.class);
    private static final EnumMap<class_1268, Long> LID_LAST_UPDATE = new EnumMap(class_1268.class);

    private ChestInHandRenderer() {
    }

    public static boolean isChestStack(class_1799 stack) {
        return ChestInHandRenderer.isChest(stack);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean tryRender(class_1799 stack, class_1268 hand, class_4587 matrices, class_4597 buffer, int light) {
        float open;
        boolean disablePhysics;
        class_824 dispatcher;
        if (!ChestInHandRenderer.isChest(stack)) {
            return false;
        }
        class_2680 state = ChestInHandRenderer.chestState(stack);
        if (state == null) {
            return false;
        }
        class_2586 blockEntity = ChestInHandRenderer.createBlockEntity(state);
        if (blockEntity == null) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client == null) {
            return false;
        }
        class_638 level = client.field_1687;
        if (level != null) {
            blockEntity.method_31662((class_1937)level);
        }
        if ((dispatcher = client.method_31975()) == null) {
            return false;
        }
        class_827 renderer = dispatcher.method_3550(blockEntity);
        if (renderer == null) {
            return false;
        }
        EmfEtfCemCompat.ProxyPhysicsTarget proxyTarget = EmfEtfCemCompat.resolveChestTarget(state.method_26204());
        boolean bl = disablePhysics = proxyTarget != null && EmfEtfCemCompat.disableProxyPhysics(proxyTarget);
        if (disablePhysics) {
            open = 0.0f;
        } else {
            float target = InHandLidPhysics.computeFallTarget((class_1657)client.field_1724);
            open = InHandLidPhysics.updateLid(LID_OPEN, LID_LAST_UPDATE, hand, target);
        }
        if (blockEntity instanceof PunchyChestBlockEntity) {
            PunchyChestBlockEntity chest = (PunchyChestBlockEntity)blockEntity;
            chest.punchy$setOpen(open);
        } else if (blockEntity instanceof PunchyTrappedChestBlockEntity) {
            PunchyTrappedChestBlockEntity trapped = (PunchyTrappedChestBlockEntity)blockEntity;
            trapped.punchy$setOpen(open);
        } else if (blockEntity instanceof PunchyEnderChestBlockEntity) {
            PunchyEnderChestBlockEntity ender = (PunchyEnderChestBlockEntity)blockEntity;
            ender.punchy$setOpen(open);
        }
        ChestInHandContext.push();
        try {
            renderer.method_3569(blockEntity, 0.0f, matrices, buffer, light, class_4608.field_21444);
        }
        finally {
            ChestInHandContext.pop();
        }
        return true;
    }

    private static boolean isChest(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_1792 class_17922 = stack.method_7909();
        if (class_17922 instanceof class_1747) {
            class_1747 blockItem = (class_1747)class_17922;
            class_2248 block = blockItem.method_7711();
            return block instanceof class_4739 || block instanceof class_2336 || block instanceof class_2531;
        }
        return false;
    }

    private static class_2680 chestState(class_1799 stack) {
        class_1792 class_17922 = stack.method_7909();
        if (!(class_17922 instanceof class_1747)) {
            return null;
        }
        class_1747 blockItem = (class_1747)class_17922;
        class_2248 block = blockItem.method_7711();
        return block.method_9564();
    }

    private static class_2586 createBlockEntity(class_2680 state) {
        class_2248 block = state.method_26204();
        if (block == class_2246.field_10443) {
            return new PunchyEnderChestBlockEntity(ORIGIN, state);
        }
        if (block == class_2246.field_10380) {
            return new PunchyTrappedChestBlockEntity(ORIGIN, state);
        }
        if (block == class_2246.field_10034) {
            return new PunchyChestBlockEntity(ORIGIN, state);
        }
        if (block instanceof class_2343) {
            class_2343 entityBlock = (class_2343)block;
            return entityBlock.method_10123(ORIGIN, state);
        }
        return null;
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

    private static final class PunchyTrappedChestBlockEntity
    extends class_2646 {
        private float punchy$open;

        private PunchyTrappedChestBlockEntity(class_2338 pos, class_2680 state) {
            super(pos, state);
        }

        private void punchy$setOpen(float open) {
            this.punchy$open = open;
        }

        public float method_11274(float partialTicks) {
            return this.punchy$open;
        }
    }

    private static final class PunchyEnderChestBlockEntity
    extends class_2611 {
        private float punchy$open;

        private PunchyEnderChestBlockEntity(class_2338 pos, class_2680 state) {
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


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1688$class_1689
 */
package punchy.client.render;

import net.minecraft.class_1688;

public final class MinecartInHandContext {
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private MinecartInHandContext() {
    }

    public static void push() {
        ++MinecartInHandContext.STATE.get().depth;
    }

    public static void pop() {
        State state = STATE.get();
        state.depth = Math.max(0, state.depth - 1);
        if (state.depth == 0) {
            state.vy = 0.0;
            state.tntOffset = 0.0f;
            state.type = null;
        }
    }

    public static boolean isActive() {
        return MinecartInHandContext.STATE.get().depth > 0;
    }

    public static void setVy(double vy) {
        MinecartInHandContext.STATE.get().vy = vy;
    }

    public static double getVy() {
        return MinecartInHandContext.STATE.get().vy;
    }

    public static void setTntOffset(float offset) {
        MinecartInHandContext.STATE.get().tntOffset = offset;
    }

    public static float getTntOffset() {
        return MinecartInHandContext.STATE.get().tntOffset;
    }

    public static void setType(class_1688.class_1689 type) {
        MinecartInHandContext.STATE.get().type = type;
    }

    public static class_1688.class_1689 getType() {
        return MinecartInHandContext.STATE.get().type;
    }

    private static final class State {
        int depth;
        double vy;
        float tntOffset;
        class_1688.class_1689 type;

        private State() {
        }
    }
}


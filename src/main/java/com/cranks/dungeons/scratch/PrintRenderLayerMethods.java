package com.cranks.dungeons.scratch;

public class PrintRenderLayerMethods {
    public static void print() {
        for (java.lang.reflect.Method m : net.minecraft.client.render.RenderLayer.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("energy") || m.getName().toLowerCase().contains("swirl")) {
                System.out.println("RENDER_LAYER_METHOD: " + m.getName() + " params: " + m.getParameterCount());
                for (Class<?> p : m.getParameterTypes()) {
                    System.out.println("  Param: " + p.getName());
                }
            }
        }
    }
}

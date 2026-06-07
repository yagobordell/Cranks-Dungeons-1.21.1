package com.cranks.dungeons.scratch;

import java.io.FileWriter;
import java.io.IOException;

public class RenderLayerDumper {
    static {
        try (FileWriter writer = new FileWriter("render_layer_methods.txt")) {
            writer.write("Methods in RenderLayer:\n");
            for (java.lang.reflect.Method m : net.minecraft.client.render.RenderLayer.class.getDeclaredMethods()) {
                writer.write(m.getName() + "(");
                Class<?>[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    writer.write(params[i].getSimpleName());
                    if (i < params.length - 1) writer.write(", ");
                }
                writer.write(")\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Just a dummy method to trigger the static block
    public static void dump() {}
}

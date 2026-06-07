package com.cranks.dungeons.scratch;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PrintRenderLayers {
    public static void print() {
        for (Method m : RenderLayer.class.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == RenderLayer.class) {
                System.out.println(m.getName() + "(" + m.getParameterCount() + " params)");
            }
        }
    }
}

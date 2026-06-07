package com.cranks.dungeons.scratch;

import net.minecraft.client.render.VertexConsumerProvider;
import java.lang.reflect.Method;

public class PrintImmediate {
    public static void print() {
        for (Method m : VertexConsumerProvider.Immediate.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + m.getParameterCount());
        }
    }
}

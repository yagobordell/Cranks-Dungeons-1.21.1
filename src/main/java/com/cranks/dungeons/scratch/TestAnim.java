package com.cranks.dungeons.scratch;

import net.minecraft.entity.AnimationState;
import java.lang.reflect.Method;

public class TestAnim {
    public static void dump() {
        for (Method m : AnimationState.class.getDeclaredMethods()) {
            System.out.println(m.getName());
        }
    }
}

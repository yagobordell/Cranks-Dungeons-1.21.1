package com.cranks.dungeons.client;

import net.minecraft.client.option.KeyBinding;

public class TestCategory {
    public static void main(String[] args) {
        System.out.println("=== CATEGORY METHODS ===");
        try {
            for (java.lang.reflect.Method m : KeyBinding.Category.class.getDeclaredMethods()) {
                System.out.println(m.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

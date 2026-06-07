package com.cranks.dungeons.scratch;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class PrintEntityMethods {
    public static void print() {
        for (java.lang.reflect.Method m : Entity.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("freeze") || m.getName().toLowerCase().contains("frost")) {
                System.out.println("ENTITY_METHOD: " + m.getName() + " " + m.getReturnType().getName());
            }
        }
        for (java.lang.reflect.Method m : LivingEntity.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("freeze") || m.getName().toLowerCase().contains("frost")) {
                System.out.println("LIVING_METHOD: " + m.getName() + " " + m.getReturnType().getName());
            }
        }
    }
}

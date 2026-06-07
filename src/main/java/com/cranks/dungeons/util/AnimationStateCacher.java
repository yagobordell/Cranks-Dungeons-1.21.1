package com.cranks.dungeons.util;

import net.minecraft.entity.AnimationState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationStateCacher {
    private static final Map<Class<?>, List<Field>> CACHE = new HashMap<>();

    public static List<Field> getAnimFields(Class<?> clazz) {
        return CACHE.computeIfAbsent(clazz, c -> {
            List<Field> fields = new ArrayList<>();
            // We need to check all declared fields in the class hierarchy up to Object
            Class<?> current = c;
            while (current != null && current != Object.class) {
                for (Field f : current.getDeclaredFields()) {
                    if (f.getType() == AnimationState.class) {
                        f.setAccessible(true);
                        fields.add(f);
                    }
                }
                current = current.getSuperclass();
            }
            return fields;
        });
    }
}

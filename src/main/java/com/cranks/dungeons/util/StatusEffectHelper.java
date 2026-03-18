package com.cranks.dungeons.util;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Optional;

public class StatusEffectHelper {

    private static Constructor<StatusEffectInstance> instanceConstructor;
    private static Constructor<?> paramsConstructor;
    private static boolean initAttempted = false;

    public static StatusEffectInstance create(RegistryEntry<StatusEffect> effect, int duration, int amplifier,
                                             boolean ambient, boolean showParticles, boolean showIcon) {
        if (!initAttempted) {
            initAttempted = true;
            try {
                Class<?> paramsClass = null;
                for (Class<?> declared : StatusEffectInstance.class.getDeclaredClasses()) {
                    if (declared.getName().contains("Parameters")) {
                        paramsClass = declared;
                        break;
                    }
                }
                
                if (paramsClass != null) {
                    RecordComponent[] components = paramsClass.getRecordComponents();
                    Class<?>[] paramTypes = new Class<?>[components.length];
                    for (int i = 0; i < components.length; i++) {
                        paramTypes[i] = components[i].getType();
                    }
                    paramsConstructor = paramsClass.getDeclaredConstructor(paramTypes);
                    paramsConstructor.setAccessible(true);
                }
                
                // Try the 6-arg constructor: RegistryEntry, int, int, boolean, boolean, boolean
                Constructor<?>[] constructors = StatusEffectInstance.class.getDeclaredConstructors();
                for (Constructor<?> c : constructors) {
                    Class<?>[] params = c.getParameterTypes();
                    if (params.length == 6 &&
                        params[1] == int.class &&
                        params[2] == int.class &&
                        params[3] == boolean.class &&
                        params[4] == boolean.class &&
                        params[5] == boolean.class) {
                        instanceConstructor = (Constructor<StatusEffectInstance>) c;
                        instanceConstructor.setAccessible(true);
                        CranksDungeons.LOGGER.info("StatusEffectHelper: Found 6-arg constructor!");
                        break;
                    }
                }
                
                if (instanceConstructor == null) {
                    // Try the 2-arg constructor with Parameters
                    for (Constructor<?> c : constructors) {
                        Class<?>[] params = c.getParameterTypes();
                        if (params.length == 2 && params[1].getName().contains("Parameters")) {
                            instanceConstructor = (Constructor<StatusEffectInstance>) c;
                            instanceConstructor.setAccessible(true);
                            CranksDungeons.LOGGER.info("StatusEffectHelper: Found 2-arg constructor!");
                            break;
                        }
                    }
                }
                
            } catch (Exception e) {
                CranksDungeons.LOGGER.error("StatusEffectHelper init FAILED: " + e.getMessage());
            }
        }
        
        if (instanceConstructor == null) {
            return null;
        }
        
        try {
            Class<?>[] params = instanceConstructor.getParameterTypes();
            
            if (params.length == 6) {
                // 6-arg constructor: RegistryEntry, duration, amplifier, ambient, showParticles, showIcon
                return instanceConstructor.newInstance(effect, duration, amplifier, ambient, showParticles, showIcon);
            } else if (params.length == 2 && paramsConstructor != null) {
                // 2-arg constructor: RegistryEntry, Parameters
                Object parameters = paramsConstructor.newInstance(
                        amplifier, duration, ambient, showParticles, showIcon, Optional.empty());
                return instanceConstructor.newInstance(effect, parameters);
            }
            
            return null;
        } catch (Exception e) {
            CranksDungeons.LOGGER.error("StatusEffectHelper: Failed to create: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

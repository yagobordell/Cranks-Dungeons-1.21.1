package com.cranks.dungeons.client.render;

import net.minecraft.client.model.ModelPart;
import java.lang.reflect.Method;
import java.util.Optional;

/** Optional EMF root lookup shared by model attachments and ailment coats. */
final class AnimatedModelParts {
    private static final ClassValue<Optional<Method>> ROOT_METHOD=new ClassValue<>() {
        @Override protected Optional<Method> computeValue(Class<?> type) {
            try {return Optional.of(type.getMethod("getRoot"));}
            catch(NoSuchMethodException e) {return Optional.empty();}
        }
    };
    private AnimatedModelParts() {}
    static ModelPart root(ModelPart seed) {
        if(seed==null || !seed.getClass().getName().startsWith("traben.entity_model_features."))return null;
        try {
            var method=ROOT_METHOD.get(seed.getClass());
            return method.isPresent()?(ModelPart)method.get().invoke(seed):seed;
        } catch(ReflectiveOperationException ignored) {return null;}
    }
}

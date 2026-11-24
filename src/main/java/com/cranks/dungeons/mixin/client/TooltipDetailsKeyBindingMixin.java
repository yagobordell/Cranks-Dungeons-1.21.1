package com.cranks.dungeons.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/** Details observes physical input; it must not replace Sneak or other gameplay bindings. */
@Mixin(KeyBinding.class)
abstract class TooltipDetailsKeyBindingMixin {
    @WrapOperation(method={"<init>", "updateKeysByCode"}, at=@At(value="INVOKE",
            target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap=false))
    private static Object cranks$passiveDetailsBinding(Map<Object,Object> bindings, Object key, Object value,
                                                       Operation<Object> original) {
        if (key instanceof InputUtil.Key && value instanceof KeyBinding binding
                && binding.getTranslationKey().equals("key.cranks-dungeons.show_details")) return bindings.get(key);
        return original.call(bindings, key, value);
    }
}

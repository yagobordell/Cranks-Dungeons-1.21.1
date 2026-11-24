package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.SupportedContent;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class SupportedItemStackMixin {
    @Shadow @Final @Mutable private Item item;
    @Shadow @Final @Mutable private ComponentMapImpl components;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void validateCreatedStack(CallbackInfo ci) { cranks$validateContent(); }

    @Inject(method = {"set", "remove",
            "apply(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;",
            "apply(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;Ljava/util/function/UnaryOperator;)Ljava/lang/Object;"}, at = @At("RETURN"))
    private void validateComponent(CallbackInfoReturnable<Object> cir) { cranks$validateContent(); }

    @Inject(method = {"applyChanges", "applyUnvalidatedChanges", "applyComponentsFrom", "setCount"}, at = @At("RETURN"))
    private void validateChanges(CallbackInfo ci) { cranks$validateContent(); }

    @Unique private void cranks$validateContent() {
        if (components == null) return;
        if (!(item instanceof net.minecraft.item.PotionItem) && !(item instanceof net.minecraft.item.ArrowItem)) {
            var regular = components.get(net.minecraft.component.DataComponentTypes.ENCHANTMENTS);
            var stored = components.get(net.minecraft.component.DataComponentTypes.STORED_ENCHANTMENTS);
            if ((regular == null || regular.isEmpty()) && (stored == null || stored.isEmpty())) return;
        }
        if (!SupportedContent.beginNormalization()) return;
        try {
            var stack = (ItemStack) (Object) this;
            if (stack.isEmpty()) return;
            var normalized = SupportedContent.normalize(stack);
            if (normalized != stack) {
                item = normalized.getItem();
                components = ComponentMapImpl.create(normalized.getDefaultComponents(), normalized.getComponentChanges());
            }
            // A stack may be assembled in several constructor/setter steps. Keep its component
            // baseline tied to the final item, so crafted and decoded equivalents stack together.
            if (item instanceof net.minecraft.item.PotionItem || item instanceof net.minecraft.item.ArrowItem) {
                components = ComponentMapImpl.create(item.getComponents(), components.getChanges());
            }
        } finally { SupportedContent.endNormalization(); }
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.FletchingArrows;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(TippedArrowItem.class)
abstract class FletchingArrowTooltipMixin {
    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void arrowTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        var kind = FletchingArrows.kind(stack);
        if (kind == null) return;
        tooltip.add(Text.translatable("arrow.cranks-dungeons." + kind.id + ".desc").formatted(Formatting.GRAY));
        if (kind == FletchingArrows.Kind.VOID) tooltip.add(Text.translatable("arrow.cranks-dungeons.void.extra").formatted(Formatting.DARK_GRAY));
        ci.cancel();
    }
}

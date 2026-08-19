package com.cranks.dungeons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentLevelMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private static void hideLevelNumeral(RegistryEntry<Enchantment> enchantment, int level, CallbackInfoReturnable<Text> cir) {
        MutableText text = enchantment.value().description().copy();
        if (enchantment.isIn(EnchantmentTags.CURSE)) {
            text.formatted(Formatting.RED);
        } else {
            text.formatted(Formatting.GRAY);
        }
        cir.setReturnValue(text);
    }
}

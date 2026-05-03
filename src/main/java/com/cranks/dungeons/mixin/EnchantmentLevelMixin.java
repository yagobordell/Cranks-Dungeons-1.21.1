package com.cranks.dungeons.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentLevelMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private static void hideLevelNumeral(RegistryEntry<Enchantment> enchantment, int level, CallbackInfoReturnable<Text> cir) {
        // If max level is 1 in the definition, vanilla already hides it.
        // But the user wants to hide it even if it's Level 3 (highest level kept).
        // Since we only have one tier of each enchantment now, we just always return the base name.
        cir.setReturnValue(enchantment.value().description());
    }
}

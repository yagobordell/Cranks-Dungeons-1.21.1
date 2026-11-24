package com.cranks.dungeons.mixin.client;

import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla recipe-book unlocks do not represent the station discovery rules. */
@Mixin(RecipeToast.class)
public class RecipeToastMixin {
    @Inject(method = "show", at = @At("HEAD"), cancellable = true)
    private static void suppressRecipeBookNotification(ToastManager manager, RecipeEntry<?> recipe, CallbackInfo ci) { ci.cancel(); }
}

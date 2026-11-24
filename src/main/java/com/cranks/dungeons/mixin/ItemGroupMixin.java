package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;
import com.cranks.dungeons.inventory.SupportedContent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ItemGroup.class)
public class ItemGroupMixin {
    @WrapMethod(method = "updateEntries")
    private void buildEntriesBeforeFiltering(ItemGroup.DisplayContext context, Operation<Void> original) {
        SupportedContent.beginCreativeEntries();
        try { original.call(context); }
        finally { SupportedContent.endCreativeEntries(); }
    }

    @Shadow private Collection<ItemStack> displayStacks;
    @Shadow private Set<ItemStack> searchTabStacks;

    @Inject(method = "updateEntries", at = @At("RETURN"))
    private void filterCreativeEntries(ItemGroup.DisplayContext displayContext, CallbackInfo ci) {
        if (this.displayStacks != null) {
            this.displayStacks.removeIf(stack -> !SupportedContent.isSupported(stack) || CranksDungeons.shouldRemoveFromCreative(stack));
        }
        if (this.searchTabStacks != null) {
            this.searchTabStacks.removeIf(stack -> !SupportedContent.isSupported(stack) || CranksDungeons.shouldRemoveFromCreative(stack));
        }
    }
}

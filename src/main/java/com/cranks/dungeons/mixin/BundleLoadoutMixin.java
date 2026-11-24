package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(BundleItem.class)
abstract class BundleLoadoutMixin extends Item {
    protected BundleLoadoutMixin(Settings settings) {
        super(settings);
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return FeatureFlags.DEFAULT_ENABLED_FEATURES;
    }

    @Inject(method = {"onStackClicked", "onClicked", "isItemBarVisible"}, at = @At("HEAD"), cancellable = true)
    private void disableVanillaStorage(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void hideBundlePreview(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void explainBag(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        BundleContentsComponent legacy = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        tooltip.add(Text.translatable(legacy.isEmpty() ? "inventory.cranks-dungeons.bundle_capacity"
                : "inventory.cranks-dungeons.bundle_legacy").formatted(Formatting.GRAY));
        ci.cancel();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void emptyLegacyBundle(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = player.getStackInHand(hand);
        BundleContentsComponent contents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        if (contents.isEmpty()) {
            cir.setReturnValue(TypedActionResult.pass(stack));
            return;
        }
        if (!world.isClient) {
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
            for (ItemStack saved : contents.iterate()) {
                ItemStack remaining = saved.copy();
                player.getInventory().insertStack(remaining);
                if (!remaining.isEmpty()) player.dropItem(remaining, false);
            }
            player.getInventory().markDirty();
        }
        cir.setReturnValue(TypedActionResult.success(stack, world.isClient));
    }
}

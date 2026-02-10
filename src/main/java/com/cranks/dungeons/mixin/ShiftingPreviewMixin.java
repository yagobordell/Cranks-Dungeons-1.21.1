package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ShiftingPreviewMixin {

    @Unique
    private boolean wasShowingMessage = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void showEnchantmentDisabledWarning(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        boolean isSneaking = player.isSneaking();
        String disabledEnchant = null;

        if (isSneaking) {
            ItemStack stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                var enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
                if (enchants != null) {
                    boolean hasSilk = enchants.getEnchantments().stream()
                            .anyMatch(e -> e.getKey().get().getValue().toString().equals("minecraft:silk_touch"));
                    boolean hasVein = enchants.getEnchantments().stream()
                            .anyMatch(e -> e.getKey().get().getValue().toString().equals("cranks-dungeons:vein_mining"));

                    if (hasSilk && hasVein) disabledEnchant = "Silk Touch & Vein Mining";
                    else if (hasSilk) disabledEnchant = "Silk Touch";
                    else if (hasVein) disabledEnchant = "Vein Mining";
                }
            }
        }

        if (disabledEnchant != null) {
            serverPlayer.sendMessage(
                    Text.literal("⚠ " + disabledEnchant + " Disabled (Sneaking) ⚠")
                            .formatted(Formatting.RED, Formatting.BOLD),
                    true
            );
            wasShowingMessage = true;
        } else if (wasShowingMessage) {
            serverPlayer.sendMessage(Text.empty(), true);
            wasShowingMessage = false;
        }
    }
}
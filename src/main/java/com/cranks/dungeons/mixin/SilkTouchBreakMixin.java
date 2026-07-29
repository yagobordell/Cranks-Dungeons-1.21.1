package com.cranks.dungeons.mixin;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class SilkTouchBreakMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Unique
    private ItemEnchantmentsComponent cranks$savedEnchants;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void cranks$beforeBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cranks$savedEnchants = null;

        Identifier silkTouchId = Identifier.of("minecraft", "silk_touch");
        if (!EnchantmentDisableManager.isDisabled(player.getUuid(), silkTouchId)) return;

        ItemStack tool = player.getMainHandStack();
        if (tool.isEmpty()) return;

        ItemEnchantmentsComponent enchants = tool.getOrDefault(
                DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        RegistryEntry<Enchantment> silkTouchEntry = player.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(Enchantments.SILK_TOUCH).get();

        if (EnchantmentHelper.getLevel(silkTouchEntry, tool) <= 0) return;

        // Save original enchantments
        cranks$savedEnchants = enchants;

        // Rebuild enchantments without Silk Touch
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchants);
        builder.set(silkTouchEntry, 0); // setting to 0 removes it
        tool.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void cranks$afterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cranks$savedEnchants != null) {
            ItemStack tool = player.getMainHandStack();
            // Only restore if tool still exists (didn't break during mining)
            if (!tool.isEmpty()) {
                tool.set(DataComponentTypes.ENCHANTMENTS, cranks$savedEnchants);
            }
            cranks$savedEnchants = null;
        }
    }
}
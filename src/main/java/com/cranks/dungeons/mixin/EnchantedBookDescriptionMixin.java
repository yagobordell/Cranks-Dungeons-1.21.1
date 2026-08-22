package com.cranks.dungeons.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 1500)
public class EnchantedBookDescriptionMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void addEnchantedBookDescriptions(Item.TooltipContext context,
                                              net.minecraft.entity.player.PlayerEntity player,
                                              TooltipType type,
                                              CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isOf(Items.ENCHANTED_BOOK)) {
            return;
        }


        List<Text> tooltip = cir.getReturnValue();

        var enchantments = stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS,
                net.minecraft.component.type.ItemEnchantmentsComponent.DEFAULT);



        for (var entry : enchantments.getEnchantmentEntries()) {
            String enchantId = com.cranks.dungeons.CranksDungeons.getEnchantmentId(entry.getKey());
            if (enchantId.isEmpty()) continue;
            String enchantName = enchantId.contains(":") ? enchantId.split(":")[1] : enchantId;

            if (isCustomEnchantment(enchantName)) {
                String descKey1 = "enchantment.cranks-dungeons." + enchantName + ".desc";
                String descKey2 = "enchantment.cranks-dungeons." + enchantName + ".desc2";
                String displayName = entry.getKey().value().description().getString();

                for (int i = 1; i < tooltip.size(); i++) {
                    String line = tooltip.get(i).getString();

                    if (line.equals(displayName) || line.startsWith(displayName + " ") || line.toLowerCase().contains(displayName.toLowerCase())) {
                        int offset = 1;
                        tooltip.add(i + offset++, Text.empty());
                        
                        Text description1 = Text.translatable(descKey1).formatted(Formatting.GRAY, Formatting.ITALIC);
                        tooltip.add(i + offset++, description1);

                        try {
                            Text description2 = Text.translatable(descKey2).formatted(Formatting.GRAY, Formatting.ITALIC);
                            if (!description2.getString().equals(descKey2)) {
                                tooltip.add(i + offset++, description2);
                            }
                        } catch (Exception e) {
                        }

                        break;
                    }
                }
            }
        }
    }

    private boolean isCustomEnchantment(String name) {
        return name.equals("silk_touch") ||
                name.equals("vein_mining") ||
                name.equals("adept") ||
                name.equals("greater_adept") ||
                name.equals("perfect_adept") ||
                name.equals("body") ||
                name.equals("greater_body") ||
                name.equals("perfect_body") ||
                name.equals("desert") ||
                name.equals("greater_desert") ||
                name.equals("perfect_desert") ||
                name.equals("glacial") ||
                name.equals("greater_glacial") ||
                name.equals("perfect_glacial") ||
                name.equals("inspiration") ||
                name.equals("greater_inspiration") ||
                name.equals("perfect_inspiration") ||
                name.equals("iron") ||
                name.equals("greater_iron") ||
                name.equals("perfect_iron") ||
                name.equals("stone") ||
                name.equals("greater_stone") ||
                name.equals("perfect_stone") ||
                name.equals("storm") ||
                name.equals("greater_storm") ||
                name.equals("perfect_storm") ||
                name.equals("vision") ||
                name.equals("greater_vision") ||
                name.equals("perfect_vision");
    }
}
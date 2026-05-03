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
            String enchantId = entry.getKey().getKey().get().getValue().toString();
            String enchantName = enchantId.contains(":") ? enchantId.split(":")[1] : enchantId;

            if (isCustomEnchantment(enchantName)) {
                String descKey1 = "enchantment.cranks-dungeons." + enchantName + ".desc";
                String descKey2 = "enchantment.cranks-dungeons." + enchantName + ".desc2";
                String displayName = entry.getKey().value().description().getString();

                for (int i = 1; i < tooltip.size(); i++) {
                    String line = tooltip.get(i).getString();

                    if (line.equals(displayName)) {
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
        return name.equals("lunge") ||
                name.equals("riptide") ||
                name.equals("channeling") ||
                name.equals("infinity") ||
                name.equals("multishot") ||
                name.equals("silk_touch") ||
                name.equals("frost_walker") ||
                name.equals("loyalty") ||
                name.equals("depth_strider") ||
                name.equals("respiration") ||
                name.equals("soul_speed") ||
                name.equals("aqua_affinity") ||
                name.equals("wind_burst") ||
                name.equals("vein_mining") ||
                name.equals("body") ||
                name.equals("greater_body") ||
                name.equals("desert") ||
                name.equals("greater_desert") ||
                name.equals("glacial") ||
                name.equals("greater_glacial") ||
                name.equals("storm") ||
                name.equals("greater_storm") ||
                name.equals("iron") ||
                name.equals("greater_iron") ||
                name.equals("vision") ||
                name.equals("greater_vision") ||
                name.equals("alacrity") ||
                name.equals("greater_alacrity") ||
                name.equals("sundering") ||
                name.equals("greater_sundering") ||
                name.equals("avarice") ||
                name.equals("greater_avarice") ||
                name.equals("preservation") ||
                name.equals("greater_preservation") ||
                name.equals("reach") ||
                name.equals("greater_reach");
    }
}
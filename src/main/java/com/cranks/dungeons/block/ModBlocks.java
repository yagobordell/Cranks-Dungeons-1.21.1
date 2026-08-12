package com.cranks.dungeons.block;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModBlocks {
    public static Block RUNIC_ENHANCEMENT_ALTAR;
    public static Block ARTIFICIAL_ENHANCEMENT_ALTAR;

    public static void register() {
        // Runic Enhancement Altar
        Identifier runicId = Identifier.of(CranksDungeons.MOD_ID, "runic_enhancement_altar");
        RegistryKey<Block> runicBlockKey = RegistryKey.of(RegistryKeys.BLOCK, runicId);

        AbstractBlock.Settings runicBlockSettings = AbstractBlock.Settings.create()
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque();

        RUNIC_ENHANCEMENT_ALTAR = Registry.register(
                Registries.BLOCK,
                runicId,
                new RunicEnhancementAltarBlock(runicBlockSettings)
        );

        RegistryKey<Item> runicItemKey = RegistryKey.of(RegistryKeys.ITEM, runicId);
        Registry.register(Registries.ITEM, runicItemKey,
                new BlockItem(RUNIC_ENHANCEMENT_ALTAR, new Item.Settings()) {
                    @Override
                    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
                        tooltip.add(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
                        super.appendTooltip(stack, context, tooltip, type);
                    }
                }
        );

        // Artificial Enhancement Altar
        Identifier artificialId = Identifier.of(CranksDungeons.MOD_ID, "artificial_enhancement_altar");

        AbstractBlock.Settings artificialBlockSettings = AbstractBlock.Settings.create()
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque();

        ARTIFICIAL_ENHANCEMENT_ALTAR = Registry.register(
                Registries.BLOCK,
                artificialId,
                new ArtificialEnhancementAltarBlock(artificialBlockSettings)
        );

        Registry.register(Registries.ITEM, artificialId,
                new BlockItem(ARTIFICIAL_ENHANCEMENT_ALTAR, new Item.Settings()) {
                    @Override
                    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
                        tooltip.add(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
                        super.appendTooltip(stack, context, tooltip, type);
                    }
                }
        );
    }
}
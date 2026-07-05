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
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static Block RUNIC_ENHANCEMENT_ALTAR;
    public static Block ARTIFICIAL_ENHANCEMENT_ALTAR;
    public static Block QUALITY_STATION;

    public static void register() {
        // Runic Enhancement Altar
        Identifier runicId = Identifier.of(CranksDungeons.MOD_ID, "runic_enhancement_altar");
        RegistryKey<Block> runicBlockKey = RegistryKey.of(RegistryKeys.BLOCK, runicId);

        AbstractBlock.Settings runicBlockSettings = AbstractBlock.Settings.create()
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque()
                .registryKey(runicBlockKey);

        RUNIC_ENHANCEMENT_ALTAR = Registry.register(
                Registries.BLOCK,
                runicId,
                new RunicEnhancementAltarBlock(runicBlockSettings)
        );

        RegistryKey<Item> runicItemKey = RegistryKey.of(RegistryKeys.ITEM, runicId);
        Registry.register(
                Registries.ITEM,
                runicId,
                new BlockItem(RUNIC_ENHANCEMENT_ALTAR, new Item.Settings().registryKey(runicItemKey)) {
                    @Override
                    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, java.util.function.Consumer<net.minecraft.text.Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {
                        textConsumer.accept(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
                        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
                    }
                }
        );

        // Artificial Enhancement Altar
        Identifier artificialId = Identifier.of(CranksDungeons.MOD_ID, "artificial_enhancement_altar");
        RegistryKey<Block> artificialBlockKey = RegistryKey.of(RegistryKeys.BLOCK, artificialId);

        AbstractBlock.Settings artificialBlockSettings = AbstractBlock.Settings.create()
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque()
                .registryKey(artificialBlockKey);

        ARTIFICIAL_ENHANCEMENT_ALTAR = Registry.register(
                Registries.BLOCK,
                artificialId,
                new ArtificialEnhancementAltarBlock(artificialBlockSettings)
        );

        RegistryKey<Item> artificialItemKey = RegistryKey.of(RegistryKeys.ITEM, artificialId);
        Registry.register(
                Registries.ITEM,
                artificialId,
                new BlockItem(ARTIFICIAL_ENHANCEMENT_ALTAR, new Item.Settings().registryKey(artificialItemKey)) {
                    @Override
                    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, java.util.function.Consumer<net.minecraft.text.Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {
                        textConsumer.accept(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
                        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
                    }
                }
        );

        // Quality Station
        Identifier qualityId = Identifier.of(CranksDungeons.MOD_ID, "quality_station");
        RegistryKey<Block> qualityBlockKey = RegistryKey.of(RegistryKeys.BLOCK, qualityId);

        AbstractBlock.Settings qualityBlockSettings = AbstractBlock.Settings.create()
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque()
                .registryKey(qualityBlockKey);

        QUALITY_STATION = Registry.register(
                Registries.BLOCK,
                qualityId,
                new QualityStationBlock(qualityBlockSettings)
        );

        RegistryKey<Item> qualityItemKey = RegistryKey.of(RegistryKeys.ITEM, qualityId);
        Registry.register(
                Registries.ITEM,
                qualityId,
                new BlockItem(QUALITY_STATION, new Item.Settings().registryKey(qualityItemKey)) {
                    @Override
                    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, java.util.function.Consumer<net.minecraft.text.Text> textConsumer, net.minecraft.item.tooltip.TooltipType type) {
                        textConsumer.accept(net.minecraft.text.Text.translatable(this.getTranslationKey() + ".desc").formatted(net.minecraft.util.Formatting.GRAY));
                        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
                    }
                }
        );
    }
}
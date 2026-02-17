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
                new BlockItem(RUNIC_ENHANCEMENT_ALTAR, new Item.Settings().registryKey(runicItemKey))
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
                new BlockItem(ARTIFICIAL_ENHANCEMENT_ALTAR, new Item.Settings().registryKey(artificialItemKey))
        );
    }
}
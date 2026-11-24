package com.cranks.dungeons.registry;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModBlockTags {

    public static final String MOD_ID = ModAttributes.MOD_ID; // "cranks-dungeons"

    public static final TagKey<Block> LOG_FORTUNE_LOGS = TagKey.of(
            RegistryKeys.BLOCK, Identifier.of(MOD_ID, "log_fortune_logs")
    );

    public static final TagKey<Block> DIG_FORTUNE_BLOCKS = TagKey.of(
            RegistryKeys.BLOCK, Identifier.of(MOD_ID, "dig_fortune_blocks")
    );

    // NEW: all ores affected by your custom ore fortune
    public static final TagKey<Block> ORE_FORTUNE_ORES = TagKey.of(
            RegistryKeys.BLOCK, Identifier.of(MOD_ID, "ore_fortune_ores")
    );

    public static boolean isOreFamily(TagKey<Block> tag) {
        Identifier id = tag.id();
        return (id.getNamespace().equals("minecraft") && id.getPath().endsWith("_ores"))
                || (id.getNamespace().equals("c") && id.getPath().startsWith("ores/"));
    }

    private ModBlockTags() {}
}
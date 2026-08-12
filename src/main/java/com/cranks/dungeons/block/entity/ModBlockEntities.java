package com.cranks.dungeons.block.entity;

import com.cranks.dungeons.CranksDungeons;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<RunicEnhancementAltarBlockEntity> RUNIC_ALTAR_BE;
    public static BlockEntityType<ArtificialEnhancementAltarBlockEntity> ARTIFICIAL_ALTAR_BE;
    public static BlockEntityType<QualityStationBlockEntity> QUALITY_STATION_BE;

    public static void register() {
        RUNIC_ALTAR_BE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(CranksDungeons.MOD_ID, "runic_altar_be"),
                FabricBlockEntityTypeBuilder.create(
                        RunicEnhancementAltarBlockEntity::new,
                        Registries.BLOCK.get(Identifier.of(CranksDungeons.MOD_ID, "runic_enhancement_altar"))
                ).build()
        );

        ARTIFICIAL_ALTAR_BE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(CranksDungeons.MOD_ID, "artificial_altar_be"),
                FabricBlockEntityTypeBuilder.create(
                        ArtificialEnhancementAltarBlockEntity::new,
                        Registries.BLOCK.get(Identifier.of(CranksDungeons.MOD_ID, "artificial_enhancement_altar"))
                ).build()
        );

        QUALITY_STATION_BE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(CranksDungeons.MOD_ID, "quality_station_be"),
                FabricBlockEntityTypeBuilder.create(
                        QualityStationBlockEntity::new,
                        Blocks.SMITHING_TABLE
                ).build()
        );
    }
}
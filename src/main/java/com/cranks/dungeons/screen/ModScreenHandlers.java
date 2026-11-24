package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public class ModScreenHandlers {
    public static ScreenHandlerType<LoadoutCrafterHandler> LOADOUT_CRAFTER;
    public static ScreenHandlerType<LoadoutStorageHandler> LOADOUT_STORAGE_3, LOADOUT_STORAGE_6, LOADOUT_SHULKER, LOADOUT_HOPPER, LOADOUT_DISPENSER;
    public static ScreenHandlerType<RunicEnhancementAltarScreenHandler> RUNIC_ENHANCEMENT_ALTAR;
    public static ScreenHandlerType<ArtificialEnhancementAltarScreenHandler> ARTIFICIAL_ENHANCEMENT_ALTAR;
    public static ScreenHandlerType<QualityStationScreenHandler> QUALITY_STATION_SCREEN_HANDLER;
    public static ScreenHandlerType<MountCargoScreenHandler> MOUNT_CARGO;
    public static ScreenHandlerType<LoadoutCraftingTableHandler> LOADOUT_CRAFTING_TABLE;
    public static ScreenHandlerType<LoadoutCuttingBenchHandler> LOADOUT_CUTTING_BENCH;
    public static ScreenHandlerType<LoadoutAnvilHandler> LOADOUT_ANVIL;
    public static ScreenHandlerType<LoadoutGrindstoneHandler> LOADOUT_GRINDSTONE;
    public static ScreenHandlerType<LoadoutEnchantingHandler> LOADOUT_ENCHANTING;
    public static ScreenHandlerType<LoadoutAlchemyHandler> LOADOUT_ALCHEMY;
    public static ScreenHandlerType<LoadoutFurnaceHandler> LOADOUT_FURNACE;
    public static ScreenHandlerType<LoadoutFurnaceHandler> LOADOUT_BLAST_FURNACE;
    public static ScreenHandlerType<LoadoutFurnaceHandler> LOADOUT_SMOKER;

    public static ScreenHandlerType<LoadoutFletchingHandler> LOADOUT_FLETCHING;
    public static ScreenHandlerType<LoadoutTailoringHandler> LOADOUT_TAILORING;
    public static ScreenHandlerType<LoadoutCartographyHandler> LOADOUT_CARTOGRAPHY;

    public static void register() {
        LOADOUT_CRAFTER = Registry.register(Registries.SCREEN_HANDLER,Identifier.of(CranksDungeons.MOD_ID,"loadout_crafter"),new ScreenHandlerType<>(LoadoutCrafterHandler::new,FeatureFlags.VANILLA_FEATURES));
        LOADOUT_STORAGE_3 = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID,"loadout_storage_3"),new ScreenHandlerType<>((id,inventory)->new LoadoutStorageHandler(id,inventory,3,false),FeatureFlags.VANILLA_FEATURES));
        LOADOUT_STORAGE_6 = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID,"loadout_storage_6"),new ScreenHandlerType<>((id,inventory)->new LoadoutStorageHandler(id,inventory,6,false),FeatureFlags.VANILLA_FEATURES));
        LOADOUT_SHULKER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID,"loadout_shulker"),new ScreenHandlerType<>((id,inventory)->new LoadoutStorageHandler(id,inventory,3,true),FeatureFlags.VANILLA_FEATURES));
        LOADOUT_HOPPER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID,"loadout_hopper"),new ScreenHandlerType<>((id,inventory)->new LoadoutStorageHandler(id,inventory,5,1,false),FeatureFlags.VANILLA_FEATURES));
        LOADOUT_DISPENSER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID,"loadout_dispenser"),new ScreenHandlerType<>((id,inventory)->new LoadoutStorageHandler(id,inventory,3,3,false),FeatureFlags.VANILLA_FEATURES));
        LOADOUT_CARTOGRAPHY = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_cartography"),
                new ScreenHandlerType<>(LoadoutCartographyHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_TAILORING = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_tailoring"),
                new ScreenHandlerType<>(LoadoutTailoringHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_FLETCHING = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_fletching"),
                new ScreenHandlerType<>(LoadoutFletchingHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_ALCHEMY = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_alchemy"),
                new ScreenHandlerType<>(LoadoutAlchemyHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_CRAFTING_TABLE = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_crafting_table"),
                new ScreenHandlerType<>(LoadoutCraftingTableHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_CUTTING_BENCH = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_cutting_bench"),
                new ScreenHandlerType<>(LoadoutCuttingBenchHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_ANVIL = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_anvil"),
                new ScreenHandlerType<>(LoadoutAnvilHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_GRINDSTONE = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_grindstone"),
                new ScreenHandlerType<>(LoadoutGrindstoneHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_ENCHANTING = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_enchanting"),
                new ScreenHandlerType<>(LoadoutEnchantingHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_FURNACE = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_furnace"),
                new ScreenHandlerType<>(LoadoutFurnaceHandler::new, FeatureFlags.VANILLA_FEATURES));
        LOADOUT_BLAST_FURNACE = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_blast_furnace"),
                new ScreenHandlerType<>((id, inventory) -> new LoadoutFurnaceHandler(id, inventory, LoadoutFurnaceHandler.Station.BLAST_FURNACE),
                        FeatureFlags.VANILLA_FEATURES));
        LOADOUT_SMOKER = Registry.register(
                Registries.SCREEN_HANDLER, Identifier.of(CranksDungeons.MOD_ID, "loadout_smoker"),
                new ScreenHandlerType<>((id, inventory) -> new LoadoutFurnaceHandler(id, inventory, LoadoutFurnaceHandler.Station.SMOKER),
                        FeatureFlags.VANILLA_FEATURES));

        RUNIC_ENHANCEMENT_ALTAR = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(CranksDungeons.MOD_ID, "runic_enhancement_altar_gui"),
                new ScreenHandlerType<>(RunicEnhancementAltarScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        ARTIFICIAL_ENHANCEMENT_ALTAR = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(CranksDungeons.MOD_ID, "artificial_enhancement_altar_gui"),
                new ScreenHandlerType<>(ArtificialEnhancementAltarScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        QUALITY_STATION_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(CranksDungeons.MOD_ID, "quality_station_gui"),
                new ScreenHandlerType<>(QualityStationScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        MOUNT_CARGO = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(CranksDungeons.MOD_ID, "mount_cargo"),
                new ExtendedScreenHandlerType<>(MountCargoScreenHandler::new,
                        MountCargoScreenHandler.OpeningData.PACKET_CODEC)
        );
    }
}

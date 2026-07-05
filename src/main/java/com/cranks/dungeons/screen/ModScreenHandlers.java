package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static ScreenHandlerType<RunicEnhancementAltarScreenHandler> RUNIC_ENHANCEMENT_ALTAR;
    public static ScreenHandlerType<ArtificialEnhancementAltarScreenHandler> ARTIFICIAL_ENHANCEMENT_ALTAR;
    public static ScreenHandlerType<QualityStationScreenHandler> QUALITY_STATION_SCREEN_HANDLER;

    public static void register() {
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
    }
}
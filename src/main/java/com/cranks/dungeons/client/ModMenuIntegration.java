package com.cranks.dungeons.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> io.wispforest.owo.config.ui.ConfigScreen.create(com.cranks.dungeons.CranksDungeons.CONFIG, parent);
    }
}

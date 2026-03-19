package com.cranks.dungeons.client;

import com.cranks.dungeons.client.screen.ModConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Return a factory that creates our config screen. 
        // This class is only ever loaded if ModMenu is present.
        return parent -> new ModConfigScreen(parent);
    }
}

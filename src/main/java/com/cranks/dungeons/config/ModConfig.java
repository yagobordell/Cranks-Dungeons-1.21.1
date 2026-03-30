package com.cranks.dungeons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("cranks-dungeons.json").toFile();

    // Damage toggles
    public boolean showMyDealtDamage = true;
    public boolean showMyReceivedDamage = true;
    public boolean showOtherPlayerDamage = true;
    
    // Healing toggles
    public boolean showMyReceivedHealing = true; // "Self Healing" (includes life steal, regen)
    public boolean showOtherPlayerHealing = true; // "Other Healing" (enemies healing, other players healing)
    
    // Party HUD settings
    public boolean partyHudEnabled = true;
    public boolean partyHudShowSelf = true; // Set to false to hide yourself from party HUD
    public int partyHudX = -1; // -1 = default (middle-left)
    public int partyHudY = -1; // -1 = default (middle-left)

    private static ModConfig INSTANCE;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

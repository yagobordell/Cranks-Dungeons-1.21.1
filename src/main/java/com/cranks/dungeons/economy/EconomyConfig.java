package com.cranks.dungeons.economy;

import com.cranks.dungeons.config.ConfigFileUtil;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;

/** Server settings; amounts remain independent of experience and item-rarity bonuses. */
public final class EconomyConfig {
    public static EconomyConfig RULES = new EconomyConfig();
    public double coinsPerBaseXp = 0.6;
    public int minimumMobCoins = 1, maximumMobCoins = 10000;
    public int minimumChestCoins = 10, maximumChestCoins = 30;
    public double tradeDistance = 3;
    public int invitationSeconds = 30;
    public int declinedRequestSeconds = 30;
    public static void load() {
        var file = FabricLoader.getInstance().getConfigDir().resolve("cranks-dungeons/economy.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(file)) RULES = gson.fromJson(Files.readString(file), EconomyConfig.class);
            if (RULES == null || !Double.isFinite(RULES.coinsPerBaseXp) || RULES.coinsPerBaseXp < 0 || RULES.coinsPerBaseXp > 100
                    || RULES.minimumMobCoins < 0 || RULES.maximumMobCoins < RULES.minimumMobCoins || RULES.maximumMobCoins > 1000000
                    || RULES.minimumChestCoins < 0 || RULES.maximumChestCoins < RULES.minimumChestCoins || RULES.maximumChestCoins > 1000000
                    || !Double.isFinite(RULES.tradeDistance) || RULES.tradeDistance < 1 || RULES.tradeDistance > 6
                    || RULES.invitationSeconds < 5 || RULES.invitationSeconds > 120
                    || RULES.declinedRequestSeconds < 5 || RULES.declinedRequestSeconds > 300) throw new IllegalArgumentException("Economy settings out of range");
            if (!Files.exists(file)) ConfigFileUtil.atomicWrite(file, gson.toJson(RULES));
        } catch (Exception error) { throw new IllegalStateException("Invalid economy configuration: " + file, error); }
    }
}

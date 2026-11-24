package com.cranks.dungeons.config;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;

/** Server-owned progression rules, independent of editable tree size. Restart to reload. */
public final class ProgressionConfig {
    public static ProgressionConfig RULES = new ProgressionConfig();
    public int combatCap = 90, worldCap = 311;
    public double combatBase = 50, combatLinear = 5, combatQuadratic = .2;
    public double worldBase = 75, worldLinear = 6, worldQuadratic = .02;
    public int activityTicks = 600, shareRadius = 64;
    public int resourceXp = 60, recipeXp = 15, biomeXp = 150;
    public int structureXp = 300, firstStructureTypeXp = 300, firstAnimalXp = 75;
    public double minimumDamageFraction = .05;
    public static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("cranks-dungeons/progression.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                var value = gson.fromJson(Files.readString(path), ProgressionConfig.class);
                if (value == null) throw new IllegalArgumentException("Empty progression config");
                value.validate(); RULES = value;
            } else ConfigFileUtil.atomicWrite(path, gson.toJson(RULES));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid progression config: " + path, e);
        }
    }
    private void validate() {
        if (combatCap < 1 || combatCap > 10000 || worldCap < 1 || worldCap > 10000
                || activityTicks < 20 || activityTicks > 2400 || shareRadius < 8 || shareRadius > 128
                || !Double.isFinite(minimumDamageFraction) || minimumDamageFraction < .01 || minimumDamageFraction > 1)
            throw new IllegalArgumentException("Progression cap/activity settings out of range");
        for (double v : new double[]{combatBase,combatLinear,combatQuadratic,worldBase,worldLinear,worldQuadratic})
            if (!Double.isFinite(v) || v < 0 || v > 100000) throw new IllegalArgumentException("Invalid XP curve");
        for (int v : new int[]{resourceXp,recipeXp,biomeXp,structureXp,firstStructureTypeXp,firstAnimalXp})
            if (v < 0 || v > 100000) throw new IllegalArgumentException("Invalid discovery reward");
    }
    public int required(boolean combat, int level) {
        if (level >= (combat ? combatCap : worldCap)) return 0;
        double l = Math.max(0, level);
        return (int)Math.clamp((combat ? combatBase+combatLinear*l+combatQuadratic*l*l
                : worldBase+worldLinear*l+worldQuadratic*l*l), 1, 100000000);
    }
}

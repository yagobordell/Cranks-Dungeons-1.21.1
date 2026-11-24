package com.cranks.dungeons.alchemy;

import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Registry-independent checks; the live station transactions are also exercised in a running game. */
public final class AlchemyRulesRegression {
    private AlchemyRulesRegression() {}

    public static void run() {
        var first = new AlchemyRecipes.Experiment("spider_eye_extract", "stone_salt", "binding", true, AlchemyItems.Delivery.DRINK);
        var reversed = new AlchemyRecipes.Experiment("stone_salt", "spider_eye_extract", "binding", true, AlchemyItems.Delivery.DRINK);
        require(first.key().equals(reversed.key()), "Ingredient order changed the experiment identity");
        require(AlchemyRecipes.valid(first), "Valid two-extract experiment rejected");
        require(!AlchemyRecipes.valid(new AlchemyRecipes.Experiment("dirt", "", "binding", true, AlchemyItems.Delivery.DRINK)), "Arbitrary blocks accepted as extracts");
        require(!AlchemyRecipes.valid(new AlchemyRecipes.Experiment("spider_eye_extract", "", "unknown", true, AlchemyItems.Delivery.DRINK)), "Unknown suspension accepted");
        require(!first.key().equals(new AlchemyRecipes.Experiment("spider_eye_extract", "stone_salt", "binding", false, AlchemyItems.Delivery.DRINK).key()), "Tier was omitted from experiment identity");
        require(!first.key().equals(new AlchemyRecipes.Experiment("spider_eye_extract", "stone_salt", "binding", true, AlchemyItems.Delivery.SPLASH).key()), "Delivery was omitted from experiment identity");
        require(AlchemyRecipes.EXTRACTS.size() == 21, "Unexpected main-extract catalogue");

        for (String family : new String[]{"adept", "body", "desert", "glacial", "inspiration", "iron", "stone", "storm", "vision"}) {
            require(AlchemyRulesRegression.class.getResource("/data/minecraft/enchantment/" + family + ".json") != null, "Lesser family missing: " + family);
            require(AlchemyRulesRegression.class.getResource("/data/minecraft/enchantment/greater_" + family + ".json") != null, "Regular family missing: " + family);
            require(AlchemyRulesRegression.class.getResource("/data/minecraft/enchantment/perfect_" + family + ".json") == null, "Removed Perfect tier is still packaged: " + family);
        }
        value("adept", 0, 9); value("greater_adept", 0, 12);
        value("body", 1, 45); value("greater_body", 1, 60);
    }

    private static void value(String family, int index, double expected) {
        try (var stream = AlchemyRulesRegression.class.getResourceAsStream("/data/minecraft/enchantment/" + family + ".json");
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            double actual = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("effects")
                    .getAsJsonArray("minecraft:attributes").get(index).getAsJsonObject().getAsJsonObject("amount").get("base").getAsDouble();
            require(actual == expected, "Enchantment values changed when renaming " + family);
        } catch (java.io.IOException error) { throw new AssertionError(error); }
    }

    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}

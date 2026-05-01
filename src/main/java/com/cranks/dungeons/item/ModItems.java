package com.cranks.dungeons.item;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.item.ElementalTomeItem.Element;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item TOME_OF_TRANSMUTATION = registerRunicTome("transmutation_tome", RunicTomeItem.TomeType.TRANSMUTATION);
    public static final Item TOME_OF_AUGMENTATION = registerRunicTome("augmentation_tome", RunicTomeItem.TomeType.AUGMENTATION);
    public static final Item REGAL_TOME = registerRunicTome("regal_tome", RunicTomeItem.TomeType.REGAL);
    public static final Item TOME_OF_ALCHEMY = registerRunicTome("alchemy_tome", RunicTomeItem.TomeType.ALCHEMY);
    public static final Item EXALTED_TOME = registerRunicTome("exalted_tome", RunicTomeItem.TomeType.EXALTED);

    // Normal Elemental Tomes
    public static final Item TOME_OF_FIRE = registerElementalTome("fire_tome", Element.FIRE, false, CustomRarity.RARE);
    public static final Item TOME_OF_COLD = registerElementalTome("cold_tome", Element.COLD, false, CustomRarity.RARE);
    public static final Item TOME_OF_LIGHTNING = registerElementalTome("lightning_tome", Element.LIGHTNING, false, CustomRarity.RARE);
    public static final Item TOME_OF_VOID = registerElementalTome("void_tome", Element.VOID, false, CustomRarity.RARE);
    public static final Item TOME_OF_PHYSICALITY = registerElementalTome("physicality_tome", Element.PHYSICAL, false, CustomRarity.RARE);

    public static final Item TOME_OF_SWIFTNESS = registerSwiftnessTome("swiftness_tome", CustomRarity.RARE);
    public static final Item TOME_OF_ENERGY = registerEnergyTome("energy_tome", CustomRarity.RARE);
    public static final Item TOME_OF_EVASION = registerEvasionTome("evasion_tome", CustomRarity.RARE);
    public static final Item TOME_OF_HEALTH_BONUS = registerHealthBonusTome("health_bonus_tome", CustomRarity.RARE);
    public static final Item TOME_OF_HEALTH_REGEN = registerHealthRegenTome("health_regen_tome", CustomRarity.RARE);
    public static final Item TOME_OF_RANGE = registerRangeTome("range_tome", CustomRarity.RARE);


    // Greater Elemental Tomes
    public static final Item GREATER_TOME_OF_FIRE = registerElementalTome("greater_fire_tome", Element.FIRE, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_COLD = registerElementalTome("greater_cold_tome", Element.COLD, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_LIGHTNING = registerElementalTome("greater_lightning_tome", Element.LIGHTNING, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_VOID = registerElementalTome("greater_void_tome", Element.VOID, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_PHYSICALITY = registerElementalTome("greater_physicality_tome", Element.PHYSICAL, true, CustomRarity.UNIQUE);

    // Artificial Tome
    public static final Item ARTIFICIAL_TOME = registerArtificialTome("artificial_tome", CustomRarity.UNIQUE);

    private static Item registerRunicTome(String name, RunicTomeItem.TomeType type) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new RunicTomeItem(type, new Item.Settings().registryKey(key)));
    }

    private static Item registerSwiftnessTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new SwiftnessTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerEnergyTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key,
                new EnergyTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerEvasionTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key,
                new EvasionTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerElementalTome(String name, Element element, boolean isGreater, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new ElementalTomeItem(element, isGreater, rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerArtificialTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new ArtificialTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerHealthBonusTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key,
                new HealthBonusTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerHealthRegenTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key,
                new HealthRegenTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerRangeTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key,
                new RangeTomeItem(rarity, new Item.Settings().registryKey(key)));
    }

    public static void registerModItems() {
        CranksDungeons.LOGGER.info("Registering Mod Items for Cranks Dungeons: " + TOME_OF_TRANSMUTATION.getName().getString());
    }
}

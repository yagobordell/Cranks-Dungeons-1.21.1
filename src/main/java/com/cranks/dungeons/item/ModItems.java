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

    public static final Item RUNIC_TOME_T1 = registerRunicTome("runic_tome_t1", 1, CustomRarity.COMMON);
    public static final Item RUNIC_TOME_T2 = registerRunicTome("runic_tome_t2", 2, CustomRarity.COMMON);
    public static final Item RUNIC_TOME_T3 = registerRunicTome("runic_tome_t3", 3, CustomRarity.RARE);
    public static final Item RUNIC_TOME_T4 = registerRunicTome("runic_tome_t4", 4, CustomRarity.RARE);
    public static final Item RUNIC_TOME_T5 = registerRunicTome("runic_tome_t5", 5, CustomRarity.UNIQUE);

    // Normal Elemental Tomes
    public static final Item TOME_OF_FIRE = registerElementalTome("fire_tome", Element.FIRE, false, CustomRarity.RARE);
    public static final Item TOME_OF_COLD = registerElementalTome("cold_tome", Element.COLD, false, CustomRarity.RARE);
    public static final Item TOME_OF_LIGHTNING = registerElementalTome("lightning_tome", Element.LIGHTNING, false, CustomRarity.RARE);
    public static final Item TOME_OF_VOID = registerElementalTome("void_tome", Element.VOID, false, CustomRarity.RARE);
    public static final Item TOME_OF_PHYSICALITY = registerElementalTome("physicality_tome", Element.PHYSICAL, false, CustomRarity.RARE);

    // Swiftness Tome
    public static final Item TOME_OF_SWIFTNESS = registerSwiftnessTome("swiftness_tome", CustomRarity.RARE);

    // Greater Elemental Tomes
    public static final Item GREATER_TOME_OF_FIRE = registerElementalTome("greater_fire_tome", Element.FIRE, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_COLD = registerElementalTome("greater_cold_tome", Element.COLD, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_LIGHTNING = registerElementalTome("greater_lightning_tome", Element.LIGHTNING, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_VOID = registerElementalTome("greater_void_tome", Element.VOID, true, CustomRarity.UNIQUE);
    public static final Item GREATER_TOME_OF_PHYSICALITY = registerElementalTome("greater_physicality_tome", Element.PHYSICAL, true, CustomRarity.UNIQUE);

    // Artificial Tome
    public static final Item ARTIFICIAL_TOME = registerArtificialTome("artificial_tome", CustomRarity.UNIQUE);

    private static Item registerRunicTome(String name, int tier, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new RunicTomeItem(tier, rarity, new Item.Settings().registryKey(key)));
    }

    private static Item registerSwiftnessTome(String name, CustomRarity rarity) {
        Identifier id = Identifier.of(CranksDungeons.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        return Registry.register(Registries.ITEM, key,
                new SwiftnessTomeItem(rarity, new Item.Settings().registryKey(key)));
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

    public static void registerModItems() {
    }
}
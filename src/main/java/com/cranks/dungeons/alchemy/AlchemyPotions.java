package com.cranks.dungeons.alchemy;

import com.cranks.dungeons.effect.ModEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.*;

public final class AlchemyPotions {
    public record Formula(String family, boolean lesser, RegistryEntry<Potion> potion, List<String> extracts, String suspension) {
        public String key() { return (lesser ? "lesser_" : "") + family; }
        public ItemStack stack(AlchemyItems.Delivery delivery) {
            ItemStack stack = AlchemyItems.filled(lesser, delivery).getDefaultStack();
            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
            return stack;
        }
    }

    public static final List<Formula> FORMULAS = new ArrayList<>();
    private static final Map<String, Formula> BY_KEY = new LinkedHashMap<>();
    private static final List<RegistryEntry<Potion>> FAILURES = new ArrayList<>();
    private static final Set<String> VANILLA_STRENGTH_VARIANTS = Set.of("healing", "regeneration", "strength", "swiftness",
            "fire_resistance", "turtle_master", "leaping", "harming", "poison", "slowness");
    private AlchemyPotions() {}

    public static void register() {
        pair("healing", Potions.HEALING, Potions.STRONG_HEALING, "herbal", "melon_extract");
        pair("regeneration", Potions.REGENERATION, Potions.STRONG_REGENERATION, "herbal", "honey_extract");
        pair("strength", Potions.STRENGTH, Potions.STRONG_STRENGTH, "binding", "bone_salt");
        pair("swiftness", Potions.SWIFTNESS, Potions.STRONG_SWIFTNESS, "herbal", "sugar_extract");
        add("fire_resistance", effects(StatusEffects.FIRE_RESISTANCE, 2400, 0), effects(StatusEffects.FIRE_RESISTANCE, 1200, 1), "mineral", "heat_salt");
        pair("turtle_master", Potions.TURTLE_MASTER, Potions.STRONG_TURTLE_MASTER, "binding", "turtle_extract");
        pair("leaping", Potions.LEAPING, Potions.STRONG_LEAPING, "binding", "rabbit_foot_extract");
        utility("night_vision", Potions.NIGHT_VISION, "mineral", "golden_carrot_extract");
        utility("invisibility", Potions.INVISIBILITY, "herbal", "golden_carrot_extract", "fermented_eye_extract");
        utility("water_breathing", Potions.WATER_BREATHING, "herbal", "pufferfish_extract");
        utility("slow_falling", Potions.SLOW_FALLING, "herbal", "membrane_extract");
        pair("harming", Potions.HARMING, Potions.STRONG_HARMING, "mineral", "fermented_eye_extract");
        pair("poison", Potions.POISON, Potions.STRONG_POISON, "binding", "spider_eye_extract");
        utility("weakness", Potions.WEAKNESS, "herbal", "fermented_eye_extract");
        pair("slowness", Potions.SLOWNESS, Potions.STRONG_SLOWNESS, "mineral", "mushroom_extract");
        utility("luck", Potions.LUCK, "herbal", "rabbit_foot_extract", "lapis_salt");
        utility("wind_charged", Potions.WIND_CHARGED, "mineral", "wind_essence");
        utility("weaving", Potions.WEAVING, "binding", "thread_extract");
        utility("oozing", Potions.OOZING, "binding", "slime_extract");
        utility("infested", Potions.INFESTED, "binding", "spider_eye_extract", "stone_salt");
        add("cold_resistance", effects(ModEffects.COLD_RESISTANCE, 2400, 0), effects(ModEffects.COLD_RESISTANCE, 1200, 1), "mineral", "frost_salt");
        add("lightning_resistance", effects(ModEffects.LIGHTNING_RESISTANCE, 2400, 0), effects(ModEffects.LIGHTNING_RESISTANCE, 1200, 1), "binding", "copper_salt");
        add("void_resistance", effects(ModEffects.VOID_RESISTANCE, 2400, 0), effects(ModEffects.VOID_RESISTANCE, 1200, 1), "mineral", "amethyst_essence");
        FAILURES.add(register("queasy_concoction", effects(StatusEffects.NAUSEA, 160, 0)));
        FAILURES.add(register("sluggish_sludge", effects(StatusEffects.SLOWNESS, 60, 0)));
        FAILURES.add(register("bitter_swill", effects(StatusEffects.HUNGER, 200, 0)));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (player.age % 5 != 0) continue;
                var inventory = player.getInventory();
                boolean changed = false;
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack previous = inventory.getStack(i);
                    ItemStack canonical = canonicalize(previous);
                    if (canonical != previous) { inventory.setStack(i, canonical); changed = true; }
                }
                if (changed) { inventory.markDirty(); player.currentScreenHandler.sendContentUpdates(); }
            }
        });
    }

    private static List<StatusEffectInstance> effects(RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int ticks, int amplifier) {
        return List.of(new StatusEffectInstance(effect, ticks, amplifier));
    }

    private static void pair(String family, RegistryEntry<Potion> lesser, RegistryEntry<Potion> regular, String suspension, String... extracts) {
        add(family, lesser.value().getEffects(), regular.value().getEffects(), suspension, extracts);
    }

    private static void utility(String family, RegistryEntry<Potion> regular, String suspension, String... extracts) {
        var lesser = regular.value().getEffects().stream().map(effect -> new StatusEffectInstance(effect.getEffectType(),
                Math.max(1, effect.getDuration() / 2), effect.getAmplifier())).toList();
        add(family, lesser, regular.value().getEffects(), suspension, extracts);
    }

    private static void add(String family, List<StatusEffectInstance> lesser, List<StatusEffectInstance> regular, String suspension, String... extracts) {
        for (boolean low : new boolean[]{true, false}) {
            String key = (low ? "lesser_" : "") + family;
            Formula formula = new Formula(family, low, register(key, low ? lesser : regular), List.of(extracts), suspension);
            FORMULAS.add(formula);
            BY_KEY.put(key, formula);
        }
    }

    private static RegistryEntry<Potion> register(String key, List<StatusEffectInstance> effects) {
        return Registry.registerReference(Registries.POTION, AlchemyItems.id(key), new Potion(key, effects.toArray(StatusEffectInstance[]::new)));
    }

    public static Formula get(String key) { return BY_KEY.get(key); }

    /** Looted potions share bottle forms and stack identity with player-brewed equivalents. */
    public static ItemStack canonicalize(ItemStack stack) {
        var arrow = com.cranks.dungeons.inventory.FletchingArrows.canonicalize(stack);
        if (arrow != stack) return arrow;
        boolean vanilla = stack.getItem() instanceof net.minecraft.item.PotionItem;
        if (!vanilla && !(stack.getItem() instanceof AlchemyItems.Brew)) return stack;
        var brew = stack.getItem() instanceof AlchemyItems.Brew own ? own : null;
        var delivery = brew != null ? brew.delivery : stack.getItem() instanceof net.minecraft.item.SplashPotionItem ? AlchemyItems.Delivery.SPLASH
                : stack.getItem() instanceof net.minecraft.item.LingeringPotionItem ? AlchemyItems.Delivery.LINGERING : AlchemyItems.Delivery.DRINK;
        var contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        var id = contents.potion().flatMap(RegistryEntry::getKey).map(key -> key.getValue()).orElse(null);
        Formula formula = null;
        boolean lesser = brew != null && brew.lesser;
        if (id != null && id.getNamespace().equals("cranks-dungeons")) formula = get(id.getPath());
        else if (id != null && id.getNamespace().equals("minecraft")) {
            String family = id.getPath().replaceFirst("^(long_|strong_)", "");
            boolean legacyLesser = !id.getPath().startsWith("strong_") && VANILLA_STRENGTH_VARIANTS.contains(family);
            formula = get((legacyLesser ? "lesser_" : "") + family);
        }
        PotionContentsComponent canonical;
        boolean failure = isFailure(contents);
        if (failure) {
            int selected = FAILURES.indexOf(contents.potion().orElseThrow());
            int[] colors = {0x82763B, 0x777187, 0x778270};
            canonical = new PotionContentsComponent(contents.potion(), Optional.of(colors[selected]), List.of());
        } else {
            if (formula != null) lesser = formula.lesser();
            canonical = new PotionContentsComponent(formula == null ? Potions.WATER : formula.potion());
        }
        var item = AlchemyItems.filled(lesser, delivery);
        var model = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        boolean correctModel = failure ? model != null && model.value() == 1 : model == null;
        if (stack.isOf(item) && contents.equals(canonical) && correctModel) return stack;
        var converted = stack.copyComponentsToNewStack(item, stack.getCount());
        converted.set(DataComponentTypes.POTION_CONTENTS, canonical);
        if (failure) converted.set(DataComponentTypes.CUSTOM_MODEL_DATA, new net.minecraft.component.type.CustomModelDataComponent(1));
        else converted.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        return converted;
    }
    public static boolean isFailure(ItemStack stack) {
        return isFailure(stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT));
    }

    public static boolean isFailure(PotionContentsComponent contents) {
        return contents.potion()
                .map(potion -> FAILURES.contains(potion)).orElse(false);
    }

    public static boolean canApplyFailure(net.minecraft.entity.Entity owner, net.minecraft.entity.LivingEntity target) {
        if (!(owner instanceof net.minecraft.server.network.ServerPlayerEntity attacker)
                || !(target instanceof net.minecraft.server.network.ServerPlayerEntity victim) || attacker == victim) return true;
        return attacker.getServer().isPvpEnabled()
                && !com.cranks.dungeons.party.PartyManager.areInSameParty(attacker.getUuid(), victim.getUuid());
    }

    public static ItemStack failed(String combination, boolean lesser, AlchemyItems.Delivery delivery) {
        ItemStack stack = AlchemyItems.filled(lesser, delivery).getDefaultStack();
        int selected = Math.floorMod(combination.hashCode(), FAILURES.size());
        int[] colors = {0x82763B, 0x777187, 0x778270};
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.of(FAILURES.get(selected)), Optional.of(colors[selected]), List.of()));
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new net.minecraft.component.type.CustomModelDataComponent(1));
        return stack;
    }

    public static Text name(ItemStack stack, boolean lesser, AlchemyItems.Delivery delivery) {
        String path = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion()
                .flatMap(potion -> potion.getKey()).map(key -> key.getValue().getPath()).orElse("water");
        if (isFailure(stack)) return Text.translatable("alchemy.cranks-dungeons." + path);
        String family = path.replaceFirst("^lesser_", "");
        if (family.equals("water")) return Text.translatable("alchemy.cranks-dungeons." + (lesser ? "thin_" : "") + "water_" + delivery.name().toLowerCase(Locale.ROOT));
        String prefix = delivery == AlchemyItems.Delivery.DRINK ? "potion" : delivery == AlchemyItems.Delivery.SPLASH ? "splash" : "lingering";
        return Text.translatable("alchemy.cranks-dungeons." + (lesser ? "lesser_" : "") + prefix,
                Text.translatable("alchemy.cranks-dungeons.effect." + family));
    }
}

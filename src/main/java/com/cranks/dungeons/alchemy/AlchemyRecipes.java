package com.cranks.dungeons.alchemy;

import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

import java.util.*;

/** Shared immutable recipe definitions and a simulated transaction used by both UI and server. */
public final class AlchemyRecipes {
    public record Need(List<Item> alternatives, int count, boolean water) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && alternatives.contains(stack.getItem()) && (!water ||
                    stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).matches(Potions.WATER));
        }
        public ItemStack example() { return water ? AlchemyItems.water(((AlchemyItems.Brew) alternatives.getFirst()).lesser,
                ((AlchemyItems.Brew) alternatives.getFirst()).delivery) : alternatives.getFirst().getDefaultStack(); }
        public int available(MaterialCrafting.Storage storage) {
            int total = 0;
            for (int i = 0; i < storage.size(); i++) if (matches(storage.stack(i))) total += storage.stack(i).getCount();
            return total;
        }
    }
    public record Extraction(String key, List<Need> needs, ItemStack output) {}
    public record Experiment(String primary, String secondary, String suspension, boolean lesser, AlchemyItems.Delivery delivery) {
        public String key() {
            var ingredients = new ArrayList<>(extracts());
            ingredients.sort(String::compareTo);
            return String.join("+", ingredients) + ":" + suspension + ":" + lesser + ":" + delivery.name();
        }
        public List<String> extracts() { return secondary.isEmpty() ? List.of(primary) : List.of(primary, secondary); }
    }
    public record Plan(MaterialCrafting.Storage source, ItemStack[] result) {
        public boolean commit(net.minecraft.entity.player.PlayerInventory inventory) {
            if (!source.isCurrent()) return false;
            for (int i = 0; i < result.length; i++) {
                if (!ItemStack.areEqual(source.stack(i), result[i])) inventory.setStack(source.slot(i), result[i]);
            }
            inventory.markDirty();
            return true;
        }
    }

    public static final List<String> EXTRACTS = List.of("melon_extract", "honey_extract", "bone_salt", "sugar_extract", "heat_salt",
            "turtle_extract", "rabbit_foot_extract", "golden_carrot_extract", "pufferfish_extract", "membrane_extract", "fermented_eye_extract",
            "spider_eye_extract", "mushroom_extract", "lapis_salt", "wind_essence", "thread_extract", "slime_extract", "stone_salt", "frost_salt", "copper_salt", "amethyst_essence");
    public static final List<String> SUSPENSIONS = List.of("herbal", "mineral", "binding");
    private static List<Extraction> extractions;
    private AlchemyRecipes() {}

    public static List<Extraction> extractions() {
        if (extractions != null) return extractions;
        List<Extraction> list = new ArrayList<>();
        raw(list, "melon_extract", Items.MELON_SLICE, 3); raw(list, "honey_extract", Items.HONEYCOMB, 3);
        raw(list, "bone_salt", Items.BONE_MEAL, 3); raw(list, "sugar_extract", Items.SUGAR, 3);
        raw(list, "heat_salt", Items.MAGMA_BLOCK, 3); raw(list, "turtle_extract", Items.TURTLE_SCUTE, 1);
        raw(list, "rabbit_foot_extract", Items.RABBIT_FOOT, 1); raw(list, "golden_carrot_extract", Items.GOLDEN_CARROT, 1);
        raw(list, "pufferfish_extract", Items.PUFFERFISH, 1); raw(list, "membrane_extract", Items.PHANTOM_MEMBRANE, 1);
        raw(list, "fermented_eye_extract", Items.FERMENTED_SPIDER_EYE, 1); raw(list, "spider_eye_extract", Items.SPIDER_EYE, 3);
        raw(list, "mushroom_extract", Items.BROWN_MUSHROOM, 3); raw(list, "lapis_salt", Items.LAPIS_LAZULI, 3);
        raw(list, "wind_essence", Items.BREEZE_ROD, 1); raw(list, "thread_extract", Items.COBWEB, 3);
        raw(list, "slime_extract", Items.SLIME_BLOCK, 1); raw(list, "stone_salt", Items.STONE, 3);
        raw(list, "frost_salt", Items.SNOWBALL, 3); raw(list, "copper_salt", Items.RAW_COPPER, 3);
        raw(list, "amethyst_essence", Items.AMETHYST_SHARD, 3); raw(list, "amethyst_dust", Items.AMETHYST_SHARD, 1);
        list.add(new Extraction("glowing_catalyst", List.of(need(AlchemyItems.reagent("amethyst_dust"), 1), need(Items.GLOW_BERRIES, 1)), AlchemyItems.reagent("glowing_catalyst").getDefaultStack()));
        raw(list, "wheat_suspension", Items.WHEAT, 3); raw(list, "cane_suspension", Items.SUGAR_CANE, 3);
        raw(list, "dandelion_suspension", Items.DANDELION, 3); raw(list, "clay_suspension", Items.CLAY_BALL, 3);
        raw(list, "flint_suspension", Items.FLINT, 3); raw(list, "calcite_suspension", Items.CALCITE, 3);
        raw(list, "slime_suspension", Items.SLIME_BALL, 3); raw(list, "flesh_suspension", Items.ROTTEN_FLESH, 3);
        raw(list, "leather_suspension", Items.LEATHER, 3);
        extractions = List.copyOf(list);
        return extractions;
    }

    private static void raw(List<Extraction> list, String key, Item input, int amount) {
        list.add(new Extraction(key, List.of(need(input, amount)), AlchemyItems.reagent(key).getDefaultStack()));
    }
    private static Need need(Item item, int amount) { return new Need(List.of(item), amount, false); }
    public static List<Item> suspensionItems(String family) {
        return switch (family) {
            case "herbal" -> List.of(AlchemyItems.reagent("wheat_suspension"), AlchemyItems.reagent("cane_suspension"), AlchemyItems.reagent("dandelion_suspension"));
            case "mineral" -> List.of(AlchemyItems.reagent("clay_suspension"), AlchemyItems.reagent("flint_suspension"), AlchemyItems.reagent("calcite_suspension"));
            case "binding" -> List.of(AlchemyItems.reagent("slime_suspension"), AlchemyItems.reagent("flesh_suspension"), AlchemyItems.reagent("leather_suspension"));
            default -> List.of();
        };
    }

    public static List<Need> needs(Experiment experiment) {
        List<Need> list = new ArrayList<>();
        for (String extract : experiment.extracts()) list.add(need(AlchemyItems.reagent(extract), 1));
        list.add(new Need(suspensionItems(experiment.suspension), 1, false));
        list.add(new Need(List.of(AlchemyItems.filled(experiment.lesser, experiment.delivery)), 1, true));
        if (!experiment.lesser) list.add(need(AlchemyItems.reagent("glowing_catalyst"), 1));
        return List.copyOf(list);
    }

    public static Experiment forFormula(AlchemyPotions.Formula formula, AlchemyItems.Delivery delivery) {
        return new Experiment(formula.extracts().getFirst(), formula.extracts().size() > 1 ? formula.extracts().get(1) : "", formula.suspension(), formula.lesser(), delivery);
    }

    public static AlchemyPotions.Formula resolve(Experiment experiment) {
        var ingredients = experiment.extracts().stream().sorted().toList();
        return AlchemyPotions.FORMULAS.stream().filter(formula -> formula.lesser() == experiment.lesser
                && formula.suspension().equals(experiment.suspension)
                && formula.extracts().stream().sorted().toList().equals(ingredients)).findFirst().orElse(null);
    }

    public static boolean valid(Experiment experiment) {
        return EXTRACTS.contains(experiment.primary) && (experiment.secondary.isEmpty() || EXTRACTS.contains(experiment.secondary))
                && SUSPENSIONS.contains(experiment.suspension) && experiment.delivery != null;
    }

    public static int materialLimit(MaterialCrafting.Storage storage, List<Need> needs) {
        int result = MaterialCrafting.MAX_BATCH;
        for (Need need : needs) result = Math.min(result, need.available(storage) / need.count);
        return result;
    }

    public static Plan plan(MaterialCrafting.Storage storage, List<Need> needs, ItemStack output, int quantity) {
        if (quantity < 1 || quantity > MaterialCrafting.MAX_BATCH || output.isEmpty()) return null;
        ItemStack[] stacks = new ItemStack[storage.size()];
        for (int i = 0; i < stacks.length; i++) stacks[i] = storage.stack(i).copy();
        for (Need need : needs) {
            int remaining = need.count * quantity;
            for (int i = 0; i < stacks.length && remaining > 0; i++) {
                if (!need.matches(stacks[i])) continue;
                int used = Math.min(remaining, stacks[i].getCount());
                stacks[i].decrement(used);
                remaining -= used;
            }
            if (remaining > 0) return null;
        }
        long remaining = (long) output.getCount() * quantity;
        // Merge across all unlocked storage first, before using any empty slot.
        for (ItemStack stack : stacks) {
            if (!ItemStack.areItemsAndComponentsEqual(stack, output)) continue;
            int move = (int) Math.min(remaining, Math.max(0, stack.getMaxCount() - stack.getCount()));
            stack.increment(move); remaining -= move;
        }
        // Empty slots follow the same priority as pickups: hotbar, normal inventory, bags.
        for (int priority = 0; priority < 3; priority++) {
            for (int i = 0; i < stacks.length && remaining > 0; i++) {
                int slot = storage.slot(i);
                boolean hotbar = false;
                for (int j = 0; j < PlayerLoadout.HOTBAR_COUNT; j++) hotbar |= slot == PlayerLoadout.getHotbarSlot(j);
                int group = hotbar ? 0 : slot >= 9 && slot < 36 ? 1 : 2;
                if (group != priority || !stacks[i].isEmpty()) continue;
                int move = (int) Math.min(remaining, output.getMaxCount());
                stacks[i] = output.copyWithCount(move); remaining -= move;
            }
        }
        return remaining == 0 ? new Plan(storage, stacks) : null;
    }
}

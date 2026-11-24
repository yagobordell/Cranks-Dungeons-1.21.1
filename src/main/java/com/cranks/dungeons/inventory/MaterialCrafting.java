package com.cranks.dungeons.inventory;

import com.cranks.dungeons.component.ModComponents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingDecoratedPotRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/** Plans a craft against a snapshot, then applies it in one server-side inventory update. */
public final class MaterialCrafting {
    public static final int MAX_BATCH = 10_000;
    public static final int MAX_INPUTS = 9;

    private MaterialCrafting() {}

    public static boolean supports(CraftingRecipe recipe, int grid, World world) {
        if (!recipe.fits(grid, grid)) return false;
        if (recipe instanceof SpecialCraftingRecipe) return true;
        if (!(recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) || recipe.isEmpty()) return false;
        ItemStack result = recipe.getResult(world.getRegistryManager());
        return !result.isEmpty() && result.isItemEnabled(world.getEnabledFeatures());
    }

    public static boolean supportsCutting(Recipe<?> recipe, World world) {
        if (supportsAssembly(recipe, StationAssemblyRecipe.Station.CUTTING_BENCH, world)) return true;
        if (!(recipe instanceof StonecuttingRecipe cutting)) return false;
        ItemStack result = cutting.getResult(world.getRegistryManager());
        return cutting.getIngredients().size() == 1 && !cutting.isEmpty()
                && !result.isEmpty() && result.isItemEnabled(world.getEnabledFeatures());
    }

    public static boolean supportsAssembly(Recipe<?> recipe, StationAssemblyRecipe.Station station, World world) {
        return recipe instanceof StationAssemblyRecipe assembly && assembly.station() == station && supports(assembly.assembly(), 3, world);
    }

    public static Recipe<?> unwrap(Recipe<?> recipe) {
        return recipe instanceof StationAssemblyRecipe assembly ? assembly.assembly() : recipe;
    }

    public static boolean usesChoices(Recipe<?> recipe) {
        recipe = unwrap(recipe);
        return recipe instanceof SpecialCraftingRecipe || recipe instanceof MapExtendingRecipe;
    }

    public static int choiceLimit(CraftingRecipe recipe, int grid) {
        if (recipe instanceof CartographyRecipe) return 1;
        if (recipe instanceof TailoringPatternRecipe pattern) return pattern.template().isEmpty() ? 2 : 3;
        if (recipe instanceof MapExtendingRecipe || recipe instanceof TippedArrowRecipe) return 1;
        return recipe instanceof CraftingDecoratedPotRecipe ? 4 : grid * grid;
    }

    public static boolean allowed(ServerPlayerEntity player, RecipeEntry<?> entry) {
        var discoveries = ModComponents.ITEM_DISCOVERIES.get(player);
        discoveries.recordInventory();
        if (!RecipeDiscovery.isDiscovered(entry.value(), discoveries)) return false;
        return entry.value() instanceof StonecuttingRecipe || entry.value().isIgnoredInRecipeBook()
                || !player.getWorld().getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)
                || player.getRecipeBook().contains(entry.id());
    }

    public static void commit(ServerPlayerEntity player, RecipeEntry<?> entry, Plan plan, int quantity) {
        PlayerInventory inventory = player.getInventory();
        ItemStack output = plan.output;
        boolean newMap = output.contains(DataComponentTypes.MAP_POST_PROCESSING);
        for (int i = 0; i < plan.storage.size(); i++) {
            ItemStack original = plan.storage.stack(i);
            ItemStack next = plan.counts[i] == 0 ? ItemStack.EMPTY : plan.items[i].copyWithCount(plan.counts[i]);
            if (newMap && plan.items[i] == output && !next.isEmpty()) next.onCraftByCrafter(player.getWorld());
            if (!ItemStack.areEqual(original, next)) inventory.setStack(plan.storage.slot(i), next);
        }
        inventory.markDirty();
        int resultCount = output.getCount() * quantity;
        if (newMap) player.increaseStat(Stats.CRAFTED.getOrCreateStat(output.getItem()), resultCount);
        else output.copy().onCraftByPlayer(player.getWorld(), player, resultCount);
        if (!entry.value().isIgnoredInRecipeBook() || entry.value() instanceof StationAssemblyRecipe) {
            Criteria.RECIPE_CRAFTED.trigger(player, entry.id(), plan.input.getStacks());
        }
        if (!entry.value().isIgnoredInRecipeBook()) player.unlockRecipes(List.of(entry));
    }

    public static final class Storage {
        private final net.minecraft.inventory.Inventory inventory;
        private final int[] slots;
        private final ItemStack[] stacks;

        public Storage(PlayerInventory inventory) {
            this.inventory = inventory;
            PlayerLoadout loadout = PlayerLoadout.of(inventory);
            int[] indices = new int[27 + PlayerLoadout.BAG_COUNT * PlayerLoadout.BAG_WIDTH + PlayerLoadout.HOTBAR_COUNT];
            int size = 0;
            for (int slot = 9; slot < PlayerInventory.MAIN_SIZE; slot++) indices[size++] = slot;
            for (int row = 0; row < PlayerLoadout.BAG_COUNT; row++) {
                int first = PlayerLoadout.BAG_STORAGE + row * PlayerLoadout.BAG_WIDTH;
                for (int j = 0; j < loadout.capacity(row); j++) indices[size++] = first + j;
            }
            for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) indices[size++] = PlayerLoadout.getHotbarSlot(i);
            slots = Arrays.copyOf(indices, size);
            stacks = new ItemStack[size];
            for (int i = 0; i < size; i++) stacks[i] = inventory.getStack(slots[i]).copy();
        }

        /** Machine storage has no access to a nearby player's inventory. */
        public Storage(net.minecraft.inventory.Inventory inventory) {
            this.inventory = inventory;
            slots = java.util.stream.IntStream.range(0, inventory.size()).toArray();
            stacks = new ItemStack[slots.length];
            for (int i = 0; i < slots.length; i++) stacks[i] = inventory.getStack(i).copy();
        }

        public boolean isCurrent() {
            if (!(inventory instanceof PlayerInventory playerInventory)) {
                if (inventory.size() != slots.length) return false;
                for (int i = 0; i < slots.length; i++) if (!ItemStack.areEqual(stacks[i], inventory.getStack(i))) return false;
                return true;
            }
            int size = 27 + PlayerLoadout.HOTBAR_COUNT;
            PlayerLoadout loadout = PlayerLoadout.of(playerInventory);
            int pos = 27;
            for (int row = 0; row < PlayerLoadout.BAG_COUNT; row++) {
                int count = loadout.capacity(row);
                size += count;
                for (int j = 0; j < count; j++, pos++) {
                    if (pos >= slots.length || slots[pos] != PlayerLoadout.BAG_STORAGE + row * PlayerLoadout.BAG_WIDTH + j) {
                        return false;
                    }
                }
            }
            if (size != slots.length) return false;
            for (int i = 0; i < slots.length; i++) {
                if (!ItemStack.areEqual(stacks[i], inventory.getStack(slots[i]))) return false;
            }
            return true;
        }

        public int size() { return slots.length; }
        public int slot(int index) { return slots[index]; }
        public ItemStack stack(int index) { return stacks[index]; }

        public ItemStack stackAtSlot(int slot) {
            for (int i = 0; i < size(); i++) if (slots[i] == slot) return stacks[i];
            return ItemStack.EMPTY;
        }
    }

    public record Material(List<ItemStack> examples, int perCraft, int available) {}

    public static final class Selection {
        private final Storage storage;
        private final Recipe<?> recipe;
        private final World world;
        private final int width;
        private final int height;
        private final int[] positions;
        private final int[] matches;
        private final int reusable;
        private final boolean manual;
        private final List<Material> materials;
        private final int upperBound;

        public Selection(Storage storage, RecipeEntry<?> entry, World world, int grid, List<Integer> sourceSlots) {
            this.storage = storage;
            this.world = world;
            this.recipe = entry.value() instanceof StationAssemblyRecipe assembly ? assembly.assembly() : entry.value();
            List<Predicate<ItemStack>> tests = new ArrayList<>();
            List<List<ItemStack>> icons = new ArrayList<>();
            List<Integer> activePositions = new ArrayList<>();
            manual = recipe instanceof CraftingRecipe crafting && usesChoices(crafting);
            if (manual) {
                boolean centerChoice = recipe instanceof MapExtendingRecipe || recipe instanceof TippedArrowRecipe;
                int[] pattern = recipe instanceof CraftingDecoratedPotRecipe ? new int[]{1, 3, 5, 7} : null;
                width = recipe instanceof CartographyRecipe ? 2 : pattern != null || centerChoice ? 3 : Math.min(grid, Math.max(1, sourceSlots.size()));
                height = pattern != null || centerChoice ? 3 : Math.max(1, (sourceSlots.size() + width - 1) / width);
                for (int i = 0; i < sourceSlots.size(); i++) {
                    ItemStack stack = storage.stackAtSlot(sourceSlots.get(i));
                    if (stack.isEmpty() || i >= choiceLimit((CraftingRecipe) recipe, grid)) break;
                    tests.add(candidate -> ItemStack.areItemsAndComponentsEqual(stack, candidate));
                    icons.add(List.of(stack.copyWithCount(1)));
                    activePositions.add(pattern != null ? pattern[i] : centerChoice ? 4 : i);
                }
                if (tests.size() != sourceSlots.size()) {
                    tests.clear();
                    icons.clear();
                    activePositions.clear();
                } else if (recipe instanceof CartographyRecipe cartography && sourceSlots.size() == 1) {
                    var material = cartography.operation().material().getDefaultStack();
                    tests.add(candidate -> candidate.isOf(material.getItem()));
                    icons.add(List.of(material));
                    activePositions.add(1);
                } else if (centerChoice && sourceSlots.size() == 1) {
                    ItemStack outer = new ItemStack(recipe instanceof MapExtendingRecipe ? Items.PAPER : Items.ARROW);
                    for (int i = 0; i < 9; i++) {
                        if (i == 4) continue;
                        tests.add(candidate -> candidate.isOf(outer.getItem()));
                        icons.add(List.of(outer));
                        activePositions.add(i);
                    }
                }
            } else {
                var ingredients = recipe.getIngredients();
                width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth()
                        : Math.min(grid, Math.max(1, ingredients.size()));
                height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight()
                        : Math.max(1, (ingredients.size() + width - 1) / width);
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ingredient = ingredients.get(i);
                    if (ingredient.isEmpty()) continue;
                    tests.add(ingredient::test);
                    icons.add(List.of(ingredient.getMatchingStacks()));
                    activePositions.add(i);
                }
            }
            positions = activePositions.stream().mapToInt(Integer::intValue).toArray();
            reusable = manual ? findReusableInputs(icons) : 0;
            matches = new int[storage.size()];
            int[] available = new int[tests.size()];
            int total = 0;
            for (int stock = 0; stock < storage.size(); stock++) {
                ItemStack stack = storage.stack(stock);
                if (stack.isEmpty()) continue;
                for (int need = 0; need < tests.size(); need++) {
                    if (tests.get(need).test(stack)) {
                        matches[stock] |= 1 << need;
                        available[need] += stack.getCount();
                    }
                }
                if (matches[stock] != 0) total += stack.getCount();
            }
            List<Material> grouped = new ArrayList<>();
            boolean[] groupedPositions = new boolean[tests.size()];
            int consumedPositions = tests.size() - Integer.bitCount(reusable);
            int limit = tests.isEmpty() ? 0 : consumedPositions == 0 ? 1 : Math.min(MAX_BATCH, total / consumedPositions);
            for (int i = 0; i < tests.size(); i++) {
                if (groupedPositions[i]) continue;
                int count = 1;
                for (int j = i + 1; j < tests.size(); j++) {
                    if (isReusable(i) == isReusable(j) && sameAlternatives(icons.get(i), icons.get(j))) {
                        groupedPositions[j] = true;
                        count++;
                    }
                }
                grouped.add(new Material(icons.get(i), count, available[i]));
                if (!isReusable(i)) limit = Math.min(limit, available[i] / count);
                else if (available[i] < count) limit = 0;
            }
            materials = List.copyOf(grouped);
            upperBound = limit;
        }

        private int findReusableInputs(List<List<ItemStack>> examples) {
            if (positions.length == 0) return 0;
            ItemStack[] grid = new ItemStack[width * height];
            Arrays.fill(grid, ItemStack.EMPTY);
            for (int i = 0; i < positions.length; i++) grid[positions[i]] = examples.get(i).getFirst();
            CraftingRecipeInput input = CraftingRecipeInput.create(width, height, Arrays.asList(grid));
            if (!matchesRecipe(input) || input.getSize() != grid.length) return 0;
            var remainders = ((CraftingRecipe) recipe).getRemainder(input);
            int result = 0;
            for (int i = 0; i < positions.length; i++) {
                ItemStack original = grid[positions[i]];
                ItemStack remainder = remainders.get(positions[i]);
                // Map cloning includes the original in the result. Other copying recipes return it.
                if (recipe instanceof MapCloningRecipe && original.isOf(Items.FILLED_MAP)
                        || ItemStack.areEqual(original, remainder)) result |= 1 << i;
            }
            return result;
        }

        private boolean isReusable(int ingredient) { return (reusable & (1 << ingredient)) != 0; }

        public List<Material> materials() { return materials; }

        public ItemStack preview() {
            if (!manual) return recipe.getResult(world.getRegistryManager());
            if (upperBound == 0) return ItemStack.EMPTY;
            CraftingRecipeInput input = inputFor(allocate(1));
            return input != null && matchesRecipe(input)
                    ? craftResult(input) : ItemStack.EMPTY;
        }

        public int materialLimit() {
            int low = 0, high = upperBound;
            while (low < high) {
                int middle = (low + high + 1) / 2;
                if (allocate(middle) != null) low = middle;
                else high = middle - 1;
            }
            return low;
        }

        public int fittingLimit(int materials) {
            ItemStack output = preview();
            if (output.isEmpty()) return 0;
            int max = output.contains(DataComponentTypes.MAP_POST_PROCESSING)
                    ? output.getCount() : storage.inventory.getMaxCount(output);
            // Occupied input stacks can give up their slot when the batch consumes them entirely.
            // Search downwards since a smaller batch can require more additional slots.
            for (int count = Math.min(materials, storage.size() * max / output.getCount()); count > 0; count--) {
                if (plan(count) != null) return count;
            }
            return 0;
        }

        public Plan plan(int quantity) {
            if (quantity <= 0 || quantity > upperBound) return null;
            int[][] allocation = allocate(quantity);
            CraftingRecipeInput first = inputFor(allocation);
            if (first == null || !matchesRecipe(first)) return null;
            ItemStack output = craftResult(first);
            if (output.isEmpty() || !output.isItemEnabled(world.getEnabledFeatures())) return null;
            ItemStack[] items = storage.stacks.clone();
            int[] counts = new int[items.length];
            for (int stock = 0; stock < counts.length; stock++) {
                counts[stock] = items[stock].getCount();
                for (int i = 0; i < allocation.length; i++) {
                    if (!isReusable(i)) counts[stock] -= allocation[i][stock];
                }
            }
            if (output.contains(DataComponentTypes.MAP_POST_PROCESSING)) {
                if (!insertNewMaps(items, counts, output, quantity)) return null;
            } else if (!insert(items, counts, output, (long) output.getCount() * quantity)) return null;
            if (!(recipe instanceof StonecuttingRecipe) && !hasCustomRemainders()) {
                for (int stock = 0; stock < counts.length; stock++) {
                    ItemStack consumed = storage.stack(stock);
                    if (consumed.isEmpty() || !consumed.getItem().hasRecipeRemainder()) continue;
                    int used = 0;
                    for (int[] assigned : allocation) used += assigned[stock];
                    if (used > 0 && !insert(items, counts, consumed.getItem().getRecipeRemainder().getDefaultStack(), used)) return null;
                }
            } else if (recipe instanceof CraftingRecipe crafting) {
                // Special recipes can preserve components on returned items.
                int unchanged = 0;
                for (int i = 0; i < positions.length; i++) if (isReusable(i)) unchanged |= 1 << positions[i];
                var remainders = crafting.getRemainder(first);
                for (int i = 0; i < remainders.size(); i++) {
                    if ((unchanged & (1 << i)) != 0) continue;
                    ItemStack remainder = remainders.get(i);
                    if (!remainder.isEmpty() && !insert(items, counts, remainder, (long) remainder.getCount() * quantity)) return null;
                }
            }
            return new Plan(storage, items, counts, first, output);
        }

        /** One pulse: consume materials, retain reusable originals, eject result and remainders. */
        public AutomationPlan automationPlan() {
            if (upperBound == 0 || !(recipe instanceof CraftingRecipe crafting)) return null;
            int[][] allocation = allocate(1);
            CraftingRecipeInput input = inputFor(allocation);
            if (input == null || !matchesRecipe(input)) return null;
            ItemStack output = craftResult(input);
            if (output.isEmpty() || !output.isItemEnabled(world.getEnabledFeatures())) return null;
            var remaining = new ArrayList<ItemStack>();
            for (int stock = 0; stock < storage.size(); stock++) {
                int count = storage.stack(stock).getCount();
                for (int i = 0; i < allocation.length; i++) if (!isReusable(i)) count -= allocation[i][stock];
                remaining.add(count == 0 ? ItemStack.EMPTY : storage.stack(stock).copyWithCount(count));
            }
            var outputs = new ArrayList<ItemStack>();
            outputs.add(output.copy());
            var remainders = crafting.getRemainder(input);
            int unchanged = 0;
            for (int i = 0; i < positions.length; i++) if (isReusable(i)) unchanged |= 1 << positions[i];
            for (int i = 0; i < remainders.size(); i++) if ((unchanged & (1 << i)) == 0 && !remainders.get(i).isEmpty()) outputs.add(remainders.get(i).copy());
            return new AutomationPlan(storage, List.copyOf(remaining), List.copyOf(outputs));
        }

        private ItemStack craftResult(CraftingRecipeInput input) {
            if (recipe instanceof StonecuttingRecipe cutting) {
                return cutting.craft(new SingleStackRecipeInput(input.getStackInSlot(0)), world.getRegistryManager());
            }
            ItemStack result = ((CraftingRecipe) recipe).craft(input, world.getRegistryManager());
            if (reusable != 0 && recipe instanceof MapCloningRecipe) result.decrement(1);
            return result;
        }

        private boolean matchesRecipe(CraftingRecipeInput input) {
            if (recipe instanceof StonecuttingRecipe cutting) {
                return input.getSize() == 1 && cutting.matches(new SingleStackRecipeInput(input.getStackInSlot(0)), world);
            }
            if (((CraftingRecipe) recipe).matches(input, world)) return true;
            // Map state for maps in a bag may be missing on the client until a player holds them.
            if (!world.isClient() || !(recipe instanceof MapExtendingRecipe) || input.getSize() != 9) return false;
            ItemStack map = input.getStackInSlot(4);
            return map.isOf(Items.FILLED_MAP) && FilledMapItem.getMapState(map, world) == null;
        }

        private boolean hasCustomRemainders() {
            Class<?> type = recipe.getClass();
            return type != ShapedRecipe.class && type != ShapelessRecipe.class;
        }

        private CraftingRecipeInput inputFor(int[][] allocation) {
            if (allocation == null) return null;
            ItemStack[] grid = new ItemStack[width * height];
            Arrays.fill(grid, ItemStack.EMPTY);
            for (int need = 0; need < positions.length; need++) {
                for (int stock = 0; stock < storage.size(); stock++) {
                    if (allocation[need][stock] > 0) {
                        grid[positions[need]] = storage.stack(stock).copyWithCount(1);
                        break;
                    }
                }
            }
            return CraftingRecipeInput.create(width, height, Arrays.asList(grid));
        }

        private boolean insert(ItemStack[] items, int[] counts, ItemStack item, long amount) {
            int max = storage.inventory.getMaxCount(item);
            for (int i = 0; amount > 0 && i < counts.length; i++) {
                if (counts[i] <= 0 || counts[i] >= max || !ItemStack.areItemsAndComponentsEqual(items[i], item)) continue;
                int moved = (int) Math.min(amount, max - counts[i]);
                counts[i] += moved;
                amount -= moved;
            }
            for (int i = 0; amount > 0 && i < counts.length; i++) {
                if (counts[i] != 0) continue;
                int moved = (int) Math.min(amount, max);
                items[i] = item;
                counts[i] = moved;
                amount -= moved;
            }
            return amount == 0;
        }

        private static boolean insertNewMaps(ItemStack[] items, int[] counts, ItemStack output, int quantity) {
            for (int i = 0; quantity > 0 && i < counts.length; i++) {
                if (counts[i] != 0) continue;
                items[i] = output;
                counts[i] = output.getCount();
                quantity--;
            }
            return quantity == 0;
        }

        private int[][] allocate(int quantity) {
            int[][] allocation = new int[positions.length][storage.size()];
            int[] left = new int[positions.length];
            int[] visited = new int[1];
            int needed = 0;
            for (int i = 0; i < left.length; i++) {
                left[i] = isReusable(i) ? 1 : quantity;
                needed += left[i];
            }
            for (int stock = 0; stock < storage.size() && needed > 0; stock++) {
                if (matches[stock] == 0) continue;
                int remaining = storage.stack(stock).getCount();
                while (remaining > 0 && needed > 0) {
                    visited[0] = 0;
                    int used = augment(stock, remaining, visited, allocation, left);
                    if (used == 0) break;
                    remaining -= used;
                    needed -= used;
                }
            }
            return needed == 0 ? allocation : null;
        }

        private int augment(int stock, int limit, int[] visited, int[][] allocation, int[] left) {
            int eligible = matches[stock] & ~visited[0];
            for (int mask = eligible; mask != 0; mask &= mask - 1) {
                int need = Integer.numberOfTrailingZeros(mask);
                if (left[need] == 0) continue;
                int moved = Math.min(limit, left[need]);
                allocation[need][stock] += moved;
                left[need] -= moved;
                return moved;
            }
            for (int mask = eligible; mask != 0; mask &= mask - 1) {
                int need = Integer.numberOfTrailingZeros(mask);
                if ((visited[0] & (1 << need)) != 0) continue;
                visited[0] |= 1 << need;
                for (int previous = 0; previous < storage.size(); previous++) {
                    int assigned = allocation[need][previous];
                    if (assigned == 0 || previous == stock) continue;
                    int moved = augment(previous, Math.min(limit, assigned), visited, allocation, left);
                    if (moved == 0) continue;
                    allocation[need][previous] -= moved;
                    allocation[need][stock] += moved;
                    return moved;
                }
            }
            return 0;
        }
    }

    private static boolean sameAlternatives(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) {
            if (!ItemStack.areItemsAndComponentsEqual(first.get(i), second.get(i))) return false;
        }
        return true;
    }

    public record Plan(Storage storage, ItemStack[] items, int[] counts, CraftingRecipeInput input, ItemStack output) {}
    public record AutomationPlan(Storage storage, List<ItemStack> remaining, List<ItemStack> outputs) {}
}

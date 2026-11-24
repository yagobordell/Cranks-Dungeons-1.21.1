package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.component.ItemDiscoveryComponent;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.FurnaceLoading;
import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.RecipeDiscovery;
import com.cranks.dungeons.inventory.SpecialCraftingMaterials;
import com.cranks.dungeons.inventory.StationAssemblyRecipe;
import com.cranks.dungeons.network.CraftMaterialsPayload;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class MaterialCraftingPanel {
    static final int WIDTH = 250;
    private static final int ROW = 20;
    private static final int LIST_TOP = 68;
    private static final int FILTER_Y = 49, FILTER_HEIGHT = 14, FILTER_GAP = 4, FILTER_PADDING = 3;
    private static final int FURNACE_SLOT_Y = 111;
    private static final int PAD = 8;
    private static final int TEXT = CompendiumStyle.TEXT;
    private static final int MUTED = CompendiumStyle.TEXT_MUTED;
    private static final int WARNING = CompendiumStyle.TEXT_WARNING;
    private static final int SUCCESS = CompendiumStyle.TEXT_SUCCESS;
    private static final ItemStack TABLE = new ItemStack(Items.CRAFTING_TABLE);
    private static final ItemStack CUTTING_BENCH = new ItemStack(Items.STONECUTTER);
    private static int requestSequence;

    private record RecipeRow(RecipeEntry<?> entry, String name, ItemStack icon, StationRecipeCategories.Category category, String nameKey,
                             String idKey, String pathKey) {
        boolean special() { return MaterialCrafting.usesChoices(entry.value()); }

        RecipeEntry<CraftingRecipe> crafting() {
            return new RecipeEntry<>(entry.id(), (CraftingRecipe) MaterialCrafting.unwrap(entry.value()));
        }

        AbstractCookingRecipe cooking() { return (AbstractCookingRecipe) entry.value(); }
    }

    private record SearchMatch(RecipeRow row, int rank) {}

    private record BatchPreview(ItemStack input, long inputCount, ItemStack output, long outputCount) {
        Text tooltip() {
            return input.isEmpty() ? message("batch_total", outputCount, com.cranks.dungeons.client.guide.GuideText.itemName(output))
                    : message("batch_conversion", inputCount, com.cranks.dungeons.client.guide.GuideText.itemName(input), outputCount, com.cranks.dungeons.client.guide.GuideText.itemName(output));
        }
    }

    private final PlayerInventory inventory;
    private final World world;
    private final ItemDiscoveryComponent discoveries;
    private final int grid, syncId;
    private final LoadoutFurnaceHandler furnace;
    private final boolean cutting;
    private final boolean anvil;
    private final boolean fletching;
    private final ItemStack stationIcon;
    private final GearSalvagePanel salvage;
    private final RecipeCategoryTabs categories;
    private final Text title;
    private final List<Integer> sources = new ArrayList<>();
    private final Map<RecipeRow, Integer> counts = new HashMap<>();
    private final Map<RecipeRow, Predicate<Item>> searchMaterials = new HashMap<>();
    private final List<RecipeRow> allRecipes;
    private int discoveryRevision = -1;
    private List<RecipeRow> discoveredRecipes = List.of();
    private List<RecipeRow> filtered = List.of();
    private List<Integer> ingredientRows = List.of();
    private MaterialCrafting.Storage storage;
    private RecipeRow selected;
    private MaterialCrafting.Selection selection;
    private ItemStack output = ItemStack.EMPTY;
    private ItemStack lastFurnaceInput = ItemStack.EMPTY;
    private ItemStack expectedFurnaceOutput = ItemStack.EMPTY;
    private ItemStack loadInput = ItemStack.EMPTY;
    private int fit, materialLimit, assessedAmount = -1;
    private boolean amountFits, picking, searchIngredients, availableOnly;
    private int request, scroll;
    private double grab;
    private boolean grabbed, capturing;
    private long feedbackUntil;
    private Text feedback;
    private boolean feedbackGood;
    private TextRenderer font;
    private TextFieldWidget search, quantity;
    private int x, y, width, height, divider;
    private int searchModeWidth;
    private int salvageTabX;
    private boolean salvaging;

    MaterialCraftingPanel(PlayerInventory inventory, boolean table, boolean cutting, int syncId, Text title,
                          LoadoutFurnaceHandler furnace, boolean anvil, boolean fletching, boolean tailoring, boolean cartography,
                          BiConsumer<Integer, ItemStack> submitSalvage) {
        this.inventory = inventory;
        this.world = inventory.player.getWorld();
        this.discoveries = ModComponents.ITEM_DISCOVERIES.get(inventory.player);
        this.grid = table || cutting || anvil || fletching || tailoring || cartography ? 3 : 2;
        this.syncId = syncId;
        this.furnace = furnace;
        this.cutting = cutting;
        this.anvil = anvil;
        this.fletching = fletching;
        this.title = table || cutting || furnace != null || anvil || fletching || tailoring || cartography ? title : message("hand");
        stationIcon = furnace != null ? furnace.station().block().asItem().getDefaultStack()
                : table ? TABLE : cutting ? CUTTING_BENCH : anvil ? Items.ANVIL.getDefaultStack() : fletching ? Items.FLETCHING_TABLE.getDefaultStack() : tailoring ? Items.LOOM.getDefaultStack() : cartography ? Items.CARTOGRAPHY_TABLE.getDefaultStack() : ItemStack.EMPTY;
        boolean blast = furnace != null && furnace.isBlastFurnace();
        salvage = table || blast ? new GearSalvagePanel(inventory, furnace, submitSalvage) : null;
        salvaging = blast;
        StationRecipeCategories station = furnace != null ? switch (furnace.station()) {
            case FURNACE -> StationRecipeCategories.FURNACE;
            case BLAST_FURNACE -> StationRecipeCategories.FOUNDRY;
            case SMOKER -> StationRecipeCategories.COOKSTOVE;
        } : cutting ? StationRecipeCategories.CUTTING_BENCH : anvil ? StationRecipeCategories.ANVIL
                : fletching ? StationRecipeCategories.FLETCHING_TABLE : tailoring ? StationRecipeCategories.TAILORING_BENCH : cartography ? StationRecipeCategories.CARTOGRAPHY_TABLE : table ? StationRecipeCategories.CRAFTING_TABLE : StationRecipeCategories.HAND_CRAFTING;
        var recipes = world.getRecipeManager();
        Stream<? extends RecipeEntry<?>> entries;
        if (furnace != null) {
            entries = recipes.listAllOfType(furnace.recipeType()).stream()
                    .filter(entry -> !GearSalvage.isBlastRecipe(entry))
                    .filter(entry -> entry.value().getResult(world.getRegistryManager()).isItemEnabled(world.getEnabledFeatures()));
        } else if (cutting) {
            entries = Stream.concat(recipes.listAllOfType(RecipeType.STONECUTTING).stream(),
                            recipes.listAllOfType(StationAssemblyRecipe.Station.CUTTING_BENCH.type()).stream())
                    .filter(entry -> MaterialCrafting.supportsCutting(entry.value(), world));
        } else if (cartography) {
            entries = recipes.listAllOfType(StationAssemblyRecipe.Station.CARTOGRAPHY_TABLE.type()).stream()
                    .filter(entry -> MaterialCrafting.supportsAssembly(entry.value(), StationAssemblyRecipe.Station.CARTOGRAPHY_TABLE, world));
        } else if (tailoring) {
            entries = recipes.listAllOfType(StationAssemblyRecipe.Station.TAILORING_BENCH.type()).stream()
                    .filter(entry -> MaterialCrafting.supportsAssembly(entry.value(), StationAssemblyRecipe.Station.TAILORING_BENCH, world));
        } else if (fletching) {
            entries = recipes.listAllOfType(StationAssemblyRecipe.Station.FLETCHING_TABLE.type()).stream()
                    .filter(entry -> MaterialCrafting.supportsAssembly(entry.value(), StationAssemblyRecipe.Station.FLETCHING_TABLE, world));
        } else if (anvil) {
            entries = recipes.listAllOfType(StationAssemblyRecipe.Station.ANVIL.type()).stream()
                    .filter(entry -> MaterialCrafting.supportsAssembly(entry.value(), StationAssemblyRecipe.Station.ANVIL, world));
        } else {
            entries = recipes.listAllOfType(RecipeType.CRAFTING).stream()
                    .filter(entry -> MaterialCrafting.supports(entry.value(), grid, world));
        }
        allRecipes = entries
                .map(entry -> recipeRow(entry, station))
                .sorted(Comparator.comparing(RecipeRow::special).thenComparing(RecipeRow::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(row -> row.entry.id()))
                .toList();
        categories = allRecipes.isEmpty() || station.tabs().isEmpty() ? null : new RecipeCategoryTabs(station);
    }

    private RecipeRow recipeRow(RecipeEntry<?> entry, StationRecipeCategories station) {
        boolean special = MaterialCrafting.usesChoices(entry.value());
        String name = recipeName(entry, world).getString();
        ItemStack icon = recipeIcon(entry, world);
        return new RecipeRow(entry, name, icon, station.classify(entry.value(), icon),
                searchKey(name), searchKey(entry.id().toString()), searchKey(entry.id().getPath()));
    }

    static Text recipeName(RecipeEntry<?> entry, World world) {
        var pattern = MaterialCrafting.unwrap(entry.value()) instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe design ? design : null;
        var mapOperation = MaterialCrafting.unwrap(entry.value()) instanceof com.cranks.dungeons.inventory.CartographyRecipe operation ? operation : null;
        return mapOperation != null ? message("map_" + mapOperation.operation().asString()) : pattern != null ? pattern.title(world.getRegistryManager())
                : Text.literal(MaterialCrafting.usesChoices(entry.value()) ? specialName(entry) : recipeResultName(entry.value().getResult(world.getRegistryManager())));
    }

    static ItemStack recipeIcon(RecipeEntry<?> entry, World world) {
        var recipe = MaterialCrafting.unwrap(entry.value());
        return recipe instanceof com.cranks.dungeons.inventory.CartographyRecipe ? Items.FILLED_MAP.getDefaultStack()
                : recipe instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe pattern ? pattern.icon(world.getRegistryManager())
                : MaterialCrafting.usesChoices(recipe) ? specialIcon(entry) : recipe.getResult(world.getRegistryManager());
    }

    private static String recipeResultName(ItemStack result) {
        if (result.getItem() instanceof net.minecraft.item.BannerPatternItem) {
            String key = "crafting.cranks-dungeons.banner_template." + Registries.ITEM.getId(result.getItem()).toTranslationKey();
            if (net.minecraft.util.Language.getInstance().hasTranslation(key)) return Text.translatable(key).getString();
            String description = result.getItem().getTranslationKey() + ".desc";
            if (net.minecraft.util.Language.getInstance().hasTranslation(description)) return Text.translatable(description).getString();
        }
        return result.getName().getString();
    }

    private static String specialName(RecipeEntry<?> entry) {
        String path = entry.id().getPath();
        String key = "crafting.cranks-dungeons.recipe." + path;
        if (net.minecraft.util.Language.getInstance().hasTranslation(key)) return Text.translatable(key).getString();
        if (path.isEmpty()) return entry.id().toString();
        String name = path.replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static ItemStack specialIcon(RecipeEntry<?> entry) {
        return new ItemStack(switch (entry.id().getPath()) {
            case "armor_dye" -> Items.LEATHER_CHESTPLATE;
            case "banner_duplicate" -> Items.WHITE_BANNER;
            case "book_cloning" -> Items.WRITTEN_BOOK;
            case "decorated_pot" -> Items.DECORATED_POT;
            case "firework_rocket" -> Items.FIREWORK_ROCKET;
            case "firework_star", "firework_star_fade" -> Items.FIREWORK_STAR;
            case "map_cloning", "map_extending" -> Items.FILLED_MAP;
            case "shield_decoration" -> Items.SHIELD;
            case "shulker_box_coloring" -> Items.SHULKER_BOX;
            case "suspicious_stew" -> Items.SUSPICIOUS_STEW;
            case "tipped_arrow" -> Items.TIPPED_ARROW;
            default -> Items.PAPER;
        });
    }

    void init(TextRenderer font, int x, int y, int width, int height, boolean compact) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.divider = x + width * 44 / 100;
        int filterWidth = divider - x - PAD - 5 - FILTER_GAP;
        int modeLabelWidth = Math.max(font.getWidth(message("input_mode")), font.getWidth(message("result_mode")));
        int stockLabelWidth = font.getWidth(message("available_only"));
        // Divide the spare width between the labels, with a little extra space for Craftable.
        searchModeWidth = Math.clamp((filterWidth + modeLabelWidth - stockLabelWidth) / 2 - 1,
                filterWidth / 3, 2 * filterWidth / 3);
        salvageTabX = x + width - (compact ? 79 : PAD) - 61;
        if (salvage != null) salvage.init(font, x + PAD, y + 32, divider, x + width - PAD, y + height - PAD);
        String query = search == null ? "" : search.getText();
        String amount = quantity == null ? "1" : quantity.getText();
        search = field(x + PAD, y + 30, divider - x - PAD - 5, message("search"));
        search.setMaxLength(80);
        search.setPlaceholder(message(searchIngredients ? "search_material" : "search"));
        search.setText(query);
        search.setChangedListener(value -> filter(true));
        quantity = field(divider + 30, y + height - 49, actionWidth() - 48, message(furnace == null ? "quantity" : "load_amount"));
        quantity.setMaxLength(5);
        quantity.setTextPredicate(value -> value.chars().allMatch(c -> c >= '0' && c <= '9'));
        quantity.setText(amount);
        filter(false);
        capturing = false;
        grabbed = false;
    }

    private TextFieldWidget field(int x, int y, int width, Text label) {
        TextFieldWidget field = new TextFieldWidget(font, x + 3, y + 3, width - 6, 10, label);
        field.setDrawsBackground(false);
        return field;
    }

    static String searchKey(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').strip();
    }

    private void filter(boolean queryChanged) {
        if (discoveryRevision != discoveries.revision()) {
            discoveryRevision = discoveries.revision();
            searchMaterials.clear();
            discoveredRecipes = allRecipes.stream()
                    .filter(row -> RecipeDiscovery.isDiscovered(row.entry.value(), discoveries))
                    .toList();
            if (categories != null) categories.setDiscovered(discoveredRecipes.stream().map(RecipeRow::category));
        }
        String query = searchKey(search.getText());
        var candidates = discoveredRecipes.stream();
        if (categories != null) candidates = candidates.filter(row -> categories.includes(row.category));
        if (!query.isEmpty()) {
            String[] terms = query.split("\\s+");
            boolean qualified = query.contains(":");
            var namedMaterials = searchIngredients ? findMaterials(query, terms, qualified) : Map.<Item, Integer>of();
            candidates = candidates
                    .map(row -> new SearchMatch(row, searchIngredients ? materialRank(row, namedMaterials)
                            : searchRank(row.nameKey, query, terms, qualified ? row.idKey : row.pathKey)))
                    .filter(match -> match.rank != Integer.MAX_VALUE)
                    .sorted(Comparator.comparingInt(SearchMatch::rank))
                    .map(SearchMatch::row);
        }
        filtered = candidates.filter(row -> !availableOnly || storage != null && materialCount(row) > 0).toList();
        if (queryChanged) scroll = 0;
        if ((queryChanged && !query.isEmpty()) || selected == null || !filtered.contains(selected)) {
            RecipeRow first = filtered.isEmpty() ? null : filtered.getFirst();
            if (first != selected) select(first);
        }
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    private Map<Item, Integer> findMaterials(String query, String[] terms, boolean qualified) {
        Map<Item, Integer> matches = new HashMap<>();
        for (Item item : Registries.ITEM) {
            if (!discoveries.contains(item)) continue;
            var id = Registries.ITEM.getId(item);
            int score = searchRank(searchKey(item.getName().getString()), query, terms,
                    searchKey(qualified ? id.toString() : id.getPath()));
            if (score < Integer.MAX_VALUE) matches.put(item, score);
        }
        return matches;
    }

    private int materialRank(RecipeRow row, Map<Item, Integer> matches) {
        if (matches.isEmpty()) return Integer.MAX_VALUE;
        var test = searchMaterials.computeIfAbsent(row, key -> RecipeDiscovery.materialTest(key.entry.value()));
        int best = Integer.MAX_VALUE;
        for (var entry : matches.entrySet()) {
            if (entry.getValue() < best && test.test(entry.getKey())) best = entry.getValue();
            if (best == 0) break;
        }
        return best;
    }

    static int searchRank(String nameKey, String query, String[] terms, String recipeKey) {
        if (nameKey.equals(query) || recipeKey.equals(query)) return 0;
        if (nameKey.startsWith(query)) return 1;
        boolean namePrefixes = true;
        boolean allInName = true;
        for (String term : terms) {
            int position = nameKey.indexOf(term);
            if (position < 0 && !recipeKey.contains(term)) return Integer.MAX_VALUE;
            if (position < 0) allInName = false;
            while (position > 0 && !Character.isWhitespace(nameKey.charAt(position - 1))) {
                position = nameKey.indexOf(term, position + 1);
            }
            if (position < 0) namePrefixes = false;
        }
        if (namePrefixes) return 2;
        return allInName ? 3 : 4;
    }

    private void select(RecipeRow row) {
        selected = row;
        sources.clear();
        picking = false;
        feedback = null;
        invalidateSelection();
    }

    private void invalidateSelection() {
        selection = null;
        assessedAmount = -1;
        amountFits = false;
        loadInput = ItemStack.EMPTY;
    }

    private void update() {
        boolean refresh = discoveryRevision != discoveries.revision();
        if (storage == null || !storage.isCurrent()) {
            var next = new MaterialCrafting.Storage(inventory);
            if (storage != null && sources.stream().anyMatch(slot ->
                    !ItemStack.areItemsAndComponentsEqual(storage.stackAtSlot(slot), next.stackAtSlot(slot)))) sources.clear();
            storage = next;
            counts.clear();
            buildIngredientRows();
            invalidateSelection();
            refresh |= availableOnly;
        }
        if (refresh) filter(false);
        if (furnace != null) {
            updateFurnace();
            return;
        }
        if (selected == null) return;
        if (selection == null) {
            selection = new MaterialCrafting.Selection(storage, selected.entry, world, grid, sources);
            output = selection.preview();
            materialLimit = output.isEmpty() ? 0 : selection.materialLimit();
            fit = selection.fittingLimit(materialLimit);
        }
        int amount = amount();
        if (amount != assessedAmount) {
            assessedAmount = amount;
            amountFits = amount > 0 && amount <= fit && (amount == fit || selection.plan(amount) != null);
        }
    }

    private int materialCount(RecipeRow row) {
        return counts.computeIfAbsent(row, recipe -> {
            if (furnace != null) return FurnaceLoading.available(storage, recipe.cooking(), world);
            if (recipe.special()) return SpecialCraftingMaterials.isAvailable(storage, recipe.crafting(), world, grid) ? 1 : 0;
            return new MaterialCrafting.Selection(storage, recipe.entry, world, grid, List.of()).materialLimit();
        });
    }

    private void updateFurnace() {
        updateFurnaceInput();
        var input = furnace.getSlot(LoadoutFurnaceHandler.INPUT);
        if (selected == null) return;
        int amount = amount();
        if (amount == assessedAmount) return;
        assessedAmount = amount;
        output = selected.icon;
        materialLimit = materialCount(selected);
        fit = FurnaceLoading.limit(storage, selected.cooking(), input, world);
        if (amount > 0 && amount <= fit) loadInput = FurnaceLoading.chooseInput(storage, furnace, selected.entry, world, amount);
        else loadInput = ItemStack.EMPTY;
        amountFits = !loadInput.isEmpty();
    }

    private void updateFurnaceInput() {
        ItemStack loaded = furnace.getSlot(LoadoutFurnaceHandler.INPUT).getStack();
        if (ItemStack.areEqual(loaded, lastFurnaceInput)) return;
        lastFurnaceInput = loaded.copy();
        expectedFurnaceOutput = world.getRecipeManager().getFirstMatch(furnace.recipeType(),
                new SingleStackRecipeInput(loaded), world)
                .map(entry -> GearSalvage.furnaceOutput(entry, loaded, world.getRegistryManager())).orElse(ItemStack.EMPTY);
        invalidateSelection();
    }

    private void buildIngredientRows() {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < storage.size(); i++) {
            if (!storage.stack(i).isEmpty() && (!(selected != null && MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.CartographyRecipe)
                    || storage.stack(i).isOf(Items.FILLED_MAP))) rows.add(i);
        }
        ingredientRows = rows;
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    void render(DrawContext context, int mx, int my, float delta) {
        if (salvaging && furnace != null) updateFurnaceInput();
        if (!salvaging) update();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        if (!stationIcon.isEmpty()) context.drawItem(stationIcon, x + PAD, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,title,x+PAD+(stationIcon.isEmpty()?0:21),y+11,
                width-PAD*2-(stationIcon.isEmpty()?0:21)-(hasSalvageTab()?66:0),TEXT,
                com.cranks.dungeons.client.guide.GuideReferences.station(stationIcon.isEmpty()?"hand_crafting":net.minecraft.registry.Registries.ITEM.getId(stationIcon.getItem()).getPath()));
        InventoryStyle.drawDivider(context, x + PAD, x + width - PAD, y + 24);
        context.fill(divider, y + 29, divider + 1, y + height - PAD, 0xFF3A3A3A);
        if (hasSalvageTab()) {
            String tab = !salvaging ? "salvage" : furnace == null ? "tab" : "recipes";
            drawButton(context, message(tab), salvageTabX, y + 7, 61, 15, mx, my, true);
        }
        if (salvaging) {
            salvage.render(context, mx, my);
            if (furnace != null) renderFurnace(context, divider + 6, mx, my);
            return;
        }
        renderList(context, mx, my, delta);
        renderDetails(context, mx, my, delta);
    }

    private void renderList(DrawContext context, int mx, int my, float delta) {
        if (picking) {
            drawButton(context, message("back"), x + PAD, y + 30, divider - x - PAD - 5, 15, mx, my, true);
        } else {
            CompendiumStyle.drawTextFieldFrame(context, x + PAD, y + 30, divider - x - PAD - 5, 15, search.isFocused());
            search.render(context, mx, my, delta);
            drawFilter(context, message(searchIngredients ? "input_mode" : "result_mode"),
                    x + PAD, searchModeWidth, mx, my, searchIngredients);
            drawFilter(context, message("available_only"), stockButtonX(), stockButtonWidth(), mx, my, availableOnly);
            if (categories != null) categories.render(context, x + PAD, y + LIST_TOP, categoryWidth(), mx, my);
        }
        StatsScrollbar bar = scrollbar();
        int left = x + PAD, right = divider - 8;
        if (listSize() == 0) drawLine(context, message(picking ? "empty_storage" : "no_recipes").getString(),
                left, bar.top() + 4, right - left, MUTED);
        context.enableScissor(left - 1, bar.top(), right + 1, bar.top() + bar.height());
        int first = scroll / ROW;
        for (int i = first; i < listSize() && (i * ROW - scroll) < bar.height(); i++) {
            int rowY = bar.top() + i * ROW - scroll;
            boolean hover = mx >= left && mx < right && my >= bar.top() && my < bar.top() + bar.height()
                    && my >= rowY && my < rowY + ROW;
            if (picking) {
                ItemStack stack = storage.stack(ingredientRows.get(i));
                drawRow(context, stack, stack.getName().getString(), String.valueOf(stack.getCount()), rowY, hover, false);
            } else {
                RecipeRow row = filtered.get(i);
                int count = row.special() ? 0 : materialCount(row);
                drawRow(context, row.icon, row.name, row.special() ? "" : Integer.toString(count), rowY, hover, selected == row);
            }
        }
        context.disableScissor();
        if (bar.maxScroll() > 0) {
            int top = (int) Math.round(bar.thumbTop(scroll));
            context.fill(divider - 6, bar.top(), divider - 3, bar.top() + bar.height(), 0xFF090909);
            context.fill(divider - 6, top, divider - 3, top + bar.thumbHeight(), grabbed ? 0xFFA0A0A0 : 0xFF737373);
        }
    }

    private void drawRow(DrawContext context, ItemStack icon, String name, String count, int y, boolean hovered, boolean active) {
        int left = x + PAD, right = divider - 8;
        CompendiumStyle.drawRailPlaque(context, left, y + 1, right - left, ROW - 2, active, hovered);
        context.drawItem(icon, left + 2, y + 2);
        int countWidth = font.getWidth(count);
        drawLine(context, name, left + 21, y + 6, right - left - 27 - countWidth, TEXT);
        if (!count.isEmpty()) context.drawText(font, count, right - countWidth - 3, y + 6,
                count.equals("0") ? MUTED : SUCCESS, false);
    }

    private void renderDetails(DrawContext context, int mx, int my, float delta) {
        int left = divider + 6;
        int footer = y + height - 49;
        if (selected != null) {
            ItemStack icon = output.isEmpty() ? selected.icon : output;
            boolean tailoringPreview = MaterialCrafting.unwrap(selected.entry.value()) instanceof net.minecraft.recipe.ArmorDyeRecipe
                    || MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe;
            int previewSize = tailoringPreview ? 28 : 20;
            CompendiumStyle.drawRailPlaque(context, left, y + 32, previewSize, previewSize, false, false);
            if (tailoringPreview) drawSmallItem(context, icon, left + 2, y + 34, 25);
            else context.drawItem(icon, left + 2, y + 34);
            int textOffset = previewSize + 5;
            drawLine(context, output.isEmpty() || MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.CartographyRecipe
                            ? selected.name : recipeResultName(output),
                    left + textOffset, y + 34, actionWidth() - textOffset, TEXT);
            if (!output.isEmpty()) context.drawText(font, message("makes", output.getCount()), left + textOffset, y + 46, MUTED, false);
            if (selected.special()) {
                renderSpecial(context, left, mx, my);
            } else {
                context.drawText(font, message("materials"), left, y + 60, MUTED, false);
                Text units = message("have_need");
                context.drawText(font, units, left + actionWidth() - font.getWidth(units), y + 60, MUTED, false);
                int rowY = y + 71;
                int pitch = materialPitch();
                for (var material : selectedMaterials()) {
                    ItemStack example = example(material);
                    if (example.isEmpty()) {
                        rowY += pitch;
                        continue;
                    }
                    drawSmallItem(context, example, left, rowY, pitch);
                    int needed = material.perCraft() * Math.max(1, amount());
                    String count = material.available() + "/" + needed;
                    int countWidth = font.getWidth(count);
                    drawLine(context, example.getName().getString(), left + 13, rowY + 1, actionWidth() - countWidth - 17, TEXT);
                    context.drawText(font, count, left + actionWidth() - countWidth, rowY + 1,
                            needed <= material.available() ? SUCCESS : WARNING, false);
                    rowY += pitch;
                }
            }
        }
        if (furnace != null) renderFurnace(context, left, mx, my);
        Text status = getStatus();
        boolean recent = feedback != null && System.currentTimeMillis() < feedbackUntil;
        drawLine(context, status.getString(), left, furnace == null ? footer - 23 : y + 85, actionWidth(),
                recent ? (feedbackGood ? SUCCESS : WARNING) : amountFits ? SUCCESS : MUTED);
        renderBatchPreview(context, left, footer - 12);
        drawButton(context, Text.literal("−"), left, footer, 20, 15, mx, my, selected != null && request == 0);
        drawButton(context, Text.literal("+"), left + actionWidth() - 20, footer, 20, 15, mx, my, selected != null && request == 0);
        CompendiumStyle.drawTextFieldFrame(context, left + 24, footer, actionWidth() - 48, 15, quantity.isFocused());
        if (quantity.isFocused()) quantity.render(context, mx, my, delta);
        else context.drawCenteredTextWithShadow(font, quantity.getText(), left + actionWidth() / 2, footer + 4, TEXT);
        String action = anvil ? "forge" : cutting && (selected == null || selected.entry.value().getType() == RecipeType.STONECUTTING)
                ? "cut" : "craft";
        Text craft = furnace == null ? amount() > 1 ? message(action + "_many", amount()) : message(action) : loadButton();
        drawButton(context, craft, left, footer + 20, actionWidth(), 19, mx, my, amountFits && request == 0);
    }

    private BatchPreview batchPreview() {
        int amount = amount();
        if (selected == null || output.isEmpty() || amount == 0) return null;
        ItemStack input = loadInput;
        int perCraft = 1;
        if (input.isEmpty() && !selected.special()) {
            var materials = selectedMaterials();
            if (materials.size() == 1 && materials.getFirst().examples().size() == 1) {
                var material = materials.getFirst();
                input = example(material);
                perCraft = material.perCraft();
            }
        }
        return new BatchPreview(input, (long) perCraft * amount, output, (long) output.getCount() * amount);
    }

    private void renderBatchPreview(DrawContext context, int left, int top) {
        BatchPreview preview = batchPreview();
        if (preview == null) {
            if (furnace != null) drawLine(context, message("load_amount").getString(), left, top + 1, actionWidth(), MUTED);
            return;
        }
        String inputCount = Long.toString(preview.inputCount);
        String outputCount = Long.toString(preview.outputCount);
        String arrow = "→";
        int inputWidth = font.getWidth(inputCount);
        int arrowWidth = font.getWidth(arrow);
        int totalWidth = inputWidth + arrowWidth + font.getWidth(outputCount) + 35;
        int cursor = left;
        if (!preview.input.isEmpty() && totalWidth <= actionWidth()) {
            drawSmallItem(context, preview.input, cursor, top, 12);
            context.drawText(font, inputCount, cursor + 13, top + 1, TEXT, false);
            cursor += inputWidth + 17;
            context.drawText(font, arrow, cursor, top + 1, MUTED, false);
            cursor += arrowWidth + 4;
        } else {
            Text total = message("batch_output");
            context.drawText(font, total, cursor, top + 1, MUTED, false);
            cursor += font.getWidth(total) + 4;
        }
        drawSmallItem(context, preview.output, cursor, top, 12);
        context.drawText(font, outputCount, cursor + 13, top + 1, amountFits ? SUCCESS : MUTED, false);
    }

    private Text loadButton() {
        ItemStack named = loadInput;
        if (named.isEmpty() && selected != null) named = example(selectedMaterials().getFirst());
        if (!named.isEmpty() && amount() > 0) {
            Text full = message("load_item", amount(), named.getName());
            if (font.getWidth(full) <= actionWidth() - 8) return full;
        }
        return amount() > 1 ? message("load_many", amount()) : message("load");
    }


    private List<MaterialCrafting.Material> selectedMaterials() {
        if (furnace == null) return selection.materials();
        var inputs = selected.cooking().getIngredients();
        return List.of(new MaterialCrafting.Material(List.of(inputs.getFirst().getMatchingStacks()), 1, materialLimit));
    }

    int slotX(int index) {
        return divider + 16 + (index - LoadoutFurnaceHandler.INPUT) * (actionWidth() - 36) / 2;
    }

    int slotY() {
        return y + FURNACE_SLOT_Y;
    }

    private void renderFurnace(DrawContext context, int left, int mx, int my) {
        for (int id = LoadoutFurnaceHandler.INPUT; id <= LoadoutFurnaceHandler.OUTPUT; id++) {
            int sx = slotX(id);
            String label = id == LoadoutFurnaceHandler.INPUT ? "input" : id == LoadoutFurnaceHandler.FUEL ? "fuel" : "output";
            context.drawCenteredTextWithShadow(font, message(label), sx + 8, slotY() - 13, MUTED);
            InventoryStyle.drawSocket(context, sx - 3, slotY() - 3, 22, 22,
                    inside(mx, my, sx, slotY(), 16, 16));
        }
        var resultSlot = furnace.getSlot(LoadoutFurnaceHandler.OUTPUT);
        ItemStack result = resultSlot.getStack();
        boolean blocked = !result.isEmpty() && !expectedFurnaceOutput.isEmpty()
                && (!ItemStack.areItemsAndComponentsEqual(result, expectedFurnaceOutput)
                    || result.getCount() + expectedFurnaceOutput.getCount() > resultSlot.getMaxItemCount(expectedFurnaceOutput));
        ItemStack fuel = furnace.getSlot(LoadoutFurnaceHandler.FUEL).getStack();
        String working = switch (furnace.station()) {
            case FURNACE -> "furnace_smelting";
            case BLAST_FURNACE -> salvaging ? "furnace_recycling" : "furnace_smelting";
            case SMOKER -> "furnace_cooking";
        };
        String status = lastFurnaceInput.isEmpty() ? "furnace_idle" : expectedFurnaceOutput.isEmpty() ? "furnace_invalid"
                : blocked ? "furnace_full" : furnace.burning()
                    || net.minecraft.block.entity.AbstractFurnaceBlockEntity.canUseAsFuel(fuel) ? working : "furnace_no_fuel";
        drawLine(context, message(status).getString(), left, y + 136, actionWidth(), blocked ? WARNING : MUTED);
        drawProgress(context, left, y + 148, actionWidth(), furnace.cookProgress(), 0xFFB9B9B9);
        drawProgress(context, left, y + 159, actionWidth(), furnace.fuelProgress(), 0xFFBB8951);
    }

    private static void drawProgress(DrawContext context, int x, int y, int width, float amount, int color) {
        CompendiumStyle.drawRailPlaque(context, x, y, width, 7, false, false);
        int length = Math.round((width - 4) * amount);
        if (length > 0) context.fill(x + 2, y + 2, x + 2 + length, y + 5, color);
    }

    private void renderSpecial(DrawContext context, int left, int mx, int my) {
        var recipe = MaterialCrafting.unwrap(selected.entry.value());
        if (recipe instanceof com.cranks.dungeons.inventory.CartographyRecipe operation) {
            renderCartography(context, left, mx, my, operation);
            return;
        }
        Text choose = message(recipe instanceof MapExtendingRecipe ? "choose_map"
                : recipe instanceof TippedArrowRecipe ? "choose_potion" : "choose");
        drawButton(context, choose, left, y + 62, actionWidth(), 15, mx, my, !picking && request == 0);
        if (recipe instanceof net.minecraft.recipe.ArmorDyeRecipe
                || recipe instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe) {
            String hint = recipe instanceof net.minecraft.recipe.ArmorDyeRecipe ? "tailoring_dye_hint"
                    : ((com.cranks.dungeons.inventory.TailoringPatternRecipe) recipe).template().isEmpty()
                    ? "tailoring_pattern_hint" : "tailoring_template_hint";
            drawLine(context, message(hint).getString(), left, y + 78, actionWidth(), MUTED);
        }
        int sy = specialSourceY();
        for (int i = 0; i < sources.size(); i++) {
            ItemStack stack = inventory.getStack(sources.get(i));
            int sx = left + i % 5 * 19;
            int itemY = sy + i / 5 * 19;
            CompendiumStyle.drawRailPlaque(context, sx, itemY, 17, 17, false, false);
            if (!stack.isEmpty()) context.drawItem(stack, sx, itemY);
        }
        if (recipe instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe pattern && !pattern.template().isEmpty()) {
            drawLine(context, message("tailoring_template_reusable").getString(), left, sy + 24, actionWidth(), MUTED);
        }
        if (!sources.isEmpty()) {
            drawButton(context, message("undo"), left, sy + 43, actionWidth(), 14, mx, my, request == 0);
        }
        if (recipe instanceof MapExtendingRecipe || recipe instanceof TippedArrowRecipe) {
            Text needs = message(recipe instanceof MapExtendingRecipe ? "paper_needed" : "arrows_needed");
            drawLine(context, needs.getString(), left, sy + 62, actionWidth(), MUTED);
        }
    }

    private int specialSourceY() {
        var recipe = selected == null ? null : MaterialCrafting.unwrap(selected.entry.value());
        return y + (recipe instanceof net.minecraft.recipe.ArmorDyeRecipe
                || recipe instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe ? 92 : 86);
    }

    private void renderCartography(DrawContext context, int left, int mx, int my,
                                   com.cranks.dungeons.inventory.CartographyRecipe recipe) {
        drawButton(context, message("choose_map"), left, y + 62, actionWidth(), 15, mx, my, !picking && request == 0);
        ItemStack material = recipe.operation().material().getDefaultStack();
        int available = 0;
        for (int i = 0; i < storage.size(); i++) if (storage.stack(i).isOf(material.getItem())) available += storage.stack(i).getCount();
        drawSmallItem(context, material, left, y + 82, 12);
        drawLine(context, available + "/" + Math.max(1, amount()) + " " + material.getName().getString(),
                left + 14, y + 83, actionWidth() - 14, available >= Math.max(1, amount()) ? SUCCESS : WARNING);
        ItemStack map = sources.isEmpty() ? ItemStack.EMPTY : inventory.getStack(sources.getFirst());
        var state = net.minecraft.item.FilledMapItem.getMapState(map, world);
        CompendiumStyle.drawRailPlaque(context, left, y + 98, 54, 54, false, false);
        if (state == null) {
            StationSlotHints.draw(context, StationSlotHints.Kind.MAP, left + 19, y + 117);
            return;
        }
        var id = map.get(net.minecraft.component.DataComponentTypes.MAP_ID);
        context.getMatrices().push();
        context.getMatrices().translate(left + 2, y + 100, 1);
        context.getMatrices().scale(50 / 128f, 50 / 128f, 1);
        net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(
                context.getMatrices(), context.getVertexConsumers(), id, state, true, 15728880);
        context.getMatrices().pop();
        context.draw();
        int after = recipe.operation() == com.cranks.dungeons.inventory.CartographyRecipe.Operation.EXPAND && !output.isEmpty()
                ? Math.min(4, state.scale + 1) : state.scale;
        int textX = left + 59, textWidth = actionWidth() - 59;
        drawLine(context, "#" + id.id(), textX, y + 100, textWidth, MUTED);
        drawLine(context, "1:" + (1 << state.scale) + (after != state.scale ? " → " + (1 << after) : ""), textX, y + 112, textWidth, TEXT);
        drawLine(context, (128 << after) + "×" + (128 << after), textX, y + 124, textWidth, TEXT);
        drawLine(context, message(recipe.operation() == com.cranks.dungeons.inventory.CartographyRecipe.Operation.COPY
                ? "map_original_kept" : recipe.operation() == com.cranks.dungeons.inventory.CartographyRecipe.Operation.LOCK || state.locked
                ? "map_locked" : "map_unlocked").getString(), textX, y + 136, textWidth, MUTED);
    }

    private int materialPitch() {
        return furnace != null ? 12 : Math.min(12, (height - 134) / Math.max(1, selection.materials().size()));
    }

    private static void drawSmallItem(DrawContext context, ItemStack stack, int x, int y, int pitch) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        float scale = (pitch - 1) / 16f;
        context.getMatrices().scale(scale, scale, 1);
        context.drawItem(stack, 0, 0);
        context.getMatrices().pop();
    }

    private ItemStack example(MaterialCrafting.Material material) {
        var examples = material.examples();
        if (examples.isEmpty()) return ItemStack.EMPTY;
        int start = (int) ((System.currentTimeMillis() / 1800) % examples.size());
        for (int i = 0; i < examples.size(); i++) {
            ItemStack candidate = examples.get((start + i) % examples.size());
            if (discoveries.contains(candidate.getItem())) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private Text getStatus() {
        if (request != 0) return message(furnace == null ? "working" : "loading");
        if (feedback != null && System.currentTimeMillis() < feedbackUntil) return feedback;
        if (selected == null) return Text.empty();
        if (MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.CartographyRecipe recipe && !sources.isEmpty()) {
            var mapState = net.minecraft.item.FilledMapItem.getMapState(inventory.getStack(sources.getFirst()), world);
            if (mapState == null) return message("map_missing_data");
            if (recipe.operation() != com.cranks.dungeons.inventory.CartographyRecipe.Operation.COPY && mapState.locked) return message("map_already_locked");
            if (recipe.operation() == com.cranks.dungeons.inventory.CartographyRecipe.Operation.EXPAND && mapState.scale >= 4) return message("map_max_scale");
        }
        if (output.isEmpty()) return message("choose_hint");
        if (materialLimit == 0) return message("missing");
        if (furnace != null) return fit > 0 ? message("load_max", fit) : message("input_unavailable");
        if (fit == 0) return message("no_room");
        if (amount() > 0 && amount() <= fit && !amountFits) return message("smaller_no_room", fit);
        return message("max", fit);
    }

    private void drawLine(DrawContext context, String value, int x, int y, int width, int color) {
        if (width <= 0) return;
        String visible = font.getWidth(value) <= width ? value : font.trimToWidth(value, Math.max(0, width - 6)) + "…";
        context.drawText(font, visible, x, y, color, false);
    }

    static void drawButton(DrawContext context, Text label, int x, int y, int w, int h, int mx, int my, boolean active) {
        CompendiumButtonWidget.drawButton(context, x, y, w, h, inside(mx, my, x, y, w, h), active, label.getString());
    }

    private int stockButtonX() { return x + PAD + searchModeWidth + FILTER_GAP; }

    private int stockButtonWidth() { return divider - stockButtonX() - 5; }

    private void drawFilter(DrawContext context, Text label, int left, int width, int mx, int my, boolean selected) {
        int textWidth = width - 2 * FILTER_PADDING;
        String text = label.getString();
        if (font.getWidth(text) > textWidth) {
            String ellipsis = "…";
            text = font.trimToWidth(text, Math.max(0, textWidth - font.getWidth(ellipsis))) + ellipsis;
        }
        CompendiumButtonWidget.drawButton(context, left, y + FILTER_Y, width, FILTER_HEIGHT,
                inside(mx, my, left, y + FILTER_Y, width, FILTER_HEIGHT), true, selected, text);
    }

    void tooltip(DrawContext context, int mx, int my) {
        if (!contains(mx, my)) return;
        if (salvaging) {
            salvage.tooltip(context, mx, my);
            return;
        }
        if (!picking && inside(mx, my, x + PAD, y + FILTER_Y, searchModeWidth, FILTER_HEIGHT)) {
            context.drawTooltip(font, message("toggle_search_hint"), mx, my);
            return;
        }
        if (!picking && inside(mx, my, stockButtonX(), y + FILTER_Y, stockButtonWidth(), FILTER_HEIGHT)) {
            context.drawTooltip(font, message("available_hint"), mx, my);
            return;
        }
        if (!picking && categories != null
                && categories.tooltip(context, font, x + PAD, y + LIST_TOP, categoryWidth(), mx, my)) return;
        if (selected == null) return;
        if (MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.CartographyRecipe recipe
                && inside(mx, my, divider + 6, y + 98, actionWidth(), 54)) {
            context.drawOrderedTooltip(font, font.wrapLines(message("map_" + recipe.operation().asString() + "_hint"), 210), mx, my);
            return;
        }
        if (inside(mx, my, divider + 6, y + height - 61, actionWidth(), 12)) {
            BatchPreview preview = batchPreview();
            if (preview != null) context.drawTooltip(font, preview.tooltip(), mx, my);
            return;
        }
        int previewSize = MaterialCrafting.unwrap(selected.entry.value()) instanceof net.minecraft.recipe.ArmorDyeRecipe
                || MaterialCrafting.unwrap(selected.entry.value()) instanceof com.cranks.dungeons.inventory.TailoringPatternRecipe ? 28 : 20;
        if (mx >= divider + 6 && mx < divider + 6 + previewSize && my >= y + 32 && my < y + 32 + previewSize) {
            ItemStack icon = output.isEmpty() ? selected.icon : output;
            if (!icon.isEmpty()) context.drawItemTooltip(font, icon, mx, my);
        } else if (mx < divider - 8 && my >= listTop() && my < y + height - PAD) {
            int row = (my - listTop() + scroll) / ROW;
            if (row < listSize()) {
                if (picking) context.drawItemTooltip(font, storage.stack(ingredientRows.get(row)), mx, my);
                else if (mx < x + PAD + 20 && !filtered.get(row).special()) context.drawItemTooltip(font, filtered.get(row).icon, mx, my);
                else if (font.getWidth(filtered.get(row).name) > divider - x - 42) {
                    context.drawTooltip(font, com.cranks.dungeons.client.guide.GuideText.link(filtered.get(row).name,
                            new com.cranks.dungeons.client.guide.GuideTarget(com.cranks.dungeons.client.guide.GuideTarget.Kind.RECIPE,filtered.get(row).entry.id().toString())), mx, my);
                }
            }
        } else if (!selected.special() && mx >= divider + 6 && my >= y + 71 && my < y + height - 61) {
            int index = (my - y - 71) / materialPitch();
            var materials = selectedMaterials();
            if (index < materials.size()) {
                ItemStack stack = example(materials.get(index));
                if (!stack.isEmpty()) context.drawItemTooltip(font, stack, mx, my);
            }
        }
    }

    boolean mouseClicked(double mx, double my, int button, Consumer<TextFieldWidget> focus) {
        if (!contains(mx, my)) return false;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
        if (hasSalvageTab() && inside(mx, my, salvageTabX, y + 7, 61, 15)) {
            salvaging = !salvaging;
            focus.accept(null);
            return true;
        }
        if (salvaging) {
            focus.accept(null);
            salvage.click(mx, my);
            return true;
        }
        capturing = true;
        if (!picking && inside(mx, my, x + PAD, y + 30, divider - x - PAD - 5, 15)) {
            focus.accept(search);
            search.mouseClicked(mx, my, button);
            return true;
        }
        int left = divider + 6, footer = y + height - 49;
        if (inside(mx, my, left + 22, footer, actionWidth() - 44, 15)) {
            focus.accept(quantity);
            quantity.mouseClicked(mx, my, button);
            return true;
        }
        focus.accept(null);
        if (picking && inside(mx, my, x + PAD, y + 30, divider - x - PAD - 5, 15)) {
            togglePicker();
        } else if (!picking && inside(mx, my, x + PAD, y + FILTER_Y, searchModeWidth, FILTER_HEIGHT)) {
            searchIngredients = !searchIngredients;
            search.setPlaceholder(message(searchIngredients ? "search_material" : "search"));
            filter(true);
        } else if (!picking && inside(mx, my, stockButtonX(), y + FILTER_Y, stockButtonWidth(), FILTER_HEIGHT)) {
            availableOnly = !availableOnly;
            filter(true);
        } else if (!picking && categories != null && categories.click(x + PAD, y + LIST_TOP, categoryWidth(), mx, my)) {
            filter(true);
        } else if (inside(mx, my, divider - 7, listTop(), 6, scrollbar().height()) && scrollbar().maxScroll() > 0) {
            grabbed = true;
            grab = scrollbar().grabOffset(my, scroll);
            scroll = scrollbar().scrollAt(my, grab);
        } else if (inside(mx, my, x + PAD, listTop(), divider - x - PAD - 8, scrollbar().height())) {
            int i = ((int) my - listTop() + scroll) / ROW;
            if (i < listSize() && request == 0) {
                if (picking && selected.crafting().value() instanceof com.cranks.dungeons.inventory.CartographyRecipe) {
                    sources.clear();
                    sources.add(storage.slot(ingredientRows.get(i)));
                    picking = false;
                    scroll = 0;
                    invalidateSelection();
                } else if (picking && sources.size() < MaterialCrafting.choiceLimit(selected.crafting().value(), grid)) {
                    sources.add(storage.slot(ingredientRows.get(i)));
                    invalidateSelection();
                } else if (!picking) {
                    select(filtered.get(i));
                }
            }
        } else if (selected != null && selected.special() && request == 0
                && inside(mx, my, left, y + 62, actionWidth(), 15) && !picking) {
            togglePicker();
        } else if (selected != null && selected.special() && request == 0 && !sources.isEmpty()
                && !(selected.crafting().value() instanceof com.cranks.dungeons.inventory.CartographyRecipe)
                && inside(mx, my, left, specialSourceY() + 43, actionWidth(), 14)) {
            sources.removeLast();
            invalidateSelection();
        } else if (selected != null && request == 0 && inside(mx, my, left, footer, 20, 15)) {
            step(-1);
        } else if (selected != null && request == 0 && inside(mx, my, left + actionWidth() - 20, footer, 20, 15)) {
            step(1);
        } else if (inside(mx, my, left, footer + 20, actionWidth(), 19)) {
            submit();
        }
        return true;
    }

    private void togglePicker() {
        picking = !picking;
        if (picking) buildIngredientRows();
        scroll = 0;
    }

    private void step(int by) {
        quantity.setText(Integer.toString(Math.clamp(amount() + by, 1, MaterialCrafting.MAX_BATCH)));
        feedback = null;
    }

    void submit() {
        if (salvaging) return;
        update();
        if (!amountFits || request != 0 || selected == null) return;
        request = nextRequest();
        List<CraftMaterialsPayload.Source> choices = sources.stream().map(slot ->
                new CraftMaterialsPayload.Source(slot, storage.stackAtSlot(slot).copyWithCount(1))).toList();
        ClientPlayNetworking.send(new CraftMaterialsPayload(syncId, request, selected.entry.id(), amount(), choices));
    }

    static int nextRequest() {
        if (++requestSequence == 0) ++requestSequence;
        return requestSequence;
    }

    void startSalvageRequest() {
        salvaging = true;
        salvage.setPending();
    }

    void receiveSalvage(int result) {
        salvage.receive(result);
    }

    private boolean hasSalvageTab() {
        return salvage != null && (furnace == null || !allRecipes.isEmpty());
    }

    void receive(CraftMaterialsResultPayload response) {
        if (request == 0 || response.request() != request) return;
        request = 0;
        feedbackGood = response.result() > 0;
        feedback = switch (response.result()) {
            case CraftMaterialsResultPayload.LOCKED -> message("locked");
            case CraftMaterialsResultPayload.UNAVAILABLE -> message("unavailable");
            case CraftMaterialsResultPayload.INVENTORY_CHANGED -> message("changed");
            default -> message(furnace == null ? "success" : "loaded", response.result());
        };
        feedbackUntil = System.currentTimeMillis() + 2800;
        invalidateSelection();
    }

    boolean mouseScrolled(double mx, double my, double vertical) {
        if (!contains(mx, my)) return false;
        if (salvaging) {
            salvage.scroll(mx, vertical);
            return true;
        }
        if (mx < divider) scroll = Math.clamp(scroll - (int) (vertical * ROW * 2), 0, scrollbar().maxScroll());
        return true;
    }

    boolean mouseDragged(double mx, double my, int button, double dx, double dy, Element focus) {
        if (salvaging) return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && salvage.drag(my);
        if (!capturing || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (grabbed) scroll = scrollbar().scrollAt(my, grab);
        else if (owns(focus)) focus.mouseDragged(mx, my, button, dx, dy);
        return true;
    }

    boolean mouseReleased(int button) {
        if (salvaging) return button == GLFW.GLFW_MOUSE_BUTTON_LEFT && salvage.release();
        if (!capturing || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        capturing = false;
        grabbed = false;
        return true;
    }

    boolean owns(Element element) {
        return element != null && (element == search || element == quantity);
    }

    boolean quantityFocused(Element element) { return element != null && element == quantity; }

    boolean contains(double mx, double my) { return inside(mx, my, x, y, width, height); }

    private int actionWidth() { return x + width - PAD - divider - 6; }

    private int listSize() { return picking ? ingredientRows.size() : filtered.size(); }

    private int categoryWidth() { return divider - x - PAD - 5; }

    private int listTop() {
        return y + LIST_TOP + (picking || categories == null ? 0 : categories.height(categoryWidth()));
    }

    private int amount() { return quantity.getText().isEmpty() ? 0 : Integer.parseInt(quantity.getText()); }

    private StatsScrollbar scrollbar() {
        int top = listTop();
        return new StatsScrollbar(top, y + height - PAD - top, listSize() * ROW);
    }

    static boolean inside(double mx, double my, int x, int y, int width, int height) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }

    static Text message(String key, Object... args) {
        return Text.translatable("crafting.cranks-dungeons." + key, args);
    }
}

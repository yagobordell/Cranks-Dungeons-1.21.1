package com.cranks.dungeons.client;

import com.cranks.dungeons.alchemy.*;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.network.AlchemyPayload;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.screen.LoadoutAlchemyHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

/** The same station dock as crafting: no private inventories, cursor ingredients, or client-side crafting. */
final class AlchemyPanel {
    private record Row(String key, ItemStack icon, List<AlchemyRecipes.Need> needs) {}
    private record RecipeReturn(String key, String query, String quantity, int scroll, boolean craftable, boolean materialSearch) {}
    private enum Picker { NONE, MAIN, EXTRA, SUPPORT, BOTTLE }
    private record Box(int x, int y, int width, int height) {
        boolean contains(double mx, double my) { return MaterialCraftingPanel.inside(mx, my, x, y, width, height); }
    }
    private static final int PAD = 8, ROW = 26, TOP = 84;
    private static int sequence;
    private final LoadoutAlchemyHandler handler;
    private final PlayerInventory inventory;
    private TextRenderer font;
    private TextFieldWidget search, quantity;
    private MaterialCrafting.Storage storage;
    private List<Row> rows = List.of();
    private Row selected;
    private RecipeReturn returnRecipe;
    private Set<String> extractionTargets = Set.of();
    private int x, y, width, height, divider, tab, scroll, pending;
    private boolean craftable, materialSearch, capturing, dragging;
    private double grab;
    private String primary = "", secondary = "";
    private int suspension;
    private Picker picker = Picker.NONE;
    private boolean supportChosen, bottleChosen;
    private final String[] queries = {"", "", ""};
    private boolean lesser = true;
    private AlchemyItems.Delivery delivery = AlchemyItems.Delivery.DRINK;
    private Text feedback;
    private long feedbackUntil;
    private ItemStack lastResult = ItemStack.EMPTY, requestedResult = ItemStack.EMPTY;

    AlchemyPanel(LoadoutAlchemyHandler handler, PlayerInventory inventory) { this.handler = handler; this.inventory = inventory; }

    void init(TextRenderer font, int x, int y, int width, int height) {
        this.font = font; this.x = x; this.y = y; this.width = width; this.height = height;
        divider = x + width * 49 / 100;
        String query = search == null ? "" : search.getText();
        String count = quantity == null ? "1" : quantity.getText();
        search = field(x + PAD + 3, y + 51, leftWidth() - 6, text("search"));
        search.setMaxLength(80); search.setText(query);
        search.setChangedListener(value -> { scroll = 0; rebuild(); });
        positionSearch();
        quantity = field(right() + 22, bottom() - 37, rightWidth() - 44, text("quantity"));
        quantity.setMaxLength(5);
        quantity.setTextPredicate(value -> value.isEmpty() || value.matches("[0-9]{1,5}") && Integer.parseInt(value) <= MaterialCrafting.MAX_BATCH);
        quantity.setText(count);
        dragging = capturing = false;
        refresh();
    }

    private TextFieldWidget field(int x, int y, int width, Text label) {
        var field = new TextFieldWidget(font, x, y, width, 10, label);
        field.setDrawsBackground(false); field.setPlaceholder(label);
        return field;
    }
    private int leftWidth() { return divider - x - PAD - 5; }
    private int right() { return divider + 7; }
    private int rightWidth() { return x + width - PAD - right(); }
    private int craftableWidth() { return Math.max((leftWidth() - 3) / 2, font.getWidth(MaterialCraftingPanel.message("available_only")) + 6); }
    private int bottom() { return y + height - PAD; }
    private int listTop() { return y + (tab == 1 ? 91 : TOP); }
    private int listWidth() { return tab == 1 ? width - 2 * PAD : leftWidth(); }
    private int listRight() { return x + PAD + listWidth(); }
    private boolean hasIngredientPicker() { return picker == Picker.MAIN || picker == Picker.EXTRA; }
    boolean contains(double mx, double my) { return MaterialCraftingPanel.inside(mx, my, x, y, width, height); }
    boolean quantityFocused(Element focused) { return focused == quantity; }
    private static Text text(String key, Object... args) { return Text.translatable("alchemy.cranks-dungeons." + key, args); }

    private void refresh() {
        if (storage == null || !storage.isCurrent()) storage = new MaterialCrafting.Storage(inventory);
        rebuild();
    }

    private void rebuild() {
        if (storage == null || search == null) return;
        List<Row> choices = new ArrayList<>();
        if (tab == 0) {
            var discovered = ModComponents.ITEM_DISCOVERIES.get(inventory.player);
            for (var recipe : AlchemyRecipes.extractions()) {
                if ((extractionTargets.isEmpty() || extractionTargets.contains(recipe.key()))
                        && recipe.needs().stream().allMatch(need -> need.alternatives().stream().anyMatch(discovered::contains)))
                    choices.add(new Row(recipe.key(), recipe.output(), recipe.needs()));
            }
        } else if (tab == 1 && hasIngredientPicker()) {
            for (String name : AlchemyRecipes.EXTRACTS) {
                ItemStack icon = AlchemyItems.reagent(name).getDefaultStack();
                var need = new AlchemyRecipes.Need(List.of(icon.getItem()), 1, false);
                if (need.available(storage) > 0) choices.add(new Row(name, icon, List.of(need)));
            }
        } else if (tab == 2) {
            var known = ModComponents.ALCHEMY_KNOWLEDGE.get(inventory.player);
            for (var formula : AlchemyPotions.FORMULAS) if (known.knows(formula.key()))
                choices.add(new Row(formula.key(), formula.stack(delivery), AlchemyRecipes.needs(AlchemyRecipes.forFormula(formula, delivery))));
        }
        String query = MaterialCraftingPanel.searchKey(search.getText());
        choices.removeIf(row -> tab != 1 && craftable && AlchemyRecipes.materialLimit(storage, row.needs) == 0 || !matches(row, query));
        choices.sort(Comparator.comparingInt((Row row) -> query.isEmpty() ? 0 : rank(row.icon.getName().getString(), query, row.key))
                .thenComparing(row -> row.icon.getName().getString()));
        rows = choices;
        if (selected != null) selected = rows.stream().filter(row -> row.key.equals(selected.key)).findFirst().orElse(null);
        scroll = Math.clamp(scroll, 0, maxScroll());
    }

    private static int rank(String name, String query, String id) {
        return MaterialCraftingPanel.searchRank(MaterialCraftingPanel.searchKey(name), query, query.split("\\s+"), MaterialCraftingPanel.searchKey(id));
    }

    private boolean matches(Row row, String query) {
        if (query.isEmpty()) return true;
        if (tab != 1 && materialSearch) return row.needs.stream().flatMap(need -> need.alternatives().stream())
                .anyMatch(item -> rank(item.getName().getString(), query, net.minecraft.registry.Registries.ITEM.getId(item).getPath()) != Integer.MAX_VALUE);
        return rank(row.icon.getName().getString(), query, row.key) != Integer.MAX_VALUE
                || MaterialCraftingPanel.searchKey(row.key).contains(query);
    }

    private AlchemyRecipes.Experiment experiment() {
        return new AlchemyRecipes.Experiment(primary, secondary, AlchemyRecipes.SUSPENSIONS.get(suspension), lesser, delivery);
    }
    private boolean experimentReady() { return supportChosen && bottleChosen && AlchemyRecipes.valid(experiment()); }
    private List<AlchemyRecipes.Need> needs() {
        if (tab == 1) return experimentReady() ? AlchemyRecipes.needs(experiment()) : List.of();
        return selected == null ? List.of() : selected.needs;
    }
    private ItemStack output() {
        if (tab != 1) return selected == null ? ItemStack.EMPTY : selected.icon;
        if (!experimentReady()) return ItemStack.EMPTY;
        var formula = AlchemyRecipes.resolve(experiment());
        return formula == null ? AlchemyPotions.failed(experiment().key(), lesser, delivery) : formula.stack(delivery);
    }
    private int amount() { try { return tab == 1 ? 1 : Integer.parseInt(quantity.getText()); } catch (NumberFormatException e) { return 0; } }
    private int maxScroll() { return Math.max(0, rows.size() * ROW - (bottom() - listTop())); }

    void render(DrawContext context, int mx, int my, float delta) {
        refresh();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        context.drawItem(Items.BREWING_STAND.getDefaultStack(), x + PAD, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,text("title"),x+29,y+11,width-40,CompendiumStyle.TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("alchemy"));
        if (returnRecipe != null) button(context, text("back_recipe"), x + width - PAD - 57, y + 7, 57, 16, mx, my, pending == 0, false);
        int tw = (width - 2 * PAD) / 3;
        for (int i = 0; i < 3; i++) button(context, text(switch (i) { case 0 -> "extraction"; case 1 -> "experiment"; default -> "recipes"; }), x + PAD + i * tw, y + 28, tw - 2, 16, mx, my, pending == 0, tab == i);
        if (tab == 1) {
            if (picker == Picker.NONE) drawExperiment(context, mx, my);
            else drawPicker(context, mx, my, delta);
        } else {
            context.fill(divider, y + 48, divider + 1, bottom(), 0xFF393939);
            CompendiumStyle.drawTextFieldFrame(context, x + PAD, y + 48, leftWidth(), 16, search.isFocused());
            search.render(context, mx, my, delta);
            int half = craftableWidth();
            button(context, MaterialCraftingPanel.message("available_only"), x + PAD, y + 67, half, 14, mx, my, true, craftable);
            button(context, text("materials"), x + PAD + half + 3, y + 67, leftWidth() - half - 3, 14, mx, my, true, materialSearch);
            drawRows(context, mx, my);
            drawRecipe(context, mx, my, delta);
        }
        if (tab != 1 && feedback != null && System.currentTimeMillis() < feedbackUntil) {
            int feedbackHeight = font.wrapLines(feedback, width - 2 * PAD - 6).size() * 10 + 8;
            context.fill(x + PAD, bottom() - feedbackHeight, x + width - PAD, bottom(), 0xF0101010);
            wrapped(context, feedback, x + PAD + 3, bottom() - feedbackHeight + 4, width - 2 * PAD - 6, CompendiumStyle.TEXT);
        }
    }

    private void drawRows(DrawContext context, int mx, int my) {
        int w = listWidth() - 6;
        context.enableScissor(x + PAD, listTop(), listRight(), bottom());
        for (int i = scroll / ROW; i < rows.size() && i * ROW - scroll < bottom() - listTop(); i++) {
            Row row = rows.get(i); int sy = listTop() + i * ROW - scroll;
            boolean active = tab == 1 ? row.key.equals(picker == Picker.MAIN ? primary : secondary) : selected != null && selected.key.equals(row.key);
            CompendiumStyle.drawRailPlaque(context, x + PAD, sy, w, ROW - 1, active, MaterialCraftingPanel.inside(mx, my, x + PAD, sy, w, ROW - 1));
            context.drawItem(row.icon, x + PAD + 3, sy + (ROW - 1 - 16) / 2);
            String count = tab == 1 ? Integer.toString(row.needs.getFirst().available(storage)) : "";
            int countWidth = tab == 1 ? font.getWidth(count) + 7 : 0;
            twoLines(context, compactName(row.icon), x + PAD + 23, sy + 3, w - 27 - countWidth, ROW - 6);
            if (tab == 1) context.drawText(font, count, x + PAD + w - font.getWidth(count) - 4, sy + (ROW - font.fontHeight) / 2, CompendiumStyle.TEXT_MUTED, false);
        }
        context.disableScissor();
        int max = maxScroll();
        if (max > 0) {
            int h = bottom() - listTop(), thumb = Math.max(12, h * h / (rows.size() * ROW));
            int sy = listTop() + scroll * (h - thumb) / max;
            context.fill(listRight() - 4, listTop(), listRight() - 1, bottom(), 0xFF202020);
            context.fill(listRight() - 4, sy, listRight() - 1, sy + thumb, 0xFF868686);
        }
        if (rows.isEmpty()) {
            boolean noExtracts = tab == 1 && search.getText().isBlank();
            wrapped(context, text(noExtracts ? "no_extracts" : tab == 2 ? "no_recipes" : "no_matches"), x + PAD + 3, listTop() + 5, listWidth() - 6, CompendiumStyle.TEXT_MUTED);
            if (noExtracts) button(context, text("open_extraction"), x + PAD, listTop() + 37, listWidth(), 17, mx, my, pending == 0, false);
        }
    }

    private void drawRecipe(DrawContext context, int mx, int my, float delta) {
        if (selected == null) { wrapped(context, text(tab == 0 ? "select_extract" : "select_recipe"), right(), y + 53, rightWidth(), CompendiumStyle.TEXT_MUTED); return; }
        context.drawItem(selected.icon, right(), y + 49);
        twoLines(context, compactName(selected.icon), right() + 20, y + 49, rightWidth() - 20);
        if (tab == 2) button(context, text("delivery_" + delivery.name().toLowerCase(Locale.ROOT)), right(), y + 69, rightWidth(), 15, mx, my, pending == 0, false);
        drawNeeds(context, selected.needs, tab == 2 ? y + 89 : y + 74, mx, my);
        if (tab == 0) wrapped(context, text("hint." + selected.key), right(), y + 111, rightWidth(), CompendiumStyle.TEXT_MUTED);
        int qy = bottom() - 40;
        button(context, Text.literal("−"), right(), qy, 17, 16, mx, my, pending == 0, false);
        CompendiumStyle.drawTextFieldFrame(context, right() + 20, qy, rightWidth() - 40, 16, quantity.isFocused());
        quantity.render(context, mx, my, delta);
        button(context, Text.literal("+"), x + width - PAD - 17, qy, 17, 16, mx, my, pending == 0, false);
        line(context, text("total", (long) Math.max(0, amount()) * selected.icon.getCount()), right(), qy - 12, rightWidth());
        boolean enabled = pending == 0 && amount() > 0 && AlchemyRecipes.plan(storage, needs(), output(), amount()) != null;
        button(context, text(tab == 0 ? "extract" : "brew"), right(), bottom() - 20, rightWidth(), 17, mx, my, enabled, false);
    }

    private void drawExperiment(DrawContext context, int mx, int my) {
        context.drawText(font, text("step_ingredients"), x + PAD, y + 50, CompendiumStyle.TEXT, false);
        button(context, text("reset"), x + width - PAD - 40, y + 48, 40, 12, mx, my, pending == 0, false);
        drawIngredient(context, ingredientBox(0), text("slot_main"), primary, false, mx, my);
        drawIngredient(context, ingredientBox(1), text("slot_extra"), secondary, true, mx, my);
        Box support = ingredientBox(2);
        card(context, support, mx, my, false);
        centered(context, text("slot_support"), support.x, support.y + 5, support.width, CompendiumStyle.TEXT);
        if (supportChosen) {
            context.drawItem(supportExample(), support.x + (support.width - 16) / 2, support.y + 18);
            centeredLines(context, text("family_" + AlchemyRecipes.SUSPENSIONS.get(suspension)), support.x + 4, support.y + 38, support.width - 8, 18);
            drawClear(context, support, mx, my);
        } else emptyIngredient(context, support, text("choose"));

        context.drawText(font, text("step_bottle"), x + PAD, y + 129, CompendiumStyle.TEXT, false);
        Box bottle = bottleBox();
        card(context, bottle, mx, my, false);
        if (bottleChosen) {
            context.drawItem(AlchemyItems.water(lesser, delivery), bottle.x + 6, bottle.y + 7);
            int textWidth = bottle.width - (lesser ? 43 : 66);
            line(context, AlchemyItems.water(lesser, delivery).getName(), bottle.x + 28, bottle.y + 5, textWidth);
            context.drawText(font, text(lesser ? "lesser_tier" : "regular_tier"), bottle.x + 28, bottle.y + 18, CompendiumStyle.TEXT_MUTED, false);
            if (!lesser) {
                context.drawItem(AlchemyItems.reagent("glowing_catalyst").getDefaultStack(), bottle.x + bottle.width - 36, bottle.y + 7);
                context.drawText(font, "1", bottle.x + bottle.width - 27, bottle.y + 18, CompendiumStyle.TEXT, false);
            }
        } else {
            StationSlotHints.draw(context, StationSlotHints.Kind.BOTTLE, bottle.x + 6, bottle.y + 7);
            context.drawText(font, text("choose_bottle"), bottle.x + 28, bottle.y + 5, CompendiumStyle.TEXT, false);
            line(context, text("bottle_forms"), bottle.x + 28, bottle.y + 18, bottle.width - 42);
        }
        centered(context, Text.literal("›"), bottle.x + bottle.width - 13, bottle.y + 12, 9, CompendiumStyle.TEXT);

        boolean enabled = pending == 0 && experimentReady() && AlchemyRecipes.plan(storage, needs(), output(), 1) != null;
        if (!lastResult.isEmpty() && pending == 0) {
            context.drawItem(lastResult, x + PAD + 1, y + 180);
            line(context, compactName(lastResult), x + PAD + 22, y + 177, width - 2 * PAD - 22);
            context.drawText(font, text(AlchemyPotions.isFailure(lastResult) ? "failed_result" : "saved_recipe"), x + PAD + 22, y + 188, CompendiumStyle.TEXT_MUTED, false);
        } else twoLines(context, pending == 0 && feedback != null && System.currentTimeMillis() < feedbackUntil
                ? feedback : experimentStatus(enabled), x + PAD + 2, y + 177, width - 2 * PAD - 4, 21);
        button(context, text("mix_one"), x + PAD, bottom() - 20, width - 2 * PAD, 17, mx, my, enabled, false);
    }

    private Box ingredientBox(int index) {
        int w = (width - 2 * PAD - 12) / 3;
        return new Box(x + PAD + index * (w + 6), y + 62, w, 61);
    }
    private Box bottleBox() { return new Box(x + PAD, y + 141, width - 2 * PAD, 31); }
    private Box supportBox(int index) { return new Box(x + PAD, y + 91 + index * 36, width - 2 * PAD, 32); }
    private Box bottleChoice(int tier, int form) {
        int w = (width - 2 * PAD - 6) / 2;
        return new Box(x + PAD + tier * (w + 6), y + 102 + form * 34, w, 31);
    }
    private Box clearBox(Box box) { return new Box(box.x + box.width - 10, box.y + 3, 8, 9); }
    private void card(DrawContext context, Box box, int mx, int my, boolean selected) {
        CompendiumStyle.drawRailPlaque(context, box.x, box.y, box.width, box.height, selected, pending == 0 && box.contains(mx, my));
    }
    private void drawClear(DrawContext context, Box box, int mx, int my) {
        Box clear = clearBox(box);
        centered(context, Text.literal("×"), clear.x, clear.y, clear.width, clear.contains(mx, my) ? CompendiumStyle.TEXT_WARNING : CompendiumStyle.TEXT_MUTED);
    }
    private void drawIngredient(DrawContext context, Box box, Text label, String ingredient, boolean optional, int mx, int my) {
        card(context, box, mx, my, false);
        centered(context, label, box.x, box.y + 5, box.width, CompendiumStyle.TEXT);
        if (ingredient.isEmpty()) emptyIngredient(context, box, text(optional ? "optional" : "choose"));
        else {
            context.drawItem(AlchemyItems.reagent(ingredient).getDefaultStack(), box.x + (box.width - 16) / 2, box.y + 18);
            centeredLines(context, text("short_item." + ingredient), box.x + 4, box.y + 38, box.width - 8, 18);
            drawClear(context, box, mx, my);
        }
    }
    private void emptyIngredient(DrawContext context, Box box, Text caption) {
        centered(context, Text.literal("+"), box.x, box.y + 21, box.width, CompendiumStyle.TEXT_MUTED);
        centered(context, caption, box.x, box.y + 43, box.width, CompendiumStyle.TEXT_MUTED);
    }
    private AlchemyRecipes.Need supportNeed(String family) { return new AlchemyRecipes.Need(AlchemyRecipes.suspensionItems(family), 1, false); }
    private AlchemyRecipes.Need waterNeed(boolean low, AlchemyItems.Delivery form) { return new AlchemyRecipes.Need(List.of(AlchemyItems.filled(low, form)), 1, true); }
    private ItemStack supportExample() {
        var need = supportNeed(AlchemyRecipes.SUSPENSIONS.get(suspension));
        for (int i = 0; i < storage.size(); i++) if (need.matches(storage.stack(i))) return storage.stack(i).copyWithCount(1);
        return need.example();
    }
    private Text experimentStatus(boolean fits) {
        if (pending != 0) return text("mixing");
        if (primary.isEmpty()) return text("need_main");
        if (!supportChosen) return text("need_support");
        if (!bottleChosen) return text("need_bottle");
        Map<AlchemyRecipes.Need, Integer> totals = new LinkedHashMap<>();
        for (var need : needs()) totals.merge(need, need.count(), Integer::sum);
        for (var entry : totals.entrySet()) {
            var need = entry.getKey(); int available = need.available(storage);
            if (available < entry.getValue()) return text("missing_ingredient", need.alternatives().size() > 1
                    ? text("suspension_" + AlchemyRecipes.SUSPENSIONS.get(suspension)) : need.example().getName(), available, entry.getValue());
        }
        if (!fits) return text("no_output_space");
        return text(ModComponents.ALCHEMY_KNOWLEDGE.get(inventory.player).tried(experiment().key()) ? "failed_before" : "ready_experiment");
    }

    private void drawPicker(DrawContext context, int mx, int my, float delta) {
        button(context, text("back"), x + PAD, y + 48, 45, 16, mx, my, pending == 0, false);
        line(context, text("pick_" + picker.name().toLowerCase(Locale.ROOT)), x + PAD + 52, y + 52, width - 2 * PAD - 52);
        if (hasIngredientPicker()) {
            CompendiumStyle.drawTextFieldFrame(context, x + PAD, y + 70, width - 2 * PAD, 16, search.isFocused());
            search.render(context, mx, my, delta);
            drawRows(context, mx, my);
        } else if (picker == Picker.SUPPORT) {
            context.drawText(font, text("support_hint"), x + PAD, y + 73, CompendiumStyle.TEXT_MUTED, false);
            for (int i = 0; i < 3; i++) {
                String family = AlchemyRecipes.SUSPENSIONS.get(i); Box box = supportBox(i);
                card(context, box, mx, my, supportChosen && suspension == i);
                context.drawText(font, text("suspension_" + family), box.x + 6, box.y + 5, CompendiumStyle.TEXT, false);
                context.drawText(font, text("have_count", supportNeed(family).available(storage)), box.x + 6, box.y + 18, CompendiumStyle.TEXT_MUTED, false);
                var items = AlchemyRecipes.suspensionItems(family);
                for (int j = 0; j < items.size(); j++) context.drawItem(items.get(j).getDefaultStack(), box.x + box.width - 61 + j * 18, box.y + 8);
            }
        } else {
            context.drawText(font, text("water_hint"), x + PAD, y + 73, CompendiumStyle.TEXT_MUTED, false);
            int w = (width - 2 * PAD - 6) / 2;
            centered(context, text("lesser_tier"), x + PAD, y + 88, w, CompendiumStyle.TEXT);
            centered(context, text("regular_tier"), x + PAD + w + 6, y + 88, w, CompendiumStyle.TEXT);
            for (int tier = 0; tier < 2; tier++) for (var form : AlchemyItems.Delivery.values()) {
                boolean low = tier == 0; Box box = bottleChoice(tier, form.ordinal());
                card(context, box, mx, my, bottleChosen && lesser == low && delivery == form);
                context.drawItem(AlchemyItems.water(low, form), box.x + 4, box.y + 8);
                line(context, text("delivery_" + form.name().toLowerCase(Locale.ROOT)), box.x + 25, box.y + 5, box.width - 29);
                int available = waterNeed(low, form).available(storage);
                context.drawText(font, text("have_count", available), box.x + 25, box.y + 18, available > 0 ? CompendiumStyle.TEXT_MUTED : CompendiumStyle.TEXT_WARNING, false);
            }
            context.drawItem(AlchemyItems.reagent("glowing_catalyst").getDefaultStack(), x + PAD, y + 206);
            line(context, text("catalyst_hint"), x + PAD + 21, y + 211, width - 2 * PAD - 21);
        }
    }

    private void drawNeeds(DrawContext context, List<AlchemyRecipes.Need> needs, int top, int mx, int my) {
        for (int i = 0; i < needs.size(); i++) {
            var need = needs.get(i); int sy = top + i * 16;
            boolean shortcut = tab == 2 && !extractionsFor(need).isEmpty();
            if (shortcut && MaterialCraftingPanel.inside(mx, my, right(), sy, rightWidth(), 16))
                context.fill(right(), sy, right() + rightWidth(), sy + 16, 0xFF292929);
            context.drawItem(need.example(), right(), sy);
            String count = need.available(storage) + "/" + (long) need.count() * Math.max(1, amount());
            context.drawText(font, count, right() + 20, sy + 4, need.available(storage) >= (long) need.count() * Math.max(1, amount()) ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_WARNING, false);
            if (shortcut) context.drawText(font, "›", right() + rightWidth() - 6, sy + 4, CompendiumStyle.TEXT_MUTED, false);
        }
    }

    private List<AlchemyRecipes.Extraction> extractionsFor(AlchemyRecipes.Need need) {
        return AlchemyRecipes.extractions().stream().filter(recipe -> need.alternatives().contains(recipe.output().getItem())).toList();
    }

    private void openExtraction(AlchemyRecipes.Need need) {
        var discovered = ModComponents.ITEM_DISCOVERIES.get(inventory.player);
        var choices = extractionsFor(need).stream().filter(recipe -> recipe.needs().stream()
                .allMatch(input -> input.alternatives().stream().anyMatch(discovered::contains))).toList();
        if (choices.isEmpty()) {
            feedback = text("extraction_undiscovered"); feedbackUntil = System.currentTimeMillis() + 3500;
            return;
        }
        var previous = new RecipeReturn(selected.key, search.getText(), quantity.getText(), scroll, craftable, materialSearch);
        changeTab(0);
        returnRecipe = previous;
        extractionTargets = choices.stream().map(AlchemyRecipes.Extraction::key).collect(java.util.stream.Collectors.toUnmodifiableSet());
        craftable = materialSearch = false;
        search.setText(""); quantity.setText("1"); rebuild();
        selected = rows.size() == 1 ? rows.getFirst() : null;
    }

    private void backToRecipe() {
        var previous = returnRecipe;
        changeTab(2);
        craftable = previous.craftable; materialSearch = previous.materialSearch;
        search.setText(previous.query); quantity.setText(previous.quantity); rebuild();
        selected = rows.stream().filter(row -> row.key.equals(previous.key)).findFirst().orElse(null);
        scroll = Math.clamp(previous.scroll, 0, maxScroll());
    }

    private void line(DrawContext context, Text value, int left, int top, int max) {
        String s = font.trimToWidth(value.getString(), Math.max(0, max));
        context.drawText(font, s, left, top, CompendiumStyle.TEXT, false);
    }
    private Text compactName(ItemStack stack) {
        var contents = stack.getOrDefault(net.minecraft.component.DataComponentTypes.POTION_CONTENTS, net.minecraft.component.type.PotionContentsComponent.DEFAULT);
        var formula = contents.potion().flatMap(potion -> potion.getKey()).map(key -> AlchemyPotions.get(key.getValue().getPath())).orElse(null);
        if (formula == null) return stack.getName();
        Text effect = text("effect." + formula.family());
        return formula.lesser() ? text("short_lesser", effect) : effect;
    }
    private void twoLines(DrawContext context, Text value, int left, int top, int max) {
        twoLines(context, value, left, top, max, 20);
    }
    private void twoLines(DrawContext context, Text value, int left, int top, int max, int height) {
        var lines = font.wrapLines(value, max);
        int count = Math.min(2, lines.size());
        int sy = top + Math.max(0, (height - count * font.fontHeight) / 2);
        for (int i = 0; i < count; i++) context.drawText(font, lines.get(i), left, sy + i * font.fontHeight, CompendiumStyle.TEXT, false);
    }
    private void centered(DrawContext context, Text value, int left, int top, int width, int color) {
        context.drawText(font, value, left + (width - font.getWidth(value)) / 2, top, color, false);
    }
    private void centeredLines(DrawContext context, Text value, int left, int top, int width, int height) {
        var lines = font.wrapLines(value, width);
        int count = Math.min(2, lines.size());
        int sy = top + Math.max(0, (height - count * font.fontHeight) / 2);
        for (int i = 0; i < count; i++) context.drawText(font, lines.get(i), left + (width - font.getWidth(lines.get(i))) / 2,
                sy + i * font.fontHeight, CompendiumStyle.TEXT, false);
    }
    private void wrapped(DrawContext context, Text value, int left, int top, int max, int color) {
        for (var line : font.wrapLines(value, Math.max(1, max))) { context.drawText(font, line, left, top, color, false); top += 10; }
    }
    private void button(DrawContext context, Text value, int left, int top, int w, int h, int mx, int my, boolean enabled, boolean active) {
        CompendiumStyle.drawRailPlaque(context, left, top, w, h, active, enabled && MaterialCraftingPanel.inside(mx, my, left, top, w, h));
        String shown = font.trimToWidth(value.getString(), Math.max(1, w - 6));
        context.drawText(font, shown, left + (w - font.getWidth(shown)) / 2, top + (h - 8) / 2,
                enabled ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_MUTED, false);
    }

    boolean click(double mx, double my, int button, Consumer<Element> focus) {
        capturing = true;
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || pending != 0) return true;
        if (returnRecipe != null && MaterialCraftingPanel.inside(mx, my, x + width - PAD - 57, y + 7, 57, 16)) {
            focus.accept(null); backToRecipe(); return true;
        }
        int tw = (width - 2 * PAD) / 3;
        if (MaterialCraftingPanel.inside(mx, my, x + PAD, y + 28, tw * 3, 16)) {
            focus.accept(null);
            changeTab(Math.clamp((int) (mx - x - PAD) / tw, 0, 2)); return true;
        }
        if (tab == 1) return clickExperiment(mx, my, button, focus);
        if (MaterialCraftingPanel.inside(mx, my, x + PAD, y + 48, leftWidth(), 16)) { focus.accept(search); search.mouseClicked(mx, my, button); return true; }
        if (selected != null && MaterialCraftingPanel.inside(mx, my, right() + 20, bottom() - 40, rightWidth() - 40, 16)) {
            focus.accept(quantity); quantity.mouseClicked(mx, my, button); return true;
        }
        focus.accept(null);
        int half = craftableWidth();
        if (MaterialCraftingPanel.inside(mx, my, x + PAD, y + 67, leftWidth(), 14)) {
            if (mx < x + PAD + half) craftable = !craftable; else materialSearch = !materialSearch;
            scroll = 0; rebuild(); return true;
        }
        if (clickList(mx, my, button)) return true;
        if (tab == 2 && selected != null) {
            for (int i = 0; i < selected.needs.size(); i++) {
                var need = selected.needs.get(i);
                if (!extractionsFor(need).isEmpty() && MaterialCraftingPanel.inside(mx, my, right(), y + 89 + i * 16, rightWidth(), 16)) {
                    openExtraction(need); return true;
                }
            }
        }
        if (mx >= right() && mx < x + width - PAD) {
            if (tab == 2 && my >= y + 69 && my < y + 84) cycleDelivery();
            else if (my >= bottom() - 40 && my < bottom() - 24) {
                if (mx < right() + 17) quantity.setText(Integer.toString(Math.max(1, amount() - 1)));
                else if (mx >= x + width - PAD - 17) quantity.setText(Integer.toString(Math.min(MaterialCrafting.MAX_BATCH, amount() + 1)));
            }
            if (my >= bottom() - 20 && my < bottom() - 3) submit();
        }
        return true;
    }

    private void positionSearch() {
        search.setX(x + PAD + 3);
        search.setY(y + (tab == 1 ? 73 : 51));
        search.setWidth((tab == 1 ? width - 2 * PAD : leftWidth()) - 6);
    }
    private void changeTab(int next) {
        returnRecipe = null; extractionTargets = Set.of();
        if (next == tab) return;
        if (tab != 1) queries[tab] = search.getText();
        tab = next; picker = Picker.NONE; scroll = 0; selected = null; feedback = null;
        search.setText(queries[tab]); positionSearch(); rebuild();
    }
    private void openPicker(Picker next) {
        picker = next; scroll = 0; search.setText(""); positionSearch(); rebuild();
    }
    private void closePicker() {
        picker = Picker.NONE; scroll = 0; search.setText(""); positionSearch(); rebuild();
    }
    private void changedExperiment() { lastResult = ItemStack.EMPTY; feedback = null; }
    private boolean clickList(double mx, double my, int button) {
        if (MaterialCraftingPanel.inside(mx, my, listRight() - 5, listTop(), 5, bottom() - listTop()) && maxScroll() > 0) {
            dragging = true; grab = 0.5; drag(mx, my, button, 0, 0, null); return true;
        }
        if (!MaterialCraftingPanel.inside(mx, my, x + PAD, listTop(), listWidth() - 6, bottom() - listTop())) return false;
        int row = ((int) my - listTop() + scroll) / ROW;
        if (row < rows.size()) {
            Row chosen = rows.get(row);
            if (tab == 1) {
                if (picker == Picker.MAIN) primary = chosen.key; else secondary = chosen.key;
                changedExperiment(); closePicker();
            } else selected = chosen;
        }
        return true;
    }
    private boolean clickExperiment(double mx, double my, int button, Consumer<Element> focus) {
        if (hasIngredientPicker() && MaterialCraftingPanel.inside(mx, my, x + PAD, y + 70, width - 2 * PAD, 16)) {
            focus.accept(search); search.mouseClicked(mx, my, button); return true;
        }
        focus.accept(null);
        if (picker != Picker.NONE) {
            if (MaterialCraftingPanel.inside(mx, my, x + PAD, y + 48, 45, 16)) { closePicker(); return true; }
            if (hasIngredientPicker()) {
                if (rows.isEmpty() && search.getText().isBlank() && MaterialCraftingPanel.inside(mx, my, x + PAD, listTop() + 37, listWidth(), 17)) {
                    queries[0] = ""; changeTab(0);
                }
                else clickList(mx, my, button);
            } else if (picker == Picker.SUPPORT) {
                for (int i = 0; i < 3; i++) if (supportBox(i).contains(mx, my)) {
                    suspension = i; supportChosen = true; changedExperiment(); closePicker(); break;
                }
            } else {
                for (int tier = 0; tier < 2; tier++) for (int form = 0; form < 3; form++) if (bottleChoice(tier, form).contains(mx, my)) {
                    lesser = tier == 0; delivery = AlchemyItems.Delivery.values()[form]; bottleChosen = true;
                    changedExperiment(); closePicker(); return true;
                }
            }
            return true;
        }
        if (MaterialCraftingPanel.inside(mx, my, x + width - PAD - 40, y + 48, 40, 12)) {
            primary = secondary = ""; supportChosen = bottleChosen = false; lesser = true; delivery = AlchemyItems.Delivery.DRINK;
            changedExperiment(); return true;
        }
        for (int i = 0; i < 3; i++) if (ingredientBox(i).contains(mx, my)) {
            boolean chosen = i == 0 ? !primary.isEmpty() : i == 1 ? !secondary.isEmpty() : supportChosen;
            if (chosen && clearBox(ingredientBox(i)).contains(mx, my)) {
                if (i == 0) primary = ""; else if (i == 1) secondary = ""; else supportChosen = false;
                changedExperiment();
            } else openPicker(i == 0 ? Picker.MAIN : i == 1 ? Picker.EXTRA : Picker.SUPPORT);
            return true;
        }
        if (bottleBox().contains(mx, my)) { openPicker(Picker.BOTTLE); return true; }
        if (MaterialCraftingPanel.inside(mx, my, x + PAD, bottom() - 20, width - 2 * PAD, 17)) submit();
        return true;
    }

    private void cycleDelivery() { delivery = AlchemyItems.Delivery.values()[(delivery.ordinal() + 1) % 3]; lastResult = ItemStack.EMPTY; rebuild(); }
    void submit() {
        refresh();
        if (pending != 0 || output().isEmpty() || amount() < 1 || AlchemyRecipes.plan(storage, needs(), output(), amount()) == null) return;
        String selection = tab == 1 ? primary + "," + secondary + "," + AlchemyRecipes.SUSPENSIONS.get(suspension) + "," + (lesser ? "0" : "1") + "," + delivery.ordinal()
                : selected.key + (tab == 2 ? "," + delivery.ordinal() : "");
        requestedResult = output().copy(); pending = ++sequence;
        ClientPlayNetworking.send(new AlchemyPayload(handler.syncId, pending, tab, amount(), selection));
    }
    void receive(CraftMaterialsResultPayload result) {
        if (result.request() != pending || pending == 0) return;
        pending = 0;
        if (result.result() > 0) {
            if (tab == 1) lastResult = requestedResult;
            feedback = text(tab == 1 ? AlchemyPotions.isFailure(requestedResult) ? "mix_failed" : "learned" : "completed", requestedResult.getName());
        } else { feedback = text("changed"); if (tab == 1) lastResult = ItemStack.EMPTY; }
        feedbackUntil = System.currentTimeMillis() + 2600;
        refresh();
    }
    void tooltip(DrawContext context, int mx, int my) {
        if (tab == 1) { experimentTooltip(context, mx, my); return; }
        if (mx >= x + PAD && mx < listRight() - 6 && my >= listTop() && my < bottom()) {
            int index = (my - listTop() + scroll) / ROW;
            if (index >= 0 && index < rows.size()) {
                Row row = rows.get(index);
                var lines = new ArrayList<Text>(); lines.add(row.icon.getName());
                for (var need : row.needs) lines.add(Text.literal(need.count() + " × ").append(need.example().getName()));
                hint(context, lines, mx, my);
            }
        } else if (selected != null) {
            int top = tab == 2 ? y + 89 : y + 74;
            for (int i = 0; i < selected.needs.size(); i++) if (MaterialCraftingPanel.inside(mx, my, right(), top + i * 16, rightWidth(), 16)) {
                var need = selected.needs.get(i);
                var lines = new ArrayList<Text>(need.alternatives().stream().map(item -> item.getName()).toList());
                if (tab == 2 && !extractionsFor(need).isEmpty()) lines.add(text("open_ingredient_extraction").copy().formatted(net.minecraft.util.Formatting.GRAY));
                context.drawTooltip(font, lines, mx, my);
            }
            if (MaterialCraftingPanel.inside(mx, my, right(), y + 49, rightWidth(), 20)) context.drawItemTooltip(font, selected.icon, mx, my);
        }
    }
    private void hint(DrawContext context, List<Text> values, int mx, int my) {
        var lines = new ArrayList<net.minecraft.text.OrderedText>();
        for (Text value : values) lines.addAll(font.wrapLines(value, Math.max(80, Math.min(220, width - 16))));
        context.drawOrderedTooltip(font, lines, mx, my);
    }
    private void experimentTooltip(DrawContext context, int mx, int my) {
        if (hasIngredientPicker()) {
            if (mx < x + PAD || mx >= listRight() - 6 || my < listTop() || my >= bottom()) return;
            int index = (my - listTop() + scroll) / ROW;
            if (index < rows.size()) {
                Row row = rows.get(index);
                hint(context, List.of(row.icon.getName(), text("hint." + row.key), text("have_count", row.needs.getFirst().available(storage))), mx, my);
            }
        } else if (picker == Picker.SUPPORT) {
            for (int i = 0; i < 3; i++) if (supportBox(i).contains(mx, my)) {
                String family = AlchemyRecipes.SUSPENSIONS.get(i);
                var lines = new ArrayList<Text>(); lines.add(text("support_use_one"));
                for (var item : AlchemyRecipes.suspensionItems(family)) lines.add(item.getName());
                hint(context, lines, mx, my);
            }
        } else if (picker == Picker.BOTTLE) {
            for (int tier = 0; tier < 2; tier++) for (var form : AlchemyItems.Delivery.values()) if (bottleChoice(tier, form.ordinal()).contains(mx, my)) {
                boolean low = tier == 0;
                var lines = new ArrayList<Text>(); lines.add(AlchemyItems.water(low, form).getName());
                lines.add(text("have_count", waterNeed(low, form).available(storage))); lines.add(text("water_hint"));
                if (!low) lines.add(text("catalyst_hint"));
                hint(context, lines, mx, my);
            }
            if (MaterialCraftingPanel.inside(mx, my, x + PAD, y + 206, width - 2 * PAD, 16))
                context.drawItemTooltip(font, AlchemyItems.reagent("glowing_catalyst").getDefaultStack(), mx, my);
        } else {
            for (int i = 0; i < 3; i++) if (ingredientBox(i).contains(mx, my)) {
                String ingredient = i == 0 ? primary : secondary;
                boolean chosen = i == 2 ? supportChosen : !ingredient.isEmpty();
                if (chosen && clearBox(ingredientBox(i)).contains(mx, my)) { hint(context, List.of(text("clear_choice")), mx, my); return; }
                if (i == 2) {
                    var lines = new ArrayList<Text>(); lines.add(text("support_use_one"));
                    if (supportChosen) for (var item : AlchemyRecipes.suspensionItems(AlchemyRecipes.SUSPENSIONS.get(suspension))) lines.add(item.getName());
                    hint(context, lines, mx, my);
                } else if (ingredient.isEmpty()) hint(context, List.of(text(i == 0 ? "main_hint" : "extra_hint"), text("inventory_hint")), mx, my);
                else hint(context, List.of(AlchemyItems.reagent(ingredient).getName(), text("hint." + ingredient), text("inventory_hint")), mx, my);
                return;
            }
            if (bottleBox().contains(mx, my)) {
                var lines = new ArrayList<Text>(); lines.add(text("water_hint"));
                if (bottleChosen && !lesser) lines.add(text("catalyst_hint"));
                hint(context, lines, mx, my);
            }
            if (!lastResult.isEmpty() && MaterialCraftingPanel.inside(mx, my, x + PAD, y + 176, width - 2 * PAD, 23))
                context.drawItemTooltip(font, lastResult, mx, my);
        }
    }
    boolean release(int button) { boolean result = capturing || dragging; capturing = dragging = false; return result; }
    boolean drag(double mx, double my, int button, double dx, double dy, Element focused) {
        if (capturing && focused instanceof TextFieldWidget field) return field.mouseDragged(mx, my, button, dx, dy);
        if (!dragging) return false;
        int h = bottom() - listTop(), thumb = Math.max(12, h * h / Math.max(1, rows.size() * ROW));
        scroll = Math.clamp((int) ((my - listTop() - thumb * grab) * maxScroll() / Math.max(1, h - thumb)), 0, maxScroll());
        return true;
    }
    void scroll(double mx, double vertical) { scroll = Math.clamp(scroll - (int) (vertical * ROW), 0, maxScroll()); }
}

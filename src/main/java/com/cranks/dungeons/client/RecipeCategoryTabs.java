package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.client.StationRecipeCategories.Category;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

final class RecipeCategoryTabs {
    private static final int TAB = 15;
    private static final int PITCH = 16;

    private final List<Category> categories;
    private Category active = Category.ALL;
    private List<Category> visible = List.of(Category.ALL);

    RecipeCategoryTabs(StationRecipeCategories station) {
        categories = station.tabs();
    }

    void setDiscovered(Stream<Category> recipeCategories) {
        var discovered = EnumSet.noneOf(Category.class);
        recipeCategories.forEach(discovered::add);
        var available = new ArrayList<Category>();
        available.add(Category.ALL);
        for (Category category : categories) {
            if (discovered.contains(category)) available.add(category);
        }
        if (!available.contains(active)) active = Category.ALL;
        visible = List.copyOf(available);
    }

    boolean includes(Category category) {
        return active == Category.ALL || active == category;
    }

    int height(int width) {
        return visible.size() == 1 ? 0 : Math.ceilDiv(visible.size(), columns(width)) * PITCH + 3;
    }

    void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        if (height(width) == 0) return;
        int columns = columns(width);
        for (int i = 0; i < visible.size(); i++) {
            Category tab = visible.get(i);
            int left = x + i % columns * PITCH;
            int top = y + i / columns * PITCH;
            CompendiumStyle.drawRailPlaque(context, left, top, TAB, TAB, tab == active,
                    MaterialCraftingPanel.inside(mouseX, mouseY, left, top, TAB, TAB));
            var matrices = context.getMatrices();
            matrices.push();
            matrices.translate(left + 2, top + 2, 0);
            matrices.scale(0.6875F, 0.6875F, 1);
            context.drawItem(tab.icon, 0, 0);
            matrices.pop();
        }
    }

    boolean click(int x, int y, int width, double mouseX, double mouseY) {
        Category hit = tabAt(x, y, width, mouseX, mouseY);
        if (hit == null) return false;
        active = hit;
        return true;
    }

    boolean tooltip(DrawContext context, TextRenderer font, int x, int y, int width, int mouseX, int mouseY) {
        Category hit = tabAt(x, y, width, mouseX, mouseY);
        if (hit == null) return false;
        context.drawTooltip(font, hit.label, mouseX, mouseY);
        return true;
    }

    private Category tabAt(int x, int y, int width, double mouseX, double mouseY) {
        if (!MaterialCraftingPanel.inside(mouseX, mouseY, x, y, width, height(width))) return null;
        int offsetX = (int) (mouseX - x), offsetY = (int) (mouseY - y), columns = columns(width);
        int column = offsetX / PITCH, row = offsetY / PITCH;
        int index = row * columns + column;
        return column < columns && index < visible.size() && offsetX % PITCH < TAB && offsetY % PITCH < TAB
                ? visible.get(index) : null;
    }

    private static int columns(int width) {
        return Math.max(1, (width + 1) / PITCH);
    }
}

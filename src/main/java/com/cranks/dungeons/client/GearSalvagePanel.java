package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.inventory.GearSalvage;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.message;

final class GearSalvagePanel {
    private static final int ROW_HEIGHT = 20;
    private record GearRow(int slot, ItemStack stack, ItemStack output) {}

    private final PlayerInventory inventory;
    private final LoadoutFurnaceHandler furnace;
    private final BiConsumer<Integer, ItemStack> submit;
    private MaterialCrafting.Storage storage;
    private List<GearRow> rows = List.of();
    private GearRow selected;
    private TextRenderer font;
    private int left, top, divider, right, bottom, scroll;
    private boolean dragging;
    private double grab;
    private boolean pending;
    private Text feedback;
    private boolean feedbackGood;
    private long feedbackUntil;

    GearSalvagePanel(PlayerInventory inventory, LoadoutFurnaceHandler furnace, BiConsumer<Integer, ItemStack> submit) {
        this.inventory = inventory;
        this.furnace = furnace;
        this.submit = submit;
    }

    void init(TextRenderer font, int left, int top, int divider, int right, int bottom) {
        this.font = font;
        this.left = left;
        this.top = top;
        this.divider = divider;
        this.right = right;
        this.bottom = bottom;
        dragging = false;
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    private void refresh() {
        if (storage != null && storage.isCurrent()) return;
        storage = new MaterialCrafting.Storage(inventory);
        var next = new ArrayList<GearRow>();
        GearRow nextSelection = null;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.stack(i);
            if (stack.getCount() != 1 || !GearSalvage.supports(stack, furnace != null)) continue;
            var row = new GearRow(storage.slot(i), stack, GearSalvage.output(stack, furnace != null));
            next.add(row);
            if (selected != null && selected.slot == row.slot && ItemStack.areEqual(selected.stack, row.stack)) {
                nextSelection = row;
            }
        }
        rows = next;
        selected = nextSelection;
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    void render(DrawContext context, int mouseX, int mouseY) {
        refresh();
        int listRight = divider - 8;
        if (rows.isEmpty()) line(context, message("salvage_empty"), left, top + 5, listRight - left, CompendiumStyle.TEXT_MUTED);
        context.enableScissor(left, top, listRight, bottom);
        for (int i = scroll / ROW_HEIGHT; i < rows.size(); i++) {
            int y = top + i * ROW_HEIGHT - scroll;
            if (y >= bottom) break;
            GearRow row = rows.get(i);
            boolean hover = mouseY >= top && inside(mouseX, mouseY, left, y, listRight - left, ROW_HEIGHT);
            CompendiumStyle.drawRailPlaque(context, left, y + 1, listRight - left, 18, row == selected, hover);
            context.drawItem(row.stack, left + 2, y + 2);
            line(context, row.stack.getName(), left + 21, y + 6, listRight - left - 25,
                    row.output.isEmpty() ? CompendiumStyle.TEXT_MUTED : CompendiumStyle.TEXT);
        }
        context.disableScissor();
        var bar = scrollbar();
        if (bar.maxScroll() > 0) {
            int thumb = (int) Math.round(bar.thumbTop(scroll));
            context.fill(divider - 6, top, divider - 3, bottom, 0xFF090909);
            context.fill(divider - 6, thumb, divider - 3, thumb + bar.thumbHeight(), dragging ? 0xFFA0A0A0 : 0xFF737373);
        }

        int details = divider + 6;
        if (selected != null) {
            context.drawItem(selected.stack, details + 2, top + 1);
            line(context, selected.stack.getName(), details + 22, top + 2, right - details - 22, CompendiumStyle.TEXT);
            if (selected.stack.isDamageable()) {
                int max = selected.stack.getMaxDamage();
                line(context, message("salvage_durability", max - selected.stack.getDamage(), max),
                        details + 22, top + 13, right - details - 22, CompendiumStyle.TEXT_MUTED);
            }
            if (!selected.output.isEmpty()) {
                context.drawItem(selected.output, details + 2, top + 26);
                line(context, message("salvage_yield", selected.output.getCount(), selected.output.getName()),
                        details + 22, top + 31, right - details - 22, CompendiumStyle.TEXT_SUCCESS);
            } else {
                line(context, message("salvage_no_yield"), details, top + 31, right - details, CompendiumStyle.TEXT_MUTED);
            }
        } else {
            line(context, message("salvage_choose"), details, top + 5, right - details, CompendiumStyle.TEXT_MUTED);
        }
        if (feedback != null && System.currentTimeMillis() < feedbackUntil) {
            line(context, feedback, details, bottom - 33, right - details,
                    feedbackGood ? CompendiumStyle.TEXT_SUCCESS : CompendiumStyle.TEXT_WARNING);
        } else if (selected != null && !selected.output.isEmpty() && !canSubmit() && furnace != null && !pending) {
            line(context, message("input_unavailable"), details, bottom - 33, right - details, CompendiumStyle.TEXT_MUTED);
        }
        drawButton(context, message(furnace == null ? "salvage" : "salvage_load"),
                details, bottom - 20, right - details, 19, mouseX, mouseY, canSubmit());
    }

    void tooltip(DrawContext context, int mx, int my) {
        int index = (my - top + scroll) / ROW_HEIGHT;
        if (index >= 0 && index < rows.size() && inside(mx, my, left, top, divider - 8 - left, bottom - top)) {
            context.drawItemTooltip(font, rows.get(index).stack, mx, my);
        } else if (selected != null && inside(mx, my, divider + 6, top, right - divider - 6, 21)) {
            context.drawItemTooltip(font, selected.stack, mx, my);
        } else if (selected != null && !selected.output.isEmpty() && inside(mx, my, divider + 6, top + 25, right - divider - 6, 20)) {
            context.drawItemTooltip(font, selected.output, mx, my);
        }
    }

    void click(double mx, double my) {
        refresh();
        var bar = scrollbar();
        if (inside(mx, my, divider - 7, top, 6, bar.height()) && bar.maxScroll() > 0) {
            dragging = true;
            grab = bar.grabOffset(my, scroll);
            scroll = bar.scrollAt(my, grab);
        } else if (inside(mx, my, left, top, divider - 8 - left, bar.height())) {
            int index = ((int) my - top + scroll) / ROW_HEIGHT;
            if (index < rows.size()) {
                selected = rows.get(index);
                feedback = null;
            }
        } else if (inside(mx, my, divider + 6, bottom - 20, right - divider - 6, 19) && canSubmit()) {
            submit.accept(selected.slot, selected.stack);
        }
    }

    boolean drag(double mouseY) {
        if (!dragging) return false;
        scroll = scrollbar().scrollAt(mouseY, grab);
        return true;
    }

    boolean release() {
        boolean wasDragging = dragging;
        dragging = false;
        return wasDragging;
    }

    void scroll(double mx, double vertical) {
        if (mx < divider) scroll = Math.clamp(scroll - (int) (vertical * ROW_HEIGHT * 2), 0, scrollbar().maxScroll());
    }

    void setPending() {
        pending = true;
        feedback = null;
    }

    void receive(int result) {
        pending = false;
        feedbackGood = result > 0;
        feedback = switch (result) {
            case CraftMaterialsResultPayload.INVENTORY_CHANGED -> message("changed");
            case CraftMaterialsResultPayload.UNAVAILABLE -> message(furnace == null ? "unavailable" : "input_unavailable");
            default -> furnace == null ? message("salvage_recovered", result) : message("salvage_loaded");
        };
        feedbackUntil = System.currentTimeMillis() + 2800;
    }

    private boolean canSubmit() {
        return selected != null && !selected.output.isEmpty() && !pending
                && (furnace == null || furnace.canLoadSalvage(selected.stack));
    }

    private StatsScrollbar scrollbar() {
        return new StatsScrollbar(top, bottom - top, rows.size() * ROW_HEIGHT);
    }

    private void line(DrawContext context, Text text, int x, int y, int width, int color) {
        String value = text.getString();
        if (font.getWidth(value) > width) value = font.trimToWidth(value, Math.max(0, width - 6)) + "…";
        context.drawText(font, value, x, y, color, false);
    }
}

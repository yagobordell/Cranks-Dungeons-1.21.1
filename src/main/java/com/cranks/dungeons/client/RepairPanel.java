package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.inventory.EquipmentRepair;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.RepairGearPayload;
import com.cranks.dungeons.screen.LoadoutGrindstoneHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.message;

final class RepairPanel {
    private static final int ROW = 20;
    private static final int MUTED = CompendiumStyle.TEXT_MUTED;
    private static final int TEXT = CompendiumStyle.TEXT;
    private static final int SUCCESS = CompendiumStyle.TEXT_SUCCESS;
    private static final int WARNING = CompendiumStyle.TEXT_WARNING;
    private static final ItemStack GRINDSTONE = Items.GRINDSTONE.getDefaultStack();
    private record Gear(int slot, ItemStack stack) {
        RepairGearPayload.Source source() {
            return new RepairGearPayload.Source(slot, stack.copy());
        }
    }

    private final LoadoutGrindstoneHandler screen;
    private final PlayerInventory inventory;
    private final Text title;
    private final BiConsumer<RepairGearPayload, ItemStack> submit;
    private MaterialCrafting.Storage storage;
    private Gear target, donor;
    private EquipmentRepair.Materials materials;
    private List<Gear> rows = List.of();
    private boolean donorMode, choosingDonor, dragging, captured;
    private int amount = 1, available, scroll;
    private double grab;
    private RepairGearPayload pending;
    private Text feedback;
    private int feedbackColor;
    private long feedbackUntil;
    private TextRenderer font;
    private int x, y, width, height, divider;

    RepairPanel(LoadoutGrindstoneHandler screen, PlayerInventory inventory, Text title,
                BiConsumer<RepairGearPayload, ItemStack> submit) {
        this.screen = screen;
        this.inventory = inventory;
        this.title = title;
        this.submit = submit;
    }

    void init(TextRenderer font, int x, int y, int width, int height) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        divider = x + width * 44 / 100;
        captured = dragging = false;
    }

    private void refresh() {
        boolean storageChanged = storage == null || !storage.isCurrent();
        if (storageChanged) storage = new MaterialCrafting.Storage(inventory);
        if (target != null && !matches(target)) select(null);
        if (donor != null && !matches(donor)) donor = null;
        if (storageChanged) updateAvailable();

        List<Gear> next = new ArrayList<>();
        for (int i = 5; i < LoadoutStationHandler.PLAYER_SLOT_END; i++) {
            var slot = screen.getSlot(i);
            ItemStack stack = slot.getStack();
            if (slot.inventory != inventory || !slot.isEnabled() || !stack.isDamageable()) continue;
            if (choosingDonor) {
                if (target == null || i == target.slot || !EquipmentRepair.isDonor(target.stack, stack)
                        || storage.stackAtSlot(slot.getIndex()).isEmpty()) continue;
            } else if (!EquipmentRepair.canRepair(stack)) continue;
            next.add(new Gear(i, stack));
        }
        rows = next;
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    private boolean matches(Gear gear) {
        var slot = screen.getSlot(gear.slot);
        return slot.isEnabled() && ItemStack.areEqual(slot.getStack(), gear.stack);
    }

    private void select(Gear selected) {
        target = selected == null ? null : new Gear(selected.slot, selected.stack.copy());
        donor = null;
        donorMode = choosingDonor = false;
        materials = target == null ? null : EquipmentRepair.materials(target.stack);
        updateAvailable();
        amount = Math.max(1, Math.min(available, fullCost()));
    }

    private void updateAvailable() {
        available = target == null ? 0
                : EquipmentRepair.available(storage, materials.ingredient(), screen.getSlot(target.slot).getIndex());
    }

    private int fullCost() {
        return target == null ? 0 : materials.needed(target.stack);
    }

    int restored() {
        if (target == null) return 0;
        if (donorMode) return donor == null ? 0 : Math.min(target.stack.getDamage(), EquipmentRepair.remaining(donor.stack));
        if (materials.ingredient().isEmpty()) return 0;
        return (int) Math.min(target.stack.getDamage(), (long) amount * materials.durabilityPerMaterial(target.stack));
    }

    private boolean canSubmit() {
        return pending == null && restored() > 0 && (donorMode || amount <= available);
    }

    void render(DrawContext context, int mx, int my) {
        refresh();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        context.drawItem(GRINDSTONE, x + 8, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,title,x+29,y+11,width-40,TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("repairing"));
        InventoryStyle.drawDivider(context, x + 8, x + width - 8, y + 24);
        context.fill(divider, y + 29, divider + 1, bottom(), 0xFF3A3A3A);
        if (choosingDonor) drawButton(context, message("repair_back"), x + 8, y + 30, divider - x - 14, 15, mx, my, true);
        else line(context, message("repair_damaged"), x + 9, y + 34, divider - x - 14, MUTED);
        renderRows(context, mx, my);
        renderDetails(context, mx, my);
    }

    private void renderRows(DrawContext context, int mx, int my) {
        var bar = scrollbar();
        int left = x + 8, right = divider - 8;
        if (rows.isEmpty()) line(context, message(choosingDonor ? "repair_no_donor" : "repair_no_gear"),
                left, bar.top() + 5, right - left, MUTED);
        context.enableScissor(left, bar.top(), right, bottom());
        for (int i = scroll / ROW; i < rows.size(); i++) {
            int top = bar.top() + i * ROW - scroll;
            if (top >= bottom()) break;
            Gear row = rows.get(i);
            Gear active = choosingDonor ? donor : target;
            boolean highlighted = active != null && active.slot == row.slot;
            boolean hover = my >= bar.top() && inside(mx, my, left, top, right - left, ROW);
            CompendiumStyle.drawRailPlaque(context, left, top + 1, right - left, 18, highlighted, hover);
            context.drawItem(row.stack, left + 2, top + 2);
            line(context, row.stack.getName(), left + 21, top + 6, right - left - 24, TEXT);
        }
        context.disableScissor();
        if (bar.maxScroll() > 0) {
            int top = (int) Math.round(bar.thumbTop(scroll));
            context.fill(divider - 6, bar.top(), divider - 3, bottom(), 0xFF090909);
            context.fill(divider - 6, top, divider - 3, top + bar.thumbHeight(), dragging ? 0xFFA0A0A0 : 0xFF737373);
        }
    }

    private void renderDetails(DrawContext context, int mx, int my) {
        int left = details(), w = detailWidth();
        if (target != null) {
            context.drawItem(target.stack, left + 1, y + 35);
            line(context, target.stack.getName(), left + 21, y + 35, w - 21, TEXT);
            int max = target.stack.getMaxDamage();
            int current = EquipmentRepair.remaining(target.stack);
            line(context, message("repair_durability", current, max), left + 21, y + 47, w - 21, MUTED);
            drawMode(context, "repair_materials", left, y + 64, w / 2 - 2, mx, my, !donorMode);
            drawMode(context, "repair_donor", left + w / 2 + 2, y + 64, w - w / 2 - 2, mx, my, donorMode);

            ItemStack cost = donorMode ? donor == null ? ItemStack.EMPTY : donor.stack : materialIcon();
            if (!cost.isEmpty()) {
                context.drawItem(cost, left + 2, y + 87);
                Text label = donorMode ? cost.getName() : materials.ingredient().getMatchingStacks().length > 1
                        ? message("repair_either", amount) : message("repair_cost", amount, cost.getName());
                line(context, label, left + 22, y + 91, w - 22, TEXT);
                line(context, donorMode ? message("repair_donor_cost") : message("repair_in_stock", available),
                        left + 22, y + 103, w - 22, canSubmit() ? MUTED : WARNING);
            } else {
                line(context, message(donorMode ? "repair_choose_donor" : "repair_no_material"),
                        left, y + 91, w, MUTED);
            }
            int result = current + restored();
            line(context, message("repair_after", result, max), left, y + 128, w, canSubmit() ? SUCCESS : MUTED);
            CompendiumStyle.drawRailPlaque(context, left, y + 142, w, 8, false, false);
            int barWidth = w - 4;
            int currentWidth = (int) ((long) barWidth * current / max);
            int repairedWidth = (int) ((long) barWidth * result / max);
            context.fill(left + 2, y + 144, left + 2 + currentWidth, y + 148, 0xFF757575);
            context.fill(left + 2 + currentWidth, y + 144, left + 2 + repairedWidth, y + 148,
                    canSubmit() ? 0xFF90AD8C : 0xFF586455);

            if (!donorMode && !cost.isEmpty()) {
                int quantityY = bottom() - 42;
                drawButton(context, Text.literal("−"), left, quantityY, 20, 15, mx, my, amount > 1);
                drawButton(context, Text.literal("+"), left + w - 20, quantityY, 20, 15, mx, my, amount < fullCost());
                CompendiumStyle.drawRailPlaque(context, left + 24, quantityY, w - 48, 15, false, false);
                context.drawCenteredTextWithShadow(font, Integer.toString(amount), left + w / 2, quantityY + 4, TEXT);
            }
        } else {
            line(context, message("repair_choose"), left, y + 35, w, MUTED);
        }
        if (feedback != null && System.currentTimeMillis() < feedbackUntil) {
            line(context, feedback, left, bottom() - 60, w, feedbackColor);
        }
        drawButton(context, message(pending == null ? "repair_action" : "repair_waiting"), left, bottom() - 20, w, 19, mx, my, canSubmit());
    }

    private void drawMode(DrawContext context, String label, int left, int top, int w, int mx, int my, boolean active) {
        CompendiumButtonWidget.drawButton(context, left, top, w, 15,
                inside(mx, my, left, top, w, 15), true, active, message(label).getString());
    }

    private ItemStack materialIcon() {
        var candidates = materials.ingredient().getMatchingStacks();
        if (candidates.length == 0) return ItemStack.EMPTY;
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.stack(i);
            if (!stack.isEmpty() && storage.slot(i) != screen.getSlot(target.slot).getIndex()
                    && materials.ingredient().test(stack)) return stack;
        }
        return candidates[0];
    }

    void tooltip(DrawContext context, int mx, int my) {
        int row = (my - listTop() + scroll) / ROW;
        if (row >= 0 && row < rows.size() && inside(mx, my, x + 8, listTop(), divider - x - 16, bottom() - listTop())) {
            context.drawItemTooltip(font, rows.get(row).stack, mx, my);
        } else if (target != null && inside(mx, my, details(), y + 34, detailWidth(), 22)) {
            context.drawItemTooltip(font, target.stack, mx, my);
        } else if (target != null && inside(mx, my, details(), y + 87, detailWidth(), 27)) {
            if (!donorMode && materials.ingredient().getMatchingStacks().length > 1) {
                List<Text> accepted = new ArrayList<>();
                accepted.add(message("repair_accepted"));
                for (ItemStack stack : materials.ingredient().getMatchingStacks()) accepted.add(stack.getName());
                context.drawTooltip(font, accepted, mx, my);
                return;
            }
            ItemStack stack = donorMode ? donor == null ? ItemStack.EMPTY : donor.stack : materialIcon();
            if (!stack.isEmpty()) context.drawItemTooltip(font, stack, mx, my);
        }
    }

    void click(double mx, double my, int button) {
        if (button != 0) return;
        captured = true;
        refresh();
        if (pending != null) return;
        int left = details(), w = detailWidth();
        if (choosingDonor && inside(mx, my, x + 8, y + 30, divider - x - 14, 15)) {
            choosingDonor = false;
            scroll = 0;
        } else if (inside(mx, my, divider - 7, listTop(), 6, bottom() - listTop()) && scrollbar().maxScroll() > 0) {
            dragging = true;
            grab = scrollbar().grabOffset(my, scroll);
            scroll = scrollbar().scrollAt(my, grab);
        } else if (inside(mx, my, x + 8, listTop(), divider - x - 16, bottom() - listTop())) {
            int index = ((int) my - listTop() + scroll) / ROW;
            if (index < rows.size()) {
                Gear row = rows.get(index);
                if (choosingDonor) donor = new Gear(row.slot, row.stack.copy());
                else select(row);
                feedback = null;
            }
        } else if (target != null) {
            if (inside(mx, my, left, y + 64, w / 2 - 2, 15)) {
                donorMode = choosingDonor = false;
                scroll = 0;
            } else if (inside(mx, my, left + w / 2 + 2, y + 64, w - w / 2 - 2, 15)) {
                donorMode = choosingDonor = true;
                scroll = 0;
            } else if (!donorMode && inside(mx, my, left, bottom() - 42, 20, 15)) {
                amount = Math.max(1, amount - 1);
            } else if (!donorMode && inside(mx, my, left + w - 20, bottom() - 42, 20, 15)) {
                amount = Math.min(fullCost(), amount + 1);
            } else if (inside(mx, my, left, bottom() - 20, w, 19) && canSubmit()) {
                var request = new RepairGearPayload(screen.syncId, MaterialCraftingPanel.nextRequest(), target.source(),
                        donorMode ? 0 : amount, donorMode ? Optional.of(donor.source()) : Optional.empty());
                var repaired = target.stack.copy();
                repaired.setDamage(repaired.getDamage() - restored());
                submit.accept(request, repaired);
            }
        }
    }

    void send(RepairGearPayload request) {
        pending = request;
        feedback = null;
        ClientPlayNetworking.send(request);
    }

    void receive(CraftMaterialsResultPayload payload) {
        if (pending == null || pending.request() != payload.request()) return;
        int slot = pending.target().slot();
        pending = null;
        boolean success = payload.result() > 0;
        if (success) {
            storage = new MaterialCrafting.Storage(inventory);
            ItemStack stack = screen.getSlot(slot).getStack();
            select(EquipmentRepair.canRepair(stack) ? new Gear(slot, stack) : null);
        }
        feedback = success ? message("repair_success", payload.result()) : message("changed");
        feedbackColor = success ? SUCCESS : WARNING;
        feedbackUntil = System.currentTimeMillis() + 2800;
    }

    boolean release(int button) {
        if (button != 0 || !captured) return false;
        dragging = captured = false;
        return true;
    }

    boolean drag(double my, int button) {
        if (button != 0 || !captured) return false;
        if (dragging) scroll = scrollbar().scrollAt(my, grab);
        return true;
    }

    void scroll(double mx, double vertical) {
        if (mx < divider) scroll = Math.clamp(scroll - (int) (vertical * ROW * 2), 0, scrollbar().maxScroll());
    }

    boolean contains(double mx, double my) { return inside(mx, my, x, y, width, height); }
    private int details() { return divider + 6; }
    private int detailWidth() { return x + width - 8 - details(); }
    private int bottom() { return y + height - 8; }
    private int listTop() { return y + 49; }
    private StatsScrollbar scrollbar() { return new StatsScrollbar(listTop(), bottom() - listTop(), rows.size() * ROW); }

    private void line(DrawContext context, Text text, int left, int top, int w, int color) {
        String value = text.getString();
        if (font.getWidth(value) > w) value = font.trimToWidth(value, Math.max(0, w - 6)) + "…";
        context.drawText(font, value, left, top, color, false);
    }
}

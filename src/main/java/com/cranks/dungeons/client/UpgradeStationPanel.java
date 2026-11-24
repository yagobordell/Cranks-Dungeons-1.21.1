package com.cranks.dungeons.client;

import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.item.ModItems;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.UpgradeStationPayload;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.message;

final class UpgradeStationPanel {
    private static final ItemStack ICON = Items.SMITHING_TABLE.getDefaultStack();
    private final QualityStationScreenHandler bench;
    private final Text title;
    private final Text equipmentLabel = message("upgrade_equipment");
    private TextRenderer font;
    private int x, y, width, height;
    private int amount = 1, pending;
    private boolean slots, cancelPending, captured;
    private ItemStack lastMaterial = ItemStack.EMPTY;
    private Text feedback;
    private int feedbackColor;
    private long feedbackUntil;

    UpgradeStationPanel(QualityStationScreenHandler bench, Text title) {
        this.bench = bench;
        this.title = title;
    }

    void init(TextRenderer font, int x, int y, int width, int height) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        captured = false;
    }

    int slotX(int id) {
        return x + width / 2 - 8 + (id == QualityStationScreenHandler.EQUIPMENT ? -42 : 42);
    }

    int slotY() { return y + 72; }
    private int left() { return x + 12; }
    private int innerWidth() { return width - 24; }
    private int actionTop() { return y + height - 27; }
    private int controlsTop() { return actionTop() - 20; }

    private ItemStack expectedMaterial() {
        if (slots) return ModItems.DEEPSLATE_SIGIL.getDefaultStack();
        var kind = QualityStationBlockEntity.upgradeMaterial(bench.equipment());
        if (kind == null) return ItemStack.EMPTY;
        return switch (kind) {
            case ARMOR -> ModItems.ARMOURERS_SCRAP.getDefaultStack();
            case WEAPON -> ModItems.BLACKSMITHS_WHETSTONE.getDefaultStack();
        };
    }

    private int limit() {
        ItemStack material = bench.material();
        if (material.isEmpty() || QualityStationBlockEntity.isSigil(material) != slots) return 0;
        return QualityStationBlockEntity.upgradeLimit(bench.equipment(), material);
    }

    private void update() {
        ItemStack material = bench.material();
        if (!ItemStack.areItemsEqual(material, lastMaterial)) {
            if (!material.isEmpty()) slots = QualityStationBlockEntity.isSigil(material);
            lastMaterial = material.copy();
        }
        amount = Math.clamp(amount, 1, Math.max(1, limit()));
    }

    void render(DrawContext context, int mx, int my) {
        update();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        context.drawItem(ICON, x + 8, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,title,x+29,y+11,width-40,CompendiumStyle.TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("quality"));
        InventoryStyle.drawDivider(context, x + 8, x + width - 8, y + 24);
        int half = innerWidth() / 2 - 2;
        tab(context, "upgrade_quality", left(), half, mx, my, !slots);
        tab(context, "upgrade_slots", left() + half + 4, innerWidth() - half - 4, mx, my, slots);
        drawSlot(context, QualityStationScreenHandler.EQUIPMENT, equipmentLabel, ItemStack.EMPTY, mx, my);
        drawSlot(context, QualityStationScreenHandler.MATERIAL, message("upgrade_material"), expectedMaterial(), mx, my);
        renderPreview(context);
        renderProgress(context);

        if (!bench.processing()) {
            boolean enabled = pending == 0;
            drawButton(context, Text.literal("−"), left(), controlsTop(), 22, 15, mx, my, enabled && amount > 1);
            drawButton(context, Text.literal("+"), left() + innerWidth() - 22,
                    controlsTop(), 22, 15, mx, my, enabled && amount < limit());
            CompendiumStyle.drawRailPlaque(context, left() + 26, controlsTop(), innerWidth() - 52, 15, false, false);
            context.drawCenteredTextWithShadow(font, message("upgrade_amount", amount), x + width / 2,
                    controlsTop() + 4, limit() > 0 ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_MUTED);
        }
        drawButton(context, message(bench.processing() ? "upgrade_cancel" : "upgrade_start"),
                left(), actionTop(), innerWidth(), 19, mx, my,
                pending == 0 && (bench.processing() ? bench.canCancel() : amount <= limit()));
    }

    private void tab(DrawContext context, String key, int left, int w, int mx, int my, boolean active) {
        CompendiumButtonWidget.drawButton(context, left, y + 31, w, 17,
                inside(mx, my, left, y + 31, w, 17), !bench.processing(), active, message(key).getString());
    }

    private void drawSlot(DrawContext context, int id, Text label, ItemStack example, int mx, int my) {
        int left = slotX(id), top = slotY();
        context.drawCenteredTextWithShadow(font, label, left + 8, top - 15, CompendiumStyle.TEXT_MUTED);
        InventoryStyle.drawSocket(context, left - 3, top - 3, 22, 22,
                inside(mx, my, left, top, 16, 16) && !bench.processing());
        if (bench.getSlot(id).getStack().isEmpty()) {
            if (id != QualityStationScreenHandler.EQUIPMENT && example.isEmpty()) return;
            var hint = id == QualityStationScreenHandler.EQUIPMENT ? StationSlotHints.Kind.EQUIPMENT
                    : slots ? StationSlotHints.Kind.SIGIL : example.isOf(ModItems.BLACKSMITHS_WHETSTONE)
                    ? StationSlotHints.Kind.WHETSTONE : example.isOf(ModItems.ARMOURERS_SCRAP)
                    ? StationSlotHints.Kind.SCRAP : StationSlotHints.Kind.MATERIAL;
            StationSlotHints.draw(context, hint, left, top);
        }
    }

    private void renderPreview(DrawContext context) {
        ItemStack gear = bench.equipment();
        if (gear.isEmpty()) {
            center(context, message("upgrade_choose"), y + 105, CompendiumStyle.TEXT_MUTED);
            return;
        }
        ItemStack expected = expectedMaterial();
        if (expected.isEmpty()) {
            center(context, message("upgrade_ineligible"), y + 105, CompendiumStyle.TEXT_MUTED);
            return;
        }
        int current = slots ? EnchantmentSlotHelper.getEnchantmentSlots(gear) : QualityStationBlockEntity.getQuality(gear);
        int cap = slots ? EnchantmentSlotHelper.getMaxEnchantmentSlots(gear) : QualityStationBlockEntity.QUALITY_CAP;
        int batch = bench.processing() ? bench.remaining() : amount;
        int minimum = Math.min(cap, current + batch);
        int maximum = Math.min(cap, current + 2 * batch);
        int color = current < cap ? CompendiumStyle.TEXT_SUCCESS : CompendiumStyle.TEXT_MUTED;
        Text result = slots ? message("upgrade_slot_preview", current, cap, minimum, cap)
                : message(minimum == maximum ? "upgrade_quality_preview" : "upgrade_quality_range", current, minimum, maximum);
        center(context, current >= cap ? message("upgrade_at_cap") : result, y + 105, color);
        if (slots) {
            center(context, message("upgrade_slot_hint"), y + 118, CompendiumStyle.TEXT_MUTED);
        } else if (current + 1 < cap) {
            center(context, message("upgrade_chance", QualityStationBlockEntity.BONUS_CHANCE), y + 118, CompendiumStyle.TEXT_MUTED);
        }
        if (bench.material().isEmpty() && current < cap) {
            center(context, message("upgrade_needs", expected.getName()), y + 133, CompendiumStyle.TEXT_MUTED);
        } else if (!bench.material().isEmpty() && QualityStationBlockEntity.isSigil(bench.material()) != slots) {
            center(context, message("upgrade_material_mode"), y + 133, CompendiumStyle.TEXT_MUTED);
        }
    }

    private void renderProgress(DrawContext context) {
        if (bench.processing()) {
            int total = QualityStationBlockEntity.ticksPerUpgrade(bench.material());
            int remainingSeconds = Math.ceilDiv(Math.max(0, total - bench.progress()), 20);
            center(context, message("upgrade_progress", remainingSeconds, bench.remaining()), y + 155, CompendiumStyle.TEXT);
            CompendiumStyle.drawRailPlaque(context, left(), y + 170, innerWidth(), 8, false, false);
            int fill = (innerWidth() - 4) * Math.clamp(bench.progress(), 0, total) / total;
            if (fill > 0) context.fill(left() + 2, y + 172, left() + 2 + fill, y + 176, 0xFFA7BFA2);
            center(context, batchTime(), y + 184, CompendiumStyle.TEXT_MUTED);
            return;
        } else if (feedback != null && System.currentTimeMillis() < feedbackUntil) {
            center(context, feedback, y + 158, feedbackColor);
        } else if (bench.completed()) {
            center(context, message("upgrade_complete"), y + 158, CompendiumStyle.TEXT_SUCCESS);
        }
        if (limit() > 0) center(context, batchTime(), controlsTop() - 12, CompendiumStyle.TEXT_MUTED);
    }

    private Text batchTime() {
        boolean processing = bench.processing();
        ItemStack material = bench.material();
        ItemStack gear = bench.equipment();
        int cycles = processing ? Math.min(bench.remaining(), QualityStationBlockEntity.upgradeLimit(gear, material)) : amount;
        if (cycles == 0 || material.isEmpty()) return Text.empty();
        int ticks = cycles * QualityStationBlockEntity.ticksPerUpgrade(material) - (processing ? bench.progress() : 0);
        int seconds = Math.ceilDiv(Math.max(0, ticks), 20);
        Text duration;
        if (seconds < 60) duration = message("upgrade_seconds", seconds);
        else if (seconds % 60 == 0) duration = message("upgrade_minutes", seconds / 60);
        else duration = message("upgrade_minutes_seconds", seconds / 60, seconds % 60);
        int qualityRoom = QualityStationBlockEntity.QUALITY_CAP - QualityStationBlockEntity.getQuality(gear);
        boolean canEndEarly = !QualityStationBlockEntity.isSigil(material) && qualityRoom <= 2 * (cycles - 1);
        if (canEndEarly) duration = message("upgrade_up_to", duration);
        return message(processing ? "upgrade_batch_left" : "upgrade_batch_time", duration);
    }

    private void center(DrawContext context, Text line, int top, int color) {
        String text = line.getString();
        if (font.getWidth(text) > innerWidth()) text = font.trimToWidth(text, innerWidth() - 6) + "…";
        context.drawCenteredTextWithShadow(font, text, x + width / 2, top, color);
    }

    void tooltip(DrawContext context, int mx, int my) {
        if (bench.processing() && !bench.canCancel() && inside(mx, my, left(), actionTop(), innerWidth(), 19)) {
            context.drawTooltip(font, message("upgrade_cancel_owner"), mx, my);
            return;
        }
        if (inside(mx, my, slotX(QualityStationScreenHandler.MATERIAL), slotY(), 16, 16) && bench.material().isEmpty()) {
            ItemStack expected = expectedMaterial();
            if (!expected.isEmpty()) context.drawItemTooltip(font, expected, mx, my);
        }
    }

    void click(double mx, double my, int button) {
        if (button != 0) return;
        captured = true;
        update();
        if (pending != 0) return;
        if (inside(mx, my, left(), actionTop(), innerWidth(), 19)) {
            if (bench.processing()) {
                if (bench.canCancel()) submit(0);
            } else if (amount <= limit()) {
                submit(amount);
            }
        } else if (!bench.processing()) {
            int half = innerWidth() / 2 - 2;
            if (inside(mx, my, left(), y + 31, half, 17)) slots = false;
            if (inside(mx, my, left() + half + 4, y + 31, innerWidth() - half - 4, 17)) slots = true;
            if (inside(mx, my, left(), controlsTop(), 22, 15)) amount = Math.max(1, amount - 1);
            if (amount < limit() && inside(mx, my, left() + innerWidth() - 22, controlsTop(), 22, 15)) amount++;
        }
    }

    private void submit(int count) {
        pending = MaterialCraftingPanel.nextRequest();
        cancelPending = count == 0;
        feedback = null;
        ClientPlayNetworking.send(new UpgradeStationPayload(bench.syncId, pending, count, bench.jobId(),
                bench.equipment().copy(), bench.material().copy()));
    }

    void receive(CraftMaterialsResultPayload result) {
        if (pending == 0 || pending != result.request()) return;
        pending = 0;
        feedback = switch (result.result()) {
            case CraftMaterialsResultPayload.LOCKED -> message("upgrade_cancel_owner");
            case CraftMaterialsResultPayload.UNAVAILABLE, CraftMaterialsResultPayload.INVENTORY_CHANGED -> message("changed");
            default -> cancelPending ? message("upgrade_cancelled") : null;
        };
        feedbackColor = result.result() > 0 ? CompendiumStyle.TEXT_MUTED : CompendiumStyle.TEXT_WARNING;
        feedbackUntil = System.currentTimeMillis() + 2800;
    }

    boolean release(int button) {
        if (button != 0 || !captured) return false;
        captured = false;
        return true;
    }

    boolean contains(double mx, double my) { return inside(mx, my, x, y, width, height); }
}

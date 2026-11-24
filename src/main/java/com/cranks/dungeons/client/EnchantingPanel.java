package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.equipment.EnchantmentSlotHelper;
import com.cranks.dungeons.inventory.EquipmentEnchanting;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.EnchantGearPayload;
import com.cranks.dungeons.screen.LoadoutEnchantingHandler;
import com.cranks.dungeons.screen.LoadoutStationHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.message;

final class EnchantingPanel {
    private static final int ROW = 19, PAD = 8;
    private static final int TEXT = CompendiumStyle.TEXT, MUTED = CompendiumStyle.TEXT_MUTED;
    private static final int SUCCESS = CompendiumStyle.TEXT_SUCCESS, WARNING = CompendiumStyle.TEXT_WARNING;
    private static final ItemStack TABLE = Items.ENCHANTING_TABLE.getDefaultStack();
    private static final ItemStack LAPIS = Items.LAPIS_LAZULI.getDefaultStack();

    private enum View { ADD, MANAGE, REPLACE }

    private record Gear(int slot, ItemStack stack) {}

    private record Offer(RegistryEntry<Enchantment> enchantment, Text name, int cost, List<Text> effects) {
        static Offer of(RegistryEntry<Enchantment> enchantment) {
            String path = enchantment.getKey().orElseThrow().getValue().getPath();
            String prefix = "enchantment.cranks-dungeons." + path;
            List<Text> effects = new ArrayList<>();
            for (String suffix : List.of(".desc", ".desc2")) {
                if (Language.getInstance().hasTranslation(prefix + suffix)) effects.add(Text.translatable(prefix + suffix));
            }
            return new Offer(enchantment, enchantment.value().description(), EquipmentEnchanting.lapisCost(enchantment),
                    List.copyOf(effects));
        }
    }

    private final LoadoutEnchantingHandler handler;
    private final PlayerInventory inventory;
    private final Text title;
    private final BiConsumer<EnchantGearPayload, ItemStack> submit;
    private final List<Offer> catalogue;
    private final Map<Item, List<Offer>> byItem = new HashMap<>();
    private List<Gear> gearRows = List.of();
    private List<Offer> offerRows = List.of();
    private List<RegistryEntry<Enchantment>> installed = List.of();
    private Gear target;
    private View view = View.ADD;
    private RegistryEntry<Enchantment> editingCopy;
    private ItemStack availableGear = ItemStack.EMPTY;
    private ItemStack preview = ItemStack.EMPTY;
    private List<EnchantingStatPreview.Change> changes = List.of();
    private Offer selected;
    private MaterialCrafting.Storage storage;
    private int lapis, pending, scroll;
    private boolean capturing, dragging;
    private double grab;
    private long feedbackUntil;
    private Text feedback;
    private int feedbackColor;
    private int x, y, width, height, divider;
    private TextRenderer font;
    private TextFieldWidget search;

    EnchantingPanel(LoadoutEnchantingHandler handler, PlayerInventory inventory, Text title,
                    BiConsumer<EnchantGearPayload, ItemStack> submit) {
        this.handler = handler;
        this.inventory = inventory;
        this.title = title;
        this.submit = submit;
        catalogue = inventory.player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).streamEntries()
                .filter(entry -> entry.isIn(EnchantmentTags.IN_ENCHANTING_TABLE) && com.cranks.dungeons.util.EnchantmentFilterUtil.isApproved(entry))
                .map(Offer::of)
                .sorted(Comparator.comparingInt(Offer::cost).thenComparing(offer -> offer.name().getString()))
                .toList();
    }

    void init(TextRenderer font, int x, int y, int width, int height) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        divider = x + width * 44 / 100;
        String query = search == null ? "" : search.getText();
        search = new TextFieldWidget(font, x + PAD + 3, searchY() + 3, leftWidth() - 6, 10, message("search"));
        search.setDrawsBackground(false);
        search.setMaxLength(80);
        search.setPlaceholder(message(target == null ? "enchant_search_gear" : "enchant_search_offer"));
        search.setText(query);
        search.setChangedListener(value -> {
            scroll = 0;
            filter();
        });
        capturing = dragging = false;
        refresh();
        filter();
    }

    private List<Offer> supported(ItemStack stack) {
        return byItem.computeIfAbsent(stack.getItem(), item -> catalogue.stream()
                .filter(offer -> EquipmentEnchanting.supports(item.getDefaultStack(), offer.enchantment())).toList());
    }

    private void filter() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        if (target == null) {
            var rows = new ArrayList<Gear>();
            for (int id = 5; id < LoadoutStationHandler.PLAYER_SLOT_END; id++) {
                var slot = handler.getSlot(id);
                ItemStack stack = slot.getStack();
                if (slot.inventory == inventory && slot.isEnabled() && stack.getCount() == 1 && !stack.isOf(Items.ENCHANTED_BOOK)
                        && stack.getName().getString().toLowerCase(Locale.ROOT).contains(query)
                        && (!stack.getEnchantments().isEmpty() || !supported(stack).isEmpty())) rows.add(new Gear(id, stack));
            }
            gearRows = rows;
        } else if (view == View.MANAGE) {
            offerRows = List.of();
        } else {
            offerRows = supported(target.stack()).stream()
                    .filter(offer -> !offer.enchantment().equals(editingCopy) && EquipmentEnchanting.canApply(availableGear, offer.enchantment()))
                    .filter(offer -> offer.name().getString().toLowerCase(Locale.ROOT).contains(query)).toList();
            if (selected != null && !offerRows.contains(selected)) select(null);
        }
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    private void refresh() {
        if (storage == null || !storage.isCurrent()) {
            storage = new MaterialCrafting.Storage(inventory);
            lapis = EquipmentEnchanting.availableLapis(storage);
        }
        if (target != null && pending == 0) {
            var slot = handler.getSlot(target.slot());
            if (!slot.isEnabled() || !ItemStack.areEqual(slot.getStack(), target.stack())) {
                choose(null);
                showFeedback(message("changed"), WARNING);
            }
        }
        if (target == null) filter();
    }

    private void choose(Gear gear) {
        target = gear == null ? null : new Gear(gear.slot(), gear.stack().copy());
        installed = target == null ? List.of()
                : target.stack().getEnchantments().getEnchantments().stream()
                        .sorted(Comparator.comparing(entry -> entry.value().description().getString())).toList();
        setView(!installed.isEmpty() && !hasOpenSlot() ? View.MANAGE : View.ADD, null);
    }

    private boolean hasOpenSlot() {
        return target != null && EquipmentEnchanting.hasOpenSlot(target.stack());
    }

    private void setView(View next, RegistryEntry<Enchantment> removed) {
        view = next;
        editingCopy = removed;
        availableGear = target == null ? ItemStack.EMPTY : removed == null
                ? target.stack() : EquipmentEnchanting.withoutOneCopy(target.stack(), removed);
        select(null);
        scroll = 0;
        search.setY(searchY() + 3);
        search.setPlaceholder(message(target == null ? "enchant_search_gear" : "enchant_search_offer"));
        search.setText("");
        filter();
    }

    private String navigationKey() {
        return switch (view) {
            case ADD -> installed.isEmpty() ? null : "enchant_manage";
            case MANAGE -> hasOpenSlot() ? "enchant_add" : null;
            case REPLACE -> "enchant_change_copy";
        };
    }

    private void select(Offer offer) {
        selected = offer;
        preview = view == View.MANAGE && editingCopy != null ? availableGear
                : offer == null ? ItemStack.EMPTY : EquipmentEnchanting.withEnchantment(availableGear, offer.enchantment());
        changes = preview.isEmpty() ? List.of() : EnchantingStatPreview.compare(target.stack(), preview,
                inventory.player.getPreferredEquipmentSlot(target.stack()));
    }

    void render(DrawContext context, int mx, int my, float delta) {
        refresh();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        context.drawItem(TABLE, x + PAD, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,title,x+29,y+11,width-40,TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("enchantments"));
        InventoryStyle.drawDivider(context, x + PAD, x + width - PAD, y + 24);
        context.fill(divider, y + 29, divider + 1, bottom(), 0xFF3A3A3A);
        if (target != null) drawButton(context, message("enchant_change_gear"), x + PAD, y + 30, leftWidth(), 15, mx, my, pending == 0);
        String navigation = navigationKey();
        if (navigation != null) drawButton(context, message(navigation), x + PAD, y + 48, leftWidth(), 15, mx, my, pending == 0);
        if (view == View.MANAGE) {
            line(context, message("enchant_installed"), x + PAD + 2, searchY() + 4, leftWidth() - 4, MUTED);
        } else {
            CompendiumStyle.drawTextFieldFrame(context, x + PAD, searchY(), leftWidth(), 15, search.isFocused());
            search.render(context, mx, my, delta);
        }
        renderList(context, mx, my);
        renderDetails(context, mx, my);
    }

    private void renderList(DrawContext context, int mx, int my) {
        var bar = scrollbar();
        int left = x + PAD, w = leftWidth() - 7;
        if (rows() == 0) {
            boolean searching = !search.getText().isBlank();
            Text empty = message(target == null ? searching ? "enchant_no_gear_matches" : "enchant_no_gear"
                    : searching ? "enchant_no_offer_matches" : "enchant_no_options");
            wrapped(context, empty, left + 2, listTop() + 3, leftWidth() - 3, MUTED);
            return;
        }
        context.enableScissor(left, listTop(), divider - 6, bottom());
        int hovered = rowAt(mx, my);
        for (int i = scroll / ROW; i < rows() && i * ROW - scroll < bar.height(); i++) {
            int sy = listTop() + i * ROW - scroll;
            Offer offer = target == null || view == View.MANAGE ? null : offerRows.get(i);
            boolean active = view == View.MANAGE ? installed.get(i).equals(editingCopy) : offer != null && offer == selected;
            CompendiumStyle.drawRailPlaque(context, left, sy, w, ROW - 1, active, hovered == i);
            if (view == View.MANAGE) {
                line(context, installedName(installed.get(i)), left + 4, sy + 5, w - 8, TEXT);
            } else if (target == null) {
                ItemStack stack = gearRows.get(i).stack();
                context.drawItem(stack, left + 2, sy + 1);
                line(context, stack.getName(), left + 20, sy + 5, w - 23, TEXT);
            } else {
                String cost = Integer.toString(offer.cost());
                int costWidth = font.getWidth(cost);
                line(context, offer.name(), left + 4, sy + 5, w - costWidth - 11, TEXT);
                context.drawText(font, cost, left + w - costWidth - 4, sy + 5, MUTED, false);
            }
        }
        context.disableScissor();
        if (bar.maxScroll() > 0) {
            int thumb = (int) Math.round(bar.thumbTop(scroll));
            context.fill(divider - 6, listTop(), divider - 3, bottom(), 0xFF090909);
            context.fill(divider - 6, thumb, divider - 3, thumb + bar.thumbHeight(), dragging ? 0xFFA0A0A0 : 0xFF737373);
        }
    }

    private void renderDetails(DrawContext context, int mx, int my) {
        int left = divider + 7, w = detailWidth();
        if (target == null) {
            wrapped(context, message("enchant_choose_gear"), left, y + 35, w, MUTED);
            if (feedback != null && System.currentTimeMillis() < feedbackUntil) {
                wrapped(context, feedback, left, bottom() - 35, w, feedbackColor);
            }
            return;
        }
        ItemStack stack = target.stack();
        int capacity = EquipmentEnchanting.capacity(stack), used = EnchantmentSlotHelper.usedSlots(stack);
        InventoryStyle.drawSocket(context, left, y + 32, 22, 22, false);
        context.drawItem(stack, left + 3, y + 35);
        line(context, stack.getName(), left + 27, y + 34, w - 27, TEXT);
        line(context, message("enchant_slots_used", used, capacity), left + 27, y + 46, w - 27, MUTED);
        InventoryStyle.drawDivider(context, left, left + w, y + 62);
        if (view == View.MANAGE) {
            if (editingCopy == null) {
                wrapped(context, message("enchant_choose_copy"), left, y + 72, w, MUTED);
            } else {
                int top = wrapped(context, message("enchant_removing", editingCopy.value().description()), left, y + 72, w, WARNING) + 3;
                renderChanges(context, left, top, w);
                line(context, message("enchant_remove_free"), left, bottom() - 47, w, MUTED);
            }
        } else if (capacity == 0) {
            wrapped(context, message("enchant_no_slots"), left, y + 72, w, WARNING);
        } else {
            int top = y + 72;
            if (view == View.REPLACE) {
                top = wrapped(context, message("enchant_replacing", editingCopy.value().description()), left, top, w, WARNING) + 2;
            }
            if (selected == null) {
                wrapped(context, message("enchant_choose_offer"), left, top, w, MUTED);
            } else renderPreview(context, left, top, w);
        }
        if (feedback != null && System.currentTimeMillis() < feedbackUntil) {
            line(context, feedback, left, bottom() - 33, w, feedbackColor);
        }
        if (view == View.MANAGE) {
            drawButton(context, message(pending == 0 ? "enchant_remove" : "working"),
                    left, bottom() - 20, editButtonWidth(), 19, mx, my, canSubmit());
            drawButton(context, message("enchant_replace"),
                    editReplaceX(), bottom() - 20, w - editButtonWidth() - 4, 19, mx, my, canReplace());
        } else {
            String action = view == View.REPLACE ? "enchant_replace" : "enchant_action";
            drawButton(context, message(pending == 0 ? action : "working"), left, bottom() - 20, w, 19, mx, my, canSubmit());
        }
    }

    private void renderPreview(DrawContext context, int left, int top, int w) {
        int copies = copiesAfterApplying(selected);
        top = wrapped(context, EnchantmentSlotHelper.displayName(selected.enchantment(), copies), left, top, w, TEXT) + 3;
        if (top < bottom() - 62) {
            context.enableScissor(left, top, left + w, bottom() - 62);
            top = renderChanges(context, left, top, w);
            if (showEffectDescriptions()) {
                for (Text effect : selected.effects()) top = wrapped(context, effect, left, top, w, MUTED) + 3;
            }
            context.disableScissor();
        }
        context.drawItem(LAPIS, left, bottom() - 57);
        line(context, message("enchant_lapis_cost", selected.cost()), left + 20, bottom() - 59, w - 20, TEXT);
        line(context, inventory.player.isCreative() ? message("enchant_creative")
                : message("enchant_lapis_stock", lapis), left + 20, bottom() - 47, w - 20,
                canSubmit() || pending != 0 ? MUTED : WARNING);
    }

    private int renderChanges(DrawContext context, int left, int top, int w) {
        for (var change : changes) {
            int valueWidth = font.getWidth(change.values());
            line(context, change.label(), left, top, w - valueWidth - 4, MUTED);
            context.drawText(font, change.values(), left + w - valueWidth, top, change.increased() ? SUCCESS : WARNING, false);
            top += 11;
        }
        return top;
    }

    private boolean showEffectDescriptions() {
        return changes.isEmpty() || selected.enchantment().value().effects().getOrDefault(
                EnchantmentEffectComponentTypes.ATTRIBUTES, List.of()).isEmpty();
    }

    private Text installedName(RegistryEntry<Enchantment> enchantment) {
        int copies = EnchantmentSlotHelper.copyCount(enchantment, target.stack().getEnchantments().getLevel(enchantment));
        return com.cranks.dungeons.client.guide.GuideText.link(EnchantmentSlotHelper.displayName(enchantment,copies),
                new com.cranks.dungeons.client.guide.GuideTarget(com.cranks.dungeons.client.guide.GuideTarget.Kind.ENCHANTMENT,enchantment.getKey().orElseThrow().getValue().toString()));
    }

    void tooltip(DrawContext context, int mx, int my) {
        int row = rowAt(mx, my);
        if (row >= 0 && target == null) {
            context.drawItemTooltip(font, gearRows.get(row).stack(), mx, my);
        } else if (row >= 0 && view == View.MANAGE) {
            context.drawTooltip(font, installedName(installed.get(row)), mx, my);
        } else if (row >= 0) {
            Offer offer = offerRows.get(row);
            List<Text> tooltip = new ArrayList<>();
            int copies = copiesAfterApplying(offer);
            tooltip.add(com.cranks.dungeons.client.guide.GuideText.link(EnchantmentSlotHelper.displayName(offer.enchantment(),copies),
                    new com.cranks.dungeons.client.guide.GuideTarget(com.cranks.dungeons.client.guide.GuideTarget.Kind.ENCHANTMENT,offer.enchantment().getKey().orElseThrow().getValue().toString())));
            if (copies > 1 && !offer.effects().isEmpty()) tooltip.add(Text.translatable("enchantment.cranks-dungeons.per_copy"));
            tooltip.addAll(offer.effects());
            tooltip.add(message("enchant_lapis_cost", offer.cost()));
            context.drawTooltip(font, tooltip, mx, my);
        } else if (target != null && inside(mx, my, divider + 7, y + 32, detailWidth(), 22)) {
            context.drawItemTooltip(font, target.stack(), mx, my);
        } else if (!preview.isEmpty() && inside(mx, my, divider + 7, y + 72, detailWidth(), bottom() - 62 - (y + 72))) {
            List<Text> tooltip = new ArrayList<>();
            if (editingCopy != null) {
                tooltip.add(message(view == View.MANAGE ? "enchant_removing" : "enchant_replacing", editingCopy.value().description()));
            }
            if (selected != null) tooltip.add(EnchantmentSlotHelper.displayName(selected.enchantment(), copiesAfterApplying(selected)));
            for (var change : changes) tooltip.add(change.tooltip());
            if (selected != null && showEffectDescriptions()) tooltip.addAll(selected.effects());
            context.drawTooltip(font, tooltip, mx, my);
        }
    }

    void click(double mx, double my, int button, Consumer<TextFieldWidget> focus) {
        if (button != 0) return;
        capturing = true;
        refresh();
        if (view != View.MANAGE && inside(mx, my, x + PAD, searchY(), leftWidth(), 15)) {
            focus.accept(search);
            search.mouseClicked(mx, my, button);
            return;
        }
        focus.accept(null);
        if (pending != 0) return;
        if (target != null && inside(mx, my, x + PAD, y + 30, leftWidth(), 15)) {
            choose(null);
            feedback = null;
        } else if (navigationKey() != null && inside(mx, my, x + PAD, y + 48, leftWidth(), 15)) {
            switch (view) {
                case ADD -> setView(View.MANAGE, null);
                case MANAGE -> setView(View.ADD, null);
                case REPLACE -> setView(View.MANAGE, editingCopy);
            }
            feedback = null;
        } else if (inside(mx, my, divider - 7, listTop(), 6, bottom() - listTop()) && scrollbar().maxScroll() > 0) {
            dragging = true;
            grab = scrollbar().grabOffset(my, scroll);
            scroll = scrollbar().scrollAt(my, grab);
        } else if (inside(mx, my, divider + 7, bottom() - 20, detailWidth(), 19)) {
            if (view == View.MANAGE && mx >= editReplaceX()) {
                if (canReplace()) setView(View.REPLACE, editingCopy);
            } else if (canSubmit() && (view != View.MANAGE || mx < divider + 7 + editButtonWidth())) {
                var request = new EnchantGearPayload(handler.syncId, MaterialCraftingPanel.nextRequest(), target.slot(), target.stack(),
                        Optional.ofNullable(selected).map(offer -> offer.enchantment().getKey().orElseThrow().getValue()),
                        Optional.ofNullable(editingCopy).map(entry -> entry.getKey().orElseThrow().getValue()));
                submit.accept(request, preview);
            }
        } else {
            int row = rowAt(mx, my);
            if (row >= 0) {
                if (target == null) choose(gearRows.get(row));
                else if (view == View.MANAGE) setView(View.MANAGE, installed.get(row));
                else select(offerRows.get(row));
                feedback = null;
            }
        }
    }

    void receive(CraftMaterialsResultPayload response) {
        if (pending == 0 || pending != response.request()) return;
        pending = 0;
        if (response.result() > 0) {
            String success = switch (view) {
                case ADD -> "enchant_success";
                case MANAGE -> "enchant_removed";
                case REPLACE -> "enchant_replaced";
            };
            var slot = handler.getSlot(target.slot());
            choose(slot.hasStack() && slot.isEnabled() ? new Gear(target.slot(), slot.getStack()) : null);
            showFeedback(message(success), SUCCESS);
        } else {
            refresh();
            showFeedback(message(response.result() == CraftMaterialsResultPayload.LOCKED ? "enchant_invalid" : "changed"), WARNING);
        }
    }

    void send(EnchantGearPayload request) {
        if (target == null || target.slot() != request.slot() || !ItemStack.areEqual(target.stack(), request.expected())) {
            showFeedback(message("changed"), WARNING);
            return;
        }
        pending = request.request();
        feedback = null;
        ClientPlayNetworking.send(request);
    }

    private boolean canSubmit() {
        return pending == 0 && !preview.isEmpty()
                && (view == View.MANAGE || inventory.player.isCreative() || lapis >= selected.cost());
    }

    private boolean canReplace() {
        return pending == 0 && editingCopy != null && EquipmentEnchanting.hasOpenSlot(availableGear);
    }

    private int copiesAfterApplying(Offer offer) {
        return EnchantmentSlotHelper.copyCount(offer.enchantment(), availableGear.getEnchantments().getLevel(offer.enchantment())) + 1;
    }

    private void showFeedback(Text text, int color) {
        feedback = text;
        feedbackColor = color;
        feedbackUntil = System.currentTimeMillis() + 2800;
    }

    boolean release(int button) {
        if (button != 0 || !capturing) return false;
        capturing = dragging = false;
        return true;
    }

    boolean drag(double mx, double my, int button, double dx, double dy, Element focus) {
        if (button != 0 || !capturing) return false;
        if (dragging) scroll = scrollbar().scrollAt(my, grab);
        else if (focus == search) search.mouseDragged(mx, my, button, dx, dy);
        return true;
    }

    void scroll(double mx, double vertical) {
        if (mx < divider) scroll = Math.clamp(scroll - (int) (vertical * ROW * 2), 0, scrollbar().maxScroll());
    }

    boolean contains(double mx, double my) { return inside(mx, my, x, y, width, height); }
    private int searchY() { return y + (target == null ? 30 : navigationKey() == null ? 48 : 66); }
    private int listTop() { return searchY() + 19; }
    private int leftWidth() { return divider - x - PAD - 4; }
    private int detailWidth() { return x + width - PAD - divider - 7; }
    private int editButtonWidth() { return (detailWidth() - 4) / 2; }
    private int editReplaceX() { return divider + 7 + editButtonWidth() + 4; }
    private int bottom() { return y + height - PAD; }
    private int rows() { return target == null ? gearRows.size() : view == View.MANAGE ? installed.size() : offerRows.size(); }
    private StatsScrollbar scrollbar() { return new StatsScrollbar(listTop(), bottom() - listTop(), rows() * ROW); }

    private int rowAt(double mx, double my) {
        if (!inside(mx, my, x + PAD, listTop(), leftWidth() - 7, bottom() - listTop())) return -1;
        int row = ((int) my - listTop() + scroll) / ROW;
        return row < rows() ? row : -1;
    }

    private void line(DrawContext context, Text text, int left, int top, int w, int color) {
        String value = text.getString();
        if (font.getWidth(value) > w) value = font.trimToWidth(value, Math.max(0, w - 6)) + "…";
        context.drawText(font, value, left, top, color, false);
    }

    private int wrapped(DrawContext context, Text text, int left, int top, int w, int color) {
        for (var line : font.wrapLines(text, w)) {
            context.drawText(font, line, left, top, color, false);
            top += 10;
        }
        return top;
    }
}

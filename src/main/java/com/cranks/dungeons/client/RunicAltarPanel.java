package com.cranks.dungeons.client;

import com.cranks.dungeons.block.ModBlocks;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.inventory.MaterialCrafting;
import com.cranks.dungeons.inventory.RunicAltarRules;
import com.cranks.dungeons.inventory.RuneCrafting;
import com.cranks.dungeons.equipment.RuneTiers;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.item.RunicTomeItem;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import com.cranks.dungeons.network.RunicAltarPayload;
import com.cranks.dungeons.screen.RunicEnhancementAltarScreenHandler;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.cranks.dungeons.client.MaterialCraftingPanel.inside;
import static com.cranks.dungeons.client.MaterialCraftingPanel.drawButton;
import static com.cranks.dungeons.inventory.RunicAltarRules.text;

/** Tome selection and rune inspection; random outcomes are generated only by the server. */
final class RunicAltarPanel {
    static final int WIDTH = 300;
    static final int HEIGHT = 330;
    private record Tome(ItemStack stack, int count, String reason) {}
    private record RuneLine(List<OrderedText> lines, List<Text> tooltip, boolean changed) {}
    private record RuneLabel(Text value, int top, int color) {}
    private record TargetRow(int index,CustomStat.AffixType heading,int top,int height) {}
    private final RunicEnhancementAltarScreenHandler altar;
    private final PlayerInventory inventory;
    private final Consumer<RunicAltarPayload> apply;
    private final RunePoolPanel pool;
    private TextRenderer font;
    private TextFieldWidget search;
    private int x, y, width, height, divider, scroll, runeScroll, runeHeight;
    private boolean runes, possible, usable, grabbed;
    private double grab;
    private MaterialCrafting.Storage storage;
    private ItemStack lastGear = ItemStack.EMPTY, lastStored = ItemStack.EMPTY, lastResult = ItemStack.EMPTY;
    private ItemStack selected = ItemStack.EMPTY, before = ItemStack.EMPTY, after = ItemStack.EMPTY, pendingBefore = ItemStack.EMPTY;
    private List<Tome> all = List.of(), filtered = List.of();
    private List<RuneLine> runeLines = List.of();
    private List<Integer> runeTops = List.of();
    private int pending;
    private int selectedRune=-1,targetScroll,comparisonScroll,comparisonHeight;
    private boolean targetGrab,comparisonGrab;
    private Text feedback;
    private List<Text> changeDetails = List.of();
    private long feedbackUntil;
    private int feedbackColor;

    RunicAltarPanel(RunicEnhancementAltarScreenHandler altar, PlayerInventory inventory, Consumer<RunicAltarPayload> apply) {
        this.altar = altar; this.inventory = inventory; this.apply = apply;
        this.pool = new RunePoolPanel(altar, () -> selected);
    }
    void init(TextRenderer font, int x, int y, int width, int height) {
        this.font = font; this.x = x; this.y = y; this.width = width; this.height = height;
        divider = x + width * 44 / 100;
        String query = search == null ? "" : search.getText();
        search = new TextFieldWidget(font, x + 11, y + 89, divider - x - 61, 10, text("search"));
        search.setDrawsBackground(false); search.setMaxLength(80); search.setPlaceholder(text("search")); search.setText(query);
        search.setChangedListener(value -> { filter(true); });
        grabbed = false;
        pool.init(font, x + 8, y + 86, width - 16, height - 130);
        storage = null;
        refresh();
    }
    int slotX(int slot) {
        return slot == RunicEnhancementAltarScreenHandler.EQUIPMENT ? x + 12
                : slot == RunicEnhancementAltarScreenHandler.STORED_TOME ? x + width - 48 : x + width - 24;
    }
    int slotY() { return y + 36; }
    boolean contains(double mx, double my) { return inside(mx, my, x, y, width, height); }
    private int actionY() { return y + height - 27; }
    private int listWidth() { return divider - x - 18; }
    private int detailX() { return divider + 7; }
    private int detailWidth() { return x + width - 10 - detailX(); }
    private int count() { return all.stream().filter(row -> same(row.stack, selected)).mapToInt(Tome::count).sum(); }
    private String reason() { return RunicAltarRules.reason(altar.equipment(), selected, altar.oldResult(),selectedRune); }
    private static boolean same(ItemStack a, ItemStack b) { return !a.isEmpty() && !b.isEmpty() && ItemStack.areItemsAndComponentsEqual(a, b); }

    private void refresh() {
        if (storage != null && storage.isCurrent() && ItemStack.areEqual(lastGear, altar.equipment())
                && ItemStack.areEqual(lastStored, altar.storedTome()) && ItemStack.areEqual(lastResult, altar.oldResult())) return;
        storage = new MaterialCrafting.Storage(inventory);
        if (!ItemStack.areEqual(lastGear, altar.equipment())) {runeScroll = 0;selectedRune=-1;targetScroll=0;comparisonScroll=0;}
        lastGear = altar.equipment().copy(); lastStored = altar.storedTome().copy(); lastResult = altar.oldResult().copy();
        var grouped = new ArrayList<Tome>();
        add(grouped, altar.storedTome());
        for (int i = 0; i < storage.size(); i++) add(grouped, storage.stack(i));
        all = grouped.stream().sorted(Comparator.comparing((Tome row) -> row.reason != null)
                .thenComparing(row -> row.stack.getName().getString(), String.CASE_INSENSITIVE_ORDER)).toList();
        filter(false);
    }
    private void add(List<Tome> rows, ItemStack stack) {
        if (stack.isEmpty() || !RunicAltarRules.isTome(stack)) return;
        for (int i = 0; i < rows.size(); i++) if (same(rows.get(i).stack, stack)) {
            var old = rows.get(i); rows.set(i, new Tome(old.stack, old.count + stack.getCount(), old.reason)); return;
        }
        rows.add(new Tome(stack.copyWithCount(1), stack.getCount(), RunicAltarRules.reason(altar.equipment(), stack, altar.oldResult())));
    }
    private void filter(boolean changedQuery) {
        String query = search.getText().toLowerCase(Locale.ROOT).strip();
        var terms = query.isEmpty() ? new String[0] : query.split("\\s+");
        filtered = all.stream().filter(row -> !usable || row.reason == null).filter(row -> {
            String key = (row.stack.getName().getString() + " " + RunicAltarRules.effect(row.stack).getString()).toLowerCase(Locale.ROOT);
            return Arrays.stream(terms).allMatch(key::contains);
        }).toList();
        if (selected.isEmpty() || changedQuery) selected = filtered.isEmpty() ? ItemStack.EMPTY : filtered.getFirst().stack;
        if (changedQuery) scroll = 0;
        scroll = Math.clamp(scroll, 0, bar().maxScroll());
    }
    private StatsScrollbar bar() { return new StatsScrollbar(y + 108, height - 142, filtered.size() * 20); }
    private StatsScrollbar runeBar() { return new StatsScrollbar(y + 86, height - 130, runeHeight); }

    void render(DrawContext context, int mx, int my, float delta) {
        refresh();
        CompendiumStyle.drawPanel(context, x, y, width, height);
        context.drawItem(ModBlocks.RUNIC_ENHANCEMENT_ALTAR.asItem().getDefaultStack(), x + 8, y + 7);
        com.cranks.dungeons.client.guide.GuideWidgets.header(context,font,ModBlocks.RUNIC_ENHANCEMENT_ALTAR.getName(),x+29,y+11,width-40,CompendiumStyle.TEXT,com.cranks.dungeons.client.guide.GuideTarget.article("runic_altar"));
        InventoryStyle.drawDivider(context, x + 8, x + width - 8, y + 24);
        for (int id = RunicEnhancementAltarScreenHandler.EQUIPMENT; id <= RunicEnhancementAltarScreenHandler.OLD_RESULT; id++) {
            if (!altar.getSlot(id).isEnabled()) continue;
            InventoryStyle.drawSocket(context, slotX(id) - 3, slotY() - 3, 22, 22, inside(mx, my, slotX(id), slotY(), 16, 16));
            if (id == RunicEnhancementAltarScreenHandler.EQUIPMENT && altar.equipment().isEmpty()) {
                StationSlotHints.draw(context, StationSlotHints.Kind.EQUIPMENT, slotX(id), slotY());
            }
        }
        ItemStack gear = altar.equipment();
        line(context, gear.isEmpty() ? text("equipment") : gear.getName(), x + 38, y + 35, width - 96, CompendiumStyle.TEXT);
        line(context, gear.isEmpty() ? text("equipment_hint") : Text.literal(CustomRarity.getRarity(gear).getName()),
                x + 38, y + 48, width - 96, CompendiumStyle.TEXT_MUTED);
        if(RuneCrafting.pending(gear)){renderComparison(context,mx,my);return;}
        int tabWidth = (width - 28) / 3;
        String[] tabs = {"tomes", "runes", "possible"};
        int active = possible ? 2 : runes ? 1 : 0;
        for (int i = 0; i < tabs.length; i++) {
            int left = x + 10 + i * (tabWidth + 4);
            drawButton(context, text(tabs[i]), left, y + 64, tabWidth, 16, mx, my, true);
            if (i == active) context.fill(left + 2, y + 79, left + tabWidth - 2, y + 80, CompendiumStyle.TEXT_MUTED);
        }
        if (possible) pool.render(context, mx, my, delta);
        else if (runes) renderRunes(context);
        else renderTomes(context, mx, my, delta);
        Text status = feedback != null && System.currentTimeMillis() < feedbackUntil ? feedback
                : pending != 0 ? text("working") : runes || possible ? Text.empty() : selected.isEmpty() ? text("choose")
                : count() == 0 ? text("no_tomes") : reason() != null ? Text.literal(reason()) : selected.getName();
        line(context, status, runes || possible ? x + 10 : detailX(), actionY() - 14, runes || possible ? width - 20 : detailWidth(),
                feedback != null && System.currentTimeMillis() < feedbackUntil ? feedbackColor : CompendiumStyle.TEXT_MUTED);
        drawButton(context, text(pending == 0 ? RuneCrafting.comparison(gear,selected,selectedRune)?"roll_proposal":"apply" : "working"), x + 10, actionY(), width - 20, 19, mx, my,
                pending == 0 && count() > 0 && reason() == null);
    }
    private void renderTomes(DrawContext context, int mx, int my, float delta) {
        CompendiumStyle.drawTextFieldFrame(context, x + 8, y + 86, divider - x - 55, 15, search.isFocused());
        search.render(context, mx, my, delta);
        com.cranks.dungeons.client.compendium.CompendiumButtonWidget.drawButton(context,
                divider - 43, y + 86, 35, 15, inside(mx, my, divider - 43, y + 86, 35, 15), true, usable, text("usable").getString());
        var bar = bar();
        context.enableScissor(x + 8, bar.top(), divider - 7, bar.top() + bar.height());
        if (filtered.isEmpty()) line(context, text("empty"), x + 10, bar.top() + 5, listWidth(), CompendiumStyle.TEXT_MUTED);
        for (int i = scroll / 20; i < filtered.size() && i * 20 - scroll < bar.height(); i++) {
            var row = filtered.get(i); int top = bar.top() + i * 20 - scroll;
            CompendiumStyle.drawRailPlaque(context, x + 8, top + 1, listWidth(), 18, same(selected, row.stack),
                    inside(mx, my, x + 8, Math.max(top, bar.top()), listWidth(), Math.min(top + 20, bar.top() + bar.height()) - Math.max(top, bar.top())));
            context.drawItem(row.stack, x + 10, top + 2);
            String amount = Integer.toString(row.count);
            context.drawText(font, amount, divider - 13 - font.getWidth(amount), top + 6, CompendiumStyle.TEXT_MUTED, false);
            line(context, row.stack.getName(), x + 29, top + 6, listWidth() - 26 - font.getWidth(amount),
                    row.reason == null ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_DISABLED);
        }
        context.disableScissor();
        if (bar.maxScroll() > 0) {
            context.fill(divider - 6, bar.top(), divider - 3, bar.top() + bar.height(), 0xff090909);
            int top = (int)Math.round(bar.thumbTop(scroll));
            context.fill(divider - 6, top, divider - 3, top + bar.thumbHeight(), 0xff939393);
        }
        if (selected.isEmpty()) return;
        context.drawItem(selected, detailX(), y + 87);
        line(context, selected.getName(), detailX() + 21, y + 89, detailWidth() - 21, CompendiumStyle.TEXT);
        line(context, text("cost", count()), detailX(), y + 105, detailWidth(), CompendiumStyle.TEXT_MUTED);
        boolean targets=RuneCrafting.allowsTarget(selected);
        wrapped(context, RunicAltarRules.effect(selected), detailX(), y + 119, detailWidth(), targets?y+141:y+height-65, CompendiumStyle.TEXT);
        if(targets)renderTargets(context,mx,my);
        var tiers=selected.getItem() instanceof RunicTomeItem runic?RuneTiers.random(altar.equipment(),RuneCrafting.strength(runic))
                :selected.getItem() instanceof com.cranks.dungeons.item.AbstractTomeItem targeted?RuneCrafting.targetedTiers(altar.equipment(),targeted):RuneTiers.artificial(altar.equipment());
        String range=tiers.getFirst().equals(tiers.getLast())?"T"+tiers.getFirst():"T"+tiers.getFirst()+"–T"+tiers.getLast();
        boolean valuesOnly=selected.getItem() instanceof RunicTomeItem runic&&(runic.getTomeType()==RunicTomeItem.TomeType.DIVINE||runic.getTomeType()==RunicTomeItem.TomeType.ANNULMENT);
        line(context, valuesOnly?text("gear_cap",RuneTiers.best(altar.equipment())):Text.literal("Rolls "+range+" · cap T"+RuneTiers.best(altar.equipment())),detailX(), y + height - 58, detailWidth(), CompendiumStyle.TEXT_MUTED);
    }
    private int targetTop(){return y+156;}
    private int targetHeight(){return Math.max(16,height-220);}
    private List<TargetRow> targetRows(){
        var rows=new ArrayList<TargetRow>();int top=0;
        if(!RuneCrafting.needsTarget(altar.equipment(),selected)){rows.add(new TargetRow(-1,null,top,18));top+=18;}
        var entries=RuneDisplayOrder.entries(altar.equipment());
        for(var side:CustomStat.AffixType.values()){
            rows.add(new TargetRow(-2,side,top,14));top+=14;
            for(var entry:entries)if(entry.definition().getAffixType()==side){rows.add(new TargetRow(entry.index(),null,top,18));top+=18;}
        }
        return List.copyOf(rows);
    }
    private StatsScrollbar targetBar(){var rows=targetRows();return new StatsScrollbar(targetTop(),targetHeight(),rows.isEmpty()?0:rows.getLast().top+rows.getLast().height);}
    private TargetRow targetAt(double mouseY){
        int offset=(int)Math.floor(mouseY-targetTop()+targetScroll);
        return targetRows().stream().filter(row->offset>=row.top&&offset<row.top+row.height).findFirst().orElse(null);
    }
    private void renderTargets(DrawContext context,int mx,int my){
        line(context,text("target_rune"),detailX(),y+144,detailWidth(),CompendiumStyle.TEXT_MUTED);
        targetScroll=Math.clamp(targetScroll,0,targetBar().maxScroll());
        context.enableScissor(detailX(),targetTop(),detailX()+detailWidth(),targetTop()+targetHeight());
        for(var row:targetRows()){
            int index=row.index,top=targetTop()+row.top-targetScroll;
            if(row.heading!=null){
                String label=text(row.heading==CustomStat.AffixType.PREFIX?"selector_prefixes":"selector_suffixes").getString();
                line(context,Text.literal(label),detailX()+2,top+3,detailWidth()-4,CompendiumStyle.TEXT_MUTED);
                int from=detailX()+font.getWidth(label)+7;
                if(from<detailX()+detailWidth()-2)context.fill(from,top+7,detailX()+detailWidth()-2,top+8,0xff555555);
                continue;
            }
            boolean enabled=index<0||RuneCrafting.editable(altar.equipment(),index);
            String label=index<0?text("free_slot").getString():describe(ItemStatManager.getStats(altar.equipment()).get(index),altar.equipment());
            CompendiumStyle.drawRailPlaque(context,detailX(),top,detailWidth(),17,selectedRune==index,enabled&&inside(mx,my,detailX(),top,detailWidth(),17));
            line(context,Text.literal(label),detailX()+4,top+4,detailWidth()-8,enabled?CompendiumStyle.TEXT:CompendiumStyle.TEXT_DISABLED);
        }
        context.disableScissor();
        var bar=targetBar();if(bar.maxScroll()>0)CompendiumStyle.drawGuideScrollbar(context,detailX()+detailWidth()-3,targetTop(),targetHeight(),(int)Math.round(bar.thumbTop(targetScroll)),bar.thumbHeight());
    }
    private List<OrderedText> comparisonLines(ItemStack gear){
        var lines=new ArrayList<OrderedText>();
        var entries=RuneDisplayOrder.entries(gear);
        for(var side:CustomStat.AffixType.values()){
            lines.add(text(side==CustomStat.AffixType.PREFIX?"selector_prefixes":"selector_suffixes").copy().formatted(net.minecraft.util.Formatting.GRAY).asOrderedText());
            boolean any=false;
            for(var entry:entries)if(entry.definition().getAffixType()==side){lines.addAll(font.wrapLines(Text.literal(describe(entry.rune(),gear)),(width-34)/2));lines.add(Text.empty().asOrderedText());any=true;}
            if(ItemStatManager.hasCrypticStat(gear)&&ItemStatManager.getCrypticAffixType(gear)==side){lines.addAll(font.wrapLines(text("cryptic"),(width-34)/2));any=true;}
            if(!any)lines.add(text("none").asOrderedText());
            lines.add(Text.empty().asOrderedText());
        }
        return lines;
    }
    private void renderComparison(DrawContext context,int mx,int my){
        line(context,text("proposal_paid"),x+10,y+67,width-20,CompendiumStyle.TEXT_MUTED);
        int col=(width-26)/2,top=y+99,h=Math.max(20,height-155);
        var candidate=RuneCrafting.proposal(altar.equipment());
        var left=comparisonLines(altar.equipment());var right=comparisonLines(candidate);
        comparisonHeight=Math.max(left.size(),right.size())*11;
        comparisonScroll=Math.clamp(comparisonScroll,0,Math.max(0,comparisonHeight-h));
        for(int side=0;side<2;side++){
            int px=x+10+side*(col+6);line(context,text(side==0?"current_result":"new_result"),px,y+84,col,CompendiumStyle.TEXT);
            CompendiumStyle.drawInsetPanel(context,px-2,top-3,col+3,h+6);
            context.enableScissor(px,top,px+col,top+h);int py=top-comparisonScroll;
            for(var line:side==0?left:right){context.drawText(font,line,px+2,py,CompendiumStyle.TEXT,false);py+=11;}
            context.disableScissor();
        }
        drawButton(context,text("keep_current"),x+10,actionY(),col,19,mx,my,pending==0);
        drawButton(context,text("use_new"),x+16+col,actionY(),col,19,mx,my,pending==0&&!candidate.isEmpty());
        var bar=new StatsScrollbar(top,h,comparisonHeight);
        if(bar.maxScroll()>0)CompendiumStyle.drawGuideScrollbar(context,x+width-6,top,h,(int)Math.round(bar.thumbTop(comparisonScroll)),bar.thumbHeight());
    }
    private void renderRunes(DrawContext context) {
        runeLines = new ArrayList<>(); runeTops = new ArrayList<>();
        runeHeight = 0;
        var gear = altar.equipment();
        if (gear.isEmpty()) { line(context, text("equipment_hint"), x + 12, y + 91, width - 24, CompendiumStyle.TEXT_MUTED); return; }
        var rarity = CustomRarity.getRarity(gear);
        var labels = new ArrayList<RuneLabel>();
        int top = 0;
        for (var side : CustomStat.AffixType.values()) {
            int cap = side == CustomStat.AffixType.PREFIX ? rarity.getMaxPrefixes() : rarity.getMaxSuffixes();
            labels.add(new RuneLabel(text(side == CustomStat.AffixType.PREFIX ? "prefixes" : "suffixes",
                    RunicAltarRules.occupied(gear, side), cap), top, CompendiumStyle.TEXT_MUTED));
            top += 12;
            boolean any = false;
            for (var roll : ItemStatManager.getStats(gear)) {
                var stat = StatRegistry.getStat(roll.statId);
                if (stat == null || stat.getAffixType() != side) continue;
                any = true;
                var description = new ArrayList<Text>();
                description.add(com.cranks.dungeons.client.guide.GuideText.link("T"+roll.tier+(roll.isArtificial?" · Artificial":""),com.cranks.dungeons.client.guide.GuideTarget.rune(stat.getId())));
                var shown = new ArrayList<OrderedText>();
                for (int i = 0; i < stat.getComponents().size(); i++) {
                    String value = stat.getComponents().get(i).formatValue(roll.getValue(i), EquipmentType.getTypeForItem(gear).orElse(null));
                    shown.addAll(font.wrapLines(Text.literal((i == 0 ? "T" + roll.tier + "  " : "    ") + value), width - 37));
                    description.add(com.cranks.dungeons.client.guide.GuideText.link(value,com.cranks.dungeons.client.guide.GuideTarget.rune(stat.getId())));
                    description.add(Text.literal(stat.getComponents().get(i).getFormattedMinMax(roll.tier)));
                }
                boolean changed = ItemStack.areEqual(gear, after) && ItemStatManager.getStats(before).stream().noneMatch(old ->
                        old.statId.equals(roll.statId) && old.tier == roll.tier && Arrays.equals(old.values, roll.values) && old.isArtificial == roll.isArtificial);
                runeLines.add(new RuneLine(List.copyOf(shown), description, changed));
                runeTops.add(top); top += shown.size() * 10 + 3;
            }
            if (ItemStatManager.hasCrypticStat(gear) && ItemStatManager.getCrypticAffixType(gear) == side) {
                labels.add(new RuneLabel(text("cryptic"), top, CompendiumStyle.TEXT)); top += 12; any = true;
            }
            if (!any) { labels.add(new RuneLabel(text("none"), top, CompendiumStyle.TEXT_DISABLED)); top += 12; }
            top += 4;
        }
        runeHeight = top;
        var bar = runeBar();
        runeScroll = Math.clamp(runeScroll, 0, bar.maxScroll());
        context.enableScissor(x + 10, bar.top(), x + width - 9, bar.top() + bar.height());
        for (var label : labels) line(context, label.value, x + 12, bar.top() + label.top - runeScroll, width - 32, label.color);
        for (int i = 0; i < runeLines.size(); i++) {
            var row = runeLines.get(i);
            int rowY = bar.top() + runeTops.get(i) - runeScroll;
            if (rowY + row.lines.size() * 10 <= bar.top() || rowY >= bar.top() + bar.height()) continue;
            if (row.changed) context.fill(x + 10, rowY - 1, x + width - 10, rowY + row.lines.size() * 10, 0xff303030);
            for (var component : row.lines) { context.drawText(font, component, x + 14, rowY, CompendiumStyle.TEXT, false); rowY += 10; }
        }
        context.disableScissor();
        if (bar.maxScroll() > 0) {
            context.fill(x + width - 7, bar.top(), x + width - 4, bar.top() + bar.height(), 0xff090909);
            int thumbY = (int)Math.round(bar.thumbTop(runeScroll));
            context.fill(x + width - 7, thumbY, x + width - 4, thumbY + bar.thumbHeight(), 0xff939393);
        }
    }
    void tooltip(DrawContext context, int mx, int my) {
        if (!contains(mx, my)) return;
        if(RuneCrafting.pending(altar.equipment()))return;
        if(!runes&&!possible&&RuneCrafting.allowsTarget(selected)&&inside(mx,my,detailX(),targetTop(),detailWidth(),targetHeight())){
            var row=targetAt(my);int index=row==null?-2:row.index;
            var rolls=ItemStatManager.getStats(altar.equipment());
            if(index>=0&&index<rolls.size())context.drawOrderedTooltip(font,font.wrapLines(Text.literal(describe(rolls.get(index),altar.equipment())
                    +(RuneCrafting.protectedRune(altar.equipment(),index)?" · Protected":"")),230),mx,my);
            else if(index==-1&&reason()!=null)context.drawOrderedTooltip(font,font.wrapLines(Text.literal(reason()),230),mx,my);
            return;
        }
        if (feedback != null && System.currentTimeMillis() < feedbackUntil && !changeDetails.isEmpty()
                && inside(mx, my, runes || possible ? x + 10 : detailX(), actionY() - 15, runes || possible ? width - 20 : detailWidth(), 12)) {
            var lines = new ArrayList<net.minecraft.text.OrderedText>();
            for (var detail : changeDetails) lines.addAll(font.wrapLines(detail, 230));
            context.drawOrderedTooltip(font, lines, mx, my); return;
        }
        if (inside(mx, my, slotX(RunicEnhancementAltarScreenHandler.STORED_TOME), slotY(), 16, 16) && !altar.storedTome().isEmpty()) {
            context.drawOrderedTooltip(font, font.wrapLines(text("stored_tome"), 210), mx, my); return;
        }
        if (inside(mx, my, slotX(RunicEnhancementAltarScreenHandler.OLD_RESULT), slotY(), 16, 16) && !altar.oldResult().isEmpty()) {
            context.drawOrderedTooltip(font, font.wrapLines(text("old_result"), 210), mx, my); return;
        }
        if (inside(mx, my, x + 10 + 2 * ((width - 28) / 3 + 4), y + 64, (width - 28) / 3, 16)) {
            context.drawOrderedTooltip(font, font.wrapLines(text("pool_hint"), 230), mx, my); return;
        }
        if (possible) { pool.tooltip(context, mx, my); return; }
        if (!runes && selected.getItem() instanceof RunicTomeItem && inside(mx, my, detailX(), y + height - 59, detailWidth(), 12)) {
            context.drawOrderedTooltip(font, font.wrapLines(text("level_hint"), 230), mx, my); return;
        }
        if (runes) {
            var bar = runeBar();
            if (!inside(mx, my, x + 10, bar.top(), width - 19, bar.height())) return;
            for (int i = 0; i < runeLines.size(); i++) if (inside(mx, my, x + 10,
                    bar.top() + runeTops.get(i) - runeScroll, width - 19, runeLines.get(i).lines.size() * 10)) {
                context.drawTooltip(font, runeLines.get(i).tooltip, mx, my); return;
            }
        } else {
            if (inside(mx, my, divider - 43, y + 86, 35, 15)) { context.drawTooltip(font, text("usable_hint"), mx, my); return; }
            Tome hovered = null;
            var bar = bar();
            if (inside(mx, my, x + 8, bar.top(), listWidth(), bar.height())) {
                int row = (my - bar.top() + scroll) / 20;
                if (row < filtered.size()) hovered = filtered.get(row);
            } else if (inside(mx, my, detailX(), y + 86, detailWidth(), height - 123) && !selected.isEmpty()) {
                hovered = new Tome(selected, count(), reason());
            }
            if (hovered != null) {
                var lines = new ArrayList<net.minecraft.text.OrderedText>();
                for (var line : List.of(com.cranks.dungeons.client.guide.GuideText.itemName(hovered.stack), RunicAltarRules.effect(hovered.stack),
                        hovered.reason == null ? text("cost", hovered.count) : Text.literal(hovered.reason))) lines.addAll(font.wrapLines(line, 210));
                context.drawOrderedTooltip(font, lines, mx, my);
            }
        }
    }
    void click(double mx, double my, int button, Consumer<TextFieldWidget> focus) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        refresh();
        if(RuneCrafting.pending(altar.equipment())){
            focus.accept(null);int col=(width-26)/2;
            var bar=new StatsScrollbar(y+99,Math.max(20,height-155),comparisonHeight);
            if(mx>=x+width-10&&inside(mx,my,x+width-10,bar.top(),10,bar.height())&&bar.maxScroll()>0){comparisonGrab=true;grab=bar.grabOffset(my,comparisonScroll);comparisonScroll=bar.scrollAt(my,grab);return;}
            if(pending==0&&inside(mx,my,x+10,actionY(),col,19))resolve(1);
            else if(pending==0&&inside(mx,my,x+16+col,actionY(),col,19))resolve(2);
            return;
        }
        if (!runes && !possible && inside(mx, my, x + 8, y + 86, divider - x - 55, 15)) {
            focus.accept(search); search.mouseClicked(mx, my, button); return;
        }
        focus.accept(null);
        if (inside(mx, my, x + 10, y + 64, width - 24, 16)) {
            int tab = Math.clamp(((int)mx - x - 10) / ((width - 28) / 3 + 4), 0, 2);
            runes = tab == 1; possible = tab == 2; grabbed = false; return;
        }
        if (inside(mx, my, x + 10, actionY(), width - 20, 19)) { submit(); return; }
        if (possible) { pool.click(mx, my, button, focus); return; }
        if(!runes&&RuneCrafting.allowsTarget(selected)&&inside(mx,my,detailX(),targetTop(),detailWidth(),targetHeight())){
            var targetBar=targetBar();if(mx>=detailX()+detailWidth()-6&&targetBar.maxScroll()>0){targetGrab=true;grab=targetBar.grabOffset(my,targetScroll);targetScroll=targetBar.scrollAt(my,grab);return;}
            var row=targetAt(my);if(row!=null&&row.heading==null&&(row.index==-1||RuneCrafting.editable(altar.equipment(),row.index)))selectedRune=row.index;return;
        }
        if (runes) {
            var bar = runeBar();
            if (inside(mx, my, x + width - 9, bar.top(), 7, bar.height()) && bar.maxScroll() > 0) {
                grabbed = true; grab = bar.grabOffset(my, runeScroll); runeScroll = bar.scrollAt(my, grab);
            }
            return;
        }
        if (inside(mx, my, divider - 43, y + 86, 35, 15)) { usable = !usable; filter(true); return; }
        var bar = bar();
        if (inside(mx, my, divider - 7, bar.top(), 6, bar.height()) && bar.maxScroll() > 0) {
            grabbed = true; grab = bar.grabOffset(my, scroll); scroll = bar.scrollAt(my, grab); return;
        }
        if (inside(mx, my, x + 8, bar.top(), listWidth(), bar.height())) {
            int row = ((int)my - bar.top() + scroll) / 20;
            if (row < filtered.size()) { selected = filtered.get(row).stack; selectedRune=-1;targetScroll=0;feedback = null; }
        }
    }
    boolean drag(double mx, double my, int button, double dx, double dy, Element focused) {
        if(button==0&&comparisonGrab){comparisonScroll=new StatsScrollbar(y+99,Math.max(20,height-155),comparisonHeight).scrollAt(my,grab);return true;}
        if(RuneCrafting.pending(altar.equipment()))return false;
        if(button==0&&targetGrab){targetScroll=targetBar().scrollAt(my,grab);return true;}
        if (possible) return pool.drag(mx, my, button, dx, dy, focused);
        if (button != 0) return false;
        if (grabbed) { if (runes) runeScroll = runeBar().scrollAt(my, grab); else scroll = bar().scrollAt(my, grab); return true; }
        return focused == search && search.mouseDragged(mx, my, button, dx, dy);
    }
    boolean release(int button) { boolean result = pool.release(button) | (button == 0 && (grabbed||targetGrab||comparisonGrab)); if (button == 0) {grabbed = false;targetGrab=false;comparisonGrab=false;} return result; }
    void scroll(double mx, double amount) {
        if(RuneCrafting.pending(altar.equipment()))comparisonScroll=Math.clamp(comparisonScroll-(int)(amount*20),0,Math.max(0,comparisonHeight-Math.max(20,height-155)));
        else if (possible) pool.scroll(mx, amount);
        else if (runes) runeScroll = Math.clamp(runeScroll - (int)(amount * 20), 0, runeBar().maxScroll());
        else if (mx < divider) scroll = Math.clamp(scroll - (int)(amount * 20), 0, bar().maxScroll());
        else targetScroll=Math.clamp(targetScroll-(int)(amount*18),0,targetBar().maxScroll());
    }
    void submit() {
        refresh();
        if (pending != 0 || count() == 0 || reason() != null) return;
        apply.accept(new RunicAltarPayload(altar.syncId, MaterialCraftingPanel.nextRequest(), altar.revision(),
                altar.equipment().copy(), selected.copyWithCount(1), false,selectedRune,0));
    }
    private void resolve(int action){apply.accept(new RunicAltarPayload(altar.syncId,MaterialCraftingPanel.nextRequest(),altar.revision(),altar.equipment().copy(),ItemStack.EMPTY,true,-1,action));}
    void send(RunicAltarPayload request) {
        if (pending != 0) return;
        pending = request.request(); pendingBefore = request.equipment().copy();
        ClientPlayNetworking.send(request);
    }
    void receive(CraftMaterialsResultPayload result) {
        if (result.request() != pending || pending == 0) return;
        pending = 0;
        if (result.result() > 0) {
            before = pendingBefore; after = altar.equipment().copy();
            var old = ItemStatManager.getStats(before); var next = ItemStatManager.getStats(after);
            long added = next.stream().filter(n -> old.stream().noneMatch(o -> o.statId.equals(n.statId))).count();
            long removed = old.stream().filter(o -> next.stream().noneMatch(n -> n.statId.equals(o.statId))).count();
            long changed = next.stream().filter(n -> old.stream().anyMatch(o -> o.statId.equals(n.statId)
                    && (o.tier != n.tier || !Arrays.equals(o.values, n.values)))).count();
            var details = new ArrayList<Text>();
            details.add(text("changes_legend"));
            for (var roll : old) if (next.stream().noneMatch(n -> n.statId.equals(roll.statId))) details.add(text("removed", describe(roll, before)));
            for (var roll : next) {
                var previous = old.stream().filter(o -> o.statId.equals(roll.statId)).findFirst().orElse(null);
                if (previous == null) details.add(text("added", describe(roll, after)));
                else if (previous.tier != roll.tier || !Arrays.equals(previous.values, roll.values)) {
                    details.add(text("previous", describe(previous, before)));
                    details.add(text("current", describe(roll, after)));
                }
            }
            if (ItemStatManager.hasCrypticStat(after) && !ItemStatManager.hasCrypticStat(before)) details.add(text("cryptic_added"));
            changeDetails = List.copyOf(details);
            feedback = ItemStatManager.hasCrypticStat(after) && !ItemStatManager.hasCrypticStat(before) ? text("cryptic_added") : text("changes", added, removed, changed);
            feedbackColor = CompendiumStyle.TEXT_SUCCESS;
        } else {
            changeDetails = List.of();
            feedback = text(result.result() == CraftMaterialsResultPayload.INVENTORY_CHANGED ? "stale"
                    : result.result() == RunicEnhancementAltarScreenHandler.CONFIRM_REQUIRED ? "confirm"
                    : result.result() == RunicEnhancementAltarScreenHandler.NO_OUTCOME ? "no_outcome" : "unavailable");
            feedbackColor = CompendiumStyle.TEXT_WARNING;
        }
        feedbackUntil = System.currentTimeMillis() + 5000; storage = null; refresh();
    }
    private static String describe(ItemStatManager.ItemStat roll, ItemStack gear) {
        var stat = StatRegistry.getStat(roll.statId);
        if (stat == null) return roll.statId;
        String result = "T" + roll.tier;
        for (int i = 0; i < stat.getComponents().size(); i++) result += " · " + stat.getComponents().get(i)
                .formatValue(roll.getValue(i), EquipmentType.getTypeForItem(gear).orElse(null));
        return result;
    }
    private void line(DrawContext context, Text text, int left, int top, int w, int color) {
        String value = text.getString();
        if (font.getWidth(value) > w) value = font.trimToWidth(value, Math.max(0, w - 6)) + "…";
        context.drawText(font, value, left, top, color, false);
    }
    private void wrapped(DrawContext context, Text text, int left, int top, int w, int bottom, int color) {
        for (var line : font.wrapLines(text, w)) { if (top + 9 > bottom) break; context.drawText(font, line, left, top, color, false); top += 10; }
    }
}

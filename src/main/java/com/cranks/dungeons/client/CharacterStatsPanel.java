package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.client.render.HudStyle;
import com.cranks.dungeons.component.ModComponents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Shared searchable stats for inventory and both trees. */
public final class CharacterStatsPanel {
    private static final int BODY_TOP = 42;
    private static final int BODY_BOTTOM = 5;
    private static final int SCROLLBAR_INSET = 6;
    private static final int SCROLLBAR_WIDTH = 4;

    private enum Progression {
        PLAYER("Player Level", "player level combat experience xp", HudStyle.Ink.XP),
        WORLD("World Level", "world level gathering discovery exploration recipes experience xp", HudStyle.Ink.STAMINA);

        final String title, search;
        final HudStyle.Ink ink;
        Progression(String title,String search,HudStyle.Ink ink){this.title=title;this.search=search;this.ink=ink;}
    }
    private record Progress(int level,int current,int required,float fraction) {
        String label(){return required<=0?"Max level":current+" / "+required;}
    }
    private record Row(CharacterStats.Stat stat, String title, int offset, Progression progression) {
        Row(CharacterStats.Stat stat,String title,int offset){this(stat,title,offset,null);}
        int height(){return progression==null?13:28;}
    }

    private final CharacterStats stats = new CharacterStats();
    private final List<Row> rows = new ArrayList<>();
    private TextFieldWidget search;
    private TextRenderer font;
    private int x, y, width, height;
    private int scroll;
    private int contentHeight;
    private String query = "";
    private int capturedButton = -1;
    private boolean draggingThumb;
    private double thumbGrabOffset;

    public void init(TextRenderer font, int x, int y, int width, int height) {
        endPointerInteraction();
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        search = new TextFieldWidget(font, x + 25, y + 25, width - 33, 11, Text.literal("Find stat"));
        search.setDrawsBackground(false);
        search.setMaxLength(100);
        search.setPlaceholder(Text.literal("Find stat…"));
        search.setText(query);
        search.setChangedListener(value -> {
            query = value;
            scroll = 0;
            filter();
        });
        filter();
    }

    private void filter() {
        rows.clear();
        int offset = 0;
        String[] terms = CharacterStats.normalize(query).split("\\s+");
        var tracks=java.util.Arrays.stream(Progression.values())
                .filter(track->java.util.Arrays.stream(terms).allMatch(term->("progression "+track.search).contains(term))).toList();
        if(!tracks.isEmpty()){
            rows.add(new Row(null,"Progression",offset));offset+=15;
            for(var track:tracks){rows.add(new Row(null,track.title,offset,track));offset+=28;}
            offset+=5;
        }
        for (var group : stats.groups().entrySet()) {
            List<CharacterStats.Stat> matching = group.getValue().stream()
                    .filter(stat -> java.util.Arrays.stream(terms).allMatch(stat.search()::contains)).toList();
            if (matching.isEmpty()) continue;
            rows.add(new Row(null, group.getKey().title, offset));
            offset += 15;
            for (var stat : matching) {
                rows.add(new Row(stat, stat.name(), offset));
                offset += 13;
            }
            offset += 5;
        }
        contentHeight = offset;
        scroll = Math.clamp(scroll, 0, scrollbar().maxScroll());
    }

    private StatsScrollbar scrollbar() {
        return new StatsScrollbar(y + BODY_TOP, height - BODY_TOP - BODY_BOTTOM, contentHeight);
    }

    private int scrollbarX() {
        return x + width - SCROLLBAR_INSET;
    }

    public boolean contains(double mouseX, double mouseY) {
        return search != null && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, Consumer<TextFieldWidget> focus) {
        if (!contains(mouseX, mouseY)) return false;
        if (capturedButton == -1) capturedButton = button;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && capturedButton == button) {
            StatsScrollbar scrollbar = scrollbar();
            if (mouseY >= y + 20 && mouseY < y + 40) {
                search.mouseClicked(mouseX, mouseY, button);
                focus.accept(search);
            } else {
                focus.accept(null);
                if (scrollbar.maxScroll() > 0
                        && mouseX >= scrollbarX() - 1 && mouseX < scrollbarX() + SCROLLBAR_WIDTH + 1
                        && mouseY >= scrollbar.top() && mouseY < scrollbar.top() + scrollbar.height()) {
                    draggingThumb = true;
                    thumbGrabOffset = scrollbar.grabOffset(mouseY, scroll);
                    scroll = scrollbar.scrollAt(mouseY, thumbGrabOffset);
                }
            }
        }
        return true;
    }

    public boolean mouseDragged(double mouseY, int button) {
        if (button != capturedButton) return false;
        if (draggingThumb) scroll = scrollbar().scrollAt(mouseY, thumbGrabOffset);
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != capturedButton) return false;
        endPointerInteraction();
        return true;
    }

    public void endPointerInteraction() {
        capturedButton = -1;
        draggingThumb = false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!contains(mouseX, mouseY)) return false;
        if (!draggingThumb) {
            scroll = Math.clamp(scroll - (int) Math.round(amount * 22), 0, scrollbar().maxScroll());
        }
        return true;
    }

    public boolean ownsFocus(Object focused) {
        return search != null && search == focused;
    }

    public void render(DrawContext context, PlayerEntity player, Map<String, Double> draft, int mouseX, int mouseY) {
        if (search == null) return;
        StatsScrollbar scrollbar = scrollbar();
        int top = scrollbar.top();
        int bottom = top + scrollbar.height();
        InventoryStyle.drawPanel(context, x, y, width, height, false, InventoryStyle.Panel.STATS);
        context.drawText(font, "Character Stats", x + 9, y + 8, CompendiumStyle.TEXT, false);
        CompendiumStyle.drawRailPlaque(context, x + 5, y + 19, width - 10, 19, false, search.isFocused());
        CompendiumStyle.drawJournalIcon(context, 13, x + 6, y + 20);
        search.render(context, mouseX, mouseY, 0);
        if (rows.isEmpty()) {
            context.drawText(font, "No matching stats", x + 9, top + 2, CompendiumStyle.TEXT_MUTED, false);
            return;
        }
        CharacterStats.Values current = new CharacterStats.Values(player, Map.of());
        CharacterStats.Values preview = draft.isEmpty() ? current : new CharacterStats.Values(player, draft);
        context.enableScissor(x + 4, top, x + width - 4, bottom);
        Row hovered = null;
        String hoveredValue = "";
        String hoveredCurrent = "";
        List<Text> progressionTooltip=null;
        for (Row row : rows) {
            int ry = top + row.offset - scroll;
            if (ry + row.height() <= top || ry >= bottom) continue;
            if(row.progression!=null){
                Progress progress=progress(player,row.progression);
                int right=x+width-11;
                boolean hover=!draggingThumb&&mouseY>=top&&mouseY<bottom&&mouseY>=ry&&mouseY<ry+row.height()
                        &&mouseX>=x+5&&mouseX<right+4;
                if(hover)context.fill(x+5,ry,right+4,ry+row.height(),0xFF323232);
                String level=Integer.toString(progress.level);
                context.drawText(font,font.trimToWidth(row.title,Math.max(1,width-24-font.getWidth(level))),x+9,ry+2,CompendiumStyle.TEXT_MUTED,false);
                context.drawText(font,level,right-font.getWidth(level),ry+2,CompendiumStyle.TEXT,false);
                HudStyle.meter(context,x+8,ry+12,width-18,11,row.progression.ink,progress.fraction);
                HudStyle.meterLabel(context,font,progress.label(),x+8,ry+12,width-18,11,CompendiumStyle.TEXT);
                if(hover)progressionTooltip=List.of(Text.literal(row.title+" "+level),
                        Text.literal(progress.required<=0?"Maximum level reached.":"Experience: "+progress.label()+" XP").formatted(net.minecraft.util.Formatting.GRAY),
                        Text.literal(row.progression==Progression.PLAYER?"Earned through combat participation.":"Earned through discoveries and exploration.").formatted(net.minecraft.util.Formatting.GRAY));
                continue;
            }
            if (row.stat == null) {
                context.drawText(font, row.title, x + 9, ry + 2, CompendiumStyle.TEXT_GOLD, false);
                continue;
            }

            double baseValue = row.stat.value().applyAsDouble(current);
            double displayed = preview == current ? baseValue : row.stat.value().applyAsDouble(preview);
            boolean changed = Math.abs(displayed - baseValue) > 0.000001;
            String value = row.stat.display(displayed);
            int right = x + width - 11;
            int labelLimit = Math.max(1, right - font.getWidth(value) - x - 15);
            boolean hover = !draggingThumb && mouseY >= top && mouseY < bottom && mouseY >= ry && mouseY < ry + 13
                    && mouseX >= x + 5 && mouseX < right + 4;
            if (hover) {
                context.fill(x + 5, ry, right + 4, ry + 13, 0xFF323232);
                hovered = row;
                hoveredValue = value;
                hoveredCurrent = changed ? row.stat.display(baseValue) : "";
            }
            String name = row.title;
            if (font.getWidth(name) > labelLimit) name = font.trimToWidth(name, Math.max(0, labelLimit - font.getWidth("…"))) + "…";
            context.drawText(font, name, x + 9, ry + 2, CompendiumStyle.TEXT_MUTED, false);
            context.drawText(font, value, right - font.getWidth(value), ry + 2,
                    changed ? displayed > baseValue ? CompendiumStyle.TEXT_SUCCESS : CompendiumStyle.TEXT_WARNING : CompendiumStyle.TEXT, false);
        }
        context.disableScissor();
        if (scrollbar.maxScroll() > 0) {
            CompendiumStyle.drawGuideScrollbar(context, scrollbarX(), top, scrollbar.height(),
                    (int) Math.round(scrollbar.thumbTop(scroll)), scrollbar.thumbHeight());
        }
        if(progressionTooltip!=null){progressionTooltip=new java.util.ArrayList<>(progressionTooltip);progressionTooltip.set(0,
                com.cranks.dungeons.client.guide.GuideText.link(progressionTooltip.getFirst(),com.cranks.dungeons.client.guide.GuideTarget.article("xp_progression")));
            context.drawTooltip(font,progressionTooltip,mouseX,mouseY);}
        else if (hovered != null) {
            List<net.minecraft.text.OrderedText> tooltip = new ArrayList<>();
            tooltip.add(com.cranks.dungeons.client.guide.GuideText.link(Text.literal(hovered.title + "  " + hoveredValue),
                    com.cranks.dungeons.client.guide.GuideTarget.stat(hovered.stat.guideId())).asOrderedText());
            if (!hoveredCurrent.isEmpty()) tooltip.add(Text.literal("Current: " + hoveredCurrent).asOrderedText());
            if (!hovered.stat.detail().isEmpty()) tooltip.addAll(font.wrapLines(
                    Text.literal(hovered.stat.detail()).formatted(net.minecraft.util.Formatting.GRAY),
                    Math.max(80, Math.min(240, context.getScaledWindowWidth() - 24))));
            context.drawOrderedTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private static Progress progress(PlayerEntity player,Progression track){
        if(track==Progression.PLAYER){
            var data=ModComponents.PROGRESSION.get(player);
            int required=data.required();
            if(required<=0)return new Progress(data.level(),0,0,1);
            int current=(int)(data.units()/com.cranks.dungeons.progression.ProgressionMath.UNIT);
            return new Progress(data.level(),current,required,(float)((double)data.units()/(required*com.cranks.dungeons.progression.ProgressionMath.UNIT)));
        }
        var data=ModComponents.WORLD_TREE_DATA.get(player);
        int required=data.getNextLevelXp();
        if(required<=0)return new Progress(data.getGatheringLevel(),0,0,1);
        int current=Math.clamp(data.getGatheringXp(),0,required);
        return new Progress(data.getGatheringLevel(),current,required,(float)current/required);
    }
}

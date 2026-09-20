package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Renders the Compendium guide pages in a two-column layout inspired by Ars Elixirum.
 *
 * <p><b>Left column:</b> Collapsible section navigation with page links.</p>
 * <p><b>Right column:</b> Scrollable rich text content area.</p>
 *
 * <p>Content is loaded from JSON resource files at:</p>
 * <ul>
 *   <li>{@code assets/cranks-dungeons/compendium/compendium_index.json} — section/page structure</li>
 *   <li>{@code assets/cranks-dungeons/compendium/pages/<page_id>.json} — individual page content</li>
 * </ul>
 */
public class CompendiumGuideTabRenderer implements CompendiumTabRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("CompendiumGuide");
    private static final Gson GSON = new Gson();

    // Layout constants
    private static final int MIN_NAV_WIDTH = 138;
    private static final int MAX_NAV_WIDTH = 190;
    private static final int NAV_PADDING = 6;
    private static final int CONTENT_PADDING = 12;
    private static final int SECTION_HEIGHT = 18;
    private static final int PAGE_LINK_HEIGHT = 14;
    private static final int SECTION_GAP = 4;
    private static final int NAV_SEARCH_HEIGHT = 20;
    private static final int NAV_CONTENT_TOP_OFFSET = 28;

    // Colors
    private static final int SECTION_HEADER_COLOR = CompendiumStyle.TEXT_DARK;
    private static final int PAGE_LINK_COLOR = 0xFF555555;
    private static final int PAGE_LINK_HOVER_COLOR = 0xFF202020;
    private static final int PAGE_LINK_SELECTED_COLOR = CompendiumStyle.TEXT_GOLD;
    private static final int CONTENT_TEXT_COLOR = CompendiumStyle.TEXT_DARK;
    private static final int HEADING_COLOR = CompendiumStyle.TEXT_DARK;
    private static final int SUBHEADING_COLOR = 0xFF6B4E00;
    private static final int NOTE_BG_COLOR = 0xFF686868;
    private static final int NOTE_BORDER_COLOR = CompendiumStyle.TEXT_GOLD;
    private static final int NOTE_TEXT_COLOR = CompendiumStyle.TEXT;
    private static final int SEARCH_HIGHLIGHT_COLOR = 0xFFFFFF55;

    // State
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;
    private int boxX, boxY, boxW, boxH;
    private int navWidth;
    private final List<Section> sections = new ArrayList<>();
    private final Map<String, PageContent> pageCache = new HashMap<>();
    private TextFieldWidget searchField;
    private String searchQuery = "";
    private String selectedPageId = null;
    private PageContent currentPage = null;
    private int navScrollOffset = 0;
    private int maxNavScroll = 0;
    private int contentScrollOffset = 0;
    private int maxContentScroll = 0;
    private boolean isDraggingNavigationScrollbar = false;
    private boolean isDraggingScrollbar = false;
    private int navigationDragClickOffsetY = 0;
    private int dragClickOffsetY = 0;
    private boolean loaded = false;

    // Data classes
    private record Section(String title, boolean collapsed, List<PageLink> pages) {
        Section withCollapsed(boolean collapsed) {
            return new Section(title, collapsed, pages);
        }
    }
    private record PageLink(String id, String title) {}
    private record PageContent(String title, List<ContentElement> elements) {}
    private record ContentElement(String type, String value, String style) {}

    @Override
    public void init(CompendiumScreen parent, int contentX, int contentY, int contentW, int contentH) {
        this.parent = parent;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentW = contentW;
        this.contentH = contentH;
        this.isDraggingNavigationScrollbar = false;
        this.isDraggingScrollbar = false;
        
        this.boxW = Math.max(1, contentW);
        this.boxH = Math.max(1, contentH);
        this.boxX = contentX;
        this.boxY = contentY;
        this.navWidth = Math.min(MAX_NAV_WIDTH, Math.max(MIN_NAV_WIDTH, contentW / 4));
        this.navWidth = Math.min(this.navWidth, Math.max(1, boxW - 130));

        if (!loaded) {
            loadIndex();
            loaded = true;
            // Auto-select first page
            if (!sections.isEmpty() && !sections.get(0).pages.isEmpty()) {
                selectedPageId = sections.get(0).pages.get(0).id;
                loadPage(selectedPageId);
            }
        }

        int searchX = boxX + 5 + NAV_PADDING;
        int searchY = boxY + 5 + 2;
        this.searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                searchX, searchY, Math.max(20, navWidth - NAV_PADDING * 2), NAV_SEARCH_HEIGHT,
                Text.literal("Search topics"));
        this.searchField.setMaxLength(80);
        this.searchField.setPlaceholder(Text.literal("Search topics..."));
        this.searchField.setChangedListener(this::updateSearchQuery);
        this.searchField.setText(searchQuery);
        parent.addTabWidget(this.searchField);
    }

    @Override
    public void onActivated() {
        this.isDraggingNavigationScrollbar = false;
        this.isDraggingScrollbar = false;
        if (selectedPageId == null && !sections.isEmpty()) {
            for (Section section : sections) {
                if (!section.pages.isEmpty()) {
                    selectedPageId = section.pages.get(0).id;
                    loadPage(selectedPageId);
                    break;
                }
            }
        }
    }

    @Override
    public void onDeactivated() {
        this.isDraggingNavigationScrollbar = false;
        this.isDraggingScrollbar = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // The screen header owns the tab title; page titles are rendered in
        // the content column below.

        // Render the navigation column inside a restrained vanilla-style well.
        int navX = boxX + 5;
        int navY = boxY + 5;
        int navW = navWidth;
        int navH = boxH - 10;
        renderNavigation(context, tr, navX, navY, navW, navH, mouseX, mouseY);

        // Render the page column inside the same shared container.
        int pageX = boxX + navWidth + 10;
        int pageY = boxY + 5;
        int pageW = boxW - navWidth - 15;
        int pageH = boxH - 10;
        renderPageContent(context, tr, pageX, pageY, pageW, pageH, mouseX, mouseY);
    }

    private void renderNavigation(DrawContext context, TextRenderer tr, int navX, int navY,
                                   int navW, int navH, int mouseX, int mouseY) {
        int navContentTop = navY + NAV_CONTENT_TOP_OFFSET;
        context.fill(navX + 6, navContentTop - 4, navX + navW - 6, navContentTop - 3,
                0x99FFFFFF);

        // Navigation and content are separated by a quiet divider instead of
        // another solid rectangle, matching the visual economy of Advancements.
        int dividerX = boxX + navWidth + 4;
        context.fill(dividerX, boxY + 8, dividerX + 1, boxY + boxH - 8,
                0x80555555);
        context.fill(dividerX + 1, boxY + 8, dividerX + 2, boxY + boxH - 8,
                0x40555555);

        maxNavScroll = Math.max(0, getNavigationContentHeight()
                - (navH - NAV_CONTENT_TOP_OFFSET - NAV_PADDING));
        navScrollOffset = Math.max(0, Math.min(maxNavScroll, navScrollOffset));

        context.enableScissor(navX + 2, navContentTop, navX + navW - 2, navY + navH - 2);

        int curY = navContentTop - navScrollOffset;

        List<Section> visibleSections = getVisibleSections();
        for (Section section : visibleSections) {

            // Section header. The selected section and page carry the
            // interaction feedback, keeping the list visually quiet.
            String sectionPrefix = section.collapsed ? "> " : "v ";

            boolean headerHovered = mouseX >= navX && mouseX < navX + navW
                    && mouseY >= curY && mouseY < curY + SECTION_HEIGHT;

            int headerColor = headerHovered ? 0xFF000000 : SECTION_HEADER_COLOR;
            context.drawText(tr, Text.literal(sectionPrefix), navX + NAV_PADDING, curY + 4, headerColor, false);
            drawSearchHighlightedText(context, tr, section.title,
                    navX + NAV_PADDING + tr.getWidth(sectionPrefix), curY + 4, headerColor);
            curY += SECTION_HEIGHT;

            // Page links (if section is expanded)
            if (!section.collapsed) {
                for (PageLink page : section.pages) {
                    boolean isSelected = page.id.equals(selectedPageId);
                    boolean pageHovered = mouseX >= navX && mouseX < navX + navW
                            && mouseY >= curY && mouseY < curY + PAGE_LINK_HEIGHT;

                    int linkColor;
                    if (isSelected) {
                        linkColor = PAGE_LINK_SELECTED_COLOR;
                        // Selection indicator
                        context.fill(navX, curY, navX + 2, curY + PAGE_LINK_HEIGHT, PAGE_LINK_SELECTED_COLOR);
                    } else if (pageHovered) {
                        linkColor = PAGE_LINK_HOVER_COLOR;
                    } else {
                        linkColor = PAGE_LINK_COLOR;
                    }

                    drawSearchHighlightedText(context, tr, "  " + page.title,
                            navX + NAV_PADDING + 4, curY + 2, linkColor);
                    curY += PAGE_LINK_HEIGHT;
                }
            }

            curY += SECTION_GAP;
        }

        context.disableScissor();

        if (maxNavScroll > 0) {
            ScrollbarMetrics scrollbar = getNavigationScrollbarMetrics();
            CompendiumStyle.drawVanillaScrollbar(context, scrollbar.trackX, scrollbar.trackY,
                    scrollbar.trackW, scrollbar.trackH, scrollbar.thumbY, scrollbar.thumbH);
        }
    }

    private int getNavigationContentHeight() {
        int height = NAV_PADDING * 2;
        for (Section section : getVisibleSections()) {
            height += SECTION_HEIGHT + SECTION_GAP;
            if (!section.collapsed) {
                height += section.pages.size() * PAGE_LINK_HEIGHT;
            }
        }
        return height;
    }

    private void renderPageContent(DrawContext context, TextRenderer tr, int pageX, int pageY,
                                    int pageW, int pageH, int mouseX, int mouseY) {
        if (currentPage == null) {
            // No page selected — show welcome text
            String welcome = "Select a topic from the menu.";
            int textW = tr.getWidth(welcome);
            context.drawText(tr, Text.literal(welcome),
                    pageX + (pageW - textW) / 2,
                    pageY + pageH / 2 - 4,
                    PAGE_LINK_COLOR, false);
            return;
        }

        context.enableScissor(pageX + 2, pageY + 2, pageX + pageW - 2, pageY + pageH - 2);

        int curY = pageY + CONTENT_PADDING - contentScrollOffset;
        int maxTextWidth = pageW - CONTENT_PADDING * 2;

        // Page title
        Text titleText = Text.literal(currentPage.title);
        drawSearchHighlightedText(context, tr, currentPage.title,
                pageX + CONTENT_PADDING, curY, HEADING_COLOR);
        curY += 16;

        // Separator line under title
        context.fill(pageX + CONTENT_PADDING, curY,
                pageX + CONTENT_PADDING + Math.min(maxTextWidth, tr.getWidth(titleText) + 20),
                curY + 1, CompendiumStyle.PANEL_HIGHLIGHT);
        curY += 8;

        // Render content elements
        for (ContentElement element : currentPage.elements) {
            switch (element.type) {
                case "heading" -> {
                    curY += 6;
                    drawSearchHighlightedText(context, tr, element.value,
                            pageX + CONTENT_PADDING, curY, HEADING_COLOR);
                    curY += 14;
                }
                case "subheading" -> {
                    curY += 3;
                    drawSearchHighlightedText(context, tr, element.value,
                            pageX + CONTENT_PADDING + 2, curY, SUBHEADING_COLOR);
                    curY += 12;
                }
                case "text" -> {
                    curY = drawWrappedText(context, tr, element.value,
                            pageX + CONTENT_PADDING, curY, maxTextWidth, CONTENT_TEXT_COLOR);
                    curY += 6;
                }
                case "list" -> {
                    curY = drawList(context, tr, element.value, element.style,
                            pageX + CONTENT_PADDING, curY, maxTextWidth);
                    curY += 5;
                }
                case "stat_catalog" -> {
                    curY = drawStatCatalog(context, tr, element.value,
                            pageX + CONTENT_PADDING, curY, maxTextWidth);
                    curY += 6;
                }
                case "note" -> {
                    curY += 2;
                    curY = drawNoteBlock(context, tr, element.value, element.style,
                            pageX + CONTENT_PADDING, curY, maxTextWidth);
                    curY += 6;
                }
                case "spacer" -> curY += 10;
            }
        }

        // Update max scroll
        int totalHeight = curY + contentScrollOffset - (pageY + CONTENT_PADDING);
        maxContentScroll = Math.max(0, totalHeight - pageH + CONTENT_PADDING * 2);

        context.disableScissor();

        // Use the same vanilla scrollbar sprites as the rest of Minecraft's
        // scrollable screens; resource packs can replace them directly.
        if (maxContentScroll > 0) {
            ScrollbarMetrics scrollbar = getContentScrollbarMetrics(pageX, pageY, pageW, pageH);
            CompendiumStyle.drawVanillaScrollbar(context, scrollbar.trackX, scrollbar.trackY,
                    scrollbar.trackW, scrollbar.trackH, scrollbar.thumbY, scrollbar.thumbH);
        }
    }

    private record ScrollbarMetrics(int trackX, int trackY, int trackW, int trackH,
                                    int thumbY, int thumbH) {
    }

    private ScrollbarMetrics getNavigationScrollbarMetrics() {
        int navX = boxX + 5;
        int navY = boxY + 5;
        int navW = navWidth;
        int navH = boxH - 10;
        int trackX = navX + navW - 7;
        int trackY = navY + NAV_CONTENT_TOP_OFFSET;
        int trackW = 6;
        int trackH = navH - NAV_CONTENT_TOP_OFFSET - 2;
        int thumbH = Math.max(18, (int) ((float) (navH - NAV_CONTENT_TOP_OFFSET - NAV_PADDING)
                / getNavigationContentHeight() * trackH));
        int thumbY = trackY + (int) ((float) navScrollOffset / maxNavScroll * (trackH - thumbH));
        return new ScrollbarMetrics(trackX, trackY, trackW, trackH, thumbY, thumbH);
    }

    private ScrollbarMetrics getContentScrollbarMetrics(int pageX, int pageY, int pageW, int pageH) {
        int trackW = 6;
        int trackX = pageX + pageW - trackW - 2;
        int trackY = pageY + 2;
        int trackH = pageH - 4;
        int thumbH = Math.max(22, (int) ((float) pageH / (pageH + maxContentScroll) * trackH));
        int thumbY = trackY + (int) ((float) contentScrollOffset / maxContentScroll * (trackH - thumbH));
        return new ScrollbarMetrics(trackX, trackY, trackW, trackH, thumbY, thumbH);
    }

    private boolean isScrollbarTrackHovered(ScrollbarMetrics scrollbar, double mouseX, double mouseY) {
        return mouseX >= scrollbar.trackX - 4 && mouseX <= scrollbar.trackX + scrollbar.trackW + 4
                && mouseY >= scrollbar.trackY && mouseY <= scrollbar.trackY + scrollbar.trackH;
    }

    private void drawSearchHighlightedText(DrawContext context, TextRenderer tr,
                                           String text, int x, int y, int color) {
        String query = searchQuery.trim();
        if (query.isEmpty()) {
            context.drawText(tr, Text.literal(text), x, y, color, false);
            return;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        int cursor = 0;
        int drawX = x;

        while (cursor < text.length()) {
            int matchStart = lowerText.indexOf(lowerQuery, cursor);
            if (matchStart < 0) {
                String remaining = text.substring(cursor);
                context.drawText(tr, Text.literal(remaining), drawX, y, color, false);
                break;
            }

            if (matchStart > cursor) {
                String before = text.substring(cursor, matchStart);
                context.drawText(tr, Text.literal(before), drawX, y, color, false);
                drawX += tr.getWidth(before);
            }

            int matchEnd = matchStart + lowerQuery.length();
            String match = text.substring(matchStart, matchEnd);
            int matchWidth = tr.getWidth(match);
            context.fill(drawX, y - 1, drawX + matchWidth, y + 10, SEARCH_HIGHLIGHT_COLOR);
            context.drawText(tr, Text.literal(match), drawX, y, CompendiumStyle.TEXT_DARK, false);
            drawX += matchWidth;
            cursor = matchEnd;
        }
    }

    private int drawWrappedText(DrawContext context, TextRenderer tr, String text,
                                 int x, int y, int maxWidth, int color) {
        // Split into paragraphs first (handle \n)
        String[] paragraphs = text.split("\\\\n|\\n");
        int curY = y;
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                curY += 6;
                continue;
            }
            // Word wrap
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.isEmpty() ? word : line + " " + word;
                if (tr.getWidth(test) > maxWidth && !line.isEmpty()) {
                    drawSearchHighlightedText(context, tr, line.toString(), x, curY, color);
                    curY += 10;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (!line.isEmpty()) {
                drawSearchHighlightedText(context, tr, line.toString(), x, curY, color);
                curY += 10;
            }
        }
        return curY;
    }

    private int drawList(DrawContext context, TextRenderer tr, String text, String style,
                         int x, int y, int maxWidth) {
        String[] entries = text.split("\\\\n|\\n");
        int curY = y;
        boolean numbered = "numbered".equalsIgnoreCase(style);
        int number = 1;
        for (String raw : entries) {
            String entry = raw.trim();
            if (entry.isEmpty()) continue;
            String marker = numbered ? (number++) + "." : "-";
            context.drawText(tr, Text.literal(marker), x + 2, curY, HEADING_COLOR, false);
            curY = drawWrappedText(context, tr, entry, x + 15, curY,
                    Math.max(20, maxWidth - 15), CONTENT_TEXT_COLOR);
            curY += 2;
        }
        return curY;
    }

    /** Draw the live random-affix pool so guide values stay in sync with StatRegistry. */
    private int drawStatCatalog(DrawContext context, TextRenderer tr, String value,
                                int x, int y, int maxWidth) {
        EquipmentType type;
        try {
            type = EquipmentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return drawWrappedText(context, tr, "Unknown equipment catalog: " + value,
                    x, y, maxWidth, 0xFFFF6666);
        }

        List<CustomStat> stats = new ArrayList<>(StatRegistry.getStatsByEquipmentType(type));
        stats.removeIf(stat -> !StatRegistry.isRandomlyRollable(stat));
        stats.sort(Comparator
                .comparing(CustomStat::getAffixType)
                .thenComparing(CustomStat::getDisplayName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CustomStat::getId));

        int curY = y;
        CustomStat.AffixType lastAffix = null;
        for (CustomStat stat : stats) {
            if (lastAffix != stat.getAffixType()) {
                curY += lastAffix == null ? 1 : 6;
                String groupTitle = stat.getAffixType() == CustomStat.AffixType.PREFIX
                        ? "Prefixes" : "Suffixes";
                drawSearchHighlightedText(context, tr, groupTitle, x, curY, SUBHEADING_COLOR);
                curY += 13;
                lastAffix = stat.getAffixType();
            }

            Integer rgb = stat.getColor().getColorValue();
            drawSearchHighlightedText(context, tr, stat.getDisplayName(), x + 2, curY,
                    rgb != null ? 0xFF000000 | rgb : CONTENT_TEXT_COLOR);
            curY += 10;

            String metadata = "Rune level " + stat.getLevel()
                    + "  |  Weight " + stat.getWeight()
                    + "  |  Affinity " + formatAffinity(stat.getArmorBaseType().name());
            curY = drawWrappedText(context, tr, metadata, x + 8, curY,
                    Math.max(20, maxWidth - 8), CompendiumStyle.TEXT_MUTED);

            for (CustomStat.StatComponent component : stat.getComponents()) {
                StringBuilder ranges = new StringBuilder(component.getDisplayName()).append(": ");
                for (int tier = 10; tier >= 1; tier--) {
                    if (tier < 10) ranges.append("  ");
                    ranges.append('T').append(tier).append(' ').append(component.getFormattedMinMax(tier));
                }
                curY = drawWrappedText(context, tr, ranges.toString(), x + 8, curY,
                        Math.max(20, maxWidth - 8), CONTENT_TEXT_COLOR);
            }
            curY += 5;
        }

        if (stats.isEmpty()) {
            curY = drawWrappedText(context, tr,
                    "This equipment type has no random rune pool.", x, curY, maxWidth, CONTENT_TEXT_COLOR);
        }
        return curY;
    }

    private String formatAffinity(String affinity) {
        return switch (affinity) {
            case "STR" -> "Strength";
            case "DEX" -> "Dexterity";
            case "INT" -> "Intelligence";
            case "STR_DEX" -> "Strength + Dexterity";
            case "STR_INT" -> "Strength + Intelligence";
            case "DEX_INT" -> "Dexterity + Intelligence";
            case "ALL" -> "all defenses";
            default -> "any base";
        };
    }

    private int drawNoteBlock(DrawContext context, TextRenderer tr, String text, String style,
                               int x, int y, int maxWidth) {
        int noteColor;
        int noteBorder;
        String prefix;
        switch (style != null ? style : "info") {
            case "warning" -> {
                noteColor = 0xFF6A5A2A;
                noteBorder = CompendiumStyle.TEXT_GOLD_LIGHT;
                prefix = "⚠ Warning";
            }
            case "tip" -> {
                noteColor = 0xFF35633D;
                noteBorder = CompendiumStyle.TEXT_SUCCESS;
                prefix = "💡 Tip";
            }
            case "important" -> {
                noteColor = 0xFF633737;
                noteBorder = CompendiumStyle.TEXT_WARNING;
                prefix = "❗ Important";
            }
            default -> {
                noteColor = NOTE_BG_COLOR;
                noteBorder = NOTE_BORDER_COLOR;
                prefix = "ℹ Note";
            }
        }

        int noteWidth = maxWidth - 8;
        int noteX = x + 4;

        // Pre-calculate note height
        int preCalcY = y + 20; // header height
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (tr.getWidth(test) > noteWidth - 12 && !line.isEmpty()) {
                preCalcY += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) preCalcY += 10;
        int noteHeight = preCalcY - y + 6;

        // Draw note background and border
        context.fill(noteX, y, noteX + noteWidth, y + noteHeight, noteColor);
        context.fill(noteX, y, noteX + 2, y + noteHeight, noteBorder); // Left border

        // Draw prefix
        drawSearchHighlightedText(context, tr, prefix, noteX + 6, y + 4, noteBorder);

        // Draw text
        int textY = y + 18;
        line = new StringBuilder();
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (tr.getWidth(test) > noteWidth - 12 && !line.isEmpty()) {
                drawSearchHighlightedText(context, tr, line.toString(), noteX + 6, textY, NOTE_TEXT_COLOR);
                textY += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) {
            drawSearchHighlightedText(context, tr, line.toString(), noteX + 6, textY, NOTE_TEXT_COLOR);
            textY += 10;
        }

        return y + noteHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // Check the Topics navigation scrollbar before the page scrollbar.
        if (maxNavScroll > 0) {
            ScrollbarMetrics scrollbar = getNavigationScrollbarMetrics();
            if (isScrollbarTrackHovered(scrollbar, mouseX, mouseY)) {
                isDraggingNavigationScrollbar = true;
                if (mouseY >= scrollbar.thumbY && mouseY <= scrollbar.thumbY + scrollbar.thumbH) {
                    navigationDragClickOffsetY = (int) (mouseY - scrollbar.thumbY);
                } else {
                    navigationDragClickOffsetY = scrollbar.thumbH / 2;
                    int availableTrack = scrollbar.trackH - scrollbar.thumbH;
                    if (availableTrack > 0) {
                        float relativeY = (float) (mouseY - scrollbar.trackY - navigationDragClickOffsetY);
                        float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                        navScrollOffset = Math.round(ratio * maxNavScroll);
                    }
                }
                return true;
            }
        }

        // Check content scrollbar click & drag start
        if (maxContentScroll > 0) {
            int pageX = boxX + navWidth + 10;
            int pageY = boxY + 5;
            int pageW = boxW - navWidth - 15;
            int pageH = boxH - 10;
            ScrollbarMetrics scrollbar = getContentScrollbarMetrics(pageX, pageY, pageW, pageH);

            if (isScrollbarTrackHovered(scrollbar, mouseX, mouseY)) {
                isDraggingScrollbar = true;
                if (mouseY >= scrollbar.thumbY && mouseY <= scrollbar.thumbY + scrollbar.thumbH) {
                    dragClickOffsetY = (int) (mouseY - scrollbar.thumbY);
                } else {
                    dragClickOffsetY = scrollbar.thumbH / 2;
                    int availableTrack = scrollbar.trackH - scrollbar.thumbH;
                    if (availableTrack > 0) {
                        float relativeY = (float) (mouseY - scrollbar.trackY - dragClickOffsetY);
                        float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                        contentScrollOffset = Math.round(ratio * maxContentScroll);
                    }
                }
                return true;
            }
        }

        int navX = boxX + 5;
        int navY = boxY + 5;
        int navW = navWidth;
        int navH = boxH - 10;

        // Check nav column clicks
        if (mouseX >= navX && mouseX < navX + navW
                && mouseY >= navY && mouseY < navY + navH) {
            int curY = navY + NAV_CONTENT_TOP_OFFSET - navScrollOffset;

            List<Section> visibleSections = getVisibleSections();
            for (Section section : visibleSections) {

                // Section header click → toggle collapse
                if (mouseY >= curY && mouseY < curY + SECTION_HEIGHT) {
                    if (searchQuery.isBlank()) {
                        int sectionIndex = sections.indexOf(section);
                        if (sectionIndex >= 0) {
                            sections.set(sectionIndex, section.withCollapsed(!section.collapsed));
                        }
                    }
                    return true;
                }
                curY += SECTION_HEIGHT;

                // Page link clicks
                if (!section.collapsed) {
                    for (PageLink page : section.pages) {
                        if (mouseY >= curY && mouseY < curY + PAGE_LINK_HEIGHT) {
                            selectedPageId = page.id;
                            contentScrollOffset = 0;
                            loadPage(page.id);
                            return true;
                        }
                        curY += PAGE_LINK_HEIGHT;
                    }
                }
                curY += SECTION_GAP;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingNavigationScrollbar && maxNavScroll > 0) {
            ScrollbarMetrics scrollbar = getNavigationScrollbarMetrics();
            int availableTrack = scrollbar.trackH - scrollbar.thumbH;

            if (availableTrack > 0) {
                float relativeY = (float) (mouseY - scrollbar.trackY - navigationDragClickOffsetY);
                float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                navScrollOffset = Math.round(ratio * maxNavScroll);
            }
            return true;
        }

        if (isDraggingScrollbar && maxContentScroll > 0) {
            int pageY = boxY + 5;
            int pageH = boxH - 10;
            int pageX = boxX + navWidth + 10;
            int pageW = boxW - navWidth - 15;
            ScrollbarMetrics scrollbar = getContentScrollbarMetrics(pageX, pageY, pageW, pageH);
            int availableTrack = scrollbar.trackH - scrollbar.thumbH;

            if (availableTrack > 0) {
                float relativeY = (float) (mouseY - scrollbar.trackY - dragClickOffsetY);
                float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                contentScrollOffset = Math.round(ratio * maxContentScroll);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (isDraggingNavigationScrollbar || isDraggingScrollbar)) {
            isDraggingNavigationScrollbar = false;
            isDraggingScrollbar = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int navX = boxX + 5;
        int navY = boxY + 5;
        int navW = navWidth;
        int navH = boxH - 10;

        if (mouseX >= navX && mouseX < navX + navW
                && mouseY >= navY && mouseY < navY + navH) {
            // Scroll navigation
            navScrollOffset = Math.max(0, Math.min(maxNavScroll,
                    navScrollOffset - (int) (verticalAmount * 10)));
            return true;
        } else if (mouseX >= navX + navW && mouseX < boxX + boxW && mouseY >= boxY && mouseY < boxY + boxH) {
            // Scroll content (only if hovering over content area)
            contentScrollOffset = Math.max(0, Math.min(maxContentScroll,
                    contentScrollOffset - (int) (verticalAmount * 10)));
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    private void updateSearchQuery(String value) {
        String nextQuery = value == null ? "" : value.trim();
        if (nextQuery.equals(searchQuery)) {
            return;
        }

        searchQuery = nextQuery;
        navScrollOffset = 0;

        if (!searchQuery.isBlank()) {
            PageLink firstMatch = findFirstSearchMatch();
            if (firstMatch != null
                    && (currentPage == null || !pageContains(currentPage, searchQuery))) {
                selectedPageId = firstMatch.id;
                contentScrollOffset = 0;
                loadPage(selectedPageId);
            }
        }
    }

    private List<Section> getVisibleSections() {
        if (searchQuery.isBlank()) {
            return sections;
        }

        List<Section> visibleSections = new ArrayList<>();
        for (Section section : sections) {
            boolean sectionMatches = textContains(section.title, searchQuery);
            List<PageLink> visiblePages = new ArrayList<>();
            for (PageLink page : section.pages) {
                if (sectionMatches || pageMatches(page, searchQuery)) {
                    visiblePages.add(page);
                }
            }

            if (!visiblePages.isEmpty()) {
                // Search results are always expanded so matching topics are
                // immediately visible even when their original section was collapsed.
                visibleSections.add(new Section(section.title, false, visiblePages));
            }
        }
        return visibleSections;
    }

    private PageLink findFirstSearchMatch() {
        for (Section section : sections) {
            for (PageLink page : section.pages) {
                if (pageMatches(page, searchQuery)) {
                    return page;
                }
            }
        }
        return null;
    }

    private boolean pageMatches(PageLink page, String query) {
        if (textContains(page.title, query)) {
            return true;
        }

        return pageContains(pageCache.computeIfAbsent(page.id, this::readPage), query);
    }

    private boolean pageContains(PageContent page, String query) {
        if (page == null || textContains(page.title, query)) {
            return page != null && textContains(page.title, query);
        }

        for (ContentElement element : page.elements) {
            if (textContains(element.value, query)) {
                return true;
            }
        }
        return false;
    }

    private boolean textContains(String value, String query) {
        return value != null && query != null
                && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    // --- Data Loading ---

    private void loadIndex() {
        sections.clear();
        pageCache.clear();
        try {
            var resource = MinecraftClient.getInstance().getResourceManager()
                    .getResource(Identifier.of("cranks-dungeons", "compendium/compendium_index.json"));

            if (resource.isEmpty()) {
                LOGGER.warn("Compendium index not found, using defaults");
                createDefaultSections();
                return;
            }

            try (InputStream is = resource.get().getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                JsonArray sectionsArray = root.getAsJsonArray("sections");

                for (JsonElement sectionEl : sectionsArray) {
                    JsonObject sectionObj = sectionEl.getAsJsonObject();
                    String title = sectionObj.get("title").getAsString();
                    boolean collapsed = sectionObj.has("collapsed") && sectionObj.get("collapsed").getAsBoolean();

                    List<PageLink> pages = new ArrayList<>();
                    JsonArray pagesArray = sectionObj.getAsJsonArray("pages");
                    for (JsonElement pageEl : pagesArray) {
                        JsonObject pageObj = pageEl.getAsJsonObject();
                        pages.add(new PageLink(
                                pageObj.get("id").getAsString(),
                                pageObj.get("title").getAsString()
                        ));
                    }

                    sections.add(new Section(title, collapsed, pages));
                }
            }

            LOGGER.info("Loaded compendium index with {} sections", sections.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load compendium index", e);
            createDefaultSections();
        }
    }

    private void loadPage(String pageId) {
        currentPage = pageCache.computeIfAbsent(pageId, this::readPage);
    }

    private PageContent readPage(String pageId) {
        try {
            var resource = MinecraftClient.getInstance().getResourceManager()
                    .getResource(Identifier.of("cranks-dungeons", "compendium/pages/" + pageId + ".json"));

            if (resource.isEmpty()) {
                LOGGER.warn("Compendium page '{}' not found", pageId);
                return new PageContent("Page Not Found",
                        List.of(new ContentElement("text", "This page has not been written yet.", null)));
            }

            try (InputStream is = resource.get().getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                String title = root.get("title").getAsString();

                List<ContentElement> elements = new ArrayList<>();
                JsonArray contentArray = root.getAsJsonArray("content");
                for (JsonElement el : contentArray) {
                    JsonObject obj = el.getAsJsonObject();
                    String type = obj.get("type").getAsString();
                    String value = obj.has("value") ? obj.get("value").getAsString() : "";
                    String style = obj.has("style") ? obj.get("style").getAsString() : null;
                    elements.add(new ContentElement(type, value, style));
                }

                return new PageContent(title, elements);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load compendium page: {}", pageId, e);
            return new PageContent("Error",
                    List.of(new ContentElement("text", "Failed to load page: " + e.getMessage(), null)));
        }
    }

    private void createDefaultSections() {
        sections.add(new Section("Getting Started", false, List.of(
                new PageLink("introduction", "Introduction"),
                new PageLink("first_steps", "First Steps"),
                new PageLink("controls_hud", "Controls & HUD")
        )));
        sections.add(new Section("Character Progression", false, List.of(
                new PageLink("classes", "Choosing a Class"),
                new PageLink("xp_progression", "Levels & XP"),
                new PageLink("passive_tree", "Passive Skill Tree"),
                new PageLink("primary_attributes", "Primary Attributes"),
                new PageLink("keystones", "Keystones")
        )));
        sections.add(new Section("Combat Systems", true, List.of(
                new PageLink("stats", "Reading Your Stats"),
                new PageLink("damage", "Damage & Criticals"),
                new PageLink("defences", "Defences"),
                new PageLink("enemies", "Enemies & Threats"),
                new PageLink("mob_equipment", "Mob Equipment"),
                new PageLink("ailments", "Ailments"),
                new PageLink("recovery", "Recovery & Mana"),
                new PageLink("food_effects", "Food & Effects")
        )));
        sections.add(new Section("Equipment Fundamentals", true, List.of(
                new PageLink("equipment", "Equipment Bases"),
                new PageLink("item_rarity", "Item Rarity"),
                new PageLink("rarity_requirements", "Item Requirements"),
                new PageLink("runes", "Understanding Runes"),
                new PageLink("rune_tiers", "Tiers, Levels & Weights"),
                new PageLink("unique_equipment", "Unique Equipment")
        )));
        sections.add(new Section("Crafting & Upgrades", true, List.of(
                new PageLink("runic_altar", "Using the Runic Altar"),
                new PageLink("tomes", "Random Runic Tomes"),
                new PageLink("targeted_tomes", "Targeted Tomes"),
                new PageLink("artificial_tome", "Artificial Runes"),
                new PageLink("quality", "Quality Station"),
                new PageLink("enchantments", "Enchantments")
        )));
        sections.add(new Section("Rune Pools: Weapons", true, List.of(
                new PageLink("rune_sword", "Swords"),
                new PageLink("rune_spear", "Spears"),
                new PageLink("rune_bow", "Bows"),
                new PageLink("rune_crossbow", "Crossbows"),
                new PageLink("rune_shield", "Shields")
        )));
        sections.add(new Section("Rune Pools: Armour", true, List.of(
                new PageLink("rune_helmet", "Helmets"),
                new PageLink("rune_chestplate", "Chestplates"),
                new PageLink("rune_leggings", "Leggings"),
                new PageLink("rune_boots", "Boots")
        )));
        sections.add(new Section("World & Gathering", true, List.of(
                new PageLink("world_tree", "The World Tree"),
                new PageLink("gathering", "Gathering XP"),
                new PageLink("gathering_stats", "Yield & Fortune"),
                new PageLink("loot", "Tome & Material Loot"),
                new PageLink("parties", "Parties")
        )));
        sections.add(new Section("Horse Mastery", true, List.of(
                new PageLink("horses", "Horse Statistics"),
                new PageLink("breeding", "Breeding & Genetics"),
                new PageLink("horse_training", "Training & Stamina"),
                new PageLink("horse_traits", "Traits & Mutations"),
                new PageLink("animal_armor", "Animal Armour")
        )));
        sections.add(new Section("Reference", true, List.of(
                new PageLink("vanilla_changes", "Changed Vanilla Rules")
        )));
    }
}

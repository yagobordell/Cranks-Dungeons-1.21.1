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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    private static final int NAV_WIDTH = 154;
    private static final int NAV_PADDING = 6;
    private static final int CONTENT_PADDING = 12;
    private static final int SECTION_HEIGHT = 18;
    private static final int PAGE_LINK_HEIGHT = 14;
    private static final int SECTION_GAP = 4;

    // Colors
    private static final int NAV_BG_COLOR = 0xFF141418;
    private static final int NAV_BORDER_COLOR = 0xFF2E2E36;
    private static final int CONTENT_BG_COLOR = 0xFF18181E;
    private static final int SECTION_HEADER_COLOR = 0xFFE2E2EA;
    private static final int PAGE_LINK_COLOR = 0xFF9E9EAA;
    private static final int PAGE_LINK_HOVER_COLOR = 0xFFFFFFFF;
    private static final int PAGE_LINK_SELECTED_COLOR = 0xFFFFAA00;
    private static final int CONTENT_TEXT_COLOR = 0xFFD4D4DC;
    private static final int HEADING_COLOR = 0xFFFFAA00;
    private static final int SUBHEADING_COLOR = 0xFFFFD27A;
    private static final int NOTE_BG_COLOR = 0xFF1E1E26;
    private static final int NOTE_BORDER_COLOR = 0xFFFFAA00;
    private static final int NOTE_TEXT_COLOR = 0xFFE0E0E8;
    private static final int ARROW_COLOR = 0xFF888894;

    // State
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;
    private int boxX, boxY, boxW, boxH;
    private final List<Section> sections = new ArrayList<>();
    private String selectedPageId = null;
    private PageContent currentPage = null;
    private int navScrollOffset = 0;
    private int maxNavScroll = 0;
    private int contentScrollOffset = 0;
    private int maxContentScroll = 0;
    private boolean isDraggingScrollbar = false;
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
        this.isDraggingScrollbar = false;
        
        this.boxW = Math.min(600, contentW - 40);
        this.boxH = Math.min(320, contentH - 40);
        this.boxX = contentX + (contentW - boxW) / 2;
        this.boxY = contentY + (contentH - boxH) / 2;

        if (!loaded) {
            loadIndex();
            loaded = true;
            // Auto-select first page
            if (!sections.isEmpty() && !sections.get(0).pages.isEmpty()) {
                selectedPageId = sections.get(0).pages.get(0).id;
                loadPage(selectedPageId);
            }
        }
    }

    @Override
    public void onActivated() {
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
        this.isDraggingScrollbar = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // Draw title floating above the box
        String titleText = "§6Compendium Guide";
        int titleW = tr.getWidth(titleText);
        context.drawTextWithShadow(tr, titleText, boxX + (boxW - titleW) / 2, boxY - 16, 0xFFFFAA00);

        // Main Outset Beveled Stone Frame
        ShapeRenderer.drawBeveledPanel(context, boxX, boxY, boxW, boxH);

        // Render navigation column inside an Inset Stone Well
        int navX = boxX + 4;
        int navY = boxY + 4;
        int navW = NAV_WIDTH;
        int navH = boxH - 8;
        renderNavigation(context, tr, navX, navY, navW, navH, mouseX, mouseY);

        // Render content column inside an Inset Stone Well
        int pageX = boxX + NAV_WIDTH + 6;
        int pageY = boxY + 4;
        int pageW = boxW - NAV_WIDTH - 10;
        int pageH = boxH - 8;
        renderPageContent(context, tr, pageX, pageY, pageW, pageH, mouseX, mouseY);
    }

    private void renderNavigation(DrawContext context, TextRenderer tr, int navX, int navY,
                                   int navW, int navH, int mouseX, int mouseY) {
        // Inset sunken stone well for navigation
        ShapeRenderer.drawInsetPanel(context, navX, navY, navW, navH);

        maxNavScroll = Math.max(0, getNavigationContentHeight() - (navH - NAV_PADDING * 2));
        navScrollOffset = Math.max(0, Math.min(maxNavScroll, navScrollOffset));

        context.enableScissor(navX + 2, navY + 2, navX + navW - 2, navY + navH - 2);

        int curY = navY + NAV_PADDING - navScrollOffset;

        for (int si = 0; si < sections.size(); si++) {
            Section section = sections.get(si);

            // Section header with collapse arrow
            String arrow = section.collapsed ? "▶" : "▼";
            String headerText = arrow + " " + section.title;

            boolean headerHovered = mouseX >= navX && mouseX < navX + navW
                    && mouseY >= curY && mouseY < curY + SECTION_HEIGHT;

            int headerColor = headerHovered ? 0xFFFFFFFF : SECTION_HEADER_COLOR;
            context.drawText(tr, Text.literal(headerText), navX + NAV_PADDING, curY + 4, headerColor, false);
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

                    context.drawText(tr, Text.literal("  " + page.title),
                            navX + NAV_PADDING + 4, curY + 2, linkColor, false);
                    curY += PAGE_LINK_HEIGHT;
                }
            }

            curY += SECTION_GAP;
        }

        context.disableScissor();

        if (maxNavScroll > 0) {
            int trackX = navX + navW - 6;
            int trackY = navY + 3;
            int trackW = 4;
            int trackH = navH - 6;
            int thumbH = Math.max(18, (int) ((float) (navH - NAV_PADDING * 2)
                    / getNavigationContentHeight() * trackH));
            int thumbY = trackY + (int) ((float) navScrollOffset / maxNavScroll * (trackH - thumbH));
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF0C0C10);
            context.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFF686878);
            context.fill(trackX, thumbY, trackX + 1, thumbY + thumbH, 0xFFFFAA00);
        }
    }

    private int getNavigationContentHeight() {
        int height = NAV_PADDING * 2;
        for (Section section : sections) {
            height += SECTION_HEIGHT + SECTION_GAP;
            if (!section.collapsed) {
                height += section.pages.size() * PAGE_LINK_HEIGHT;
            }
        }
        return height;
    }

    private void renderPageContent(DrawContext context, TextRenderer tr, int pageX, int pageY,
                                    int pageW, int pageH, int mouseX, int mouseY) {
        // Inset sunken stone well for page content
        ShapeRenderer.drawInsetPanel(context, pageX, pageY, pageW, pageH);

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
        context.drawText(tr, titleText, pageX + CONTENT_PADDING, curY, HEADING_COLOR, false);
        curY += 16;

        // Separator line under title
        context.fill(pageX + CONTENT_PADDING, curY,
                pageX + CONTENT_PADDING + Math.min(maxTextWidth, tr.getWidth(titleText) + 20),
                curY + 1, NAV_BORDER_COLOR);
        curY += 8;

        // Render content elements
        for (ContentElement element : currentPage.elements) {
            switch (element.type) {
                case "heading" -> {
                    curY += 6;
                    context.drawText(tr, Text.literal(element.value),
                            pageX + CONTENT_PADDING, curY, HEADING_COLOR, false);
                    curY += 14;
                }
                case "subheading" -> {
                    curY += 3;
                    context.drawText(tr, Text.literal(element.value),
                            pageX + CONTENT_PADDING + 2, curY, SUBHEADING_COLOR, false);
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

        // 3D Beveled Stone Scrollbar
        if (maxContentScroll > 0) {
            int trackW = 6;
            int trackX = pageX + pageW - trackW - 2;
            int trackY = pageY + 2;
            int trackH = pageH - 4;
            int thumbH = Math.max(22, (int) ((float) pageH / (pageH + maxContentScroll) * trackH));
            int thumbY = trackY + (int) ((float) contentScrollOffset / maxContentScroll * (trackH - thumbH));

            // Inset sunken stone track
            ShapeRenderer.drawInsetPanel(context, trackX, trackY, trackW, trackH, 0xFF101014, 0xFF0A0A0E, 0xFF2A2A32, 0xFF08080C);

            // 3D Beveled Stone Thumb
            ShapeRenderer.drawBeveledPanel(context, trackX, thumbY, trackW, thumbH, 0xFF383842, 0xFF686878, 0xFF18181E, 0xFF101014);

            // Center gold ridge detail
            if (thumbH >= 12) {
                int midY = thumbY + thumbH / 2;
                context.fill(trackX + 2, midY - 1, trackX + trackW - 2, midY, 0xFFFFAA00);
                context.fill(trackX + 2, midY + 1, trackX + trackW - 2, midY + 2, 0xFFFFAA00);
            }
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
                    context.drawText(tr, Text.literal(line.toString()), x, curY, color, false);
                    curY += 10;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (!line.isEmpty()) {
                context.drawText(tr, Text.literal(line.toString()), x, curY, color, false);
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
                context.drawText(tr, Text.literal(groupTitle), x, curY, SUBHEADING_COLOR, false);
                curY += 13;
                lastAffix = stat.getAffixType();
            }

            Integer rgb = stat.getColor().getColorValue();
            context.drawText(tr, Text.literal(stat.getDisplayName()), x + 2, curY,
                    rgb != null ? 0xFF000000 | rgb : CONTENT_TEXT_COLOR, false);
            curY += 10;

            String metadata = "Rune level " + stat.getLevel()
                    + "  |  Weight " + stat.getWeight()
                    + "  |  Affinity " + formatAffinity(stat.getArmorBaseType().name());
            curY = drawWrappedText(context, tr, metadata, x + 8, curY,
                    Math.max(20, maxWidth - 8), 0xFF9E9EAA);

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
                noteColor = 0xFF2A2A10;
                noteBorder = 0xFFAAAA40;
                prefix = "⚠ Warning";
            }
            case "tip" -> {
                noteColor = 0xFF102A10;
                noteBorder = 0xFF40AA40;
                prefix = "💡 Tip";
            }
            case "important" -> {
                noteColor = 0xFF2A1010;
                noteBorder = 0xFFAA4040;
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
        context.drawText(tr, Text.literal(prefix), noteX + 6, y + 4, noteBorder, false);

        // Draw text
        int textY = y + 18;
        line = new StringBuilder();
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (tr.getWidth(test) > noteWidth - 12 && !line.isEmpty()) {
                context.drawText(tr, Text.literal(line.toString()), noteX + 6, textY, NOTE_TEXT_COLOR, false);
                textY += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) {
            context.drawText(tr, Text.literal(line.toString()), noteX + 6, textY, NOTE_TEXT_COLOR, false);
            textY += 10;
        }

        return y + noteHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // Check content scrollbar click & drag start
        if (maxContentScroll > 0) {
            int pageX = boxX + NAV_WIDTH + 6;
            int pageY = boxY + 4;
            int pageW = boxW - NAV_WIDTH - 10;
            int pageH = boxH - 8;
            int trackW = 6;
            int trackX = pageX + pageW - trackW - 2;
            int trackY = pageY + 2;
            int trackH = pageH - 4;
            int thumbH = Math.max(22, (int) ((float) pageH / (pageH + maxContentScroll) * trackH));
            int thumbY = trackY + (int) ((float) contentScrollOffset / maxContentScroll * (trackH - thumbH));

            if (mouseX >= trackX - 4 && mouseX <= trackX + trackW + 4 && mouseY >= trackY && mouseY <= trackY + trackH) {
                isDraggingScrollbar = true;
                if (mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                    dragClickOffsetY = (int) (mouseY - thumbY);
                } else {
                    dragClickOffsetY = thumbH / 2;
                    int availableTrack = trackH - thumbH;
                    if (availableTrack > 0) {
                        float relativeY = (float) (mouseY - trackY - dragClickOffsetY);
                        float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                        contentScrollOffset = Math.round(ratio * maxContentScroll);
                    }
                }
                return true;
            }
        }

        int navX = boxX + 4;
        int navY = boxY + 4;
        int navW = NAV_WIDTH;
        int navH = boxH - 8;

        // Check nav column clicks
        if (mouseX >= navX && mouseX < navX + navW
                && mouseY >= navY && mouseY < navY + navH) {
            int curY = navY + NAV_PADDING - navScrollOffset;

            for (int si = 0; si < sections.size(); si++) {
                Section section = sections.get(si);

                // Section header click → toggle collapse
                if (mouseY >= curY && mouseY < curY + SECTION_HEIGHT) {
                    sections.set(si, section.withCollapsed(!section.collapsed));
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
        if (isDraggingScrollbar && maxContentScroll > 0) {
            int pageY = boxY + 4;
            int pageH = boxH - 8;
            int trackY = pageY + 2;
            int trackH = pageH - 4;
            int thumbH = Math.max(22, (int) ((float) pageH / (pageH + maxContentScroll) * trackH));
            int availableTrack = trackH - thumbH;

            if (availableTrack > 0) {
                float relativeY = (float) (mouseY - trackY - dragClickOffsetY);
                float ratio = Math.max(0.0f, Math.min(1.0f, relativeY / availableTrack));
                contentScrollOffset = Math.round(ratio * maxContentScroll);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int navX = boxX + 4;
        int navY = boxY + 4;
        int navW = NAV_WIDTH;
        int navH = boxH - 8;

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

    // --- Data Loading ---

    private void loadIndex() {
        sections.clear();
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
        try {
            var resource = MinecraftClient.getInstance().getResourceManager()
                    .getResource(Identifier.of("cranks-dungeons", "compendium/pages/" + pageId + ".json"));

            if (resource.isEmpty()) {
                LOGGER.warn("Compendium page '{}' not found", pageId);
                currentPage = new PageContent("Page Not Found",
                        List.of(new ContentElement("text", "This page has not been written yet.", null)));
                return;
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

                currentPage = new PageContent(title, elements);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load compendium page: {}", pageId, e);
            currentPage = new PageContent("Error",
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

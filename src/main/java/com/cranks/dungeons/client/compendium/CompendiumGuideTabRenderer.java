package com.cranks.dungeons.client.compendium;

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
import java.util.List;

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
    private static final int NAV_WIDTH = 130;
    private static final int NAV_PADDING = 6;
    private static final int CONTENT_PADDING = 12;
    private static final int SECTION_HEIGHT = 18;
    private static final int PAGE_LINK_HEIGHT = 14;
    private static final int SECTION_GAP = 4;

    // Colors
    private static final int NAV_BG_COLOR = 0xFF0A0A18;
    private static final int NAV_BORDER_COLOR = 0xFF2A2A4A;
    private static final int CONTENT_BG_COLOR = 0xFF111122;
    private static final int SECTION_HEADER_COLOR = 0xFFCCCCDD;
    private static final int PAGE_LINK_COLOR = 0xFF8888AA;
    private static final int PAGE_LINK_HOVER_COLOR = 0xFFAAAADD;
    private static final int PAGE_LINK_SELECTED_COLOR = 0xFF6060FF;
    private static final int CONTENT_TEXT_COLOR = 0xFFCCCCDD;
    private static final int HEADING_COLOR = 0xFFEEEEFF;
    private static final int NOTE_BG_COLOR = 0xFF1A1A35;
    private static final int NOTE_BORDER_COLOR = 0xFF4040AA;
    private static final int NOTE_TEXT_COLOR = 0xFFAAAADD;
    private static final int ARROW_COLOR = 0xFF888899;

    // State
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;
    private int boxX, boxY, boxW, boxH;
    private final List<Section> sections = new ArrayList<>();
    private String selectedPageId = null;
    private PageContent currentPage = null;
    private int navScrollOffset = 0;
    private int contentScrollOffset = 0;
    private int maxContentScroll = 0;
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
        
        this.boxW = Math.min(600, contentW - 40);
        this.boxH = Math.min(320, contentH - 40);
        this.boxX = contentX + (contentW - boxW) / 2;
        this.boxY = contentY + (contentH - boxH) / 2;

        if (!loaded) {
            loadIndex();
            loaded = true;
            // Auto-select first page
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
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // Draw title floating above the box
        String titleText = "Compendium";
        int titleW = tr.getWidth(titleText);
        context.drawTextWithShadow(tr, titleText, boxX + (boxW - titleW) / 2, boxY - 16, 0xFFFFFFFF);

        // Box Background (semi-transparent)
        context.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xE61A1A2A); // Dark purple-grey
        
        // Box Border
        context.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY, NAV_BORDER_COLOR);
        context.fill(boxX - 1, boxY + boxH, boxX + boxW + 1, boxY + boxH + 1, NAV_BORDER_COLOR);
        context.fill(boxX - 1, boxY, boxX, boxY + boxH, NAV_BORDER_COLOR);
        context.fill(boxX + boxW, boxY, boxX + boxW + 1, boxY + boxH, NAV_BORDER_COLOR);

        // Render navigation column
        int navX = boxX;
        int navW = NAV_WIDTH;
        renderNavigation(context, tr, navX, boxY, navW, boxH, mouseX, mouseY);

        // Nav/Content border
        context.fill(boxX + navW, boxY, boxX + navW + 1, boxY + boxH, NAV_BORDER_COLOR);

        // Render content column
        int pageX = boxX + navW + 1;
        int pageW = boxW - navW - 1;
        renderPageContent(context, tr, pageX, boxY, pageW, boxH, mouseX, mouseY);
    }

    private void renderNavigation(DrawContext context, TextRenderer tr, int navX, int navY,
                                   int navW, int navH, int mouseX, int mouseY) {
        // Nav background
        context.fill(navX, navY, navX + navW, navY + navH, NAV_BG_COLOR);

        context.enableScissor(navX, navY, navX + navW, navY + navH);

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

        context.enableScissor(pageX, pageY, pageX + pageW, pageY + pageH);

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
                case "text" -> {
                    curY = drawWrappedText(context, tr, element.value,
                            pageX + CONTENT_PADDING, curY, maxTextWidth, CONTENT_TEXT_COLOR);
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

        // Scroll indicator
        if (maxContentScroll > 0) {
            int trackH = pageH - 4;
            int thumbH = Math.max(20, (int) ((float) pageH / (pageH + maxContentScroll) * trackH));
            int thumbY = pageY + 2 + (int) ((float) contentScrollOffset / maxContentScroll * (trackH - thumbH));
            context.fill(pageX + pageW - 4, pageY + 2, pageX + pageW - 1, pageY + pageH - 2, 0xFF1A1A2A);
            context.fill(pageX + pageW - 4, thumbY, pageX + pageW - 1, thumbY + thumbH, 0xFF4040AA);
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

        int navX = boxX;
        int navW = NAV_WIDTH;

        // Check nav column clicks
        if (mouseX >= navX && mouseX < navX + navW) {
            int curY = boxY + NAV_PADDING - navScrollOffset;

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
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int navX = boxX;
        int navW = NAV_WIDTH;

        if (mouseX >= navX && mouseX < navX + navW) {
            // Scroll navigation
            navScrollOffset = Math.max(0, navScrollOffset - (int) (verticalAmount * 10));
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
                new PageLink("classes", "Choosing a Class"),
                new PageLink("passive_tree", "The Passive Tree")
        )));
        sections.add(new Section("Combat", false, List.of(
                new PageLink("stats", "Stats & Attributes"),
                new PageLink("runes", "Rune System"),
                new PageLink("ailments", "Ailments")
        )));
        sections.add(new Section("Gathering", true, List.of(
                new PageLink("world_tree", "The World Tree"),
                new PageLink("gathering", "Gathering XP")
        )));
    }
}

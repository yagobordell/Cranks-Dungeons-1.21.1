package com.cranks.dungeons.client.compendium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Canonical wiki directory and reader, sharing content and layout with contextual references.
 */
public class CompendiumGuideTabRenderer implements CompendiumTabRenderer {
    private static final int PADDING = 10;
    private static final int CARD_HEIGHT = 47;
    private static final int GAP = 6;
    private static final int TOPIC_HEIGHT = 26;
    private static final int HEADING_COLOR = CompendiumStyle.TEXT;

    private int x, y, width, height;
    private CompendiumScreen parent;
    private final List<Section> sections = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();
    private List<Topic> results = List.of();
    private TextFieldWidget searchField;
    private String searchQuery = "";
    private Section chapter;
    private PageLink article;
    private int indexScroll, chapterScroll, articleScroll, articleHeight, maxScroll;
    private int dragOffset = -1;
    private boolean loaded;
    private com.cranks.dungeons.client.guide.GuidePageLayout wikiLayout;
    private String wikiLayoutKey="";private int wikiLayoutWidth=-1;private boolean anchorPending;
    /** A return destination includes the directory context, not just the last article. */
    private record Visit(PageLink page,String chapter,String query,int scroll){}
    private final List<Visit> history=new ArrayList<>();private int historyIndex=-1;
    private boolean restoringSearch;

    private int referenceRevision=-1,referenceAlchemyRevision=-1,referenceGeneration=-1;

    private record Section(String title, List<PageLink> pages) {}
    private record PageLink(String id, String title) {}
    private record Topic(Section section, PageLink page) {}
    private record Entry(int x, int y, int width, int height, boolean toolbar, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    @Override
    public void init(CompendiumScreen parent, int contentX, int contentY, int contentW, int contentH) {
        this.parent = parent;
        x = contentX;
        y = contentY;
        width = contentW;
        height = contentH;
        entries.clear();
        dragOffset = -1;
        int revision=MinecraftClient.getInstance().player==null?-1:com.cranks.dungeons.component.ModComponents.ITEM_DISCOVERIES.get(MinecraftClient.getInstance().player).revision();
        int alchemyRevision=MinecraftClient.getInstance().player==null?-1:com.cranks.dungeons.component.ModComponents.ALCHEMY_KNOWLEDGE.get(MinecraftClient.getInstance().player).revision();
        if (!loaded || revision!=referenceRevision || alchemyRevision!=referenceAlchemyRevision || referenceGeneration!=com.cranks.dungeons.client.guide.GuideBook.generation()) {
            loadIndex();
            loaded = true;
        }
        if (chapter != null) chapter = sections.stream().filter(section -> section.title.equals(chapter.title)).findFirst().orElse(null);
        rebuildSearchResults();
        if (historyIndex < 0) pushVisit();
        int searchWidth = Math.min(186, Math.max(52, width - 100));
        searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                x + width - searchWidth - PADDING, y + 13, searchWidth, 12, Text.literal("Search the guide"));
        searchField.setMaxLength(80);
        searchField.setText(searchQuery);
        searchField.setPlaceholder(Text.literal("Search the guide…"));
        searchField.setChangedListener(this::updateSearchQuery);
        searchField.setDrawsBackground(false);
        updateSearchVisibility();
        parent.addTabWidget(searchField);
    }

    @Override
    public void onDeactivated() {
        dragOffset = -1;
    }

    private boolean atIndex() {
        return chapter == null && article == null;
    }

    private void updateSearchVisibility() {
        searchField.visible = atIndex();
        if (!searchField.visible && parent.getFocused() == searchField) parent.setFocused(null);
    }

    private int bodyTop() {
        return y + 36;
    }

    private int bodyBottom() {
        return Math.max(bodyTop(), y + height - 7);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        entries.clear();
        boolean chapterIndex = atIndex() && searchQuery.isBlank();
        CompendiumStyle.drawGuideFrame(context, x, y, width, height, bodyTop());
        drawToolbar(context, tr, mouseX, mouseY);
        if (bodyBottom() <= bodyTop() || width < 45) return;

        context.enableScissor(x + 5, bodyTop(), x + width - 10, bodyBottom());
        try {
            if (article != null) {
                renderArticle(context, tr);
            } else if (chapterIndex) {
                renderChapters(context, tr, mouseX, mouseY);
            } else {
                renderTopics(context, tr, mouseX, mouseY);
            }
        } finally {
            context.disableScissor();
        }
        if (maxScroll > 0) {
            CompendiumStyle.drawGuideScrollbar(context, x + width - 9, bodyTop(),
                    bodyBottom() - bodyTop(), thumbY(), thumbHeight());
        }
    }

    private void drawToolbar(DrawContext context, TextRenderer tr, int mouseX, int mouseY) {
        if (atIndex()) {
            toolbarButton(context,tr,"<",x+12,20,mouseX,mouseY,()->navigateHistory(-1));
            toolbarButton(context,tr,">",x+36,20,mouseX,mouseY,()->navigateHistory(1));
            int searchBox = searchField.getX() - 18;
            context.fill(searchBox, y + 8, x + width - 7, y + 26, CompendiumStyle.DIVIDER);
            context.fill(searchBox + 1, y + 9, x + width - 8, y + 25, CompendiumStyle.RECESSED);
            CompendiumStyle.drawJournalIcon(context, 13, searchBox + 2, y + 9);
            int titleRoom = searchBox - x - 66;
            if (titleRoom >= tr.getWidth("Compendium")) {
                context.drawTextWithShadow(tr, "Compendium", x + 62, y + 13, CompendiumStyle.TEXT);
            } else if (titleRoom >= 16) {
                CompendiumStyle.drawJournalIcon(context, 0, x + 62, y + 8);
            }
            return;
        }

        if(article!=null){
            toolbarButton(context,tr,"<",x+12,20,mouseX,mouseY,()->navigateHistory(-1));
            toolbarButton(context,tr,">",x+36,20,mouseX,mouseY,()->navigateHistory(1));
            toolbarButton(context,tr,"Index",x+60,44,mouseX,mouseY,this::openIndex);
            drawLabel(context,tr,article.title,x+113,y+13,width-128,CompendiumStyle.TEXT);return;
        }
        toolbarButton(context,tr,"<",x+12,20,mouseX,mouseY,()->navigateHistory(-1));
        toolbarButton(context,tr,">",x+36,20,mouseX,mouseY,()->navigateHistory(1));
        toolbarButton(context,tr,"Index",x+60,44,mouseX,mouseY,this::openIndex);
        if(chapter!=null)drawLabel(context,tr,chapter.title,x+113,y+13,width-128,CompendiumStyle.TEXT);
    }

    private void toolbarButton(DrawContext context, TextRenderer tr, String label, int buttonX, int buttonW,
                               int mouseX, int mouseY, Runnable action) {
        Entry entry = new Entry(buttonX, y + 8, buttonW, 17, true, action);
        entries.add(entry);
        CompendiumStyle.drawToolbarControl(context, entry.x, entry.y, entry.width, entry.height,
                entry.contains(mouseX, mouseY));
        context.drawCenteredTextWithShadow(tr, label, buttonX + buttonW / 2, y + 13, CompendiumStyle.TEXT);
    }

    private void renderChapters(DrawContext context, TextRenderer tr, int mouseX, int mouseY) {
        int available = width - PADDING * 2 - 7;
        int columns = Math.clamp((available + GAP) / 123, 1, 3);
        int cardWidth = (available - (columns - 1) * GAP) / columns;
        int rows = (sections.size() + columns - 1) / columns;
        int scroll = fitScroll(rows * (CARD_HEIGHT + GAP) + GAP);
        for (int index = 0; index < sections.size(); index++) {
            Section section = sections.get(index);
            int cardX = x + PADDING + index % columns * (cardWidth + GAP);
            int cardY = bodyTop() + GAP + index / columns * (CARD_HEIGHT + GAP) - scroll;
            if (cardY + CARD_HEIGHT <= bodyTop() || cardY >= bodyBottom()) continue;
            Entry entry = new Entry(cardX, cardY, cardWidth, CARD_HEIGHT, false, () -> openChapter(section));
            entries.add(entry);
            int icon = chapterIcon(section, index);
            CompendiumStyle.drawChapterCard(context, cardX, cardY, cardWidth, CARD_HEIGHT, icon,
                    inBody(mouseY) && entry.contains(mouseX, mouseY));
            int labelWidth = cardWidth - 38;
            var lines = tr.wrapLines(Text.literal(section.title), Math.max(1, labelWidth));
            for (int line = 0; line < Math.min(2, lines.size()); line++) {
                context.drawText(tr, lines.get(line), cardX + 33, cardY + 9 + line * 9, CompendiumStyle.TEXT, true);
            }
            int count = section.pages.size();
            drawLabel(context, tr, count + (count == 1 ? " topic" : " topics"), cardX + 33, cardY + 31,
                    labelWidth, CompendiumStyle.TEXT_MUTED);
        }
    }

    /** Pick a motif by the chapter's first topic so resource packs can reorder chapters. */
    private static int chapterIcon(Section section, int fallback) {
        if (section.pages.isEmpty()) return 0;
        return switch (section.pages.getFirst().id) {
            case "introduction" -> 3;
            case "classes" -> 4;
            case "stats" -> 5;
            case "equipment" -> 6;
            case "runic_altar" -> 7;
            case "rune_sword" -> 8;
            case "rune_helmet" -> 9;
            case "world_tree" -> 10;
            case "mounts" -> 11;
            case "vanilla_changes" -> 12;
            default -> 3 + fallback % 10;
        };
    }

    private void renderTopics(DrawContext context, TextRenderer tr, int mouseX, int mouseY) {
        List<Topic> topics = chapter == null ? results : chapter.pages.stream().map(page -> new Topic(chapter, page)).toList();
        int scroll = fitScroll(topics.size() * TOPIC_HEIGHT + GAP * 2);
        if (topics.isEmpty()) {
            context.drawText(tr, chapter == null ? "No matching topics" : "No topics in this chapter",
                    x + PADDING, bodyTop() + 10, HEADING_COLOR, false);
            return;
        }
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            int rowY = bodyTop() + GAP + i * TOPIC_HEIGHT - scroll;
            if (rowY + TOPIC_HEIGHT <= bodyTop() || rowY >= bodyBottom()) continue;
            Entry entry = new Entry(x + PADDING, rowY, width - PADDING * 2 - 6, TOPIC_HEIGHT - 3,
                    false, () -> openArticle(topic.page));
            entries.add(entry);
            CompendiumStyle.drawTopicStrip(context, entry.x, entry.y, entry.width, entry.height,
                    inBody(mouseY) && entry.contains(mouseX, mouseY));
            CompendiumStyle.drawJournalIcon(context, chapterIcon(topic.section, 0), entry.x + 4, entry.y + 3);
            drawLabel(context, tr, topic.page.title, entry.x + 25, entry.y + (chapter == null ? 3 : 7),
                    entry.width - 38, HEADING_COLOR);
            if (chapter == null) {
                drawLabel(context, tr, topic.section.title, entry.x + 25, entry.y + 13,
                        entry.width - 38, CompendiumStyle.TEXT_MUTED);
            }
        }
    }

    private static void drawLabel(DrawContext context, TextRenderer tr, String label,
                                  int x, int y, int room, int color) {
        if (room <= 0) return;
        int ellipsisWidth = tr.getWidth("…");
        if (room < ellipsisWidth) return;
        String text = tr.getWidth(label) <= room ? label : tr.trimToWidth(label, room - ellipsisWidth) + "…";
        context.drawText(tr, text, x, y, color, false);
    }

    private com.cranks.dungeons.client.guide.GuideTarget target(PageLink link){
        return link.id.startsWith("@")?com.cranks.dungeons.client.guide.GuideTarget.parse(link.id.substring(1)).orElseThrow():com.cranks.dungeons.client.guide.GuideTarget.article(link.id);
    }
    private void renderArticle(DrawContext context,TextRenderer tr){
        int textWidth=Math.max(70,Math.min(530,width-34)),textX=x+(width-6-textWidth)/2;
        var target=target(article);
        if(wikiLayout==null||!wikiLayoutKey.equals(article.id)||wikiLayoutWidth!=textWidth){
            wikiLayout=com.cranks.dungeons.client.guide.GuidePageLayout.build(com.cranks.dungeons.client.guide.GuideBook.load(target),target,textWidth);
            wikiLayoutKey=article.id;wikiLayoutWidth=textWidth;
        }
        articleHeight=wikiLayout.height+16;fitScroll(articleHeight);
        if(anchorPending){String anchor=target.key().contains("#")?target.key().split("#",2)[1]:"";setScroll(wikiLayout.anchorY(anchor));anchorPending=false;}
        wikiLayout.draw(context,textX,bodyTop()+8-articleScroll,bodyTop(),bodyBottom(),null);
    }
    public void openReference(com.cranks.dungeons.client.guide.GuideTarget requested){
        var resolved=com.cranks.dungeons.client.guide.GuideTopics.resolve(requested);
        openArticle(new PageLink("@"+resolved.encode(),com.cranks.dungeons.client.guide.GuideTopics.title(resolved,com.cranks.dungeons.client.guide.GuideBook.load(resolved).title())));
    }
    private void rememberVisit(){
        if(historyIndex>=0)history.set(historyIndex,currentVisit());
    }
    private Visit currentVisit(){
        return new Visit(article,chapter==null?null:chapter.title,searchQuery,scrollOffset());
    }
    private void pushVisit(){
        while(history.size()>historyIndex+1)history.removeLast();
        history.add(currentVisit());historyIndex=history.size()-1;
    }
    private void openChapter(Section section){
        rememberVisit();article=null;chapter=section;chapterScroll=0;pushVisit();changeView();
    }
    private void openIndex(){
        if(atIndex())return;
        rememberVisit();article=null;chapter=null;pushVisit();changeView();
    }
    private void openArticle(PageLink link){
        rememberVisit();
        var resolved=com.cranks.dungeons.client.guide.GuideTopics.resolve(target(link));
        article=new PageLink("@"+resolved.encode(),com.cranks.dungeons.client.guide.GuideBook.load(resolved).title());
        chapter=null;articleScroll=0;articleHeight=0;anchorPending=true;pushVisit();changeView();
    }
    private void navigateHistory(int step){
        rememberVisit();
        int next=historyIndex+step;if(next<0||next>=history.size())return;historyIndex=next;
        var visit=history.get(next);article=visit.page;
        chapter=visit.chapter==null?null:sections.stream().filter(section->section.title.equals(visit.chapter)).findFirst().orElse(null);
        searchQuery=visit.query;restoringSearch=true;
        try{searchField.setText(searchQuery);}finally{restoringSearch=false;}
        rebuildSearchResults();
        if(article!=null)articleScroll=visit.scroll;else if(chapter!=null)chapterScroll=visit.scroll;else indexScroll=visit.scroll;
        anchorPending=false;changeView();
    }

    private void changeView() {
        dragOffset = -1;
        entries.clear();
        maxScroll = 0;
        updateSearchVisibility();
    }

    private int scrollOffset() {
        if (article != null) return articleScroll;
        return chapter != null ? chapterScroll : indexScroll;
    }

    private void setScroll(int offset) {
        int scroll = Math.clamp(offset, 0, maxScroll);
        if (article != null) articleScroll = scroll;
        else if (chapter != null) chapterScroll = scroll;
        else indexScroll = scroll;
    }

    private int fitScroll(int contentHeight) {
        maxScroll = Math.max(0, contentHeight - (bodyBottom() - bodyTop()));
        setScroll(scrollOffset());
        return scrollOffset();
    }

    private boolean inBody(double mouseY) {
        return mouseY >= bodyTop() && mouseY < bodyBottom();
    }

    private int thumbHeight() {
        int viewHeight = bodyBottom() - bodyTop();
        return Math.min(viewHeight, Math.max(12, viewHeight * viewHeight / (viewHeight + maxScroll)));
    }

    private int thumbY() {
        return bodyTop() + (int) Math.round((double) scrollOffset() / maxScroll
                * (bodyBottom() - bodyTop() - thumbHeight()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (maxScroll > 0 && inBody(mouseY) && mouseX >= x + width - 10 && mouseX < x + width - 3) {
            dragOffset = mouseY >= thumbY() && mouseY < thumbY() + thumbHeight()
                    ? (int) mouseY - thumbY() : thumbHeight() / 2;
            dragScrollbar(mouseY);
            return true;
        }
        for (Entry entry : entries) {
            if ((entry.toolbar || inBody(mouseY)) && entry.contains(mouseX, mouseY)) {
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                entry.action.run();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0 || dragOffset < 0) return false;
        dragScrollbar(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || dragOffset < 0) return false;
        dragOffset = -1;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!inBody(mouseY) || maxScroll == 0 || verticalAmount == 0) return false;
        setScroll(scrollOffset() - (int) (verticalAmount * 26));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode==GLFW.GLFW_KEY_F&&(modifiers&GLFW.GLFW_MOD_CONTROL)!=0){openIndex();parent.setFocused(searchField);searchField.setFocused(true);return true;}
        if((modifiers&GLFW.GLFW_MOD_ALT)!=0&&(keyCode==GLFW.GLFW_KEY_LEFT||keyCode==GLFW.GLFW_KEY_RIGHT)){navigateHistory(keyCode==GLFW.GLFW_KEY_LEFT?-1:1);return true;}

        if (maxScroll == 0) return false;
        int page = Math.max(1, bodyBottom() - bodyTop() - 10);
        int target = switch (keyCode) {
            case GLFW.GLFW_KEY_PAGE_UP -> scrollOffset() - page;
            case GLFW.GLFW_KEY_PAGE_DOWN -> scrollOffset() + page;
            case GLFW.GLFW_KEY_HOME -> 0;
            case GLFW.GLFW_KEY_END -> maxScroll;
            default -> Integer.MIN_VALUE;
        };
        if (target == Integer.MIN_VALUE) return false;
        setScroll(target);
        return true;
    }

    private void dragScrollbar(double mouseY) {
        int travel = bodyBottom() - bodyTop() - thumbHeight();
        if (travel > 0) setScroll((int) Math.round((mouseY - bodyTop() - dragOffset) * maxScroll / travel));
    }

    private void updateSearchQuery(String value) {
        if(restoringSearch)return;
        if(value.equals(searchQuery))return;
        searchQuery = value;
        rebuildSearchResults();
        indexScroll = 0;
        // Editing a query updates this destination instead of making every keystroke a Back step.
        while(history.size()>historyIndex+1)history.removeLast();
        rememberVisit();
        changeView();
    }

    private void rebuildSearchResults() {
        String query = searchQuery.trim();
        List<Topic> matches = new ArrayList<>();
        if (!query.isEmpty()) {
            for (Section section : sections) {
                boolean sectionMatches = textContains(section.title, query);
                for (PageLink page : section.pages) {
                    if (sectionMatches || pageMatches(page, query)) matches.add(new Topic(section, page));
                }
            }
        }
        matches.sort(Comparator.comparingInt((Topic topic)->com.cranks.dungeons.client.guide.GuideSearch.rank(new com.cranks.dungeons.client.guide.GuideBook.Entry(target(topic.page),topic.page.title,topic.section.title),query)).thenComparing(t->t.page.title,String.CASE_INSENSITIVE_ORDER));
        results = List.copyOf(matches);
    }

    private boolean pageMatches(PageLink page, String query) {
        return textContains(page.title,query)||textContains(com.cranks.dungeons.client.guide.GuideSearch.text(target(page)),query);
    }

    private boolean textContains(String value, String query) {
        return value != null && query != null
                && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    // --- Data Loading ---

    private void loadIndex(){
        sections.clear();wikiLayout=null;
        var groups=new java.util.LinkedHashMap<String,List<PageLink>>();
        for(var entry:com.cranks.dungeons.client.guide.GuideBook.index())groups.computeIfAbsent(entry.category(),unused->new ArrayList<>()).add(new PageLink("@"+entry.target().encode(),entry.title()));
        groups.forEach((title,pages)->{pages.sort(Comparator.comparing(PageLink::title,String.CASE_INSENSITIVE_ORDER));sections.add(new Section(title,List.copyOf(pages)));});
        referenceRevision=MinecraftClient.getInstance().player==null?-1:com.cranks.dungeons.component.ModComponents.ITEM_DISCOVERIES.get(MinecraftClient.getInstance().player).revision();
        referenceAlchemyRevision=MinecraftClient.getInstance().player==null?-1:com.cranks.dungeons.component.ModComponents.ALCHEMY_KNOWLEDGE.get(MinecraftClient.getInstance().player).revision();
        referenceGeneration=com.cranks.dungeons.client.guide.GuideBook.generation();
    }
}

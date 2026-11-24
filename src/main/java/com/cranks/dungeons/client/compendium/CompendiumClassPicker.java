package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

/** Class roster, dossier, and confirmation for players choosing their starting path. */
final class CompendiumClassPicker {
    private static final int ROW_HEIGHT = 30;

    private CompendiumScreen parent;
    private List<ClassDefinition> classes = List.of();
    private int x, y, width, height, rosterWidth;
    private int firstClass, detailScroll, maxDetailScroll;
    private ClassDefinition selected;
    private boolean mapPreview, confirming;
    private CompendiumButtonWidget cancelButton, confirmButton;

    void init(CompendiumScreen parent, int x, int y, int width, int height, List<ClassDefinition> classes,
              Consumer<ClassDefinition> preview, Consumer<ClassDefinition> select) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.classes = classes;
        rosterWidth = Math.min(142, Math.max(84, width / 3));
        if (selected != null) {
            String id = selected.id();
            selected = classes.stream().filter(def -> def.id().equals(id)).findFirst().orElse(null);
        }
        if (selected == null) {
            mapPreview = false;
            confirming = false;
        }

        if (mapPreview) {
            parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("< Classes"), button -> {
                mapPreview = false;
                parent.refreshTab();
            }).toolbar().dimensions(x + 12, y + 9, 70, 17).build());
            return;
        }
        int visible = visibleClasses();
        firstClass = Math.clamp(firstClass, 0, Math.max(0, classes.size() - visible));
        for (int i = firstClass; i < Math.min(classes.size(), firstClass + visible); i++) {
            var row = new ClassButton(classes.get(i), x + 8, rosterTop() + (i - firstClass) * ROW_HEIGHT);
            row.active = !confirming;
            parent.addTabWidget(row);
        }
        if (classes.size() > visible) {
            int arrows = y + height - 27;
            var up = button("^", x + 8, arrows, 24, () -> scrollRoster(-1));
            var down = button("v", x + 36, arrows, 24, () -> scrollRoster(1));
            up.active = !confirming && firstClass > 0;
            down.active = !confirming && firstClass + visible < classes.size();
        }
        if (selected != null) {
            int left = detailX() + 9;
            int available = x + width - 9 - left;
            int split = Math.max(1, (available - 5) / 2);
            int bottom = y + height - 28;
            button("Preview", left, bottom, split, () -> {
                mapPreview = true;
                preview.accept(selected);
                parent.refreshTab();
            });
            button("Choose", left + split + 5, bottom, available - split - 5, () -> {
                confirming = true;
                parent.refreshTab();
            });
        }
        if (confirming) {
            int left = modalX() + 10;
            int buttonW = (modalWidth() - 26) / 2;
            int buttonsY = modalY() + 71;
            cancelButton = parent.addOverlayWidget(createButton("Cancel", left, buttonsY, buttonW, () -> {
                confirming = false;
                parent.refreshTab();
            }));
            confirmButton = parent.addOverlayWidget(createButton("Confirm Path",
                    left + buttonW + 6, buttonsY, buttonW, () -> select.accept(selected)));
        }
    }

    private CompendiumButtonWidget button(String label, int x, int y, int width, Runnable action) {
        var button = createButton(label, x, y, width, action);
        button.active = !confirming;
        return parent.addTabWidget(button);
    }

    private static CompendiumButtonWidget createButton(String label, int x, int y, int width, Runnable action) {
        return CompendiumButtonWidget.builder(Text.literal(label), widget -> action.run())
                .dimensions(x, y, Math.max(1, width), 19).build();
    }

    boolean isPreviewing() {
        return mapPreview;
    }

    ClassDefinition previewClass() {
        return mapPreview ? selected : null;
    }

    void render(DrawContext context) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        if (mapPreview) {
            int titleWidth = Math.min(width - 104, font.getWidth(selected.name()) + 18);
            if (titleWidth > 10) {
                CompendiumStyle.drawRailPlaque(context, x + width - titleWidth - 9, y + 9, titleWidth, 19, true, false);
                context.drawCenteredTextWithShadow(font, font.trimToWidth(selected.name(), titleWidth - 8),
                        x + width - 9 - titleWidth / 2, y + 14, CompendiumStyle.TEXT);
            }
            return;
        }

        CompendiumStyle.drawGuideFrame(context, x, y, width, height, y + 32);
        CompendiumStyle.drawJournalIcon(context, 4, x + 8, y + 8);
        context.drawTextWithShadow(font, "Choose a Class", x + 30, y + 12, CompendiumStyle.TEXT);

        int detailWidth = detailWidth();
        CompendiumStyle.drawInsetPanel(context, detailX(), rosterTop(), detailWidth, y + height - rosterTop() - 7);
        renderDossier(context, font, detailWidth);
    }

    void renderConfirmation(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!confirming) return;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        var matrices = context.getMatrices();
        matrices.push();
        try {
            matrices.translate(0, 0, 10);
            context.fill(x, y, x + width, y + height, 0x68000000);
            matrices.translate(0, 0, 1);
            CompendiumStyle.drawGuideFrame(context, modalX(), modalY(), modalWidth(), 99, modalY() + 31);
            context.drawCenteredTextWithShadow(font, font.trimToWidth("Choose " + selected.name() + "?", modalWidth() - 16),
                    modalX() + modalWidth() / 2, modalY() + 12, CompendiumStyle.TEXT);
            paragraph(context, font, Text.literal("This choice is permanent."),
                    modalX() + 12, modalY() + 44, modalWidth() - 24, CompendiumStyle.TEXT);
            cancelButton.render(context, mouseX, mouseY, delta);
            confirmButton.render(context, mouseX, mouseY, delta);
        } finally {
            matrices.pop();
        }
    }

    private void renderDossier(DrawContext context, TextRenderer font, int detailWidth) {
        int top = rosterTop() + 6;
        int bottom = y + height - (selected == null ? 10 : 34);
        if (bottom <= top || detailWidth < 24) return;
        int textX = detailX() + 9;
        int textWidth = detailWidth - 22;
        context.enableScissor(detailX() + 3, top, detailX() + detailWidth - 6, bottom);
        try {
            int lineY = top + 4 - detailScroll;
            if (classes.isEmpty()) {
                lineY = paragraph(context, font, Text.literal("No classes are available."), textX, lineY, textWidth, CompendiumStyle.TEXT);
            } else if (selected == null) {
                lineY = paragraph(context, font, Text.literal("Select a class to see its starting attributes and preview its place on the Passive Tree."),
                        textX, lineY, textWidth, CompendiumStyle.TEXT);
            } else {
                CompendiumStyle.drawJournalIcon(context, icon(selected), textX, lineY - 3);
                lineY = paragraph(context, font, Text.literal(selected.name()).styled(s -> s.withBold(true)),
                        textX + 21, lineY, textWidth - 21, CompendiumStyle.TEXT);
                lineY = paragraph(context, font, Text.literal(selected.subtitle()), textX, lineY + 6, textWidth, CompendiumStyle.TEXT_MUTED);
                lineY = paragraph(context, font, Text.literal(selected.description()), textX, lineY + 4, textWidth, CompendiumStyle.TEXT);
                lineY = paragraph(context, font, Text.literal("Starting attributes").styled(s -> s.withBold(true)),
                        textX, lineY + 7, textWidth, CompendiumStyle.TEXT);
                for (String stat : List.of("strength", "dexterity", "intelligence")) {
                    double value = selected.baseAttributes().getOrDefault("cranks-dungeons:" + stat, 0.0);
                    String label = Character.toUpperCase(stat.charAt(0)) + stat.substring(1);
                    String number = value == (long) value ? Long.toString((long) value) : Double.toString(value);
                    lineY = paragraph(context, font, Text.literal(label + "  " + number), textX, lineY, textWidth, CompendiumStyle.TEXT);
                }
                lineY = paragraph(context, font, Text.literal("Recommended stats").styled(s -> s.withBold(true)),
                        textX, lineY + 8, textWidth, CompendiumStyle.TEXT);
                lineY = paragraph(context, font, Text.literal(selected.recommendedStats()), textX, lineY, textWidth, CompendiumStyle.TEXT);
            }
            maxDetailScroll = Math.max(0, lineY + detailScroll + 4 - bottom);
            detailScroll = Math.clamp(detailScroll, 0, maxDetailScroll);
        } finally {
            context.disableScissor();
        }
        if (maxDetailScroll > 0) {
            int viewport = bottom - top;
            int thumb = Math.min(viewport, Math.max(12, viewport * viewport / (viewport + maxDetailScroll)));
            int thumbY = top + (int) ((double) detailScroll / maxDetailScroll * (viewport - thumb));
            CompendiumStyle.drawGuideScrollbar(context, detailX() + detailWidth - 6, top, viewport, thumbY, thumb);
        }
    }

    private static int paragraph(DrawContext context, TextRenderer font, Text text, int x, int y, int width, int color) {
        for (var line : font.wrapLines(text, Math.max(1, width))) {
            context.drawText(font, line, x, y, color, false);
            y += 11;
        }
        return y;
    }

    boolean scroll(double mouseX, double amount) {
        if (confirming || amount == 0) return false;
        if (mouseX < detailX()) {
            scrollRoster(amount > 0 ? -1 : 1);
        } else {
            detailScroll = Math.clamp(detailScroll - (int) (amount * 22), 0, maxDetailScroll);
        }
        return true;
    }

    boolean keyPressed(int key) {
        if (!confirming || key != GLFW.GLFW_KEY_ESCAPE) return false;
        confirming = false;
        parent.refreshTab();
        return true;
    }

    private void scrollRoster(int delta) {
        int next = Math.clamp(firstClass + delta, 0, Math.max(0, classes.size() - visibleClasses()));
        if (next != firstClass) {
            firstClass = next;
            parent.refreshTab();
        }
    }

    private int visibleClasses() {
        int allRows = Math.max(1, (height - 48) / ROW_HEIGHT);
        return classes.size() <= allRows ? allRows : Math.max(1, (height - 69) / ROW_HEIGHT);
    }

    private int rosterTop() { return y + 39; }
    private int detailX() { return x + rosterWidth + 4; }
    private int detailWidth() { return x + width - detailX() - 6; }
    private int modalWidth() { return Math.min(Math.min(214, width - 10), Math.max(132, detailWidth() - 8)); }
    private int modalX() {
        return detailWidth() >= modalWidth()
                ? detailX() + (detailWidth() - modalWidth()) / 2
                : x + (width - modalWidth()) / 2;
    }
    private int modalY() { return y + (height - 99) / 2; }

    private static int icon(ClassDefinition def) {
        return switch (def.id()) {
            case "warrior" -> 9;
            case "ranger" -> 15;
            case "mercenary" -> 5;
            case "monk" -> 1;
            case "witch" -> 16;
            case "death_knight" -> 17;
            default -> 4;
        };
    }

    private final class ClassButton extends ClickableWidget {
        private final ClassDefinition def;

        ClassButton(ClassDefinition def, int x, int y) {
            super(x, y, rosterWidth - 10, ROW_HEIGHT - 4, Text.literal(def.name()));
            this.def = def;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            selected = def;
            detailScroll = 0;
            parent.refreshTab();
        }

        @Override
        public boolean keyPressed(int key, int scanCode, int modifiers) {
            if (!active || !KeyCodes.isToggle(key)) return false;
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            onClick(0, 0);
            return true;
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            boolean current = selected != null && def.id().equals(selected.id());
            CompendiumStyle.drawRailPlaque(context, getX(), getY(), getWidth(), getHeight(), current,
                    active && (isHovered() || isFocused()));
            CompendiumStyle.drawJournalIcon(context, icon(def), getX() + 5, getY() + 5);
            context.drawTextWithShadow(font, font.trimToWidth(def.name(), getWidth() - 29),
                    getX() + 25, getY() + 9, current ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_MUTED);
        }
    }
}

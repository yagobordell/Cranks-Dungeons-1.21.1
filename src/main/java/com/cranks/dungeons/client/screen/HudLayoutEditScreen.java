package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.party.PartyHudRenderer;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
import com.cranks.dungeons.client.render.StatusEffectHudRenderer;
import com.cranks.dungeons.config.StatusEffectOrientation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HudLayoutEditScreen extends Screen {

    private final Screen parent;
    private final List<DraggableElement> elements = new ArrayList<>();
    private DraggableElement draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean snapToGrid = true;

    public static class DraggableElement {
        public final String id;
        public final Text displayName;
        public int x;
        public int y;
        public int width;
        public int height;
        public final int defaultX;
        public final int defaultY;
        public boolean isCustom;
        public final Consumer<DrawContext> previewRenderer;
        public final BiConsumer<Integer, Integer> positionSaver;
        public final Runnable positionResetter;

        public DraggableElement(String id, Text displayName, int x, int y, int width, int height,
                                int defaultX, int defaultY, boolean isCustom,
                                Consumer<DrawContext> previewRenderer,
                                BiConsumer<Integer, Integer> positionSaver,
                                Runnable positionResetter) {
            this.id = id;
            this.displayName = displayName;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
            this.isCustom = isCustom;
            this.previewRenderer = previewRenderer;
            this.positionSaver = positionSaver;
            this.positionResetter = positionResetter;
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void reset() {
            this.x = defaultX;
            this.y = defaultY;
            this.isCustom = false;
            this.positionResetter.run();
        }
    }

    public HudLayoutEditScreen(Screen parent) {
        super(Text.literal("HUD Layout Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default 1.21 screen Gaussian blur so HUD layout and game world remain crystal clear
    }

    @Override
    protected void init() {
        this.clearChildren();
        super.init();

        initDraggableElements();

        int btnHeight = 20;
        int tab1W = 95;
        int tab2W = 90;
        int snapW = 75;
        int resetW = 70;
        int doneW = 60;
        int spacing = 4;
        int totalW = tab1W + tab2W + snapW + resetW + doneW + (spacing * 4);
        int startX = (this.width - totalW) / 2;
        int topBarY = 4;

        // Tab 1: Mod Settings
        this.addDrawableChild(ButtonWidget.builder(Text.literal("⚙ Settings").formatted(Formatting.GRAY), b -> {
            saveChanges();
            if (this.client != null) {
                this.client.setScreen(new CranksConfigScreen(this.parent));
            }
        }).dimensions(startX, topBarY, tab1W, btnHeight).build());

        // Tab 2: HUD Layout (Active tab)
        ButtonWidget editorTab = ButtonWidget.builder(Text.literal("📐 HUD Layout").formatted(Formatting.GOLD, Formatting.BOLD), b -> {})
                .dimensions(startX + tab1W + spacing, topBarY, tab2W, btnHeight).build();
        editorTab.active = false;
        this.addDrawableChild(editorTab);

        // Snap toggle
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🧲 Snap: " + (snapToGrid ? "ON" : "OFF")),
                b -> {
                    snapToGrid = !snapToGrid;
                    b.setMessage(Text.literal("🧲 Snap: " + (snapToGrid ? "ON" : "OFF")));
                }
        ).dimensions(startX + tab1W + tab2W + (spacing * 2), topBarY, snapW, btnHeight).build());

        // Reset All Positions
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("↺ Reset"),
                b -> resetAllPositions()
        ).dimensions(startX + tab1W + tab2W + snapW + (spacing * 3), topBarY, resetW, btnHeight).build());

        // Done / Save
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💾 Done").formatted(Formatting.GREEN),
                b -> {
                    saveChanges();
                    this.close();
                }
        ).dimensions(startX + tab1W + tab2W + snapW + resetW + (spacing * 4), topBarY, doneW, btnHeight).build());
    }

    private void initDraggableElements() {
        elements.clear();
        var config = CranksDungeons.CONFIG;
        MinecraftClient mc = MinecraftClient.getInstance();

        // 1. Party HUD
        int partyMaxRows = config.partyHudMaxRows();
        int partyMaxCols = config.partyHudMaxColumns();
        float partyScale = config.partyHudSize();
        int partyW = (int) (partyMaxCols * (PartyHudRenderer.BASE_MEMBER_WIDTH + 5) * partyScale);
        int partyH = (int) (partyMaxRows * (PartyHudRenderer.BASE_MEMBER_HEIGHT + 2) * partyScale);
        int defaultPartyX = 5;
        int defaultPartyY = 190;
        int partyX = config.partyHudX() >= 0 ? config.partyHudX() : defaultPartyX;
        int partyY = config.partyHudY() >= 0 ? config.partyHudY() : defaultPartyY;
        elements.add(new DraggableElement(
                "party_hud",
                Text.literal("Party HUD (Max " + (partyMaxRows * partyMaxCols) + ")"),
                partyX, partyY, partyW, partyH,
                defaultPartyX, defaultPartyY,
                config.partyHudX() >= 0 || config.partyHudY() >= 0,
                ctx -> PartyHudRenderer.renderPreview(ctx, mc, 0, 0, partyMaxRows, partyMaxCols, partyScale),
                (nx, ny) -> {
                    config.partyHudX(nx);
                    config.partyHudY(ny);
                },
                () -> {
                    config.partyHudX(-1);
                    config.partyHudY(-1);
                }
        ));

        // 2. Status Effects HUD
        StatusEffectOrientation statusOrientation = config.statusEffectsOrientation();
        float statusScale = config.statusEffectsScale();
        int statusMaxPerLine = config.statusEffectsMaxPerLine();
        int rawSpacing = config.statusEffectsSpacing();
        int statusSpacing = (int) (rawSpacing * statusScale);
        int statusBaseIcon = (int) (24 * statusScale);
        boolean statusDuration = config.statusEffectsShowDuration();
        boolean statusAmp = config.statusEffectsShowAmplifier();

        int statusW, statusH;
        int durationExtra = statusDuration ? (int) (9 * statusScale) : 0;
        if (statusOrientation.isHorizontal()) {
            statusW = statusMaxPerLine * statusSpacing;
            statusH = statusBaseIcon + durationExtra;
        } else {
            statusW = statusBaseIcon;
            statusH = statusMaxPerLine * (statusSpacing + durationExtra);
        }

        int defaultStatusX = (statusOrientation == StatusEffectOrientation.LEFT_TO_RIGHT) ? 5 : (this.width - statusW - 5);
        int defaultStatusY = (statusOrientation == StatusEffectOrientation.BOTTOM_TO_TOP) ? (this.height - statusH - 60) : 1;
        int statusX = config.statusEffectsX() >= 0 ? config.statusEffectsX() : defaultStatusX;
        int statusY = config.statusEffectsY() >= 0 ? config.statusEffectsY() : defaultStatusY;

        String directionName = switch (statusOrientation) {
            case LEFT_TO_RIGHT -> "Left to Right";
            case RIGHT_TO_LEFT -> "Right to Left";
            case TOP_TO_BOTTOM -> "Top to Bottom";
            case BOTTOM_TO_TOP -> "Bottom to Top";
        };

        elements.add(new DraggableElement(
                "status_effects",
                Text.literal("Status Effects (" + directionName + ")"),
                statusX, statusY, statusW, statusH,
                defaultStatusX, defaultStatusY,
                config.statusEffectsX() >= 0 || config.statusEffectsY() >= 0,
                ctx -> StatusEffectHudRenderer.renderPreview(ctx, mc, 0, 0, statusOrientation, statusMaxPerLine, statusScale, rawSpacing, statusDuration, statusAmp),
                (nx, ny) -> {
                    config.statusEffectsX(nx);
                    config.statusEffectsY(ny);
                },
                () -> {
                    config.statusEffectsX(-1);
                    config.statusEffectsY(-1);
                }
        ));

        // 3. Health Bar
        int defaultHealthX = this.width / 2 - 91;
        int defaultHealthY = this.height - 39;
        int healthX = config.healthBarX() >= 0 ? config.healthBarX() : defaultHealthX;
        int healthY = config.healthBarY() >= 0 ? config.healthBarY() : defaultHealthY;
        elements.add(new DraggableElement(
                "health_bar",
                Text.literal("Health Bar"),
                healthX, healthY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultHealthX, defaultHealthY,
                config.healthBarX() >= 0 || config.healthBarY() >= 0,
                ctx -> StatBarHudRenderer.renderHealthBarDirect(ctx, mc, null, 0, 0, 100, 100, 0, 100),
                (nx, ny) -> {
                    config.healthBarX(nx);
                    config.healthBarY(ny);
                },
                () -> {
                    config.healthBarX(-1);
                    config.healthBarY(-1);
                }
        ));

        // 4. Shield Bar
        int defaultShieldX = healthX;
        int defaultShieldY = healthY - 7;
        int shieldX = config.shieldBarX() >= 0 ? config.shieldBarX() : defaultShieldX;
        int shieldY = config.shieldBarY() >= 0 ? config.shieldBarY() : defaultShieldY;
        elements.add(new DraggableElement(
                "shield_bar",
                Text.literal("Energy Shield Bar"),
                shieldX, shieldY, StatBarHudRenderer.BAR_WIDTH, 6,
                defaultShieldX, defaultShieldY,
                config.shieldBarX() >= 0 || config.shieldBarY() >= 0,
                ctx -> StatBarHudRenderer.renderShieldBarDirect(ctx, mc, 0, 0, 50, 50, 50),
                (nx, ny) -> {
                    config.shieldBarX(nx);
                    config.shieldBarY(ny);
                },
                () -> {
                    config.shieldBarX(-1);
                    config.shieldBarY(-1);
                }
        ));

        // 5. Mana Bar
        int defaultManaX = this.width / 2 + 10;
        int defaultManaY = this.height - 49;
        int manaX = config.manaBarX() >= 0 ? config.manaBarX() : defaultManaX;
        int manaY = config.manaBarY() >= 0 ? config.manaBarY() : defaultManaY;
        elements.add(new DraggableElement(
                "mana_bar",
                Text.literal("Mana Bar"),
                manaX, manaY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultManaX, defaultManaY,
                config.manaBarX() >= 0 || config.manaBarY() >= 0,
                ctx -> StatBarHudRenderer.renderManaBarDirect(ctx, mc, 0, 0, 100, 100, 100),
                (nx, ny) -> {
                    config.manaBarX(nx);
                    config.manaBarY(ny);
                },
                () -> {
                    config.manaBarX(-1);
                    config.manaBarY(-1);
                }
        ));

        // 6. Hunger Bar
        int defaultHungerX = this.width / 2 + 10;
        int defaultHungerY = this.height - 39;
        int hungerX = config.hungerBarX() >= 0 ? config.hungerBarX() : defaultHungerX;
        int hungerY = config.hungerBarY() >= 0 ? config.hungerBarY() : defaultHungerY;
        elements.add(new DraggableElement(
                "hunger_bar",
                Text.literal("Hunger Bar"),
                hungerX, hungerY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultHungerX, defaultHungerY,
                config.hungerBarX() >= 0 || config.hungerBarY() >= 0,
                ctx -> StatBarHudRenderer.renderHungerBarDirect(ctx, mc, 0, 0, 20),
                (nx, ny) -> {
                    config.hungerBarX(nx);
                    config.hungerBarY(ny);
                },
                () -> {
                    config.hungerBarX(-1);
                    config.hungerBarY(-1);
                }
        ));
    }

    private void resetAllPositions() {
        for (DraggableElement elem : elements) {
            elem.reset();
        }
        if (client != null && client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
        saveChanges();
    }

    private void saveChanges() {
        for (DraggableElement elem : elements) {
            if (elem.isCustom) {
                elem.positionSaver.accept(elem.x, elem.y);
            } else {
                elem.positionResetter.run();
            }
        }
        CranksDungeons.CONFIG.save();
    }

    @Override
    public void close() {
        saveChanges();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Iterate elements in reverse (topmost first)
        for (int i = elements.size() - 1; i >= 0; i--) {
            DraggableElement elem = elements.get(i);
            if (elem.isHovered(mx, my)) {
                if (button == 0) {
                    // Left click: Start dragging
                    draggingElement = elem;
                    dragOffsetX = mx - elem.x;
                    dragOffsetY = my - elem.y;
                    return true;
                } else if (button == 1) {
                    // Right click: Reset element position
                    elem.reset();
                    if (client != null && client.player != null) {
                        client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingElement != null && button == 0) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;

            if (snapToGrid) {
                // Snap to 5px grid
                newX = Math.round(newX / 5.0f) * 5;
                newY = Math.round(newY / 5.0f) * 5;

                // Center snapping
                int centerX = (this.width - draggingElement.width) / 2;
                if (Math.abs(newX - centerX) <= 8) {
                    newX = centerX;
                }

                // Edge snapping
                if (Math.abs(newX - 0) <= 6) newX = 0;
                if (Math.abs(newX - 5) <= 6) newX = 5;
                if (Math.abs(newX - (this.width - draggingElement.width)) <= 6) {
                    newX = this.width - draggingElement.width;
                }
                if (Math.abs(newX - (this.width - draggingElement.width - 5)) <= 6) {
                    newX = this.width - draggingElement.width - 5;
                }

                if (Math.abs(newY - 0) <= 6) newY = 0;
                if (Math.abs(newY - 1) <= 6) newY = 1;
                if (Math.abs(newY - 5) <= 6) newY = 5;
                if (Math.abs(newY - (this.height - draggingElement.height)) <= 6) {
                    newY = this.height - draggingElement.height;
                }
                if (Math.abs(newY - (this.height - draggingElement.height - 5)) <= 6) {
                    newY = this.height - draggingElement.height - 5;
                }
            }

            // Clamping: allow all the way to 0
            newX = Math.max(0, Math.min(this.width - draggingElement.width, newX));
            newY = Math.max(0, Math.min(this.height - draggingElement.height, newY));

            draggingElement.x = newX;
            draggingElement.y = newY;
            draggingElement.isCustom = true;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingElement != null && button == 0) {
            draggingElement = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Very light darkening overlay (NO blur shader)
        context.fill(0, 0, this.width, this.height, 0x44000000);

        // Draw guidelines (Screen Center)
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        context.fill(centerX - 1, 30, centerX, this.height - 20, 0x25FFFFFF);
        context.fill(0, centerY - 1, this.width, centerY, 0x25FFFFFF);

        // Render each draggable HUD element
        for (DraggableElement elem : elements) {
            boolean isHovered = elem.isHovered(mouseX, mouseY);
            boolean isDragging = (elem == draggingElement);

            // Bounding box fill & border
            int fillColor = isDragging ? 0x4055FF55 : (isHovered ? 0x3055FFFF : 0x10FFFFFF);
            int borderColor = isDragging ? 0xFF55FF55 : (isHovered ? 0xFF55FFFF : 0x50FFFFFF);

            context.fill(elem.x - 2, elem.y - 2, elem.x + elem.width + 2, elem.y + elem.height + 2, fillColor);
            context.drawBorder(elem.x - 2, elem.y - 2, elem.width + 4, elem.height + 4, borderColor);

            // Element Label Tag
            String labelText = elem.displayName.getString();
            int labelWidth = textRenderer.getWidth(labelText);
            int tagY = elem.y - 12;
            if (tagY < 4) tagY = elem.y + elem.height + 3;

            context.fill(elem.x - 2, tagY - 1, elem.x + labelWidth + 4, tagY + 9, 0xCC000000);
            context.drawTextWithShadow(textRenderer, labelText, elem.x + 1, tagY, isHovered || isDragging ? 0xFF55FFFF : 0xFFDDDDDD);

            // Render live preview
            context.getMatrices().push();
            context.getMatrices().translate(elem.x, elem.y, 0);
            elem.previewRenderer.accept(context);
            context.getMatrices().pop();
        }

        // Render compact floating center pill background for toolbar
        int tab1W = 95;
        int tab2W = 90;
        int snapW = 75;
        int resetW = 70;
        int doneW = 60;
        int spacing = 4;
        int totalW = tab1W + tab2W + snapW + resetW + doneW + (spacing * 4);
        int startX = (this.width - totalW) / 2;

        context.fill(startX - 6, 2, startX + totalW + 6, 26, 0xDD141418);
        context.drawBorder(startX - 6, 2, totalW + 12, 24, 0xFF2A2A32);

        // Bottom status & helper text
        context.fill(0, this.height - 18, this.width, this.height, 0xEE141418);
        context.fill(0, this.height - 19, this.width, this.height - 18, 0xFF2A2A32);

        String infoText = (draggingElement != null)
                ? ("Dragging: " + draggingElement.displayName.getString() + " | Position: X=" + draggingElement.x + ", Y=" + draggingElement.y)
                : "💡 Drag elements to position them. Right-click any element to reset to default position.";
        int infoWidth = textRenderer.getWidth(infoText);
        context.drawTextWithShadow(textRenderer, infoText, (this.width - infoWidth) / 2, this.height - 14, 0xFFCCCCCC);

        super.render(context, mouseX, mouseY, delta);
    }
}

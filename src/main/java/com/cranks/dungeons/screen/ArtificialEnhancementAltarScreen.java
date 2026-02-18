package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArtificialEnhancementAltarScreen extends HandledScreen<ArtificialEnhancementAltarScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_enhancement_altar.png");

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 16;

    // End portal color palette
    private static final int COLOR_INACTIVE_FILL   = 0xFF050A0A; // near-black void
    private static final int COLOR_INACTIVE_BORDER = 0xFF0A1A14; // dark teal shadow

    private static final int COLOR_ACTIVE_FILL     = 0xFF071A15; // deep portal green-black
    private static final int COLOR_ACTIVE_BORDER   = 0xFF1A7A4A; // muted end-green border

    private static final int COLOR_HOVERED_FILL    = 0xFF0D2E22; // brightened portal dark green
    private static final int COLOR_HOVERED_BORDER  = 0xFF00FFA0; // vivid cyan-green glow (end portal frame highlight)

    private final int[] buttonXPositions = new int[3];
    private final int[] buttonYPositions = new int[3];
    private final boolean[] buttonActive = new boolean[3];

    private final String[] displayNames = new String[3];
    private final int[] displayTiers = new int[3];
    private boolean hasOptions = false;

    public ArtificialEnhancementAltarScreen(ArtificialEnhancementAltarScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        this.titleY = -10;
        this.playerInventoryTitleY = 87;

        // Absolute button positions in screen coordinates
        for (int i = 0; i < 3; i++) {
            buttonXPositions[i] = this.x + 8;
            buttonYPositions[i] = this.y + 30 + (i * (BUTTON_HEIGHT + 2));
            buttonActive[i] = false;
            displayNames[i] = "";
            displayTiers[i] = 0;
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();

        hasOptions = handler.hasClientOptions();

        for (int i = 0; i < 3; i++) {
            if (hasOptions) {
                String statId = handler.getClientStatId(i);
                buttonActive[i] = statId != null && !statId.isEmpty();
                if (buttonActive[i]) {
                    CustomStat stat = StatRegistry.getStat(statId);
                    displayNames[i] = stat != null ? stat.getDisplayName() : "???";
                    displayTiers[i] = handler.getClientTier(i);
                }
            } else {
                buttonActive[i] = false;
                displayNames[i] = "";
                displayTiers[i] = 0;
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean moved) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        int button = click.button();

        if (button == 0) {
            for (int i = 0; i < 3; i++) {
                if (buttonActive[i] && isMouseOverButton(i, mouseX, mouseY)) {
                    if (this.client != null && this.client.interactionManager != null) {

                        // Play your custom sound
                        this.client.getSoundManager().play(
                                PositionedSoundInstance.ui(
                                        ModSounds.ARTIFICIAL_ALTAR_CLICK,  // custom sound
                                        1.0F                                // volume
                                )
                        );

                        // Send selection to server
                        this.client.interactionManager.clickButton(handler.syncId, i);
                        handler.clearClientOptions();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(click, moved);
    }

    private boolean isMouseOverButton(int index, int mouseX, int mouseY) {
        int bx = buttonXPositions[index];
        int by = buttonYPositions[index];
        return mouseX >= bx && mouseX < bx + BUTTON_WIDTH
                && mouseY >= by && mouseY < by + BUTTON_HEIGHT;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int guiX = (this.width - this.backgroundWidth) / 2;
        int guiY = (this.height - this.backgroundHeight) / 2;

        // GUI texture
        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                guiX, guiY,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                176, 180
        );

        // Button backgrounds (absolute coordinates)
        for (int i = 0; i < 3; i++) {
            int bx = buttonXPositions[i];
            int by = buttonYPositions[i];
            boolean hovered = isMouseOverButton(i, mouseX, mouseY);

            if (!buttonActive[i]) {
                context.fill(bx, by, bx + BUTTON_WIDTH, by + BUTTON_HEIGHT, COLOR_INACTIVE_FILL);
                drawBorder(context, bx, by, BUTTON_WIDTH, BUTTON_HEIGHT, COLOR_INACTIVE_BORDER);
            } else if (hovered) {
                context.fill(bx, by, bx + BUTTON_WIDTH, by + BUTTON_HEIGHT, COLOR_HOVERED_FILL);
                drawBorder(context, bx, by, BUTTON_WIDTH, BUTTON_HEIGHT, COLOR_HOVERED_BORDER);
            } else {
                context.fill(bx, by, bx + BUTTON_WIDTH, by + BUTTON_HEIGHT, COLOR_ACTIVE_FILL);
                drawBorder(context, bx, by, BUTTON_WIDTH, BUTTON_HEIGHT, COLOR_ACTIVE_BORDER);
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Draw title (local coordinates)
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFF404040, false);

        // Text for buttons in local GUI coordinates (0,0 is top-left of GUI)
        for (int i = 0; i < 3; i++) {
            int bxLocal = 8;
            int byLocal = 30 + (i * (BUTTON_HEIGHT + 2));
            int textY = byLocal + (BUTTON_HEIGHT - 8) / 2;

            if (hasOptions && buttonActive[i]) {
                String name = displayNames[i];
                int tier = displayTiers[i];

                String full = name + " [T" + tier + "]";
                int totalWidth = this.textRenderer.getWidth(full);
                int textX = bxLocal + (BUTTON_WIDTH - totalWidth) / 2;

                // Name in white
                context.drawTextWithShadow(this.textRenderer, name + " ", textX, textY, 0xFFFFFFFF);
                int nameWidth = this.textRenderer.getWidth(name + " ");
                // Tier in colored [T#]
                context.drawTextWithShadow(this.textRenderer, "[T" + tier + "]", textX + nameWidth, textY, getTierColorInt(tier));
            } else {
                String txt = "";
                int textX = bxLocal + (BUTTON_WIDTH - this.textRenderer.getWidth(txt)) / 2;
                context.drawTextWithShadow(this.textRenderer, txt, textX, textY, 0xFF1A4A30);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int getTierColorInt(int tier) {
        return switch (tier) {
            case 1, 2 -> 0xFFAAAAAA;
            case 3, 4 -> 0xFF55FF55;
            case 5 -> 0xFFFFAA00;
            default -> 0xFFFFFFFF;
        };
    }
}
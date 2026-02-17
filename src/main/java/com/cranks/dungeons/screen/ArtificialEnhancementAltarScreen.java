package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ArtificialEnhancementAltarScreen extends HandledScreen<ArtificialEnhancementAltarScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_enhancement_altar.png");

    private ButtonWidget[] optionButtons = new ButtonWidget[3];

    public ArtificialEnhancementAltarScreen(ArtificialEnhancementAltarScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        titleY = -10;
        playerInventoryTitleY = 73;

        int buttonWidth = 150;
        int buttonHeight = 16;
        int startX = this.x + (backgroundWidth - buttonWidth) / 2;
        int startY = this.y + 30;

        for (int i = 0; i < 3; i++) {
            final int optionIndex = i;
            optionButtons[i] = ButtonWidget.builder(
                    Text.literal("???"),
                    button -> {
                        if (this.client != null && this.client.interactionManager != null) {
                            this.client.interactionManager.clickButton(handler.syncId, optionIndex);
                            handler.clearClientOptions();
                        }
                    }
            ).dimensions(startX, startY + (i * (buttonHeight + 2)), buttonWidth, buttonHeight).build();
            addDrawableChild(optionButtons[i]);
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();

        boolean hasOptions = handler.hasClientOptions();

        for (int i = 0; i < 3; i++) {
            if (hasOptions) {
                String statId = handler.getClientStatId(i);
                int tier = handler.getClientTier(i);

                if (statId != null && !statId.isEmpty()) {
                    CustomStat stat = StatRegistry.getStat(statId);
                    String displayName = stat != null ? stat.getDisplayName() : "???";
                    Formatting tierColor = getTierColor(tier);

                    optionButtons[i].setMessage(
                            Text.literal(displayName + " ")
                                    .formatted(Formatting.WHITE)
                                    .append(Text.literal("[T" + tier + "]").formatted(tierColor))
                    );
                    optionButtons[i].active = true;
                } else {
                    optionButtons[i].setMessage(Text.literal("???").formatted(Formatting.DARK_GRAY));
                    optionButtons[i].active = false;
                }
            } else {
                optionButtons[i].setMessage(Text.literal("Insert equipment with cryptic stat").formatted(Formatting.DARK_GRAY));
                optionButtons[i].active = false;
            }

            // Reset focus/hover state so buttons don't look pressed
            optionButtons[i].setFocused(false);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                176, 166
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private static Formatting getTierColor(int tier) {
        return switch (tier) {
            case 1, 2 -> Formatting.GRAY;
            case 3, 4 -> Formatting.GREEN;
            case 5 -> Formatting.GOLD;
            default -> Formatting.WHITE;
        };
    }
}
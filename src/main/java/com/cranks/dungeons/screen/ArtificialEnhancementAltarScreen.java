package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArtificialEnhancementAltarScreen extends HandledScreen<ArtificialEnhancementAltarScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_enhancement_altar.png");

    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 16;

    private final ButtonWidget[] optionButtons = new ButtonWidget[3];
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

        createButtons();
    }

    private void createButtons() {
        for (int i = 0; i < 3; i++) {
            int buttonX = this.x + 8;
            int buttonY = this.y + 30 + (i * (BUTTON_HEIGHT + 2));
            final int index = i;

            if (optionButtons[i] != null) {
                this.remove(optionButtons[i]);
            }

            optionButtons[i] = ButtonWidget.builder(Text.empty(), button -> {
                        if (this.client != null && this.client.interactionManager != null) {
                            this.client.getSoundManager().play(
                                    PositionedSoundInstance.ui(ModSounds.ARTIFICIAL_ALTAR_CLICK, 1.0F)
                            );
                            this.client.interactionManager.clickButton(handler.syncId, index);
                            handler.clearClientOptions();
                        }
                    })
                    .dimensions(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            optionButtons[i].active = false;
            this.addDrawableChild(optionButtons[i]);
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();

        hasOptions = handler.hasClientOptions();

        for (int i = 0; i < 3; i++) {
            if (hasOptions) {
                String statId = handler.getClientStatId(i);
                boolean active = statId != null && !statId.isEmpty();
                if (active) {
                    CustomStat stat = StatRegistry.getStat(statId);
                    String name = stat != null ? stat.getDisplayName() : "???";
                    int tier = handler.getClientTier(i);
                    final int tierColor = getTierColorRgb(tier);

                    optionButtons[i].setMessage(
                            Text.literal(name + " ")
                                    .append(Text.literal("[T" + tier + "]").styled(s -> s.withColor(tierColor)))
                    );
                    optionButtons[i].active = true;
                } else {
                    optionButtons[i].setMessage(Text.empty());
                    optionButtons[i].active = false;
                }
            } else {
                optionButtons[i].setMessage(Text.empty());
                optionButtons[i].active = false;
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int guiX = (this.width - this.backgroundWidth) / 2;
        int guiY = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                guiX, guiY,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                176, 180
        );
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFF404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private static int getTierColorRgb(int tier) {
        return switch (tier) {
            case 1, 2 -> 0xAAAAAA;
            case 3, 4 -> 0x55FF55;
            case 5 -> 0xFFAA00;
            default -> 0xFFFFFF;
        };
    }
}
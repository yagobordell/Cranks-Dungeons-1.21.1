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
    private static final Identifier STANDARD_TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar.png");

    private static final Identifier[] TEXTURES = {
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_1.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_2.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_3.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_4.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_5.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/artificial_altar/artificial_enhancement_altar_6.png")
    };

    private static final int BUTTON_WIDTH = 210;
    private static final int BUTTON_HEIGHT = 16;

    private final ButtonWidget[] optionButtons = new ButtonWidget[3];
    private boolean hasOptions = false;
    private int animationTick = 0;

    public ArtificialEnhancementAltarScreen(ArtificialEnhancementAltarScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 177;
        this.backgroundHeight = 186;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = 8;
        this.titleY = 6;
        this.playerInventoryTitleY = 93;

        createButtons();
    }

    private void createButtons() {
        for (int i = 0; i < 3; i++) {
            int buttonX = this.x + (this.backgroundWidth - BUTTON_WIDTH) / 2;
            int buttonY = this.y + 39 + (i * (BUTTON_HEIGHT + 2));
            final int index = i;

            if (optionButtons[i] != null) {
                this.remove(optionButtons[i]);
            }

            optionButtons[i] = ButtonWidget.builder(Text.empty(), button -> {
                        if (this.client != null && this.client.interactionManager != null) {
                            this.client.getSoundManager().play(
                                    PositionedSoundInstance.master(ModSounds.ARTIFICIAL_ALTAR_CLICK, 1.0F)
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
        animationTick++;

        hasOptions = handler.hasClientOptions();

        for (int i = 0; i < 3; i++) {
            String optionText = handler.getClientStatId(i);
            boolean hasText = optionText != null && !optionText.isEmpty();
            
            if (hasText) {
                boolean isErrorMsg = optionText.equals("No Artificial Rune");
                boolean isActive = hasOptions && !isErrorMsg;
                
                int color = isActive ? 0xFFFFFF : 0xA0A0A0;
                optionButtons[i].setMessage(Text.literal(optionText).styled(s -> s.withColor(color)));
                optionButtons[i].active = isActive;
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

        boolean hasItem = !handler.getSlot(0).getStack().isEmpty();
        Identifier textureToDraw;

        if (hasItem) {
            textureToDraw = STANDARD_TEXTURE;
            animationTick = 0; // reset so it starts from frame 1 when item is removed
        } else {
            // Cycle through the 6 textures (1 frame every 30 ticks / 1.5 seconds)
            int frameIndex = (animationTick / 30) % TEXTURES.length;
            textureToDraw = TEXTURES[frameIndex];
        }

        context.drawTexture(
                textureToDraw,
                guiX, guiY,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                256, 256
        );
    }



    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Empty to prevent default title rendering
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
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
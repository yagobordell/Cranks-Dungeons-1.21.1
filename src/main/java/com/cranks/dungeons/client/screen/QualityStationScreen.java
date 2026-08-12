package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class QualityStationScreen extends HandledScreen<QualityStationScreenHandler> {
    // Standard (non-animated) texture — shown when an item is in the equipment slot
    private static final Identifier TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station.png");

    // Animated idle frames 1-6, cycled when the equipment slot is empty
    private static final Identifier[] ANIMATED_TEXTURES = new Identifier[] {
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_1.png"),
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_2.png"),
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_3.png"),
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_4.png"),
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_5.png"),
        Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_6.png"),
    };

    private int animationTick = 0;

    public QualityStationScreen(QualityStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        animationTick++;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Determine which texture to use based on whether the equipment slot has an item
        boolean hasEquipment = !handler.getSlot(QualityStationBlockEntity.EQUIPMENT_SLOT).getStack().isEmpty();
        Identifier currentTexture;

        if (hasEquipment) {
            // Item present — show the standard static texture and reset animation
            currentTexture = TEXTURE;
            animationTick = 0;
        } else {
            // No item — cycle through animated textures (30 ticks per frame = ~1.5 seconds)
            int frameIndex = (animationTick / 30) % ANIMATED_TEXTURES.length;
            currentTexture = ANIMATED_TEXTURES[frameIndex];
        }

        context.drawTexture(
                currentTexture,
                x, y,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                256,
                256
        );

        int progress = handler.getScaledProgress();
        if (progress > 0) {
            context.drawTexture(
                    currentTexture,
                    x + 121, y + 48,
                    176.0f, 0.0f,
                    progress,
                    15,
                    256,
                    256
            );
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Empty to prevent default title rendering
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

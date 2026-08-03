package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.network.StartQualityUpgradePayload;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class QualityStationScreen extends HandledScreen<QualityStationScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station.png");
    private ButtonWidget upgradeButton;

    public QualityStationScreen(QualityStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        
        int buttonX = x + 62;
        int buttonY = y + 53;
        upgradeButton = ButtonWidget.builder(Text.literal("Upgrade"), button -> {
            ClientPlayNetworking.send(new StartQualityUpgradePayload(!handler.isProcessing()));
        })
        .dimensions(buttonX, buttonY, 48, 14)
        .build();
        
        addDrawableChild(upgradeButton);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                TEXTURE,
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
                    TEXTURE,
                    x + 76, y + 26,
                    176.0f, 14.0f,
                    progress + 1,
                    16,
                    256,
                    256
            );
        }
        
        if (handler.isProcessing()) {
            upgradeButton.setMessage(Text.literal("Stop"));
        } else {
            upgradeButton.setMessage(Text.literal("Upgrade"));
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Empty to prevent default title rendering
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

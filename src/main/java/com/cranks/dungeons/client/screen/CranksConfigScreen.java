package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CranksConfigScreen extends ConfigScreen {

    public CranksConfigScreen(Screen parent) {
        super(Identifier.of("owo", "config"), CranksDungeons.CONFIG, parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        super.build(rootComponent);
        LabelComponent titleLabel = rootComponent.childById(LabelComponent.class, "title-label");
        if (titleLabel != null) {
            titleLabel.text(Text.empty());
            titleLabel.margins(Insets.top(14));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().push();
        // High Z translation so tabs are completely on top of all panels and background containers
        context.getMatrices().translate(0, 0, 300.0f);

        // Render Top Tab Bar Overlay
        int topBarY = 3;
        int tabHeight = 20;
        int tab1W = 100;
        int tab2W = 95;
        int spacing = 4;
        int totalWidth = tab1W + tab2W + spacing;
        int startX = (this.width - totalWidth) / 2;

        int tab1X = startX;
        int tab2X = startX + tab1W + spacing;

        // Background pill behind the two tabs
        context.fill(startX - 4, topBarY - 2, startX + totalWidth + 4, topBarY + tabHeight + 2, 0xEE141418);
        context.drawBorder(startX - 4, topBarY - 2, totalWidth + 8, tabHeight + 4, 0xFF2A2A32);

        // Tab 1: Mod Settings (Active)
        context.fill(tab1X, topBarY, tab1X + tab1W, topBarY + tabHeight, 0xEE2A2A38);
        context.drawBorder(tab1X, topBarY, tab1W, tabHeight, 0xFFFFD700);
        Text tab1Text = Text.literal("⚙ Mod Settings").formatted(Formatting.GOLD, Formatting.BOLD);
        int t1w = textRenderer.getWidth(tab1Text);
        context.drawTextWithShadow(textRenderer, tab1Text, tab1X + (tab1W - t1w) / 2, topBarY + (tabHeight - 8) / 2, 0xFFFFFFFF);

        // Tab 2: HUD Layout (Inactive / Clickable)
        boolean tab2Hovered = mouseX >= tab2X && mouseX <= tab2X + tab2W && mouseY >= topBarY && mouseY <= topBarY + tabHeight;
        int tab2Bg = tab2Hovered ? 0xDD3A3A4C : 0xAA181822;
        int tab2Border = tab2Hovered ? 0xFFFFFFFF : 0xFF555566;
        context.fill(tab2X, topBarY, tab2X + tab2W, topBarY + tabHeight, tab2Bg);
        context.drawBorder(tab2X, topBarY, tab2W, tabHeight, tab2Border);
        Text tab2Text = Text.literal("📐 HUD Layout").formatted(tab2Hovered ? Formatting.WHITE : Formatting.GRAY);
        int t2w = textRenderer.getWidth(tab2Text);
        context.drawTextWithShadow(textRenderer, tab2Text, tab2X + (tab2W - t2w) / 2, topBarY + (tabHeight - 8) / 2, 0xFFFFFFFF);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int topBarY = 3;
            int tabHeight = 20;
            int tab1W = 100;
            int tab2W = 95;
            int spacing = 4;
            int totalWidth = tab1W + tab2W + spacing;
            int startX = (this.width - totalWidth) / 2;
            int tab2X = startX + tab1W + spacing;

            if (mouseX >= tab2X && mouseX <= tab2X + tab2W && mouseY >= topBarY && mouseY <= topBarY + tabHeight) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null && mc.player != null) {
                    mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                }
                if (mc != null) {
                    mc.setScreen(new HudLayoutEditScreen(this.parent));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}

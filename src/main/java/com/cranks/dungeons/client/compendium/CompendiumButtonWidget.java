package com.cranks.dungeons.client.compendium;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class CompendiumButtonWidget extends ClickableWidget {
    public interface PressAction {
        void onPress(CompendiumButtonWidget button);
    }

    private final PressAction onPress;

    public CompendiumButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public static Builder builder(Text message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        private int x, y, width = 150, height = 20;

        public Builder(Text message, PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public CompendiumButtonWidget build() {
            return new CompendiumButtonWidget(x, y, width, height, message, onPress);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()) {
                if (button == 0) {
                    this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                    this.onPress.onPress(this);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawButton(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHovered(), this.active, this.getMessage().getString());
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height, boolean hovered, boolean active, String text) {
        int bgColor = hovered ? 0xFF1A1A3A : 0xFF0D0D1A;
        if (!active) {
            bgColor = 0xFF05050A;
        }
        int borderColor = hovered ? 0xFF4A4A7A : 0xFF2A2A4A;
        if (!active) {
            borderColor = 0xFF15152A;
        }
        
        context.fill(x, y, x + width, y + height, bgColor);
        
        // Borders
        context.fill(x, y, x + width, y + 1, borderColor);
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        context.fill(x, y, x + 1, y + height, borderColor);
        context.fill(x + width - 1, y, x + width, y + height, borderColor);
        
        int textColor = active ? (hovered ? 0xFFEEEEFF : 0xFFCCCCDD) : 0xFF555566;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer, 
                text, 
                x + width / 2, 
                y + (height - 8) / 2, 
                textColor);
    }
}

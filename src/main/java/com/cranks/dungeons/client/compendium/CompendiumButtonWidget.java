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
        int centerColor = hovered ? 0xFF3C3C46 : 0xFF2B2B33;
        int lightBevel = hovered ? 0xFF707080 : 0xFF585864;
        int darkBevel = hovered ? 0xFF22222A : 0xFF18181E;
        int outerBorder = 0xFF101014;

        if (!active) {
            centerColor = 0xFF1A1A20;
            lightBevel = 0xFF2E2E36;
            darkBevel = 0xFF121216;
            outerBorder = 0xFF0E0E12;
        }

        // Outer 1px frame
        context.fill(x, y, x + width, y + height, outerBorder);

        // Inner beveled stone
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, centerColor);

        // Light bevel on Top & Left
        context.fill(x + 1, y + 1, x + width - 1, y + 2, lightBevel);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, lightBevel);

        // Dark bevel on Bottom & Right
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, darkBevel);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, darkBevel);

        int textColor = active ? (hovered ? 0xFFFFFFFF : 0xFFE0E0E6) : 0xFF6C6C78;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                text,
                x + width / 2,
                y + (height - 8) / 2,
                textColor);
    }
}

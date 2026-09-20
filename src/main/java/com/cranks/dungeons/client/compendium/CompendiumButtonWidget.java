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
    private final boolean crossButton;
    private final boolean tabStyle;
    private final boolean selected;
    private final int selectedColor;

    public CompendiumButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        this(x, y, width, height, message, onPress, false, false, false, CompendiumStyle.TEXT_GOLD_LIGHT);
    }

    private CompendiumButtonWidget(int x, int y, int width, int height, Text message,
                                   PressAction onPress, boolean crossButton, boolean tabStyle,
                                   boolean selected, int selectedColor) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.crossButton = crossButton;
        this.tabStyle = tabStyle;
        this.selected = selected;
        this.selectedColor = selectedColor;
    }

    public static Builder builder(Text message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        private int x, y, width = 150, height = 20;
        private boolean crossButton;
        private boolean tabStyle;
        private boolean selected;
        private int selectedColor = CompendiumStyle.TEXT_GOLD_LIGHT;

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

        public Builder crossButton() {
            this.crossButton = true;
            return this;
        }

        public Builder tab() {
            this.tabStyle = true;
            return this;
        }

        public Builder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder selectedColor(int selectedColor) {
            this.selectedColor = selectedColor;
            return this;
        }

        public CompendiumButtonWidget build() {
            return new CompendiumButtonWidget(x, y, width, height, message, onPress, crossButton,
                    tabStyle, selected, selectedColor);
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
        if (crossButton) {
            CompendiumStyle.drawVanillaCrossButton(context, this.getX(), this.getY(), this.getWidth(),
                    this.isHovered(), this.active);
            return;
        }
        if (tabStyle) {
            CompendiumStyle.drawCreativeTab(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    this.isHovered(), this.selected);
            String label = MinecraftClient.getInstance().textRenderer.trimToWidth(
                    this.getMessage().getString(), Math.max(10, this.getWidth() - 12));
            int textColor = this.selected ? this.selectedColor : CompendiumStyle.TEXT;
            if (this.selected) {
                context.fill(this.getX() + 5, this.getY() + this.getHeight() - 2,
                        this.getX() + this.getWidth() - 5, this.getY() + this.getHeight(),
                        this.selectedColor);
            }
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, label,
                    this.getX() + this.getWidth() / 2, this.getY() + 12, textColor);
            return;
        }
        drawButton(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHovered(),
                this.active, this.selected, this.getMessage().getString());
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height, boolean hovered, boolean active, String text) {
        drawButton(context, x, y, width, height, hovered, active, false, text);
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height,
                                  boolean hovered, boolean active, boolean selected, String text) {
        // Use the actual vanilla widget sprites instead of reproducing their
        // bevels in code. Resource packs can therefore restyle every button.
        CompendiumStyle.drawVanillaButton(context, x, y, width, height, hovered || selected, active);

        int textColor = selected ? CompendiumStyle.TEXT_GOLD_LIGHT
                : active ? (hovered ? 0xFFFFFFFF : CompendiumStyle.TEXT) : CompendiumStyle.TEXT_DISABLED;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                text,
                x + width / 2,
                y + (height - 8) / 2,
                textColor);
    }
}

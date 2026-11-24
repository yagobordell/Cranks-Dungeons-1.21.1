package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.InventoryStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;

public class CompendiumButtonWidget extends ClickableWidget {
    public interface PressAction {
        void onPress(CompendiumButtonWidget button);
    }

    private final PressAction onPress;
    private enum Style { BUTTON, TOOLBAR, CLOSE, CLASP, ARMOR_SWAP }
    private final Style style;
    private final boolean selected;

    public CompendiumButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        this(x, y, width, height, message, onPress, Style.BUTTON, false);
    }

    private CompendiumButtonWidget(int x, int y, int width, int height, Text message,
                                   PressAction onPress, Style style, boolean selected) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.style = style;
        this.selected = selected;
    }

    public static Builder builder(Text message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        private int x, y, width = 150, height = 20;
        private Style style = Style.BUTTON;
        private boolean selected;

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
            this.style = Style.CLOSE;
            return this;
        }

        public Builder clasp() {
            this.style = Style.CLASP;
            return this;
        }

        public Builder armorSwap() {
            this.style = Style.ARMOR_SWAP;
            return this;
        }

        Builder toolbar() {
            this.style = Style.TOOLBAR;
            return this;
        }

        public Builder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public CompendiumButtonWidget build() {
            return new CompendiumButtonWidget(x, y, width, height, message, onPress, style, selected);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !this.visible || !KeyCodes.isToggle(keyCode)) return false;
        this.playDownSound(MinecraftClient.getInstance().getSoundManager());
        this.onPress.onPress(this);
        return true;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (style != Style.BUTTON) {
            boolean highlighted = active && (isHovered() || isFocused());
            if (style == Style.CLASP || style == Style.ARMOR_SWAP) {
                InventoryStyle.drawSocket(context, getX(), getY(), getWidth(), getHeight(), highlighted);
            } else {
                CompendiumStyle.drawToolbarControl(context, getX(), getY(), getWidth(), getHeight(), highlighted);
            }
            if (style == Style.CLOSE) {
                CompendiumStyle.drawJournalIcon(context, 14, getX() + (getWidth() - 16) / 2,
                        getY() + (getHeight() - 16) / 2);
            } else if (style == Style.ARMOR_SWAP) {
                InventoryStyle.drawSwapIcon(context, getX(), getY(), getWidth(), getHeight());
            } else {
                context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                        getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2,
                        active ? CompendiumStyle.TEXT : CompendiumStyle.TEXT_DISABLED);
            }
            return;
        }
        drawButton(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHovered() || this.isFocused(),
                this.active, this.selected, this.getMessage().getString());
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height, boolean hovered, boolean active, String text) {
        drawButton(context, x, y, width, height, hovered, active, false, text);
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height,
                                  boolean hovered, boolean active, boolean selected, String text) {
        CompendiumStyle.drawRailPlaque(context, x, y, width, height, selected, hovered && active);

        int textColor = !active ? CompendiumStyle.TEXT_DISABLED : CompendiumStyle.TEXT;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                text,
                x + width / 2,
                y + (height - 8) / 2,
                textColor);
    }
}

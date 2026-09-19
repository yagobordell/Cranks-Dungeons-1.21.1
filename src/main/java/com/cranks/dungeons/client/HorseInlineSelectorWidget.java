package com.cranks.dungeons.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

/** Transparent input surface for a panel-native {@code < value >} selector. */
public final class HorseInlineSelectorWidget extends ClickableWidget {
    private static final int ARROW_HIT_WIDTH = 8;
    private final IntConsumer selectionHandler;

    public HorseInlineSelectorWidget(int width, int height, Text message,
                                     IntConsumer selectionHandler) {
        super(0, 0, width, height, message);
        this.selectionHandler = selectionHandler;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) return false;

        playDownSound(MinecraftClient.getInstance().getSoundManager());
        selectionHandler.accept(mouseX < getX() + ARROW_HIT_WIDTH ? -1 : 1);
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!active || !visible || mouseY < getY() || mouseY >= getY() + getHeight()) return false;
        return (mouseX >= getX() && mouseX < getX() + ARROW_HIT_WIDTH)
                || (mouseX >= getX() + getWidth() - ARROW_HIT_WIDTH
                && mouseX < getX() + getWidth());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // The owning panel draws the visible text in its normal scaled style.
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}

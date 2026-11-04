package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.client.GuiTextureResolver;
import com.cranks.dungeons.name.NameTagManager;
import com.cranks.dungeons.network.ApplyNameTagPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Vanilla-style name entry screen opened before a name tag is consumed. */
public final class NameTagScreen extends Screen {
    private static final String NAME_PLACEHOLDER = "Enter a name";
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 108;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;

    private final int entityId;
    private final String initialName;
    private final boolean offHand;

    private NameTagTextField nameField;
    private NameTagButton confirmButton;
    private Identifier panelTexture;
    private Identifier fieldTexture;
    private Identifier fieldHighlightedTexture;
    private Identifier buttonTexture;
    private Identifier buttonHighlightedTexture;
    private Identifier buttonDisabledTexture;

    public NameTagScreen(int entityId, String initialName, boolean offHand) {
        super(Text.literal("Name Tag"));
        this.entityId = entityId;
        this.initialName = initialName == null ? "" : initialName;
        this.offHand = offHand;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // render() draws the blurred world once before the panel. Screen.render()
        // calls this hook again while rendering widgets, so keep that second pass empty.
    }

    @Override
    protected void init() {
        super.init();

        panelTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/background", Identifier.ofVanilla("popup/background"));
        fieldTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/text_field", Identifier.ofVanilla("widget/text_field"));
        fieldHighlightedTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/text_field_highlighted", Identifier.ofVanilla("widget/text_field_highlighted"));
        buttonTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/button", Identifier.ofVanilla("widget/button"));
        buttonHighlightedTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/button_highlighted", Identifier.ofVanilla("widget/button_highlighted"));
        buttonDisabledTexture = GuiTextureResolver.customOrVanilla(
                "name_tag/button_disabled", Identifier.ofVanilla("widget/button_disabled"));

        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int fieldX = panelX + (PANEL_WIDTH - FIELD_WIDTH) / 2;

        nameField = new NameTagTextField(textRenderer, fieldX, panelY + 34,
                FIELD_WIDTH, FIELD_HEIGHT, Text.literal("Name"),
                fieldTexture, fieldHighlightedTexture);
        nameField.setMaxLength(NameTagManager.MAX_NAME_LENGTH);
        nameField.setPlaceholder(Text.literal(NAME_PLACEHOLDER));
        nameField.setTextPredicate(value -> value != null
                && (value.isEmpty() || isAllowedText(value)));
        nameField.setChangedListener(ignored -> updateConfirmState());
        nameField.setText(initialName);
        addDrawableChild(nameField);

        int buttonY = panelY + 76;
        int cancelX = panelX + 24;
        int confirmX = panelX + PANEL_WIDTH - 24 - BUTTON_WIDTH;
        addDrawableChild(new NameTagButton(cancelX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal("Cancel"), this::close,
                buttonTexture, buttonHighlightedTexture, buttonDisabledTexture));
        confirmButton = addDrawableChild(new NameTagButton(confirmX, buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT, Text.literal("Confirm"), this::confirm,
                buttonTexture, buttonHighlightedTexture, buttonDisabledTexture));
        updateConfirmState();

        nameField.setCursorToEnd(false);
    }

    @Override
    protected void setInitialFocus() {
        if (nameField != null) {
            // Screen calls this after init() returns. Set the parent and widget
            // focus together so keyboard input reaches the field immediately.
            setFocused(nameField);
        } else {
            super.setInitialFocus();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        context.drawGuiTexture(panelTexture, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        context.drawCenteredTextWithShadow(textRenderer, title,
                width / 2, panelY + 10, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Enter a name"),
                panelX + 20, panelY + 24, 0xFF404040);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                && confirmButton != null && confirmButton.active) {
            confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateConfirmState() {
        if (confirmButton != null) confirmButton.active = isAllowedText(nameField.getText());
    }

    private void confirm() {
        if (nameField == null || !isAllowedText(nameField.getText())) return;
        if (ClientPlayNetworking.canSend(ApplyNameTagPayload.ID)) {
            ClientPlayNetworking.send(new ApplyNameTagPayload(
                    entityId, nameField.getText().strip(), offHand));
        }
        close();
    }

    private static boolean isAllowedText(String value) {
        if (value == null) return false;
        String trimmed = value.strip();
        return !trimmed.isEmpty()
                && trimmed.length() <= NameTagManager.MAX_NAME_LENGTH
                && trimmed.codePoints().noneMatch(Character::isISOControl);
    }

    private static final class NameTagTextField extends TextFieldWidget {
        private final TextRenderer fieldTextRenderer;
        private final Identifier normalTexture;
        private final Identifier highlightedTexture;

        private NameTagTextField(TextRenderer textRenderer, int x, int y, int width, int height,
                                 Text message, Identifier normalTexture, Identifier highlightedTexture) {
            super(textRenderer, x, y, width, height, message);
            this.fieldTextRenderer = textRenderer;
            this.normalTexture = normalTexture;
            this.highlightedTexture = highlightedTexture;
            setDrawsBackground(false);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier texture = isFocused() ? highlightedTexture : normalTexture;
            context.drawGuiTexture(texture, getX(), getY(), getWidth(), getHeight());

            String visibleContent = getText();
            if (visibleContent.isEmpty() && !isFocused()) visibleContent = NAME_PLACEHOLDER;
            int contentWidth = Math.min(textContentWidth(), fieldTextRenderer.getWidth(visibleContent));
            float horizontalOffset = 4.0F + (textContentWidth() - contentWidth) / 2.0F;
            // With the built-in background disabled, TextFieldWidget also omits
            // its normal 4px horizontal and 6px vertical text inset.
            float verticalOffset = (getHeight() - 8) / 2.0F;

            context.getMatrices().push();
            context.getMatrices().translate(horizontalOffset, verticalOffset, 0.0);
            super.renderWidget(context, mouseX, mouseY, delta);
            context.getMatrices().pop();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean insideField = mouseX >= getX() && mouseX < getX() + getWidth()
                    && mouseY >= getY() && mouseY < getY() + getHeight();
            if (insideField) {
                mouseX -= centeredTextOffset();
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        private double centeredTextOffset() {
            String visibleContent = getText();
            if (visibleContent.isEmpty() && !isFocused()) visibleContent = NAME_PLACEHOLDER;
            int contentWidth = Math.min(textContentWidth(), fieldTextRenderer.getWidth(visibleContent));
            return 4.0 + (textContentWidth() - contentWidth) / 2.0;
        }

        private int textContentWidth() {
            return Math.max(0, getWidth() - 8);
        }
    }

    private static final class NameTagButton extends ClickableWidget {
        private final Runnable onPress;
        private final Identifier normalTexture;
        private final Identifier highlightedTexture;
        private final Identifier disabledTexture;

        private NameTagButton(int x, int y, int width, int height, Text message,
                              Runnable onPress, Identifier normalTexture,
                              Identifier highlightedTexture, Identifier disabledTexture) {
            super(x, y, width, height, message);
            this.onPress = onPress;
            this.normalTexture = normalTexture;
            this.highlightedTexture = highlightedTexture;
            this.disabledTexture = disabledTexture;
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!active || !visible || button != 0
                    || mouseX < getX() || mouseX >= getX() + getWidth()
                    || mouseY < getY() || mouseY >= getY() + getHeight()) {
                return false;
            }
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            onPress.run();
            return true;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier texture = !active ? disabledTexture
                    : isHovered() ? highlightedTexture : normalTexture;
            context.drawGuiTexture(texture, getX(), getY(), getWidth(), getHeight());
            int color = active ? 0xFFFFFFFF : 0xFF777777;
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    getMessage(), getX() + getWidth() / 2,
                    getY() + (getHeight() - 8) / 2, color);
        }
    }
}

package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class KeybindScreen extends Screen {
    private final Screen parent;
    private final KeyBinding keyBinding;
    private boolean listening = false;

    public KeybindScreen(Screen parent, KeyBinding keyBinding) {
        super(Text.literal("Press a Key"));
        this.parent = parent;
        this.keyBinding = keyBinding;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Cancel"),
            button -> close()
        ).dimensions(this.width / 2 - 100, this.height / 2 + 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        String message = listening ? "> Press any key <" : "Press a key to bind...";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Current: ").append(keyBinding.getBoundKeyLocalizedText()), 
            this.width / 2, this.height / 2 + 10, 0xAAAAAA);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        try {
            if (input.key() == 256) { // ESC
                close();
            } else {
                keyBinding.setBoundKey(InputUtil.fromKeyCode(input));
                close();
            }
        } catch (Exception e) {
            CranksDungeons.LOGGER.error("Error setting keybind", e);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean bl) {
        try {
            keyBinding.setBoundKey(InputUtil.Type.MOUSE.createFromCode(click.button()));
            close();
        } catch (Exception e) {
            CranksDungeons.LOGGER.error("Error setting mouse bind", e);
        }
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}

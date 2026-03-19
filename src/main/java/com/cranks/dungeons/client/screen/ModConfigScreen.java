package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.client.ModKeybinds;
import com.cranks.dungeons.config.ModConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class ModConfigScreen extends Screen {
    private final Screen parent;
    private KeyBinding selectedKeyBinding = null;

    public ModConfigScreen(Screen parent) {
        super(Text.literal("Cranks Dungeons Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 22;
        int buttonWidth = 200;

        ModConfig config = ModConfig.get();

        // --- Damage Section ---
        addToggleButton(centerX - buttonWidth / 2, startY, "Damage Dealt", config.showMyDealtDamage, (val) -> config.showMyDealtDamage = val);
        addToggleButton(centerX - buttonWidth / 2, startY + spacing, "Damage Received", config.showMyReceivedDamage, (val) -> config.showMyReceivedDamage = val);
        addToggleButton(centerX - buttonWidth / 2, startY + spacing * 2, "Other Damage", config.showOtherPlayerDamage, (val) -> config.showOtherPlayerDamage = val);

        // --- Healing Section ---
        addToggleButton(centerX - buttonWidth / 2, startY + spacing * 4, "Self Healing", config.showMyReceivedHealing, (val) -> config.showMyReceivedHealing = val);
        addToggleButton(centerX - buttonWidth / 2, startY + spacing * 5, "Other Healing", config.showOtherPlayerHealing, (val) -> config.showOtherPlayerHealing = val);

        // --- Keybinds Section ---
        addKeybindButton(centerX - buttonWidth / 2, startY + spacing * 7, ModKeybinds.TOGGLE_ENCHANTMENT_DISABLE);

        // --- Back Button ---
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            ModConfig.save();
            this.client.options.write();
            this.client.setScreen(this.parent);
        }).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    private void addToggleButton(int x, int y, String label, boolean initialValue, java.util.function.Consumer<Boolean> setter) {
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(initialValue)
                .build(x, y, 200, 20, Text.literal(label), (button, value) -> {
                    setter.accept(value);
                }));
    }

    private void addKeybindButton(int x, int y, KeyBinding keyBinding) {
        this.addDrawableChild(ButtonWidget.builder(getKeybindText(keyBinding), button -> {
            this.selectedKeyBinding = keyBinding;
            button.setMessage(Text.literal("> ").append(keyBinding.getBoundKeyLocalizedText()).append(" <"));
        }).dimensions(x, y, 200, 20).build());
    }

    private Text getKeybindText(KeyBinding keyBinding) {
        return Text.literal("Toggle Enchantment: ").append(keyBinding.getBoundKeyLocalizedText());
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (selectedKeyBinding != null) {
            if (input.key() == 256) { // ESC
                selectedKeyBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
            } else {
                selectedKeyBinding.setBoundKey(InputUtil.fromKeyCode(input));
            }
            selectedKeyBinding = null;
            KeyBinding.updateKeysByCode(); // Refresh the internal key mapping
            this.clearAndInit();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (selectedKeyBinding != null) {
            selectedKeyBinding.setBoundKey(InputUtil.Type.MOUSE.createFromCode(click.button()));
            selectedKeyBinding = null;
            KeyBinding.updateKeysByCode(); // Refresh the internal key mapping
            this.clearAndInit();
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("--- Damage Numbers ---"), this.width / 2, 28, 0xFFAA00);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("--- Healing Numbers ---"), this.width / 2, 110, 0x55FF55);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("--- Keybinds ---"), this.width / 2, 175, 0xAAAAAA);
    }

    @Override
    public void close() {
        ModConfig.save();
        this.client.options.write();
        this.client.setScreen(this.parent);
    }
}

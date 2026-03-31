package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.client.ModKeybinds;
import com.cranks.dungeons.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModConfigScreen extends Screen {
    private final Screen parent;
    private Tab currentTab = Tab.COMBAT;

    private enum Tab {
        COMBAT("Combat"),
        PARTY_HUD("Party HUD"),
        CONTROLS("Controls");

        final String name;

        Tab(String name) {
            this.name = name;
        }
    }

    public ModConfigScreen(Screen parent) {
        super(Text.literal("Cranks Dungeons Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabCount = Tab.values().length;
        int tabWidth = this.width / tabCount;
        int tabHeight = 25;
        int tabY = 30;

        // Tab buttons
        for (int i = 0; i < tabCount; i++) {
            final Tab tab = Tab.values()[i];
            final int tabX = i * tabWidth;
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(tab.name),
                button -> {
                    currentTab = tab;
                    this.clearAndInit();
                }
            ).dimensions(tabX, tabY, tabWidth, tabHeight).build());
        }

        // Tab content
        switch (currentTab) {
            case COMBAT:
                initCombatTab();
                break;
            case PARTY_HUD:
                initPartyHudTab();
                break;
            case CONTROLS:
                initControlsTab();
                break;
        }

        // Done button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> this.close()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    private void initCombatTab() {
        int centerX = this.width / 2;
        int startY = 70;
        int spacing = 35;
        int buttonWidth = 200;
        ModConfig config = ModConfig.get();

        // Show Damage Dealt
        addToggleSetting(centerX - buttonWidth / 2, startY, "Show Damage Dealt", config.showMyDealtDamage,
            val -> config.showMyDealtDamage = val,
            () -> {
                config.showMyDealtDamage = true;
                refreshValues();
            },
            "Display damage numbers when you deal damage to mobs and players");

        // Show Damage Received
        addToggleSetting(centerX - buttonWidth / 2, startY + spacing, "Show Damage Received", config.showMyReceivedDamage,
            val -> config.showMyReceivedDamage = val,
            () -> {
                config.showMyReceivedDamage = true;
                refreshValues();
            },
            "Display damage numbers when you take damage from mobs or players");

        // Show Other Damage
        addToggleSetting(centerX - buttonWidth / 2, startY + spacing * 2, "Show Other Damage", config.showOtherPlayerDamage,
            val -> config.showOtherPlayerDamage = val,
            () -> {
                config.showOtherPlayerDamage = true;
                refreshValues();
            },
            "Display damage numbers from other players and mob attacks");

        // Show Self Healing
        addToggleSetting(centerX - buttonWidth / 2, startY + spacing * 3, "Show Self Healing", config.showMyReceivedHealing,
            val -> config.showMyReceivedHealing = val,
            () -> {
                config.showMyReceivedHealing = true;
                refreshValues();
            },
            "Display healing numbers from life steal, regeneration, and other sources");

        // Show Other Healing
        addToggleSetting(centerX - buttonWidth / 2, startY + spacing * 4, "Show Other Healing", config.showOtherPlayerHealing,
            val -> config.showOtherPlayerHealing = val,
            () -> {
                config.showOtherPlayerHealing = true;
                refreshValues();
            },
            "Display healing numbers when enemies or other players heal themselves");
    }

    private void initPartyHudTab() {
        int centerX = this.width / 2;
        int startY = 70;
        int spacing = 30;
        int buttonWidth = 200;
        ModConfig config = ModConfig.get();

        // Enable Party HUD
        addToggleSetting(centerX - buttonWidth / 2, startY, "Enable Party HUD", config.partyHudEnabled,
            val -> config.partyHudEnabled = val,
            () -> {
                config.partyHudEnabled = true;
                refreshValues();
            },
            "Show the party member status HUD on screen when you're in a party");

        // Show Self
        addToggleSetting(centerX - buttonWidth / 2, startY + spacing, "Show Self", config.partyHudShowSelf,
            val -> config.partyHudShowSelf = val,
            () -> {
                config.partyHudShowSelf = true;
                refreshValues();
            },
            "Include your own health and shield in the party HUD display");

        // Position X (Slider)
        addIntSlider(centerX - buttonWidth / 2, startY + spacing * 2, "Position X", config.partyHudX, -1, this.width,
            val -> config.partyHudX = val,
            () -> config.partyHudX = -1,
            val -> val < 0 ? "Auto" : String.valueOf(val),
            "Horizontal position of the HUD on screen (-1 = auto)");

        // Position Y (Slider)
        addIntSlider(centerX - buttonWidth / 2, startY + spacing * 3, "Position Y", config.partyHudY, 0, this.height,
            val -> config.partyHudY = val,
            () -> config.partyHudY = 190,
            String::valueOf,
            "Vertical position of the HUD on screen (190 = default)");

        // HUD Size (Slider)
        addFloatSlider(centerX - buttonWidth / 2, startY + spacing * 4, "HUD Size", config.partyHudSize, 0.5f, 2.0f,
            val -> config.partyHudSize = val,
            () -> config.partyHudSize = 1.0f,
            val -> String.format("%.1fx", val),
            "Scale the entire HUD size (0.5x = half, 2.0x = double)");

        // Max Rows (Slider)
        addIntSlider(centerX - buttonWidth / 2, startY + spacing * 5, "Max Rows", config.partyHudMaxRows, 1, 10,
            val -> config.partyHudMaxRows = val,
            () -> config.partyHudMaxRows = 5,
            String::valueOf,
            "Maximum number of party members to display in each column");

        // Max Columns (Slider)
        addIntSlider(centerX - buttonWidth / 2, startY + spacing * 6, "Max Columns", config.partyHudMaxColumns, 1, 2,
            val -> config.partyHudMaxColumns = val,
            () -> config.partyHudMaxColumns = 2,
            String::valueOf,
            "Maximum number of columns for the party HUD layout");
    }

    private void initControlsTab() {
        int centerX = this.width / 2;
        int startY = 70;
        int buttonWidth = 200;

        // Keybind button
        addKeybindButton(centerX - buttonWidth / 2, startY, ModKeybinds.TOGGLE_ENCHANTMENT_DISABLE,
            "Key to toggle enchantment effects on and off");
    }

    private void addToggleSetting(int x, int y, String label, boolean currentValue, Consumer<Boolean> setter, 
                                 Runnable resetAction, String tooltip) {
        int buttonWidth = 200;
        int resetWidth = 60;
        int spacing = 10;

        CyclingButtonWidget<Boolean> button = CyclingButtonWidget.onOffBuilder(currentValue)
            .build(x, y, buttonWidth, 20, Text.literal(label), (btn, value) -> {
                setter.accept(value);
                ModConfig.save();
            });
        
        button.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(button);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            resetAction.run();
            ModConfig.save();
        }).dimensions(x + buttonWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default")));
        this.addDrawableChild(resetButton);
    }

    private void addIntButtonSetting(int x, int y, String label, int currentValue, Consumer<Integer> setter,
                                    Runnable resetAction, java.util.function.Function<Integer, String> valueFormatter,
                                    String tooltip) {
        int buttonWidth = 200;
        int resetWidth = 60;
        int spacing = 10;

        String valueText = valueFormatter.apply(currentValue);
        
        ButtonWidget valueButton = ButtonWidget.builder(
            Text.literal(label + ": " + valueText),
            btn -> {
                int newValue;
                if (label.equals("Position X")) {
                    newValue = (currentValue + 50) % 1000;
                    if (newValue > 800) newValue = -1;
                } else if (label.equals("Position Y")) {
                    newValue = (currentValue + 50) % 600;
                } else if (label.equals("Max Rows")) {
                    newValue = currentValue >= 10 ? 1 : currentValue + 1;
                } else if (label.equals("Max Columns")) {
                    newValue = currentValue >= 2 ? 1 : currentValue + 1;
                } else {
                    newValue = currentValue + 1;
                }
                setter.accept(newValue);
                ModConfig.save();
                this.clearAndInit();
            }
        ).dimensions(x, y, buttonWidth, 20).build();
        
        valueButton.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(valueButton);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            resetAction.run();
            ModConfig.save();
            this.clearAndInit();
        }).dimensions(x + buttonWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default")));
        this.addDrawableChild(resetButton);
    }

    private void addFloatButtonSetting(int x, int y, String label, float currentValue, Consumer<Float> setter,
                                      Runnable resetAction, java.util.function.Function<Float, String> valueFormatter,
                                      String tooltip) {
        int buttonWidth = 200;
        int resetWidth = 60;
        int spacing = 10;

        String valueText = valueFormatter.apply(currentValue);
        
        ButtonWidget valueButton = ButtonWidget.builder(
            Text.literal(label + ": " + valueText),
            btn -> {
                float newValue = currentValue >= 2.0f ? 0.5f : currentValue + 0.25f;
                setter.accept(newValue);
                ModConfig.save();
                this.clearAndInit();
            }
        ).dimensions(x, y, buttonWidth, 20).build();
        
        valueButton.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(valueButton);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            resetAction.run();
            ModConfig.save();
            this.clearAndInit();
        }).dimensions(x + buttonWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default")));
        this.addDrawableChild(resetButton);
    }

    private void addKeybindButton(int x, int y, KeyBinding keyBinding, String tooltip) {
        int buttonWidth = 200;
        int resetWidth = 60;
        int spacing = 10;

        ButtonWidget button = ButtonWidget.builder(
            getKeybindText(keyBinding),
            btn -> {
                if (this.client != null) {
                    this.client.setScreen(new KeybindScreen(this, keyBinding));
                }
            }
        ).dimensions(x, y, buttonWidth, 20).build();

        button.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(button);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            keyBinding.setBoundKey(keyBinding.getDefaultKey());
            button.setMessage(getKeybindText(keyBinding));
        }).dimensions(x + buttonWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default keybind")));
        this.addDrawableChild(resetButton);
    }

    private Text getKeybindText(KeyBinding keyBinding) {
        return Text.literal("Toggle Enchantment: ").append(keyBinding.getBoundKeyLocalizedText());
    }

    private void refreshValues() {
        this.clearAndInit();
    }

    private void addIntSlider(int x, int y, String label, int currentValue, int min, int max,
                              Consumer<Integer> setter, Runnable resetAction, Function<Integer, String> formatter,
                              String tooltip) {
        int sliderWidth = 150;
        int resetWidth = 50;
        int spacing = 10;
        double range = max - min;
        double progress = range == 0 ? 0 : (double) (currentValue - min) / range;

        SliderWidget slider = new SliderWidget(x, y, sliderWidth, 20, 
            Text.literal(label + ": " + formatter.apply(currentValue)), progress) {
            @Override
            protected void updateMessage() {
                int value = min + (int) Math.round(this.value * range);
                this.setMessage(Text.literal(label + ": " + formatter.apply(value)));
            }

            @Override
            protected void applyValue() {
                int value = min + (int) Math.round(this.value * range);
                setter.accept(value);
                ModConfig.save();
            }
        };

        slider.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(slider);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            resetAction.run();
            ModConfig.save();
            this.clearAndInit();
        }).dimensions(x + sliderWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default")));
        this.addDrawableChild(resetButton);
    }

    private void addFloatSlider(int x, int y, String label, float currentValue, float min, float max,
                                Consumer<Float> setter, Runnable resetAction, Function<Float, String> formatter,
                                String tooltip) {
        int sliderWidth = 150;
        int resetWidth = 50;
        int spacing = 10;
        double range = max - min;
        double progress = range == 0 ? 0 : (currentValue - min) / range;

        SliderWidget slider = new SliderWidget(x, y, sliderWidth, 20,
            Text.literal(label + ": " + formatter.apply(currentValue)), progress) {
            @Override
            protected void updateMessage() {
                float value = (float) (min + this.value * range);
                // Round to nearest 0.05 to avoid floating point issues
                value = Math.round(value * 20) / 20.0f;
                this.setMessage(Text.literal(label + ": " + formatter.apply(value)));
            }

            @Override
            protected void applyValue() {
                float value = (float) (min + this.value * range);
                // Round to nearest 0.05
                value = Math.round(value * 20) / 20.0f;
                setter.accept(value);
                ModConfig.save();
            }
        };

        slider.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.addDrawableChild(slider);

        ButtonWidget resetButton = ButtonWidget.builder(Text.literal("Reset"), btn -> {
            resetAction.run();
            ModConfig.save();
            this.clearAndInit();
        }).dimensions(x + sliderWidth + spacing, y, resetWidth, 20).build();
        
        resetButton.setTooltip(Tooltip.of(Text.literal("Reset to default")));
        this.addDrawableChild(resetButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        
        // Tab underline for selected tab
        int tabCount = Tab.values().length;
        int tabWidth = this.width / tabCount;
        int selectedTabIndex = currentTab.ordinal();
        context.fill(selectedTabIndex * tabWidth, 53, (selectedTabIndex + 1) * tabWidth, 55, 0xFFFFFFFF);
        
        // Tab description
        String tabDescription;
        switch (currentTab) {
            case COMBAT:
                tabDescription = "Configure damage and healing number displays";
                break;
            case PARTY_HUD:
                tabDescription = "Configure party member status display";
                break;
            case CONTROLS:
                tabDescription = "Configure keybindings for mod features";
                break;
            default:
                tabDescription = "";
        }
        
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(tabDescription), this.width / 2, 60, 0xAAAAAA);
    }

    @Override
    public void close() {
        ModConfig.save();
        if (this.client != null && this.client.options != null) {
            this.client.options.write();
        }
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}

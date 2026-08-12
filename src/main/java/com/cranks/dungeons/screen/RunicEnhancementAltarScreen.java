package com.cranks.dungeons.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.block.entity.RunicEnhancementAltarBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RunicEnhancementAltarScreen extends HandledScreen<RunicEnhancementAltarScreenHandler> {
    private static final Identifier STANDARD_TEXTURE = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui.png");

    private static final Identifier[] TEXTURES = {
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_1.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_2.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_3.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_4.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_5.png"),
            Identifier.of(CranksDungeons.MOD_ID, "textures/gui/runic_altar/runic_enhancement_altar_gui_6.png")
    };

    private int animationTick = 0;

    public RunicEnhancementAltarScreen(RunicEnhancementAltarScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 170; // updated container height
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = 8;
        this.titleY = 6;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        animationTick++;
    }



    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        boolean hasEquipment = !handler.getSlot(RunicEnhancementAltarBlockEntity.EQUIPMENT_SLOT).getStack().isEmpty();

        Identifier textureToUse;

        if (hasEquipment) {
            textureToUse = STANDARD_TEXTURE;
            animationTick = 0; // reset so it starts from frame 1 when item is removed
        } else {
            // Cycle through the 6 textures (1 frame every 30 ticks / 1.5 seconds)
            int frameIndex = (animationTick / 30) % TEXTURES.length;
            textureToUse = TEXTURES[frameIndex];
        }

        context.drawTexture(
                textureToUse,
                x, y,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                256, 256
        );

        // Draw hover button
        int buttonX = x + 68;
        int buttonY = y + 35;
        if (mouseX >= buttonX && mouseX <= buttonX + 26 && mouseY >= buttonY && mouseY <= buttonY + 19) {
            context.drawTexture(STANDARD_TEXTURE, buttonX, buttonY, 176, 0, 26, 19, 256, 256);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int buttonX = this.x + 68;
        int buttonY = this.y + 35;
        if (button == 0 && mouseX >= buttonX && mouseX <= buttonX + 26 && mouseY >= buttonY && mouseY <= buttonY + 19) {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 0);
                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Empty to prevent default title rendering
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }


}
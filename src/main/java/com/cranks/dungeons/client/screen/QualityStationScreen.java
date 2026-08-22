package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.item.DeepslateSigilItem;
import com.cranks.dungeons.item.QualityMaterialItem;
import com.cranks.dungeons.screen.QualityStationScreenHandler;
import com.cranks.dungeons.block.entity.QualityStationBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class QualityStationScreen extends HandledScreen<QualityStationScreenHandler> {
    // Static textures
    private static final Identifier TEXTURE_DEFAULT = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station.png");
    private static final Identifier TEXTURE_MARTIAL = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_martial.png");
    private static final Identifier TEXTURE_ARMOR = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_armor.png");
    private static final Identifier TEXTURE_ENCHANTMENT = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_enchantment.png");

    private static final Identifier FRAME_1 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_1.png");
    private static final Identifier FRAME_2 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_2.png");
    private static final Identifier FRAME_3 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_3.png");
    private static final Identifier FRAME_4 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_4.png");
    private static final Identifier FRAME_5 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_5.png");
    private static final Identifier FRAME_6 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_6.png");
    private static final Identifier FRAME_7 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_7.png");
    private static final Identifier FRAME_8 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_8.png");
    private static final Identifier FRAME_9 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_9.png");
    private static final Identifier FRAME_10 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_10.png");
    private static final Identifier FRAME_11 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_11.png");
    private static final Identifier FRAME_12 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_12.png");
    private static final Identifier FRAME_13 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_13.png");
    private static final Identifier FRAME_14 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_14.png");
    private static final Identifier FRAME_15 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_15.png");
    private static final Identifier FRAME_16 = Identifier.of(CranksDungeons.MOD_ID, "textures/gui/quality_station/quality_station_16.png");

    // All frames 1 to 16 (when no items are placed)
    private static final Identifier[] ALL_FRAMES = new Identifier[] {
        FRAME_1, FRAME_2, FRAME_3, FRAME_4, FRAME_5, FRAME_6, FRAME_7, FRAME_8,
        FRAME_9, FRAME_10, FRAME_11, FRAME_12, FRAME_13, FRAME_14, FRAME_15, FRAME_16
    };

    // Deepslate sigil frames 9 to 16 (when only deepslate sigil is placed)
    private static final Identifier[] SIGIL_FRAMES = new Identifier[] {
        FRAME_9, FRAME_10, FRAME_11, FRAME_12, FRAME_13, FRAME_14, FRAME_15, FRAME_16
    };

    // Armourer's scrap frames 2 to 6 (when only armourer's scrap is placed)
    private static final Identifier[] ARMOR_FRAMES = new Identifier[] {
        FRAME_2, FRAME_3, FRAME_4, FRAME_5, FRAME_6
    };

    // Blacksmith's whetstone frames 1, 7, 8 (when only blacksmith's whetstone is placed)
    private static final Identifier[] WHETSTONE_FRAMES = new Identifier[] {
        FRAME_1, FRAME_7, FRAME_8
    };

    private int animationTick = 0;

    public QualityStationScreen(QualityStationScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        animationTick++;
    }

    private boolean isTool(EquipmentType type) {
        return type == EquipmentType.AXE || type == EquipmentType.PICKAXE || 
               type == EquipmentType.SHOVEL || type == EquipmentType.HOE;
    }

    private boolean isMartialWeapon(EquipmentType type) {
        return type == EquipmentType.SWORD || type == EquipmentType.SPEAR || 
               type == EquipmentType.MACE || type == EquipmentType.TRIDENT || 
               type == EquipmentType.BOW || type == EquipmentType.CROSSBOW;
    }

    private boolean isArmorPiece(EquipmentType type) {
        return type == EquipmentType.HELMET || type == EquipmentType.CHESTPLATE || 
               type == EquipmentType.LEGGINGS || type == EquipmentType.BOOTS || 
               type == EquipmentType.SHIELD || type == EquipmentType.ELYTRA;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        ItemStack equipmentStack = handler.getSlot(QualityStationBlockEntity.EQUIPMENT_SLOT).getStack();
        ItemStack materialStack = handler.getSlot(QualityStationBlockEntity.MATERIAL_SLOT).getStack();

        boolean hasEquipment = !equipmentStack.isEmpty();
        boolean hasMaterial = !materialStack.isEmpty();

        Identifier currentTexture;

        if (!hasEquipment && !hasMaterial) {
            // 1. No items placed: loops from 1 to 16
            int frameIndex = (animationTick / 30) % ALL_FRAMES.length;
            currentTexture = ALL_FRAMES[frameIndex];
        } else if (hasEquipment && !hasMaterial) {
            // 2. Only equipment item is placed on the right
            Optional<EquipmentType> typeOpt = EquipmentType.getTypeForItem(equipmentStack);
            if (typeOpt.isPresent()) {
                EquipmentType type = typeOpt.get();
                if (isTool(type)) {
                    // Tools only show the enchantment texture with no loop
                    currentTexture = TEXTURE_ENCHANTMENT;
                } else {
                    Identifier partnerTexture = isArmorPiece(type) ? TEXTURE_ARMOR : TEXTURE_MARTIAL;
                    int frame = (animationTick / 30) % 2;
                    currentTexture = (frame == 0) ? partnerTexture : TEXTURE_ENCHANTMENT;
                }
            } else {
                currentTexture = TEXTURE_DEFAULT;
            }
        } else if (!hasEquipment && hasMaterial) {
            // 3. Only material is placed on the left
            if (materialStack.getItem() instanceof DeepslateSigilItem) {
                int frameIndex = (animationTick / 30) % SIGIL_FRAMES.length;
                currentTexture = SIGIL_FRAMES[frameIndex];
            } else if (materialStack.getItem() instanceof QualityMaterialItem qualityItem) {
                if (qualityItem.getMaterialType() == QualityMaterialItem.Type.ARMOR) {
                    int frameIndex = (animationTick / 30) % ARMOR_FRAMES.length;
                    currentTexture = ARMOR_FRAMES[frameIndex];
                } else if (qualityItem.getMaterialType() == QualityMaterialItem.Type.WEAPON) {
                    int frameIndex = (animationTick / 30) % WHETSTONE_FRAMES.length;
                    currentTexture = WHETSTONE_FRAMES[frameIndex];
                } else {
                    int frameIndex = (animationTick / 30) % ALL_FRAMES.length;
                    currentTexture = ALL_FRAMES[frameIndex];
                }
            } else {
                int frameIndex = (animationTick / 30) % ALL_FRAMES.length;
                currentTexture = ALL_FRAMES[frameIndex];
            }
        } else {
            // 4. Both items placed: static default texture without any looping
            currentTexture = TEXTURE_DEFAULT;
        }

        context.drawTexture(
                currentTexture,
                x, y,
                0.0f, 0.0f,
                this.backgroundWidth,
                this.backgroundHeight,
                256,
                256
        );

        int progress = handler.getScaledProgress();
        if (progress > 0) {
            context.drawTexture(
                    currentTexture,
                    x + 121, y + 48,
                    176.0f, 0.0f,
                    progress,
                    16,
                    256,
                    256
            );
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Empty to prevent default title rendering
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenStatsMixin {

    @Unique
    private boolean statsVisible = false;
    @Unique
    private int scrollOffset = 0;
    @Unique
    private int maxScrollOffset = 0;
    @Unique
    private boolean draggingScrollbar = false;
    @Unique
    private boolean lastClickState = false;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderStatsPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        try {
            int x = ((HandledScreenAccessor) screen).getX();
            int y = ((HandledScreenAccessor) screen).getY();
            int backgroundWidth = ((HandledScreenAccessor) screen).getBackgroundWidth();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            // --- TOGGLE BUTTON (Vanilla style) ---
            int buttonX = x + backgroundWidth - 20;
            int buttonY = y + 6;
            int buttonWidth = 14;
            int buttonHeight = 14;

            boolean isHovering = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                    mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

            boolean leftClick = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                    MinecraftClient.getInstance().getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

            // Toggle on click
            if (isHovering && leftClick && !lastClickState && !draggingScrollbar) {
                statsVisible = !statsVisible;
                scrollOffset = 0;
            }
            lastClickState = leftClick;

            // Draw button with vanilla 3D style, rounded corners, and black outline
            int buttonColor = isHovering ? 0xFF8080FF : 0xFFC6C6C6;

            // Black outline with rounded corners (like recipe book button)
            context.fill(buttonX + 1, buttonY, buttonX + buttonWidth - 1, buttonY + buttonHeight, 0xFF000000); // Vertical
            context.fill(buttonX, buttonY + 1, buttonX + buttonWidth, buttonY + buttonHeight - 1, 0xFF000000); // Horizontal

            // Main button background
            context.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 1, buttonY + buttonHeight - 1, buttonColor);

            // White border (top/left) - single pixel with rounded corners
            context.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 2, buttonY + 2, 0xFFFFFFFF); // Top
            context.fill(buttonX + 1, buttonY + 1, buttonX + 2, buttonY + buttonHeight - 2, 0xFFFFFFFF); // Left

            // Dark gray border (bottom/right) - single pixel with rounded corners
            context.fill(buttonX + 2, buttonY + buttonHeight - 2, buttonX + buttonWidth - 1, buttonY + buttonHeight - 1, 0xFF555555); // Bottom
            context.fill(buttonX + buttonWidth - 2, buttonY + 2, buttonX + buttonWidth - 1, buttonY + buttonHeight - 2, 0xFF555555); // Right

            // Draw larger arrow symbol on button, centered
            String arrow = statsVisible ? "◀" : "▶";
            int arrowWidth = tr.getWidth(arrow);
            // Adjust X position for right arrow to center it better
            int arrowX = buttonX + (buttonWidth - arrowWidth) / 2 + (statsVisible ? 0 : 1);
            int arrowY = buttonY + (buttonHeight - 8) / 2;
            context.drawText(tr, arrow, arrowX, arrowY, 0xFF404040, false);

            if (!statsVisible) return;

            // --- PANEL SETUP (Vanilla GUI style with rounded corners) ---
            int panelX = x + backgroundWidth + 2; // 2 pixels of space
            int panelY = y;
            int panelWidth = 147;
            int panelHeight = 166;

            // Draw panel with rounded corners (vanilla Minecraft style)
            // Outer black border with LARGER rounded corners (cut 3 pixels diagonally from each corner)
            context.fill(panelX + 3, panelY, panelX + panelWidth - 3, panelY + panelHeight, 0xFF000000); // Main vertical (starts/ends 3 pixels from edge)
            context.fill(panelX, panelY + 3, panelX + panelWidth, panelY + panelHeight - 3, 0xFF000000); // Main horizontal (starts/ends 3 pixels from edge)
            // Corner L-shapes for bigger radius
            context.fill(panelX, panelY + 2, panelX + 1, panelY + 3, 0xFF000000); // Top-left L
            context.fill(panelX + 1, panelY + 1, panelX + 2, panelY + 2, 0xFF000000);
            context.fill(panelX + 1, panelY + 2, panelX + 2, panelY + 3, 0xFFFFFFFF);
            context.fill(panelX + 2, panelY, panelX + 3, panelY + 1, 0xFF000000);
            context.fill(panelX + 2, panelY + 1, panelX + 3, panelY + 2, 0xFFFFFFFF);
            context.fill(panelX + panelWidth - 2, panelY + 2, panelX + panelWidth - 1, panelY + 3, 0xFF000000); // Top-right L
            context.fill(panelX + panelWidth - 3, panelY + 1, panelX + panelWidth - 2, panelY + 2, 0xFF000000);
            context.fill(panelX + 1, panelY + panelHeight - 3, panelX + 2, panelY + panelHeight - 2, 0xFF000000); // Bottom-left L
            context.fill(panelX + 2, panelY + panelHeight - 2, panelX + 3, panelY + panelHeight - 1, 0xFF000000);
            // Bottom-right mirrored corner (L-shape and highlights)
            context.fill(panelX + panelWidth - 1, panelY + panelHeight - 3, panelX + panelWidth, panelY + panelHeight - 2, 0xFF000000); // Bottom-right outer L (vertical bit)
            context.fill(panelX + panelWidth - 2, panelY + panelHeight - 2, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF000000); // Bottom-right outer L (diagonal bit)
            context.fill(panelX + panelWidth - 2, panelY + panelHeight - 3, panelX + panelWidth - 1, panelY + panelHeight - 2, 0xFF555555); // Inner shadow pixel 1
            context.fill(panelX + panelWidth - 3, panelY + panelHeight - 1, panelX + panelWidth - 2, panelY + panelHeight, 0xFF000000); // Bottom-right outer L (horizontal bit)
            context.fill(panelX + panelWidth - 3, panelY + panelHeight - 2, panelX + panelWidth - 2, panelY + panelHeight - 1, 0xFF555555); // Inner shadow pixel 2

            // Main gray background
            context.fill(panelX + 3, panelY + 1, panelX + panelWidth - 3, panelY + panelHeight - 1, 0xFFC6C6C6);
            context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFFC6C6C6);
            context.fill(panelX + 1, panelY + 3, panelX + panelWidth - 1, panelY + panelHeight - 3, 0xFFC6C6C6);

            // White borders (top/left)
            // Outer white border with larger rounded corners
            context.fill(panelX + 3, panelY + 1, panelX + panelWidth - 3, panelY + 2, 0xFFFFFFFF); // Top outer
            context.fill(panelX + 2, panelY + 2, panelX + 3, panelY + 3, 0xFFFFFFFF); // Top-left corner piece
            context.fill(panelX + panelWidth - 3, panelY + 2, panelX + panelWidth - 2, panelY + 3, 0xFFFFFFFF); // Top-right corner piece
            context.fill(panelX + 1, panelY + 3, panelX + 2, panelY + panelHeight - 3, 0xFFFFFFFF); // Left outer
            context.fill(panelX + 2, panelY + panelHeight - 3, panelX + 3, panelY + panelHeight - 2, 0xFFFFFFFF); // Bottom-left corner piece

            // Inner white border
            context.fill(panelX + 3, panelY + 2, panelX + panelWidth - 3, panelY + 3, 0xFFFFFFFF); // Top inner
            context.fill(panelX + 2, panelY + 3, panelX + 3, panelY + panelHeight - 3, 0xFFFFFFFF); // Left inner

            // Gray borders (bottom/right)
            // Outer gray border with larger rounded corners
            context.fill(panelX + panelWidth - 2, panelY + 3, panelX + panelWidth - 1, panelY + panelHeight - 3, 0xFF555555); // Right outer
            context.fill(panelX + panelWidth - 3, panelY + 2, panelX + panelWidth - 2, panelY + 3, 0xFF555555); // Top-right corner piece
            context.fill(panelX + panelWidth - 3, panelY + panelHeight - 3, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFF555555); // Bottom-right corner piece
            context.fill(panelX + 3, panelY + panelHeight - 2, panelX + panelWidth - 3, panelY + panelHeight - 1, 0xFF555555); // Bottom outer
            context.fill(panelX + 2, panelY + panelHeight - 3, panelX + 3, panelY + panelHeight - 2, 0xFF555555); // Bottom-left corner piece

            // Inner gray border
            context.fill(panelX + panelWidth - 3, panelY + 3, panelX + panelWidth - 2, panelY + panelHeight - 3, 0xFF555555); // Right inner
            context.fill(panelX + 3, panelY + panelHeight - 3, panelX + panelWidth - 3, panelY + panelHeight - 2, 0xFF555555); // Bottom inner

            context.fill(panelX + 3, panelY + 3, panelX + 4, panelY + 4, 0xFFFFFFFF);
            context.fill(panelX + panelWidth - 4, panelY + panelHeight - 4, panelX + panelWidth - 3, panelY + panelHeight - 3, 0xFF555555);

            // Transition pixels at top-right and bottom-left corners
            context.fill(panelX + panelWidth - 3, panelY + 2, panelX + panelWidth - 2, panelY + 3, 0xFF8B8B8B); // Top-right transition
            context.fill(panelX + 2, panelY + panelHeight - 3, panelX + 3, panelY + panelHeight - 2, 0xFF8B8B8B); // Bottom-left transition

            var player = MinecraftClient.getInstance().player;
            if (player == null) return;

            context.enableScissor(panelX + 4, panelY + 4, panelX + panelWidth - 4, panelY + panelHeight - 4);

            int curY = panelY + 8 - scrollOffset;

            // --- CATEGORY 1: COMBAT ---
            context.drawText(tr, Text.literal("⚔ Combat"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Manual Attack Damage: Base + Weapon + Chestplate
            double finalAtkDmg = player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
            finalAtkDmg += getModifierFromStack(player.getMainHandStack(), EntityAttributes.ATTACK_DAMAGE);
            finalAtkDmg += getModifierFromStack(player.getEquippedStack(EquipmentSlot.CHEST), EntityAttributes.ATTACK_DAMAGE);
            finalAtkDmg += player.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE);

            curY = drawStat(context, tr, panelX, curY, "Attack Damage", finalAtkDmg, "");
            curY = drawStat(context, tr, panelX, curY, "Attack Speed", player.getAttributeValue(EntityAttributes.ATTACK_SPEED), "");
            curY = drawStat(context, tr, panelX, curY, "Crit Chance", player.getAttributeValue(ModAttributes.CRIT_CHANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Attack Range", player.getAttributeValue(ModAttributes.ATTACK_RANGE), " blocks");
            curY = drawStat(context, tr, panelX, curY, "Knockback", player.getAttributeValue(ModAttributes.KNOCKBACK) * 100, "%");
            curY += 6;

            // --- CATEGORY 2: ELEMENTAL ---
            context.drawText(tr, Text.literal("✨ Elemental"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Fire Damage", player.getAttributeValue(ModAttributes.FIRE_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Burn Chance", player.getAttributeValue(ModAttributes.CHANCE_TO_BURN) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Cold Damage", player.getAttributeValue(ModAttributes.COLD_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Lightning Dmg", player.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Void Damage", player.getAttributeValue(ModAttributes.VOID_DAMAGE), "");
            curY += 6;

            // --- CATEGORY 3: SURVIVAL ---
            context.drawText(tr, Text.literal("❤ Survival"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP");
            curY = drawStat(context, tr, panelX, curY, "Regeneration", player.getAttributeValue(ModAttributes.LIFE_REGENERATION), " HP/s");
            curY = drawStat(context, tr, panelX, curY, "Life Steal", player.getAttributeValue(ModAttributes.LIFE_STEAL) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "");
            curY = drawStat(context, tr, panelX, curY, "Toughness", player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS), "");
            curY += 6;

            // --- CATEGORY 4: RESISTANCES ---
            context.drawText(tr, Text.literal("🛡 Resistances"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Knockback Res", player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%");
            curY += 6;

            // --- CATEGORY 5: UTILITY ---
            context.drawText(tr, Text.literal("✦ Utility"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Move Speed", player.getAttributeValue(ModAttributes.MOVEMENT_SPEED) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Luck", player.getAttributeValue(ModAttributes.LUCK), "");
            curY = drawStat(context, tr, panelX, curY, "Exp Bonus", player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%");

            // Mining sub-section
            curY += 4;
            context.drawText(tr, Text.literal("Mining:"), panelX + 8, curY, 0xFF555555, false);
            curY += 10;
            ItemStack tool = player.getMainHandStack();
            // 1. Get the base reach (usually 3.0 or 4.5)
            double baseRange = player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE);

            double weaponRangeBonus = EquipmentStatApplier.getItemStatValue(tool, "breaking_range");

            double totalRangeBonus = baseRange + weaponRangeBonus;
            curY = drawStat(context, tr, panelX, curY, "Speed", EquipmentStatApplier.getItemStatValue(tool, "mining_efficiency") * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Ore Fortune", EquipmentStatApplier.getItemStatValue(tool, "fortune") * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Range", totalRangeBonus, " blocks");

            int totalContentHeight = (curY + scrollOffset) - (panelY + 8);
            maxScrollOffset = Math.max(0, totalContentHeight - panelHeight + 16);
            context.disableScissor();

            // --- SCROLLBAR LOGIC (Vanilla 3D style) ---
            if (maxScrollOffset > 0) {
                renderScrollbar(context, tr, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, leftClick);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        String valStr = String.format("%.1f", val);
        String text = name + ": " + valStr + suffix;
        // Draw text once, clean and readable
        context.drawText(tr, Text.literal(text), x + 12, y, 0xFF404040, false);
        return y + 10;
    }

    @Unique
    private double getModifierFromStack(ItemStack stack, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        double val = 0;
        var comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp != null) {
            for (var entry : comp.modifiers()) {
                if (entry.attribute().equals(attr)) val += entry.modifier().value();
            }
        }
        return val;
    }

    @Unique
    private void renderScrollbar(DrawContext context, TextRenderer tr, int panelX, int panelY, int panelWidth, int panelHeight, int mouseX, int mouseY, boolean leftClick) {
        int sbX = panelX + panelWidth - 12;
        int sbY = panelY + 18;
        int sbHeight = panelHeight - 36;

        // Scrollbar track (darker gray like vanilla)
        context.fill(sbX, sbY, sbX + 6, sbY + sbHeight, 0xFF8B8B8B);

        // Scrollbar thumb
        float scrollPercent = (float) scrollOffset / maxScrollOffset;
        int thumbHeight = Math.max(20, sbHeight / 4);
        int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

        // Thumb with vanilla 3D effect
        context.fill(sbX, thumbY, sbX + 6, thumbY + thumbHeight, 0xFFC6C6C6);

        // Light border (top/left)
        context.fill(sbX, thumbY, sbX + 6, thumbY + 1, 0xFFFFFFFF);
        context.fill(sbX, thumbY, sbX + 1, thumbY + thumbHeight, 0xFFFFFFFF);

        // Dark border (bottom/right)
        context.fill(sbX + 5, thumbY + 1, sbX + 6, thumbY + thumbHeight, 0xFF555555);
        context.fill(sbX + 1, thumbY + thumbHeight - 1, sbX + 6, thumbY + thumbHeight, 0xFF555555);

        // Handle dragging
        if (leftClick) {
            if (!draggingScrollbar && mouseX >= sbX && mouseX <= sbX + 6 && mouseY >= sbY && mouseY <= sbY + sbHeight) {
                draggingScrollbar = true;
            }
            if (draggingScrollbar) {
                float newPercent = Math.max(0, Math.min(1, (float) (mouseY - sbY - thumbHeight / 2) / (sbHeight - thumbHeight)));
                scrollOffset = (int) (newPercent * maxScrollOffset);
            }
        } else {
            draggingScrollbar = false;
        }
    }
}
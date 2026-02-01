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

    @Inject(method = "render", at = @At("TAIL"))
    private void renderStatsPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        try {
            int x = ((HandledScreenAccessor) screen).getX();
            int y = ((HandledScreenAccessor) screen).getY();
            int backgroundWidth = ((HandledScreenAccessor) screen).getBackgroundWidth();

            // --- TOGGLE BUTTON ---
            int buttonX = x + backgroundWidth - 20;
            int buttonY = y + 6;
            int buttonWidth = 14;
            int buttonHeight = 14;

            boolean isHovering = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                    mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

            int buttonColor = isHovering ? 0xFF666666 : 0xFF444444;
            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);
            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 1, 0xFFAAAAAA);
            context.fill(buttonX, buttonY, buttonX + 1, buttonY + buttonHeight, 0xFFAAAAAA);
            context.fill(buttonX + buttonWidth - 1, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF222222);
            context.fill(buttonX, buttonY + buttonHeight - 1, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF222222);

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            String buttonText = statsVisible ? "◀" : "▶";
            context.drawText(tr, buttonText, buttonX + (buttonWidth - tr.getWidth(buttonText)) / 2, buttonY + (buttonHeight - 8) / 2, 0xFFFFFFFF, false);

            boolean leftClick = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                    MinecraftClient.getInstance().getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

            if (isHovering && leftClick && !draggingScrollbar) {
                statsVisible = !statsVisible;
                scrollOffset = 0;
            }

            if (!statsVisible) return;

            // --- PANEL SETUP ---
            int panelX = x + backgroundWidth;
            int panelY = y;
            int panelWidth = 130;
            int panelHeight = 166;

            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xC0202020);
            int borderColor = 0xFF8B8B8B;
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, borderColor);
            context.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, borderColor);
            context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, borderColor);
            context.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

            var player = MinecraftClient.getInstance().player;
            if (player == null) return;

            context.enableScissor(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1);

            int curY = panelY + 5 - scrollOffset;

            // --- CATEGORY 1: COMBAT (⚔) ---
            context.drawTextWithShadow(tr, Text.literal("⚔ Combat"), panelX + 5, curY, 0xFFFFCC00);
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

            // --- CATEGORY 2: ELEMENTAL (✨) ---
            context.drawTextWithShadow(tr, Text.literal("✨ Elemental"), panelX + 5, curY, 0xFFAA00FF);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Fire Damage", player.getAttributeValue(ModAttributes.FIRE_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Burn Chance", player.getAttributeValue(ModAttributes.CHANCE_TO_BURN) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Cold Damage", player.getAttributeValue(ModAttributes.COLD_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Lightning Dmg", player.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE), "");
            curY = drawStat(context, tr, panelX, curY, "Void Damage", player.getAttributeValue(ModAttributes.VOID_DAMAGE), "");
            curY += 6;

            // --- CATEGORY 3: SURVIVAL (❤️) ---
            context.drawTextWithShadow(tr, Text.literal("❤️ Survival"), panelX + 5, curY, 0xFFFF5555);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP");
            curY = drawStat(context, tr, panelX, curY, "Regeneration", player.getAttributeValue(ModAttributes.LIFE_REGENERATION), " HP/s");
            curY = drawStat(context, tr, panelX, curY, "Life Steal", player.getAttributeValue(ModAttributes.LIFE_STEAL) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "");
            curY = drawStat(context, tr, panelX, curY, "Toughness", player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS), "");
            curY += 6;

            // --- CATEGORY 4: RESISTANCES (🛡) ---
            context.drawTextWithShadow(tr, Text.literal("🛡 Resistances"), panelX + 5, curY, 0xFF5555FF);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "K.B. Res", player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%");
            curY += 6;

            // --- CATEGORY 5: UTILITY (✦) ---
            context.drawTextWithShadow(tr, Text.literal("✦ Utility"), panelX + 5, curY, 0xFF55FF55);
            curY += 11;
            curY = drawStat(context, tr, panelX, curY, "Move Speed", player.getAttributeValue(ModAttributes.MOVEMENT_SPEED) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Luck", player.getAttributeValue(ModAttributes.LUCK), "");
            curY = drawStat(context, tr, panelX, curY, "Exp Bonus", player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%");

            // Mining sub-section
            curY += 4;
            context.drawTextWithShadow(tr, Text.literal("Efficiency:"), panelX + 5, curY, 0xFFAAAAAA);
            curY += 10;
            ItemStack tool = player.getMainHandStack();
            curY = drawStat(context, tr, panelX, curY, "Efficiency", EquipmentStatApplier.getItemStatValue(tool, "mining_efficiency") * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Ore Fortune", EquipmentStatApplier.getItemStatValue(tool, "fortune") * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Breaking Range", EquipmentStatApplier.getItemStatValue(tool, "breaking_range"), " blocks");

            int totalContentHeight = (curY + scrollOffset) - (panelY + 5);
            maxScrollOffset = Math.max(0, totalContentHeight - panelHeight + 10);
            context.disableScissor();

            // --- SCROLLBAR LOGIC ---
            if (maxScrollOffset > 0) {
                renderScrollbar(context, tr, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, leftClick);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        int sbX = panelX + panelWidth - 5;
        int trackH = panelHeight - 40;
        context.fill(sbX, panelY + 20, sbX + 3, panelY + panelHeight - 20, 0xFF404040);

        float scrollPercent = (float) scrollOffset / maxScrollOffset;
        int thumbY = panelY + 20 + (int) (scrollPercent * trackH);
        context.fill(sbX, thumbY, sbX + 3, thumbY + 20, 0xFF8B8B8B);

        if (leftClick) {
            if (!draggingScrollbar && mouseX >= sbX && mouseX <= sbX + 3 && mouseY >= panelY + 20 && mouseY <= panelY + panelHeight - 20) {
                draggingScrollbar = true;
            }
            if (draggingScrollbar) {
                float newPercent = Math.max(0, Math.min(1, (float) (mouseY - panelY - 30) / trackH));
                scrollOffset = (int) (newPercent * maxScrollOffset);
            }
        } else {
            draggingScrollbar = false;
        }
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        String valStr = String.format("%.1f", val);
        context.drawTextWithShadow(tr, Text.literal(name + ": " + valStr + suffix), x + 7, y, 0xFFD0D0D0);
        return y + 9;
    }
}
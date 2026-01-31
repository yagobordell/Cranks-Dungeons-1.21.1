package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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
    private ButtonWidget toggleButton;

    @Unique
    private ButtonWidget scrollUpButton;

    @Unique
    private ButtonWidget scrollDownButton;

    @Unique
    private int scrollOffset = 0;

    @Unique
    private int maxScrollOffset = 0;

    @Unique
    private boolean draggingScrollbar = false;

    @Unique
    private int lastMouseX = -1;

    @Unique
    private int lastMouseY = -1;

    @Inject(method = "init", at = @At("TAIL"))
    private void addStatsButton(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        int x = ((HandledScreenAccessor) screen).getX();
        int y = ((HandledScreenAccessor) screen).getY();

        toggleButton = ButtonWidget.builder(
                Text.literal(statsVisible ? "◀" : "▶"),
                button -> {
                    statsVisible = !statsVisible;
                    button.setMessage(Text.literal(statsVisible ? "◀" : "▶"));
                    scrollOffset = 0;
                    button.setFocused(false); // Remove focus after click
                }
        ).dimensions(x + 160, y + 4, 12, 12).build();

        ((ScreenAccessor) screen).invokeAddDrawableChild(toggleButton);

        // Scroll buttons - initially hidden
        scrollUpButton = ButtonWidget.builder(
                Text.literal("▲"),
                button -> {
                    scrollOffset = Math.max(0, scrollOffset - 15);
                    button.setFocused(false); // Remove focus after click
                }
        ).dimensions(x + 176 + 115, y + 4, 12, 12).build();
        scrollUpButton.visible = false;

        scrollDownButton = ButtonWidget.builder(
                Text.literal("▼"),
                button -> {
                    scrollOffset = Math.min(maxScrollOffset, scrollOffset + 15);
                    button.setFocused(false); // Remove focus after click
                }
        ).dimensions(x + 176 + 115, y + 150, 12, 12).build();
        scrollDownButton.visible = false;

        ((ScreenAccessor) screen).invokeAddDrawableChild(scrollUpButton);
        ((ScreenAccessor) screen).invokeAddDrawableChild(scrollDownButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void updateButtonsBeforeRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Update button visibility at the start of render
        if (scrollUpButton != null) scrollUpButton.visible = statsVisible && maxScrollOffset > 0;
        if (scrollDownButton != null) scrollDownButton.visible = statsVisible && maxScrollOffset > 0;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderStatsPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!statsVisible) return;

        InventoryScreen screen = (InventoryScreen) (Object) this;
        int x = ((HandledScreenAccessor) screen).getX();
        int y = ((HandledScreenAccessor) screen).getY();

        int panelX = x + 176;
        int panelY = y;
        int panelWidth = 130;
        int panelHeight = 166;

        // Draw panel background
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xC0202020);

        // Draw border
        int borderColor = 0xFF8B8B8B;
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, borderColor);
        context.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, borderColor);
        context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, borderColor);
        context.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        // Enable scissor to clip content
        context.enableScissor(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1);

        ItemStack weapon = player.getMainHandStack();
        ItemStack tool = player.getMainHandStack();

        int curY = panelY + 5 - scrollOffset;

        // --- OFFENSIVE STATS ---
        context.drawTextWithShadow(tr, Text.literal("⚔ Offensive"), panelX + 5, curY, 0xFFFF5555);
        curY += 11;

        double baseDamage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        double weaponBonusDmg = EquipmentStatApplier.getItemStatValue(weapon, "bonus_attack_damage");
        double totalBonusDmg = player.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE) + weaponBonusDmg;
        curY = drawStat(context, tr, panelX, curY, "Attack Damage", baseDamage + totalBonusDmg, "");

        double totalSpeed = EquipmentStatApplier.getItemStatValue(player.getMainHandStack(), "attack_speed") + EquipmentStatApplier.getItemStatValue(player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST), "attack_speed");
        curY = drawStat(context, tr, panelX, curY, "Attack Speed", totalSpeed * 100, "%");

        double totalCrit = player.getAttributeValue(ModAttributes.CRIT_CHANCE) + EquipmentStatApplier.getItemStatValue(weapon, "crit_chance");
        curY = drawStat(context, tr, panelX, curY, "Critical Chance", totalCrit * 100, "%");

        curY = drawStat(context, tr, panelX, curY, "Bonus Damage", totalBonusDmg, "");

        double totalFireDmg = player.getAttributeValue(ModAttributes.FIRE_DAMAGE) + EquipmentStatApplier.getItemStatValue(weapon, "fire_damage");
        curY = drawStat(context, tr, panelX, curY, "Fire Damage", totalFireDmg, "");

        double totalColdDmg = player.getAttributeValue(ModAttributes.COLD_DAMAGE) + EquipmentStatApplier.getItemStatValue(weapon, "cold_damage");
        curY = drawStat(context, tr, panelX, curY, "Cold Damage", totalColdDmg, "");

        double totalLightningDmg = player.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE) + EquipmentStatApplier.getItemStatValue(weapon, "lightning_damage");
        curY = drawStat(context, tr, panelX, curY, "Lightning Damage", totalLightningDmg, "");

        double totalVoidDmg = player.getAttributeValue(ModAttributes.VOID_DAMAGE) + EquipmentStatApplier.getItemStatValue(weapon, "void_damage");
        curY = drawStat(context, tr, panelX, curY, "Void Damage", totalVoidDmg, "");

        double totalLifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL) + EquipmentStatApplier.getItemStatValue(weapon, "life_steal");
        curY = drawStat(context, tr, panelX, curY, "Life Steal", totalLifeSteal * 100, "%");

        double totalBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN) + EquipmentStatApplier.getItemStatValue(weapon, "chance_to_burn");
        curY = drawStat(context, tr, panelX, curY, "Burn Chance", totalBurn * 100, "%");

        double totalRange = player.getAttributeValue(ModAttributes.ATTACK_RANGE) + EquipmentStatApplier.getItemStatValue(weapon, "attack_range");
        curY = drawStat(context, tr, panelX, curY, "Attack Range", totalRange, " blocks");

        double totalKnockback = player.getAttributeValue(ModAttributes.KNOCKBACK) + EquipmentStatApplier.getItemStatValue(weapon, "knockback");
        curY = drawStat(context, tr, panelX, curY, "Knockback", totalKnockback * 100, "%");

        curY += 4;

        // --- DEFENSIVE STATS ---
        context.drawTextWithShadow(tr, Text.literal("🛡 Defensive"), panelX + 5, curY, 0xFF5555FF);
        curY += 11;

        curY = drawStat(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP");
        curY = drawStat(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "");

        double toughness = player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
        curY = drawStat(context, tr, panelX, curY, "Armor Toughness", toughness, "");

        double lifeRegen = player.getAttributeValue(ModAttributes.LIFE_REGENERATION);
        curY = drawStat(context, tr, panelX, curY, "Life Regeneration", lifeRegen, " HP/s");

        double fireRes = player.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
        curY = drawStat(context, tr, panelX, curY, "Fire Resistance", fireRes * 100, "%");

        double coldRes = player.getAttributeValue(ModAttributes.COLD_RESISTANCE);
        curY = drawStat(context, tr, panelX, curY, "Cold Resistance", coldRes * 100, "%");

        double lightningRes = player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
        curY = drawStat(context, tr, panelX, curY, "Lightning Resistance", lightningRes * 100, "%");

        double voidRes = player.getAttributeValue(ModAttributes.VOID_RESISTANCE);
        curY = drawStat(context, tr, panelX, curY, "Void Resistance", voidRes * 100, "%");

        double kbRes = player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE);
        curY = drawStat(context, tr, panelX, curY, "Knockback Resistance", kbRes * 100, "%");

        double featherFall = player.getAttributeValue(ModAttributes.FEATHER_FALLING);
        curY = drawStat(context, tr, panelX, curY, "Feather Falling", featherFall * 100, "%");

        curY += 4;

        // --- UTILITY STATS ---
        context.drawTextWithShadow(tr, Text.literal("✦ Utility"), panelX + 5, curY, 0xFF55FF55);
        curY += 11;

        double speed = player.getAttributeValue(ModAttributes.MOVEMENT_SPEED);
        curY = drawStat(context, tr, panelX, curY, "Movement Speed", speed * 100, "%");

        double luck = player.getAttributeValue(ModAttributes.LUCK);
        curY = drawStat(context, tr, panelX, curY, "Luck", luck, "");

        double expBonus = player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS);
        curY = drawStat(context, tr, panelX, curY, "Experience Bonus", expBonus * 100, "%");

        double miningEff = EquipmentStatApplier.getItemStatValue(tool, "mining_efficiency");
        curY = drawStat(context, tr, panelX, curY, "Mining Efficiency", miningEff * 100, "%");

        double fortune = EquipmentStatApplier.getItemStatValue(tool, "fortune");
        curY = drawStat(context, tr, panelX, curY, "Fortune Chance", fortune * 100, "%");

        double precision = EquipmentStatApplier.getItemStatValue(tool, "precision_mining");
        curY = drawStat(context, tr, panelX, curY, "Precision Mining", precision * 100, "%");

        double breakRange = EquipmentStatApplier.getItemStatValue(tool, "breaking_range");
        curY = drawStat(context, tr, panelX, curY, "Breaking Range", breakRange, " blocks");

        // Calculate total content height and max scroll
        int totalContentHeight = (curY + scrollOffset) - (panelY + 5);
        maxScrollOffset = Math.max(0, totalContentHeight - panelHeight + 10);

        context.disableScissor();

        // Draw scrollbar if content is scrollable
        if (maxScrollOffset > 0) {
            int scrollbarX = panelX + panelWidth - 5;
            int scrollbarTrackHeight = panelHeight - 24;

            // Scrollbar track
            context.fill(scrollbarX, panelY + 2, scrollbarX + 3, panelY + panelHeight - 2, 0xFF404040);

            // Scrollbar thumb
            float scrollPercent = (float) scrollOffset / maxScrollOffset;
            int thumbY = panelY + 2 + (int) (scrollPercent * scrollbarTrackHeight);
            context.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + 20, 0xFF8B8B8B);

            // Handle scrollbar interaction - check if mouse is in scrollbar area
            boolean isOverScrollbar = mouseX >= scrollbarX && mouseX <= scrollbarX + 3 &&
                    mouseY >= panelY + 2 && mouseY <= panelY + panelHeight - 2;

            // Get current mouse button state
            boolean leftButtonDown = MinecraftClient.getInstance().mouse.wasLeftButtonClicked();

            // Start dragging on click
            if (leftButtonDown && isOverScrollbar && !draggingScrollbar) {
                // Check if clicked on thumb
                if (mouseY >= thumbY && mouseY <= thumbY + 20) {
                    draggingScrollbar = true;
                } else {
                    // Clicked on track - jump to position
                    int relativeY = mouseY - panelY - 12;
                    float newScrollPercent = Math.max(0, Math.min(1, (float) relativeY / scrollbarTrackHeight));
                    scrollOffset = (int) (newScrollPercent * maxScrollOffset);
                    draggingScrollbar = true;
                }
            }

            // Continue dragging
            if (draggingScrollbar && leftButtonDown) {
                int relativeY = mouseY - panelY - 12;
                float newScrollPercent = Math.max(0, Math.min(1, (float) relativeY / scrollbarTrackHeight));
                scrollOffset = (int) (newScrollPercent * maxScrollOffset);
            }

            // Stop dragging when mouse released
            if (!leftButtonDown) {
                draggingScrollbar = false;
            }
        } else {
            draggingScrollbar = false;
        }

        // Store last mouse position for next frame
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        // Draw scroll buttons AFTER the panel so they appear on top
        if (scrollUpButton != null && scrollDownButton != null) {
            scrollUpButton.visible = statsVisible && maxScrollOffset > 0;
            scrollDownButton.visible = statsVisible && maxScrollOffset > 0;

            if (scrollUpButton.visible) {
                scrollUpButton.render(context, mouseX, mouseY, delta);
            }
            if (scrollDownButton.visible) {
                scrollDownButton.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        String valStr = String.format("%.1f", val);
        context.drawTextWithShadow(tr, Text.literal(name + ": " + valStr + suffix), x + 7, y, 0xFFD0D0D0);
        return y + 9;
    }
}
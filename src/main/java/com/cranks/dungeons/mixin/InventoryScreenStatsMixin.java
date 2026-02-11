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
    private static boolean statsVisible = false;
    @Unique
    private static int scrollOffset = 0;
    @Unique
    private int maxScrollOffset = 0;
    @Unique
    private boolean draggingScrollbar = false;
    @Unique
    private boolean lastClickState = false;
    @Unique
    private String hoveredStat = null;
    @Unique
    private int tooltipX = 0;
    @Unique
    private int tooltipY = 0;

    // Override mouseScrolled to handle scrolling in stats panel
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (statsVisible && maxScrollOffset > 0) {
            InventoryScreen screen = (InventoryScreen) (Object) this;

            try {
                int x = ((HandledScreenAccessor) screen).getX();
                int y = ((HandledScreenAccessor) screen).getY();
                int backgroundWidth = ((HandledScreenAccessor) screen).getBackgroundWidth();

                int panelX = x + backgroundWidth + 2;
                int panelY = y;
                int panelWidth = 147;
                int panelHeight = 166;

                // Check if mouse is over the stats panel
                if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                        mouseY >= panelY && mouseY <= panelY + panelHeight) {

                    // Scroll by 10 pixels per scroll notch
                    scrollOffset -= (int)(verticalAmount * 10);
                    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

                    return true; // Consume the scroll event
                }
            } catch (Exception e) {
                // Ignore errors
            }
        }

        // Call parent's mouseScrolled if we didn't handle it
        return ((net.minecraft.client.gui.screen.ingame.HandledScreen) (Object) this).mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

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
                // Play click sound
                MinecraftClient.getInstance().getSoundManager().play(
                        net.minecraft.client.sound.PositionedSoundInstance.ui(
                                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK,
                                1.0F
                        )
                );
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

            // Reset hovered stat each frame
            hoveredStat = null;

            context.enableScissor(panelX + 4, panelY + 4, panelX + panelWidth - 4, panelY + panelHeight - 4);

            int curY = panelY + 8 - scrollOffset;

            // --- CATEGORY 1: COMBAT ---
            context.drawText(tr, Text.literal("⚔ Combat"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            ItemStack weapon = player.getMainHandStack();
            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

            // Attack Damage with enchantments (including from all armor pieces - netherite gives attack damage!)
            double baseAttackDamage = player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += getModifierFromStack(weapon, EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += getModifierFromStack(chestplate, EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += getModifierFromStack(helmet, EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += getModifierFromStack(leggings, EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += getModifierFromStack(boots, EntityAttributes.ATTACK_DAMAGE);
            baseAttackDamage += player.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE);
            baseAttackDamage += getEnchantmentValue(weapon, "minecraft:attack_damage", 4.0);
            baseAttackDamage += getEnchantmentValue(chestplate, "minecraft:attack_damage", 4.0);

            double attackDamagePercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent");
            double attackDamagePercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent");
            double totalAttackDamagePercent = attackDamagePercentFromWeapon + attackDamagePercentFromChestplate;
            double finalAtkDmg = baseAttackDamage * (1.0 + totalAttackDamagePercent);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Damage", finalAtkDmg, "", mouseX, mouseY, panelX, panelWidth);

            // Attack Speed with enchantments (from weapon and all armor pieces - chainmail gives attack speed!)
            double attackSpeed = player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
            attackSpeed += getEnchantmentValue(weapon, "minecraft:attack_speed", 0.2);
            attackSpeed += getEnchantmentValue(chestplate, "minecraft:attack_speed", 0.2);
            attackSpeed += getEnchantmentValue(helmet, "minecraft:attack_speed", 0.2);
            attackSpeed += getEnchantmentValue(leggings, "minecraft:attack_speed", 0.2);
            attackSpeed += getEnchantmentValue(boots, "minecraft:attack_speed", 0.2);

            // Apply attack speed percentage from runes
            double attackSpeedPercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "attack_speed_percent");
            double attackSpeedPercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "attack_speed_percent");
            double totalAttackSpeedPercent = attackSpeedPercentFromWeapon + attackSpeedPercentFromChestplate;
            double finalAttackSpeed = attackSpeed * (1.0 + totalAttackSpeedPercent);

            // Display as attacks per second (the actual value)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Speed", finalAttackSpeed, "", mouseX, mouseY, panelX, panelWidth);

            // Crit Chance with enchantments
            double critChance = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(chestplate, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Chance", critChance * 100, "%", mouseX, mouseY, panelX, panelWidth);

            // Attack Range with enchantments
            double attackRange = player.getAttributeValue(ModAttributes.ATTACK_RANGE);
            attackRange += getEnchantmentValue(weapon, "minecraft:attack_range", 1.0);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Range", attackRange, " blocks", mouseX, mouseY, panelX, panelWidth);

            // Knockback with enchantments
            double knockback = player.getAttributeValue(ModAttributes.KNOCKBACK);
            knockback += getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.35);
            knockback += getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.35);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback", knockback * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 2: ELEMENTAL ---
            context.drawText(tr, Text.literal("✨ Elemental"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Chance to Burn with enchantments
            double chanceToBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN);
            chanceToBurn += getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.2);
            chanceToBurn += getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.2);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Chance", chanceToBurn * 100, "%", mouseX, mouseY, panelX, panelWidth);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Damage", calculateFinalElementalDamage(player, weapon, chestplate, "fire_damage_percent", ModAttributes.FIRE_DAMAGE, "minecraft:fire_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Damage", calculateFinalElementalDamage(player, weapon, chestplate, "cold_damage_percent", ModAttributes.COLD_DAMAGE, "minecraft:cold_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Damage", calculateFinalElementalDamage(player, weapon, chestplate, "lightning_damage_percent", ModAttributes.LIGHTNING_DAMAGE, "minecraft:lightning_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Damage", calculateFinalElementalDamage(player, weapon, chestplate, "void_damage_percent", ModAttributes.VOID_DAMAGE, "minecraft:void_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 3: SURVIVAL ---
            context.drawText(tr, Text.literal("❤ Survival"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Energy Shield (from gold and chainmail armor!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Energy Shield", player.getAttributeValue(ModAttributes.ENERGY_SHIELD), "", mouseX, mouseY, panelX, panelWidth);

            // Max Health (from diamond armor!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP", mouseX, mouseY, panelX, panelWidth);

            // HP Regeneration (from diamond armor!)
            double hpRegen = player.getAttributeValue(ModAttributes.LIFE_REGENERATION);
            hpRegen += player.getAttributeValue(ModAttributes.LIFE_REGENERATION);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Regeneration", hpRegen, " HP/s", mouseX, mouseY, panelX, panelWidth);

            // Life Steal with enchantments
            double lifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL);
            lifeSteal += getEnchantmentValue(weapon, "minecraft:life_steal", 0.06);
            lifeSteal += getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Life Steal", lifeSteal * 100, "%", mouseX, mouseY, panelX, panelWidth);

            // Evasion Rating (from leather and chainmail armor!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion Rating", player.getAttributeValue(ModAttributes.EVASION_RATING), "", mouseX, mouseY, panelX, panelWidth);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Toughness", player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS), "", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 4: RESISTANCES ---
            context.drawText(tr, Text.literal("🛡 Resistances"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Elemental resistances (copper armor gives all 4!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            // Knockback Resistance (from iron armor!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback Res", player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 5: UTILITY ---
            context.drawText(tr, Text.literal("✦ Utility"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Movement Speed (from leather armor!)
            // Armor adds to EntityAttributes.MOVEMENT_SPEED with small decimals (0.015 = 1.5%)
            double moveSpeed = player.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
            double baseMoveSpeed = 0.1; // Default player base movement speed
            double moveSpeedBonus = (moveSpeed - baseMoveSpeed) * 100; // Convert difference to percentage
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Move Speed", moveSpeedBonus, "%", mouseX, mouseY, panelX, panelWidth);

            // Luck (from gold armor!)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Luck", player.getAttributeValue(EntityAttributes.LUCK), "", mouseX, mouseY, panelX, panelWidth);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Exp Bonus", player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            // Mining sub-section
            curY += 4;
            context.drawText(tr, Text.literal("⚒ Gathering:"), panelX + 8, curY, 0xFF404040, false);
            curY += 10;
            ItemStack tool = player.getMainHandStack();
            double baseRange = player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
            double weaponRangeBonus = EquipmentStatApplier.getItemStatValue(tool, "breaking_range");
            double totalRangeBonus = baseRange + weaponRangeBonus;

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Gathering Speed", player.getAttributeValue(ModAttributes.MINING_EFFICIENCY) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Ore Fortune", player.getAttributeValue(ModAttributes.FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop Fortune", EquipmentStatApplier.getItemStatValue(tool, "crop_fortune") * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Range", totalRangeBonus, " blocks", mouseX, mouseY, panelX, panelWidth);

            int totalContentHeight = (curY + scrollOffset) - (panelY + 8);
            maxScrollOffset = Math.max(0, totalContentHeight - panelHeight + 16);
            int maxScrollOffsetCache = maxScrollOffset; // Cache for mouse scroll handler

            // Clamp scrollOffset to valid range (in case it was changed by mouse wheel)
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

            context.disableScissor();

            // --- SCROLLBAR LOGIC (Vanilla 3D style) ---
            if (maxScrollOffset > 0) {
                renderScrollbar(context, tr, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, leftClick);
            }

            // --- RENDER TOOLTIP AFTER EVERYTHING ELSE ---
            if (hoveredStat != null) {
                renderStatTooltip(context, tr, hoveredStat, tooltipX, tooltipY);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private void renderStatTooltip(DrawContext context, TextRenderer tr, String statName, int x, int y) {
        String description = getStatDescription(statName);
        if (description == null) return;

        Text text = Text.literal(description);
        int textWidth = tr.getWidth(text);

        // Positioning
        int tx = x + 12;
        int ty = y - 12;
        int w = textWidth;
        int h = 8; // standard text height

        // 1. THE OUTER BLACK BOX (The "Shell")
        // This creates the black outline. Notice the notched corners.
        context.fill(tx - 3, ty - 5, tx + w + 3, ty + h + 5, 0xFF000000); // Main vertical
        context.fill(tx - 5, ty - 3, tx + w + 5, ty + h + 3, 0xFF000000); // Main horizontal
        context.fill(tx - 4, ty - 4, tx + w + 4, ty + h + 4, 0xFF000000); // Corner fillers

        // 2. THE INNER DARK BACKGROUND
        context.fill(tx - 4, ty - 3, tx + w + 4, ty + h + 3, 0xF0100010);

        // 3. THE DEEP PURPLE BORDER (The Sheen)
        // Values matched to vanilla modern tooltips
        int pTop = 0x505000FF;    // Deep Purple
        int pBottom = 0x5028007F; // Dark Indigo

        // Left & Right Vertical Gradients
        context.fillGradient(tx - 4, ty - 3, tx - 3, ty + h + 3, pTop, pBottom);
        context.fillGradient(tx + w + 3, ty - 3, tx + w + 4, ty + h + 3, pTop, pBottom);

        // Top & Bottom Horizontal Lines
        context.fill(tx - 3, ty - 4, tx + w + 3, ty - 3, pTop);
        context.fill(tx - 3, ty + h + 3, tx + w + 3, ty + h + 4, pBottom);

        // 4. THE VERTICAL BLACK CAPS (The final detail)
        // These cap the purple gradient on the far left/right
        context.fill(tx - 5, ty - 3, tx - 4, ty + h + 3, 0xFF000000);
        context.fill(tx + w + 4, ty - 3, tx + w + 5, ty + h + 3, 0xFF000000);

        // 5. RENDER TEXT
        context.drawText(tr, text, tx, ty, 0xFFFFFFFF, true);
    }

    @Unique
    private String getStatDescription(String statName) {
        return switch (statName) {
            case "Attack Damage" -> "Physical damage dealt per hit";
            case "Attack Speed" -> "Number of attacks per second";
            case "Crit Chance" -> "Chance to deal critical damage";
            case "Attack Range" -> "Maximum melee attack distance";
            case "Knockback" -> "How far enemies are pushed back";
            case "Burn Chance" -> "Chance to ignite enemies on hit";
            case "Fire Damage" -> "Bonus fire damage per hit";
            case "Cold Damage" -> "Bonus cold damage per hit";
            case "Lightning Damage" -> "Bonus lightning damage per hit";
            case "Void Damage" -> "Bonus void damage per hit";
            case "Energy Shield" -> "Absorbs damage before health";
            case "Max Health" -> "Maximum health points";
            case "Regeneration" -> "Health restored per second";
            case "Life Steal" -> "% of damage converted to health";
            case "Evasion Rating" -> calculateEvasionDescription();
            case "Armor" -> calculateArmorDescription();
            case "Toughness" -> "Reduces damage from big hits";
            case "Fire Res" -> "Reduces fire damage taken";
            case "Cold Res" -> "Reduces cold damage taken";
            case "Lightning Res" -> "Reduces lightning damage taken";
            case "Void Res" -> "Reduces void damage taken";
            case "Knockback Res" -> "Resistance to being pushed";
            case "Move Speed" -> "Movement speed modifier";
            case "Luck" -> "Increases rarity of drops";
            case "Exp Bonus" -> "Extra experience gained";
            case "Gathering Speed" -> "Mining and harvesting speed";
            case "Ore Fortune" -> "Chance to double drops from ores";
            case "Crop Fortune" -> "Chance to double drops from crops";
            case "Range" -> "Block interaction distance";
            default -> null;
        };
    }

    @Unique
    private String calculateEvasionDescription() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return "Chance to dodge attacks";

        double rating = player.getAttributeValue(ModAttributes.EVASION_RATING);
        double armor = player.getAttributeValue(EntityAttributes.ARMOR);

        double k = 400.0;
        double dodgeChance = rating / (rating + k + (armor * 5.0));

        int dodgePercent = (int) (dodgeChance * 100);

        return dodgePercent + "% chance to dodge";
    }

    @Unique
    private String calculateArmorDescription() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return "Reduces damage taken";

        double armor = player.getAttributeValue(EntityAttributes.ARMOR);

        // Minecraft's armor formula: reduction = min(20, max(armor/5, armor - damage/(2+toughness/4)))
        // For display, show the maximum possible reduction at current armor
        double maxReduction = Math.min(20.0, armor / 5.0);
        int reductionPercent = (int) (maxReduction * 5); // Each point = 5% reduction (capped at 100%)

        return reductionPercent + "% Reduced attack damage taken";
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                return baseValue;
            }
        }
        return 0.0;
    }

    @Unique
    private double calculateFinalElementalDamage(
            net.minecraft.entity.player.PlayerEntity player,
            ItemStack weapon,
            ItemStack chestplate,
            String percentStatName,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> damageAttribute,
            String enchantmentId
    ) {
        // Get base damage from attribute + weapon enchantment
        double baseDamage = player.getAttributeValue(damageAttribute);
        baseDamage += getEnchantmentValue(weapon, enchantmentId, 4.0);

        // Get percentage multipliers from items
        double percentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, percentStatName);
        double percentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, percentStatName);
        double totalPercent = percentFromWeapon + percentFromChestplate;

        // Apply percentage to base damage
        return baseDamage * (1.0 + totalPercent);
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        String valStr = String.format("%.1f", val);
        String text = name + ": " + valStr + suffix;

        int textWidth = tr.getWidth(text);
        int statX = x + 12;
        int statY = y;

        context.drawText(tr, Text.literal(text), statX, statY, 0xFF404040, false);

        return y + 10;
    }

    @Unique
    private int drawStatWithTooltip(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix, int mouseX, int mouseY, int panelX, int panelWidth) {
        String valStr = String.format("%.1f", val);
        String text = name + ": " + valStr + suffix;

        int textWidth = tr.getWidth(text);
        int statX = x + 12;
        int statY = y;

        // Check if mouse is hovering over this stat
        if (mouseX >= statX && mouseX <= statX + textWidth &&
                mouseY >= statY && mouseY <= statY + 9) {
            hoveredStat = name;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }

        context.drawText(tr, Text.literal(text), statX, statY, 0xFF404040, false);

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

        context.fill(sbX, sbY, sbX + 6, sbY + sbHeight, 0xFF8B8B8B);

        float scrollPercent = (float) scrollOffset / maxScrollOffset;
        int thumbHeight = Math.max(20, sbHeight / 4);
        int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

        context.fill(sbX, thumbY, sbX + 6, thumbY + thumbHeight, 0xFFC6C6C6);

        context.fill(sbX, thumbY, sbX + 6, thumbY + 1, 0xFFFFFFFF);
        context.fill(sbX, thumbY, sbX + 1, thumbY + thumbHeight, 0xFFFFFFFF);

        context.fill(sbX + 5, thumbY + 1, sbX + 6, thumbY + thumbHeight, 0xFF555555);
        context.fill(sbX + 1, thumbY + thumbHeight - 1, sbX + 6, thumbY + thumbHeight, 0xFF555555);

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
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
            baseAttackDamage += getEnchantmentValue(weapon, "minecraft:attack_damage", 6.0);
            baseAttackDamage += getEnchantmentValue(chestplate, "minecraft:attack_damage", 6.0);

            double attackDamagePercentFromWeapon = EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent");
            double attackDamagePercentFromChestplate = EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent");
            double totalAttackDamagePercent = attackDamagePercentFromWeapon + attackDamagePercentFromChestplate;
            double finalAtkDmg = baseAttackDamage * (1.0 + totalAttackDamagePercent);

            curY = drawStat(context, tr, panelX, curY, "Attack Damage", finalAtkDmg, "");

            // Attack Speed with enchantments (from weapon and all armor pieces - chainmail gives attack speed!)
            double attackSpeed = player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
            attackSpeed += getEnchantmentValue(weapon, "minecraft:attack_speed", 0.3);
            attackSpeed += getEnchantmentValue(chestplate, "minecraft:attack_speed", 0.3);
            attackSpeed += getEnchantmentValue(helmet, "minecraft:attack_speed", 0.3);
            attackSpeed += getEnchantmentValue(leggings, "minecraft:attack_speed", 0.3);
            attackSpeed += getEnchantmentValue(boots, "minecraft:attack_speed", 0.3);

            // Display as attacks per second (the actual value)
            curY = drawStat(context, tr, panelX, curY, "Attack Speed", attackSpeed, "");

            // Crit Chance with enchantments
            double critChance = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.15);
            critChance += getEnchantmentValue(chestplate, "minecraft:crit_chance", 0.15);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.15);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.15);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.15);
            curY = drawStat(context, tr, panelX, curY, "Crit Chance", critChance * 100, "%");

            // Attack Range with enchantments
            double attackRange = player.getAttributeValue(ModAttributes.ATTACK_RANGE);
            attackRange += getEnchantmentValue(weapon, "minecraft:attack_range", 1.0);
            curY = drawStat(context, tr, panelX, curY, "Attack Range", attackRange, " blocks");

            // Knockback with enchantments
            double knockback = player.getAttributeValue(ModAttributes.KNOCKBACK);
            knockback += getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.5);
            knockback += getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.5);
            curY = drawStat(context, tr, panelX, curY, "Knockback", knockback * 100, "%");
            curY += 6;

            // --- CATEGORY 2: ELEMENTAL ---
            context.drawText(tr, Text.literal("✨ Elemental"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Chance to Burn with enchantments
            double chanceToBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN);
            chanceToBurn += getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.25);
            chanceToBurn += getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.25);
            curY = drawStat(context, tr, panelX, curY, "Burn Chance", chanceToBurn * 100, "%");

            curY = drawStat(context, tr, panelX, curY, "Fire Damage", calculateFinalElementalDamage(player, weapon, chestplate, "fire_damage_percent", ModAttributes.FIRE_DAMAGE, "minecraft:fire_damage"), "");
            curY = drawStat(context, tr, panelX, curY, "Cold Damage", calculateFinalElementalDamage(player, weapon, chestplate, "cold_damage_percent", ModAttributes.COLD_DAMAGE, "minecraft:cold_damage"), "");
            curY = drawStat(context, tr, panelX, curY, "Lightning Damage", calculateFinalElementalDamage(player, weapon, chestplate, "lightning_damage_percent", ModAttributes.LIGHTNING_DAMAGE, "minecraft:lightning_damage"), "");
            curY = drawStat(context, tr, panelX, curY, "Void Damage", calculateFinalElementalDamage(player, weapon, chestplate, "void_damage_percent", ModAttributes.VOID_DAMAGE, "minecraft:void_damage"), "");
            curY += 6;

            // --- CATEGORY 3: SURVIVAL ---
            context.drawText(tr, Text.literal("❤ Survival"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Energy Shield (from gold and chainmail armor!)
            curY = drawStat(context, tr, panelX, curY, "Energy Shield", player.getAttributeValue(ModAttributes.ENERGY_SHIELD), "");

            // Max Health (from diamond armor!)
            curY = drawStat(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP");

            // HP Regeneration (from diamond armor!)
            double hpRegen = player.getAttributeValue(ModAttributes.LIFE_REGENERATION);
            hpRegen += player.getAttributeValue(ModAttributes.LIFE_REGENERATION);
            curY = drawStat(context, tr, panelX, curY, "Regeneration", hpRegen, " HP/s");

            // Life Steal with enchantments
            double lifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL);
            lifeSteal += getEnchantmentValue(weapon, "minecraft:life_steal", 0.08);
            lifeSteal += getEnchantmentValue(chestplate, "minecraft:life_steal", 0.08);
            curY = drawStat(context, tr, panelX, curY, "Life Steal", lifeSteal * 100, "%");

            // Evasion Rating (from leather and chainmail armor!)
            curY = drawStat(context, tr, panelX, curY, "Evasion Rating", player.getAttributeValue(ModAttributes.EVASION_RATING), "");

            curY = drawStat(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "");
            curY = drawStat(context, tr, panelX, curY, "Toughness", player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS), "");
            curY += 6;

            // --- CATEGORY 4: RESISTANCES ---
            context.drawText(tr, Text.literal("🛡 Resistances"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            // Elemental resistances (copper armor gives all 4!)
            curY = drawStat(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100, "%");

            // Knockback Resistance (from iron armor!)
            curY = drawStat(context, tr, panelX, curY, "Knockback Res", player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%");
            curY += 6;

            // --- CATEGORY 5: UTILITY ---
            context.drawText(tr, Text.literal("✦ Utility"), panelX + 8, curY, 0xFF404040, false);
            curY += 11;

            double moveSpeed = player.getAttributeValue(ModAttributes.MOVEMENT_SPEED);
            curY = drawStat(context, tr, panelX, curY, "Move Speed", moveSpeed * 100, "%");

            // Luck (from gold armor!)
            curY = drawStat(context, tr, panelX, curY, "Luck", player.getAttributeValue(ModAttributes.LUCK), "");

            curY = drawStat(context, tr, panelX, curY, "Exp Bonus", player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%");

            // Mining sub-section
            curY += 4;
            context.drawText(tr, Text.literal("⚒ Gathering:"), panelX + 8, curY, 0xFF555555, false);
            curY += 10;
            ItemStack tool = player.getMainHandStack();
            double baseRange = player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
            double weaponRangeBonus = EquipmentStatApplier.getItemStatValue(tool, "breaking_range");
            double totalRangeBonus = baseRange + weaponRangeBonus;

            curY = drawStat(context, tr, panelX, curY, "Gathering Speed", player.getAttributeValue(ModAttributes.MINING_EFFICIENCY) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Ore Fortune", player.getAttributeValue(ModAttributes.FORTUNE) * 100, "%");
            curY = drawStat(context, tr, panelX, curY, "Crop Fortune", EquipmentStatApplier.getItemStatValue(tool, "crop_fortune") * 100, "%");
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
        baseDamage += getEnchantmentValue(weapon, enchantmentId, 6.0);

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
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.client.StatsPanelScrollHandler;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenStatsMixin {

    @Unique
    private static final Identifier STATS_PANEL_TEXTURE = Identifier.of("cranks-dungeons", "textures/gui/stats_panel.png");
    @Unique
    private static final Identifier STATS_BUTTON_NORMAL = Identifier.of("cranks-dungeons", "textures/gui/stats_button.png");
    @Unique
    private static final Identifier STATS_BUTTON_HOVER = Identifier.of("cranks-dungeons", "textures/gui/stats_button_hover.png");
    @Unique
    private static final Identifier STATS_BUTTON_PRESSED = Identifier.of("cranks-dungeons", "textures/gui/stats_button_pressed.png");
    @Unique
    private static final Identifier STATS_BUTTON_PRESSED_HOVER = Identifier.of("cranks-dungeons", "textures/gui/stats_button_pressed_hover.png");
    @Unique
    private static final Identifier SCROLLBAR_TRACK_TEXTURE = Identifier.of("cranks-dungeons", "textures/gui/stats_scrollbar_background.png");
    @Unique
    private static final Identifier SCROLLBAR_THUMB_TEXTURE = Identifier.of("cranks-dungeons", "textures/gui/stats_scrollbar_scroller.png");

    // Note: statsVisible, scrollOffset and maxScrollOffset are now managed by StatsPanelScrollHandler
    // to enable proper cross-mixin communication for scroll wheel handling
    @Unique private static boolean statsVisible = false;
    @Unique private boolean draggingScrollbar = false;
    @Unique private boolean lastClickState = false;
    @Unique private String hoveredStat = null;
    @Unique private int tooltipX = 0;
    @Unique private int tooltipY = 0;

    // Debug: log once per second so we don't spam
    @Unique private static long lastLogTime = 0;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderStatsPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        try {
            int x = ((HandledScreenAccessor) screen).getX();
            int y = ((HandledScreenAccessor) screen).getY();
            int backgroundWidth = ((HandledScreenAccessor) screen).getBackgroundWidth();
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;

            int buttonX = x + backgroundWidth - 21;
            int buttonY = y + 6;
            int buttonWidth = 14;
            int buttonHeight = 14;

            boolean isHovering = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                    mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

            boolean leftClick = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                    MinecraftClient.getInstance().getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

            if (isHovering && leftClick && !lastClickState && !draggingScrollbar) {
                statsVisible = !statsVisible;
                StatsPanelScrollHandler.setStatsVisible(statsVisible);
                System.out.println("[StatsPanel] Toggled statsVisible=" + statsVisible);
            }
            lastClickState = leftClick;

            Identifier buttonTexture;
            if (statsVisible && isHovering) {
                buttonTexture = STATS_BUTTON_PRESSED_HOVER;
            } else if (statsVisible) {
                buttonTexture = STATS_BUTTON_PRESSED;
            } else if (isHovering) {
                buttonTexture = STATS_BUTTON_HOVER;
            } else {
                buttonTexture = STATS_BUTTON_NORMAL;
            }

            context.drawTexturedQuad(buttonTexture, buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0.0F, 1.0F, 0.0F, 1.0F);

            if (!statsVisible) return;

            int panelX = x + backgroundWidth + 2;
            int panelY = y;
            int panelWidth = 147;
            int panelHeight = 166;

            context.drawTexturedQuad(STATS_PANEL_TEXTURE, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0.0F, 1.0F, 0.0F, 1.0F);

            var player = MinecraftClient.getInstance().player;
            if (player == null) return;

            hoveredStat = null;

            // Pre-calculate total content height before drawing
            int totalContentHeight = calculateTotalContentHeight();
            int visibleHeight = panelHeight - 16;
            int scrollOffset = StatsPanelScrollHandler.getScrollOffset();
            int maxScrollOffset = Math.max(0, totalContentHeight - visibleHeight);
            StatsPanelScrollHandler.setMaxScrollOffset(maxScrollOffset);
            // Clamp scroll offset to new max
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            StatsPanelScrollHandler.setScrollOffset(scrollOffset);

            context.enableScissor(panelX + 4, panelY + 4, panelX + panelWidth - 14, panelY + panelHeight - 4);

            int curY = panelY + 8 - scrollOffset;

            // --- CATEGORY 1: COMBAT ---
            context.drawText(tr, Text.literal("⚔ Combat"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            ItemStack weapon = player.getMainHandStack();
            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

            double attackDamage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            attackDamage *= (1.0 + EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent") + EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent"));
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Damage", attackDamage, "", mouseX, mouseY, panelX, panelWidth);

            double attackSpeed = player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
            attackSpeed *= (1.0 + EquipmentStatApplier.getItemStatValue(weapon, "attack_speed_percent"));
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Speed", attackSpeed, "", mouseX, mouseY, panelX, panelWidth);

            double critChance = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(chestplate, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Chance", critChance * 100, "%", mouseX, mouseY, panelX, panelWidth);

            double baseAttackRange = player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
            double attrBonus = 0.0;
            var inst = player.getAttributeInstance(ModAttributes.ATTACK_RANGE);
            if (inst != null) attrBonus = inst.getValue();
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Range", baseAttackRange + attrBonus + getEnchantmentValue(weapon, "minecraft:attack_range", 1.0), " blocks", mouseX, mouseY, panelX, panelWidth);

            double knockback = player.getAttributeValue(ModAttributes.KNOCKBACK);
            knockback += getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.35);
            knockback += getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.35);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback", knockback * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Reload Speed", player.getAttributeValue(ModAttributes.RELOAD_SPEED) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 2: ELEMENTAL ---
            context.drawText(tr, Text.literal("✨ Elemental"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Damage", calculateFinalElementalDamage(player, weapon, chestplate, "fire_damage_percent", ModAttributes.FIRE_DAMAGE, "minecraft:fire_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Damage", calculateFinalElementalDamage(player, weapon, chestplate, "cold_damage_percent", ModAttributes.COLD_DAMAGE, "minecraft:cold_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Damage", calculateFinalElementalDamage(player, weapon, chestplate, "lightning_damage_percent", ModAttributes.LIGHTNING_DAMAGE, "minecraft:lightning_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Damage", calculateFinalElementalDamage(player, weapon, chestplate, "void_damage_percent", ModAttributes.VOID_DAMAGE, "minecraft:void_damage"), "", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 3: AILMENTS ---
            context.drawText(tr, Text.literal("☠ Ailments"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            // BURN stats
            double chanceToBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN);
            chanceToBurn += getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.05);
            chanceToBurn += getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Chance", chanceToBurn * 100, "%", mouseX, mouseY, panelX, panelWidth);
            double burnBaseDamage = player.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE);
            double burnDamagePercent = player.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT);
            double burnDamage = burnBaseDamage * burnDamagePercent / 100.0;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Damage", burnDamage, "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Duration", 4.0, "s", mouseX, mouseY, panelX, panelWidth);

            // POISON stats
            double chanceToPoison = player.getAttributeValue(ModAttributes.CHANCE_TO_POISON);
            chanceToPoison += getEnchantmentValue(weapon, "minecraft:chance_to_poison", 0.05);
            chanceToPoison += getEnchantmentValue(chestplate, "minecraft:chance_to_poison", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Chance", chanceToPoison * 100, "%", mouseX, mouseY, panelX, panelWidth);
            double poisonBaseDamage = player.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE);
            double poisonDamagePercent = player.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT);
            double poisonDamage = poisonBaseDamage * (1.0 + poisonDamagePercent / 100.0);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Damage", poisonDamage, "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Duration", 2.0, "s", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Max Stacks", 20.0, "", mouseX, mouseY, panelX, panelWidth);

            // BLEED stats
            double chanceToBleed = player.getAttributeValue(ModAttributes.CHANCE_TO_BLEED);
            chanceToBleed += getEnchantmentValue(weapon, "minecraft:chance_to_bleed", 0.05);
            chanceToBleed += getEnchantmentValue(chestplate, "minecraft:chance_to_bleed", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Chance", chanceToBleed * 100.0, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Duration", 5.0, "s", mouseX, mouseY, panelX, panelWidth);

            // FREEZE stats
            double chanceToFreeze = player.getAttributeValue(ModAttributes.CHANCE_TO_FREEZE);
            chanceToFreeze += getEnchantmentValue(weapon, "minecraft:chance_to_freeze", 0.05);
            chanceToFreeze += getEnchantmentValue(chestplate, "minecraft:chance_to_freeze", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Chance", chanceToFreeze * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Duration", 4.0, "s", mouseX, mouseY, panelX, panelWidth);

            // SHOCK stats
            double chanceToShock = player.getAttributeValue(ModAttributes.CHANCE_TO_SHOCK);
            chanceToShock += getEnchantmentValue(weapon, "minecraft:chance_to_shock", 0.05);
            chanceToShock += getEnchantmentValue(chestplate, "minecraft:chance_to_shock", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Chance", chanceToShock * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Duration", 10.0, "s", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 4: SURVIVAL ---
            context.drawText(tr, Text.literal("❤ Survival"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Energy Shield", player.getAttributeValue(ModAttributes.ENERGY_SHIELD), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health", player.getAttributeValue(EntityAttributes.MAX_HEALTH), " HP", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Regeneration", player.getAttributeValue(ModAttributes.LIFE_REGENERATION), " HP/s", mouseX, mouseY, panelX, panelWidth);
            double lifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL);
            lifeSteal += getEnchantmentValue(weapon, "minecraft:life_steal", 0.06);
            lifeSteal += getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Life Steal", lifeSteal * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion", player.getAttributeValue(ModAttributes.EVASION_RATING), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Armor", player.getAttributeValue(EntityAttributes.ARMOR), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Toughness", player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS), "", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 5: RESISTANCES ---
            context.drawText(tr, Text.literal("🛡 Resistances"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback Res", player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY += 6;

            // --- CATEGORY 6: UTILITY ---
            context.drawText(tr, Text.literal("✦ Utility"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Move Speed", player.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) / 0.1 * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Luck", player.getAttributeValue(EntityAttributes.LUCK), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Exp Bonus", player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY += 4;
            context.drawText(tr, Text.literal("⚒ Gathering:"), panelX + 6, curY, 0xFF404040, false);
            curY += 10;

            ItemStack tool = player.getMainHandStack();
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Gather Speed", player.getAttributeValue(ModAttributes.MINING_EFFICIENCY) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            double precisionChance = 0.0;
            var precisionInstance = player.getAttributeInstance(ModAttributes.PRECISION_MINING);
            if (precisionInstance != null) precisionChance = precisionInstance.getValue();
            if (precisionChance <= 0.0) precisionChance = ToolStatHelper.getPrecisionMining(tool);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Repair Chance", precisionChance * 100.0, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Ore Fortune", player.getAttributeValue(ModAttributes.FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Log Fortune", player.getAttributeValue(ModAttributes.LOG_FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Dig Fortune", player.getAttributeValue(ModAttributes.DIG_FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop Fortune", EquipmentStatApplier.getItemStatValue(tool, "crop_fortune") * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop XP", EquipmentStatApplier.getItemStatValue(tool, "crop_experience"), " xp", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Range", player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE) + EquipmentStatApplier.getItemStatValue(tool, "breaking_range"), " blocks", mouseX, mouseY, panelX, panelWidth);

            // Debug log once per second
            long now = System.currentTimeMillis();
            if (now - lastLogTime > 1000) {
                lastLogTime = now;
                System.out.println("[StatsPanel] panelY=" + panelY + " panelHeight=" + panelHeight
                        + " curY=" + curY + " scrollOffset=" + StatsPanelScrollHandler.getScrollOffset()
                        + " totalContentHeight=" + totalContentHeight
                        + " maxScrollOffset=" + StatsPanelScrollHandler.getMaxScrollOffset());
            }

            context.disableScissor();

            if (StatsPanelScrollHandler.getMaxScrollOffset() > 0) {
                renderScrollbar(context, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, leftClick);
            }

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
        int tx = x + 12, ty = y - 12, w = textWidth, h = 8;
        context.fill(tx - 3, ty - 5, tx + w + 3, ty + h + 5, 0xFF000000);
        context.fill(tx - 5, ty - 3, tx + w + 5, ty + h + 3, 0xFF000000);
        context.fill(tx - 4, ty - 4, tx + w + 4, ty + h + 4, 0xFF000000);
        context.fill(tx - 4, ty - 3, tx + w + 4, ty + h + 3, 0xF0100010);
        int pTop = 0x505000FF, pBottom = 0x5028007F;
        context.fillGradient(tx - 4, ty - 3, tx - 3, ty + h + 3, pTop, pBottom);
        context.fillGradient(tx + w + 3, ty - 3, tx + w + 4, ty + h + 3, pTop, pBottom);
        context.fill(tx - 3, ty - 4, tx + w + 3, ty - 3, pTop);
        context.fill(tx - 3, ty + h + 3, tx + w + 3, ty + h + 4, pBottom);
        context.fill(tx - 5, ty - 3, tx - 4, ty + h + 3, 0xFF000000);
        context.fill(tx + w + 4, ty - 3, tx + w + 5, ty + h + 3, 0xFF000000);
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
            case "Reload Speed" -> "How much faster bows/crossbows charge";
            case "Burn Chance" -> "Chance to ignite enemies on hit";
            case "Burn Damage" -> "Fire damage per burn tick";
            case "Burn Duration" -> "Base duration of burn ailment";
            case "Poison Chance" -> "Chance to poison enemies on hit";
            case "Poison Damage" -> "Void damage per poison tick";
            case "Poison Duration" -> "Base duration of poison ailment";
            case "Poison Max Stacks" -> "Maximum poison stacks (20)";
            case "Bleed Chance" -> "Chance to cause bleeding on hit";
            case "Bleed Duration" -> "Base duration of bleed ailment";
            case "Freeze Chance" -> "Chance to freeze enemies on hit";
            case "Freeze Duration" -> "Base duration of freeze ailment";
            case "Shock Chance" -> "Chance to shock enemies on hit";
            case "Shock Duration" -> "Base duration of shock ailment";
            case "Fire Damage" -> "Bonus fire damage per hit";
            case "Cold Damage" -> "Bonus cold damage per hit";
            case "Lightning Dmg" -> "Bonus lightning damage per hit";
            case "Void Damage" -> "Bonus void damage per hit";
            case "Energy Shield" -> "Absorbs damage before health";
            case "Max Health" -> "Maximum health points";
            case "Regeneration" -> "Health restored per second";
            case "Life Steal" -> "% of damage converted to health";
            case "Evasion" -> calculateEvasionDescription();
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
            case "Gather Speed" -> "Mining and harvesting speed";
            case "Repair Chance" -> "Chance that tools avoid durability loss";
            case "Ore Fortune" -> "Chance to double drops from ores";
            case "Crop Fortune" -> "Chance to double drops from crops";
            case "Log Fortune" -> "Chance to gain extra logs from trees";
            case "Dig Fortune" -> "Chance to gain extra materials";
            case "Crop XP" -> "Experience gained when harvesting fully grown crops with a hoe";
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
        return (int) (rating / (rating + 400.0 + armor * 5.0) * 100) + "% chance to dodge";
    }

    @Unique
    private String calculateArmorDescription() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return "Reduces damage taken";
        double armor = player.getAttributeValue(EntityAttributes.ARMOR);
        return (int) (Math.min(20.0, armor / 5.0) * 5) + "% Reduced attack damage taken";
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            if (entry.getKey().getKey().get().getValue().toString().equals(enchantmentId)) return baseValue;
        }
        return 0.0;
    }

    @Unique
    private double calculateFinalElementalDamage(
            net.minecraft.entity.player.PlayerEntity player, ItemStack weapon, ItemStack chestplate,
            String percentStatName,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> damageAttribute,
            String enchantmentId) {
        double base = player.getAttributeValue(damageAttribute) + getEnchantmentValue(weapon, enchantmentId, 4.0);
        return base * (1.0 + EquipmentStatApplier.getItemStatValue(weapon, percentStatName) + EquipmentStatApplier.getItemStatValue(chestplate, percentStatName));
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        context.drawText(tr, Text.literal(name + ": " + String.format("%.1f", val) + suffix), x + 8, y, 0xFF404040, false);
        return y + 10;
    }

    @Unique
    private int drawStatWithTooltip(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix, int mouseX, int mouseY, int panelX, int panelWidth) {
        String text = name + ": " + String.format("Crop XP".equals(name) ? "%.2f" : "%.1f", val) + suffix;
        int statX = x + 8;
        if (mouseX >= statX && mouseX <= statX + tr.getWidth(text) && mouseY >= y && mouseY <= y + 9) {
            hoveredStat = name;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        context.drawText(tr, Text.literal(text), statX, y, 0xFF404040, false);
        return y + 10;
    }

    @Unique
    private double getModifierFromStack(ItemStack stack, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        double val = 0;
        var comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp != null) for (var entry : comp.modifiers()) if (entry.attribute().equals(attr)) val += entry.modifier().value();
        return val;
    }

    @Unique
    private void renderScrollbar(DrawContext context, int panelX, int panelY, int panelWidth, int panelHeight, int mouseX, int mouseY, boolean leftClick) {
        int sbX = panelX + panelWidth - 12;
        int sbY = panelY + 18;
        int sbWidth = 6;
        int sbHeight = panelHeight - 36;
        
        int scrollOffset = StatsPanelScrollHandler.getScrollOffset();
        int maxScrollOffset = StatsPanelScrollHandler.getMaxScrollOffset();

        context.drawTexturedQuad(SCROLLBAR_TRACK_TEXTURE, sbX, sbY, sbX + sbWidth, sbY + sbHeight, 0.0F, 1.0F, 0.0F, 1.0F);

        float scrollPercent = maxScrollOffset > 0 ? (float) scrollOffset / maxScrollOffset : 0;
        int thumbHeight = Math.max(20, sbHeight / 4);
        int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

        context.drawTexturedQuad(SCROLLBAR_THUMB_TEXTURE, sbX, thumbY, sbX + sbWidth, thumbY + thumbHeight, 0.0F, 1.0F, 0.0F, 1.0F);

        if (leftClick) {
            if (!draggingScrollbar && mouseX >= sbX && mouseX <= sbX + sbWidth && mouseY >= sbY && mouseY <= sbY + sbHeight) {
                draggingScrollbar = true;
            }
            if (draggingScrollbar) {
                float newPercent = Math.max(0, Math.min(1, (float) (mouseY - sbY - thumbHeight / 2) / (sbHeight - thumbHeight)));
                StatsPanelScrollHandler.setScrollOffset((int) (newPercent * maxScrollOffset));
            }
        } else {
            draggingScrollbar = false;
        }
    }

    @Unique
    private int calculateTotalContentHeight() {
        int height = 0;
        
        // Category header + spacing
        height += 11;
        
        // Combat stats (6 stats: Attack Damage, Attack Speed, Crit Chance, Attack Range, Knockback, Reload Speed)
        height += 6 * 10;
        height += 6; // spacing after category
        
        // Elemental category
        height += 11;
        // Elemental stats (4 stats: Fire, Cold, Lightning, Void)
        height += 4 * 10;
        height += 6;
        
        // Ailments category
        height += 11;
        // Ailment stats (13 stats: burn 3 + poison 4 + bleed 2 + freeze 2 + shock 2)
        height += 13 * 10;
        height += 6;
        
        // Survival category
        height += 11;
        // Survival stats (7 stats: Energy Shield, Max Health, Regen, Life Steal, Evasion, Armor, Toughness)
        height += 7 * 10;
        height += 6;
        
        // Resistances category
        height += 11;
        // Resistance stats (5 stats: Fire, Cold, Lightning, Void, Knockback)
        height += 5 * 10;
        height += 6;
        
        // Utility category
        height += 11;
        // Utility stats (3 stats: Move Speed, Luck, Exp Bonus)
        height += 3 * 10;
        height += 4;
        
        // Gathering sub-header
        height += 10;
        // Gathering stats (8 stats: Gather Speed, Repair Chance, Ore, Log, Dig, Crop Fortune, Crop XP, Range)
        height += 8 * 10;
        
        return height;
    }
}
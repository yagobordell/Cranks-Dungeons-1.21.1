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
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenStatsMixin {

    @Unique
    private static final Identifier STATS_PANEL_TEXTURE = Identifier.of("cranks-dungeons",
            "textures/gui/stats_panel.png");
    @Unique
    private static final Identifier STATS_BUTTON_NORMAL = Identifier.of("cranks-dungeons",
            "textures/gui/stats_button.png");
    @Unique
    private static final Identifier STATS_BUTTON_HOVER = Identifier.of("cranks-dungeons",
            "textures/gui/stats_button_hover.png");
    @Unique
    private static final Identifier STATS_BUTTON_PRESSED = Identifier.of("cranks-dungeons",
            "textures/gui/stats_button_pressed.png");
    @Unique
    private static final Identifier STATS_BUTTON_PRESSED_HOVER = Identifier.of("cranks-dungeons",
            "textures/gui/stats_button_pressed_hover.png");
            
    // SKILL TREE BUTTON
    @Unique
    private static final Identifier SKILLS_BUTTON_NORMAL = Identifier.of("cranks-dungeons",
            "textures/gui/skills_button.png"); // We'll assume a similar texture naming convention
    @Unique
    private static final Identifier SKILLS_BUTTON_HOVER = Identifier.of("cranks-dungeons",
            "textures/gui/skills_button_hover.png");

    @Unique
    private static final Identifier SCROLLBAR_TRACK_TEXTURE = Identifier.of("cranks-dungeons",
            "textures/gui/stats_scrollbar_background.png");
    @Unique
    private static final Identifier SCROLLBAR_THUMB_TEXTURE = Identifier.of("cranks-dungeons",
            "textures/gui/stats_scrollbar_scroller.png");

    // Note: statsVisible, scrollOffset and maxScrollOffset are now managed by
    // StatsPanelScrollHandler
    // to enable proper cross-mixin communication for scroll wheel handling
    @Unique
    private static boolean statsVisible = false;
    @Unique
    private boolean draggingScrollbar = false;
    @Unique
    private boolean lastClickState = false;
    @Unique
    private boolean lastSkillClickState = false;
    @Unique
    private String hoveredStat = null;
    @Unique
    private int tooltipX = 0;
    @Unique
    private int tooltipY = 0;
    @Unique
    private double hoveredBaseVal = 0;
    @Unique
    private double hoveredEquipVal = 0;
    @Unique
    private String hoveredSuffix = "";

    // Debug: log once per second so we don't spam
    @Unique
    private static long lastLogTime = 0;

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
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

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

            context.drawTexturedQuad(buttonTexture, buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight,
                    0.0F, 1.0F, 0.0F, 1.0F);

            // --- SKILL TREE BUTTON ---
            int skillButtonX = buttonX;
            int skillButtonY = buttonY + 16;
            boolean isHoveringSkill = mouseX >= skillButtonX && mouseX <= skillButtonX + buttonWidth &&
                    mouseY >= skillButtonY && mouseY <= skillButtonY + buttonHeight;
                    
            if (isHoveringSkill && leftClick && !lastSkillClickState && !draggingScrollbar) {
                MinecraftClient.getInstance().setScreen(new com.cranks.dungeons.client.screen.PassiveSkillTreeScreen(screen));
            }
            lastSkillClickState = leftClick;

            Identifier skillButtonTexture = isHoveringSkill ? SKILLS_BUTTON_HOVER : SKILLS_BUTTON_NORMAL;
            context.drawTexturedQuad(skillButtonTexture, skillButtonX, skillButtonY, skillButtonX + buttonWidth, skillButtonY + buttonHeight,
                    0.0F, 1.0F, 0.0F, 1.0F);


            if (!statsVisible)
                return;

            int panelX = x + backgroundWidth + 2;
            int panelY = y;
            int panelWidth = 147;
            int panelHeight = 166;

            context.drawTexturedQuad(STATS_PANEL_TEXTURE, panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                    0.0F, 1.0F, 0.0F, 1.0F);

            var player = MinecraftClient.getInstance().player;
            if (player == null)
                return;

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

            // --- CATEGORY 1: OFFENSIVE ---
            context.drawText(tr, Text.literal("⚔ Offensive"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            ItemStack weapon = player.getMainHandStack();
            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

            // 1. Physical Damage
            double baseAD = player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
            double weaponAD = getModifierFromStack(weapon, EntityAttributes.ATTACK_DAMAGE);
            double attackDamage = baseAD + weaponAD;
            attackDamage *= (1.0 + EquipmentStatApplier.getItemStatValue(weapon, "attack_damage_percent")
                    + EquipmentStatApplier.getItemStatValue(chestplate, "attack_damage_percent"));
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Physical Damage", attackDamage, baseAD, "", mouseX,
                    mouseY, panelX, panelWidth);

            // 2. Elemental Damages (Moved here)
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Damage",
                    calculateFinalElementalDamage(player, weapon, chestplate, "fire_damage_percent",
                            ModAttributes.FIRE_DAMAGE, "minecraft:fire_damage"),
                    player.getAttributeBaseValue(ModAttributes.FIRE_DAMAGE), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Damage",
                    calculateFinalElementalDamage(player, weapon, chestplate, "cold_damage_percent",
                            ModAttributes.COLD_DAMAGE, "minecraft:cold_damage"),
                    player.getAttributeBaseValue(ModAttributes.COLD_DAMAGE), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Damage",
                    calculateFinalElementalDamage(player, weapon, chestplate, "lightning_damage_percent",
                            ModAttributes.LIGHTNING_DAMAGE, "minecraft:lightning_damage"),
                    player.getAttributeBaseValue(ModAttributes.LIGHTNING_DAMAGE), "", mouseX, mouseY, panelX,
                    panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Damage",
                    calculateFinalElementalDamage(player, weapon, chestplate, "void_damage_percent",
                            ModAttributes.VOID_DAMAGE, "minecraft:void_damage"),
                    player.getAttributeBaseValue(ModAttributes.VOID_DAMAGE), "", mouseX, mouseY, panelX, panelWidth);

            // 3. Combat Speed & Range
            double baseAS = player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED);
            double weaponAS = getModifierFromStack(weapon, EntityAttributes.ATTACK_SPEED);
            double attackSpeed = baseAS + weaponAS;
            double runePercent = EquipmentStatApplier.getItemStatValue(weapon, "attack_speed")
                    + EquipmentStatApplier.getItemStatValue(chestplate, "attack_speed")
                    + player.getAttributeValue(ModAttributes.ATTACK_SPEED)
                    + getEnchantmentValue(weapon, "minecraft:attack_speed", 0.2)
                    + getEnchantmentValue(chestplate, "minecraft:attack_speed", 0.2);
            attackSpeed += attackSpeed * runePercent;
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Speed", attackSpeed, baseAS, "", mouseX,
                    mouseY, panelX, panelWidth);

            double baseAttackRange = player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
            double attrBonus = 0.0;
            var instRange = player.getAttributeInstance(ModAttributes.ATTACK_RANGE);
            if (instRange != null)
                attrBonus = instRange.getValue();
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Range",
                    baseAttackRange + attrBonus + getEnchantmentValue(weapon, "minecraft:attack_range", 1.0),
                    baseAttackRange, " blocks", mouseX, mouseY, panelX, panelWidth);

            // 4. Critical Strike
            double critChance = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
            double baseCrit = player.getAttributeBaseValue(ModAttributes.CRIT_CHANCE);
            critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(chestplate, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
            critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Chance", critChance * 100, baseCrit * 100, "%",
                    mouseX, mouseY, panelX, panelWidth);

            double critMult = player.getAttributeValue(ModAttributes.CRIT_MULTIPLIER);
            double baseCritMult = player.getAttributeBaseValue(ModAttributes.CRIT_MULTIPLIER);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Multiplier", critMult * 100, baseCritMult * 100, "%",
                    mouseX, mouseY, panelX, panelWidth);

            // 5. Ailments (Restored secondary stats)
            // BURN
            double chanceToBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN) + getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.05) + getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Chance", chanceToBurn * 100, player.getAttributeBaseValue(ModAttributes.CHANCE_TO_BURN) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            double burnBaseVal = player.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE);
            double burnPctVal = player.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Damage", burnBaseVal * burnPctVal / 100.0, 4.0, "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Duration", 4.0 * (1.0 + player.getAttributeValue(ModAttributes.BURN_DURATION)), 4.0, "s", mouseX, mouseY, panelX, panelWidth);

            // POISON
            double chanceToPoison = player.getAttributeValue(ModAttributes.CHANCE_TO_POISON) + getEnchantmentValue(weapon, "minecraft:chance_to_poison", 0.05) + getEnchantmentValue(chestplate, "minecraft:chance_to_poison", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Chance", chanceToPoison * 100, player.getAttributeBaseValue(ModAttributes.CHANCE_TO_POISON) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            double poisonBaseVal = player.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE);
            double poisonPctVal = player.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Damage", poisonBaseVal * (1.0 + poisonPctVal / 100.0), 1.0, "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Duration", 2.0 * (1.0 + player.getAttributeValue(ModAttributes.POISON_DURATION)), 2.0, "s", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Stacks", 20.0 + player.getAttributeValue(ModAttributes.POISON_MAX_STACKS), 20.0, " max", mouseX, mouseY, panelX, panelWidth);

            // BLEED
            double chanceToBleed = player.getAttributeValue(ModAttributes.CHANCE_TO_BLEED) + getEnchantmentValue(weapon, "minecraft:chance_to_bleed", 0.05) + getEnchantmentValue(chestplate, "minecraft:chance_to_bleed", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Chance", chanceToBleed * 100, player.getAttributeBaseValue(ModAttributes.CHANCE_TO_BLEED) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Duration", 5.0 * (1.0 + player.getAttributeValue(ModAttributes.BLEED_DURATION)), 5.0, "s", mouseX, mouseY, panelX, panelWidth);

            // FREEZE
            double chanceToFreeze = player.getAttributeValue(ModAttributes.CHANCE_TO_FREEZE) + getEnchantmentValue(weapon, "minecraft:chance_to_freeze", 0.05) + getEnchantmentValue(chestplate, "minecraft:chance_to_freeze", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Chance", chanceToFreeze * 100, player.getAttributeBaseValue(ModAttributes.CHANCE_TO_FREEZE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Duration", 4.0 * (1.0 + player.getAttributeValue(ModAttributes.FREEZE_DURATION)), 4.0, "s", mouseX, mouseY, panelX, panelWidth);

            // SHOCK
            double chanceToShock = player.getAttributeValue(ModAttributes.CHANCE_TO_SHOCK) + getEnchantmentValue(weapon, "minecraft:chance_to_shock", 0.05) + getEnchantmentValue(chestplate, "minecraft:chance_to_shock", 0.05);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Chance", chanceToShock * 100, player.getAttributeBaseValue(ModAttributes.CHANCE_TO_SHOCK) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Duration", player.getAttributeValue(ModAttributes.SHOCK_DURATION), player.getAttributeBaseValue(ModAttributes.SHOCK_DURATION), "s", mouseX, mouseY, panelX, panelWidth);

            // 6. Life Steal
            double lifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL) + getEnchantmentValue(weapon, "minecraft:life_steal", 0.06) + getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Life Steal", lifeSteal * 100, player.getAttributeBaseValue(ModAttributes.LIFE_STEAL) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            curY += 6;


            // --- CATEGORY 2: DEFENSIVE ---
            context.drawText(tr, Text.literal("🛡 Defensive"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health",
                    player.getAttributeValue(EntityAttributes.MAX_HEALTH),
                    player.getAttributeBaseValue(EntityAttributes.MAX_HEALTH), " HP", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Health Regeneration",
                    player.getAttributeValue(ModAttributes.LIFE_REGENERATION),
                    player.getAttributeBaseValue(ModAttributes.LIFE_REGENERATION), " HP/s", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Armor",
                    player.getAttributeValue(EntityAttributes.ARMOR),
                    player.getAttributeBaseValue(EntityAttributes.ARMOR), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Toughness",
                    player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS),
                    player.getAttributeBaseValue(EntityAttributes.ARMOR_TOUGHNESS), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion",
                    player.getAttributeValue(ModAttributes.EVASION_RATING),
                    player.getAttributeBaseValue(ModAttributes.EVASION_RATING), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Energy Shield",
                    player.getAttributeValue(ModAttributes.ENERGY_SHIELD),
                    player.getAttributeBaseValue(ModAttributes.ENERGY_SHIELD), "", mouseX, mouseY, panelX, panelWidth);

            // Resistances Group
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Res",
                    player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100,
                    player.getAttributeBaseValue(ModAttributes.FIRE_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Res",
                    player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100,
                    player.getAttributeBaseValue(ModAttributes.COLD_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Res",
                    player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100,
                    player.getAttributeBaseValue(ModAttributes.LIGHTNING_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Res",
                    player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100,
                    player.getAttributeBaseValue(ModAttributes.VOID_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback Res",
                    player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100,
                    player.getAttributeBaseValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            curY += 6;

            // --- CATEGORY 3: UTILITY ---
            context.drawText(tr, Text.literal("✦ Utility"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Move Speed",
                    player.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) / 0.1 * 100, 100.0, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Flying Speed",
                    player.getAttributeValue(ModAttributes.FLYING_SPEED) * 100,
                    player.getAttributeBaseValue(ModAttributes.FLYING_SPEED) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Luck",
                    player.getAttributeValue(EntityAttributes.LUCK),
                    player.getAttributeBaseValue(EntityAttributes.LUCK), "", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Exp Bonus",
                    player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100,
                    player.getAttributeBaseValue(ModAttributes.EXPERIENCE_BONUS) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            double knockback = player.getAttributeValue(ModAttributes.KNOCKBACK) + getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.35) + getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.35);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback", knockback * 100, player.getAttributeBaseValue(ModAttributes.KNOCKBACK) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Reload Speed",
                    player.getAttributeValue(ModAttributes.RELOAD_SPEED) * 100,
                    player.getAttributeBaseValue(ModAttributes.RELOAD_SPEED) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            curY += 6;

            // --- CATEGORY 4: GATHERING ---
            context.drawText(tr, Text.literal("⚒ Gathering"), panelX + 6, curY, 0xFF404040, false);
            curY += 11;

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Gather Speed",
                    player.getAttributeValue(ModAttributes.MINING_EFFICIENCY) * 100,
                    player.getAttributeBaseValue(ModAttributes.MINING_EFFICIENCY) * 100, "%", mouseX, mouseY, panelX, panelWidth);

            double precisionChance = 0.0;
            var precisionInstance = player.getAttributeInstance(ModAttributes.PRECISION_MINING);
            if (precisionInstance != null) precisionChance = precisionInstance.getValue();
            if (precisionChance <= 0.0) precisionChance = ToolStatHelper.getPrecisionMining(weapon);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Repair Chance", precisionChance * 100.0, 0.0, "%", mouseX, mouseY, panelX, panelWidth);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Range",
                    player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE) + EquipmentStatApplier.getItemStatValue(weapon, "breaking_range"),
                    player.getAttributeBaseValue(EntityAttributes.ENTITY_INTERACTION_RANGE), " blocks", mouseX, mouseY, panelX, panelWidth);

            curY = drawStatWithTooltip(context, tr, panelX, curY, "Ore Fortune",
                    player.getAttributeValue(ModAttributes.FORTUNE) * 100,
                    player.getAttributeBaseValue(ModAttributes.FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Log Fortune",
                    player.getAttributeValue(ModAttributes.LOG_FORTUNE) * 100,
                    player.getAttributeBaseValue(ModAttributes.LOG_FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Dig Fortune",
                    player.getAttributeValue(ModAttributes.DIG_FORTUNE) * 100,
                    player.getAttributeBaseValue(ModAttributes.DIG_FORTUNE) * 100, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop Fortune",
                    EquipmentStatApplier.getItemStatValue(weapon, "crop_fortune") * 100, 0.0, "%", mouseX, mouseY, panelX, panelWidth);
            curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop XP",
                    EquipmentStatApplier.getItemStatValue(weapon, "crop_experience"), 0.0, " xp", mouseX, mouseY, panelX, panelWidth);

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
        if (description == null)
            return;

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(description).formatted(Formatting.GRAY));

        // Use standard formatting for values
        String baseStr = String.format("Crop XP".equals(statName) ? "%.2f" : "%.1f", hoveredBaseVal);
        String equipVal = String.format("Crop XP".equals(statName) ? "%.2f" : "%.1f", hoveredEquipVal);
        String sign = (hoveredEquipVal >= 0) ? "+" : "";

        lines.add(Text.empty()); // Spacer
        lines.add(Text.literal("Base: ").formatted(Formatting.WHITE)
                .append(Text.literal(baseStr + hoveredSuffix).formatted(Formatting.GOLD)));
        lines.add(Text.literal("Equipment: ").formatted(Formatting.WHITE)
                .append(Text.literal(sign + equipVal + hoveredSuffix).formatted(Formatting.AQUA)));

        int maxWidth = tr.getWidth(lines.get(0));
        for (Text line : lines) {
            maxWidth = Math.max(maxWidth, tr.getWidth(line));
        }

        int tx = x + 12;
        int ty = y - 12;
        int w = maxWidth;
        int h = (lines.size() * 10);

        // Render PoE-style tooltip background
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

        for (int i = 0; i < lines.size(); i++) {
            context.drawText(tr, lines.get(i), tx, ty + (i * 10), 0xFFFFFFFF, true);
        }
    }

    @Unique
    private String getStatDescription(String statName) {
        return switch (statName) {
            case "Physical Damage" -> "Physical damage dealt per hit";
            case "Attack Speed" -> "Number of attacks per second";
            case "Crit Chance" -> "Chance to deal critical damage";
            case "Crit Multiplier" -> "Multiplier applied to damage when you deal a critical strike";
            case "Attack Range" -> "Maximum melee attack distance";
            case "Knockback" -> "How far enemies are pushed back";
            case "Reload Speed" -> "How much faster bows/crossbows charge";
            case "Burn Chance" -> {
                var p = MinecraftClient.getInstance().player;
                double baseDmg = p != null ? p.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE) : 4.0;
                double pct = p != null ? p.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT) : 100.0;
                yield "Chance to ignite enemies. Deal " + String.format("%.1f", baseDmg * pct / 100.0) + " Fire dmg/s for 4.0s";
            }
            case "Poison Chance" -> {
                var p = MinecraftClient.getInstance().player;
                double baseDmg = p != null ? p.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE) : 1.0;
                double pct = p != null ? p.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT) : 0.0;
                yield "Chance to poison enemies. Deal " + String.format("%.1f", baseDmg * (1.0 + pct / 100.0)) + " Void dmg/s for 2.0s (Max 20)";
            }
            case "Bleed Chance" -> "Chance to cause bleeding. Deal physical damage over 5.0s based on hit";
            case "Freeze Chance" -> "Chance to freeze enemies for 4.0s";
            case "Shock Chance" -> "Chance to shock enemies for 10.0s, increasing damage taken";
            case "Burn Damage" -> "Fire damage dealt per second by burning";
            case "Burn Duration" -> "How long enemies stay ignited";
            case "Poison Damage" -> "Void damage dealt per second by poison";
            case "Poison Duration" -> "How long each poison stack lasts";
            case "Poison Stacks" -> "Maximum number of poison stacks allowed";
            case "Bleed Duration" -> "Time until bleed damage is fully stored and dealt";
            case "Freeze Duration" -> "How long enemies remain frozen and immobile";
            case "Shock Duration" -> "Duration of the shock debuff on enemies";
            case "Fire Damage" -> "Bonus fire damage per hit";
            case "Cold Damage" -> "Bonus cold damage per hit";
            case "Lightning Damage" -> "Bonus lightning damage per hit";
            case "Void Damage" -> "Bonus void damage per hit";
            case "Energy Shield" -> "Absorbs damage before health";
            case "Max Health" -> "Maximum health points";
            case "Health Regeneration" -> "Health restored per second";
            case "Life Steal" -> "% of damage converted to health";
            case "Evasion" -> calculateEvasionDescription();
            case "Armor" -> calculateArmorDescription();
            case "Toughness" -> "Improves armor effectiveness against big hits";
            case "Fire Res" -> "Reduces fire damage taken";
            case "Cold Res" -> "Reduces cold damage taken";
            case "Lightning Res" -> "Reduces lightning damage taken";
            case "Void Res" -> "Reduces void damage taken";
            case "Knockback Res" -> "Resistance to being pushed";
            case "Move Speed" -> "Movement speed modifier";
            case "Flying Speed" -> "Elytra flight speed bonus";
            case "Luck" -> "Increases rarity of drops";
            case "Exp Bonus" -> "Extra experience gained";
            case "Gather Speed" -> "Mining and harvesting speed";
            case "Repair Chance" -> "Chance that tools avoid durability loss";
            case "Ore Fortune" -> "Chance to double drops from ores";
            case "Crop Fortune" -> "Chance to double drops from crops";
            case "Log Fortune" -> "Chance to gain extra logs from trees";
            case "Dig Fortune" -> "Chance to gain extra materials";
            case "Crop XP" -> "Experience gained from harvesting crops with a hoe";
            case "Range" -> "Block interaction distance";
            default -> null;
        };
    }

    @Unique
    private String calculateEvasionDescription() {
        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return "Chance to dodge attacks";
        double rating = player.getAttributeValue(ModAttributes.EVASION_RATING);
        double armor = player.getAttributeValue(EntityAttributes.ARMOR);
        // Adjusted armor penalty (0.25x) to match new higher armor scale
        return (int) (rating / (rating + 400.0 + armor * 0.25) * 100) + "% chance to dodge";
    }

    @Unique
    private String calculateArmorDescription() {
        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return "Reduces incoming physical damage";
        double armor = player.getAttributeValue(EntityAttributes.ARMOR);
        double toughness = player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
        
        // Show reduction against a standard 10 damage hit
        double coeff = 80.0 / (1.0 + (toughness / 20.0));
        double pdr = armor / (armor + coeff * 10.0);
        
        return (int)(pdr * 100) + "% PDR (Estimated vs 10 Physical Damage)";
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            if (entry.getKey().getKey().get().getValue().toString().equals(enchantmentId))
                return baseValue;
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
        return base * (1.0 + EquipmentStatApplier.getItemStatValue(weapon, percentStatName)
                + EquipmentStatApplier.getItemStatValue(chestplate, percentStatName));
    }

    @Unique
    private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val, String suffix) {
        context.drawText(tr, Text.literal(name + ": " + String.format("%.1f", val) + suffix), x + 8, y, 0xFF404040,
                false);
        return y + 10;
    }

    @Unique
    private int drawStatWithTooltip(DrawContext context, TextRenderer tr, int x, int y, String name, double total,
            double base, String suffix, int mouseX, int mouseY, int panelX, int panelWidth) {
        String text = name + ": " + String.format("Crop XP".equals(name) ? "%.2f" : "%.1f", total) + suffix;
        int statX = x + 8;
        if (mouseX >= statX && mouseX <= statX + tr.getWidth(text) && mouseY >= y && mouseY <= y + 9) {
            hoveredStat = name;
            tooltipX = mouseX;
            tooltipY = mouseY;
            hoveredBaseVal = base;
            hoveredEquipVal = total - base;
            hoveredSuffix = suffix;
        }
        context.drawText(tr, Text.literal(text), statX, y, 0xFF404040, false);
        return y + 10;
    }

    @Unique
    private double getModifierFromStack(ItemStack stack,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        double val = 0;
        var comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp != null)
            for (var entry : comp.modifiers())
                if (entry.attribute().equals(attr))
                    val += entry.modifier().value();
        return val;
    }

    @Unique
    private void renderScrollbar(DrawContext context, int panelX, int panelY, int panelWidth, int panelHeight,
            int mouseX, int mouseY, boolean leftClick) {
        int sbX = panelX + panelWidth - 12;
        int sbY = panelY + 18;
        int sbWidth = 6;
        int sbHeight = panelHeight - 36;

        int scrollOffset = StatsPanelScrollHandler.getScrollOffset();
        int maxScrollOffset = StatsPanelScrollHandler.getMaxScrollOffset();

        context.drawTexturedQuad(SCROLLBAR_TRACK_TEXTURE, sbX, sbY, sbX + sbWidth, sbY + sbHeight, 0.0F, 1.0F, 0.0F,
                1.0F);

        float scrollPercent = maxScrollOffset > 0 ? (float) scrollOffset / maxScrollOffset : 0;
        int thumbHeight = Math.max(20, sbHeight / 4);
        int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

        context.drawTexturedQuad(SCROLLBAR_THUMB_TEXTURE, sbX, thumbY, sbX + sbWidth, thumbY + thumbHeight, 0.0F, 1.0F,
                0.0F, 1.0F);

        if (leftClick) {
            if (!draggingScrollbar && mouseX >= sbX && mouseX <= sbX + sbWidth && mouseY >= sbY
                     && mouseY <= sbY + sbHeight) {
                draggingScrollbar = true;
            }
            if (draggingScrollbar) {
                float newPercent = Math.max(0,
                        Math.min(1, (float) (mouseY - sbY - thumbHeight / 2) / (sbHeight - thumbHeight)));
                StatsPanelScrollHandler.setScrollOffset((int) (newPercent * maxScrollOffset));
            }
        } else {
            draggingScrollbar = false;
        }
    }

    @Unique
    private int calculateTotalContentHeight() {
        int height = 0;

        // Offensive (1 + 23 stats)
        height += 11;
        height += 23 * 10;
        height += 6;

        // Defensive (1 + 11 stats)
        height += 11;
        height += 11 * 10;
        height += 6;

        // Utility (1 + 6 stats)
        height += 11;
        height += 6 * 10;
        height += 6;

        // Gathering (1 + 8 stats)
        height += 11;
        height += 8 * 10;

        return height;
    }
}
package com.cranks.dungeons.mixin;

import com.cranks.dungeons.client.StatsPanelScrollHandler;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.CombatUtility;
import com.cranks.dungeons.ailment.AilmentType;
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
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
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
                                MinecraftClient.getInstance().getSoundManager().play(
                                                PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
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

                        context.drawTexturedQuad(buttonTexture, buttonX, buttonY, buttonX + buttonWidth,
                                        buttonY + buttonHeight,
                                        0.0F, 1.0F, 0.0F, 1.0F);

                        // --- SKILL TREE BUTTON (Relocated next to Recipe Book) ---
                        int skillButtonX = x + 126;
                        int skillButtonY = y + 61;
                        int skillButtonWidth = 20;
                        int skillButtonHeight = 18;

                        boolean isHoveringSkill = mouseX >= skillButtonX && mouseX <= skillButtonX + skillButtonWidth &&
                                        mouseY >= skillButtonY && mouseY <= skillButtonY + skillButtonHeight;

                        if (isHoveringSkill && leftClick && !lastSkillClickState && !draggingScrollbar) {
                                MinecraftClient.getInstance().getSoundManager().play(
                                                PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
                                MinecraftClient.getInstance().setScreen(
                                                new com.cranks.dungeons.client.screen.PassiveSkillTreeScreen(screen));
                        }
                        lastSkillClickState = leftClick;

                        Identifier skillButtonTexture = isHoveringSkill ? SKILLS_BUTTON_HOVER : SKILLS_BUTTON_NORMAL;
                        context.drawTexturedQuad(skillButtonTexture, skillButtonX, skillButtonY,
                                        skillButtonX + skillButtonWidth, skillButtonY + skillButtonHeight,
                                        0.0F, 1.0F, 0.0F, 1.0F);

                        if (!statsVisible)
                                return;

                        int panelX = x + backgroundWidth + 2;
                        int panelY = y;
                        int panelWidth = 147;
                        int panelHeight = 166;

                        context.drawTexturedQuad(STATS_PANEL_TEXTURE, panelX, panelY, panelX + panelWidth,
                                        panelY + panelHeight,
                                        0.0F, 1.0F, 0.0F, 1.0F);

                        var player = MinecraftClient.getInstance().player;
                        if (player == null)
                                return;

                        hoveredStat = null;

                        int visibleHeight = panelHeight - 16;
                        int scrollOffset = StatsPanelScrollHandler.getScrollOffset();

                        context.enableScissor(panelX + 4, panelY + 4, panelX + panelWidth - 14,
                                        panelY + panelHeight - 4);

                        int curY = panelY + 8 - scrollOffset;

                        // --- CATEGORY 0: ATTRIBUTES ---
                        context.drawText(tr, Text.literal("Attributes"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Strength",
                                        player.getAttributeValue(ModAttributes.STRENGTH),
                                        cranks$getPassiveBase(player, ModAttributes.STRENGTH), "", mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Dexterity",
                                        player.getAttributeValue(ModAttributes.DEXTERITY),
                                        cranks$getPassiveBase(player, ModAttributes.DEXTERITY), "", mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Intelligence",
                                        player.getAttributeValue(ModAttributes.INTELLIGENCE),
                                        cranks$getPassiveBase(player, ModAttributes.INTELLIGENCE), "", mouseX, mouseY, panelX, panelWidth);
                        curY += 6;

                        ItemStack weapon = player.getMainHandStack();
                        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
                        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
                        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

                        // --- CATEGORY 1: LIFE ---
                        context.drawText(tr, Text.literal("Life"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health",
                                        player.getAttributeValue(EntityAttributes.MAX_HEALTH),
                                        cranks$getPassiveBase(player, EntityAttributes.MAX_HEALTH), " HP", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Health Regen.",
                                        player.getAttributeValue(ModAttributes.LIFE_REGENERATION),
                                        cranks$getPassiveBase(player, ModAttributes.LIFE_REGENERATION), " HP/s", mouseX,
                                        mouseY, panelX, panelWidth);
                        double baseLifeSteal = cranks$getPassiveBase(player, ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE);
                        double totalLifeSteal = player.getAttributeValue(ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:physical_damage_leeched_as_life", 0.06)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:physical_damage_leeched_as_life", 0.06);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Life Leech", totalLifeSteal * 100,
                                        baseLifeSteal * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY += 6;

                        // --- CATEGORY 2: ENERGY SHIELD ---
                        context.drawText(tr, Text.literal("Energy Shield"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Energy Shield",
                                        player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0 ? 0 : player.getAttributeValue(ModAttributes.ENERGY_SHIELD),
                                        player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0 ? 0 : cranks$getPassiveBase(player, ModAttributes.ENERGY_SHIELD), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY += 6;

                        // --- CATEGORY 3: ARMOUR ---
                        context.drawText(tr, Text.literal("Armour"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Armor",
                                        player.getAttributeValue(EntityAttributes.ARMOR),
                                        cranks$getPassiveBase(player, EntityAttributes.ARMOR), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY += 6;

                        // --- CATEGORY 4: EVASION ---
                        context.drawText(tr, Text.literal("Evasion"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion",
                                        player.getAttributeValue(ModAttributes.EVASION_RATING),
                                        cranks$getPassiveBase(player, ModAttributes.EVASION_RATING), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY += 6;

                        // --- CATEGORY 5: RESISTANCES ---
                        context.drawText(tr, Text.literal("Resistances"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Res",
                                        player.getAttributeValue(ModAttributes.FIRE_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.FIRE_RESISTANCE) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Res",
                                        player.getAttributeValue(ModAttributes.COLD_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.COLD_RESISTANCE) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Res",
                                        player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.LIGHTNING_RESISTANCE) * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Res",
                                        player.getAttributeValue(ModAttributes.VOID_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.VOID_RESISTANCE) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback Res",
                                        player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 6: CRITICAL ---
                        context.drawText(tr, Text.literal("Critical"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        // 4. Critical Strike
                        double baseCrit = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
                        double localCrit = com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(weapon, "local_crit_chance");
                        double globalCrit = player.getAttributeValue(ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT);
                        double critChance = (baseCrit * (1.0 + localCrit)) * (1.0 + globalCrit);
                        
                        double displayBaseCrit = cranks$getPassiveBase(player, ModAttributes.CRIT_CHANCE);
                        critChance += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:crit_chance", 0.075);
                        critChance += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:crit_chance", 0.075);
                        critChance += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, helmet, "minecraft:crit_chance", 0.075);
                        critChance += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, leggings, "minecraft:crit_chance", 0.075);
                        critChance += com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, boots, "minecraft:crit_chance", 0.075);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Chance", critChance * 100,
                                        displayBaseCrit * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);

                        double critMult = player.getAttributeValue(ModAttributes.CRIT_MULTIPLIER);
                        double baseCritMult = cranks$getPassiveBase(player, ModAttributes.CRIT_MULTIPLIER);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Multiplier", critMult * 100,
                                        baseCritMult * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 7: AILMENTS ---
                        context.drawText(tr, Text.literal("Ailments"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        // BURN
                        double baseBurn = cranks$getPassiveBase(player, ModAttributes.BURN_MAGNITUDE_PERCENT);
                        double totalBurn = player.getAttributeValue(ModAttributes.BURN_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:burn_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:burn_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Mag.", totalBurn * 100,
                                        baseBurn * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // POISON
                        double basePoison = cranks$getPassiveBase(player, ModAttributes.POISON_MAGNITUDE_PERCENT);
                        double totalPoison = player.getAttributeValue(ModAttributes.POISON_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:poison_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:poison_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Mag.", totalPoison * 100,
                                        basePoison * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // BLEED
                        double baseBleed = cranks$getPassiveBase(player, ModAttributes.BLEED_MAGNITUDE_PERCENT);
                        double totalBleed = player.getAttributeValue(ModAttributes.BLEED_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:bleed_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:bleed_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Mag.", totalBleed * 100,
                                        baseBleed * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // SHOCK
                        double baseShock = cranks$getPassiveBase(player, ModAttributes.SHOCK_MAGNITUDE_PERCENT);
                        double totalShock = player.getAttributeValue(ModAttributes.SHOCK_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:shock_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:shock_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Mag.", totalShock * 100,
                                        baseShock * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // FREEZE
                        double baseFreeze = cranks$getPassiveBase(player, ModAttributes.FREEZE_MAGNITUDE_PERCENT);
                        double totalFreeze = player.getAttributeValue(ModAttributes.FREEZE_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:freeze_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:freeze_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Mag.", totalFreeze * 100,
                                        baseFreeze * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // ELECTROCUTE
                        double baseElectrocute = cranks$getPassiveBase(player, ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT);
                        double totalElectrocute = player.getAttributeValue(ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:electrocute_magnitude_percent", 0.05)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:electrocute_magnitude_percent", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Electrocute Mag.", totalElectrocute * 100,
                                        baseElectrocute * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 8: MISC ---
                        context.drawText(tr, Text.literal("Misc"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        double passiveMovePercent = cranks$getPassivePercentBonus(player, ModAttributes.MOVEMENT_SPEED);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Move Speed",
                                        player.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) / 0.1 * 100,
                                        100.0 * (1.0 + passiveMovePercent),
                                        "%", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Flying Speed",
                                        player.getAttributeValue(ModAttributes.FLYING_SPEED) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.FLYING_SPEED) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Item Rarity",
                                        player.getAttributeValue(ModAttributes.ITEM_RARITY_PERCENT) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.ITEM_RARITY_PERCENT) * 100, "%", mouseX, mouseY,
                                        panelX,
                                        panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Exp Bonus",
                                        player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.EXPERIENCE_BONUS) * 100, "%",
                                        mouseX,
                                        mouseY, panelX, panelWidth);

                        double totalKB = player.getAttributeValue(ModAttributes.KNOCKBACK)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:bonus_knockback", 0.35)
                                        + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, chestplate, "minecraft:bonus_knockback", 0.35);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback", totalKB * 100,
                                        cranks$getPassiveBase(player, ModAttributes.KNOCKBACK) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 9: GATHERING ---
                        context.drawText(tr, Text.literal("Gathering"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        // Gathering Speed
                        double gatheringSpeed = player.getAttributeValue(ModAttributes.MINING_EFFICIENCY);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Gathering Speed",
                                        gatheringSpeed,
                                        cranks$getPassiveBase(player, ModAttributes.MINING_EFFICIENCY), "",
                                        mouseX, mouseY, panelX, panelWidth);

                        double precisionChance = 0.0;
                        var precisionInstance = player.getAttributeInstance(ModAttributes.PRECISION_MINING);
                        if (precisionInstance != null)
                                precisionChance = precisionInstance.getValue();
                        if (precisionChance <= 0.0)
                                precisionChance = ToolStatHelper.getPrecisionMining(weapon);

                        double basePrecision = cranks$getPassiveBase(player, ModAttributes.PRECISION_MINING);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Repair Chance", precisionChance * 100.0,
                                        basePrecision * 100.0, "%", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Range",
                                        player.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE)
                                                        + EquipmentStatApplier.getItemStatValue(weapon,
                                                                        "breaking_range"),
                                        cranks$getPassiveBase(player, EntityAttributes.ENTITY_INTERACTION_RANGE),
                                        " blocks", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Ore Fortune",
                                        player.getAttributeValue(ModAttributes.FORTUNE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.FORTUNE) * 100, "%", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Log Fortune",
                                        player.getAttributeValue(ModAttributes.LOG_FORTUNE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.LOG_FORTUNE) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Dig Fortune",
                                        player.getAttributeValue(ModAttributes.DIG_FORTUNE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.DIG_FORTUNE) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop Fortune",
                                        EquipmentStatApplier.getItemStatValue(weapon, "crop_fortune") * 100, 0.0, "%",
                                        mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crop XP",
                                        EquipmentStatApplier.getItemStatValue(weapon, "crop_experience"), 0.0, " xp",
                                        mouseX, mouseY, panelX, panelWidth);



                        // Update total content height for the next frame
                        int totalContentHeight = curY - (panelY + 8 - scrollOffset);
                        StatsPanelScrollHandler
                                        .setMaxScrollOffset(Math.max(0, totalContentHeight - (panelHeight - 16)));

                        // Debug log once per second
                        long now = System.currentTimeMillis();
                        if (now - lastLogTime > 1000) {
                                lastLogTime = now;
                                System.out.println("[StatsPanel] panelY=" + panelY + " panelHeight=" + panelHeight
                                                + " curY=" + curY + " scrollOffset="
                                                + StatsPanelScrollHandler.getScrollOffset()
                                                + " totalContentHeight=" + totalContentHeight
                                                + " maxScrollOffset=" + StatsPanelScrollHandler.getMaxScrollOffset());
                        }

                        context.disableScissor();

                        if (StatsPanelScrollHandler.getMaxScrollOffset() > 0) {
                                renderScrollbar(context, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY,
                                                leftClick);
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
                String baseStr = formatStatValue(statName, hoveredBaseVal);
                String equipVal = formatStatValue(statName, hoveredEquipVal);
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
                        case "Strength" -> "Every 10 Strength grants +5 Max Health";
                        case "Dexterity" -> "Every 10 Dexterity grants +20 Evasion Rating";
                        case "Intelligence" -> "Every 10 Intelligence grants +5 Maximum Mana";
                        case "Physical Damage" -> "Physical damage dealt per hit";
                        case "Total Damage" -> "Combined damage from physical and all elemental sources";
                        case "Elemental Damage" -> "Increased damage for all elements (Fire, Cold, Lightning, Void)";
                        case "One-Hand Bonus" -> "Increased damage when using one weapon and a shield/offhand item";
                        case "Dual Wield Bonus" -> "Increased damage when using two weapons of the same type";
                        case "Two-Handed Bonus" -> "Increased damage when using a weapon with the offhand empty";
                        case "Phys as Fire" -> "Gain % of Physical damage as extra Fire damage";
                        case "Phys as Cold" -> "Gain % of Physical damage as extra Cold damage";
                        case "Phys as Light." -> "Gain % of Physical damage as extra Lightning damage";
                        case "Phys as Void" -> "Gain % of Physical damage as extra Void damage";
                        case "Armor Pen" -> "Reduces the effectiveness of enemy armor";
                        case "Fire Pen" -> "Bypasses a portion of enemy Fire Resistance";
                        case "Cold Pen" -> "Bypasses a portion of enemy Cold Resistance";
                        case "Light. Pen" -> "Bypasses a portion of enemy Lightning Resistance";
                        case "Void Pen" -> "Bypasses a portion of enemy Void Resistance";
                        case "Dmg vs Afflicted" -> "Increased damage against enemies with ailments (Burn, Poison, etc)";
                        case "Dmg while Aff." -> "Increased damage dealt while YOU have an ailment";
                        case "Crit vs Afflicted" -> "Increased Critical Strike chance against enemies with ailments";
                        case "Attack Speed" -> "Number of attacks per second";
                        case "Crit Chance" -> "Chance to deal critical damage";
                        case "Crit Multiplier" -> "Multiplier applied to damage when you deal a critical strike";
                        case "Attack Range" -> "Maximum melee attack distance";
                        case "Knockback" -> "How far enemies are pushed back";
                        case "Reload Speed" -> "How much faster bows/crossbows charge";
                        case "Burn Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? AilmentType.BURN.getBaseDuration() * (1.0 + p.getAttributeValue(ModAttributes.BURN_DURATION)) : AilmentType.BURN.getBaseDuration();
                                yield "Scales damage per tick of Burn.\nFire hits ignite enemies for " + String.format("%.1f", dur) + "s based on hit damage.";
                        }
                        case "Poison Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? AilmentType.POISON.getBaseDuration() * (1.0 + p.getAttributeValue(ModAttributes.POISON_DURATION)) : AilmentType.POISON.getBaseDuration();
                                double max = p != null ? 20.0 + p.getAttributeValue(ModAttributes.POISON_MAX_STACKS) : 20.0;
                                yield "Scales damage per tick of Poison.\nPhysical/Void hits poison enemies for " + String.format("%.1f", dur) + "s (Max " + (int)max + ").";
                        }
                        case "Bleed Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? AilmentType.BLEED.getBaseDuration() * (1.0 + p.getAttributeValue(ModAttributes.BLEED_DURATION)) : AilmentType.BLEED.getBaseDuration();
                                yield "Scales Bleed damage.\nPhysical hits cause bleeding over " + String.format("%.1f", dur) + "s based on hit damage.";
                        }
                        case "Freeze Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? AilmentType.FREEZE.getBaseDuration() * (1.0 + p.getAttributeValue(ModAttributes.FREEZE_DURATION)) : AilmentType.FREEZE.getBaseDuration();
                                yield "Increases Freeze buildup on enemies.\nCold hits build up freeze to stun for " + String.format("%.1f", dur) + "s.";
                        }
                        case "Electrocute Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? 0.75 * (1.0 + p.getAttributeValue(ModAttributes.ELECTROCUTE_DURATION)) : 0.75;
                                yield "Increases Electrocute buildup on enemies.\nLightning hits build up electrocute to stun for " + String.format("%.2f", dur) + "s.";
                        }
                        case "Shock Mag." -> {
                                var p = MinecraftClient.getInstance().player;
                                double dur = p != null ? p.getAttributeValue(ModAttributes.SHOCK_DURATION) : 10.0;
                                yield "Increases Shock effect on enemies.\nLightning hits apply shock for " + String.format("%.1f", dur) + "s, increasing damage taken.";
                        }
                        case "Burn Magnitude (Legacy)" -> "Fire damage dealt per second by burning";
                        case "Burn Duration" -> "How long enemies stay ignited";
                        case "Poison Magnitude (Legacy)" -> "Void damage dealt per second by poison";
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
                        case "Health Regen." -> "Health restored per second";
                        case "Damage Leeched as Life" -> "% of damage converted to health";
                        case "Life Leech" -> "% of physical damage converted to health";
                        case "Evasion" -> calculateEvasionDescription();
                        case "Armor" -> calculateArmorDescription();
                        case "Fire Res" -> "Reduces fire damage taken";
                        case "Cold Res" -> "Reduces cold damage taken";
                        case "Lightning Res" -> "Reduces lightning damage taken";
                        case "Void Res" -> "Reduces void damage taken";
                        case "Knockback Res" -> "Resistance to being pushed";
                        case "Crit Defense" -> calculateCritDefenseDescription();
                        case "Move Speed" -> "Movement speed modifier";
                        case "Flying Speed" -> "Elytra flight speed bonus";
                        case "Item Rarity" -> "Increases rarity of items found";
                        case "Exp Bonus" -> "Extra experience gained";
                        case "Gathering Speed" -> "Overall speed multiplier for breaking and harvesting blocks";
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
                // Adjusted armor penalty (4.0x) to match new balancing
                return (int) (rating / (rating + 400.0 + armor * 4.0) * 100) + "% chance to dodge";
        }

        @Unique
        private String calculateArmorDescription() {
                var player = MinecraftClient.getInstance().player;
                if (player == null)
                        return "Reduces incoming physical damage";
                double armor = player.getAttributeValue(EntityAttributes.ARMOR);

                // Show reduction against a standard 30 damage hit (modded baseline)
                double x = 40.0 * 30.0;
                double pdr = armor / (armor + x);

                boolean capped = pdr >= 0.9;
                pdr = Math.min(0.9, pdr);

                return (int) (pdr * 100) + "% Physical Damage Reduction" + (capped ? " (Capped)" : "");
        }

        @Unique
        private String calculateCritDefenseDescription() {
                var player = MinecraftClient.getInstance().player;
                if (player == null)
                        return "Reduces extra damage from critical hits";
                double mitigationMultiplier = player.getAttributeValue(ModAttributes.ARMOR_CRIT_MITIGATION);
                double armor = player.getAttributeValue(EntityAttributes.ARMOR);
                double mitigation = Math.min(1.0, armor * mitigationMultiplier);

                return String.format("Reduces Critical Extra Damage by %.1f%% (%.2f%% per Armor)",
                                mitigation * 100.0, mitigationMultiplier * 100.0);
        }

        @Unique
        private double calculateFinalElementalDamage(
                        net.minecraft.entity.player.PlayerEntity player, ItemStack weapon, ItemStack chestplate,
                        double totalPhysicalDamage,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> damageAttribute,
                        String enchantmentId,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> physAsExtraAttribute) {
                double base = player.getAttributeValue(damageAttribute)
                                + com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, enchantmentId, 4.0);

                // Lookup corresponding percent attribute
                double pct = 0;
                if (damageAttribute.equals(ModAttributes.FIRE_DAMAGE))
                        pct = player.getAttributeValue(ModAttributes.FIRE_DAMAGE_PERCENT);
                else if (damageAttribute.equals(ModAttributes.COLD_DAMAGE))
                        pct = player.getAttributeValue(ModAttributes.COLD_DAMAGE_PERCENT);
                else if (damageAttribute.equals(ModAttributes.LIGHTNING_DAMAGE))
                        pct = player.getAttributeValue(ModAttributes.LIGHTNING_DAMAGE_PERCENT);
                else if (damageAttribute.equals(ModAttributes.VOID_DAMAGE))
                        pct = player.getAttributeValue(ModAttributes.VOID_DAMAGE_PERCENT);

                pct += player.getAttributeValue(ModAttributes.ELEMENTAL_DAMAGE_PERCENT);

                // Use a dummy damage source for the helper check
                net.minecraft.entity.damage.DamageSource dummySource = player.getDamageSources().playerAttack(player);
                if (CombatUtility.isMelee(weapon, dummySource)) {
                        pct += player.getAttributeValue(ModAttributes.MELEE_DAMAGE_PERCENT);
                } else if (CombatUtility.isRanged(weapon, dummySource)) {
                        pct += player.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);
                }

                if (CombatUtility.isTwoHanding(player, weapon)) {
                        pct += player.getAttributeValue(ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
                } else if (CombatUtility.isDualWielding(player, weapon)) {
                        pct += player.getAttributeValue(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
                } else if (CombatUtility.isOneHanding(player, weapon)) {
                        pct += player.getAttributeValue(ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
                }

                double finalElemental = base * (1.0 + pct);

                // Add Physical as Extra
                double physAsExtraPercent = player.getAttributeValue(physAsExtraAttribute);
                finalElemental += totalPhysicalDamage * physAsExtraPercent;

                return finalElemental;
        }

        @Unique
        private int drawStat(DrawContext context, TextRenderer tr, int x, int y, String name, double val,
                        String suffix) {
                context.drawText(tr, Text.literal(name + ": " + String.format("%.1f", val) + suffix), x + 8, y,
                                0xFF404040,
                                false);
                return y + 10;
        }

        @Unique
        private String formatStatValue(String statName, double value) {
            if ("Crop XP".equals(statName)) {
                return String.format(java.util.Locale.US, "%.2f", value);
            }
            if (value == Math.floor(value)) {
                return String.valueOf((int) value);
            }
            return String.format(java.util.Locale.US, "%.1f", value);
        }

        @Unique
        private int drawStatWithTooltip(DrawContext context, TextRenderer tr, int x, int y, String name, double total,
                        double base, String suffix, int mouseX, int mouseY, int panelX, int panelWidth) {
                String totalStr = formatStatValue(name, total) + suffix;
                String text = name + ": " + totalStr;
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

                context.drawTexturedQuad(SCROLLBAR_TRACK_TEXTURE, sbX, sbY, sbX + sbWidth, sbY + sbHeight, 0.0F, 1.0F,
                                0.0F,
                                1.0F);

                float scrollPercent = maxScrollOffset > 0 ? (float) scrollOffset / maxScrollOffset : 0;
                int thumbHeight = Math.max(20, sbHeight / 4);
                int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

                context.drawTexturedQuad(SCROLLBAR_THUMB_TEXTURE, sbX, thumbY, sbX + sbWidth, thumbY + thumbHeight,
                                0.0F, 1.0F,
                                0.0F, 1.0F);

                if (leftClick) {
                        if (!draggingScrollbar && mouseX >= sbX && mouseX <= sbX + sbWidth && mouseY >= sbY
                                        && mouseY <= sbY + sbHeight) {
                                draggingScrollbar = true;
                        }
                        if (draggingScrollbar) {
                                float newPercent = Math.max(0,
                                                Math.min(1, (float) (mouseY - sbY - thumbHeight / 2)
                                                                / (sbHeight - thumbHeight)));
                                StatsPanelScrollHandler.setScrollOffset((int) (newPercent * maxScrollOffset));
                        }
                } else {
                        draggingScrollbar = false;
                }
        }

        @Unique
        private double cranks$getPassiveBase(net.minecraft.entity.LivingEntity player,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
                double baseVal = player.getAttributeBaseValue(attribute);
                var instance = player.getAttributeInstance(attribute);
                if (instance != null) {
                        for (var mod : instance.getModifiers()) {
                                String path = mod.id().getPath();
                                if (mod.id().getNamespace().equals("com.cranks.dungeons")
                                                || mod.id().getNamespace().equals("cranks-dungeons")) {
                                        if (path.startsWith("passive_") || path.endsWith("_passive_percent")
                                                        || path.equals("max_health_bonus")
                                                        || path.equals("bonus_armor")
                                                        || path.equals("bonus_attack_damage")
                                                        || path.equals("leggings_armor_bonus")) {
                                                baseVal += mod.value();
                                        }
                                }
                        }
                }
                return baseVal;
        }

        @Unique
        private double cranks$getPassivePercentBonus(net.minecraft.entity.LivingEntity player,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
                double sum = 0;
                var instance = player.getAttributeInstance(attribute);
                if (instance != null) {
                        for (var mod : instance.getModifiers()) {
                                String path = mod.id().getPath();
                                if (mod.id().getNamespace().equals("com.cranks.dungeons")
                                                || mod.id().getNamespace().equals("cranks-dungeons")) {
                                        if (path.startsWith("passive_") || path.endsWith("_passive_percent")
                                                        || path.equals("max_health_bonus")
                                                        || path.equals("bonus_armor")
                                                        || path.equals("bonus_attack_damage")
                                                        || path.equals("leggings_armor_bonus")) {
                                                sum += mod.value();
                                        }
                                }
                        }
                }
                return sum;
        }
}

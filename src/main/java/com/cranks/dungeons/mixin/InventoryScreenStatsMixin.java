package com.cranks.dungeons.mixin;

import com.cranks.dungeons.client.StatsPanelScrollHandler;
import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.equipment.ItemStatManager;
import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.CombatUtility;
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

                        // --- CATEGORY 1: OFFENSIVE ---
                        context.drawText(tr, Text.literal("⚔ Offensive"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        ItemStack weapon = player.getMainHandStack();
                        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
                        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
                        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

                        // 1. Physical Damage
                        double baseAD = cranks$getPassiveBase(player, EntityAttributes.ATTACK_DAMAGE);
                        double bonusAD = player.getAttributeValue(ModAttributes.BONUS_ATTACK_DAMAGE);
                        double weaponAD = getModifierFromStack(weapon, EntityAttributes.ATTACK_DAMAGE);

                        double typeBonus = 0;
                        double passiveTypeBonus = 0;
                        // Use a dummy damage source for the helper check
                        net.minecraft.entity.damage.DamageSource dummySource = player.getDamageSources()
                                        .playerAttack(player);
                        if (CombatUtility.isMelee(weapon, dummySource)) {
                                typeBonus = player.getAttributeValue(ModAttributes.MELEE_DAMAGE_PERCENT);
                                passiveTypeBonus = cranks$getPassivePercentBonus(player,
                                                ModAttributes.MELEE_DAMAGE_PERCENT);
                        } else if (CombatUtility.isRanged(weapon, dummySource)) {
                                typeBonus = player.getAttributeValue(ModAttributes.RANGED_DAMAGE_PERCENT);
                                passiveTypeBonus = cranks$getPassivePercentBonus(player,
                                                ModAttributes.RANGED_DAMAGE_PERCENT);
                        }

                        double wieldingBonus = 0;
                        double passiveWieldingBonus = 0;
                        if (CombatUtility.isTwoHanding(player, weapon)) {
                                wieldingBonus = player.getAttributeValue(ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
                                passiveWieldingBonus = cranks$getPassivePercentBonus(player,
                                                ModAttributes.TWO_HANDING_DAMAGE_PERCENT);
                        } else if (CombatUtility.isDualWielding(player, weapon)) {
                                wieldingBonus = player.getAttributeValue(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
                                passiveWieldingBonus = cranks$getPassivePercentBonus(player,
                                                ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT);
                        } else if (CombatUtility.isOneHanding(player, weapon)) {
                                wieldingBonus = player.getAttributeValue(ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
                                passiveWieldingBonus = cranks$getPassivePercentBonus(player,
                                                ModAttributes.ONE_HANDED_DAMAGE_PERCENT);
                        }

                        double attackDamage = (baseAD + bonusAD + weaponAD) * (1.0
                                        + player.getAttributeValue(ModAttributes.ATTACK_DAMAGE_PERCENT) + typeBonus
                                        + wieldingBonus);

                        // Tooltip attribution: Base includes flat bonus and the tree's percentage
                        // scaling
                        double passiveADPercent = cranks$getPassivePercentBonus(player,
                                        ModAttributes.ATTACK_DAMAGE_PERCENT);
                        double tooltipBaseAD = (baseAD + bonusAD)
                                        * (1.0 + passiveADPercent + passiveTypeBonus + passiveWieldingBonus);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Physical Damage", attackDamage,
                                        tooltipBaseAD,
                                        "", mouseX,
                                        mouseY, panelX, panelWidth);

                        // 2. Elemental Damages (Moved here)
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Fire Damage",
                                        calculateFinalElementalDamage(player, weapon, chestplate, attackDamage,
                                                        ModAttributes.FIRE_DAMAGE, "minecraft:fire_damage",
                                                        ModAttributes.PHYSICAL_AS_EXTRA_FIRE_PERCENT),
                                        cranks$getPassiveBase(player, ModAttributes.FIRE_DAMAGE) * (1.0
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.FIRE_DAMAGE_PERCENT)
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.ELEMENTAL_DAMAGE_PERCENT)),
                                        "", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Cold Damage",
                                        calculateFinalElementalDamage(player, weapon, chestplate, attackDamage,
                                                        ModAttributes.COLD_DAMAGE, "minecraft:cold_damage",
                                                        ModAttributes.PHYSICAL_AS_EXTRA_COLD_PERCENT),
                                        cranks$getPassiveBase(player, ModAttributes.COLD_DAMAGE) * (1.0
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.COLD_DAMAGE_PERCENT)
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.ELEMENTAL_DAMAGE_PERCENT)),
                                        "", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Lightning Damage",
                                        calculateFinalElementalDamage(player, weapon, chestplate, attackDamage,
                                                        ModAttributes.LIGHTNING_DAMAGE, "minecraft:lightning_damage",
                                                        ModAttributes.PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT),
                                        cranks$getPassiveBase(player, ModAttributes.LIGHTNING_DAMAGE) * (1.0
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.LIGHTNING_DAMAGE_PERCENT)
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.ELEMENTAL_DAMAGE_PERCENT)),
                                        "", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Void Damage",
                                        calculateFinalElementalDamage(player, weapon, chestplate, attackDamage,
                                                        ModAttributes.VOID_DAMAGE, "minecraft:void_damage",
                                                        ModAttributes.PHYSICAL_AS_EXTRA_VOID_PERCENT),
                                        cranks$getPassiveBase(player, ModAttributes.VOID_DAMAGE) * (1.0
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.VOID_DAMAGE_PERCENT)
                                                        + cranks$getPassivePercentBonus(player,
                                                                        ModAttributes.ELEMENTAL_DAMAGE_PERCENT)),
                                        "", mouseX, mouseY, panelX, panelWidth);

                        double elementalDmgVal = player.getAttributeValue(ModAttributes.ELEMENTAL_DAMAGE_PERCENT);
                        double passiveElemental = cranks$getPassivePercentBonus(player,
                                        ModAttributes.ELEMENTAL_DAMAGE_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Elemental Damage", elementalDmgVal * 100,
                                        passiveElemental * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        // 3. Combat Speed & Range
                        double totalAS = player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
                        double asMultiplier = (1.0 + player.getAttributeValue(ModAttributes.ATTACK_SPEED));
                        double finalAS = totalAS * asMultiplier;

                        double baseASOnly = player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED);
                        double passiveASMultiplier = (1.0
                                        + cranks$getPassivePercentBonus(player, ModAttributes.ATTACK_SPEED));

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Speed", finalAS,
                                        baseASOnly * passiveASMultiplier, "",
                                        mouseX,
                                        mouseY, panelX, panelWidth);

                        double totalReload = player.getAttributeValue(ModAttributes.RELOAD_SPEED)
                                        + ItemStatManager.getTotalStatValue(weapon, "reload_speed");
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Reload Speed", totalReload,
                                        cranks$getPassiveBase(player, ModAttributes.RELOAD_SPEED), "x", 
                                        mouseX, mouseY, panelX, panelWidth);

                        double baseAttackRange = cranks$getPassiveBase(player,
                                        EntityAttributes.ENTITY_INTERACTION_RANGE);
                        double attrBonus = 0.0;
                        var instRange = player.getAttributeInstance(ModAttributes.ATTACK_RANGE);
                        if (instRange != null)
                                attrBonus = instRange.getValue();
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Attack Range",
                                        baseAttackRange + attrBonus
                                                        + getEnchantmentValue(weapon, "minecraft:attack_range", 1.0),
                                        baseAttackRange, " blocks", mouseX, mouseY, panelX, panelWidth);

                        // 4. Critical Strike
                        double critChance = player.getAttributeValue(ModAttributes.CRIT_CHANCE);
                        double baseCrit = cranks$getPassiveBase(player, ModAttributes.CRIT_CHANCE);
                        critChance += getEnchantmentValue(weapon, "minecraft:crit_chance", 0.075);
                        critChance += getEnchantmentValue(chestplate, "minecraft:crit_chance", 0.075);
                        critChance += getEnchantmentValue(helmet, "minecraft:crit_chance", 0.075);
                        critChance += getEnchantmentValue(leggings, "minecraft:crit_chance", 0.075);
                        critChance += getEnchantmentValue(boots, "minecraft:crit_chance", 0.075);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Chance", critChance * 100,
                                        baseCrit * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);

                        double critMult = player.getAttributeValue(ModAttributes.CRIT_MULTIPLIER);
                        double baseCritMult = cranks$getPassiveBase(player, ModAttributes.CRIT_MULTIPLIER);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Crit Multiplier", critMult * 100,
                                        baseCritMult * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);

                        // 5. Ailments (Restored secondary stats)
                        // BURN
                        double baseBurn = cranks$getPassiveBase(player, ModAttributes.CHANCE_TO_BURN);
                        double totalBurn = player.getAttributeValue(ModAttributes.CHANCE_TO_BURN)
                                        + getEnchantmentValue(weapon, "minecraft:chance_to_burn", 0.05)
                                        + getEnchantmentValue(chestplate, "minecraft:chance_to_burn", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Chance", totalBurn * 100,
                                        baseBurn * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        double defaultBurnBase = ModAttributes.BURN_BASE_DAMAGE.value().getDefaultValue();
                        double burnPctVal = player.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT);
                        double passiveBurnPct = cranks$getPassivePercentBonus(player,
                                        ModAttributes.BURN_DAMAGE_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Damage",
                                        player.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE) * (1.0 + burnPctVal),
                                        cranks$getPassiveBase(player, ModAttributes.BURN_BASE_DAMAGE)
                                                        * (1.0 + passiveBurnPct),
                                        "", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Burn Duration",
                                        4.0 * (1.0 + player.getAttributeValue(ModAttributes.BURN_DURATION)),
                                        4.0 * (1.0 + cranks$getPassivePercentBonus(player,
                                                        ModAttributes.BURN_DURATION)),
                                        "s",
                                        mouseX, mouseY, panelX, panelWidth);

                        // POISON
                        double basePoison = cranks$getPassiveBase(player, ModAttributes.CHANCE_TO_POISON);
                        double totalPoison = player.getAttributeValue(ModAttributes.CHANCE_TO_POISON)
                                        + getEnchantmentValue(weapon, "minecraft:chance_to_poison", 0.05)
                                        + getEnchantmentValue(chestplate, "minecraft:chance_to_poison", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Chance", totalPoison * 100,
                                        basePoison * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        double defaultPoisonBase = ModAttributes.POISON_BASE_DAMAGE.value().getDefaultValue();
                        double poisonPctVal = player.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT);
                        double passivePoisonPct = cranks$getPassivePercentBonus(player,
                                        ModAttributes.POISON_DAMAGE_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Damage",
                                        player.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE)
                                                        * (1.0 + poisonPctVal),
                                        cranks$getPassiveBase(player, ModAttributes.POISON_BASE_DAMAGE)
                                                        * (1.0 + passivePoisonPct),
                                        "", mouseX, mouseY, panelX,
                                        panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Duration",
                                        2.0 * (1.0 + player.getAttributeValue(ModAttributes.POISON_DURATION)),
                                        2.0 * (1.0 + cranks$getPassivePercentBonus(player,
                                                        ModAttributes.POISON_DURATION)),
                                        "s",
                                        mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Poison Stacks",
                                        20.0 + player.getAttributeValue(ModAttributes.POISON_MAX_STACKS),
                                        20.0 + cranks$getPassiveBase(player, ModAttributes.POISON_MAX_STACKS), " max",
                                        mouseX, mouseY, panelX, panelWidth);

                        // BLEED
                        double baseBleed = cranks$getPassiveBase(player, ModAttributes.CHANCE_TO_BLEED);
                        double totalBleed = player.getAttributeValue(ModAttributes.CHANCE_TO_BLEED)
                                        + getEnchantmentValue(weapon, "minecraft:chance_to_bleed", 0.05)
                                        + getEnchantmentValue(chestplate, "minecraft:chance_to_bleed", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Chance", totalBleed * 100,
                                        baseBleed * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Bleed Duration",
                                        5.0 * (1.0 + player.getAttributeValue(ModAttributes.BLEED_DURATION)),
                                        5.0 * (1.0 + cranks$getPassivePercentBonus(player,
                                                        ModAttributes.BLEED_DURATION)),
                                        "s",
                                        mouseX, mouseY, panelX, panelWidth);

                        // FREEZE
                        double baseFreeze = cranks$getPassiveBase(player, ModAttributes.CHANCE_TO_FREEZE);
                        double totalFreeze = player.getAttributeValue(ModAttributes.CHANCE_TO_FREEZE)
                                        + getEnchantmentValue(weapon, "minecraft:chance_to_freeze", 0.05)
                                        + getEnchantmentValue(chestplate, "minecraft:chance_to_freeze", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Chance", totalFreeze * 100,
                                        baseFreeze * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Freeze Duration",
                                        4.0 * (1.0 + player.getAttributeValue(ModAttributes.FREEZE_DURATION)),
                                        4.0 * (1.0 + cranks$getPassivePercentBonus(player,
                                                        ModAttributes.FREEZE_DURATION)),
                                        "s",
                                        mouseX, mouseY, panelX, panelWidth);

                        // SHOCK
                        double baseShock = cranks$getPassiveBase(player, ModAttributes.CHANCE_TO_SHOCK);
                        double totalShock = player.getAttributeValue(ModAttributes.CHANCE_TO_SHOCK)
                                        + getEnchantmentValue(weapon, "minecraft:chance_to_shock", 0.05)
                                        + getEnchantmentValue(chestplate, "minecraft:chance_to_shock", 0.05);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Chance", totalShock * 100,
                                        baseShock * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Shock Duration",
                                        player.getAttributeValue(ModAttributes.SHOCK_DURATION),
                                        cranks$getPassiveBase(player, ModAttributes.SHOCK_DURATION), "s", mouseX,
                                        mouseY,
                                        panelX, panelWidth);

                        // 6. Life Steal
                        double baseLifeSteal = cranks$getPassiveBase(player, ModAttributes.LIFE_STEAL);
                        double totalLifeSteal = player.getAttributeValue(ModAttributes.LIFE_STEAL)
                                        + getEnchantmentValue(weapon, "minecraft:life_steal", 0.06)
                                        + getEnchantmentValue(chestplate, "minecraft:life_steal", 0.06);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Life Steal", totalLifeSteal * 100,
                                        baseLifeSteal * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 1.5: ELEMENTAL REACTIONS ---
                        context.drawText(tr, Text.literal("✧ Reactions"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        double baseReactionDmg = cranks$getPassiveBase(player, ModAttributes.REACTION_DAMAGE_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Reaction Damage",
                                        player.getAttributeValue(ModAttributes.REACTION_DAMAGE_PERCENT) * 100,
                                        baseReactionDmg * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        double baseReactionRadius = cranks$getPassiveBase(player,
                                        ModAttributes.REACTION_RADIUS_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Reaction Radius",
                                        player.getAttributeValue(ModAttributes.REACTION_RADIUS_PERCENT) * 100,
                                        baseReactionRadius * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        double baseReactionCooldown = cranks$getPassiveBase(player,
                                        ModAttributes.REACTION_COOLDOWN_PERCENT);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Reaction CDR",
                                        player.getAttributeValue(ModAttributes.REACTION_COOLDOWN_PERCENT) * 100,
                                        baseReactionCooldown * 100, "%", mouseX, mouseY, panelX, panelWidth);

                        curY += 6;

                        // --- CATEGORY 2: DEFENSIVE ---
                        context.drawText(tr, Text.literal("🛡 Defensive"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health",
                                        player.getAttributeValue(EntityAttributes.MAX_HEALTH),
                                        cranks$getPassiveBase(player, EntityAttributes.MAX_HEALTH), " HP", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Health Regeneration",
                                        player.getAttributeValue(ModAttributes.LIFE_REGENERATION),
                                        cranks$getPassiveBase(player, ModAttributes.LIFE_REGENERATION), " HP/s", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Armor",
                                        player.getAttributeValue(EntityAttributes.ARMOR),
                                        cranks$getPassiveBase(player, EntityAttributes.ARMOR), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion",
                                        player.getAttributeValue(ModAttributes.EVASION_RATING),
                                        cranks$getPassiveBase(player, ModAttributes.EVASION_RATING), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Energy Shield",
                                        player.getAttributeValue(ModAttributes.ENERGY_SHIELD),
                                        cranks$getPassiveBase(player, ModAttributes.ENERGY_SHIELD), "", mouseX, mouseY,
                                        panelX, panelWidth);

                        // Resistances Group
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

                        // --- CATEGORY 3: UTILITY ---
                        context.drawText(tr, Text.literal("✦ Utility"), panelX + 6, curY, 0xFF404040, false);
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

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Luck",
                                        player.getAttributeValue(EntityAttributes.LUCK),
                                        cranks$getPassiveBase(player, EntityAttributes.LUCK), "", mouseX, mouseY,
                                        panelX,
                                        panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Exp Bonus",
                                        player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.EXPERIENCE_BONUS) * 100, "%",
                                        mouseX,
                                        mouseY, panelX, panelWidth);

                        double totalKB = player.getAttributeValue(ModAttributes.KNOCKBACK)
                                        + getEnchantmentValue(weapon, "minecraft:bonus_knockback", 0.35)
                                        + getEnchantmentValue(chestplate, "minecraft:bonus_knockback", 0.35);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback", totalKB * 100,
                                        cranks$getPassiveBase(player, ModAttributes.KNOCKBACK) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);



                        curY += 6;

                        // --- CATEGORY 4: GATHERING ---
                        context.drawText(tr, Text.literal("⚒ Gathering"), panelX + 6, curY, 0xFF404040, false);
                        curY += 11;

                        double totalHarvest = player.getAttributeValue(ModAttributes.MINING_EFFICIENCY)
                                        * (1.0 + ItemStatManager.getTotalStatValue(weapon, "mining_efficiency"));
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Harvest Speed",
                                        totalHarvest,
                                        cranks$getPassiveBase(player, ModAttributes.MINING_EFFICIENCY), "x",
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
                        case "Burn Chance" -> {
                                var p = MinecraftClient.getInstance().player;
                                double baseDmg = p != null ? p.getAttributeValue(ModAttributes.BURN_BASE_DAMAGE) : 4.0;
                                double pct = p != null ? p.getAttributeValue(ModAttributes.BURN_DAMAGE_PERCENT) : 100.0;
                                yield "Chance to ignite enemies. Deal " + String.format("%.1f", baseDmg * (1.0 + pct))
                                                + " Fire dmg/s for 4.0s";
                        }
                        case "Poison Chance" -> {
                                var p = MinecraftClient.getInstance().player;
                                double baseDmg = p != null ? p.getAttributeValue(ModAttributes.POISON_BASE_DAMAGE)
                                                : 1.0;
                                double pct = p != null ? p.getAttributeValue(ModAttributes.POISON_DAMAGE_PERCENT) : 0.0;
                                yield "Chance to poison enemies. Deal "
                                                + String.format("%.1f", baseDmg * (1.0 + pct))
                                                + " Void dmg/s for 2.0s (Max 20)";
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
                        case "Fire Res" -> "Reduces fire damage taken";
                        case "Cold Res" -> "Reduces cold damage taken";
                        case "Lightning Res" -> "Reduces lightning damage taken";
                        case "Void Res" -> "Reduces void damage taken";
                        case "Knockback Res" -> "Resistance to being pushed";
                        case "Crit Defense" -> calculateCritDefenseDescription();
                        case "Move Speed" -> "Movement speed modifier";
                        case "Flying Speed" -> "Elytra flight speed bonus";
                        case "Luck" -> "Increases rarity of drops";
                        case "Exp Bonus" -> "Extra experience gained";
                        case "Harvest Speed" -> "Overall speed multiplier for breaking and harvesting blocks";
                        case "Repair Chance" -> "Chance that tools avoid durability loss";
                        case "Ore Fortune" -> "Chance to double drops from ores";
                        case "Crop Fortune" -> "Chance to double drops from crops";
                        case "Log Fortune" -> "Chance to gain extra logs from trees";
                        case "Dig Fortune" -> "Chance to gain extra materials";
                        case "Crop XP" -> "Experience gained from harvesting crops with a hoe";
                        case "Range" -> "Block interaction distance";
                        case "Reaction Damage" -> "Increases damage of elemental reactions (Shatter, Miasma, etc.)";
                        case "Reaction Radius" -> "Increases the area affected by reaction explosions and clouds";
                        case "Reaction CDR" -> "Reduces the cooldown before a reaction can trigger again";
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

                return (int) (pdr * 100) + "% General Physical Damage Reduction against 30 damage" + (capped ? " (Capped)" : "");
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
        private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
                var enchantments = stack.getEnchantments();
                for (var entry : enchantments.getEnchantmentEntries()) {
                        if (entry.getKey().getKey().get().getValue().toString().equals(enchantmentId))
                                return baseValue * entry.getIntValue();
                }
                return 0.0;
        }

        @Unique
        private double calculateFinalElementalDamage(
                        net.minecraft.entity.player.PlayerEntity player, ItemStack weapon, ItemStack chestplate,
                        double totalPhysicalDamage,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> damageAttribute,
                        String enchantmentId,
                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> physAsExtraAttribute) {
                double base = player.getAttributeValue(damageAttribute)
                                + getEnchantmentValue(weapon, enchantmentId, 4.0);

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
                                                        || path.equals("bonus_armor_toughness")
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
                                                        || path.equals("bonus_armor_toughness")
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

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
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenStatsMixin {

        @Unique
        private static Identifier STATS_PANEL_BACKGROUND;
        @Unique
        private static Identifier SCROLLER_TEXTURE;
        @Unique
        private static final Identifier PAGE_FORWARD = Identifier.ofVanilla("recipe_book/page_forward");
        @Unique
        private static final Identifier PAGE_FORWARD_HIGHLIGHTED = Identifier.ofVanilla("recipe_book/page_forward_highlighted");
        @Unique
        private static final Identifier PAGE_BACKWARD = Identifier.ofVanilla("recipe_book/page_backward");
        @Unique
        private static final Identifier PAGE_BACKWARD_HIGHLIGHTED = Identifier.ofVanilla("recipe_book/page_backward_highlighted");

        // COMPENDIUM BUTTON (replaces separate Passive Tree and World Tree buttons)
        @Unique
        private static Identifier COMPENDIUM_BUTTON_NORMAL;
        @Unique
        private static Identifier COMPENDIUM_BUTTON_HOVER;

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
        private boolean lastCompendiumClickState = false;
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

        @Unique
        private int realMouseX = -1;
        @Unique
        private int realMouseY = -1;

        @Unique
        private boolean isMouseOverPanel(double mouseX, double mouseY) {
                if (!statsVisible) return false;
                InventoryScreen screen = (InventoryScreen) (Object) this;
                int x = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getX();
                int y = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getY();
                int backgroundWidth = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getBackgroundWidth();

                int panelX = x + backgroundWidth - 1;
                int panelY = y;
                int panelWidth = 158;
                int panelHeight = 166;

                return mouseX >= panelX && mouseX <= panelX + panelWidth &&
                       mouseY >= panelY && mouseY <= panelY + panelHeight;
        }

        @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
        private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
                if (isMouseOverPanel(mouseX, mouseY)) {
                        cir.setReturnValue(true);
                }
        }

        // Debug: log once per second so we don't spam
        @Unique
        private static long lastLogTime = 0;

        @Inject(method = "init", at = @At("HEAD"))
        private void onInitStatsScreen(CallbackInfo ci) {
                var rm = MinecraftClient.getInstance().getResourceManager();
                
                STATS_PANEL_BACKGROUND = rm.getResource(Identifier.of("cranks-dungeons", "textures/gui/sprites/stats_panel_background.png")).isPresent() ? 
                                Identifier.of("cranks-dungeons", "stats_panel_background") : Identifier.ofVanilla("social_interactions/background");
                
                SCROLLER_TEXTURE = rm.getResource(Identifier.of("cranks-dungeons", "textures/gui/sprites/stats_scroller.png")).isPresent() ? 
                                Identifier.of("cranks-dungeons", "stats_scroller") : Identifier.ofVanilla("container/creative_inventory/scroller");
                
                COMPENDIUM_BUTTON_NORMAL = rm.getResource(Identifier.of("cranks-dungeons", "textures/gui/sprites/compendium_button.png")).isPresent() ? 
                                Identifier.of("cranks-dungeons", "compendium_button") : Identifier.ofVanilla("recipe_book/button");
                
                COMPENDIUM_BUTTON_HOVER = rm.getResource(Identifier.of("cranks-dungeons", "textures/gui/sprites/compendium_button_highlighted.png")).isPresent() ? 
                                Identifier.of("cranks-dungeons", "compendium_button_highlighted") : Identifier.ofVanilla("recipe_book/button_highlighted");
        }

        @Inject(method = "drawBackground", at = @At("TAIL"))
        private void renderStatsPanel(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
                InventoryScreen screen = (InventoryScreen) (Object) this;

                try {
                        context.getMatrices().push();
                        context.getMatrices().translate(0.0f, 0.0f, 250.0f);

                        int x = ((HandledScreenAccessor) screen).getX();
                        int y = ((HandledScreenAccessor) screen).getY();
                        int backgroundWidth = ((HandledScreenAccessor) screen).getBackgroundWidth();
                        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

                        int buttonWidth = 12;
                        int buttonHeight = 17;
                        
                        // Center around the original intended position
                        int buttonX = x + backgroundWidth - 14 - (buttonWidth / 2);
                        int buttonY = y + 13 - (buttonHeight / 2);

                        boolean isHovering = mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                                        mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

                        boolean leftClick = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                                        MinecraftClient.getInstance().getWindow().getHandle(),
                                        org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

                        if (isHovering && leftClick && !lastClickState && !draggingScrollbar) {
                                statsVisible = !statsVisible;
                                StatsPanelScrollHandler.setStatsVisible(statsVisible);
                                MinecraftClient.getInstance().getSoundManager().play(
                                                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
                                System.out.println("[StatsPanel] Toggled statsVisible=" + statsVisible);
                        }
                        lastClickState = leftClick;

                        Identifier buttonTexture;
                        if (statsVisible) {
                                buttonTexture = isHovering ? PAGE_BACKWARD_HIGHLIGHTED : PAGE_BACKWARD;
                        } else {
                                buttonTexture = isHovering ? PAGE_FORWARD_HIGHLIGHTED : PAGE_FORWARD;
                        }

                        context.drawGuiTexture(buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight);

                        // --- COMPENDIUM BUTTON (Replaces separate Passive Tree and World Tree buttons) ---
                        int compButtonWidth = 20;
                        int compButtonHeight = 18;
                        int compButtonX = buttonX + (buttonWidth / 2) - (compButtonWidth / 2) + 4; // Centered above stats button, shifted 4px right
                        int compButtonY = y - compButtonHeight + 1; // Sits outside the inventory on the top edge, shifted 1px down

                        boolean isHoveringComp = mouseX >= compButtonX && mouseX <= compButtonX + compButtonWidth &&
                                        mouseY >= compButtonY && mouseY <= compButtonY + compButtonHeight;

                        if (isHoveringComp && leftClick && !lastCompendiumClickState && !draggingScrollbar) {
                                MinecraftClient.getInstance().getSoundManager().play(
                                                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
                                MinecraftClient.getInstance().setScreen(
                                                new com.cranks.dungeons.client.compendium.CompendiumScreen(null));
                        }
                        lastCompendiumClickState = leftClick;

                        Identifier compButtonTexture = isHoveringComp ? COMPENDIUM_BUTTON_HOVER : COMPENDIUM_BUTTON_NORMAL;
                        context.drawGuiTexture(compButtonTexture, compButtonX, compButtonY, compButtonWidth, compButtonHeight);

                        if (!statsVisible)
                                return;

                        int panelX = x + backgroundWidth - 1;
                        int panelY = y;
                        int panelWidth = 158;
                        int panelHeight = 166;

                        context.drawGuiTexture(STATS_PANEL_BACKGROUND, panelX, panelY, panelWidth, panelHeight);

                        var player = MinecraftClient.getInstance().player;
                        if (player == null)
                                return;

                        hoveredStat = null;

                        int visibleHeight = panelHeight - 16;
                        int scrollOffset = StatsPanelScrollHandler.getScrollOffset();

                        context.enableScissor(panelX + 4, panelY + 8, panelX + panelWidth - 14,
                                        panelY + panelHeight - 8);

                        int curY = panelY + 12 - scrollOffset;

                        // --- HEADER ---
                        context.drawText(tr, Text.literal(player.getName().getString()), panelX + 12, curY, 0xFFFFFFFF, false);
                        curY += 11;
                        String playerClass = com.cranks.dungeons.component.ModComponents.PASSIVE_DATA.get(player).getPlayerClass();
                        if (playerClass == null || playerClass.isEmpty()) {
                                playerClass = "Class";
                        } else {
                                playerClass = playerClass.substring(0, 1).toUpperCase() + playerClass.substring(1);
                        }
                        context.drawText(tr, Text.literal("Level " + player.experienceLevel + " " + playerClass), panelX + 12, curY, 0xFFA0A0A0, false);
                        curY += 16;

                        // --- ATTRIBUTES ---
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Strength",
                                        player.getAttributeValue(ModAttributes.STRENGTH),
                                        cranks$getPassiveBase(player, ModAttributes.STRENGTH), "", mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Dexterity",
                                        player.getAttributeValue(ModAttributes.DEXTERITY),
                                        cranks$getPassiveBase(player, ModAttributes.DEXTERITY), "", mouseX, mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Intelligence",
                                        player.getAttributeValue(ModAttributes.INTELLIGENCE),
                                        cranks$getPassiveBase(player, ModAttributes.INTELLIGENCE), "", mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- HEALTH ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Health");
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Health",
                                        player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH),
                                        cranks$getPassiveBase(player, EntityAttributes.GENERIC_MAX_HEALTH), "", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Health Regen",
                                        player.getAttributeValue(ModAttributes.LIFE_REGENERATION),
                                        cranks$getPassiveBase(player, ModAttributes.LIFE_REGENERATION), " / Sec", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- ENERGY SHIELD ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Energy Shield");
                        double maxShield = player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0 ? 0 : player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
                        double baseMaxShield = player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0 ? 0 : cranks$getPassiveBase(player, ModAttributes.ENERGY_SHIELD);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Energy Shield",
                                        maxShield,
                                        baseMaxShield, "", mouseX, mouseY,
                                        panelX, panelWidth);
                        
                        double rechargeRate = maxShield * (0.125 * (1.0 + player.getAttributeValue(ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT)));
                        double baseRechargeRate = baseMaxShield * (0.125 * (1.0 + cranks$getPassiveBase(player, ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT)));
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "ES Recharge / Sec",
                                        rechargeRate,
                                        baseRechargeRate, "", mouseX, mouseY,
                                        panelX, panelWidth);

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "ES Recharge Delay",
                                        2.0, Double.NaN, "s", mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- MANA ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Mana");
                        double maxManaStat = player.getAttributeValue(ModAttributes.MAX_MANA);
                        double baseMaxManaStat = cranks$getPassiveBase(player, ModAttributes.MAX_MANA);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Max Mana",
                                        maxManaStat,
                                        baseMaxManaStat, "", mouseX, mouseY, panelX, panelWidth);

                        double manaRegenPercent = player.getAttributeValue(ModAttributes.MANA_REGENERATION_PERCENT);
                        double manaRegenFlat = maxManaStat * manaRegenPercent;
                        double baseManaRegenPercent = cranks$getPassiveBase(player, ModAttributes.MANA_REGENERATION_PERCENT);
                        double baseManaRegenFlat = baseMaxManaStat * baseManaRegenPercent;

                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Mana Recovery / Sec",
                                        manaRegenFlat,
                                        baseManaRegenFlat, "", mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- ARMOUR ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Armour");
                        double displayArmor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
                        double displayEvasion = player.getAttributeValue(ModAttributes.EVASION_RATING);
                        if (player.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0) {
                                displayArmor += displayEvasion;
                                displayEvasion = 0;
                        }
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Armour",
                                        displayArmor,
                                        cranks$getPassiveBase(player, EntityAttributes.GENERIC_ARMOR), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        
                        double physRed = calculateArmorReduction(displayArmor);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Physical Reduction",
                                        physRed * 100, Double.NaN, "%", mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- EVASION ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Evasion");
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Evasion Rating",
                                        displayEvasion,
                                        cranks$getPassiveBase(player, ModAttributes.EVASION_RATING), "", mouseX, mouseY,
                                        panelX, panelWidth);
                        double evadeChance = calculateEvasionChance(displayEvasion, displayArmor);
                        if (player.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0) evadeChance = 0;
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Evade Chance",
                                        evadeChance * 100, Double.NaN, "%", mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- RESISTANCES ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Resistances");
                        curY = drawResistanceLine(context, tr, panelX, curY, "Fire Res", player.getAttributeValue(ModAttributes.FIRE_RESISTANCE), getUncappedAttributeValue(player, ModAttributes.FIRE_RESISTANCE), mouseX, mouseY);
                        curY = drawResistanceLine(context, tr, panelX, curY, "Cold Res", player.getAttributeValue(ModAttributes.COLD_RESISTANCE), getUncappedAttributeValue(player, ModAttributes.COLD_RESISTANCE), mouseX, mouseY);
                        curY = drawResistanceLine(context, tr, panelX, curY, "Lightning Res", player.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE), getUncappedAttributeValue(player, ModAttributes.LIGHTNING_RESISTANCE), mouseX, mouseY);
                        curY = drawResistanceLine(context, tr, panelX, curY, "Void Res", player.getAttributeValue(ModAttributes.VOID_RESISTANCE), getUncappedAttributeValue(player, ModAttributes.VOID_RESISTANCE), mouseX, mouseY);
                        curY += 16;

                        // --- KNOCKBACK ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Knockback");
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback",
                                        player.getAttributeValue(ModAttributes.KNOCKBACK) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.KNOCKBACK) * 100, "%", mouseX,
                                        mouseY, panelX, panelWidth);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Knockback Res",
                                        player.getAttributeValue(ModAttributes.KNOCKBACK_RESISTANCE) * 100,
                                        cranks$getPassiveBase(player, ModAttributes.KNOCKBACK_RESISTANCE) * 100, "%",
                                        mouseX, mouseY, panelX, panelWidth);
                        curY += 16;

                        // --- MISCELLANEOUS ---
                        curY = drawCategoryTitle(context, tr, panelX, curY, "Miscellaneous");
                        double passiveMovePercent = cranks$getPassivePercentBonus(player, ModAttributes.MOVEMENT_SPEED);
                        curY = drawStatWithTooltip(context, tr, panelX, curY, "Move Speed Mod",
                                        (player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) / 0.1 - 1.0) * 100,
                                        passiveMovePercent * 100,
                                        "%", mouseX, mouseY, panelX, panelWidth);


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
                } finally {
                        context.getMatrices().pop();
                }
        }

        @Unique
        private void renderStatTooltip(DrawContext context, TextRenderer tr, String statName, int x, int y) {
                String description = getStatDescription(statName);
                if (description == null)
                        return;

                List<OrderedText> lines = new ArrayList<>();
                int maxWrapWidth = 220; // Max width before wrapping

                for (String part : description.split("\n")) {
                        lines.addAll(tr.wrapLines(Text.literal(part).formatted(Formatting.GRAY), maxWrapWidth));
                }

                if (!Double.isNaN(hoveredBaseVal)) {
                        double overrideBase = hoveredBaseVal;
                        double overrideEquip = hoveredEquipVal;
                        String overrideSuffix = hoveredSuffix;

                        if (statName.equals("ES Recharge / Sec")) {
                                var player = MinecraftClient.getInstance().player;
                                if (player != null) {
                                        overrideBase = 12.5 * (1.0 + cranks$getPassiveBase(player, ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT));
                                        overrideEquip = (12.5 * (1.0 + player.getAttributeValue(ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT))) - overrideBase;
                                        overrideSuffix = "%";
                                }
                        } else if (statName.equals("Mana Recovery / Sec")) {
                                var player = MinecraftClient.getInstance().player;
                                if (player != null) {
                                        overrideBase = cranks$getPassiveBase(player, ModAttributes.MANA_REGENERATION_PERCENT) * 100.0;
                                        overrideEquip = (player.getAttributeValue(ModAttributes.MANA_REGENERATION_PERCENT) * 100.0) - overrideBase;
                                        overrideSuffix = "%";
                                }
                        }

                        // Use standard formatting for values
                        String baseStr = formatStatValue(statName, overrideBase);
                        String equipVal = formatStatValue(statName, overrideEquip);
                        String sign = (overrideEquip >= 0) ? "+" : "";

                        lines.add(Text.empty().asOrderedText()); // Spacer
                        lines.add(Text.literal("Base: ").formatted(Formatting.WHITE)
                                        .append(Text.literal(baseStr + overrideSuffix).formatted(Formatting.GOLD)).asOrderedText());
                        lines.add(Text.literal("Equipment: ").formatted(Formatting.WHITE)
                                        .append(Text.literal(sign + equipVal + overrideSuffix).formatted(Formatting.AQUA)).asOrderedText());
                }

                int maxWidth = 0;
                for (OrderedText line : lines) {
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
                        case "Strength" -> "Every 1 Strength grants +2 Max Health";
                        case "Dexterity" -> "Every 1 Dexterity grants +2 Evasion Rating";
                        case "Intelligence" -> "Every 1 Intelligence grants +2 Maximum Mana";
                        case "Max Mana" -> "Maximum mana available for casting abilities";
                        case "Mana Recovery / Sec" -> "Amount of mana recovered per second";
                        case "Max Health" -> "Maximum health points";
                        case "Health Regen" -> "Amount of health recovered per second";
                        case "Max Energy Shield" -> "Absorbs damage before health";
                        case "ES Recharge / Sec" -> "Amount of Energy Shield recovered per second";
                        case "ES Recharge Delay" -> "Time before Energy Shield begins to recharge";
                        case "Armour" -> "Reduces incoming physical damage";
                        case "Physical Reduction" -> "Estimated physical damage reduction against average hits";
                        case "Evasion Rating" -> "Increases chance to dodge attacks";
                        case "Evade Chance" -> "Estimated chance to evade attacks from enemies";
                        case "Fire Res" -> "Reduces fire damage taken";
                        case "Cold Res" -> "Reduces cold damage taken";
                        case "Lightning Res" -> "Reduces lightning damage taken";
                        case "Void Res" -> "Reduces void damage taken";
                        case "Knockback" -> "Chance to knock back enemies on hit";
                        case "Knockback Res" -> "Reduces distance knocked back by attacks";
                        case "Move Speed Mod" -> "Movement speed modifier";
                        case "Item Rarity" -> "Increases rarity of items found";
                        default -> null;
                };
        }

        @Unique
        private String calculateEvasionDescription() {
                var player = MinecraftClient.getInstance().player;
                if (player == null)
                        return "Chance to dodge attacks";
                if (player.getAttributeValue(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0)
                        return "Evasion converted to Armor by Iron Reflexes";
                double rating = player.getAttributeValue(ModAttributes.EVASION_RATING);
                double armor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
                return (int) (calculateEvasionChance(rating, armor) * 100) + "% chance to dodge";
        }

        @Unique
        private double calculateEvasionChance(double evasion, double armor) {
                return Math.min(0.95, evasion / (evasion + 400.0 + armor * 4.0));
        }

        @Unique
        private String calculateArmorDescription() {
                var player = MinecraftClient.getInstance().player;
                if (player == null)
                        return "Reduces incoming physical damage";
                double armor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
                return (int) (calculateArmorReduction(armor) * 100) + "% estimated physical damage reduction";
        }

        @Unique
        private double calculateArmorReduction(double armor) {
                // Simplified formula for tooltip visualization
                return armor / (armor + 50.0);
        }

        @Unique
        private double getUncappedAttributeValue(net.minecraft.entity.LivingEntity entity, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute) {
                var instance = entity.getAttributeInstance(attribute);
                if (instance == null) return entity.getAttributeBaseValue(attribute);
                
                double d = instance.getBaseValue();
                for (var mod : instance.getModifiers()) {
                        if (mod.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE) {
                                d += mod.value();
                        }
                }
                double e = d;
                for (var mod : instance.getModifiers()) {
                        if (mod.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
                                e += d * mod.value();
                        }
                }
                for (var mod : instance.getModifiers()) {
                        if (mod.operation() == net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                                e *= 1.0 + mod.value();
                        }
                }
                return e;
        }

        @Unique
        private int drawResistanceLine(DrawContext context, TextRenderer tr, int panelX, int y, String name, double cappedValue, double uncappedValue, int mouseX, int mouseY) {
                double capped = cappedValue * 100;
                double uncapped = uncappedValue * 100;
                String text = String.format(java.util.Locale.US, "%s: %.0f%% (%.0f%%)", name, capped, uncapped);
                
                int statX = panelX + 12;
                if (mouseX >= statX && mouseX <= statX + tr.getWidth(text) && mouseY >= y && mouseY <= y + 9) {
                        hoveredStat = name;
                        tooltipX = mouseX;
                        tooltipY = mouseY;
                        hoveredBaseVal = Double.NaN;
                        hoveredEquipVal = 0;
                        hoveredSuffix = "";
                }
                
                context.drawText(tr, Text.literal(text), statX, y, 0xFFE0E0E0, false);
                return y + 10;
        }

        @Unique
        private int drawCategoryTitle(DrawContext context, TextRenderer tr, int panelX, int y, String title) {
                context.getMatrices().push();
                float scale = 1.1f;
                context.getMatrices().scale(scale, scale, 1.0f);
                context.drawText(tr, Text.literal(title), (int)((panelX + 12) / scale), (int)(y / scale), 0xFFE0E0E0, false);
                context.getMatrices().pop();
                return y + (int)(11 * scale);
        }


        @Unique
        private String calculateCritDefenseDescription() {
                var player = MinecraftClient.getInstance().player;
                if (player == null)
                        return "Reduces extra damage from critical hits";
                double mitigationMultiplier = player.getAttributeValue(ModAttributes.ARMOR_CRIT_MITIGATION);
                double armor = player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
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
                double base = player.getAttributeValue(damageAttribute);

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
                context.drawText(tr, Text.literal(name + ": " + String.format("%.1f", val) + suffix), x + 12, y,
                                0xFFE0E0E0,
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
                int statX = x + 12;
                
                if (mouseX >= statX && mouseX <= statX + tr.getWidth(text) && mouseY >= y && mouseY <= y + 9) {
                        hoveredStat = name;
                        tooltipX = mouseX;
                        tooltipY = mouseY;
                        hoveredBaseVal = base;
                        hoveredEquipVal = total - base;
                        hoveredSuffix = suffix;
                }
                
                context.drawText(tr, Text.literal(text), statX, y, 0xFFE0E0E0, false);
                
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
                int sbWidth = 12; // Vanilla scroller width
                int sbX = panelX + panelWidth - 14; // Fit nicely in the panel
                int sbY = panelY + 18;
                int sbHeight = panelHeight - 36;

                int scrollOffset = StatsPanelScrollHandler.getScrollOffset();
                int maxScrollOffset = StatsPanelScrollHandler.getMaxScrollOffset();

                // The track is removed so the thumb floats directly on the panel, just like vanilla creative inventory

                float scrollPercent = maxScrollOffset > 0 ? (float) scrollOffset / maxScrollOffset : 0;
                int thumbHeight = 15; // Vanilla scroller height
                int thumbY = sbY + (int) (scrollPercent * (sbHeight - thumbHeight));

                // Draw the vanilla creative inventory scroller
                context.drawGuiTexture(SCROLLER_TEXTURE, sbX, thumbY, sbWidth, thumbHeight);

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
                                                        || path.startsWith("intrinsic_")
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

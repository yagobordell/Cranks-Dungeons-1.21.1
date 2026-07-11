/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1074
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_339
 *  net.minecraft.class_342
 *  net.minecraft.class_3532
 *  net.minecraft.class_357
 *  net.minecraft.class_364
 *  net.minecraft.class_407
 *  net.minecraft.class_410
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_5481
 *  net.minecraft.class_7923
 */
package punchy.client.config;

import java.awt.Desktop;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_1074;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_357;
import net.minecraft.class_364;
import net.minecraft.class_407;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5481;
import net.minecraft.class_7923;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.config.ConfigProfileManager;
import punchy.client.debug.PunchyDebug;
import punchy.client.tuning.MixThePacksScreen;
import punchy.client.tuning.TuningWorkbenchScreen;
import punchy.compat.TaczBlacklistCompat;
import punchy.config.PunchyConfig;
import punchy.platform.Services;

public final class PunchyConfigScreen
extends class_437 {
    private static final int CONTENT_WIDTH = 320;
    private static final int SIDEBAR_WIDTH = 120;
    private static final int SIDEBAR_GAP = 16;
    private static final int MAIN_PANEL_GAP = 12;
    private static final int PROFILE_PANEL_WIDTH = 220;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int RESET_GAP = 10;
    private static final int RESET_WIDTH = 44;
    private static final int BLACKLIST_MODAL_ROW_HEIGHT = 22;
    private static final int BLACKLIST_MODAL_FILTER_ROWS = 2;
    private static final String BLACKLIST_BULK_ENABLE_KEY = "__bulk_enable__";
    private static final String BLACKLIST_BULK_DUAL_KEY = "__bulk_dual__";
    private final class_437 parent;
    private final PunchyConfig.Data data;
    private Tab tab = Tab.SUPPORT;
    private class_342 blacklistInput;
    private String blacklistDraft = "";
    private int dividerX;
    private int dividerTop;
    private int dividerBottom;
    private List<class_5481> blacklistHelpLines;
    private int blacklistHelpX;
    private int blacklistHelpY;
    private int blacklistHelpWidth;
    private int blacklistHelpHeight;
    private final List<class_4185> blacklistEntryButtons = new ArrayList<class_4185>();
    private final List<class_4185> blacklistDualButtons = new ArrayList<class_4185>();
    private final List<class_4185> blacklistRemoveButtons = new ArrayList<class_4185>();
    private int blacklistScrollOffset;
    private int blacklistListStartY;
    private int blacklistViewportHeight;
    private int blacklistEntryHeight;
    private int blacklistScrollbarReserve;
    private final List<class_339> animationWidgets = new ArrayList<class_339>();
    private final List<Integer> animationWidgetBaseY = new ArrayList<Integer>();
    private int animationScrollOffset;
    private int animationListStartY;
    private int animationViewportHeight;
    private int animationEntryHeight;
    private int animationRowCount;
    private boolean draggingBlacklistScrollbar;
    private boolean draggingAnimationScrollbar;
    private boolean draggingBlacklistModalScrollbar;
    private boolean draggingBlacklistNamespaceScrollbar;
    private boolean blacklistModalOpen;
    private class_342 blacklistSearchInput;
    private String blacklistSearchDraft = "";
    private int blacklistModalLeft;
    private int blacklistModalTop;
    private int blacklistModalRight;
    private int blacklistModalBottom;
    private int blacklistListLeft;
    private int blacklistListRight;
    private int blacklistListTop;
    private int blacklistListBottom;
    private int blacklistCloseButtonLeft;
    private int blacklistCloseButtonTop;
    private int blacklistCloseButtonRight;
    private int blacklistCloseButtonBottom;
    private int blacklistResetButtonLeft;
    private int blacklistResetButtonTop;
    private int blacklistResetButtonRight;
    private int blacklistResetButtonBottom;
    private int blacklistDoneButtonLeft;
    private int blacklistDoneButtonTop;
    private int blacklistDoneButtonRight;
    private int blacklistDoneButtonBottom;
    private int blacklistModalScrollOffset;
    private int blacklistModalMaxScroll;
    private int blacklistNamespaceScrollOffset;
    private int blacklistNamespaceMaxScroll;
    private int blacklistNamespaceAreaLeft;
    private int blacklistNamespaceAreaRight;
    private int blacklistNamespaceAreaTop;
    private int blacklistNamespaceAreaBottom;
    private final List<BlacklistCatalogEntry> blacklistCatalogEntries = new ArrayList<BlacklistCatalogEntry>();
    private final List<BlacklistCatalogEntry> blacklistFilteredEntries = new ArrayList<BlacklistCatalogEntry>();
    private final List<BlacklistModFilter> blacklistModFilters = new ArrayList<BlacklistModFilter>();
    private String blacklistSelectedNamespace = "";
    private final List<BlacklistHitbox> blacklistNamespaceHitboxes = new ArrayList<BlacklistHitbox>();
    private final List<BlacklistHitbox> blacklistEnableHitboxes = new ArrayList<BlacklistHitbox>();
    private final List<BlacklistHitbox> blacklistDualHitboxes = new ArrayList<BlacklistHitbox>();
    private final List<class_4185> profileRowButtons = new ArrayList<class_4185>();
    private List<String> profiles = List.of();
    private class_342 profileNameInput;
    private class_4185 profileSaveButton;
    private class_4185 profileLoadButton;
    private class_4185 profileDeleteButton;
    private class_4185 profileOpenFolderButton;
    private class_4185 supportDonateButton;
    private int selectedProfile = -1;
    private String activeProfileName = null;
    private int profileListScroll = 0;
    private int profilePanelX = 0;
    private int profilePanelY = 0;
    private int profileListX = 0;
    private int profileListY = 0;
    private int profileListW = 0;
    private int profileListH = 0;
    private int mainPanelLeft = 0;
    private int mainPanelTop = 0;
    private int mainPanelRight = 0;
    private int mainPanelBottom = 0;
    private int leftPanelLeft = 0;
    private int leftPanelTop = 0;
    private int leftPanelRight = 0;
    private int leftPanelBottom = 0;
    private int centerPanelLeft = 0;
    private int centerPanelTop = 0;
    private int centerPanelRight = 0;
    private int centerPanelBottom = 0;
    private int rightPanelLeft = 0;
    private int rightPanelTop = 0;
    private int rightPanelRight = 0;
    private int rightPanelBottom = 0;
    private int contentPanelWidth = 320;
    private int profilePanelWidth = 220;
    private Integer previousGuiScaleValue;
    private boolean forcedGuiScale;

    public PunchyConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"punchy.config.title"));
        this.parent = parent;
        this.data = PunchyConfig.snapshot();
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.blacklistModalOpen && keyCode == 256) {
            this.closeBlacklistModal();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    protected void method_25426() {
        this.applySmallScreenGuiScaleOverride();
        this.syncGuiScaleOverrideDimensions();
        this.rebuild();
    }

    private void rebuild() {
        int buttonHeight;
        this.method_37067();
        this.blacklistInput = null;
        this.blacklistHelpLines = null;
        this.blacklistHelpHeight = 0;
        this.blacklistEntryButtons.clear();
        this.blacklistDualButtons.clear();
        this.blacklistRemoveButtons.clear();
        this.blacklistScrollOffset = 0;
        this.blacklistListStartY = 0;
        this.blacklistViewportHeight = 0;
        this.blacklistEntryHeight = 26;
        this.blacklistScrollbarReserve = 0;
        this.animationWidgets.clear();
        this.animationWidgetBaseY.clear();
        this.animationScrollOffset = 0;
        this.animationListStartY = 0;
        this.animationViewportHeight = 0;
        this.animationEntryHeight = 26;
        this.animationRowCount = 0;
        this.blacklistSearchInput = null;
        this.blacklistNamespaceHitboxes.clear();
        this.blacklistEnableHitboxes.clear();
        this.blacklistDualHitboxes.clear();
        this.blacklistModalMaxScroll = 0;
        this.blacklistNamespaceScrollOffset = 0;
        this.blacklistNamespaceMaxScroll = 0;
        this.profileRowButtons.clear();
        this.profileNameInput = null;
        this.profileSaveButton = null;
        this.profileLoadButton = null;
        this.profileDeleteButton = null;
        this.profileOpenFolderButton = null;
        this.supportDonateButton = null;
        this.profileListScroll = 0;
        if (this.tab == Tab.BLACKLIST) {
            this.tab = Tab.SUPPORT;
        }
        int tabHeight = 20;
        int tabSpacing = 6;
        int extraSidebarRows = 2;
        Tab[] tabs = Tab.values();
        int tabsHeight = (tabs.length + extraSidebarRows) * (tabHeight + tabSpacing) - tabSpacing;
        int baseLayoutWidth = Math.min(800, Math.max(680, this.field_22789 - 48));
        int layoutWidth = Math.max(520, (int)Math.round((double)baseLayoutWidth * 0.65));
        layoutWidth = Math.min(layoutWidth, Math.max(520, this.field_22789 - 16));
        this.profilePanelWidth = Math.max(170, (int)Math.round(143.0));
        this.contentPanelWidth = Math.max(170, layoutWidth - 120 - 16 - 12 - this.profilePanelWidth);
        int layoutX = Math.max(8, (this.field_22789 - layoutWidth) / 2);
        int availableHeight = Math.max(320, this.field_22790 - 24);
        int baseMenuHeight = Math.min(320, availableHeight);
        int menuHeight = Math.max(190, (int)Math.round((double)baseMenuHeight * 0.6));
        menuHeight += extraSidebarRows * (tabHeight + tabSpacing);
        menuHeight = Math.min(menuHeight, availableHeight);
        int menuTop = Math.max(8, (this.field_22790 - menuHeight) / 2);
        int menuBottom = menuTop + menuHeight;
        int tabX = layoutX;
        int tabY = menuTop + 8;
        for (int i = 0; i < tabs.length; ++i) {
            class_4185 tabButton;
            int visualRow;
            Tab current = tabs[i];
            int n = visualRow = i >= 2 ? i + extraSidebarRows : i;
            if (current == Tab.BLACKLIST) {
                tabButton = class_4185.method_46430((class_2561)class_2561.method_43471((String)current.labelKey), button -> this.openBlacklistModal()).method_46433(tabX, tabY + visualRow * (tabHeight + tabSpacing)).method_46437(120, tabHeight).method_46431();
                tabButton.field_22763 = !this.blacklistModalOpen;
            } else {
                tabButton = class_4185.method_46430((class_2561)class_2561.method_43471((String)current.labelKey), button -> {
                    this.tab = current;
                    this.rebuild();
                }).method_46433(tabX, tabY + visualRow * (tabHeight + tabSpacing)).method_46437(120, tabHeight).method_46431();
                tabButton.field_22763 = this.tab != current;
            }
            this.method_37063((class_364)tabButton);
        }
        int handEditorY = tabY + 2 * (tabHeight + tabSpacing);
        class_4185 handEditorShortcut = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Hand Editor"), button -> {
            if (this.field_22787 != null) {
                this.field_22787.method_1507((class_437)new TuningWorkbenchScreen(this));
            }
        }).method_46433(tabX, handEditorY).method_46437(120, tabHeight).method_46431());
        handEditorShortcut.field_22763 = !this.blacklistModalOpen;
        int mixPacksY = tabY + 3 * (tabHeight + tabSpacing);
        class_4185 mixPacksShortcut = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Mix Packs"), button -> {
            if (this.field_22787 != null) {
                this.field_22787.method_1507((class_437)new MixThePacksScreen(this));
            }
        }).method_46433(tabX, mixPacksY).method_46437(120, tabHeight).method_46431());
        mixPacksShortcut.field_22763 = !this.blacklistModalOpen;
        int startY = tabY;
        int startX = layoutX + 120 + 16;
        int profileX = startX + this.contentPanelWidth + 12;
        int row = 0;
        int optionWidth = this.contentPanelWidth - 44 - 10;
        this.dividerX = startX - 8;
        if ((this.dividerX & 1) == 1) {
            --this.dividerX;
        }
        this.dividerTop = tabY - 4;
        this.dividerBottom = tabY + tabsHeight + 4;
        if (this.tab == Tab.SUPPORT) {
            row = this.addLinkButton(row, startX, startY, this.contentPanelWidth, "punchy.config.support.donate", "https://ko-fi.com/modinpunchy");
            row = this.addLinkButton(row, startX, startY, this.contentPanelWidth, "punchy.config.support.discord", "https://discord.gg/CXttaCyGCN");
            row = this.addLinkButton(row, startX, startY, this.contentPanelWidth, "punchy.config.support.wiki", "https://github.com/punchy-mod/punchy-wiki/wiki");
            row = this.addLinkButton(row, startX, startY, this.contentPanelWidth, "punchy.config.support.bug_report", "https://discord.gg/CXttaCyGCN");
        } else if (this.tab == Tab.BLACKLIST) {
            int addWidth = 50;
            int inputWidth = this.contentPanelWidth - addWidth - 10;
            String helpText = class_2561.method_43471((String)"punchy.config.blacklist.examples").getString();
            this.blacklistHelpX = startX;
            this.blacklistHelpY = startY + this.rowOffset(row);
            this.blacklistHelpWidth = this.contentPanelWidth;
            float helpScale = 1.0f;
            Objects.requireNonNull(this.field_22793);
            int helpLineHeight = (int)Math.ceil(9.0f * helpScale);
            int maxChars = Math.max(10, (int)((float)this.blacklistHelpWidth / ((float)this.field_22793.method_1727("abcdefghijklmnopqrstuvwxyz") / 26.0f)));
            this.blacklistHelpLines = PunchyConfigScreen.splitHelpText(helpText, maxChars);
            this.blacklistHelpHeight = this.blacklistHelpLines.size() * helpLineHeight + 4;
            int baseY = startY + this.blacklistHelpHeight;
            this.blacklistInput = new class_342(this.field_22793, startX, baseY + this.rowOffset(row), inputWidth, 20, (class_2561)class_2561.method_43471((String)"punchy.config.blacklist.input"));
            this.blacklistInput.method_1852(this.blacklistDraft);
            this.method_37063((class_364)this.blacklistInput);
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.blacklist.add"), button -> this.handleAddBlacklist()).method_46433(startX + inputWidth + 10, baseY + this.rowOffset(row)).method_46437(addWidth, 20).method_46431());
            ++row;
            if (this.data.itemBlacklist != null) {
                int visibleRows = this.data.itemBlacklist.size() >= 5 ? 4 : 5;
                this.blacklistScrollbarReserve = this.data.itemBlacklist.size() >= 5 ? 7 : 0;
                this.blacklistListStartY = baseY + this.rowOffset(row);
                this.blacklistViewportHeight = visibleRows * this.blacklistEntryHeight - 6;
                for (String entry : this.data.itemBlacklist) {
                    if (entry == null || entry.isBlank()) continue;
                    String label = entry;
                    int dualWidth = 66;
                    int removeWidth = 20;
                    int labelWidth = this.contentPanelWidth - dualWidth - removeWidth - 20 - this.blacklistScrollbarReserve;
                    class_4185 labelButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)label), b -> {}).method_46433(startX, this.blacklistListStartY + this.blacklistEntryButtons.size() * this.blacklistEntryHeight).method_46437(labelWidth, 20).method_46431();
                    labelButton.field_22763 = false;
                    this.method_37063((class_364)labelButton);
                    String entryId = entry;
                    class_4185 dualButton = class_4185.method_46430((class_2561)this.blacklistDualLabel(this.isBlacklistDualHanded(entryId)), button -> {
                        this.setBlacklistDualHanded(entryId, !this.isBlacklistDualHanded(entryId));
                        this.rebuild();
                    }).method_46433(startX + labelWidth + 10, this.blacklistListStartY + this.blacklistDualButtons.size() * this.blacklistEntryHeight).method_46437(dualWidth, 20).method_46431();
                    this.method_37063((class_364)dualButton);
                    String removeId = entry;
                    class_4185 removeButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"X"), button -> {
                        this.data.itemBlacklist.remove(removeId);
                        if (this.data.blacklistApplyDualHanded != null) {
                            this.data.blacklistApplyDualHanded.remove(removeId);
                        }
                        this.rebuild();
                    }).method_46433(startX + labelWidth + 10 + dualWidth + 10, this.blacklistListStartY + this.blacklistRemoveButtons.size() * this.blacklistEntryHeight).method_46437(removeWidth, 20).method_46431();
                    this.method_37063((class_364)removeButton);
                    this.blacklistEntryButtons.add(labelButton);
                    this.blacklistDualButtons.add(dualButton);
                    this.blacklistRemoveButtons.add(removeButton);
                    ++row;
                }
                this.updateBlacklistScroll();
            }
        } else if (this.tab == Tab.RENDERING) {
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.render_armor_arms_fp", () -> this.data.renderArmorArmsFP, value -> {
                this.data.renderArmorArmsFP = value;
            }, false);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_enhanced_fire_arm_effects", () -> !this.data.disableEnhancedFireArmEffects, value -> {
                this.data.disableEnhancedFireArmEffects = value == false;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_blur_glow_at_dark", () -> this.data.enableBlurGlowAtDark, value -> {
                this.data.enableBlurGlowAtDark = value;
            }, true);
        } else if (this.tab == Tab.ANIMATION) {
            buttonHeight = 24;
            int doneY = menuBottom - (buttonHeight + 8);
            this.animationListStartY = startY;
            int maxVisibleRows = 7;
            this.animationViewportHeight = Math.min(Math.max(0, doneY - 12 - this.animationListStartY), maxVisibleRows * this.animationEntryHeight - 6);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_particles", () -> this.data.enableParticles, value -> {
                this.data.enableParticles = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_model_parts_glow", () -> this.data.enableModelPartsGlow, value -> {
                this.data.enableModelPartsGlow = value;
            }, true);
            AnimationSpeedSlider slider = new AnimationSpeedSlider(startX, startY + this.rowOffset(row++), optionWidth, 20, this.data);
            this.registerAnimationWidget(slider, slider.method_46427());
            this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
                this.data.animationSpeed = 5.5f;
                slider.setSpeed(this.data.animationSpeed);
                PunchyAnimationManager.setAnimationSpeed(this.data.animationSpeed);
            }).method_46433(startX + optionWidth + 10, startY + this.rowOffset(row - 1)).method_46437(44, 20).method_46431(), startY + this.rowOffset(row - 1));
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_boat_fp_animations", () -> !this.data.disableBoatFirstPersonAnimations, value -> {
                this.data.disableBoatFirstPersonAnimations = value == false;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_custom_walk", () -> this.data.enableCustomWalk, value -> {
                this.data.enableCustomWalk = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_sprint_arm_swing", () -> this.data.enableSprintArmSwing, value -> {
                this.data.enableSprintArmSwing = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_fall_arm_animation", () -> this.data.enableFallArmAnimation, value -> {
                this.data.enableFallArmAnimation = value;
            }, true);
            SprintArmSwingIntensitySlider sprintSlider = new SprintArmSwingIntensitySlider(startX, startY + this.rowOffset(row++), optionWidth, 20, this.data);
            this.registerAnimationWidget(sprintSlider, sprintSlider.method_46427());
            this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
                this.data.sprintArmSwingIntensity = 1.0f;
                sprintSlider.setIntensity(this.data.sprintArmSwingIntensity);
            }).method_46433(startX + optionWidth + 10, startY + this.rowOffset(row - 1)).method_46437(44, 20).method_46431(), startY + this.rowOffset(row - 1));
            SprintArmSwingSpeedSlider sprintSpeedSlider = new SprintArmSwingSpeedSlider(startX, startY + this.rowOffset(row++), optionWidth, 20, this.data);
            this.registerAnimationWidget(sprintSpeedSlider, sprintSpeedSlider.method_46427());
            this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
                this.data.sprintArmSwingSpeed = 0.0f;
                sprintSpeedSlider.setSpeed(this.data.sprintArmSwingSpeed);
            }).method_46433(startX + optionWidth + 10, startY + this.rowOffset(row - 1)).method_46437(44, 20).method_46431(), startY + this.rowOffset(row - 1));
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_crawl_animation", () -> this.data.enableCrawlAnimation, value -> {
                this.data.enableCrawlAnimation = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_climb_animation", () -> this.data.enableClimbAnimation, value -> {
                this.data.enableClimbAnimation = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_swim_animation", () -> this.data.enableSwimAnimation, value -> {
                this.data.enableSwimAnimation = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_elytra_flight_animation", () -> this.data.enableElytraFlightAnimation, value -> {
                this.data.enableElytraFlightAnimation = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.freeze_shake", () -> this.data.enableFreezeShake, value -> {
                this.data.enableFreezeShake = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.freeze_arm_overlay", () -> this.data.enableFreezeArmOverlay, value -> {
                this.data.enableFreezeArmOverlay = value;
            }, true);
            row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_mud", () -> this.data.enableMud, value -> {
                this.data.enableMud = value;
            }, true);
            this.animationRowCount = row = this.addToggleWithResetScrollable(row, startX, startY, optionWidth, "punchy.config.option.enable_burn", () -> this.data.enableBurn, value -> {
                this.data.enableBurn = value;
            }, true);
            this.updateAnimationScroll();
        } else if (this.tab == Tab.PHYSICS) {
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_arm_physics", () -> !this.data.disableArmPhysics, value -> {
                this.data.disableArmPhysics = value == false;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_native_item_physics", () -> !this.data.disableNativeItemPhysics, value -> {
                this.data.disableNativeItemPhysics = value == false;
            }, true);
        } else if (this.tab == Tab.MODELS) {
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_boat_minecart_models", () -> !this.data.disableBoatMinecartRaftModels, value -> {
                this.data.disableBoatMinecartRaftModels = value == false;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_piston_models", () -> !this.data.disablePistonModels, value -> {
                this.data.disablePistonModels = value == false;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_chest_models", () -> !this.data.disableChestModels, value -> {
                this.data.disableChestModels = value == false;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_enchanting_table_models", () -> !this.data.disableEnchantingTableModels, value -> {
                this.data.disableEnchantingTableModels = value == false;
            }, true);
        } else {
            row = this.addInteractiveStuffCompatToggle(row, startX, startY, optionWidth);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.firstperson_hide_enabled", () -> this.data.firstPersonModelHideEnabled, value -> {
                this.data.firstPersonModelHideEnabled = value;
            }, true);
            int pitchRowY = startY + this.rowOffset(row);
            FirstPersonHidePitchSlider pitchSlider = new FirstPersonHidePitchSlider(startX, pitchRowY, optionWidth, 20, this.data);
            this.registerAnimationWidget(pitchSlider, pitchRowY);
            this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
                this.data.firstPersonModelHidePitch = 60.0f;
                pitchSlider.setPitch(this.data.firstPersonModelHidePitch);
            }).method_46433(startX + optionWidth + 10, pitchRowY).method_46437(44, 20).method_46431(), pitchRowY);
            ++row;
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_mod", () -> this.data.enableMod, value -> {
                this.data.enableMod = value;
            }, true);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.enable_tuning", () -> this.data.enableTuning, value -> {
                this.data.enableTuning = value;
            }, false);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.bettercombat_compat", () -> this.data.bettercombatCompat, value -> {
                this.data.bettercombatCompat = value;
            }, false);
            row = this.addToggleWithReset(row, startX, startY, optionWidth, "punchy.config.option.swordblocking_compat", () -> this.data.swordBlockingCompat, value -> {
                this.data.swordBlockingCompat = value;
            }, false);
        }
        buttonHeight = 20;
        int doneY = menuBottom - (buttonHeight + 8);
        this.configureCenterScroll(row, startY, doneY);
        this.initProfilesPanel(profileX, startY, doneY);
        this.refreshProfiles(true);
        this.updateProfileRowButtons();
        this.updateMainPanelBounds(layoutX, menuTop, layoutWidth, menuBottom);
        if (this.blacklistModalOpen) {
            this.suppressBaseWidgetsForBlacklistModal();
            this.initBlacklistModalWidgets();
        }
    }

    private void applySmallScreenGuiScaleOverride() {
        if (this.forcedGuiScale || this.field_22787 == null || this.field_22787.field_1690 == null) {
            return;
        }
        if (!this.shouldForceSmallScreenGuiScale()) {
            return;
        }
        this.previousGuiScaleValue = (Integer)this.field_22787.field_1690.method_42474().method_41753();
        this.field_22787.method_22683().method_15997(1.0);
        this.forcedGuiScale = true;
    }

    private void syncGuiScaleOverrideDimensions() {
        if (!this.forcedGuiScale || this.field_22787 == null || this.field_22787.method_22683() == null) {
            return;
        }
        this.field_22789 = this.field_22787.method_22683().method_4486();
        this.field_22790 = this.field_22787.method_22683().method_4502();
    }

    private void restoreGuiScaleOverride() {
        int restoredScale;
        if (!this.forcedGuiScale || this.field_22787 == null || this.field_22787.field_1690 == null || this.previousGuiScaleValue == null) {
            this.forcedGuiScale = false;
            this.previousGuiScaleValue = null;
            return;
        }
        this.field_22787.field_1690.method_42474().method_41748((Object)this.previousGuiScaleValue);
        int n = restoredScale = this.previousGuiScaleValue > 0 ? this.previousGuiScaleValue.intValue() : this.field_22787.method_22683().method_4476(this.previousGuiScaleValue.intValue(), false);
        if (restoredScale < 1) {
            restoredScale = 1;
        }
        this.field_22787.method_22683().method_15997((double)restoredScale);
        this.forcedGuiScale = false;
        this.previousGuiScaleValue = null;
    }

    private boolean shouldForceSmallScreenGuiScale() {
        if (this.field_22787 == null || this.field_22787.method_22683() == null) {
            return false;
        }
        int screenWidth = this.field_22787.method_22683().method_4480();
        int screenHeight = this.field_22787.method_22683().method_4507();
        return screenWidth < 1280 || screenHeight < 720;
    }

    private int rowOffset(int row) {
        return row * 26;
    }

    private <T extends class_339> T registerAnimationWidget(T widget, int baseY) {
        this.animationWidgets.add(widget);
        this.animationWidgetBaseY.add(baseY);
        widget.method_46419(baseY - this.animationScrollOffset);
        this.method_37063((class_364)widget);
        return widget;
    }

    private void configureCenterScroll(int rowCount, int startY, int doneY) {
        this.animationListStartY = startY;
        int alignedBottom = this.centerPanelBottom - 8;
        this.animationViewportHeight = Math.max(0, alignedBottom - this.animationListStartY);
        this.animationRowCount = Math.max(0, rowCount);
        int totalHeight = this.animationRowCount * this.animationEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.animationViewportHeight);
        this.animationScrollOffset = PunchyConfigScreen.clamp(this.animationScrollOffset, 0, maxScroll);
        this.updateAnimationScroll();
    }

    private void initProfilesPanel(int x, int startY, int doneY) {
        this.profilePanelX = x;
        this.profilePanelY = startY;
        this.profileListX = x;
        this.profileListW = this.profilePanelWidth;
        int listRowHeight = 14;
        int maxRows = 6;
        int minRows = 2;
        this.profileListY = startY;
        int buttonGap = 2;
        int listToControlsGap = 6;
        int controlsToBottomActionsGap = 6;
        int controlsBlockHeight = 80 + buttonGap * 3;
        int listBottomMax = doneY - controlsToBottomActionsGap - 20 - listToControlsGap - controlsBlockHeight;
        int availableForList = Math.max(minRows * listRowHeight, listBottomMax - this.profileListY);
        int listRows = class_3532.method_15340((int)(availableForList / listRowHeight), (int)minRows, (int)maxRows);
        this.profileListH = listRows * listRowHeight;
        int nameY = this.profileListY + this.profileListH + listToControlsGap;
        int actionY = nameY + 20 + buttonGap;
        int deleteY = actionY + 20 + buttonGap;
        int openFolderY = deleteY + 20 + buttonGap;
        for (int i = 0; i < listRows; ++i) {
            int row = i;
            class_4185 rowButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43473(), b -> this.selectProfileAtVisibleRow(row)).method_46433(this.profileListX, this.profileListY + i * listRowHeight).method_46437(this.profileListW, listRowHeight - 1).method_46431());
            this.profileRowButtons.add(rowButton);
        }
        this.profileNameInput = (class_342)this.method_37063((class_364)new class_342(this.field_22793, x, nameY, this.profilePanelWidth, 20, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.profile_name")));
        this.profileNameInput.method_1880(64);
        int halfW = (this.profilePanelWidth - 6) / 2;
        this.profileSaveButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.save_profile"), b -> {
            boolean ok;
            String raw = this.profileNameInput.method_1882();
            String safe = ConfigProfileManager.sanitizeProfileName(raw);
            if (safe.isBlank()) {
                safe = "config_profile";
                this.profileNameInput.method_1852(safe);
            }
            if (ok = ConfigProfileManager.saveCurrentAs(safe, this.data)) {
                ConfigProfileManager.setActiveProfileName(safe);
                this.activeProfileName = safe;
                this.refreshProfiles(false);
                this.selectProfileByName(safe);
                this.updateProfileRowButtons();
            }
        }).method_46433(x, actionY).method_46437(halfW, 20).method_46431());
        this.profileLoadButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.load_profile"), b -> {
            PunchyConfig.Data loaded;
            String name = this.selectedProfileName();
            PunchyConfig.Data data = loaded = name == null ? null : ConfigProfileManager.load(name);
            if (loaded != null) {
                ConfigProfileManager.setActiveProfileName(name);
                this.activeProfileName = name;
                this.applyProfileDataIntoCurrent(loaded);
                this.rebuild();
            }
        }).method_46433(x + halfW + 6, actionY).method_46437(this.profilePanelWidth - halfW - 6, 20).method_46431());
        this.profileDeleteButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.delete_profile"), b -> this.openDeleteProfileConfirm()).method_46433(x, deleteY).method_46437(this.profilePanelWidth, 20).method_46431());
        int openFolderWidth = this.profilePanelWidth;
        int openFolderHeight = 20;
        int openFolderX = x;
        this.profileOpenFolderButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.open_profiles_folder"), b -> this.openConfigProfilesFolder()).method_46433(openFolderX, openFolderY).method_46437(openFolderWidth, openFolderHeight).method_46431());
        int bottomButtonWidth = (this.profilePanelWidth - 6) / 2;
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset"), button -> {
            this.resetAll();
            this.rebuild();
        }).method_46433(x, doneY).method_46437(bottomButtonWidth, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.done"), button -> this.saveAndClose()).method_46433(x + bottomButtonWidth + 6, doneY).method_46437(this.profilePanelWidth - bottomButtonWidth - 6, 20).method_46431());
    }

    private void updateMainPanelBounds(int layoutX, int menuTop, int layoutWidth, int menuBottom) {
        this.mainPanelLeft = Math.max(4, layoutX - 10);
        this.mainPanelRight = Math.min(this.field_22789 - 4, layoutX + layoutWidth + 10);
        this.mainPanelTop = Math.max(4, menuTop - 8);
        this.mainPanelBottom = Math.min(this.field_22790 - 4, menuBottom + 8);
        this.leftPanelLeft = Math.max(4, layoutX - 4);
        this.leftPanelTop = Math.max(4, menuTop);
        this.leftPanelRight = Math.min(this.field_22789 - 4, layoutX + 120 + 4);
        this.leftPanelBottom = Math.min(this.field_22790 - 4, menuBottom);
        int contentLeft = layoutX + 120 + 16;
        this.centerPanelLeft = Math.max(4, contentLeft - 4);
        this.centerPanelTop = Math.max(4, menuTop);
        this.centerPanelRight = Math.min(this.field_22789 - 4, contentLeft + this.contentPanelWidth + 4);
        this.centerPanelBottom = Math.min(this.field_22790 - 4, menuBottom);
        this.rightPanelLeft = Math.max(4, this.profilePanelX - 4);
        this.rightPanelTop = Math.max(4, menuTop);
        this.rightPanelRight = Math.min(this.field_22789 - 4, this.profilePanelX + this.profilePanelWidth + 4);
        this.rightPanelBottom = Math.min(this.field_22790 - 4, menuBottom);
    }

    private void refreshProfiles(boolean clampSelection) {
        this.profiles = ConfigProfileManager.listProfiles();
        this.activeProfileName = ConfigProfileManager.getActiveProfileName();
        if (this.activeProfileName != null && !this.profiles.contains(this.activeProfileName)) {
            this.activeProfileName = null;
            ConfigProfileManager.setActiveProfileName(null);
        }
        int maxScroll = Math.max(0, this.profiles.size() - this.profileRowButtons.size());
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)maxScroll);
        if (this.profiles.isEmpty()) {
            this.selectedProfile = -1;
            if (this.profileNameInput != null) {
                this.profileNameInput.method_1852("");
            }
            return;
        }
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size() || clampSelection) {
            this.selectedProfile = this.activeProfileName != null ? this.profiles.indexOf(this.activeProfileName) : (clampSelection ? -1 : 0);
            this.selectedProfile = class_3532.method_15340((int)this.selectedProfile, (int)-1, (int)(this.profiles.size() - 1));
        }
        this.ensureSelectedVisibleInProfileList();
        String selected = this.selectedProfileName();
        if (this.profileNameInput != null) {
            this.profileNameInput.method_1852(selected == null ? "" : selected);
        }
    }

    private void updateProfileRowButtons() {
        for (int i = 0; i < this.profileRowButtons.size(); ++i) {
            class_4185 rowButton = this.profileRowButtons.get(i);
            int index = this.profileListScroll + i;
            if (index >= 0 && index < this.profiles.size()) {
                String name = this.profiles.get(index);
                boolean selected = name != null && name.equals(this.activeProfileName);
                rowButton.field_22764 = true;
                rowButton.field_22763 = true;
                rowButton.method_25355((class_2561)class_2561.method_43470((String)((selected ? "> " : "") + name)));
                continue;
            }
            rowButton.field_22764 = true;
            rowButton.field_22763 = false;
            rowButton.method_25355((class_2561)class_2561.method_43473());
        }
    }

    private void selectProfileAtVisibleRow(int row) {
        int index = this.profileListScroll + row;
        if (index < 0 || index >= this.profiles.size()) {
            return;
        }
        this.selectedProfile = index;
        String selectedName = this.selectedProfileName();
        if (selectedName != null && this.profileNameInput != null) {
            this.profileNameInput.method_1852(selectedName);
        }
        this.ensureSelectedVisibleInProfileList();
        this.updateProfileRowButtons();
    }

    private void ensureSelectedVisibleInProfileList() {
        if (this.selectedProfile < 0) {
            return;
        }
        int visible = this.profileRowButtons.size();
        if (this.selectedProfile < this.profileListScroll) {
            this.profileListScroll = this.selectedProfile;
        } else if (this.selectedProfile >= this.profileListScroll + visible) {
            this.profileListScroll = this.selectedProfile - visible + 1;
        }
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)Math.max(0, this.profiles.size() - visible));
    }

    private String selectedProfileName() {
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size()) {
            return null;
        }
        return this.profiles.get(this.selectedProfile);
    }

    private void selectProfileByName(String profileName) {
        if (profileName == null || profileName.isBlank()) {
            return;
        }
        for (int i = 0; i < this.profiles.size(); ++i) {
            if (!profileName.equals(this.profiles.get(i))) continue;
            this.selectedProfile = i;
            this.ensureSelectedVisibleInProfileList();
            if (this.profileNameInput != null) {
                this.profileNameInput.method_1852(profileName);
            }
            return;
        }
    }

    private void openDeleteProfileConfirm() {
        String name = this.selectedProfileName();
        class_310 client = this.field_22787;
        if (name == null || client == null) {
            return;
        }
        client.method_1507((class_437)new class_410(confirmed -> {
            if (confirmed && ConfigProfileManager.deleteProfile(name)) {
                this.refreshProfiles(true);
                this.updateProfileRowButtons();
            }
            client.method_1507((class_437)this);
        }, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_delete_profile.title"), (class_2561)class_2561.method_43469((String)"punchy.tuning.ui.confirm_delete_profile.message", (Object[])new Object[]{name}), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_delete_profile.confirm"), (class_2561)class_2561.method_43471((String)"gui.cancel")));
    }

    private boolean isMouseOverProfileList(double x, double y) {
        return x >= (double)this.profileListX && x < (double)(this.profileListX + this.profileListW) && y >= (double)this.profileListY && y < (double)(this.profileListY + this.profileListH);
    }

    private void applyProfileDataIntoCurrent(PunchyConfig.Data source) {
        if (source == null) {
            return;
        }
        this.data.renderArmorArmsFP = source.renderArmorArmsFP;
        this.data.interactiveStuffCompat = source.interactiveStuffCompat;
        this.data.bettercombatCompat = source.bettercombatCompat;
        this.data.swordBlockingCompat = source.swordBlockingCompat;
        this.data.animationSpeed = source.animationSpeed;
        this.data.enableMod = source.enableMod;
        this.data.enableTuning = source.enableTuning;
        this.data.disableResourcePackModelParts = source.disableResourcePackModelParts;
        this.data.disableArmPhysics = source.disableArmPhysics;
        this.data.disableNativeItemPhysics = source.disableNativeItemPhysics;
        this.data.disableBoatMinecartRaftModels = source.disableBoatMinecartRaftModels;
        this.data.disablePistonModels = source.disablePistonModels;
        this.data.disableChestModels = source.disableChestModels;
        this.data.disableEnchantingTableModels = source.disableEnchantingTableModels;
        this.data.disableBoatFirstPersonAnimations = source.disableBoatFirstPersonAnimations;
        this.data.disableEnhancedFireArmEffects = source.disableEnhancedFireArmEffects;
        this.data.enableBlurGlowAtDark = source.enableBlurGlowAtDark;
        this.data.enableCrawlAnimation = source.enableCrawlAnimation;
        this.data.enableClimbAnimation = source.enableClimbAnimation;
        this.data.enableSwimAnimation = source.enableSwimAnimation;
        this.data.enableElytraFlightAnimation = source.enableElytraFlightAnimation;
        this.data.enableCustomWalk = source.enableCustomWalk;
        this.data.enableSprintArmSwing = source.enableSprintArmSwing;
        this.data.enableFallArmAnimation = source.enableFallArmAnimation;
        this.data.sprintArmSwingIntensity = source.sprintArmSwingIntensity;
        this.data.sprintArmSwingSpeed = source.sprintArmSwingSpeed;
        this.data.enableFreezeShake = source.enableFreezeShake;
        this.data.enableFreezeArmOverlay = source.enableFreezeArmOverlay;
        this.data.enableMud = source.enableMud;
        this.data.enableSweat = source.enableSweat;
        this.data.enableBurn = source.enableBurn;
        this.data.enableParticles = source.enableParticles;
        this.data.enableModelPartsGlow = source.enableModelPartsGlow;
        this.data.firstPersonModelHideEnabled = source.firstPersonModelHideEnabled;
        this.data.firstPersonModelHidePitch = source.firstPersonModelHidePitch;
        this.data.itemBlacklist = source.itemBlacklist == null ? new ArrayList<String>() : new ArrayList<String>(source.itemBlacklist);
        this.data.blacklistApplyDualHanded = source.blacklistApplyDualHanded == null ? new LinkedHashMap<String, Boolean>() : new LinkedHashMap<String, Boolean>(source.blacklistApplyDualHanded);
        PunchyAnimationManager.setAnimationSpeed(this.data.animationSpeed);
    }

    private void openConfigProfilesFolder() {
        try {
            Path dir = ConfigProfileManager.profilesDirectoryPath();
            Files.createDirectories(dir, new FileAttribute[0]);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir.toFile());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void suppressBaseWidgetsForBlacklistModal() {
        for (class_364 child : this.method_25396()) {
            if (!(child instanceof class_339)) continue;
            class_339 widget = (class_339)child;
            widget.field_22763 = false;
            widget.field_22764 = false;
        }
    }

    private int addToggleWithResetScrollable(int row, int startX, int startY, int optionWidth, String labelKey, BooleanSupplier getter, Consumer<Boolean> setter, boolean resetValue) {
        int y = startY + this.rowOffset(row);
        class_4185 toggle = this.createToggleButton(startX, y, optionWidth, labelKey, getter, setter);
        this.registerAnimationWidget(toggle, y);
        this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
            setter.accept(resetValue);
            toggle.method_25355(this.toggleLabel(labelKey, resetValue));
        }).method_46433(startX + optionWidth + 10, y).method_46437(44, 20).method_46431(), y);
        return row + 1;
    }

    private void handleAddBlacklist() {
        if (this.blacklistInput == null) {
            return;
        }
        String raw = this.blacklistInput.method_1882();
        if (raw == null) {
            return;
        }
        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return;
        }
        if (trimmed.endsWith(":")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.indexOf(58) >= 0 ? !PunchyConfigScreen.isValidNamespacedBlacklistPattern(trimmed) : !PunchyConfigScreen.isValidNamespace(trimmed)) {
            return;
        }
        if (this.data.itemBlacklist == null) {
            this.data.itemBlacklist = new ArrayList<String>();
        }
        if (this.data.blacklistApplyDualHanded == null) {
            this.data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>();
        }
        if (!this.data.itemBlacklist.contains(trimmed)) {
            this.data.itemBlacklist.add(trimmed);
        }
        this.data.blacklistApplyDualHanded.putIfAbsent(trimmed, false);
        this.blacklistDraft = "";
        this.blacklistInput.method_1852("");
        this.rebuild();
    }

    private static boolean isValidNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return false;
        }
        for (int i = 0; i < namespace.length(); ++i) {
            boolean ok;
            char c = namespace.charAt(i);
            boolean bl = ok = c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.';
            if (ok) continue;
            return false;
        }
        return true;
    }

    private static boolean isValidNamespacedBlacklistPattern(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        int firstColon = value.indexOf(58);
        if (firstColon <= 0 || firstColon != value.lastIndexOf(58)) {
            return false;
        }
        String namespace = value.substring(0, firstColon);
        String path = value.substring(firstColon + 1);
        if (!PunchyConfigScreen.isValidNamespace(namespace) || path.isBlank()) {
            return false;
        }
        for (int i = 0; i < path.length(); ++i) {
            boolean ok;
            char c = path.charAt(i);
            boolean bl = ok = c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '/' || c == '*' || c == '?';
            if (ok) continue;
            return false;
        }
        return true;
    }

    private void openBlacklistModal() {
        this.blacklistModalOpen = true;
        this.ensureBlacklistCatalog();
        this.refreshBlacklistFilteredEntries();
        this.blacklistModalScrollOffset = 0;
        this.blacklistNamespaceScrollOffset = 0;
        this.rebuild();
    }

    private void closeBlacklistModal() {
        if (!this.blacklistModalOpen) {
            return;
        }
        this.blacklistModalOpen = false;
        this.rebuild();
    }

    private void initBlacklistModalWidgets() {
        this.ensureBlacklistCatalog();
        this.computeBlacklistModalLayout();
        int searchX = this.blacklistModalLeft + 14;
        int searchWidth = Math.max(140, this.blacklistModalRight - 14 - searchX);
        int searchY = this.blacklistModalTop + 30;
        this.blacklistSearchInput = new class_342(this.field_22793, searchX, searchY, searchWidth, 20, (class_2561)class_2561.method_43471((String)"punchy.config.blacklist.search"));
        this.blacklistSearchInput.method_1852(this.blacklistSearchDraft);
        this.blacklistSearchInput.method_47404((class_2561)class_2561.method_43471((String)"punchy.config.blacklist.search"));
        this.blacklistSearchInput.method_1863(value -> {
            this.blacklistSearchDraft = value == null ? "" : value;
            this.blacklistModalScrollOffset = 0;
            this.refreshBlacklistFilteredEntries();
        });
        this.method_37063((class_364)this.blacklistSearchInput);
    }

    private void ensureBlacklistCatalog() {
        if (!this.blacklistCatalogEntries.isEmpty()) {
            return;
        }
        LinkedHashSet<String> namespaces = new LinkedHashSet<String>();
        HashSet<String> seenIds = new HashSet<String>();
        Collection<class_1792> catalogItems = PunchyConfigScreen.getBlacklistCatalogItems();
        for (class_1792 item : catalogItems) {
            try {
                String key;
                class_2960 id;
                if (item == class_1802.field_8162 || (id = PunchyConfigScreen.resolveBlacklistCatalogId(item)) == null || !seenIds.add(key = id.toString().toLowerCase(Locale.ROOT))) continue;
                class_1799 stack = PunchyConfigScreen.createBlacklistCatalogStack(item);
                String displayName = PunchyConfigScreen.resolveBlacklistCatalogDisplayName(stack, item, id);
                String namespace = id.method_12836().toLowerCase(Locale.ROOT);
                String searchName = (displayName + " " + key + " " + namespace).toLowerCase(Locale.ROOT);
                this.blacklistCatalogEntries.add(new BlacklistCatalogEntry(key, namespace, displayName, searchName, stack));
                namespaces.add(namespace);
            }
            catch (Throwable throwable) {}
        }
        this.appendTaczVirtualBlacklistEntries(namespaces);
        this.blacklistCatalogEntries.sort((left, right) -> {
            int byName = left.displayName.compareToIgnoreCase(right.displayName);
            if (byName != 0) {
                return byName;
            }
            return left.id.compareTo(right.id);
        });
        for (String namespace : namespaces) {
            this.blacklistModFilters.add(new BlacklistModFilter(namespace, this.resolveModDisplayName(namespace)));
        }
        this.blacklistModFilters.sort((left, right) -> left.displayName.compareToIgnoreCase(right.displayName));
    }

    private void appendTaczVirtualBlacklistEntries(Set<String> namespaces) {
        HashSet<String> seenIds = new HashSet<String>();
        for (BlacklistCatalogEntry blacklistCatalogEntry : this.blacklistCatalogEntries) {
            seenIds.add(blacklistCatalogEntry.id);
        }
        for (TaczBlacklistCompat.VirtualEntry virtualEntry : TaczBlacklistCompat.collectGunEntries()) {
            if (!seenIds.add(virtualEntry.id())) continue;
            this.blacklistCatalogEntries.add(new BlacklistCatalogEntry(virtualEntry.id(), virtualEntry.namespace(), virtualEntry.displayName(), virtualEntry.searchName(), virtualEntry.icon()));
            namespaces.add(virtualEntry.namespace());
        }
    }

    private static Collection<class_1792> getBlacklistCatalogItems() {
        Collection<class_1792> forgeItems = PunchyConfigScreen.getForgeRegistryItems();
        if (forgeItems != null && !forgeItems.isEmpty()) {
            return forgeItems;
        }
        ArrayList<class_1792> builtinItems = new ArrayList<class_1792>();
        for (class_1792 item : class_7923.field_41178) {
            builtinItems.add(item);
        }
        return builtinItems;
    }

    private static Collection<class_1792> getForgeRegistryItems() {
        if (Services.PLATFORM == null || !"forge".equalsIgnoreCase(Services.PLATFORM.getPlatformName())) {
            return null;
        }
        try {
            Class<?> forgeRegistriesClass = Class.forName("net.minecraftforge.registries.ForgeRegistries");
            Object itemRegistry = forgeRegistriesClass.getField("ITEMS").get(null);
            Method getValuesMethod = itemRegistry.getClass().getMethod("getValues", new Class[0]);
            Object values = getValuesMethod.invoke(itemRegistry, new Object[0]);
            if (!(values instanceof Collection)) {
                return null;
            }
            Collection rawValues = (Collection)values;
            ArrayList<class_1792> items = new ArrayList<class_1792>(rawValues.size());
            for (Object value : rawValues) {
                if (!(value instanceof class_1792)) continue;
                class_1792 item = (class_1792)value;
                items.add(item);
            }
            return items;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static class_2960 resolveBlacklistCatalogId(class_1792 item) {
        if (item == null) {
            return null;
        }
        try {
            class_2960 id = class_7923.field_41178.method_10221((Object)item);
            if (id != null && item != class_1802.field_8162) {
                return id;
            }
        }
        catch (Throwable id) {
            // empty catch block
        }
        class_2960 forgeId = PunchyConfigScreen.resolveForgeItemId(item);
        if (forgeId != null) {
            return forgeId;
        }
        return null;
    }

    private static class_2960 resolveForgeItemId(class_1792 item) {
        if (item == null || Services.PLATFORM == null || !"forge".equalsIgnoreCase(Services.PLATFORM.getPlatformName())) {
            return null;
        }
        try {
            Class<?> forgeRegistriesClass = Class.forName("net.minecraftforge.registries.ForgeRegistries");
            Object itemRegistry = forgeRegistriesClass.getField("ITEMS").get(null);
            Method getKeyMethod = itemRegistry.getClass().getMethod("getKey", Object.class);
            Object result = getKeyMethod.invoke(itemRegistry, item);
            if (result instanceof class_2960) {
                class_2960 id = (class_2960)result;
                return id;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static class_1799 createBlacklistCatalogStack(class_1792 item) {
        if (item == null) {
            return class_1799.field_8037;
        }
        try {
            class_1799 stack = item.method_7854();
            if (stack != null && !stack.method_7960()) {
                return stack;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            return new class_1799((class_1935)item);
        }
        catch (Throwable throwable) {
            return class_1799.field_8037;
        }
    }

    private static String resolveBlacklistCatalogDisplayName(class_1799 stack, class_1792 item, class_2960 id) {
        String displayName = null;
        if (stack != null && !stack.method_7960()) {
            try {
                displayName = stack.method_7964().getString();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (displayName == null || displayName.isBlank()) {
            try {
                displayName = class_2561.method_43471((String)item.method_7876()).getString();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = id != null ? id.toString() : "unknown:item";
        }
        return displayName;
    }

    private void refreshBlacklistFilteredEntries() {
        this.blacklistFilteredEntries.clear();
        String query = this.blacklistSearchDraft == null ? "" : this.blacklistSearchDraft.trim().toLowerCase(Locale.ROOT);
        for (BlacklistCatalogEntry entry : this.blacklistCatalogEntries) {
            if (!this.blacklistSelectedNamespace.isBlank() && !this.blacklistSelectedNamespace.equals(entry.namespace) || !query.isEmpty() && !entry.id.contains(query) && !entry.searchName.contains(query)) continue;
            this.blacklistFilteredEntries.add(entry);
        }
        this.blacklistFilteredEntries.sort((left, right) -> {
            boolean rightEnabled;
            boolean leftEnabled = this.isBlacklistEnabled(left.id);
            if (leftEnabled != (rightEnabled = this.isBlacklistEnabled(right.id))) {
                return leftEnabled ? -1 : 1;
            }
            int byName = left.displayName.compareToIgnoreCase(right.displayName);
            if (byName != 0) {
                return byName;
            }
            return left.id.compareTo(right.id);
        });
        this.blacklistModalScrollOffset = PunchyConfigScreen.clamp(this.blacklistModalScrollOffset, 0, Math.max(0, this.blacklistModalMaxScroll));
    }

    private void computeBlacklistModalLayout() {
        int modalWidth = Math.min(620, Math.max(460, this.field_22789 - 80));
        int modalHeight = Math.min(302, Math.max(202, this.field_22790 - 96));
        this.blacklistModalLeft = (this.field_22789 - modalWidth) / 2;
        this.blacklistModalTop = (this.field_22790 - modalHeight) / 2;
        this.blacklistModalRight = this.blacklistModalLeft + modalWidth;
        this.blacklistModalBottom = this.blacklistModalTop + modalHeight;
        this.blacklistListLeft = this.blacklistModalLeft + 14;
        this.blacklistListRight = this.blacklistModalRight - 14;
        this.blacklistCloseButtonLeft = this.blacklistModalRight - 28;
        this.blacklistCloseButtonTop = this.blacklistModalTop + 8;
        this.blacklistCloseButtonRight = this.blacklistCloseButtonLeft + 20;
        this.blacklistCloseButtonBottom = this.blacklistCloseButtonTop + 20;
        int footerButtonWidth = 98;
        int footerButtonHeight = 18;
        int footerGap = 8;
        int footerTotalWidth = footerButtonWidth * 2 + footerGap;
        int footerStartX = this.blacklistModalLeft + (modalWidth - footerTotalWidth) / 2;
        int footerY = this.blacklistModalBottom - 26;
        this.blacklistResetButtonLeft = footerStartX;
        this.blacklistResetButtonTop = footerY;
        this.blacklistResetButtonRight = footerStartX + footerButtonWidth;
        this.blacklistResetButtonBottom = footerY + footerButtonHeight;
        this.blacklistDoneButtonLeft = this.blacklistResetButtonRight + footerGap;
        this.blacklistDoneButtonTop = footerY;
        this.blacklistDoneButtonRight = this.blacklistDoneButtonLeft + footerButtonWidth;
        this.blacklistDoneButtonBottom = footerY + footerButtonHeight;
    }

    private void ensureBlacklistDataStructures() {
        if (this.data.itemBlacklist == null) {
            this.data.itemBlacklist = new ArrayList<String>();
        }
        if (this.data.blacklistApplyDualHanded == null) {
            this.data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>();
        }
    }

    private boolean isBlacklistEnabled(String entry) {
        if (entry == null || entry.isBlank() || this.data.itemBlacklist == null) {
            return false;
        }
        return this.data.itemBlacklist.contains(entry);
    }

    private void moveBlacklistEntryToTop(String entry) {
        this.ensureBlacklistDataStructures();
        this.data.itemBlacklist.removeIf(existing -> entry.equals(existing));
        this.data.itemBlacklist.add(0, entry);
    }

    private void toggleBlacklistEnabled(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        this.ensureBlacklistDataStructures();
        if (this.isBlacklistEnabled(entry)) {
            this.data.itemBlacklist.removeIf(existing -> entry.equals(existing));
            this.data.blacklistApplyDualHanded.remove(entry);
        } else {
            this.moveBlacklistEntryToTop(entry);
            this.data.blacklistApplyDualHanded.putIfAbsent(entry, false);
            this.blacklistModalScrollOffset = 0;
        }
        this.refreshBlacklistFilteredEntries();
    }

    private void toggleBlacklistDual(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        this.ensureBlacklistDataStructures();
        if (!this.isBlacklistEnabled(entry)) {
            this.moveBlacklistEntryToTop(entry);
            this.data.blacklistApplyDualHanded.put(entry, true);
            this.blacklistModalScrollOffset = 0;
            this.refreshBlacklistFilteredEntries();
            return;
        }
        boolean nextValue = !Boolean.TRUE.equals(this.data.blacklistApplyDualHanded.get(entry));
        this.data.blacklistApplyDualHanded.put(entry, nextValue);
    }

    private void toggleBlacklistNamespace(String namespace) {
        if (!PunchyConfigScreen.isValidNamespace(namespace)) {
            return;
        }
        this.blacklistSelectedNamespace = namespace.equals(this.blacklistSelectedNamespace) ? "" : namespace;
        this.blacklistModalScrollOffset = 0;
        this.refreshBlacklistFilteredEntries();
    }

    private String resolveModDisplayName(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return "";
        }
        if ("minecraft".equals(namespace)) {
            return "Minecraft";
        }
        String resolved = Services.PLATFORM.getModDisplayName(namespace);
        if (resolved != null && !resolved.isBlank() && !resolved.equalsIgnoreCase(namespace)) {
            return resolved;
        }
        return PunchyConfigScreen.humanizeNamespace(namespace);
    }

    private static String humanizeNamespace(String namespace) {
        String normalized = namespace.replace('_', ' ').replace('-', ' ').trim();
        if (normalized.isBlank()) {
            return namespace;
        }
        StringBuilder out = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (int i = 0; i < normalized.length(); ++i) {
            char c = normalized.charAt(i);
            if (c == ' ') {
                out.append(c);
                capitalize = true;
                continue;
            }
            if (capitalize) {
                out.append(Character.toUpperCase(c));
                capitalize = false;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private boolean allFilteredEnabled() {
        if (this.blacklistFilteredEntries.isEmpty()) {
            return false;
        }
        for (BlacklistCatalogEntry entry : this.blacklistFilteredEntries) {
            if (this.isBlacklistEnabled(entry.id)) continue;
            return false;
        }
        return true;
    }

    private boolean allFilteredDual() {
        if (this.blacklistFilteredEntries.isEmpty()) {
            return false;
        }
        for (BlacklistCatalogEntry entry : this.blacklistFilteredEntries) {
            if (!this.isBlacklistEnabled(entry.id)) {
                return false;
            }
            if (this.isBlacklistDualHanded(entry.id)) continue;
            return false;
        }
        return true;
    }

    private void setFilteredEnableAll(boolean enable) {
        if (this.blacklistFilteredEntries.isEmpty()) {
            return;
        }
        this.ensureBlacklistDataStructures();
        if (enable) {
            for (int i = this.blacklistFilteredEntries.size() - 1; i >= 0; --i) {
                BlacklistCatalogEntry entry = this.blacklistFilteredEntries.get(i);
                this.moveBlacklistEntryToTop(entry.id);
                this.data.blacklistApplyDualHanded.putIfAbsent(entry.id, false);
            }
        } else {
            for (BlacklistCatalogEntry entry : this.blacklistFilteredEntries) {
                this.data.itemBlacklist.removeIf(existing -> entry.id.equals(existing));
                this.data.blacklistApplyDualHanded.remove(entry.id);
            }
        }
        this.blacklistModalScrollOffset = 0;
        this.refreshBlacklistFilteredEntries();
    }

    private void setFilteredDualAll(boolean dual) {
        if (this.blacklistFilteredEntries.isEmpty()) {
            return;
        }
        this.ensureBlacklistDataStructures();
        for (int i = this.blacklistFilteredEntries.size() - 1; i >= 0; --i) {
            BlacklistCatalogEntry entry = this.blacklistFilteredEntries.get(i);
            if (!this.isBlacklistEnabled(entry.id)) {
                this.moveBlacklistEntryToTop(entry.id);
            }
            this.data.blacklistApplyDualHanded.put(entry.id, dual);
        }
        this.blacklistModalScrollOffset = 0;
        this.refreshBlacklistFilteredEntries();
    }

    private void resetBlacklistOnly() {
        PunchyConfig.Data defaults = new PunchyConfig.Data();
        this.data.itemBlacklist = new ArrayList<String>(defaults.itemBlacklist);
        this.data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>(defaults.blacklistApplyDualHanded);
        this.blacklistModalScrollOffset = 0;
        this.refreshBlacklistFilteredEntries();
    }

    private boolean isMouseInside(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX >= (double)left && mouseX <= (double)right && mouseY >= (double)top && mouseY <= (double)bottom;
    }

    private int addLinkButton(int row, int startX, int startY, int width, String labelKey, String url) {
        int y = startY + this.rowOffset(row);
        class_4185 linkButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)labelKey), button -> this.openLink(url)).method_46433(startX, y).method_46437(width, 20).method_46431());
        if ("punchy.config.support.donate".equals(labelKey)) {
            this.supportDonateButton = linkButton;
        }
        return row + 1;
    }

    private void openLink(String url) {
        if (url == null || url.isBlank() || this.field_22787 == null) {
            return;
        }
        class_310 client = this.field_22787;
        client.method_1507((class_437)new class_407(confirmed -> {
            if (confirmed) {
                PunchyConfigScreen.openUri(url);
            }
            client.method_1507((class_437)this);
        }, url, true));
    }

    private static void openUri(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            if (!Desktop.isDesktopSupported()) {
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return;
            }
            desktop.browse(new URI(url));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private int addToggleWithReset(int row, int startX, int startY, int optionWidth, String labelKey, BooleanSupplier getter, Consumer<Boolean> setter, boolean resetValue) {
        int y = startY + this.rowOffset(row);
        class_4185 toggle = this.createToggleButton(startX, y, optionWidth, labelKey, getter, setter);
        this.registerAnimationWidget(toggle, y);
        this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
            setter.accept(resetValue);
            toggle.method_25355(this.toggleLabel(labelKey, resetValue));
        }).method_46433(startX + optionWidth + 10, y).method_46437(44, 20).method_46431(), y);
        return row + 1;
    }

    private int addInteractiveStuffCompatToggle(int row, int startX, int startY, int optionWidth) {
        int y = startY + this.rowOffset(row);
        class_4185 toggle = class_4185.method_46430((class_2561)this.interactiveStuffCompatLabel(this.data.interactiveStuffCompat), btn -> {
            this.data.interactiveStuffCompat = !this.data.interactiveStuffCompat;
            btn.method_25355(this.interactiveStuffCompatLabel(this.data.interactiveStuffCompat));
        }).method_46433(startX, y).method_46437(optionWidth, 20).method_46431();
        this.registerAnimationWidget(toggle, y);
        this.registerAnimationWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.config.reset.option"), button -> {
            this.data.interactiveStuffCompat = false;
            toggle.method_25355(this.interactiveStuffCompatLabel(false));
        }).method_46433(startX + optionWidth + 10, y).method_46437(44, 20).method_46431(), y);
        return row + 1;
    }

    private class_4185 createToggleButton(int x, int y, int width, String labelKey, BooleanSupplier getter, Consumer<Boolean> setter) {
        class_4185 button = class_4185.method_46430((class_2561)this.toggleLabel(labelKey, getter.getAsBoolean()), btn -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            btn.method_25355(this.toggleLabel(labelKey, next));
        }).method_46433(x, y).method_46437(width, 20).method_46431();
        return button;
    }

    private class_2561 toggleLabel(String labelKey, boolean value) {
        return class_2561.method_43471((String)labelKey).method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43471((String)(value ? "punchy.toggle.on" : "punchy.toggle.off")));
    }

    private class_2561 interactiveStuffCompatLabel(boolean value) {
        return class_2561.method_43471((String)"punchy.config.option.interactive_stuff_compat").method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43471((String)(value ? "punchy.toggle.on" : "punchy.toggle.off")));
    }

    private void saveAndClose() {
        if (this.data.enableTuning && !this.data.enableMod) {
            this.data.enableMod = true;
        }
        PunchyConfig.Data live = PunchyConfig.snapshot();
        this.data.animationPackSelections = live.animationPackSelections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(live.animationPackSelections);
        this.data.animationPackTypeSelections = live.animationPackTypeSelections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(live.animationPackTypeSelections);
        this.data.mixSpecificForceOverride = live.mixSpecificForceOverride == null ? new LinkedHashMap<String, Boolean>() : new LinkedHashMap<String, Boolean>(live.mixSpecificForceOverride);
        this.restoreGuiScaleOverride();
        PunchyConfig.applyAndSave(this.data);
        PunchyDebug.setDebugPositionEnabled(this.data.enableTuning && this.data.enableMod);
        PunchyAnimationManager.setAnimationSpeed(PunchyConfig.getAnimationSpeed());
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
            this.restoreGuiScaleOverride();
        }
    }

    private void resetAll() {
        PunchyConfig.Data defaults = new PunchyConfig.Data();
        this.data.renderArmorArmsFP = defaults.renderArmorArmsFP;
        this.data.interactiveStuffCompat = defaults.interactiveStuffCompat;
        this.data.bettercombatCompat = defaults.bettercombatCompat;
        this.data.swordBlockingCompat = defaults.swordBlockingCompat;
        this.data.animationSpeed = defaults.animationSpeed;
        this.data.enableMod = defaults.enableMod;
        this.data.enableTuning = defaults.enableTuning;
        this.data.disableResourcePackModelParts = defaults.disableResourcePackModelParts;
        this.data.disableArmPhysics = defaults.disableArmPhysics;
        this.data.disableNativeItemPhysics = defaults.disableNativeItemPhysics;
        this.data.disableBoatMinecartRaftModels = defaults.disableBoatMinecartRaftModels;
        this.data.disablePistonModels = defaults.disablePistonModels;
        this.data.disableChestModels = defaults.disableChestModels;
        this.data.disableEnchantingTableModels = defaults.disableEnchantingTableModels;
        this.data.disableBoatFirstPersonAnimations = defaults.disableBoatFirstPersonAnimations;
        this.data.disableEnhancedFireArmEffects = defaults.disableEnhancedFireArmEffects;
        this.data.enableBlurGlowAtDark = defaults.enableBlurGlowAtDark;
        this.data.enableCrawlAnimation = defaults.enableCrawlAnimation;
        this.data.enableClimbAnimation = defaults.enableClimbAnimation;
        this.data.enableSwimAnimation = defaults.enableSwimAnimation;
        this.data.enableElytraFlightAnimation = defaults.enableElytraFlightAnimation;
        this.data.enableCustomWalk = defaults.enableCustomWalk;
        this.data.enableSprintArmSwing = defaults.enableSprintArmSwing;
        this.data.enableFallArmAnimation = defaults.enableFallArmAnimation;
        this.data.sprintArmSwingIntensity = defaults.sprintArmSwingIntensity;
        this.data.sprintArmSwingSpeed = defaults.sprintArmSwingSpeed;
        this.data.enableFreezeShake = defaults.enableFreezeShake;
        this.data.enableFreezeArmOverlay = defaults.enableFreezeArmOverlay;
        this.data.enableMud = defaults.enableMud;
        this.data.enableSweat = defaults.enableSweat;
        this.data.enableBurn = defaults.enableBurn;
        this.data.enableParticles = defaults.enableParticles;
        this.data.enableModelPartsGlow = defaults.enableModelPartsGlow;
        this.data.firstPersonModelHideEnabled = defaults.firstPersonModelHideEnabled;
        this.data.firstPersonModelHidePitch = defaults.firstPersonModelHidePitch;
        this.data.itemBlacklist = new ArrayList<String>(defaults.itemBlacklist);
        this.data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>(defaults.blacklistApplyDualHanded);
        PunchyAnimationManager.setAnimationSpeed(this.data.animationSpeed);
    }

    public void method_25419() {
        this.saveAndClose();
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        int maxScroll;
        int thumbBottom;
        int thumbTop;
        int thumbOffset;
        int thumbMax;
        int thumbHeight;
        int trackHeight;
        int barBottom;
        int barTop;
        int totalHeight;
        int maxScroll2;
        int i;
        graphics.method_25294(0, 0, this.field_22789, this.field_22790, -1442840576);
        if (this.blacklistModalOpen) {
            this.drawConfigPanels(graphics);
            super.method_25394(graphics, mouseX, mouseY, delta);
            this.drawBlacklistModalOverlay(graphics, mouseX, mouseY);
            if (this.blacklistSearchInput != null && this.blacklistSearchInput.field_22764) {
                this.blacklistSearchInput.method_25394(graphics, mouseX, mouseY, delta);
            }
            return;
        }
        this.drawConfigPanels(graphics);
        ArrayList<class_4185> styledButtons = new ArrayList<class_4185>();
        ArrayList<class_2561> originalMessages = new ArrayList<class_2561>();
        ArrayList<StyledConfigSlider> styledSliders = new ArrayList<StyledConfigSlider>();
        ArrayList<class_2561> originalSliderMessages = new ArrayList<class_2561>();
        for (class_364 child : this.method_25396()) {
            if (child instanceof class_4185) {
                class_4185 button = (class_4185)child;
                if (button.field_22764) {
                    styledButtons.add(button);
                    originalMessages.add(button.method_25369());
                    button.method_25355((class_2561)class_2561.method_43473());
                    continue;
                }
            }
            if (!(child instanceof StyledConfigSlider)) continue;
            StyledConfigSlider slider = (StyledConfigSlider)child;
            if (!slider.field_22764) continue;
            styledSliders.add(slider);
            originalSliderMessages.add(slider.method_25369());
            slider.method_25355((class_2561)class_2561.method_43473());
        }
        super.method_25394(graphics, mouseX, mouseY, delta);
        for (i = 0; i < styledButtons.size(); ++i) {
            ((class_4185)styledButtons.get(i)).method_25355((class_2561)originalMessages.get(i));
        }
        for (i = 0; i < styledSliders.size(); ++i) {
            ((StyledConfigSlider)((Object)styledSliders.get(i))).method_25355((class_2561)originalSliderMessages.get(i));
        }
        this.renderStyledSliders(graphics, mouseX, mouseY);
        this.renderStyledButtons(graphics, mouseX, mouseY);
        this.drawProfileListPanel(graphics);
        if (this.blacklistHelpLines != null && !this.blacklistHelpLines.isEmpty()) {
            Objects.requireNonNull(this.field_22793);
            int lineHeight = (int)Math.ceil(9.0f * 1.0f);
            for (int i2 = 0; i2 < this.blacklistHelpLines.size(); ++i2) {
                class_5481 line = this.blacklistHelpLines.get(i2);
                int textX = this.blacklistHelpX;
                int textY = this.blacklistHelpY + i2 * lineHeight;
                graphics.method_51430(this.field_22793, line, textX, textY, -1, false);
            }
        }
        if (this.tab == Tab.BLACKLIST && this.blacklistViewportHeight > 0 && !this.blacklistEntryButtons.isEmpty() && (maxScroll2 = Math.max(0, (totalHeight = this.blacklistEntryButtons.size() * this.blacklistEntryHeight - 6) - this.blacklistViewportHeight)) > 0) {
            int barWidth = 4;
            int barX = this.blacklistHelpX + this.blacklistHelpWidth - barWidth;
            barTop = this.blacklistListStartY;
            barBottom = this.blacklistListStartY + this.blacklistViewportHeight;
            graphics.method_25294(barX, barTop, barX + barWidth, barBottom, -1442840576);
            trackHeight = Math.max(1, barBottom - barTop);
            thumbHeight = Math.max(8, Math.round((float)trackHeight * ((float)this.blacklistViewportHeight / (float)totalHeight)));
            thumbMax = trackHeight - thumbHeight;
            thumbOffset = maxScroll2 == 0 ? 0 : Math.round((float)thumbMax * ((float)this.blacklistScrollOffset / (float)maxScroll2));
            thumbTop = barTop + thumbOffset;
            thumbBottom = Math.min(barBottom, thumbTop + thumbHeight);
            graphics.method_25294(barX, thumbTop, barX + barWidth, thumbBottom, -1);
        }
        if (this.animationViewportHeight > 0 && this.animationRowCount > 0 && (maxScroll = Math.max(0, (totalHeight = this.animationRowCount * this.animationEntryHeight - 6) - this.animationViewportHeight)) > 0) {
            int barWidth = 4;
            int barX = Math.max(this.centerPanelLeft + 2, this.centerPanelRight - 7);
            barTop = this.animationListStartY;
            barBottom = this.animationListStartY + this.animationViewportHeight;
            graphics.method_25294(barX, barTop, barX + barWidth, barBottom, -1442840576);
            trackHeight = Math.max(1, barBottom - barTop);
            thumbHeight = Math.max(8, Math.round((float)trackHeight * ((float)this.animationViewportHeight / (float)totalHeight)));
            thumbMax = trackHeight - thumbHeight;
            thumbOffset = maxScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.animationScrollOffset / (float)maxScroll));
            thumbTop = barTop + thumbOffset;
            thumbBottom = Math.min(barBottom, thumbTop + thumbHeight);
            graphics.method_25294(barX, thumbTop, barX + barWidth, thumbBottom, -1);
        }
    }

    public void method_25420(class_332 graphics, int mouseX, int mouseY, float delta) {
    }

    public void method_52752(class_332 graphics) {
    }

    private void renderStyledSliders(class_332 graphics, int mouseX, int mouseY) {
        for (class_364 child : this.method_25396()) {
            if (!(child instanceof StyledConfigSlider)) continue;
            StyledConfigSlider slider = (StyledConfigSlider)child;
            if (!slider.field_22764) continue;
            slider.drawStyled(graphics, mouseX, mouseY);
        }
    }

    private void drawConfigPanels(class_332 graphics) {
        int panelFill = -267381478;
        int panelBorder = -11641751;
        graphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelRight, this.mainPanelBottom, panelFill);
        graphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelRight, this.mainPanelTop + 1, panelBorder);
        graphics.method_25294(this.mainPanelLeft, this.mainPanelBottom - 1, this.mainPanelRight, this.mainPanelBottom, panelBorder);
        graphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelLeft + 1, this.mainPanelBottom, panelBorder);
        graphics.method_25294(this.mainPanelRight - 1, this.mainPanelTop, this.mainPanelRight, this.mainPanelBottom, panelBorder);
        int sectionFill = 857944122;
        int sectionBorder = 1430145621;
        this.drawSectionPanel(graphics, this.leftPanelLeft, this.leftPanelTop, this.leftPanelRight, this.leftPanelBottom, sectionFill, sectionBorder);
        this.drawSectionPanel(graphics, this.centerPanelLeft, this.centerPanelTop, this.centerPanelRight, this.centerPanelBottom, sectionFill, sectionBorder);
        this.drawSectionPanel(graphics, this.rightPanelLeft, this.rightPanelTop, this.rightPanelRight, this.rightPanelBottom, sectionFill, sectionBorder);
    }

    private void drawSectionPanel(class_332 graphics, int left, int top, int right, int bottom, int fill, int border) {
        graphics.method_25294(left, top, right, bottom, fill);
        graphics.method_25294(left, top, right, top + 1, border);
        graphics.method_25294(left, bottom - 1, right, bottom, border);
        graphics.method_25294(left, top, left + 1, bottom, border);
        graphics.method_25294(right - 1, top, right, bottom, border);
    }

    private void drawProfileListPanel(class_332 graphics) {
        if (this.profileListW <= 0 || this.profileListH <= 0) {
            return;
        }
        int x0 = this.profileListX;
        int y0 = this.profileListY;
        int x1 = this.profileListX + this.profileListW;
        int y1 = this.profileListY + this.profileListH;
        graphics.method_25294(x0, y0, x1, y1, 1141641495);
    }

    private void renderStyledButtons(class_332 graphics, int mouseX, int mouseY) {
        for (class_364 child : this.method_25396()) {
            if (!(child instanceof class_4185)) continue;
            class_4185 button = (class_4185)child;
            if (!button.field_22764) continue;
            boolean destructive = button == this.profileDeleteButton;
            boolean positive = button == this.supportDonateButton;
            this.drawStyledButton(graphics, button, mouseX, mouseY, destructive, positive);
        }
    }

    private void drawStyledButton(class_332 graphics, class_4185 button, int mouseX, int mouseY, boolean destructive, boolean positive) {
        int textColor;
        int border;
        int fill;
        int left = button.method_46426();
        int top = button.method_46427();
        int right = left + button.method_25368();
        int bottom = top + button.method_25364();
        boolean hovered = this.isMouseInside(mouseX, mouseY, left, top, right, bottom);
        boolean active = button.field_22763;
        if (!active) {
            fill = -14801620;
            border = -12694955;
            textColor = -8483429;
        } else if (positive) {
            fill = hovered ? -13726141 : -14521293;
            border = -8531567;
            textColor = -1;
        } else if (destructive) {
            fill = hovered ? -7459282 : -9690590;
            border = -3048067;
            textColor = -1;
        } else if (hovered) {
            fill = -12958126;
            border = -7485697;
            textColor = -1;
        } else {
            fill = -13682107;
            border = -7429710;
            textColor = -1;
        }
        graphics.method_25294(left, top, right, bottom, fill);
        graphics.method_25294(left, top, right, top + 1, border);
        graphics.method_25294(left, bottom - 1, right, bottom, border);
        graphics.method_25294(left, top, left + 1, bottom, border);
        graphics.method_25294(right - 1, top, right, bottom, border);
        String label = button.method_25369() == null ? "" : button.method_25369().getString();
        int maxTextWidth = Math.max(8, button.method_25368() - 12);
        String renderLabel = this.resolveScrollableButtonLabel(label, maxTextWidth, hovered);
        int textX = left + (button.method_25368() - this.field_22793.method_1727(renderLabel)) / 2;
        int textY = top + (button.method_25364() - 8) / 2;
        graphics.method_25303(this.field_22793, renderLabel, textX, textY, textColor);
    }

    private String resolveScrollableButtonLabel(String label, int maxTextWidth, boolean hovered) {
        if (label == null || label.isEmpty()) {
            return "";
        }
        if (this.field_22793.method_1727(label) <= maxTextWidth) {
            return label;
        }
        if (!hovered) {
            if (maxTextWidth > this.field_22793.method_1727("...")) {
                return this.field_22793.method_27523(label, maxTextWidth - this.field_22793.method_1727("...")) + "...";
            }
            return this.field_22793.method_27523(label, maxTextWidth);
        }
        String cycle = label + "   ";
        int cycleLength = cycle.length();
        if (cycleLength <= 1) {
            return this.field_22793.method_27523(label, maxTextWidth);
        }
        long tick = System.currentTimeMillis() / 150L;
        int offset = (int)(tick % (long)cycleLength);
        String shifted = cycle.substring(offset) + cycle.substring(0, offset);
        String visible = this.field_22793.method_27523(shifted, maxTextWidth);
        if (visible.isBlank()) {
            visible = this.field_22793.method_27523(label, maxTextWidth);
        }
        return visible;
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (!this.blacklistModalOpen) {
            if (button == 0) {
                if (this.isMouseOverBlacklistScrollbar(mouseX, mouseY)) {
                    this.draggingBlacklistScrollbar = true;
                    this.updateBlacklistScrollFromMouse(mouseY);
                    return true;
                }
                if (this.isMouseOverAnimationScrollbar(mouseX, mouseY)) {
                    this.draggingAnimationScrollbar = true;
                    this.updateAnimationScrollFromMouse(mouseY);
                    return true;
                }
            }
            return super.method_25402(mouseX, mouseY, button);
        }
        if (button != 0) {
            return true;
        }
        if (this.isMouseOverBlacklistModalScrollbar(mouseX, mouseY)) {
            this.draggingBlacklistModalScrollbar = true;
            this.updateBlacklistModalScrollFromMouse(mouseY);
            return true;
        }
        if (super.method_25402(mouseX, mouseY, button)) {
            return true;
        }
        if (this.isMouseOverBlacklistNamespaceScrollbar(mouseX, mouseY)) {
            this.draggingBlacklistNamespaceScrollbar = true;
            this.updateBlacklistNamespaceScrollFromMouse(mouseY);
            return true;
        }
        if (this.isMouseInside(mouseX, mouseY, this.blacklistResetButtonLeft, this.blacklistResetButtonTop, this.blacklistResetButtonRight, this.blacklistResetButtonBottom)) {
            this.resetBlacklistOnly();
            return true;
        }
        if (this.isMouseInside(mouseX, mouseY, this.blacklistDoneButtonLeft, this.blacklistDoneButtonTop, this.blacklistDoneButtonRight, this.blacklistDoneButtonBottom)) {
            this.closeBlacklistModal();
            return true;
        }
        for (BlacklistHitbox hitbox : this.blacklistNamespaceHitboxes) {
            if (!this.isMouseInside(mouseX, mouseY, hitbox.left, hitbox.top, hitbox.right, hitbox.bottom)) continue;
            this.toggleBlacklistNamespace(hitbox.key);
            return true;
        }
        for (BlacklistHitbox hitbox : this.blacklistEnableHitboxes) {
            if (!this.isMouseInside(mouseX, mouseY, hitbox.left, hitbox.top, hitbox.right, hitbox.bottom)) continue;
            if (BLACKLIST_BULK_ENABLE_KEY.equals(hitbox.key)) {
                this.setFilteredEnableAll(!this.allFilteredEnabled());
            } else {
                this.toggleBlacklistEnabled(hitbox.key);
            }
            return true;
        }
        for (BlacklistHitbox hitbox : this.blacklistDualHitboxes) {
            if (!this.isMouseInside(mouseX, mouseY, hitbox.left, hitbox.top, hitbox.right, hitbox.bottom)) continue;
            if (BLACKLIST_BULK_DUAL_KEY.equals(hitbox.key)) {
                this.setFilteredDualAll(!this.allFilteredDual());
            } else {
                this.toggleBlacklistDual(hitbox.key);
            }
            return true;
        }
        if (this.isMouseInside(mouseX, mouseY, this.blacklistModalLeft, this.blacklistModalTop, this.blacklistModalRight, this.blacklistModalBottom)) {
            return true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25403(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (this.draggingBlacklistModalScrollbar) {
                this.updateBlacklistModalScrollFromMouse(mouseY);
                return true;
            }
            if (this.draggingBlacklistNamespaceScrollbar) {
                this.updateBlacklistNamespaceScrollFromMouse(mouseY);
                return true;
            }
            if (!this.blacklistModalOpen && this.draggingBlacklistScrollbar) {
                this.updateBlacklistScrollFromMouse(mouseY);
                return true;
            }
            if (!this.blacklistModalOpen && this.draggingAnimationScrollbar) {
                this.updateAnimationScrollFromMouse(mouseY);
                return true;
            }
        }
        return super.method_25403(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingBlacklistModalScrollbar = false;
            this.draggingBlacklistNamespaceScrollbar = false;
            this.draggingBlacklistScrollbar = false;
            this.draggingAnimationScrollbar = false;
        }
        return super.method_25406(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        int maxScroll;
        if (!this.blacklistModalOpen && this.isMouseOverProfileList(mouseX, mouseY) && (maxScroll = Math.max(0, this.profiles.size() - this.profileRowButtons.size())) > 0) {
            this.profileListScroll = class_3532.method_15340((int)(this.profileListScroll - (int)Math.signum(scrollDeltaY)), (int)0, (int)maxScroll);
            this.updateProfileRowButtons();
            return true;
        }
        if (this.handleBlacklistNamespaceScroll(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }
        if (this.handleBlacklistModalScroll(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }
        if (this.handleBlacklistScroll(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }
        if (this.handleAnimationScroll(mouseX, mouseY, scrollDeltaY)) {
            return true;
        }
        return super.method_25401(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
    }

    private boolean handleBlacklistModalScroll(double mouseX, double mouseY, double scrollDelta) {
        if (!this.blacklistModalOpen) {
            return false;
        }
        if (this.blacklistModalMaxScroll <= 0) {
            return false;
        }
        if (!this.isMouseInside(mouseX, mouseY, this.blacklistListLeft, this.blacklistListTop, this.blacklistListRight, this.blacklistListBottom)) {
            return false;
        }
        int deltaPixels = (int)Math.round(scrollDelta * 22.0);
        this.blacklistModalScrollOffset = PunchyConfigScreen.clamp(this.blacklistModalScrollOffset - deltaPixels, 0, this.blacklistModalMaxScroll);
        return true;
    }

    private boolean handleBlacklistNamespaceScroll(double mouseX, double mouseY, double scrollDelta) {
        if (!this.blacklistModalOpen) {
            return false;
        }
        if (this.blacklistNamespaceMaxScroll <= 0) {
            return false;
        }
        if (!this.isMouseInside(mouseX, mouseY, this.blacklistNamespaceAreaLeft, this.blacklistNamespaceAreaTop, this.blacklistNamespaceAreaRight, this.blacklistNamespaceAreaBottom)) {
            return false;
        }
        int deltaPixels = (int)Math.round(scrollDelta * 20.0);
        this.blacklistNamespaceScrollOffset = PunchyConfigScreen.clamp(this.blacklistNamespaceScrollOffset - deltaPixels, 0, this.blacklistNamespaceMaxScroll);
        return true;
    }

    private boolean isMouseOverBlacklistModalScrollbar(double mouseX, double mouseY) {
        if (!this.blacklistModalOpen || this.blacklistModalMaxScroll <= 0) {
            return false;
        }
        int barWidth = 4;
        int barX = this.blacklistListRight - barWidth;
        return this.isMouseInside(mouseX, mouseY, barX, this.blacklistListTop, barX + barWidth, this.blacklistListBottom);
    }

    private boolean isMouseOverBlacklistNamespaceScrollbar(double mouseX, double mouseY) {
        if (!this.blacklistModalOpen || this.blacklistNamespaceMaxScroll <= 0) {
            return false;
        }
        int barWidth = 4;
        int barX = this.blacklistNamespaceAreaRight - barWidth;
        return this.isMouseInside(mouseX, mouseY, barX, this.blacklistNamespaceAreaTop, barX + barWidth, this.blacklistNamespaceAreaBottom);
    }

    private void updateBlacklistModalScrollFromMouse(double mouseY) {
        if (this.blacklistModalMaxScroll <= 0) {
            return;
        }
        int barTop = this.blacklistListTop;
        int barBottom = this.blacklistListBottom;
        int track = Math.max(1, barBottom - barTop);
        double ratio = (mouseY - (double)barTop) / (double)track;
        this.blacklistModalScrollOffset = PunchyConfigScreen.clamp((int)Math.round(ratio * (double)this.blacklistModalMaxScroll), 0, this.blacklistModalMaxScroll);
    }

    private void updateBlacklistNamespaceScrollFromMouse(double mouseY) {
        if (this.blacklistNamespaceMaxScroll <= 0) {
            return;
        }
        int barTop = this.blacklistNamespaceAreaTop;
        int barBottom = this.blacklistNamespaceAreaBottom;
        int track = Math.max(1, barBottom - barTop);
        double ratio = (mouseY - (double)barTop) / (double)track;
        this.blacklistNamespaceScrollOffset = PunchyConfigScreen.clamp((int)Math.round(ratio * (double)this.blacklistNamespaceMaxScroll), 0, this.blacklistNamespaceMaxScroll);
    }

    private int computeBlacklistChipTotalHeight(int chipLeft, int chipContentRight, int chipHeight, int chipGap) {
        int chipLayoutX = chipLeft;
        int chipLayoutY = 0;
        int chipTotalHeight = chipHeight;
        for (BlacklistModFilter filter : this.blacklistModFilters) {
            int chipWidth = Math.max(50, this.field_22793.method_1727(filter.displayName) + 18);
            if (chipLayoutX + chipWidth > chipContentRight) {
                chipLayoutX = chipLeft;
                chipLayoutY += chipHeight + chipGap;
            }
            chipTotalHeight = Math.max(chipTotalHeight, chipLayoutY + chipHeight);
            chipLayoutX += chipWidth + chipGap;
        }
        return chipTotalHeight;
    }

    private void drawBlacklistModalOverlay(class_332 graphics, int mouseX, int mouseY) {
        String doneLabel;
        String resetLabel;
        this.ensureBlacklistCatalog();
        this.computeBlacklistModalLayout();
        this.blacklistNamespaceHitboxes.clear();
        this.blacklistEnableHitboxes.clear();
        this.blacklistDualHitboxes.clear();
        graphics.method_25294(this.blacklistModalLeft, this.blacklistModalTop, this.blacklistModalRight, this.blacklistModalBottom, -267381478);
        int border = -11641751;
        graphics.method_25294(this.blacklistModalLeft, this.blacklistModalTop, this.blacklistModalRight, this.blacklistModalTop + 1, border);
        graphics.method_25294(this.blacklistModalLeft, this.blacklistModalBottom - 1, this.blacklistModalRight, this.blacklistModalBottom, border);
        graphics.method_25294(this.blacklistModalLeft, this.blacklistModalTop, this.blacklistModalLeft + 1, this.blacklistModalBottom, border);
        graphics.method_25294(this.blacklistModalRight - 1, this.blacklistModalTop, this.blacklistModalRight, this.blacklistModalBottom, border);
        class_5250 titleLabel = class_2561.method_43471((String)"punchy.config.blacklist.modal_title");
        String titleText = titleLabel.getString();
        if (titleText == null || titleText.isBlank() || titleText.equals("punchy.config.blacklist.modal_title")) {
            titleText = "Item Blacklist";
        }
        graphics.method_25303(this.field_22793, titleText, this.blacklistModalLeft + 14, this.blacklistModalTop + 12, -1);
        int namespaceTitleY = this.blacklistModalTop + 58;
        String globalLabel = class_1074.method_4662((String)"punchy.config.blacklist.global", (Object[])new Object[0]);
        if (globalLabel == null || globalLabel.isBlank() || globalLabel.equals("punchy.config.blacklist.global")) {
            globalLabel = "Filtrar por mod";
        }
        graphics.method_25303(this.field_22793, globalLabel, this.blacklistModalLeft + 14, namespaceTitleY, -4799537);
        int chipLeft = this.blacklistModalLeft + 14;
        int chipRight = this.blacklistModalRight - 18;
        int chipHeight = 16;
        int chipGap = 4;
        int chipViewportTop = namespaceTitleY + 12;
        int chipViewportHeight = chipHeight * 2 + chipGap * 1;
        int chipViewportBottom = chipViewportTop + chipViewportHeight;
        int chipBarWidth = 4;
        int chipContentRight = chipRight;
        int chipTotalHeight = this.computeBlacklistChipTotalHeight(chipLeft, chipContentRight, chipHeight, chipGap);
        this.blacklistNamespaceMaxScroll = Math.max(0, chipTotalHeight - chipViewportHeight);
        if (this.blacklistNamespaceMaxScroll > 0) {
            chipContentRight = chipRight - chipBarWidth - 2;
            chipTotalHeight = this.computeBlacklistChipTotalHeight(chipLeft, chipContentRight, chipHeight, chipGap);
            this.blacklistNamespaceMaxScroll = Math.max(0, chipTotalHeight - chipViewportHeight);
        }
        this.blacklistNamespaceScrollOffset = PunchyConfigScreen.clamp(this.blacklistNamespaceScrollOffset, 0, this.blacklistNamespaceMaxScroll);
        this.blacklistNamespaceAreaLeft = chipLeft;
        this.blacklistNamespaceAreaRight = chipRight;
        this.blacklistNamespaceAreaTop = chipViewportTop;
        this.blacklistNamespaceAreaBottom = chipViewportBottom;
        graphics.method_25294(chipLeft, chipViewportTop, chipRight, chipViewportBottom, 705631516);
        graphics.method_25294(chipLeft, chipViewportTop, chipRight, chipViewportTop + 1, 1430145621);
        graphics.method_25294(chipLeft, chipViewportBottom - 1, chipRight, chipViewportBottom, 1430145621);
        int chipX = chipLeft;
        int chipY = 0;
        int chipRow = 0;
        int chipRowHeight = chipHeight + chipGap;
        int firstVisibleChipRow = Math.max(0, this.blacklistNamespaceScrollOffset / Math.max(1, chipRowHeight));
        int lastVisibleChipRowExclusive = firstVisibleChipRow + 2;
        for (BlacklistModFilter filter : this.blacklistModFilters) {
            String label = filter.displayName;
            int chipWidth = Math.max(50, this.field_22793.method_1727(label) + 18);
            if (chipX + chipWidth > chipContentRight) {
                chipX = chipLeft;
                chipY += chipHeight + chipGap;
                ++chipRow;
            }
            if (chipRow < firstVisibleChipRow || chipRow >= lastVisibleChipRowExclusive) {
                chipX += chipWidth + chipGap;
                continue;
            }
            int drawTop = chipViewportTop + chipY - this.blacklistNamespaceScrollOffset;
            int drawBottom = drawTop + chipHeight;
            if (drawBottom <= chipViewportTop || drawTop >= chipViewportBottom) {
                chipX += chipWidth + chipGap;
                continue;
            }
            boolean selected = filter.namespace.equals(this.blacklistSelectedNamespace);
            int chipFill = selected ? -13869680 : -14471110;
            int chipBorder = selected ? -7485697 : -9798515;
            graphics.method_25294(chipX, drawTop, chipX + chipWidth, drawBottom, chipFill);
            graphics.method_25294(chipX, drawTop, chipX + chipWidth, drawTop + 1, chipBorder);
            graphics.method_25294(chipX, drawBottom - 1, chipX + chipWidth, drawBottom, chipBorder);
            graphics.method_25294(chipX, drawTop, chipX + 1, drawBottom, chipBorder);
            graphics.method_25294(chipX + chipWidth - 1, drawTop, chipX + chipWidth, drawBottom, chipBorder);
            graphics.method_25303(this.field_22793, label, chipX + 8, drawTop + 4, -1);
            int clippedTop = Math.max(drawTop, chipViewportTop);
            int clippedBottom = Math.min(drawBottom, chipViewportBottom);
            if (clippedBottom > clippedTop) {
                this.blacklistNamespaceHitboxes.add(new BlacklistHitbox(chipX, clippedTop, chipX + chipWidth, clippedBottom, filter.namespace));
            }
            chipX += chipWidth + chipGap;
        }
        if (this.blacklistNamespaceMaxScroll > 0) {
            int barX = chipRight - chipBarWidth;
            int trackHeight = Math.max(1, chipViewportBottom - chipViewportTop);
            int thumbHeight = Math.max(10, Math.round((float)trackHeight * ((float)chipViewportHeight / (float)chipTotalHeight)));
            int thumbMax = trackHeight - thumbHeight;
            int thumbOffset = this.blacklistNamespaceMaxScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.blacklistNamespaceScrollOffset / (float)this.blacklistNamespaceMaxScroll));
            graphics.method_25294(barX, chipViewportTop, barX + chipBarWidth, chipViewportBottom, -1442840576);
            graphics.method_25294(barX, chipViewportTop + thumbOffset, barX + chipBarWidth, Math.min(chipViewportBottom, chipViewportTop + thumbOffset + thumbHeight), -1);
        }
        this.blacklistListLeft = this.blacklistModalLeft + 14;
        this.blacklistListRight = this.blacklistModalRight - 14;
        this.blacklistListTop = chipViewportBottom + 10;
        this.blacklistListBottom = this.blacklistResetButtonTop - 8;
        if (this.blacklistListBottom <= this.blacklistListTop) {
            this.blacklistListBottom = this.blacklistListTop + 22;
        }
        int rawViewportHeight = this.blacklistListBottom - this.blacklistListTop;
        int viewportRows = Math.max(1, rawViewportHeight / 22);
        int viewportHeight = viewportRows * 22;
        this.blacklistListBottom = this.blacklistListTop + viewportHeight;
        int totalHeight = (this.blacklistFilteredEntries.size() + 1) * 22;
        this.blacklistModalMaxScroll = Math.max(0, totalHeight - viewportHeight);
        this.blacklistModalScrollOffset = PunchyConfigScreen.clamp(this.blacklistModalScrollOffset, 0, this.blacklistModalMaxScroll);
        graphics.method_25294(this.blacklistListLeft, this.blacklistListTop, this.blacklistListRight, this.blacklistListBottom, 1141641495);
        graphics.method_25294(this.blacklistListLeft, this.blacklistListTop, this.blacklistListRight, this.blacklistListTop + 1, 1430145621);
        graphics.method_25294(this.blacklistListLeft, this.blacklistListBottom - 1, this.blacklistListRight, this.blacklistListBottom, 1430145621);
        int actionEnableWidth = 66;
        int actionDualWidth = 66;
        int actionGap = 6;
        int contentRight = this.blacklistListRight - 6;
        int dualLeft = contentRight - actionDualWidth;
        int enableLeft = dualLeft - actionGap - actionEnableWidth;
        boolean bulkEnableOn = this.allFilteredEnabled();
        boolean bulkDualOn = this.allFilteredDual();
        int bulkTop = this.blacklistListTop - this.blacklistModalScrollOffset;
        int bulkBottom = bulkTop + 22;
        if (bulkBottom > this.blacklistListTop && bulkTop < this.blacklistListBottom) {
            graphics.method_25294(this.blacklistListLeft + 1, bulkTop, this.blacklistListRight - 1, bulkBottom, 859064927);
            String bulkLabel = class_1074.method_4662((String)"punchy.config.blacklist.bulk", (Object[])new Object[0]);
            if (bulkLabel == null || bulkLabel.isBlank() || bulkLabel.equals("punchy.config.blacklist.bulk")) {
                bulkLabel = "Acoes em massa (filtro atual)";
            }
            graphics.method_25303(this.field_22793, bulkLabel, this.blacklistListLeft + 8, bulkTop + 7, -1);
            this.drawBlacklistActionButton(graphics, enableLeft, bulkTop + 2, enableLeft + actionEnableWidth, bulkBottom - 2, this.isMouseInside(mouseX, mouseY, enableLeft, bulkTop + 2, enableLeft + actionEnableWidth, bulkBottom - 2), bulkEnableOn, this.blacklistFilteredEntries.isEmpty(), this.blacklistEnableLabel(bulkEnableOn));
            this.blacklistEnableHitboxes.add(new BlacklistHitbox(enableLeft, bulkTop + 2, enableLeft + actionEnableWidth, bulkBottom - 2, BLACKLIST_BULK_ENABLE_KEY));
            this.drawBlacklistActionButton(graphics, dualLeft, bulkTop + 2, dualLeft + actionDualWidth, bulkBottom - 2, this.isMouseInside(mouseX, mouseY, dualLeft, bulkTop + 2, dualLeft + actionDualWidth, bulkBottom - 2), bulkDualOn, this.blacklistFilteredEntries.isEmpty(), this.blacklistDualShortLabel(bulkDualOn));
            this.blacklistDualHitboxes.add(new BlacklistHitbox(dualLeft, bulkTop + 2, dualLeft + actionDualWidth, bulkBottom - 2, BLACKLIST_BULK_DUAL_KEY));
        }
        for (int i = 0; i < this.blacklistFilteredEntries.size(); ++i) {
            int rowTop = this.blacklistListTop + (i + 1) * 22 - this.blacklistModalScrollOffset;
            int rowBottom = rowTop + 22;
            if (rowBottom <= this.blacklistListTop || rowTop >= this.blacklistListBottom) continue;
            BlacklistCatalogEntry entry = this.blacklistFilteredEntries.get(i);
            boolean enabled = this.isBlacklistEnabled(entry.id);
            boolean dual = this.isBlacklistDualHanded(entry.id);
            int rowFill = (i & 1) == 0 ? 706553648 : 438118192;
            graphics.method_25294(this.blacklistListLeft + 1, rowTop, this.blacklistListRight - 1, rowBottom, rowFill);
            if (enabled) {
                graphics.method_25294(this.blacklistListLeft + 1, rowTop, this.blacklistListLeft + 3, rowBottom, -9713270);
            }
            int iconY = rowTop + Math.max(0, 3);
            graphics.method_51427(entry.icon, this.blacklistListLeft + 5, iconY);
            int textX = this.blacklistListLeft + 25;
            graphics.method_25303(this.field_22793, entry.displayName, textX, rowTop + 3, -1);
            graphics.method_25303(this.field_22793, entry.id, textX, rowTop + 12, -7429710);
            this.drawBlacklistActionButton(graphics, enableLeft, rowTop + 2, enableLeft + actionEnableWidth, rowBottom - 2, this.isMouseInside(mouseX, mouseY, enableLeft, rowTop + 2, enableLeft + actionEnableWidth, rowBottom - 2), enabled, false, this.blacklistEnableLabel(enabled));
            this.blacklistEnableHitboxes.add(new BlacklistHitbox(enableLeft, rowTop + 2, enableLeft + actionEnableWidth, rowBottom - 2, entry.id));
            this.drawBlacklistActionButton(graphics, dualLeft, rowTop + 2, dualLeft + actionDualWidth, rowBottom - 2, this.isMouseInside(mouseX, mouseY, dualLeft, rowTop + 2, dualLeft + actionDualWidth, rowBottom - 2), dual, false, this.blacklistDualShortLabel(dual));
            this.blacklistDualHitboxes.add(new BlacklistHitbox(dualLeft, rowTop + 2, dualLeft + actionDualWidth, rowBottom - 2, entry.id));
        }
        if (this.blacklistModalMaxScroll > 0) {
            int barWidth = 4;
            int barX = this.blacklistListRight - barWidth;
            int barTop = this.blacklistListTop;
            int barBottom = this.blacklistListBottom;
            int trackHeight = Math.max(1, barBottom - barTop);
            int thumbHeight = Math.max(10, Math.round((float)trackHeight * ((float)viewportHeight / (float)totalHeight)));
            int thumbMax = trackHeight - thumbHeight;
            int thumbOffset = this.blacklistModalMaxScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.blacklistModalScrollOffset / (float)this.blacklistModalMaxScroll));
            graphics.method_25294(barX, barTop, barX + barWidth, barBottom, -1442840576);
            graphics.method_25294(barX, barTop + thumbOffset, barX + barWidth, Math.min(barBottom, barTop + thumbOffset + thumbHeight), -1);
        }
        if ((resetLabel = class_1074.method_4662((String)"punchy.config.reset", (Object[])new Object[0])) == null || resetLabel.isBlank() || resetLabel.equals("punchy.config.reset")) {
            resetLabel = "Reset";
        }
        if ((doneLabel = class_1074.method_4662((String)"gui.done", (Object[])new Object[0])) == null || doneLabel.isBlank() || doneLabel.equals("gui.done")) {
            doneLabel = "Done";
        }
        this.drawBlacklistFooterButton(graphics, this.blacklistResetButtonLeft, this.blacklistResetButtonTop, this.blacklistResetButtonRight, this.blacklistResetButtonBottom, this.isMouseInside(mouseX, mouseY, this.blacklistResetButtonLeft, this.blacklistResetButtonTop, this.blacklistResetButtonRight, this.blacklistResetButtonBottom), true, resetLabel);
        this.drawBlacklistFooterButton(graphics, this.blacklistDoneButtonLeft, this.blacklistDoneButtonTop, this.blacklistDoneButtonRight, this.blacklistDoneButtonBottom, this.isMouseInside(mouseX, mouseY, this.blacklistDoneButtonLeft, this.blacklistDoneButtonTop, this.blacklistDoneButtonRight, this.blacklistDoneButtonBottom), false, doneLabel);
    }

    private void drawBlacklistActionButton(class_332 graphics, int left, int top, int right, int bottom, boolean hovered, boolean activeState, boolean disabled, String label) {
        int textColor;
        int border;
        int fill;
        if (disabled) {
            fill = -14801620;
            border = -12694955;
            textColor = -8483429;
        } else if (activeState) {
            fill = hovered ? -13994943 : -14393546;
            border = -8662895;
            textColor = -1;
        } else {
            fill = hovered ? -12958126 : -13682107;
            border = -7429710;
            textColor = -1;
        }
        graphics.method_25294(left, top, right, bottom, fill);
        graphics.method_25294(left, top, right, top + 1, border);
        graphics.method_25294(left, bottom - 1, right, bottom, border);
        graphics.method_25294(left, top, left + 1, bottom, border);
        graphics.method_25294(right - 1, top, right, bottom, border);
        int textX = left + (right - left - this.field_22793.method_1727(label)) / 2;
        int textY = top + (bottom - top - 8) / 2;
        graphics.method_25303(this.field_22793, label, textX, textY, textColor);
    }

    private void drawBlacklistFooterButton(class_332 graphics, int left, int top, int right, int bottom, boolean hovered, boolean destructive, String label) {
        int fill = destructive ? (hovered ? -7459282 : -9690590) : (hovered ? -12958126 : -13682107);
        int border = destructive ? -3048067 : -7429710;
        graphics.method_25294(left, top, right, bottom, fill);
        graphics.method_25294(left, top, right, top + 1, border);
        graphics.method_25294(left, bottom - 1, right, bottom, border);
        graphics.method_25294(left, top, left + 1, bottom, border);
        graphics.method_25294(right - 1, top, right, bottom, border);
        int textX = left + (right - left - this.field_22793.method_1727(label)) / 2;
        int textY = top + (bottom - top - 8) / 2;
        graphics.method_25303(this.field_22793, label, textX, textY, -1);
    }

    private boolean handleBlacklistScroll(double mouseX, double mouseY, double scrollDelta) {
        if (this.tab != Tab.BLACKLIST) {
            return false;
        }
        if (this.blacklistEntryButtons.isEmpty()) {
            return false;
        }
        int viewTop = this.blacklistListStartY;
        int viewBottom = this.blacklistListStartY + this.blacklistViewportHeight;
        if (mouseX < (double)this.blacklistHelpX || mouseX > (double)(this.blacklistHelpX + this.blacklistHelpWidth)) {
            return false;
        }
        if (mouseY < (double)viewTop || mouseY > (double)viewBottom) {
            return false;
        }
        int totalHeight = this.blacklistEntryButtons.size() * this.blacklistEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.blacklistViewportHeight);
        if (maxScroll <= 0) {
            return false;
        }
        int deltaPixels = (int)Math.round(scrollDelta * (double)this.blacklistEntryHeight);
        this.blacklistScrollOffset = PunchyConfigScreen.clamp(this.blacklistScrollOffset - deltaPixels, 0, maxScroll);
        this.updateBlacklistScroll();
        return true;
    }

    private void updateBlacklistScroll() {
        int viewTop = this.blacklistListStartY;
        int viewBottom = this.blacklistListStartY + this.blacklistViewportHeight;
        for (int i = 0; i < this.blacklistEntryButtons.size(); ++i) {
            boolean visible;
            int y = this.blacklistListStartY + i * this.blacklistEntryHeight - this.blacklistScrollOffset;
            class_4185 label = this.blacklistEntryButtons.get(i);
            class_4185 dual = this.blacklistDualButtons.get(i);
            class_4185 remove = this.blacklistRemoveButtons.get(i);
            label.method_46419(y);
            dual.method_46419(y);
            remove.method_46419(y);
            label.field_22764 = visible = y + 20 > viewTop && y < viewBottom;
            dual.field_22764 = visible;
            remove.field_22764 = visible;
            label.field_22763 = visible;
            dual.field_22763 = visible;
            remove.field_22763 = visible;
        }
    }

    private boolean isMouseOverBlacklistScrollbar(double mouseX, double mouseY) {
        int totalHeight = this.blacklistEntryButtons.size() * this.blacklistEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.blacklistViewportHeight);
        if (this.tab != Tab.BLACKLIST || maxScroll <= 0 || this.blacklistViewportHeight <= 0) {
            return false;
        }
        int barWidth = 4;
        int barX = this.blacklistHelpX + this.blacklistHelpWidth - barWidth;
        return this.isMouseInside(mouseX, mouseY, barX, this.blacklistListStartY, barX + barWidth, this.blacklistListStartY + this.blacklistViewportHeight);
    }

    private void updateBlacklistScrollFromMouse(double mouseY) {
        int totalHeight = this.blacklistEntryButtons.size() * this.blacklistEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.blacklistViewportHeight);
        if (maxScroll <= 0 || this.blacklistViewportHeight <= 0) {
            return;
        }
        int barTop = this.blacklistListStartY;
        int barBottom = this.blacklistListStartY + this.blacklistViewportHeight;
        int track = Math.max(1, barBottom - barTop);
        double ratio = (mouseY - (double)barTop) / (double)track;
        this.blacklistScrollOffset = PunchyConfigScreen.clamp((int)Math.round(ratio * (double)maxScroll), 0, maxScroll);
        this.updateBlacklistScroll();
    }

    private boolean isBlacklistDualHanded(String entry) {
        if (entry == null || entry.isBlank() || this.data.blacklistApplyDualHanded == null) {
            return false;
        }
        return Boolean.TRUE.equals(this.data.blacklistApplyDualHanded.get(entry));
    }

    private void setBlacklistDualHanded(String entry, boolean value) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        if (this.data.blacklistApplyDualHanded == null) {
            this.data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>();
        }
        this.data.blacklistApplyDualHanded.put(entry, value);
    }

    private class_2561 blacklistDualLabel(boolean value) {
        return class_2561.method_43471((String)"punchy.config.blacklist.dual_handed.short").method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43471((String)(value ? "punchy.toggle.on" : "punchy.toggle.off")));
    }

    private String blacklistEnableLabel(boolean enabled) {
        String state;
        String label = class_1074.method_4662((String)"punchy.config.blacklist.enable", (Object[])new Object[0]);
        if (label == null || label.isBlank() || label.equals("punchy.config.blacklist.enable")) {
            label = "Enable";
        }
        if ((state = class_1074.method_4662((String)(enabled ? "punchy.toggle.on" : "punchy.toggle.off"), (Object[])new Object[0])) == null || state.isBlank() || state.equals(enabled ? "punchy.toggle.on" : "punchy.toggle.off")) {
            state = enabled ? "On" : "Off";
        }
        return label + ": " + state;
    }

    private String blacklistDualShortLabel(boolean enabled) {
        String state;
        String label = class_2561.method_43471((String)"punchy.config.blacklist.dual_handed.short").getString();
        if (label == null || label.isBlank() || label.equals("punchy.config.blacklist.dual_handed.short")) {
            label = "Dual";
        }
        if ((state = class_1074.method_4662((String)(enabled ? "punchy.toggle.on" : "punchy.toggle.off"), (Object[])new Object[0])) == null || state.isBlank() || state.equals(enabled ? "punchy.toggle.on" : "punchy.toggle.off")) {
            state = enabled ? "On" : "Off";
        }
        return label + ": " + state;
    }

    private boolean handleAnimationScroll(double mouseX, double mouseY, double scrollDelta) {
        if (this.animationRowCount <= 0 || this.animationViewportHeight <= 0) {
            return false;
        }
        int viewTop = this.animationListStartY;
        int viewBottom = this.animationListStartY + this.animationViewportHeight;
        int viewLeft = this.dividerX + 16;
        int viewRight = viewLeft + this.contentPanelWidth;
        if (mouseX < (double)viewLeft || mouseX > (double)viewRight) {
            return false;
        }
        if (mouseY < (double)viewTop || mouseY > (double)viewBottom) {
            return false;
        }
        int totalHeight = this.animationRowCount * this.animationEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.animationViewportHeight);
        if (maxScroll <= 0) {
            return false;
        }
        int deltaPixels = (int)Math.round(scrollDelta * (double)this.animationEntryHeight);
        this.animationScrollOffset = PunchyConfigScreen.clamp(this.animationScrollOffset - deltaPixels, 0, maxScroll);
        this.updateAnimationScroll();
        return true;
    }

    private boolean isMouseOverAnimationScrollbar(double mouseX, double mouseY) {
        int totalHeight = this.animationRowCount * this.animationEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.animationViewportHeight);
        if (maxScroll <= 0 || this.animationViewportHeight <= 0) {
            return false;
        }
        int barWidth = 4;
        int barX = Math.max(this.centerPanelLeft + 2, this.centerPanelRight - 7);
        return this.isMouseInside(mouseX, mouseY, barX, this.animationListStartY, barX + barWidth, this.animationListStartY + this.animationViewportHeight);
    }

    private void updateAnimationScrollFromMouse(double mouseY) {
        int totalHeight = this.animationRowCount * this.animationEntryHeight - 6;
        int maxScroll = Math.max(0, totalHeight - this.animationViewportHeight);
        if (maxScroll <= 0 || this.animationViewportHeight <= 0) {
            return;
        }
        int barTop = this.animationListStartY;
        int barBottom = this.animationListStartY + this.animationViewportHeight;
        int track = Math.max(1, barBottom - barTop);
        double ratio = (mouseY - (double)barTop) / (double)track;
        this.animationScrollOffset = PunchyConfigScreen.clamp((int)Math.round(ratio * (double)maxScroll), 0, maxScroll);
        this.updateAnimationScroll();
    }

    private void updateAnimationScroll() {
        int viewTop = this.animationListStartY;
        int viewBottom = this.animationListStartY + this.animationViewportHeight;
        for (int i = 0; i < this.animationWidgets.size(); ++i) {
            boolean visible;
            class_339 widget = this.animationWidgets.get(i);
            int baseY = this.animationWidgetBaseY.get(i);
            int y = baseY - this.animationScrollOffset;
            widget.method_46419(y);
            int widgetHeight = Math.max(20, widget.method_25364());
            widget.field_22764 = visible = y >= viewTop && y < viewBottom && y + widgetHeight <= viewBottom;
            widget.field_22763 = visible;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<class_5481> splitHelpText(String text, int maxChars) {
        ArrayList<class_5481> lines = new ArrayList<class_5481>();
        if (text == null || text.isBlank()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() == 0) {
                line.append(word);
                continue;
            }
            if (line.length() + 1 + word.length() > maxChars) {
                lines.add(class_5481.method_30747((String)line.toString(), (class_2583)class_2583.field_24360));
                line.setLength(0);
                line.append(word);
                continue;
            }
            line.append(' ').append(word);
        }
        if (line.length() > 0) {
            lines.add(class_5481.method_30747((String)line.toString(), (class_2583)class_2583.field_24360));
        }
        return lines;
    }

    private /* synthetic */ void lambda$rebuild$44(Boolean value) {
        this.data.enableSweat = value;
    }

    private /* synthetic */ boolean lambda$rebuild$43() {
        return this.data.enableSweat;
    }

    private static enum Tab {
        SUPPORT("punchy.config.tab.support"),
        BLACKLIST("punchy.config.tab.blacklist"),
        RENDERING("punchy.config.tab.rendering"),
        ANIMATION("punchy.config.tab.animation"),
        PHYSICS("punchy.config.tab.physics"),
        MODELS("punchy.config.tab.models"),
        COMPAT("punchy.config.tab.compat");

        private final String labelKey;

        private Tab(String labelKey) {
            this.labelKey = labelKey;
        }
    }

    private static final class AnimationSpeedSlider
    extends StyledConfigSlider {
        private final PunchyConfig.Data data;

        private AnimationSpeedSlider(int x, int y, int width, int height, PunchyConfig.Data data) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), AnimationSpeedSlider.toSliderValue(data.animationSpeed));
            this.data = data;
            this.method_25346();
        }

        protected void method_25346() {
            float speed = AnimationSpeedSlider.sliderToSpeed(this.field_22753);
            String formatted = String.format(Locale.ROOT, "%.1f", Float.valueOf(speed));
            this.method_25355((class_2561)class_2561.method_43471((String)"punchy.config.option.animation_speed").method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43470((String)formatted)));
        }

        @Override
        protected String sliderLabel() {
            return class_2561.method_43471((String)"punchy.config.option.animation_speed").getString();
        }

        @Override
        protected String sliderValueText() {
            return String.format(Locale.ROOT, "%.1f", Float.valueOf(AnimationSpeedSlider.sliderToSpeed(this.field_22753)));
        }

        protected void method_25344() {
            float speed = AnimationSpeedSlider.sliderToSpeed(this.field_22753);
            this.data.animationSpeed = speed = (float)Math.round(speed * 10.0f) / 10.0f;
            PunchyAnimationManager.setAnimationSpeed(speed);
        }

        private void setSpeed(float speed) {
            this.field_22753 = AnimationSpeedSlider.toSliderValue(speed);
            this.method_25346();
        }

        private static double toSliderValue(float speed) {
            float min = 0.5f;
            float max = 7.0f;
            return (speed - min) / (max - min);
        }

        private static float sliderToSpeed(double sliderValue) {
            float min = 0.5f;
            float max = 7.0f;
            float clamped = (float)Math.min(1.0, Math.max(0.0, sliderValue));
            return min + (max - min) * clamped;
        }
    }

    private static final class SprintArmSwingIntensitySlider
    extends StyledConfigSlider {
        private final PunchyConfig.Data data;

        private SprintArmSwingIntensitySlider(int x, int y, int width, int height, PunchyConfig.Data data) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), SprintArmSwingIntensitySlider.toSliderValue(data.sprintArmSwingIntensity));
            this.data = data;
            this.method_25346();
        }

        protected void method_25346() {
            float intensity = SprintArmSwingIntensitySlider.sliderToIntensity(this.field_22753);
            int percent = Math.round(intensity * 100.0f);
            this.method_25355((class_2561)class_2561.method_43471((String)"punchy.config.option.sprint_arm_swing_intensity").method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43470((String)(percent + "%"))));
        }

        @Override
        protected String sliderLabel() {
            return class_2561.method_43471((String)"punchy.config.option.sprint_arm_swing_intensity").getString();
        }

        @Override
        protected String sliderValueText() {
            int percent = Math.round(SprintArmSwingIntensitySlider.sliderToIntensity(this.field_22753) * 100.0f);
            return percent + "%";
        }

        protected void method_25344() {
            float intensity = SprintArmSwingIntensitySlider.sliderToIntensity(this.field_22753);
            this.data.sprintArmSwingIntensity = intensity = (float)Math.round(intensity * 100.0f) / 100.0f;
            this.method_25346();
        }

        private void setIntensity(float intensity) {
            this.field_22753 = SprintArmSwingIntensitySlider.toSliderValue(intensity);
            this.method_25346();
        }

        private static double toSliderValue(float intensity) {
            float min = -1.0f;
            float max = 1.0f;
            return (intensity - min) / (max - min);
        }

        private static float sliderToIntensity(double sliderValue) {
            float min = -1.0f;
            float max = 1.0f;
            float clamped = (float)Math.min(1.0, Math.max(0.0, sliderValue));
            return min + (max - min) * clamped;
        }
    }

    private static final class SprintArmSwingSpeedSlider
    extends StyledConfigSlider {
        private final PunchyConfig.Data data;

        private SprintArmSwingSpeedSlider(int x, int y, int width, int height, PunchyConfig.Data data) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), SprintArmSwingSpeedSlider.toSliderValue(data.sprintArmSwingSpeed));
            this.data = data;
            this.method_25346();
        }

        protected void method_25346() {
            float speed = SprintArmSwingSpeedSlider.sliderToSpeed(this.field_22753);
            int percent = Math.round(speed * 100.0f);
            class_5250 label = class_2561.method_43471((String)"punchy.config.option.sprint_arm_swing_speed");
            class_5250 valueLabel = class_2561.method_43470((String)(percent + "%"));
            this.method_25355((class_2561)label.method_27661().method_27693(": ").method_10852((class_2561)valueLabel));
        }

        @Override
        protected String sliderLabel() {
            return class_2561.method_43471((String)"punchy.config.option.sprint_arm_swing_speed").getString();
        }

        @Override
        protected String sliderValueText() {
            int percent = Math.round(SprintArmSwingSpeedSlider.sliderToSpeed(this.field_22753) * 100.0f);
            return percent + "%";
        }

        protected void method_25344() {
            float speed;
            this.data.sprintArmSwingSpeed = speed = SprintArmSwingSpeedSlider.sliderToSpeed(this.field_22753);
        }

        private void setSpeed(float speed) {
            this.field_22753 = SprintArmSwingSpeedSlider.toSliderValue(speed);
            this.method_25346();
        }

        private static double toSliderValue(float speed) {
            return class_3532.method_15350((double)((speed - -1.0f) / 2.0f), (double)0.0, (double)1.0);
        }

        private static float sliderToSpeed(double sliderValue) {
            float value = (float)sliderValue;
            return -1.0f + value * 2.0f;
        }
    }

    private static final class FirstPersonHidePitchSlider
    extends StyledConfigSlider {
        private final PunchyConfig.Data data;

        private FirstPersonHidePitchSlider(int x, int y, int width, int height, PunchyConfig.Data data) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), FirstPersonHidePitchSlider.toSliderValue(data.firstPersonModelHidePitch));
            this.data = data;
            this.method_25346();
        }

        protected void method_25346() {
            float pitch = FirstPersonHidePitchSlider.sliderToPitch(this.field_22753);
            String formatted = FirstPersonHidePitchSlider.valueText(pitch);
            this.method_25355((class_2561)class_2561.method_43471((String)"punchy.config.option.firstperson_hide_pitch").method_10852((class_2561)class_2561.method_43470((String)": ")).method_10852((class_2561)class_2561.method_43470((String)formatted)));
        }

        @Override
        protected String sliderLabel() {
            return class_2561.method_43471((String)"punchy.config.option.firstperson_hide_pitch").getString();
        }

        @Override
        protected String sliderValueText() {
            return FirstPersonHidePitchSlider.valueText(FirstPersonHidePitchSlider.sliderToPitch(this.field_22753));
        }

        @Override
        protected String sliderReservedValueText() {
            return "never";
        }

        protected void method_25344() {
            float pitch = FirstPersonHidePitchSlider.sliderToPitch(this.field_22753);
            this.data.firstPersonModelHidePitch = pitch = (float)Math.round(pitch);
        }

        private static String valueText(float pitch) {
            if (pitch > 85.0f) {
                return "never";
            }
            return String.format(Locale.ROOT, "%.0f", Float.valueOf(pitch));
        }

        private void setPitch(float pitch) {
            this.field_22753 = FirstPersonHidePitchSlider.toSliderValue(pitch);
            this.method_25346();
        }

        private static double toSliderValue(float pitch) {
            float min = 0.0f;
            float max = 90.0f;
            return (pitch - min) / (max - min);
        }

        private static float sliderToPitch(double sliderValue) {
            float min = 0.0f;
            float max = 90.0f;
            float clamped = (float)Math.min(1.0, Math.max(0.0, sliderValue));
            return min + (max - min) * clamped;
        }
    }

    private static final class BlacklistCatalogEntry {
        private final String id;
        private final String namespace;
        private final String displayName;
        private final String searchName;
        private final class_1799 icon;

        private BlacklistCatalogEntry(String id, String namespace, String displayName, String searchName, class_1799 icon) {
            this.id = id;
            this.namespace = namespace;
            this.displayName = displayName;
            this.searchName = searchName;
            this.icon = icon;
        }
    }

    private static final class BlacklistModFilter {
        private final String namespace;
        private final String displayName;

        private BlacklistModFilter(String namespace, String displayName) {
            this.namespace = namespace;
            this.displayName = displayName;
        }
    }

    private static abstract class StyledConfigSlider
    extends class_357 {
        protected StyledConfigSlider(int x, int y, int width, int height, class_2561 message, double value) {
            super(x, y, width, height, message, value);
        }

        protected abstract String sliderLabel();

        protected abstract String sliderValueText();

        protected String sliderReservedValueText() {
            return this.sliderValueText();
        }

        protected void drawStyled(class_332 graphics, int mouseX, int mouseY) {
            int textColor;
            int border;
            int fill;
            int maxLabelWidth;
            String clippedLabel;
            int trackLeft;
            class_310 mc = class_310.method_1551();
            if (!this.field_22764 || mc == null || mc.field_1772 == null) {
                return;
            }
            int left = this.method_46426();
            int top = this.method_46427();
            int right = left + this.method_25368();
            int bottom = top + this.method_25364();
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
            boolean focused = this.method_25370();
            String label = this.sliderLabel();
            String valueText = this.sliderValueText();
            String reservedValueText = this.sliderReservedValueText();
            int textY = top + (this.method_25364() - 8) / 2;
            int labelX = left + 10;
            int valueX = right - 10 - mc.field_1772.method_1727(valueText);
            int reservedValueX = right - 10 - mc.field_1772.method_1727(reservedValueText);
            int trackRight = reservedValueX - 8;
            if (trackRight <= (trackLeft = labelX + mc.field_1772.method_1727(clippedLabel = mc.field_1772.method_27523(label, maxLabelWidth = Math.max(10, reservedValueX - labelX - 16))) + 10) + 12) {
                trackLeft = left + 8;
                trackRight = right - 8;
            }
            int trackWidth = Math.max(1, trackRight - trackLeft);
            if (!this.field_22763) {
                fill = -14801620;
                border = -12694955;
                textColor = -8483429;
            } else if (focused || hovered) {
                fill = -12958126;
                border = -3048067;
                textColor = -1;
            } else {
                fill = -13682107;
                border = -7429710;
                textColor = -1;
            }
            graphics.method_25294(left, top, right, bottom, fill);
            graphics.method_25294(left, top, right, top + 1, border);
            graphics.method_25294(left, bottom - 1, right, bottom, border);
            graphics.method_25294(left, top, left + 1, bottom, border);
            graphics.method_25294(right - 1, top, right, bottom, border);
            int trackTop = top + this.method_25364() / 2 - 2;
            int trackBottom = trackTop + 4;
            graphics.method_25294(trackLeft, trackTop, trackRight, trackBottom, -1441719774);
            int knobWidth = 8;
            int knobTravel = Math.max(0, trackWidth - knobWidth);
            int knobLeft = trackLeft + (int)Math.round(this.field_22753 * (double)knobTravel);
            int knobRight = knobLeft + knobWidth;
            int fillRight = knobLeft + knobWidth / 2;
            graphics.method_25294(trackLeft, trackTop, fillRight, trackBottom, -7459282);
            int knobFill = !this.field_22763 ? -11641751 : (focused || hovered ? -3048067 : -7709604);
            graphics.method_25294(knobLeft, top + 2, knobRight, bottom - 2, knobFill);
            graphics.method_25294(knobLeft, top + 2, knobRight, top + 3, -4732456);
            graphics.method_25294(knobLeft, bottom - 3, knobRight, bottom - 2, -14404542);
            graphics.method_25303(mc.field_1772, clippedLabel, labelX, textY, textColor);
            graphics.method_25303(mc.field_1772, valueText, valueX, textY, textColor);
        }
    }

    private static final class BlacklistHitbox {
        private final int left;
        private final int top;
        private final int right;
        private final int bottom;
        private final String key;

        private BlacklistHitbox(int left, int top, int right, int bottom, String key) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.key = key;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_339
 *  net.minecraft.class_342
 *  net.minecraft.class_3532
 *  net.minecraft.class_364
 *  net.minecraft.class_410
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_7923
 */
package punchy.client.tuning;

import java.awt.Desktop;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_156;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.tuning.MixProfileManager;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

public final class MixThePacksScreen
extends class_437 {
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_SPACING = 4;
    private static final int ROW_STEP = 24;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 6;
    private static final int SCROLLBAR_RESERVE = 10;
    private static final int EXPANDED_CHILD_INSET = 10;
    private static final int MAX_PROFILE_ROWS = 6;
    private static final int PROFILE_ROW_HEIGHT = 14;
    private static final int PANEL_GAP = 12;
    private final class_437 parent;
    private final Set<String> expandedSlots = new HashSet<String>();
    private final List<RowEntry> rows = new ArrayList<RowEntry>();
    private final List<Integer> rowBaseY = new ArrayList<Integer>();
    private final List<class_339> rowWidgets = new ArrayList<class_339>();
    private final List<class_4185> profileRowButtons = new ArrayList<class_4185>();
    private List<String> profiles = List.of();
    private class_4185 doneButton;
    private class_4185 resetOrderButton;
    private class_342 filterBox;
    private class_342 profileNameBox;
    private class_4185 saveProfileButton;
    private class_4185 loadProfileButton;
    private class_4185 deleteProfileButton;
    private class_4185 openProfilesButton;
    private int selectedProfile = -1;
    private int profileListScroll = 0;
    private String statusMessage = "";
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int listLeft = 0;
    private int listRight = 0;
    private int listTop = 0;
    private int listBottom = 0;
    private int containerLeft = 0;
    private int containerTop = 0;
    private int containerRight = 0;
    private int containerBottom = 0;
    private int selectorWidth = 0;
    private int rowWidth = 0;
    private int profilePanelX = 0;
    private int profilePanelW = 0;
    private int listTitleX = 0;
    private int listTitleY = 0;
    private int profilePanelTop = 0;
    private int profilesTitleX = 0;
    private int profilesTitleY = 0;
    private int profileListX = 0;
    private int profileListY = 0;
    private int profileListW = 0;
    private int profileListH = 0;
    private int profileVisibleRows = 6;
    private int profilePanelScroll = 0;
    private int profilePanelMaxScroll = 0;
    private int footerRowY = 0;
    private boolean compactLayout = false;
    private String filterText = "";
    private boolean hasUnsavedProfileChanges = false;
    private PunchyConfig.Data mixSessionBaseline = null;
    private boolean initializedOnce = false;
    private final Map<String, String> stagedPackSelections = new LinkedHashMap<String, String>();
    private final Map<String, String> stagedPackTypeSelections = new LinkedHashMap<String, String>();
    private final Map<String, String> previewPackSelections = new LinkedHashMap<String, String>();
    private final Map<String, String> previewPackTypeSelections = new LinkedHashMap<String, String>();
    private boolean deleteProfileConfirmOpen = false;
    private String pendingDeleteProfileName = null;
    private int deleteModalLeft = 0;
    private int deleteModalTop = 0;
    private int deleteModalRight = 0;
    private int deleteModalBottom = 0;
    private int deleteConfirmButtonLeft = 0;
    private int deleteConfirmButtonTop = 0;
    private int deleteConfirmButtonRight = 0;
    private int deleteConfirmButtonBottom = 0;
    private int deleteCancelButtonLeft = 0;
    private int deleteCancelButtonTop = 0;
    private int deleteCancelButtonRight = 0;
    private int deleteCancelButtonBottom = 0;

    public MixThePacksScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"punchy.mixpacks.title"));
        this.parent = parent;
    }

    protected void method_25426() {
        boolean preserveStagedChanges;
        this.method_37067();
        this.rows.clear();
        this.rowBaseY.clear();
        this.rowWidgets.clear();
        this.profileRowButtons.clear();
        this.scrollOffset = 0;
        boolean bl = preserveStagedChanges = this.initializedOnce && this.hasUnsavedProfileChanges;
        if (!preserveStagedChanges) {
            this.mixSessionBaseline = PunchyConfig.snapshot();
            this.hasUnsavedProfileChanges = false;
            this.initializeStagedSelectionsFromCurrent();
        }
        this.initializedOnce = true;
        int sidePadding = 24;
        int contentPadding = 14;
        int safeWidth = Math.max(420, this.field_22789 - sidePadding * 2);
        int safeHeight = Math.max(300, this.field_22790 - 24);
        int containerWidth = Math.min(760, safeWidth);
        int containerHeight = Math.min(266, safeHeight);
        containerWidth = Math.min(containerWidth, Math.max(1, this.field_22789 - 8));
        containerHeight = Math.min(containerHeight, Math.max(1, this.field_22790 - 8));
        this.containerLeft = (this.field_22789 - containerWidth) / 2;
        this.containerTop = (this.field_22790 - containerHeight) / 2;
        this.containerRight = this.containerLeft + containerWidth;
        this.containerBottom = this.containerTop + containerHeight;
        this.footerRowY = this.containerBottom - contentPadding - 20;
        this.profilePanelW = class_3532.method_15340((int)(containerWidth / 3), (int)220, (int)300);
        int contentTop = this.containerTop + 12;
        int contentBottom = this.footerRowY - 8;
        int estimatedProfileHeight = 210;
        this.listLeft = this.containerLeft + contentPadding;
        int leftAreaWidth = containerWidth - this.profilePanelW - 12;
        boolean bl2 = this.compactLayout = leftAreaWidth < 300 || contentBottom - contentTop < estimatedProfileHeight;
        if (this.compactLayout) {
            this.profilePanelX = this.containerLeft + contentPadding;
            this.profilePanelW = containerWidth - contentPadding * 2;
            this.listRight = this.containerRight - contentPadding;
            this.listTop = contentTop;
            this.profilePanelTop = Math.max(contentTop + 120, contentBottom - estimatedProfileHeight);
            this.listBottom = this.profilePanelTop - 8;
            if (this.listBottom < this.listTop + 80) {
                this.listBottom = this.listTop + 80;
                this.profilePanelTop = this.listBottom + 8;
            }
        } else {
            this.profilePanelX = this.containerRight - contentPadding - this.profilePanelW;
            this.profilePanelTop = contentTop;
            this.listRight = this.profilePanelX - 12;
            this.listTop = contentTop;
            this.listBottom = contentBottom;
        }
        this.selectorWidth = Math.max(92, Math.min(150, Math.max(96, (this.listRight - this.listLeft) / 3)));
        this.rowWidth = Math.max(100, this.listRight - this.listLeft - this.selectorWidth - 6 - 10);
        this.listTitleX = this.listLeft;
        this.listTitleY = this.listTop - 14;
        this.filterBox = (class_342)this.method_37063((class_364)new class_342(this.field_22793, this.listLeft, this.listTop, this.rowWidth + 6 + this.selectorWidth, 20, (class_2561)class_2561.method_43471((String)"punchy.mixpacks.search")));
        this.filterBox.method_1880(80);
        this.filterBox.method_1852(this.filterText == null ? "" : this.filterText);
        this.filterBox.method_47404((class_2561)class_2561.method_43471((String)"punchy.mixpacks.search"));
        this.filterBox.method_1863(value -> {
            this.filterText = value == null ? "" : value;
            this.rebuildRows();
        });
        this.listTop += 26;
        this.resetOrderButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.mixpacks.reset_resourcepack_order"), button -> this.applyResetToResourcePackOrderAndUnloadProfile()).method_46433(0, 0).method_46437(220, 20).method_46431());
        this.doneButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.done"), button -> this.method_25419()).method_46433(0, 0).method_46437(this.profilePanelW, 20).method_46431());
        this.initProfilesPanel();
        if (this.compactLayout) {
            int halfW = (this.profilePanelW - 4) / 2;
            this.resetOrderButton.method_46421(this.profilePanelX);
            this.resetOrderButton.method_46419(this.footerRowY);
            this.resetOrderButton.method_25358(halfW);
            this.doneButton.method_46421(this.profilePanelX + halfW + 4);
            this.doneButton.method_46419(this.footerRowY);
            this.doneButton.method_25358(this.profilePanelW - halfW - 4);
        } else {
            this.resetOrderButton.method_46421(this.listLeft);
            this.resetOrderButton.method_46419(this.footerRowY);
            this.resetOrderButton.method_25358(this.listRight - this.listLeft);
            this.doneButton.method_46421(this.profilePanelX);
            this.doneButton.method_46419(this.footerRowY);
            this.doneButton.method_25358(this.profilePanelW);
        }
        this.recalcProfilePanelScroll();
        this.refreshProfiles(true);
        this.rebuildRows();
        this.recalcDeleteConfirmLayout();
    }

    private void initProfilesPanel() {
        this.profilesTitleX = this.profilePanelX;
        this.profilesTitleY = this.profilePanelTop;
        this.profileListX = this.profilePanelX;
        this.profileListY = this.profilePanelTop;
        this.profileListW = this.profilePanelW;
        int openProfilesY = this.footerRowY - 24;
        int profileBottomLimit = openProfilesY - 8;
        int maxRowsFit = (profileBottomLimit - this.profilePanelTop - 66) / 14;
        this.profileVisibleRows = class_3532.method_15340((int)maxRowsFit, (int)2, (int)6);
        this.profileListH = this.profileVisibleRows * 14;
        this.profileNameBox = (class_342)this.method_37063((class_364)new class_342(this.field_22793, this.profilePanelX, this.profileListY + this.profileListH + 6, this.profilePanelW, 20, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.profile_name")));
        this.profileNameBox.method_1880(64);
        int halfW = (this.profilePanelW - 4) / 2;
        this.saveProfileButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.save_profile"), button -> {
            String name = this.profileNameBox.method_1882();
            if (name == null || name.isBlank()) {
                name = "mix_profile";
                this.profileNameBox.method_1852(name);
            }
            PunchyConfig.Data previous = PunchyConfig.snapshot();
            PunchyConfig.replaceAnimationPackSelections(this.stagedPackSelections, this.stagedPackTypeSelections, previous.mixSpecificForceOverride);
            boolean ok = MixProfileManager.saveCurrentAs(name);
            if (!ok) {
                PunchyConfig.replaceAnimationPackSelections(previous.animationPackSelections, previous.animationPackTypeSelections, previous.mixSpecificForceOverride);
            } else if (this.field_22787 != null) {
                PunchyTuningConfig.reloadActiveProfileFast(this.field_22787.method_1478());
            }
            String string = this.statusMessage = ok ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_saved", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_save_failed").getString();
            if (ok) {
                this.hasUnsavedProfileChanges = false;
                this.mixSessionBaseline = PunchyConfig.snapshot();
                this.initializeStagedSelectionsFromCurrent();
            }
            this.refreshProfiles(true);
            this.rebuildRows();
        }).method_46433(this.profilePanelX, this.profileNameBox.method_46427() + 24).method_46437(halfW, 20).method_46431());
        this.loadProfileButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.load_profile"), button -> {
            String name = this.selectedProfileName();
            boolean ok = name != null && MixProfileManager.loadProfile(name);
            String string = this.statusMessage = ok ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_loaded", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_load_failed").getString();
            if (ok) {
                this.hasUnsavedProfileChanges = false;
                this.mixSessionBaseline = PunchyConfig.snapshot();
                this.initializeStagedSelectionsFromCurrent();
            }
            this.refreshProfiles(false);
            this.rebuildRows();
        }).method_46433(this.profilePanelX + halfW + 4, this.profileNameBox.method_46427() + 24).method_46437(this.profilePanelW - halfW - 4, 20).method_46431());
        this.deleteProfileButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.delete_profile"), button -> this.openDeleteProfileConfirm()).method_46433(this.profilePanelX, this.profileNameBox.method_46427() + 46).method_46437(this.profilePanelW, 20).method_46431());
        this.openProfilesButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.open_profiles_folder"), button -> {
            boolean ok = this.openProfilesFolder();
            this.statusMessage = ok ? class_2561.method_43471((String)"punchy.tuning.ui.status.profiles_folder_opened").getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profiles_folder_open_failed").getString();
        }).method_46433(this.profilePanelX, openProfilesY).method_46437(this.profilePanelW, 20).method_46431());
        for (int i = 0; i < this.profileVisibleRows; ++i) {
            int row = i;
            class_4185 rowButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43473(), button -> this.selectProfileAtVisibleRow(row)).method_46433(this.profileListX, this.profileListY + i * 14).method_46437(this.profileListW, 13).method_46431());
            this.profileRowButtons.add(rowButton);
        }
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
        graphics.method_25294(0, 0, this.field_22789, this.field_22790, -1610612736);
        graphics.method_25294(this.containerLeft, this.containerTop, this.containerRight, this.containerBottom, -15657446);
        graphics.method_25294(this.containerLeft, this.containerTop, this.containerRight, this.containerTop + 1, -14011848);
        graphics.method_25294(this.containerLeft, this.containerBottom - 1, this.containerRight, this.containerBottom, -14011848);
        graphics.method_25294(this.containerLeft, this.containerTop, this.containerLeft + 1, this.containerBottom, -14011848);
        graphics.method_25294(this.containerRight - 1, this.containerTop, this.containerRight, this.containerBottom, -14011848);
        int sectionFill = 857944122;
        int sectionBorder = 1430145621;
        int sectionTop = (this.filterBox != null ? this.filterBox.method_46427() : this.listTop) - 6;
        int leftPanelLeft = this.listLeft - 4;
        int leftPanelRight = this.listRight + 4;
        int leftPanelTop = sectionTop;
        int leftPanelBottom = this.resetOrderButton.method_46427() + this.resetOrderButton.method_25364() + 6;
        graphics.method_25294(leftPanelLeft, leftPanelTop, leftPanelRight, leftPanelBottom, sectionFill);
        graphics.method_25294(leftPanelLeft, leftPanelTop, leftPanelRight, leftPanelTop + 1, sectionBorder);
        graphics.method_25294(leftPanelLeft, leftPanelBottom - 1, leftPanelRight, leftPanelBottom, sectionBorder);
        graphics.method_25294(leftPanelLeft, leftPanelTop, leftPanelLeft + 1, leftPanelBottom, sectionBorder);
        graphics.method_25294(leftPanelRight - 1, leftPanelTop, leftPanelRight, leftPanelBottom, sectionBorder);
        int rightPanelLeft = this.profilePanelX - 4;
        int rightPanelRight = this.profilePanelX + this.profilePanelW + 4;
        int rightPanelTop = sectionTop;
        int rightPanelBottom = this.doneButton.method_46427() + this.doneButton.method_25364() + 6;
        graphics.method_25294(rightPanelLeft, rightPanelTop, rightPanelRight, rightPanelBottom, sectionFill);
        graphics.method_25294(rightPanelLeft, rightPanelTop, rightPanelRight, rightPanelTop + 1, sectionBorder);
        graphics.method_25294(rightPanelLeft, rightPanelBottom - 1, rightPanelRight, rightPanelBottom, sectionBorder);
        graphics.method_25294(rightPanelLeft, rightPanelTop, rightPanelLeft + 1, rightPanelBottom, sectionBorder);
        graphics.method_25294(rightPanelRight - 1, rightPanelTop, rightPanelRight, rightPanelBottom, sectionBorder);
        graphics.method_25294(this.profileListX, this.profileListY, this.profileListX + this.profileListW, this.profileListY + this.profileListH, 1426985752);
        this.drawExpandedTypeGroups(graphics);
        ArrayList<class_4185> styledButtons = new ArrayList<class_4185>();
        ArrayList<class_2561> originalMessages = new ArrayList<class_2561>();
        for (class_364 child : this.method_25396()) {
            if (!(child instanceof class_4185)) continue;
            class_4185 button = (class_4185)child;
            if (!button.field_22764) continue;
            styledButtons.add(button);
            originalMessages.add(button.method_25369());
            button.method_25355((class_2561)class_2561.method_43473());
        }
        super.method_25394(graphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < styledButtons.size(); ++i) {
            ((class_4185)styledButtons.get(i)).method_25355((class_2561)originalMessages.get(i));
        }
        graphics.method_25303(this.field_22793, this.statusMessage == null ? "" : this.statusMessage, this.profilesTitleX, this.openProfilesButton.method_46427() - 12, 11075504);
        this.drawStyledButtons(graphics, mouseX, mouseY);
        this.drawRowIcons(graphics);
        this.drawScrollbar(graphics);
        this.drawDeleteProfileConfirmOverlay(graphics, mouseX, mouseY);
    }

    public void method_25420(class_332 graphics, int mouseX, int mouseY, float partialTick) {
    }

    public void method_52752(class_332 graphics) {
    }

    private void drawStyledButtons(class_332 graphics, int mouseX, int mouseY) {
        for (class_339 widget : this.rowWidgets) {
            if (!(widget instanceof class_4185)) continue;
            class_4185 button = (class_4185)widget;
            this.drawBlacklistButtonStyle(graphics, button, mouseX, mouseY, false);
        }
        for (class_4185 rowButton : this.profileRowButtons) {
            this.drawBlacklistButtonStyle(graphics, rowButton, mouseX, mouseY, false);
        }
        this.drawBlacklistButtonStyle(graphics, this.resetOrderButton, mouseX, mouseY, false);
        this.drawBlacklistButtonStyle(graphics, this.doneButton, mouseX, mouseY, false);
        this.drawBlacklistButtonStyle(graphics, this.saveProfileButton, mouseX, mouseY, false);
        this.drawBlacklistButtonStyle(graphics, this.loadProfileButton, mouseX, mouseY, false);
        this.drawBlacklistButtonStyle(graphics, this.deleteProfileButton, mouseX, mouseY, true);
        this.drawBlacklistButtonStyle(graphics, this.openProfilesButton, mouseX, mouseY, false);
    }

    private void drawBlacklistButtonStyle(class_332 graphics, class_4185 button, int mouseX, int mouseY, boolean destructive) {
        int textColor;
        int border;
        int fill;
        if (button == null || !button.field_22764) {
            return;
        }
        int left = button.method_46426();
        int top = button.method_46427();
        int right = left + button.method_25368();
        int bottom = top + button.method_25364();
        boolean hovered = MixThePacksScreen.isMouseInside(mouseX, mouseY, left, top, right, bottom);
        boolean active = button.field_22763;
        if (!active) {
            fill = -14801620;
            border = -12694955;
            textColor = -8483429;
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

    private void drawExpandedTypeGroups(class_332 graphics) {
        if (this.rows.isEmpty()) {
            return;
        }
        int contentLeft = this.listLeft;
        int contentRight = this.listLeft + this.rowWidth + 6 + this.selectorWidth;
        int i = 0;
        while (i < this.rows.size()) {
            RowEntry next;
            RowEntry row = this.rows.get(i);
            if (!row.child()) {
                ++i;
                continue;
            }
            String slotId = row.slotId();
            int start = i;
            int end = i;
            while (end + 1 < this.rows.size() && (next = this.rows.get(end + 1)).child() && slotId.equals(next.slotId())) {
                ++end;
            }
            int startY = this.rowBaseY.get(start) - this.scrollOffset;
            int endY = this.rowBaseY.get(end) - this.scrollOffset + 20;
            int padX = 0;
            int padTop = 3;
            int padBottom = 3;
            int boxLeft = contentLeft - padX;
            int boxRight = contentRight + padX;
            int boxTop = Math.max(this.listTop, startY - padTop);
            int boxBottom = Math.min(this.listBottom, endY + padBottom);
            if (boxBottom > boxTop) {
                int fillColor = 856890146;
                int borderColor = -1439020458;
                graphics.method_25294(boxLeft, boxTop, boxRight, boxBottom, fillColor);
                graphics.method_25294(boxLeft, boxTop, boxRight, boxTop + 1, borderColor);
                graphics.method_25294(boxLeft, boxBottom - 1, boxRight, boxBottom, borderColor);
                graphics.method_25294(boxLeft, boxTop, boxLeft + 1, boxBottom, borderColor);
                graphics.method_25294(boxRight - 1, boxTop, boxRight, boxBottom, borderColor);
            }
            i = end + 1;
        }
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (this.deleteProfileConfirmOpen) {
            this.recalcDeleteConfirmLayout();
            if (button == 0 || button == 0) {
                if (MixThePacksScreen.isMouseInside(mouseX, mouseY, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom)) {
                    this.confirmDeleteProfile();
                    return true;
                }
                if (MixThePacksScreen.isMouseInside(mouseX, mouseY, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom)) {
                    this.cancelDeleteProfileConfirm();
                    return true;
                }
                if (!MixThePacksScreen.isMouseInside(mouseX, mouseY, this.deleteModalLeft, this.deleteModalTop, this.deleteModalRight, this.deleteModalBottom)) {
                    this.cancelDeleteProfileConfirm();
                    return true;
                }
            }
            return true;
        }
        if (button == 0 && this.isMouseOverList(mouseX, mouseY) && this.isMouseOverScrollBar(mouseX, mouseY)) {
            this.updateScrollFromMouse(mouseY);
            return true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.deleteProfileConfirmOpen) {
            return true;
        }
        if (verticalAmount == 0.0) {
            return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (this.isMouseOverProfileList(mouseX, mouseY)) {
            int maxListScroll = Math.max(0, this.profiles.size() - this.profileVisibleRows);
            this.profileListScroll = class_3532.method_15340((int)(this.profileListScroll - (int)Math.signum(verticalAmount)), (int)0, (int)maxListScroll);
            this.updateProfileRowButtons();
            return true;
        }
        if (this.isMouseOverProfilePanel(mouseX, mouseY) && this.profilePanelMaxScroll > 0) {
            int delta = (int)Math.round(verticalAmount * 16.0);
            this.setProfilePanelScroll(class_3532.method_15340((int)(this.profilePanelScroll - delta), (int)0, (int)this.profilePanelMaxScroll));
            return true;
        }
        if (!this.isMouseOverList(mouseX, mouseY) || this.maxScroll <= 0) {
            return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int delta = (int)Math.round(verticalAmount * 24.0);
        this.scrollOffset = class_3532.method_15340((int)(this.scrollOffset - delta), (int)0, (int)this.maxScroll);
        this.updateRowVisibility();
        return true;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.deleteProfileConfirmOpen) {
            if (keyCode == 256) {
                this.cancelDeleteProfileConfirm();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                this.confirmDeleteProfile();
                return true;
            }
            return true;
        }
        if (keyCode == 299) {
            this.method_25419();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public void method_25419() {
        if (this.field_22787 == null) {
            return;
        }
        if (this.deleteProfileConfirmOpen) {
            this.cancelDeleteProfileConfirm();
            return;
        }
        if (!this.hasUnsavedProfileChanges) {
            this.field_22787.method_1507(this.parent);
            return;
        }
        this.field_22787.method_1507((class_437)new class_410(confirmed -> {
            if (this.field_22787 == null) {
                return;
            }
            if (!confirmed) {
                this.field_22787.method_1507((class_437)this);
                return;
            }
            this.initializeStagedSelectionsFromCurrent();
            this.hasUnsavedProfileChanges = false;
            this.field_22787.method_1507(this.parent);
        }, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.title"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.message"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.ignore_exit"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.cancel")));
    }

    private void recalcScroll() {
        int totalHeight = this.rows.isEmpty() ? 0 : this.rows.size() * 24 - 4;
        int viewport = Math.max(0, this.listBottom - this.listTop);
        this.maxScroll = Math.max(0, totalHeight - viewport);
        this.scrollOffset = class_3532.method_15340((int)this.scrollOffset, (int)0, (int)this.maxScroll);
    }

    private void rebuildRows() {
        if (!this.rows.isEmpty()) {
            for (class_339 widget : this.rowWidgets) {
                this.method_37066((class_364)widget);
            }
        }
        this.rows.clear();
        this.rowBaseY.clear();
        this.rowWidgets.clear();
        List<String> sourceSlotIds = PunchyAnimationManager.getMixPackAnimationNames();
        ArrayList<String> slotIds = sourceSlotIds == null ? new ArrayList<String>() : new ArrayList<String>(sourceSlotIds);
        String emptyHandSlotId = "kind:empty_hand";
        if (slotIds.remove(emptyHandSlotId)) {
            slotIds.add(0, emptyHandSlotId);
        }
        for (String slotId : slotIds) {
            if (!this.matchesFilter(slotId)) continue;
            this.addMainRow(slotId);
        }
        this.recalcScroll();
        this.updateRowVisibility();
    }

    private void addMainRow(String slotId) {
        List<PunchyAnimationManager.PackOption> options = PunchyAnimationManager.getMixPackOptions(slotId);
        String selectedId = this.stagedMainSelection(slotId);
        int index = MixThePacksScreen.selectedIndex(options, selectedId);
        int y = this.listTop + this.rows.size() * 24;
        String friendlyName = PunchyAnimationManager.getMixPackEntryLabel(slotId);
        if (friendlyName == null || friendlyName.isBlank()) {
            friendlyName = slotId;
        }
        class_4185 nameButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)friendlyName), button -> {}).method_46433(this.listLeft, y).method_46437(this.rowWidth, 20).method_46431());
        class_4185 selectorButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)MixThePacksScreen.optionLabel(options, index)), button -> {
            int nextIndex;
            String current = this.stagedMainSelection(slotId);
            int currentIndex = MixThePacksScreen.selectedIndex(options, current);
            int n = nextIndex = options.isEmpty() ? -1 : (currentIndex + 1) % options.size();
            if (nextIndex >= 0) {
                PunchyAnimationManager.PackOption next = (PunchyAnimationManager.PackOption)options.get(nextIndex);
                this.stageMainSelection(slotId, next.optionId());
                this.hasUnsavedProfileChanges = true;
                this.rebuildRows();
            }
        }).method_46433(this.listLeft + this.rowWidth + 6, y).method_46437(this.selectorWidth, 20).method_46431());
        this.rows.add(new RowEntry(slotId, null, false, nameButton, selectorButton));
        this.rowBaseY.add(y);
        this.rowWidgets.add((class_339)nameButton);
        this.rowWidgets.add((class_339)selectorButton);
    }

    private boolean matchesFilter(String slotId) {
        if (this.filterText == null || this.filterText.isBlank()) {
            return true;
        }
        String query = this.filterText.toLowerCase().trim();
        String slotKey = slotId == null ? "" : slotId.toLowerCase();
        String label = PunchyAnimationManager.getMixPackEntryLabel(slotId);
        String normalizedLabel = label == null ? "" : label.toLowerCase();
        return slotKey.contains(query) || normalizedLabel.contains(query);
    }

    private void drawRowIcons(class_332 graphics) {
        for (RowEntry row : this.rows) {
            class_1799 stack;
            if (row == null || row.child() || !row.nameButton().field_22764 || (stack = this.resolveDisplayStack(row.slotId())) == null || stack.method_7960()) continue;
            class_4185 button = row.nameButton();
            int iconX = button.method_46426() + 5;
            int iconY = button.method_46427() + Math.max(0, (button.method_25364() - 16) / 2);
            graphics.method_51427(stack, iconX, iconY);
        }
    }

    private class_1799 resolveDisplayStack(String slotId) {
        class_1792 item;
        class_2960 id;
        if (slotId == null || slotId.isBlank()) {
            return class_1799.field_8037;
        }
        if (slotId.startsWith("specific:") && (id = class_2960.method_12829((String)slotId.substring("specific:".length()))) != null && (item = class_7923.field_41178.method_17966(id).orElse(class_1802.field_8162)) != class_1802.field_8162) {
            return new class_1799((class_1935)item);
        }
        if (slotId.startsWith("kind:")) {
            String rawKind = slotId.substring("kind:".length()).trim();
            try {
                VanillaFirstPersonItemLayer.ToolKind kind = VanillaFirstPersonItemLayer.ToolKind.valueOf(rawKind.toUpperCase(Locale.ROOT));
                return this.representativeStack(kind);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return class_1799.field_8037;
    }

    private class_1799 representativeStack(VanillaFirstPersonItemLayer.ToolKind kind) {
        class_1792 item = switch (kind) {
            case VanillaFirstPersonItemLayer.ToolKind.EMPTY_HAND -> class_1802.field_8162;
            case VanillaFirstPersonItemLayer.ToolKind.SWORD -> class_1802.field_8802;
            case VanillaFirstPersonItemLayer.ToolKind.AXE -> class_1802.field_8556;
            case VanillaFirstPersonItemLayer.ToolKind.PICKAXE -> class_1802.field_8377;
            case VanillaFirstPersonItemLayer.ToolKind.SHOVEL -> class_1802.field_8250;
            case VanillaFirstPersonItemLayer.ToolKind.HOE -> class_1802.field_8527;
            case VanillaFirstPersonItemLayer.ToolKind.SPEAR -> this.resolveSpearDisplayItem();
            case VanillaFirstPersonItemLayer.ToolKind.TRIDENT -> class_1802.field_8547;
            case VanillaFirstPersonItemLayer.ToolKind.FOOD -> class_1802.field_8176;
            case VanillaFirstPersonItemLayer.ToolKind.LANTERN -> class_1802.field_16539;
            case VanillaFirstPersonItemLayer.ToolKind.CREATURE_BUCKET -> class_1802.field_28354;
            case VanillaFirstPersonItemLayer.ToolKind.BUCKET -> class_1802.field_8705;
            case VanillaFirstPersonItemLayer.ToolKind.MINECART -> class_1802.field_8045;
            case VanillaFirstPersonItemLayer.ToolKind.BOAT -> class_1802.field_8533;
            case VanillaFirstPersonItemLayer.ToolKind.BOW -> class_1802.field_8102;
            case VanillaFirstPersonItemLayer.ToolKind.ARROW -> class_1802.field_8107;
            case VanillaFirstPersonItemLayer.ToolKind.CROSSBOW -> class_1802.field_8399;
            case VanillaFirstPersonItemLayer.ToolKind.SHIELD -> class_1802.field_8255;
            case VanillaFirstPersonItemLayer.ToolKind.FISHING_ROD -> class_1802.field_8378;
            case VanillaFirstPersonItemLayer.ToolKind.SPYGLASS -> class_1802.field_27070;
            case VanillaFirstPersonItemLayer.ToolKind.FLINT -> class_1802.field_8884;
            case VanillaFirstPersonItemLayer.ToolKind.MACE -> class_1802.field_49814;
            case VanillaFirstPersonItemLayer.ToolKind.TORCH -> class_1802.field_8810;
            case VanillaFirstPersonItemLayer.ToolKind.CHEST -> class_1802.field_8106;
            case VanillaFirstPersonItemLayer.ToolKind.BUTTON_PLATE -> class_1802.field_8667;
            case VanillaFirstPersonItemLayer.ToolKind.TRAPDOOR -> class_1802.field_8376;
            case VanillaFirstPersonItemLayer.ToolKind.CARPET -> class_1802.field_8482;
            case VanillaFirstPersonItemLayer.ToolKind.BED -> class_1802.field_8789;
            case VanillaFirstPersonItemLayer.ToolKind.FULL_BLOCK, VanillaFirstPersonItemLayer.ToolKind.BLOCK, VanillaFirstPersonItemLayer.ToolKind.BUILDING_BLOCKS -> class_1802.field_20391;
            case VanillaFirstPersonItemLayer.ToolKind.COLORED_BLOCKS -> class_1802.field_19058;
            case VanillaFirstPersonItemLayer.ToolKind.NATURAL_BLOCKS -> class_1802.field_8270;
            case VanillaFirstPersonItemLayer.ToolKind.FUNCTIONAL_BLOCKS -> class_1802.field_8465;
            case VanillaFirstPersonItemLayer.ToolKind.REDSTONE -> class_1802.field_8725;
            default -> class_1802.field_8407;
        };
        return item == class_1802.field_8162 ? class_1799.field_8037 : new class_1799((class_1935)item);
    }

    private class_1792 resolveSpearDisplayItem() {
        class_1792 spear;
        class_1792 diamondSpear;
        class_2960 diamondSpearId = class_2960.method_12829((String)"minecraft:diamond_spear");
        if (diamondSpearId != null && (diamondSpear = class_7923.field_41178.method_17966(diamondSpearId).orElse(class_1802.field_8162)) != class_1802.field_8162) {
            return diamondSpear;
        }
        class_2960 spearId = class_2960.method_12829((String)"minecraft:spear");
        if (spearId != null && (spear = class_7923.field_41178.method_17966(spearId).orElse(class_1802.field_8162)) != class_1802.field_8162) {
            return spear;
        }
        return class_1802.field_8547;
    }

    private void addTypeRows(String slotId) {
        List<String> types = PunchyAnimationManager.getMixPackExpandableEntryTypes(slotId);
        for (String typeId : types) {
            List<PunchyAnimationManager.PackOption> typeOptions = this.buildTypeOptionsForDisplay(slotId, typeId);
            String selectedType = this.stagedTypeSelection(slotId, typeId);
            int typeIndex = MixThePacksScreen.selectedIndex(typeOptions, selectedType);
            int childY = this.listTop + this.rows.size() * 24;
            int childInset = Math.min(10, Math.max(4, this.rowWidth / 8));
            int childNameX = this.listLeft + childInset;
            int childNameWidth = Math.max(80, this.rowWidth - childInset * 2);
            int childSelectorX = this.listLeft + this.rowWidth + 6 - childInset;
            String typeLabel = "  - " + MixThePacksScreen.prettyTypeLabel(typeId);
            class_4185 childNameButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)typeLabel), button -> {}).method_46433(childNameX, childY).method_46437(childNameWidth, 20).method_46431());
            childNameButton.field_22763 = false;
            class_4185 childSelectorButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)MixThePacksScreen.optionLabel(typeOptions, typeIndex)), button -> {
                int nextTypeIndex;
                String currentType = this.stagedTypeSelection(slotId, typeId);
                int currentTypeIndex = MixThePacksScreen.selectedIndex(typeOptions, currentType);
                int n = nextTypeIndex = typeOptions.isEmpty() ? -1 : (currentTypeIndex + 1) % typeOptions.size();
                if (nextTypeIndex >= 0) {
                    PunchyAnimationManager.PackOption next = (PunchyAnimationManager.PackOption)typeOptions.get(nextTypeIndex);
                    this.stageTypeSelection(slotId, typeId, next.optionId());
                    this.hasUnsavedProfileChanges = true;
                    this.rebuildRows();
                }
            }).method_46433(childSelectorX, childY).method_46437(this.selectorWidth, 20).method_46431());
            this.rows.add(new RowEntry(slotId, typeId, true, childNameButton, childSelectorButton));
            this.rowBaseY.add(childY);
            this.rowWidgets.add((class_339)childNameButton);
            this.rowWidgets.add((class_339)childSelectorButton);
        }
    }

    private void updateRowVisibility() {
        for (int i = 0; i < this.rows.size(); ++i) {
            boolean visible;
            RowEntry row = this.rows.get(i);
            int base = this.rowBaseY.get(i);
            int y = base - this.scrollOffset;
            row.nameButton().method_46419(y);
            row.selectorButton().method_46419(y);
            row.nameButton().field_22764 = visible = y + 20 > this.listTop && y < this.listBottom;
            row.nameButton().field_22763 = visible && !row.child();
            row.selectorButton().field_22764 = visible;
            row.selectorButton().field_22763 = visible;
        }
    }

    private void drawScrollbar(class_332 graphics) {
        if (this.maxScroll <= 0) {
            return;
        }
        int barWidth = 4;
        int barX = this.listRight - 4;
        int barTop = this.listTop;
        int barBottom = this.listBottom;
        graphics.method_25294(barX, barTop, barX + barWidth, barBottom, 1427118368);
        int trackHeight = Math.max(1, barBottom - barTop);
        int totalHeight = this.rows.size() * 24 - 4;
        int thumbHeight = Math.max(10, Math.round((float)trackHeight * ((float)(this.listBottom - this.listTop) / (float)totalHeight)));
        int thumbMax = trackHeight - thumbHeight;
        int thumbOffset = this.maxScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.scrollOffset / (float)this.maxScroll));
        int thumbTop = barTop + thumbOffset;
        graphics.method_25294(barX, thumbTop, barX + barWidth, Math.min(barBottom, thumbTop + thumbHeight), -2827553);
    }

    private boolean isMouseOverList(double x, double y) {
        return x >= (double)this.listLeft && x <= (double)this.listRight && y >= (double)this.listTop && y <= (double)this.listBottom;
    }

    private boolean isMouseOverScrollBar(double x, double y) {
        int barX = this.listRight - 4;
        return x >= (double)barX && x <= (double)this.listRight && y >= (double)this.listTop && y <= (double)this.listBottom;
    }

    private boolean isMouseOverProfileList(double x, double y) {
        return x >= (double)this.profileListX && x <= (double)(this.profileListX + this.profileListW) && y >= (double)this.profileListY && y <= (double)(this.profileListY + this.profileListH);
    }

    private boolean isMouseOverProfilePanel(double x, double y) {
        int top = this.profilesTitleY - 2;
        int bottom = this.doneButton.method_46427() - 6;
        return x >= (double)this.profilePanelX && x <= (double)(this.profilePanelX + this.profilePanelW) && y >= (double)top && y <= (double)bottom;
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackHeight = this.listBottom - this.listTop;
        if (trackHeight <= 0) {
            return;
        }
        float ratio = (float)((mouseY - (double)this.listTop) / (double)trackHeight);
        ratio = class_3532.method_15363((float)ratio, (float)0.0f, (float)1.0f);
        this.scrollOffset = (int)(ratio * (float)this.maxScroll);
        this.updateRowVisibility();
    }

    private void recalcProfilePanelScroll() {
        this.profilePanelScroll = 0;
        this.profilePanelMaxScroll = 0;
    }

    private void openDeleteProfileConfirm() {
        String typedName;
        String name = this.selectedProfileName();
        if ((name == null || name.isBlank()) && this.profileNameBox != null && (typedName = this.profileNameBox.method_1882()) != null && !(typedName = typedName.trim()).isBlank() && this.profiles.contains(typedName)) {
            this.selectedProfile = this.profiles.indexOf(typedName);
            this.ensureSelectedVisibleInProfileList();
            this.updateProfileRowButtons();
            name = typedName;
        }
        if (name == null || name.isBlank()) {
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.profile_delete_failed").getString();
            return;
        }
        this.pendingDeleteProfileName = name;
        this.recalcDeleteConfirmLayout();
        this.deleteProfileConfirmOpen = true;
    }

    private void cancelDeleteProfileConfirm() {
        this.deleteProfileConfirmOpen = false;
        this.pendingDeleteProfileName = null;
    }

    private void confirmDeleteProfile() {
        boolean ok;
        String name = this.pendingDeleteProfileName;
        this.cancelDeleteProfileConfirm();
        boolean bl = ok = name != null && !name.isBlank() && MixProfileManager.deleteProfile(name);
        if (ok) {
            this.applyResetToResourcePackOrderAndUnloadProfile();
        }
        this.statusMessage = ok ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_deleted", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_delete_failed").getString();
        this.refreshProfiles(true);
    }

    private void applyResetToResourcePackOrderAndUnloadProfile() {
        PunchyAnimationManager.resetMixPacksToResourcePackOrder();
        MixProfileManager.unloadActiveProfileToDefault();
        this.stagedPackSelections.clear();
        this.stagedPackTypeSelections.clear();
        this.setPreviewToResourcePackOrder();
        this.hasUnsavedProfileChanges = false;
        this.mixSessionBaseline = PunchyConfig.snapshot();
        this.initializeStagedSelectionsFromCurrent();
        this.refreshProfiles(true);
        this.rebuildRows();
    }

    private void recalcDeleteConfirmLayout() {
        int modalWidth = Math.min(380, Math.max(260, this.containerRight - this.containerLeft - 48));
        int modalHeight = 112;
        this.deleteModalLeft = this.containerLeft + (this.containerRight - this.containerLeft - modalWidth) / 2;
        this.deleteModalTop = this.containerTop + (this.containerBottom - this.containerTop - modalHeight) / 2;
        this.deleteModalRight = this.deleteModalLeft + modalWidth;
        this.deleteModalBottom = this.deleteModalTop + modalHeight;
        int buttonHeight = 20;
        int buttonGap = 8;
        int buttonWidth = (modalWidth - 32 - buttonGap) / 2;
        this.deleteCancelButtonLeft = this.deleteModalLeft + 16;
        this.deleteCancelButtonTop = this.deleteModalBottom - 30;
        this.deleteCancelButtonRight = this.deleteCancelButtonLeft + buttonWidth;
        this.deleteCancelButtonBottom = this.deleteCancelButtonTop + buttonHeight;
        this.deleteConfirmButtonLeft = this.deleteCancelButtonRight + buttonGap;
        this.deleteConfirmButtonTop = this.deleteCancelButtonTop;
        this.deleteConfirmButtonRight = this.deleteConfirmButtonLeft + buttonWidth;
        this.deleteConfirmButtonBottom = this.deleteConfirmButtonTop + buttonHeight;
    }

    private void drawDeleteProfileConfirmOverlay(class_332 graphics, int mouseX, int mouseY) {
        if (!this.deleteProfileConfirmOpen) {
            return;
        }
        graphics.method_51448().method_22903();
        graphics.method_51448().method_46416(0.0f, 0.0f, 500.0f);
        graphics.method_25294(0, 0, this.field_22789, this.field_22790, -805306368);
        graphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalRight, this.deleteModalBottom, -15657446);
        int border = -11641751;
        graphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalRight, this.deleteModalTop + 1, border);
        graphics.method_25294(this.deleteModalLeft, this.deleteModalBottom - 1, this.deleteModalRight, this.deleteModalBottom, border);
        graphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalLeft + 1, this.deleteModalBottom, border);
        graphics.method_25294(this.deleteModalRight - 1, this.deleteModalTop, this.deleteModalRight, this.deleteModalBottom, border);
        class_2561 title = MixThePacksScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.title", "Voc\u00ea realmente quer fazer isso?", new Object[0]);
        String targetName = this.pendingDeleteProfileName == null ? "-" : this.pendingDeleteProfileName;
        class_2561 message = MixThePacksScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.message", "Delete profile \"" + targetName + "\"?", targetName);
        String titleText = title.getString();
        String messageText = message.getString();
        int titleX = this.deleteModalLeft + (this.deleteModalRight - this.deleteModalLeft - this.field_22793.method_1727(titleText)) / 2;
        int messageX = this.deleteModalLeft + (this.deleteModalRight - this.deleteModalLeft - this.field_22793.method_1727(messageText)) / 2;
        graphics.method_25303(this.field_22793, titleText, titleX, this.deleteModalTop + 12, -1);
        graphics.method_25303(this.field_22793, messageText, messageX, this.deleteModalTop + 34, -3747624);
        boolean hoverCancel = MixThePacksScreen.isMouseInside(mouseX, mouseY, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom);
        boolean hoverConfirm = MixThePacksScreen.isMouseInside(mouseX, mouseY, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom);
        this.drawModalButton(graphics, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom, hoverCancel, false, MixThePacksScreen.nonBlankTranslatable("gui.cancel", "Cancel", new Object[0]));
        this.drawModalButton(graphics, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom, hoverConfirm, true, MixThePacksScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.confirm", "Delete", new Object[0]));
        graphics.method_51448().method_22909();
    }

    private void drawModalButton(class_332 graphics, int left, int top, int right, int bottom, boolean hovered, boolean destructive, class_2561 label) {
        int fill = destructive ? (hovered ? -7459282 : -9690590) : (hovered ? -12958126 : -13682107);
        int border = destructive ? -3048067 : -7429710;
        graphics.method_25294(left, top, right, bottom, fill);
        graphics.method_25294(left, top, right, top + 1, border);
        graphics.method_25294(left, bottom - 1, right, bottom, border);
        graphics.method_25294(left, top, left + 1, bottom, border);
        graphics.method_25294(right - 1, top, right, bottom, border);
        String labelText = label.getString();
        int textX = left + (right - left - this.field_22793.method_1727(labelText)) / 2;
        int textY = top + (bottom - top - 8) / 2;
        graphics.method_25303(this.field_22793, labelText, textX, textY, -1);
    }

    private static boolean isMouseInside(double mouseX, double mouseY, int left, int top, int right, int bottom) {
        return mouseX >= (double)left && mouseX <= (double)right && mouseY >= (double)top && mouseY <= (double)bottom;
    }

    private static class_2561 nonBlankTranslatable(String key, String fallback, Object ... args) {
        class_5250 translated = class_2561.method_43469((String)key, (Object[])args);
        String resolved = translated.getString();
        if (resolved == null || resolved.isBlank() || resolved.equals(key)) {
            return class_2561.method_43470((String)fallback);
        }
        return translated;
    }

    private void setProfilePanelScroll(int newValue) {
        int clamped = class_3532.method_15340((int)newValue, (int)0, (int)this.profilePanelMaxScroll);
        if (clamped == this.profilePanelScroll) {
            return;
        }
        int delta = clamped - this.profilePanelScroll;
        this.profilePanelScroll = clamped;
        int shift = -delta;
        this.profilesTitleY += shift;
        this.profileListY += shift;
        this.profileNameBox.method_46419(this.profileNameBox.method_46427() + shift);
        this.saveProfileButton.method_46419(this.saveProfileButton.method_46427() + shift);
        this.loadProfileButton.method_46419(this.loadProfileButton.method_46427() + shift);
        this.deleteProfileButton.method_46419(this.deleteProfileButton.method_46427() + shift);
        this.openProfilesButton.method_46419(this.openProfilesButton.method_46427() + shift);
        for (class_4185 rowButton : this.profileRowButtons) {
            rowButton.method_46419(rowButton.method_46427() + shift);
        }
    }

    private void refreshProfiles(boolean clampSelection) {
        this.profiles = MixProfileManager.listProfiles();
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)Math.max(0, this.profiles.size() - this.profileVisibleRows));
        if (this.profiles.isEmpty()) {
            this.selectedProfile = -1;
            if (this.profileNameBox != null) {
                this.profileNameBox.method_1852("");
            }
            this.updateProfileRowButtons();
            return;
        }
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size() || clampSelection) {
            String active = MixProfileManager.getActiveProfileName();
            if (active != null) {
                int activeIndex = this.profiles.indexOf(active);
                if (activeIndex >= 0) {
                    this.selectedProfile = activeIndex;
                }
            } else if (clampSelection) {
                this.selectedProfile = -1;
            }
            if (this.selectedProfile < 0 && !clampSelection) {
                this.selectedProfile = 0;
            } else if (this.selectedProfile >= 0) {
                this.selectedProfile = class_3532.method_15340((int)this.selectedProfile, (int)0, (int)(this.profiles.size() - 1));
            }
        }
        this.ensureSelectedVisibleInProfileList();
        String selected = this.selectedProfileName();
        if (this.profileNameBox != null) {
            if (selected != null && !selected.isBlank()) {
                this.profileNameBox.method_1852(selected);
            } else {
                this.profileNameBox.method_1852("");
            }
        }
        this.updateProfileRowButtons();
    }

    private void ensureSelectedVisibleInProfileList() {
        if (this.selectedProfile < 0) {
            return;
        }
        int maxScroll = Math.max(0, this.profiles.size() - this.profileVisibleRows);
        if (this.selectedProfile < this.profileListScroll) {
            this.profileListScroll = this.selectedProfile;
        } else if (this.selectedProfile >= this.profileListScroll + this.profileVisibleRows) {
            this.profileListScroll = this.selectedProfile - this.profileVisibleRows + 1;
        }
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)maxScroll);
    }

    private void updateProfileRowButtons() {
        for (int i = 0; i < this.profileRowButtons.size(); ++i) {
            class_4185 rowButton = this.profileRowButtons.get(i);
            int index = this.profileListScroll + i;
            if (index >= 0 && index < this.profiles.size()) {
                String name = this.profiles.get(index);
                boolean selected = index == this.selectedProfile;
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
        if (selectedName != null && this.profileNameBox != null) {
            this.profileNameBox.method_1852(selectedName);
        }
        this.ensureSelectedVisibleInProfileList();
        this.updateProfileRowButtons();
    }

    private String selectedProfileName() {
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size()) {
            return null;
        }
        return this.profiles.get(this.selectedProfile);
    }

    private boolean openProfilesFolder() {
        try {
            Path dir = MixProfileManager.profilesDirectoryPath();
            class_156.method_668().method_60932(dir);
            return true;
        }
        catch (Exception ignored) {
            try {
                Path dir = MixProfileManager.profilesDirectoryPath();
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(dir.toFile());
                    return true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return false;
        }
    }

    private void initializeStagedSelectionsFromCurrent() {
        PunchyConfig.Data snapshot = PunchyConfig.snapshot();
        this.stagedPackSelections.clear();
        this.stagedPackTypeSelections.clear();
        this.previewPackSelections.clear();
        this.previewPackTypeSelections.clear();
        if (snapshot == null) {
            return;
        }
        if (snapshot.animationPackSelections != null) {
            this.stagedPackSelections.putAll(snapshot.animationPackSelections);
        }
        if (snapshot.animationPackTypeSelections != null) {
            this.stagedPackTypeSelections.putAll(snapshot.animationPackTypeSelections);
        }
    }

    private String stagedMainSelection(String slotId) {
        if (this.stagedPackSelections.containsKey(slotId)) {
            String selected = this.stagedPackSelections.get(slotId);
            if (selected == null || selected.isBlank()) {
                return "punchy_default";
            }
            return selected;
        }
        String previewSelected = this.previewPackSelections.get(slotId);
        if (previewSelected != null && !previewSelected.isBlank()) {
            return previewSelected;
        }
        String selected = this.stagedPackSelections.get(slotId);
        if (selected == null || selected.isBlank()) {
            String effective = PunchyAnimationManager.getSelectedMixPackOption(slotId);
            if (effective == null || effective.isBlank()) {
                return "punchy_default";
            }
            return effective;
        }
        return selected;
    }

    private String stagedTypeSelection(String slotId, String typeId) {
        String key = slotId + "|" + (typeId == null ? "" : typeId.trim().toLowerCase(Locale.ROOT));
        String selected = this.stagedPackTypeSelections.get(key);
        if (selected != null && !selected.isBlank()) {
            return selected;
        }
        String previewSelected = this.previewPackTypeSelections.get(key);
        if (previewSelected != null && !previewSelected.isBlank()) {
            return previewSelected;
        }
        String effectiveType = PunchyAnimationManager.getSelectedMixPackTypeOption(slotId, typeId);
        if (effectiveType != null && !effectiveType.isBlank()) {
            return effectiveType;
        }
        return this.stagedMainSelection(slotId);
    }

    private void stageMainSelection(String slotId, String optionId) {
        if (slotId == null || slotId.isBlank()) {
            return;
        }
        String keyPrefix = slotId + "|";
        this.stagedPackTypeSelections.keySet().removeIf(key -> key != null && key.startsWith(keyPrefix));
        this.previewPackSelections.remove(slotId);
        this.previewPackTypeSelections.keySet().removeIf(key -> key != null && key.startsWith(keyPrefix));
        if (optionId == null || optionId.isBlank()) {
            this.stagedPackSelections.remove(slotId);
            return;
        }
        String normalized = optionId.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            this.stagedPackSelections.remove(slotId);
            return;
        }
        this.stagedPackSelections.put(slotId, normalized);
    }

    private void stageTypeSelection(String slotId, String typeId, String optionId) {
        if (slotId == null || slotId.isBlank() || typeId == null || typeId.isBlank()) {
            return;
        }
        String normalizedType = typeId.trim().toLowerCase(Locale.ROOT);
        String key = slotId + "|" + normalizedType;
        this.previewPackTypeSelections.remove(key);
        String normalizedOption = optionId == null ? "" : optionId.trim().toLowerCase(Locale.ROOT);
        String main = this.stagedMainSelection(slotId);
        if (normalizedOption.isBlank() || "punchy_default".equals(normalizedOption) || normalizedOption.equals(main)) {
            this.stagedPackTypeSelections.remove(key);
            return;
        }
        this.stagedPackTypeSelections.put(key, normalizedOption);
    }

    private void setPreviewToResourcePackOrder() {
        this.previewPackSelections.clear();
        this.previewPackTypeSelections.clear();
        List<String> slots = PunchyAnimationManager.getMixPackAnimationNames();
        if (slots == null || slots.isEmpty()) {
            return;
        }
        for (String slotId : slots) {
            if (slotId == null || slotId.isBlank()) continue;
            String mainOption = MixThePacksScreen.resolveResourceOrderMainOption(slotId);
            this.previewPackSelections.put(slotId, mainOption);
            List<String> types = PunchyAnimationManager.getMixPackEntryTypes(slotId);
            if (types == null || types.isEmpty()) continue;
            for (String typeId : types) {
                if (typeId == null || typeId.isBlank()) continue;
                String key = slotId + "|" + typeId.trim().toLowerCase(Locale.ROOT);
                String typeOption = MixThePacksScreen.resolveResourceOrderTypeOption(slotId, typeId, mainOption);
                if (typeOption == null || typeOption.isBlank()) continue;
                this.previewPackTypeSelections.put(key, typeOption);
            }
        }
    }

    private static String resolveResourceOrderMainOption(String slotId) {
        List<PunchyAnimationManager.PackOption> options = PunchyAnimationManager.getMixPackOptions(slotId);
        if (options == null || options.isEmpty()) {
            return "punchy_default";
        }
        for (PunchyAnimationManager.PackOption option : options) {
            if (option == null || option.optionId() == null || option.optionId().isBlank() || "punchy_default".equals(option.optionId())) continue;
            return option.optionId();
        }
        return "punchy_default";
    }

    private static String resolveResourceOrderTypeOption(String slotId, String typeId, String mainOption) {
        List<String> options = PunchyTuningConfig.getMixPackSlotEntryTypeOptionIds(slotId, typeId);
        if (options == null || options.isEmpty()) {
            return mainOption == null || mainOption.isBlank() ? "punchy_default" : mainOption;
        }
        for (String option : options) {
            if (option == null || option.isBlank() || "punchy_default".equals(option)) continue;
            return option;
        }
        if (mainOption != null && !mainOption.isBlank()) {
            return mainOption;
        }
        return "punchy_default";
    }

    private List<PunchyAnimationManager.PackOption> buildTypeOptionsForDisplay(String slotId, String typeId) {
        List<String> optionIds = PunchyTuningConfig.getMixPackSlotEntryTypeOptionIds(slotId, typeId);
        ArrayList<String> normalized = new ArrayList<String>(optionIds == null ? List.of() : optionIds);
        String main = this.stagedMainSelection(slotId);
        if (main != null && !main.isBlank() && !normalized.contains(main)) {
            normalized.add(0, main);
        }
        if (normalized.isEmpty()) {
            return List.of();
        }
        ArrayList<PunchyAnimationManager.PackOption> result = new ArrayList<PunchyAnimationManager.PackOption>(normalized.size());
        for (String optionId : normalized) {
            if ("punchy_default".equals(optionId)) continue;
            result.add(new PunchyAnimationManager.PackOption(optionId, this.displayPackOptionLabel(optionId)));
        }
        return result;
    }

    private String displayPackOptionLabel(String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return "Unknown";
        }
        if ("pack:punchy:punchy".equals(optionId)) {
            return "\u00a7bPunchy! \u00a7eDefault";
        }
        String cleaned = optionId;
        if (cleaned.startsWith("pack:")) {
            cleaned = cleaned.substring("pack:".length());
        }
        if (cleaned.startsWith("file/")) {
            cleaned = cleaned.substring("file/".length());
        }
        if (cleaned.startsWith("file\\")) {
            cleaned = cleaned.substring("file\\".length());
        }
        if (cleaned.endsWith(".zip")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        if (cleaned.endsWith(".jar")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        if ((cleaned = cleaned.replace('_', ' ').replace('-', ' ').trim()).isBlank()) {
            return optionId;
        }
        return cleaned;
    }

    private static int selectedIndex(List<PunchyAnimationManager.PackOption> options, String selected) {
        if (options == null || options.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < options.size(); ++i) {
            if (!options.get(i).optionId().equals(selected)) continue;
            return i;
        }
        return 0;
    }

    private static String optionLabel(List<PunchyAnimationManager.PackOption> options, int selectedIndex) {
        if (options == null || options.isEmpty() || selectedIndex < 0 || selectedIndex >= options.size()) {
            return "-";
        }
        return options.get(selectedIndex).label();
    }

    private static String prettyTypeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "Type";
        }
        String value = type.trim().replace('_', ' ');
        value = value.replaceAll("([a-z])([A-Z])", "$1 $2");
        String[] parts = value.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() <= 1) continue;
            out.append(part.substring(1));
        }
        return out.toString();
    }

    private record RowEntry(String slotId, String entryType, boolean child, class_4185 nameButton, class_4185 selectorButton) {
    }
}


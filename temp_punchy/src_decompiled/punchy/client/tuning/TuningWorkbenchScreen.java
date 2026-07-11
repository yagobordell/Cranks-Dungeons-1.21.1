/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_156
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_339
 *  net.minecraft.class_342
 *  net.minecraft.class_3532
 *  net.minecraft.class_357
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_357;
import net.minecraft.class_364;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import punchy.client.render.FishingLineProxyTuning;
import punchy.client.render.ToolKindResolver;
import punchy.client.render.ToolTuning;
import punchy.client.renderer.layer.ArmToolTuning;
import punchy.client.renderer.layer.PlayerArmModelLayer;
import punchy.client.renderer.layer.VanillaFirstPersonItemLayer;
import punchy.client.state.SpearStateMachine;
import punchy.client.tuning.TuningProfileManager;
import punchy.compat.PunchyPuppiesCompat;
import punchy.config.PunchyTuningConfig;

public final class TuningWorkbenchScreen
extends class_437 {
    private Scope scope = Scope.SPECIFIC;
    private boolean leftHand = false;
    private VanillaFirstPersonItemLayer.ToolKind selectedKind = VanillaFirstPersonItemLayer.ToolKind.AXE;
    private class_4185 scopeButton;
    private class_4185 handButton;
    private class_4185 targetButton;
    private class_4185 kindButton;
    private class_4185 resetButton;
    private class_4185 resetCurrentButton;
    private class_4185 mirrorButton;
    private class_4185 reloadTuningButton;
    private class_4185 saveProfileButton;
    private class_4185 loadProfileButton;
    private class_4185 deleteProfileButton;
    private class_4185 importProfileButton;
    private class_4185 doneButton;
    private FloatSlider offX;
    private FloatSlider offY;
    private FloatSlider offZ;
    private FloatSlider rotX;
    private FloatSlider rotY;
    private FloatSlider rotZ;
    private FloatSlider scale;
    private FloatSlider pivotX;
    private FloatSlider pivotY;
    private FloatSlider pivotZ;
    private final List<FloatSlider> sliders = new ArrayList<FloatSlider>();
    private final List<class_339> uiWidgets = new ArrayList<class_339>();
    private FloatSlider activeSlider = null;
    private class_342 profileNameBox;
    private List<String> profiles = List.of();
    private int selectedProfile = -1;
    private String statusMessage = "";
    private boolean syncing = false;
    private boolean compactLayout = false;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int profilesTitleX = 0;
    private int profilesTitleY = 0;
    private int profileListX = 0;
    private int profileListY = 0;
    private int profileListW = 0;
    private int profileListH = 0;
    private int profileListScroll = 0;
    private boolean draggingMainScrollbar = false;
    private boolean draggingProfileScrollbar = false;
    private final List<class_4185> profileRowButtons = new ArrayList<class_4185>();
    private int containerLeft = 0;
    private int containerRight = 0;
    private int mainScrollX = 0;
    private int mainScrollTop = 0;
    private int mainScrollBottom = 0;
    private int profileScrollX = 0;
    private int profileScrollTop = 0;
    private int profileScrollBottom = 0;
    private int mainPanelLeft = 0;
    private int mainPanelTop = 0;
    private int mainPanelRight = 0;
    private int mainPanelBottom = 0;
    private int leftPanelLeft = 0;
    private int leftPanelTop = 0;
    private int leftPanelRight = 0;
    private int leftPanelBottom = 0;
    private int rightPanelLeft = 0;
    private int rightPanelTop = 0;
    private int rightPanelRight = 0;
    private int rightPanelBottom = 0;
    private EditorTarget editorTarget = EditorTarget.ITEM;
    private boolean hasUnsavedProfileChanges = false;
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
    private final class_437 parentScreen;

    public TuningWorkbenchScreen() {
        this(null);
    }

    public TuningWorkbenchScreen(class_437 parentScreen) {
        super((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.title"));
        this.parentScreen = parentScreen;
    }

    public static boolean shouldRenderPivotMarker() {
        class_437 class_4372;
        class_310 mc = class_310.method_1551();
        if (mc == null || !((class_4372 = mc.field_1755) instanceof TuningWorkbenchScreen)) {
            return false;
        }
        TuningWorkbenchScreen screen = (TuningWorkbenchScreen)class_4372;
        return true;
    }

    protected void method_25426() {
        this.method_37067();
        this.sliders.clear();
        this.uiWidgets.clear();
        this.profileRowButtons.clear();
        int sliderW = 200;
        int sliderH = 20;
        this.scopeButton = this.addUiWidget(class_4185.method_46430((class_2561)this.scopeLabel(), button -> {
            if (!this.isArmEditor()) {
                return;
            }
            this.scope = switch (this.scope.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Scope.SPECIFIC;
                case 1 -> Scope.GLOBAL;
            };
            this.refreshHeaderButtons();
            this.loadFromCurrentProfile();
            this.layoutWidgets();
        }).method_46433(0, 0).method_46437(110, 20).method_46431());
        this.handButton = this.addUiWidget(class_4185.method_46430((class_2561)this.handLabel(), button -> {
            this.leftHand = !this.leftHand;
            this.autoKindFromHeldItem();
            this.refreshHeaderButtons();
            this.loadFromCurrentProfile();
            this.layoutWidgets();
        }).method_46433(0, 0).method_46437(90, 20).method_46431());
        this.targetButton = this.addUiWidget(class_4185.method_46430((class_2561)this.targetLabel(), button -> {
            this.cycleEditorTarget();
            this.normalizeScopeForTarget();
            this.activeSlider = null;
            this.refreshHeaderButtons();
            this.loadFromCurrentProfile();
            this.layoutWidgets();
        }).method_46433(0, 0).method_46437(92, 20).method_46431());
        this.kindButton = this.addUiWidget(class_4185.method_46430((class_2561)this.kindLabel(), button -> {}).method_46433(0, 0).method_46437(160, 20).method_46431());
        this.kindButton.field_22763 = false;
        this.offX = this.addSlider(0, 0, sliderW, sliderH, "offX", -2.0f, 2.0f);
        this.offY = this.addSlider(0, 0, sliderW, sliderH, "offY", -2.0f, 2.0f);
        this.offZ = this.addSlider(0, 0, sliderW, sliderH, "offZ", -2.0f, 2.0f);
        this.rotX = this.addSlider(0, 0, sliderW, sliderH, "rotX", -180.0f, 180.0f);
        this.rotY = this.addSlider(0, 0, sliderW, sliderH, "rotY", -180.0f, 180.0f);
        this.rotZ = this.addSlider(0, 0, sliderW, sliderH, "rotZ", -180.0f, 180.0f);
        this.scale = this.addSlider(0, 0, sliderW, sliderH, "scale", 0.1f, 3.0f);
        this.pivotX = this.addSlider(0, 0, sliderW, sliderH, "pivotX", -1.0f, 1.0f);
        this.pivotY = this.addSlider(0, 0, sliderW, sliderH, "pivotY", -1.0f, 1.0f);
        this.pivotZ = this.addSlider(0, 0, sliderW, sliderH, "pivotZ", -1.0f, 1.0f);
        this.resetButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.reset_to_default"), button -> {
            boolean ok = TuningProfileManager.unloadActiveProfileToDefault();
            String string = this.statusMessage = ok ? class_2561.method_43471((String)"punchy.tuning.ui.status.reset_to_default_done").getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.reset_to_default_failed").getString();
            if (ok) {
                this.hasUnsavedProfileChanges = false;
            }
            this.refreshProfiles(true);
            this.loadFromCurrentProfile();
        }).method_46433(0, 0).method_46437(120, 20).method_46431());
        this.resetCurrentButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.reset_current"), button -> {
            this.resetCurrentProfile();
            this.loadFromCurrentProfile();
        }).method_46433(0, 0).method_46437(120, 20).method_46431());
        this.mirrorButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.mirror_hand"), button -> {
            this.mirrorToOtherHand();
            this.loadFromCurrentProfile();
        }).method_46433(0, 0).method_46437(120, 20).method_46431());
        this.reloadTuningButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.export_tuning_helper_dev"), button -> {
            boolean ok = PunchyTuningConfig.exportTuningHelperDevAndOpen();
            this.statusMessage = ok ? class_2561.method_43471((String)"punchy.tuning.ui.status.export_tuning_helper_dev_done").getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.export_tuning_helper_dev_failed").getString();
        }).method_46433(0, 0).method_46437(120, 20).method_46431());
        this.profileNameBox = new class_342(this.field_22793, 0, 0, 220, 20, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.profile_name"));
        this.profileNameBox.method_1880(64);
        this.addUiWidget(this.profileNameBox);
        this.saveProfileButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.save_profile"), button -> {
            boolean ok;
            String name = this.profileNameBox.method_1882();
            if (name == null || name.isBlank()) {
                name = this.defaultProfileName();
                this.profileNameBox.method_1852(name);
            }
            String string = this.statusMessage = (ok = TuningProfileManager.saveCurrentAs(name)) ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_saved", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_save_failed").getString();
            if (ok) {
                this.hasUnsavedProfileChanges = false;
            }
            this.refreshProfiles(true);
        }).method_46433(0, 0).method_46437(106, 20).method_46431());
        this.loadProfileButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.load_profile"), button -> {
            String name = this.selectedProfileName();
            boolean ok = name != null && TuningProfileManager.loadProfile(name);
            String string = this.statusMessage = ok ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_loaded", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_load_failed").getString();
            if (ok) {
                this.hasUnsavedProfileChanges = false;
            }
            this.loadFromCurrentProfile();
            this.refreshProfiles(false);
        }).method_46433(0, 0).method_46437(106, 20).method_46431());
        this.deleteProfileButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.delete_profile"), button -> this.openDeleteProfileConfirm()).method_46433(0, 0).method_46437(220, 20).method_46431());
        this.importProfileButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"punchy.tuning.ui.open_profiles_folder"), button -> {
            boolean ok = this.openProfilesFolder();
            this.statusMessage = ok ? class_2561.method_43471((String)"punchy.tuning.ui.status.profiles_folder_opened").getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profiles_folder_open_failed").getString();
        }).method_46433(0, 0).method_46437(220, 20).method_46431());
        this.doneButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.done"), button -> this.method_25419()).method_46433(0, 0).method_46437(220, 20).method_46431());
        int i = 0;
        while (i < 6) {
            int row = i++;
            class_4185 rowButton = this.addUiWidget(class_4185.method_46430((class_2561)class_2561.method_43473(), button -> this.selectProfileAtVisibleRow(row)).method_46433(0, 0).method_46437(220, 14).method_46431());
            this.profileRowButtons.add(rowButton);
        }
        this.normalizeScopeForTarget();
        this.hasUnsavedProfileChanges = false;
        this.refreshProfiles(true);
        this.autoKindFromHeldItem();
        this.refreshHeaderButtons();
        this.loadFromCurrentProfile();
        this.layoutWidgets();
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        if (this.hasUnsavedProfileChanges) {
            this.confirmDiscardUnsavedProfileChanges();
            return;
        }
        this.discardUnsavedChangesAndClose();
    }

    private void discardUnsavedChangesAndClose() {
        class_310 client = this.field_22787;
        if (client != null) {
            PunchyTuningConfig.reloadActiveProfileFast(client.method_1478());
            if (this.parentScreen != null) {
                client.method_1507(this.parentScreen);
                return;
            }
        }
        super.method_25419();
    }

    private void confirmDiscardUnsavedProfileChanges() {
        class_310 client = this.field_22787;
        if (client == null) {
            this.discardUnsavedChangesAndClose();
            return;
        }
        client.method_1507((class_437)new class_410(result -> {
            if (result) {
                this.hasUnsavedProfileChanges = false;
                this.discardUnsavedChangesAndClose();
            } else {
                client.method_1507((class_437)this);
            }
        }, (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.title"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.message"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.ignore_exit"), (class_2561)class_2561.method_43471((String)"punchy.tuning.ui.confirm_unsaved_profile.cancel")));
    }

    public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i;
        this.applyFocusVisibility();
        if (this.activeSlider != null) {
            this.activeSlider.drawStyled(guiGraphics, mouseX, mouseY);
            return;
        }
        guiGraphics.method_25294(0, 0, this.field_22789, this.field_22790, -2013265920);
        this.drawWorkbenchPanels(guiGraphics);
        ArrayList<class_4185> styledButtons = new ArrayList<class_4185>();
        ArrayList<class_2561> originalButtonMessages = new ArrayList<class_2561>();
        ArrayList<FloatSlider> styledSliders = new ArrayList<FloatSlider>();
        ArrayList<class_2561> originalSliderMessages = new ArrayList<class_2561>();
        for (class_339 widget : this.uiWidgets) {
            if (widget instanceof class_4185) {
                class_4185 button = (class_4185)widget;
                if (button.field_22764) {
                    styledButtons.add(button);
                    originalButtonMessages.add(button.method_25369());
                    button.method_25355((class_2561)class_2561.method_43473());
                    continue;
                }
            }
            if (!(widget instanceof FloatSlider)) continue;
            FloatSlider slider = (FloatSlider)widget;
            if (!slider.field_22764) continue;
            styledSliders.add(slider);
            originalSliderMessages.add(slider.method_25369());
            slider.method_25355((class_2561)class_2561.method_43473());
        }
        super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
        for (i = 0; i < styledButtons.size(); ++i) {
            ((class_4185)styledButtons.get(i)).method_25355((class_2561)originalButtonMessages.get(i));
        }
        for (i = 0; i < styledSliders.size(); ++i) {
            ((FloatSlider)((Object)styledSliders.get(i))).method_25355((class_2561)originalSliderMessages.get(i));
        }
        this.renderStyledSliders(guiGraphics, mouseX, mouseY);
        this.renderStyledButtons(guiGraphics, mouseX, mouseY);
        guiGraphics.method_27535(this.field_22793, this.field_22785, 14, 4, 0xFFFFFF);
        guiGraphics.method_27535(this.field_22793, (class_2561)class_2561.method_43469((String)"punchy.tuning.ui.held_item", (Object[])new Object[]{this.currentHeldItemString()}), 14, this.field_22790 - 28, 0xE0E0E0);
        this.renderKindButtonContent(guiGraphics);
        this.renderProfileList(guiGraphics);
        this.renderProfileScrollbar(guiGraphics);
        this.renderMainScrollbar(guiGraphics);
        guiGraphics.method_25303(this.field_22793, this.statusMessage == null ? "" : this.statusMessage, this.profilesTitleX, this.field_22790 - 14, 11075504);
        this.drawDeleteProfileConfirmOverlay(guiGraphics, mouseX, mouseY);
    }

    private void renderStyledSliders(class_332 guiGraphics, int mouseX, int mouseY) {
        for (FloatSlider slider : this.sliders) {
            if (!slider.field_22764) continue;
            slider.drawStyled(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderStyledButtons(class_332 guiGraphics, int mouseX, int mouseY) {
        for (class_339 widget : this.uiWidgets) {
            if (!(widget instanceof class_4185)) continue;
            class_4185 button = (class_4185)widget;
            if (!button.field_22764) continue;
            boolean destructive = button == this.deleteProfileButton;
            this.drawBlacklistButtonStyle(guiGraphics, button, mouseX, mouseY, destructive);
        }
    }

    private void drawBlacklistButtonStyle(class_332 guiGraphics, class_4185 button, int mouseX, int mouseY, boolean destructive) {
        int textColor;
        int border;
        int fill;
        int left = button.method_46426();
        int top = button.method_46427();
        int right = left + button.method_25368();
        int bottom = top + button.method_25364();
        boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
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
        guiGraphics.method_25294(left, top, right, bottom, fill);
        guiGraphics.method_25294(left, top, right, top + 1, border);
        guiGraphics.method_25294(left, bottom - 1, right, bottom, border);
        guiGraphics.method_25294(left, top, left + 1, bottom, border);
        guiGraphics.method_25294(right - 1, top, right, bottom, border);
        String label = button.method_25369() == null ? "" : button.method_25369().getString();
        int maxTextWidth = Math.max(8, button.method_25368() - 12);
        String renderLabel = this.resolveScrollableButtonLabel(label, maxTextWidth, hovered);
        int textX = left + (button.method_25368() - this.field_22793.method_1727(renderLabel)) / 2;
        int textY = top + (button.method_25364() - 8) / 2;
        guiGraphics.method_25303(this.field_22793, renderLabel, textX, textY, textColor);
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

    private void drawWorkbenchPanels(class_332 guiGraphics) {
        int panelFill = -267381478;
        int panelBorder = -11641751;
        guiGraphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelRight, this.mainPanelBottom, panelFill);
        guiGraphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelRight, this.mainPanelTop + 1, panelBorder);
        guiGraphics.method_25294(this.mainPanelLeft, this.mainPanelBottom - 1, this.mainPanelRight, this.mainPanelBottom, panelBorder);
        guiGraphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelLeft + 1, this.mainPanelBottom, panelBorder);
        guiGraphics.method_25294(this.mainPanelRight - 1, this.mainPanelTop, this.mainPanelRight, this.mainPanelBottom, panelBorder);
        int sectionFill = 857944122;
        int sectionBorder = 1430145621;
        guiGraphics.method_25294(this.leftPanelLeft, this.leftPanelTop, this.leftPanelRight, this.leftPanelBottom, sectionFill);
        guiGraphics.method_25294(this.leftPanelLeft, this.leftPanelTop, this.leftPanelRight, this.leftPanelTop + 1, sectionBorder);
        guiGraphics.method_25294(this.leftPanelLeft, this.leftPanelBottom - 1, this.leftPanelRight, this.leftPanelBottom, sectionBorder);
        guiGraphics.method_25294(this.leftPanelLeft, this.leftPanelTop, this.leftPanelLeft + 1, this.leftPanelBottom, sectionBorder);
        guiGraphics.method_25294(this.leftPanelRight - 1, this.leftPanelTop, this.leftPanelRight, this.leftPanelBottom, sectionBorder);
        guiGraphics.method_25294(this.rightPanelLeft, this.rightPanelTop, this.rightPanelRight, this.rightPanelBottom, sectionFill);
        guiGraphics.method_25294(this.rightPanelLeft, this.rightPanelTop, this.rightPanelRight, this.rightPanelTop + 1, sectionBorder);
        guiGraphics.method_25294(this.rightPanelLeft, this.rightPanelBottom - 1, this.rightPanelRight, this.rightPanelBottom, sectionBorder);
        guiGraphics.method_25294(this.rightPanelLeft, this.rightPanelTop, this.rightPanelLeft + 1, this.rightPanelBottom, sectionBorder);
        guiGraphics.method_25294(this.rightPanelRight - 1, this.rightPanelTop, this.rightPanelRight, this.rightPanelBottom, sectionBorder);
    }

    public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    public void method_52752(class_332 guiGraphics) {
    }

    public void method_25393() {
        super.method_25393();
        this.autoKindFromHeldItem();
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
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (this.deleteProfileConfirmOpen) {
            if (button == 0) {
                if (TuningWorkbenchScreen.isMouseInside(mouseX, mouseY, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom)) {
                    this.confirmDeleteProfile();
                    return true;
                }
                if (TuningWorkbenchScreen.isMouseInside(mouseX, mouseY, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom)) {
                    this.cancelDeleteProfileConfirm();
                    return true;
                }
            }
            return true;
        }
        if (button == 0 && this.activeSlider == null) {
            if (this.maxScroll > 0 && this.isMouseOverMainScrollbar(mouseX, mouseY)) {
                this.draggingMainScrollbar = true;
                this.updateMainScrollFromMouse(mouseY);
                return true;
            }
            if (this.maxProfileScroll() > 0 && this.isMouseOverProfileScrollbar(mouseX, mouseY)) {
                this.draggingProfileScrollbar = true;
                this.updateProfileScrollFromMouse(mouseY);
                return true;
            }
        }
        boolean handled = super.method_25402(mouseX, mouseY, button);
        if (button == 0) {
            this.activeSlider = null;
            for (FloatSlider slider : this.sliders) {
                if (!slider.field_22764 || !slider.field_22763 || !slider.method_25405(mouseX, mouseY)) continue;
                this.activeSlider = slider;
                break;
            }
            if (this.activeSlider == null && this.trySelectProfileFromList(mouseX, mouseY)) {
                handled = true;
            }
            this.applyFocusVisibility();
        }
        return handled;
    }

    public boolean method_25406(double mouseX, double mouseY, int button) {
        if (this.deleteProfileConfirmOpen) {
            return true;
        }
        if (button == 0) {
            this.draggingMainScrollbar = false;
            this.draggingProfileScrollbar = false;
        }
        boolean handled = super.method_25406(mouseX, mouseY, button);
        if (button == 0 && this.activeSlider != null) {
            this.activeSlider = null;
            this.applyFocusVisibility();
        }
        return handled;
    }

    public boolean method_25403(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.deleteProfileConfirmOpen) {
            return true;
        }
        if (button == 0) {
            if (this.draggingMainScrollbar) {
                this.updateMainScrollFromMouse(mouseY);
                return true;
            }
            if (this.draggingProfileScrollbar) {
                this.updateProfileScrollFromMouse(mouseY);
                return true;
            }
        }
        return super.method_25403(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.deleteProfileConfirmOpen) {
            return true;
        }
        if (this.activeSlider == null && verticalAmount != 0.0 && this.isMouseOverProfileList(mouseX, mouseY)) {
            int visible = this.visibleProfileRows();
            int maxListScroll = Math.max(0, this.profiles.size() - visible);
            if (maxListScroll > 0) {
                this.profileListScroll = class_3532.method_15340((int)(this.profileListScroll - (int)Math.signum(verticalAmount)), (int)0, (int)maxListScroll);
                this.updateProfileRowButtons();
                return true;
            }
        }
        if (this.activeSlider == null && this.maxScroll > 0 && verticalAmount != 0.0) {
            this.scrollOffset = class_3532.method_15340((int)(this.scrollOffset - (int)Math.round(verticalAmount * 24.0)), (int)0, (int)this.maxScroll);
            this.layoutWidgets();
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void autoKindFromHeldItem() {
        class_1799 other;
        class_2960 otherCarryId;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        class_2960 carryId = this.selectedCarryTuningId();
        if (carryId == null && (otherCarryId = this.carryTuningIdForHand(!this.leftHand)) != null) {
            this.leftHand = !this.leftHand;
            carryId = otherCarryId;
        }
        if (carryId != null) {
            VanillaFirstPersonItemLayer.ToolKind resolved = ToolTuning.resolveSpecificKind(carryId);
            if (resolved == null) {
                resolved = VanillaFirstPersonItemLayer.ToolKind.INGREDIENT;
            }
            if (this.isFishingLineEditor()) {
                this.editorTarget = EditorTarget.ITEM;
                this.normalizeScopeForTarget();
            }
            if (resolved != this.selectedKind) {
                this.selectedKind = resolved;
                this.refreshHeaderButtons();
                this.loadFromCurrentProfile();
            } else {
                this.refreshHeaderButtons();
            }
            return;
        }
        class_1799 stack = mc.field_1724.method_5998(this.leftHand ? class_1268.field_5810 : class_1268.field_5808);
        if ((stack == null || stack.method_7960()) && (other = mc.field_1724.method_5998(this.leftHand ? class_1268.field_5808 : class_1268.field_5810)) != null && !other.method_7960()) {
            this.leftHand = !this.leftHand;
            stack = other;
        }
        if (stack != null && !stack.method_7960()) {
            VanillaFirstPersonItemLayer.ToolKind resolved = this.resolveTuningKind(stack);
            if (this.isFishingLineEditor() && !this.isFishingRodStack(stack)) {
                this.editorTarget = EditorTarget.ITEM;
                this.normalizeScopeForTarget();
                this.loadFromCurrentProfile();
            }
            if (resolved != this.selectedKind) {
                this.selectedKind = resolved;
                this.refreshHeaderButtons();
                this.loadFromCurrentProfile();
            } else {
                this.refreshHeaderButtons();
            }
        } else {
            this.refreshHeaderButtons();
        }
    }

    private String currentHeldItemString() {
        if (this.isFishingLineEditor()) {
            return "fishing line proxy: rod";
        }
        if (this.isArmSpecificEmptyHandTarget()) {
            return class_2561.method_43471((String)"punchy.tuning.selection.empty_hand").getString();
        }
        class_2960 id = this.heldItemId();
        if (id == null) {
            return class_2561.method_43471((String)"punchy.tuning.selection.none_item").getString();
        }
        return PunchyPuppiesCompat.isCarryTuningId(id) ? "carry: " + String.valueOf(id) : id.toString();
    }

    private class_1799 heldItemStack() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return class_1799.field_8037;
        }
        class_1799 stack = mc.field_1724.method_5998(this.leftHand ? class_1268.field_5810 : class_1268.field_5808);
        return stack == null ? class_1799.field_8037 : stack;
    }

    private VanillaFirstPersonItemLayer.ToolKind resolveTuningKind(class_1799 stack) {
        if (SpearStateMachine.isSpearStack(stack)) {
            return VanillaFirstPersonItemLayer.ToolKind.SPEAR;
        }
        return ToolKindResolver.resolveItemKind(stack, this.selectedKind);
    }

    private FloatSlider addSlider(int x, int y, int width, int height, String key, float min, float max) {
        FloatSlider slider = new FloatSlider(x, y, width, height, key, min, max, value -> {
            if (!this.syncing) {
                this.applyToCurrentProfile();
            }
        });
        this.sliders.add(slider);
        this.addUiWidget(slider);
        return slider;
    }

    private void loadFromCurrentProfile() {
        this.configureSliderRangesForCurrentTarget();
        ToolTuning.Profile itemProfile = this.isItemEditor() ? this.getCurrentProfile() : null;
        ArmToolTuning.Profile armProfile = this.isArmEditor() ? this.getCurrentArmProfile() : null;
        FishingLineProxyTuning.Profile fishingLineProfile = this.isFishingLineEditor() ? this.getCurrentFishingLineProfile() : null;
        boolean has = itemProfile != null || armProfile != null || fishingLineProfile != null;
        boolean hideArmGlobalRotation = this.shouldHideArmGlobalRotationControls();
        this.syncing = true;
        if (itemProfile != null) {
            this.offX.setFloat(itemProfile.offX);
            this.offY.setFloat(itemProfile.offY);
            this.offZ.setFloat(itemProfile.offZ);
            this.rotX.setFloat(itemProfile.rotX);
            this.rotY.setFloat(itemProfile.rotY);
            this.rotZ.setFloat(itemProfile.rotZ);
            this.scale.setFloat(itemProfile.scale);
            this.pivotX.setFloat(itemProfile.pivotX);
            this.pivotY.setFloat(itemProfile.pivotY);
            this.pivotZ.setFloat(itemProfile.pivotZ);
        } else if (armProfile != null) {
            ArmToolTuning.Profile base = this.armEditorBaseProfile();
            this.offX.setFloat(armProfile.offX - base.offX);
            this.offY.setFloat(armProfile.offY - base.offY);
            this.offZ.setFloat(armProfile.offZ - base.offZ);
            this.rotX.setFloat(armProfile.rotX - base.rotX);
            this.rotY.setFloat(armProfile.rotY - base.rotY);
            this.rotZ.setFloat(armProfile.rotZ - base.rotZ);
            this.scale.setFloat(armProfile.scale);
            this.pivotX.setFloat(0.0f);
            this.pivotY.setFloat(0.0f);
            this.pivotZ.setFloat(0.0f);
        } else if (fishingLineProfile != null) {
            this.offX.setFloat(fishingLineProfile.offX);
            this.offY.setFloat(fishingLineProfile.offY);
            this.offZ.setFloat(fishingLineProfile.offZ);
            this.rotX.setFloat(fishingLineProfile.rotX);
            this.rotY.setFloat(fishingLineProfile.rotY);
            this.rotZ.setFloat(fishingLineProfile.rotZ);
            this.scale.setFloat(fishingLineProfile.scale);
            this.pivotX.setFloat(0.0f);
            this.pivotY.setFloat(0.0f);
            this.pivotZ.setFloat(0.0f);
        }
        this.syncing = false;
        for (FloatSlider slider : this.sliders) {
            slider.field_22763 = has;
        }
        if (this.isArmEditor()) {
            this.rotX.field_22763 = !hideArmGlobalRotation && has;
            this.rotY.field_22763 = !hideArmGlobalRotation && has;
            this.rotZ.field_22763 = !hideArmGlobalRotation && has;
            this.rotX.field_22764 = !hideArmGlobalRotation;
            this.rotY.field_22764 = !hideArmGlobalRotation;
            this.rotZ.field_22764 = !hideArmGlobalRotation;
            this.pivotX.field_22763 = false;
            this.pivotY.field_22763 = false;
            this.pivotZ.field_22763 = false;
            this.pivotX.field_22764 = false;
            this.pivotY.field_22764 = false;
            this.pivotZ.field_22764 = false;
            if (this.activeSlider != null && (this.activeSlider.isPivotSlider() || hideArmGlobalRotation && this.activeSlider.isRotationSlider())) {
                this.activeSlider = null;
            }
        } else if (this.isFishingLineEditor()) {
            this.pivotX.field_22763 = false;
            this.pivotY.field_22763 = false;
            this.pivotZ.field_22763 = false;
            this.pivotX.field_22764 = false;
            this.pivotY.field_22764 = false;
            this.pivotZ.field_22764 = false;
            if (this.activeSlider != null && this.activeSlider.isPivotSlider()) {
                this.activeSlider = null;
            }
        }
    }

    private void configureSliderRangesForCurrentTarget() {
        if (this.isFishingLineEditor()) {
            this.offX.setRange(-32.0f, 32.0f);
            this.offY.setRange(-32.0f, 32.0f);
            this.offZ.setRange(-32.0f, 32.0f);
            this.rotX.setRange(-720.0f, 720.0f);
            this.rotY.setRange(-720.0f, 720.0f);
            this.rotZ.setRange(-720.0f, 720.0f);
            this.scale.setRange(0.01f, 10.0f);
            this.pivotX.setRange(-1.0f, 1.0f);
            this.pivotY.setRange(-1.0f, 1.0f);
            this.pivotZ.setRange(-1.0f, 1.0f);
            return;
        }
        this.offX.setRange(-2.0f, 2.0f);
        this.offY.setRange(-2.0f, 2.0f);
        this.offZ.setRange(-2.0f, 2.0f);
        this.rotX.setRange(-180.0f, 180.0f);
        this.rotY.setRange(-180.0f, 180.0f);
        this.rotZ.setRange(-180.0f, 180.0f);
        this.scale.setRange(0.1f, 3.0f);
        this.pivotX.setRange(-1.0f, 1.0f);
        this.pivotY.setRange(-1.0f, 1.0f);
        this.pivotZ.setRange(-1.0f, 1.0f);
    }

    private ToolTuning.Profile getCurrentProfile() {
        boolean targetLeftArm = this.selectedHandTargetsLeftArm();
        return switch (this.scope.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ToolTuning.getGlobal(targetLeftArm);
            case 1 -> {
                class_2960 carryId = this.selectedCarryTuningId();
                if (carryId != null) {
                    yield ToolTuning.getSpecificProfile(carryId, this.selectedKind, targetLeftArm);
                }
                class_1799 held = this.heldItemStack();
                if (held == null || held.method_7960()) {
                    yield null;
                }
                yield ToolTuning.getSpecificProfile(held, this.selectedKind, targetLeftArm);
            }
        };
    }

    private void applyToCurrentProfile() {
        if (this.isFishingLineEditor()) {
            FishingLineProxyTuning.Profile profile = this.getCurrentFishingLineProfile();
            if (profile == null) {
                this.statusMessage = "Hold a fishing rod to edit fishing line proxy";
                return;
            }
            profile.offX = this.offX.getFloat();
            profile.offY = this.offY.getFloat();
            profile.offZ = this.offZ.getFloat();
            profile.rotX = this.rotX.getFloat();
            profile.rotY = this.rotY.getFloat();
            profile.rotZ = this.rotZ.getFloat();
            profile.scale = this.scale.getFloat();
            FishingLineProxyTuning.apply(FishingLineProxyTuning.Kind.ROD, this.selectedHandTargetsLeftArm(), profile);
            this.hasUnsavedProfileChanges = true;
            return;
        }
        if (this.isArmEditor()) {
            ArmToolTuning.Profile profile = this.getCurrentArmProfile();
            if (profile == null) {
                this.statusMessage = class_2561.method_43471((String)"punchy.tuning.toast.hold_item_edit_specific").getString();
                return;
            }
            ArmToolTuning.Profile base = this.armEditorBaseProfile();
            profile.offX = base.offX + this.offX.getFloat();
            profile.offY = base.offY + this.offY.getFloat();
            profile.offZ = base.offZ + this.offZ.getFloat();
            profile.rotX = base.rotX + this.rotX.getFloat();
            profile.rotY = base.rotY + this.rotY.getFloat();
            profile.rotZ = base.rotZ + this.rotZ.getFloat();
            profile.scale = this.scale.getFloat();
            PlayerArmModelLayer.ToolKind armKind = this.currentArmKind();
            boolean targetLeftArm = this.selectedHandTargetsLeftArm();
            switch (this.scope.ordinal()) {
                case 0: {
                    ArmToolTuning.commitGlobal(targetLeftArm);
                    break;
                }
                case 1: {
                    class_2960 id = this.heldItemId();
                    if (id == null) {
                        ArmToolTuning.commitKind(PlayerArmModelLayer.ToolKind.EMPTY_HAND, targetLeftArm);
                        break;
                    }
                    ArmToolTuning.commitSpecific(id, targetLeftArm);
                }
            }
            this.hasUnsavedProfileChanges = true;
            return;
        }
        ToolTuning.Profile profile = this.getCurrentProfile();
        if (profile == null) {
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.toast.hold_item_edit_specific").getString();
            return;
        }
        profile.offX = this.offX.getFloat();
        profile.offY = this.offY.getFloat();
        profile.offZ = this.offZ.getFloat();
        profile.rotX = this.rotX.getFloat();
        profile.rotY = this.rotY.getFloat();
        profile.rotZ = this.rotZ.getFloat();
        profile.scale = this.scale.getFloat();
        profile.pivotX = this.pivotX.getFloat();
        profile.pivotY = this.pivotY.getFloat();
        profile.pivotZ = this.pivotZ.getFloat();
        profile.recomputeRadians();
        boolean targetLeftArm = this.selectedHandTargetsLeftArm();
        switch (this.scope.ordinal()) {
            case 0: {
                ToolTuning.commitGlobal(targetLeftArm);
                break;
            }
            case 1: {
                class_2960 id = this.heldItemId();
                if (id == null) {
                    return;
                }
                class_1799 held = this.heldItemStack();
                Integer cmd = null;
                String renamed = null;
                boolean carryTuning = PunchyPuppiesCompat.isCarryTuningId(id);
                if (!carryTuning && held != null && !held.method_7960()) {
                    cmd = ToolTuning.resolveCustomModelDataForDebug(held);
                    renamed = ToolTuning.resolveStackRenamedKeyForDebug(held);
                }
                ToolTuning.Profile right = !carryTuning && held != null && !held.method_7960() ? ToolTuning.getSpecificProfile(held, this.selectedKind, false) : ToolTuning.getSpecificProfile(id, this.selectedKind, false);
                ToolTuning.Profile left = !carryTuning && held != null && !held.method_7960() ? ToolTuning.getSpecificProfile(held, this.selectedKind, true) : ToolTuning.getSpecificProfile(id, this.selectedKind, true);
                ToolTuning.applySpecific(id, cmd, this.selectedKind, renamed, null, null, null, null, null, null, right, left);
            }
        }
        this.hasUnsavedProfileChanges = true;
    }

    private void resetCurrentProfile() {
        if (this.isFishingLineEditor()) {
            FishingLineProxyTuning.reset(FishingLineProxyTuning.Kind.ROD, this.selectedHandTargetsLeftArm());
            this.loadFromCurrentProfile();
            this.hasUnsavedProfileChanges = true;
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.current_reset").getString();
            return;
        }
        if (this.isArmEditor()) {
            ArmToolTuning.Profile profile = this.getCurrentArmProfile();
            if (profile == null) {
                return;
            }
            this.syncing = true;
            this.offX.setFloat(0.0f);
            this.offY.setFloat(0.0f);
            this.offZ.setFloat(0.0f);
            this.rotX.setFloat(0.0f);
            this.rotY.setFloat(0.0f);
            this.rotZ.setFloat(0.0f);
            this.scale.setFloat(this.armEditorBaseProfile().scale);
            this.syncing = false;
            this.applyToCurrentProfile();
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.current_reset").getString();
            return;
        }
        ToolTuning.Profile profile = this.getCurrentProfile();
        if (profile == null) {
            return;
        }
        this.syncing = true;
        this.offX.setFloat(0.0f);
        this.offY.setFloat(0.0f);
        this.offZ.setFloat(0.0f);
        this.rotX.setFloat(0.0f);
        this.rotY.setFloat(0.0f);
        this.rotZ.setFloat(0.0f);
        this.scale.setFloat(1.0f);
        this.pivotX.setFloat(0.0f);
        this.pivotY.setFloat(0.0f);
        this.pivotZ.setFloat(0.0f);
        this.syncing = false;
        this.applyToCurrentProfile();
        this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.current_reset").getString();
    }

    private void mirrorToOtherHand() {
        if (this.isFishingLineEditor()) {
            FishingLineProxyTuning.Profile src = this.getCurrentFishingLineProfile();
            if (src == null) {
                return;
            }
            FishingLineProxyTuning.apply(FishingLineProxyTuning.Kind.ROD, !this.selectedHandTargetsLeftArm(), FishingLineProxyTuning.mirror(src));
            this.hasUnsavedProfileChanges = true;
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.mirrored").getString();
            return;
        }
        if (this.isArmEditor()) {
            ArmToolTuning.Profile src = this.getCurrentArmProfile();
            if (src == null) {
                return;
            }
            ArmToolTuning.Profile mirrored = src.mirror();
            boolean other = !this.selectedHandTargetsLeftArm();
            PlayerArmModelLayer.ToolKind armKind = this.currentArmKind();
            switch (this.scope.ordinal()) {
                case 0: {
                    ArmToolTuning.applyGlobal(other, mirrored);
                    ArmToolTuning.commitGlobal(other);
                    break;
                }
                case 1: {
                    class_2960 id = this.heldItemId();
                    if (id == null) {
                        ArmToolTuning.apply(PlayerArmModelLayer.ToolKind.EMPTY_HAND, other, mirrored);
                        ArmToolTuning.commitKind(PlayerArmModelLayer.ToolKind.EMPTY_HAND, other);
                        break;
                    }
                    ArmToolTuning.Profile right = other ? ArmToolTuning.getSpecificProfile(id, armKind, false) : mirrored;
                    ArmToolTuning.Profile left = other ? mirrored : ArmToolTuning.getSpecificProfile(id, armKind, true);
                    ArmToolTuning.applySpecific(id, armKind, right, left);
                }
            }
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.mirrored").getString();
            return;
        }
        ToolTuning.Profile src = this.getCurrentProfile();
        if (src == null) {
            return;
        }
        ToolTuning.Profile mirrored = src.mirror();
        boolean other = !this.selectedHandTargetsLeftArm();
        switch (this.scope.ordinal()) {
            case 0: {
                ToolTuning.applyGlobal(other, mirrored);
                ToolTuning.commitGlobal(other);
                break;
            }
            case 1: {
                ToolTuning.Profile right;
                class_2960 id = this.heldItemId();
                if (id == null) {
                    return;
                }
                class_1799 held = this.heldItemStack();
                Integer cmd = null;
                String renamed = null;
                boolean carryTuning = PunchyPuppiesCompat.isCarryTuningId(id);
                if (!carryTuning && held != null && !held.method_7960()) {
                    cmd = ToolTuning.resolveCustomModelDataForDebug(held);
                    renamed = ToolTuning.resolveStackRenamedKeyForDebug(held);
                }
                ToolTuning.Profile profile = other ? (!carryTuning && held != null && !held.method_7960() ? ToolTuning.getSpecificProfile(held, this.selectedKind, false) : ToolTuning.getSpecificProfile(id, this.selectedKind, false)) : (right = mirrored);
                ToolTuning.Profile left = other ? mirrored : (!carryTuning && held != null && !held.method_7960() ? ToolTuning.getSpecificProfile(held, this.selectedKind, true) : ToolTuning.getSpecificProfile(id, this.selectedKind, true));
                ToolTuning.applySpecific(id, cmd, this.selectedKind, renamed, null, null, null, null, null, null, right, left);
            }
        }
        this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.mirrored").getString();
    }

    private class_2561 scopeLabel() {
        return class_2561.method_43471((String)this.scope.key);
    }

    private class_2561 handLabel() {
        return this.leftHand ? class_2561.method_43471((String)"punchy.tuning.ui.hand.off") : class_2561.method_43471((String)"punchy.tuning.ui.hand.main");
    }

    private class_2561 kindLabel() {
        if (this.isFishingLineEditor()) {
            return class_2561.method_43470((String)"Editing fishing line proxy: rod");
        }
        return class_2561.method_43469((String)"punchy.tuning.ui.editing_item", (Object[])new Object[]{this.currentHeldItemString()});
    }

    private void renderKindButtonContent(class_332 guiGraphics) {
        if (this.kindButton == null || !this.kindButton.field_22764) {
            return;
        }
        if (this.selectedCarryTuningId() != null) {
            return;
        }
        int x = this.kindButton.method_46426();
        int y = this.kindButton.method_46427();
        int height = this.kindButton.method_25364();
        class_1799 stack = this.heldItemStack();
        if (stack != null && !stack.method_7960()) {
            int iconX = x + 5;
            int iconY = y + Math.max(0, (height - 16) / 2);
            this.renderItemIcon(guiGraphics, stack, iconX, iconY);
        }
    }

    private void renderItemIcon(class_332 guiGraphics, class_1799 stack, int x, int y) {
        if (stack == null || stack.method_7960() || guiGraphics == null) {
            return;
        }
        guiGraphics.method_51427(stack, x, y);
    }

    private class_2561 targetLabel() {
        return switch (this.editorTarget.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> class_2561.method_43471((String)"punchy.tuning.ui.editor.item");
            case 1 -> class_2561.method_43471((String)"punchy.tuning.ui.editor.arm");
            case 2 -> class_2561.method_43470((String)"Line");
        };
    }

    private class_2960 heldItemId() {
        class_2960 carryId = this.selectedCarryTuningId();
        if (carryId != null) {
            return carryId;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return null;
        }
        class_1799 stack = mc.field_1724.method_5998(this.leftHand ? class_1268.field_5810 : class_1268.field_5808);
        if (stack == null || stack.method_7960()) {
            return null;
        }
        return class_7923.field_41178.method_10221((Object)stack.method_7909());
    }

    private class_2960 selectedCarryTuningId() {
        return this.carryTuningIdForHand(this.leftHand);
    }

    private class_2960 carryTuningIdForHand(boolean offHand) {
        return PunchyPuppiesCompat.getCarryTuningId(offHand ? class_1268.field_5810 : class_1268.field_5808);
    }

    private void refreshProfiles(boolean clampSelection) {
        this.profiles = TuningProfileManager.listProfiles();
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)Math.max(0, this.profiles.size() - this.visibleProfileRows()));
        if (this.profiles.isEmpty()) {
            this.selectedProfile = -1;
            if (this.profileNameBox != null) {
                this.profileNameBox.method_1852("");
            }
            this.updateProfileRowButtons();
            return;
        }
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size() || clampSelection) {
            String active = TuningProfileManager.getActiveProfileName();
            if (active != null) {
                int activeIndex = this.profiles.indexOf(active);
                if (activeIndex >= 0) {
                    this.selectedProfile = activeIndex;
                }
            } else if (clampSelection) {
                this.selectedProfile = -1;
            }
            if (this.selectedProfile < 0) {
                if (!clampSelection) {
                    this.selectedProfile = 0;
                }
            } else {
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

    private String selectedProfileName() {
        if (this.selectedProfile < 0 || this.selectedProfile >= this.profiles.size()) {
            return null;
        }
        return this.profiles.get(this.selectedProfile);
    }

    private void refreshHeaderButtons() {
        if (this.scopeButton != null) {
            this.scopeButton.method_25355(this.scopeLabel());
            this.scopeButton.field_22763 = this.isArmEditor();
        }
        if (this.handButton != null) {
            this.handButton.method_25355(this.handLabel());
        }
        if (this.targetButton != null) {
            this.targetButton.method_25355(this.targetLabel());
        }
        if (this.kindButton != null) {
            this.kindButton.method_25355(this.kindButtonLabel());
            this.kindButton.field_22763 = false;
        }
    }

    private class_2561 kindButtonLabel() {
        String fullText = this.kindLabel().getString();
        int width = this.kindButton != null ? this.kindButton.method_25368() : 160;
        int availableWidth = Math.max(40, width - 34);
        String renderText = this.field_22793.method_27523(fullText, availableWidth);
        return class_2561.method_43470((String)("    " + renderText));
    }

    private String defaultProfileName() {
        class_2960 id = this.heldItemId();
        String base = this.isFishingLineEditor() ? "fishing_line_proxy" : (this.isArmSpecificEmptyHandTarget() ? "empty_hand" : (id == null ? "tuning" : id.method_12832()));
        String hand = this.leftHand ? "off" : "main";
        String sc = this.scope.name().toLowerCase(Locale.ROOT);
        return base + "_" + hand + "_" + sc;
    }

    private void layoutWidgets() {
        int leftX;
        int targetPanelWidth;
        if (this.scopeButton == null) {
            return;
        }
        int sidePadding = 24;
        int maxMenuWidth = 800;
        int gap = 6;
        int row = 20;
        int panelGap = 12;
        int availableWidth = Math.max(260, this.field_22789 - sidePadding * 2);
        int totalWidth = Math.min(maxMenuWidth, availableWidth);
        int leftWidth = totalWidth - (targetPanelWidth = class_3532.method_15340((int)(totalWidth / 3), (int)220, (int)300)) - panelGap;
        boolean bl = this.compactLayout = leftWidth < 280;
        if (this.compactLayout) {
            leftWidth = totalWidth;
            targetPanelWidth = totalWidth;
        }
        int top = this.computeContainerTop(this.compactLayout, row, gap);
        this.containerLeft = leftX = (this.field_22789 - totalWidth) / 2;
        this.containerRight = leftX + totalWidth;
        int rightX = this.compactLayout ? leftX : leftX + leftWidth + panelGap;
        int y = top;
        if (this.compactLayout) {
            int headerW = (leftWidth - gap * 3) / 4;
            this.place((class_339)this.scopeButton, leftX, y, headerW);
            this.place((class_339)this.handButton, leftX + headerW + gap, y, headerW);
            this.place((class_339)this.targetButton, leftX + (headerW + gap) * 2, y, headerW);
            this.place((class_339)this.kindButton, leftX + (headerW + gap) * 3, y, leftWidth - (headerW + gap) * 3);
        } else {
            int scopeW = Math.min(120, Math.max(96, leftWidth / 4));
            int handW = Math.min(95, Math.max(80, leftWidth / 5));
            int targetW = Math.min(110, Math.max(92, leftWidth / 6));
            int kindW = leftWidth - scopeW - handW - targetW - gap * 3;
            this.place((class_339)this.scopeButton, leftX, y, scopeW);
            this.place((class_339)this.handButton, leftX + scopeW + gap, y, handW);
            this.place((class_339)this.targetButton, leftX + scopeW + gap + handW + gap, y, targetW);
            this.place((class_339)this.kindButton, leftX + scopeW + gap + handW + gap + targetW + gap, y, kindW);
        }
        this.place((class_339)this.offX, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.offY, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.offZ, leftX, y += row + 2, leftWidth);
        y += row + 2;
        if (!this.shouldHideArmGlobalRotationControls()) {
            this.place((class_339)this.rotX, leftX, y, leftWidth);
            this.place((class_339)this.rotY, leftX, y += row + 2, leftWidth);
            this.place((class_339)this.rotZ, leftX, y += row + 2, leftWidth);
            y += row + 2;
        }
        this.place((class_339)this.scale, leftX, y, leftWidth);
        this.place((class_339)this.pivotX, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.pivotY, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.pivotZ, leftX, y += row + 2, leftWidth);
        int leftActionsBaseY = y += row + gap;
        int profileY = this.compactLayout ? leftActionsBaseY + row + gap + 12 : top;
        int listRows = 6;
        int listRowHeight = 14;
        int listPadding = 4;
        int listHeight = listRows * listRowHeight + listPadding * 2;
        this.profileListX = rightX;
        this.profileListY = profileY - this.scrollOffset;
        this.profileListW = targetPanelWidth;
        this.profileListH = listHeight;
        this.layoutProfileRowButtons();
        this.place((class_339)this.profileNameBox, rightX, profileY += listHeight + 6, targetPanelWidth);
        int profileActionW = (targetPanelWidth - gap) / 2;
        this.place((class_339)this.saveProfileButton, rightX, profileY += row + 4, profileActionW);
        this.place((class_339)this.loadProfileButton, rightX + profileActionW + gap, profileY, targetPanelWidth - profileActionW - gap);
        this.place((class_339)this.deleteProfileButton, rightX, profileY += row + 2, targetPanelWidth);
        int doneY = (profileY += row + 14) + row + 2;
        int openFolderY = Math.max(profileY, doneY - (row + 8));
        this.place((class_339)this.importProfileButton, rightX, openFolderY, targetPanelWidth);
        this.place((class_339)this.doneButton, rightX, doneY, targetPanelWidth);
        profileY = doneY + row + gap;
        int actionY = Math.max(leftActionsBaseY, doneY);
        int actionW = (leftWidth - gap * 3) / 4;
        int lastActionW = leftWidth - actionW * 3 - gap * 3;
        this.place((class_339)this.resetButton, leftX, actionY, actionW);
        this.place((class_339)this.resetCurrentButton, leftX + actionW + gap, actionY, actionW);
        this.place((class_339)this.mirrorButton, leftX + (actionW + gap) * 2, actionY, actionW);
        this.place((class_339)this.reloadTuningButton, leftX + (actionW + gap) * 3, actionY, lastActionW);
        int leftBottom = actionY + row + gap;
        int contentBottom = Math.max(leftBottom, profileY) + 8;
        this.maxScroll = Math.max(0, contentBottom - (this.field_22790 - 8));
        this.scrollOffset = class_3532.method_15340((int)this.scrollOffset, (int)0, (int)this.maxScroll);
        if (this.maxScroll > 0) {
            this.reflowWithScroll(totalWidth, leftX, leftWidth, rightX, targetPanelWidth, top, gap, row);
        } else {
            this.profilesTitleX = rightX;
            this.profilesTitleY = this.profileTitleY(top, this.compactLayout, leftBottom);
        }
        this.updateWorkbenchPanelRects(leftX, leftWidth, rightX, targetPanelWidth);
        this.updateMainScrollbarRects();
        this.updateProfileScrollbarRects();
        if (this.kindButton != null) {
            this.kindButton.method_25355(this.kindButtonLabel());
        }
    }

    private int computeContainerTop(boolean compact, int row, int gap) {
        int headerHeight = row + 2;
        int sliderHeight = (row + 2) * 9 + row + gap;
        int actionHeight = row + gap;
        int leftColumnHeight = headerHeight + sliderHeight + actionHeight;
        int listRows = 6;
        int listRowHeight = 14;
        int listPadding = 4;
        int listHeight = listRows * listRowHeight + listPadding * 2;
        int rightColumnHeight = listHeight + 6;
        rightColumnHeight += row + 4;
        rightColumnHeight += row + 2;
        rightColumnHeight += row + 14;
        rightColumnHeight += row + 2;
        int contentHeight = compact ? leftColumnHeight + 12 + rightColumnHeight : Math.max(leftColumnHeight, rightColumnHeight += row + gap);
        return Math.max(12, (this.field_22790 - contentHeight) / 2);
    }

    private void reflowWithScroll(int totalWidth, int leftX, int leftWidth, int rightX, int panelWidth, int top, int gap, int row) {
        int y = top;
        if (this.compactLayout) {
            int headerW = (leftWidth - gap * 3) / 4;
            this.place((class_339)this.scopeButton, leftX, y, headerW);
            this.place((class_339)this.handButton, leftX + headerW + gap, y, headerW);
            this.place((class_339)this.targetButton, leftX + (headerW + gap) * 2, y, headerW);
            this.place((class_339)this.kindButton, leftX + (headerW + gap) * 3, y, leftWidth - (headerW + gap) * 3);
        } else {
            int scopeW = Math.min(120, Math.max(96, leftWidth / 4));
            int handW = Math.min(95, Math.max(80, leftWidth / 5));
            int targetW = Math.min(110, Math.max(92, leftWidth / 6));
            int kindW = leftWidth - scopeW - handW - targetW - gap * 3;
            this.place((class_339)this.scopeButton, leftX, y, scopeW);
            this.place((class_339)this.handButton, leftX + scopeW + gap, y, handW);
            this.place((class_339)this.targetButton, leftX + scopeW + gap + handW + gap, y, targetW);
            this.place((class_339)this.kindButton, leftX + scopeW + gap + handW + gap + targetW + gap, y, kindW);
        }
        this.place((class_339)this.offX, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.offY, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.offZ, leftX, y += row + 2, leftWidth);
        y += row + 2;
        if (!this.shouldHideArmGlobalRotationControls()) {
            this.place((class_339)this.rotX, leftX, y, leftWidth);
            this.place((class_339)this.rotY, leftX, y += row + 2, leftWidth);
            this.place((class_339)this.rotZ, leftX, y += row + 2, leftWidth);
            y += row + 2;
        }
        this.place((class_339)this.scale, leftX, y, leftWidth);
        this.place((class_339)this.pivotX, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.pivotY, leftX, y += row + 2, leftWidth);
        this.place((class_339)this.pivotZ, leftX, y += row + 2, leftWidth);
        int leftActionsBaseY = y += row + gap;
        int profileY = this.compactLayout ? leftActionsBaseY + row + gap + 12 : top;
        int listRows = 6;
        int listRowHeight = 14;
        int listPadding = 4;
        int listHeight = listRows * listRowHeight + listPadding * 2;
        this.profileListX = rightX;
        this.profileListY = profileY - this.scrollOffset;
        this.profileListW = panelWidth;
        this.profileListH = listHeight;
        this.layoutProfileRowButtons();
        this.place((class_339)this.profileNameBox, rightX, profileY += listHeight + 6, panelWidth);
        int profileActionW = (panelWidth - gap) / 2;
        this.place((class_339)this.saveProfileButton, rightX, profileY += row + 4, profileActionW);
        this.place((class_339)this.loadProfileButton, rightX + profileActionW + gap, profileY, panelWidth - profileActionW - gap);
        this.place((class_339)this.deleteProfileButton, rightX, profileY += row + 2, panelWidth);
        int doneY = (profileY += row + 14) + row + 2;
        int openFolderY = Math.max(profileY, doneY - (row + 8));
        this.place((class_339)this.importProfileButton, rightX, openFolderY, panelWidth);
        this.place((class_339)this.doneButton, rightX, doneY, panelWidth);
        int actionY = Math.max(leftActionsBaseY, doneY);
        int actionW = (leftWidth - gap * 3) / 4;
        int lastActionW = leftWidth - actionW * 3 - gap * 3;
        this.place((class_339)this.resetButton, leftX, actionY, actionW);
        this.place((class_339)this.resetCurrentButton, leftX + actionW + gap, actionY, actionW);
        this.place((class_339)this.mirrorButton, leftX + (actionW + gap) * 2, actionY, actionW);
        this.place((class_339)this.reloadTuningButton, leftX + (actionW + gap) * 3, actionY, lastActionW);
        int leftBottom = actionY + row + gap;
        this.profilesTitleX = rightX;
        this.profilesTitleY = this.profileTitleY(top, this.compactLayout, leftBottom);
        if (this.kindButton != null) {
            this.kindButton.method_25355(this.kindButtonLabel());
        }
        this.recalcDeleteConfirmLayout();
    }

    private int profileTitleY(int top, boolean compact, int leftBottom) {
        int profileTop = compact ? leftBottom + 12 : top;
        return profileTop - 12 - this.scrollOffset;
    }

    private void renderProfileList(class_332 guiGraphics) {
        if (this.profileListW <= 0 || this.profileListH <= 0) {
            return;
        }
        int x0 = this.profileListX;
        int y0 = this.profileListY;
        int x1 = this.profileListX + this.profileListW;
        int y1 = this.profileListY + this.profileListH;
        guiGraphics.method_25294(x0, y0, x1, y1, 1141641495);
        if (this.profiles.isEmpty()) {
            guiGraphics.method_27535(this.field_22793, (class_2561)class_2561.method_43471((String)"punchy.tuning.selection.none_item"), x0 + 6, y0 + 6, 0xA0A0A0);
            return;
        }
    }

    private void renderMainScrollbar(class_332 guiGraphics) {
        if (this.maxScroll <= 0) {
            return;
        }
        int barW = 4;
        guiGraphics.method_25294(this.mainScrollX, this.mainScrollTop, this.mainScrollX + barW, this.mainScrollBottom, -1442840576);
        int trackH = Math.max(1, this.mainScrollBottom - this.mainScrollTop);
        int viewH = Math.max(1, this.field_22790 - 16);
        int contentH = viewH + this.maxScroll;
        int thumbH = Math.max(10, Math.round((float)trackH * ((float)viewH / (float)contentH)));
        int thumbMax = Math.max(0, trackH - thumbH);
        int thumbOffset = this.maxScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.scrollOffset / (float)this.maxScroll));
        int thumbTop = this.mainScrollTop + thumbOffset;
        guiGraphics.method_25294(this.mainScrollX, thumbTop, this.mainScrollX + barW, Math.min(this.mainScrollBottom, thumbTop + thumbH), -1);
    }

    private void renderProfileScrollbar(class_332 guiGraphics) {
        int maxListScroll = this.maxProfileScroll();
        if (maxListScroll <= 0 || this.profileListH <= 0) {
            return;
        }
        int barW = 4;
        guiGraphics.method_25294(this.profileScrollX, this.profileScrollTop, this.profileScrollX + barW, this.profileScrollBottom, -1442840576);
        int trackH = Math.max(1, this.profileScrollBottom - this.profileScrollTop);
        int visible = Math.max(1, this.visibleProfileRows());
        int total = Math.max(visible, this.profiles.size());
        int thumbH = Math.max(10, Math.round((float)trackH * ((float)visible / (float)total)));
        int thumbMax = Math.max(0, trackH - thumbH);
        int thumbOffset = maxListScroll == 0 ? 0 : Math.round((float)thumbMax * ((float)this.profileListScroll / (float)maxListScroll));
        int thumbTop = this.profileScrollTop + thumbOffset;
        guiGraphics.method_25294(this.profileScrollX, thumbTop, this.profileScrollX + barW, Math.min(this.profileScrollBottom, thumbTop + thumbH), -1);
    }

    private int visibleProfileRows() {
        if (this.profileListH <= 0) {
            return 1;
        }
        int rows = (this.profileListH - 8) / 14;
        return Math.max(1, rows);
    }

    private boolean isMouseOverProfileList(double mouseX, double mouseY) {
        return mouseX >= (double)this.profileListX && mouseX < (double)(this.profileListX + this.profileListW) && mouseY >= (double)this.profileListY && mouseY < (double)(this.profileListY + this.profileListH);
    }

    private boolean isMouseOverMainScrollbar(double mouseX, double mouseY) {
        return mouseX >= (double)this.mainScrollX && mouseX < (double)(this.mainScrollX + 4) && mouseY >= (double)this.mainScrollTop && mouseY < (double)this.mainScrollBottom;
    }

    private boolean isMouseOverProfileScrollbar(double mouseX, double mouseY) {
        return mouseX >= (double)this.profileScrollX && mouseX < (double)(this.profileScrollX + 4) && mouseY >= (double)this.profileScrollTop && mouseY < (double)this.profileScrollBottom;
    }

    private boolean trySelectProfileFromList(double mouseX, double mouseY) {
        if (!this.isMouseOverProfileList(mouseX, mouseY) || this.profiles.isEmpty()) {
            return false;
        }
        int relativeY = (int)mouseY - (this.profileListY + 4);
        int row = relativeY / 14;
        if (row < 0) {
            return false;
        }
        int index = this.profileListScroll + row;
        if (index < 0 || index >= this.profiles.size()) {
            return false;
        }
        this.selectedProfile = index;
        String selectedName = this.selectedProfileName();
        if (selectedName != null) {
            this.profileNameBox.method_1852(selectedName);
        }
        this.updateProfileRowButtons();
        return true;
    }

    private void ensureSelectedVisibleInProfileList() {
        if (this.selectedProfile < 0) {
            return;
        }
        int visible = this.visibleProfileRows();
        if (this.selectedProfile < this.profileListScroll) {
            this.profileListScroll = this.selectedProfile;
        } else if (this.selectedProfile >= this.profileListScroll + visible) {
            this.profileListScroll = this.selectedProfile - visible + 1;
        }
        int maxListScroll = Math.max(0, this.profiles.size() - visible);
        this.profileListScroll = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)maxListScroll);
    }

    private void updateMainScrollbarRects() {
        this.mainScrollX = Math.max(6, this.containerRight + 6);
        if (this.mainScrollX > this.field_22789 - 8) {
            this.mainScrollX = this.field_22789 - 8;
        }
        this.mainScrollTop = 18;
        this.mainScrollBottom = Math.max(this.mainScrollTop + 12, this.field_22790 - 18);
    }

    private void updateProfileScrollbarRects() {
        this.profileScrollX = Math.max(this.profileListX + 2, this.profileListX + this.profileListW - 6);
        this.profileScrollTop = this.profileListY + 2;
        this.profileScrollBottom = this.profileListY + Math.max(6, this.profileListH - 2);
    }

    private void updateMainScrollFromMouse(double mouseY) {
        int trackH = Math.max(1, this.mainScrollBottom - this.mainScrollTop);
        int knobCenter = class_3532.method_15340((int)((int)Math.round(mouseY) - this.mainScrollTop), (int)0, (int)trackH);
        int newOffset = (int)Math.round((double)knobCenter / (double)trackH * (double)this.maxScroll);
        this.scrollOffset = class_3532.method_15340((int)newOffset, (int)0, (int)this.maxScroll);
        this.layoutWidgets();
    }

    private void updateProfileScrollFromMouse(double mouseY) {
        int maxListScroll = this.maxProfileScroll();
        int trackH = Math.max(1, this.profileScrollBottom - this.profileScrollTop);
        int knobCenter = class_3532.method_15340((int)((int)Math.round(mouseY) - this.profileScrollTop), (int)0, (int)trackH);
        int newOffset = (int)Math.round((double)knobCenter / (double)trackH * (double)maxListScroll);
        this.profileListScroll = class_3532.method_15340((int)newOffset, (int)0, (int)maxListScroll);
        this.updateProfileRowButtons();
    }

    private int maxProfileScroll() {
        return Math.max(0, this.profiles.size() - this.visibleProfileRows());
    }

    private void updateWorkbenchPanelRects(int leftX, int leftWidth, int rightX, int rightWidth) {
        int mainLeft = Math.min(leftX, rightX) - 10;
        int mainRight = Math.max(leftX + leftWidth, rightX + rightWidth) + 10;
        int topY = Math.min(this.scopeButton.method_46427(), this.profileListY) - 12;
        int bottomY = Math.max(this.reloadTuningButton.method_46427() + this.reloadTuningButton.method_25364(), this.doneButton.method_46427() + this.doneButton.method_25364()) + 10;
        this.mainPanelLeft = class_3532.method_15340((int)mainLeft, (int)4, (int)(this.field_22789 - 4));
        this.mainPanelRight = class_3532.method_15340((int)mainRight, (int)4, (int)(this.field_22789 - 4));
        this.mainPanelTop = class_3532.method_15340((int)topY, (int)4, (int)(this.field_22790 - 4));
        this.mainPanelBottom = class_3532.method_15340((int)bottomY, (int)4, (int)(this.field_22790 - 4));
        if (this.mainPanelBottom <= this.mainPanelTop + 2) {
            this.mainPanelBottom = Math.min(this.field_22790 - 4, this.mainPanelTop + 3);
        }
        this.leftPanelLeft = class_3532.method_15340((int)(leftX - 4), (int)4, (int)(this.field_22789 - 4));
        this.leftPanelRight = class_3532.method_15340((int)(leftX + leftWidth + 4), (int)4, (int)(this.field_22789 - 4));
        this.leftPanelTop = class_3532.method_15340((int)(this.scopeButton.method_46427() - 6), (int)4, (int)(this.field_22790 - 4));
        this.leftPanelBottom = class_3532.method_15340((int)(this.reloadTuningButton.method_46427() + this.reloadTuningButton.method_25364() + 6), (int)4, (int)(this.field_22790 - 4));
        if (this.leftPanelBottom <= this.leftPanelTop + 2) {
            this.leftPanelBottom = Math.min(this.field_22790 - 4, this.leftPanelTop + 3);
        }
        this.rightPanelLeft = class_3532.method_15340((int)(rightX - 4), (int)4, (int)(this.field_22789 - 4));
        this.rightPanelRight = class_3532.method_15340((int)(rightX + rightWidth + 4), (int)4, (int)(this.field_22789 - 4));
        this.rightPanelTop = class_3532.method_15340((int)(this.profileListY - 6), (int)4, (int)(this.field_22790 - 4));
        this.rightPanelBottom = class_3532.method_15340((int)(this.doneButton.method_46427() + this.doneButton.method_25364() + 6), (int)4, (int)(this.field_22790 - 4));
        if (this.rightPanelBottom <= this.rightPanelTop + 2) {
            this.rightPanelBottom = Math.min(this.field_22790 - 4, this.rightPanelTop + 3);
        }
        this.recalcDeleteConfirmLayout();
    }

    private void openDeleteProfileConfirm() {
        String name = this.selectedProfileName();
        if (name == null || name.isBlank()) {
            this.statusMessage = class_2561.method_43471((String)"punchy.tuning.ui.status.profile_delete_failed").getString();
            return;
        }
        this.pendingDeleteProfileName = name;
        this.deleteProfileConfirmOpen = true;
        this.activeSlider = null;
        this.applyFocusVisibility();
        this.recalcDeleteConfirmLayout();
    }

    private void cancelDeleteProfileConfirm() {
        this.deleteProfileConfirmOpen = false;
        this.pendingDeleteProfileName = null;
    }

    private void confirmDeleteProfile() {
        boolean ok;
        String name = this.pendingDeleteProfileName;
        boolean bl = ok = name != null && !name.isBlank() && TuningProfileManager.deleteProfile(name);
        if (ok && (ok = TuningProfileManager.unloadActiveProfileToDefault())) {
            this.hasUnsavedProfileChanges = false;
            this.loadFromCurrentProfile();
        }
        this.statusMessage = ok ? class_2561.method_43469((String)"punchy.tuning.ui.status.profile_deleted", (Object[])new Object[]{name}).getString() : class_2561.method_43471((String)"punchy.tuning.ui.status.profile_delete_failed").getString();
        this.cancelDeleteProfileConfirm();
        this.refreshProfiles(true);
    }

    private void recalcDeleteConfirmLayout() {
        int modalWidth = Math.min(380, Math.max(260, this.mainPanelRight - this.mainPanelLeft - 48));
        int modalHeight = 112;
        this.deleteModalLeft = this.mainPanelLeft + (this.mainPanelRight - this.mainPanelLeft - modalWidth) / 2;
        this.deleteModalTop = this.mainPanelTop + (this.mainPanelBottom - this.mainPanelTop - modalHeight) / 2;
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

    private void drawDeleteProfileConfirmOverlay(class_332 guiGraphics, int mouseX, int mouseY) {
        if (!this.deleteProfileConfirmOpen) {
            return;
        }
        guiGraphics.method_51448().method_22903();
        guiGraphics.method_51448().method_46416(0.0f, 0.0f, 500.0f);
        guiGraphics.method_25294(this.mainPanelLeft, this.mainPanelTop, this.mainPanelRight, this.mainPanelBottom, -1610612736);
        guiGraphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalRight, this.deleteModalBottom, -15657446);
        int border = -11641751;
        guiGraphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalRight, this.deleteModalTop + 1, border);
        guiGraphics.method_25294(this.deleteModalLeft, this.deleteModalBottom - 1, this.deleteModalRight, this.deleteModalBottom, border);
        guiGraphics.method_25294(this.deleteModalLeft, this.deleteModalTop, this.deleteModalLeft + 1, this.deleteModalBottom, border);
        guiGraphics.method_25294(this.deleteModalRight - 1, this.deleteModalTop, this.deleteModalRight, this.deleteModalBottom, border);
        class_2561 title = TuningWorkbenchScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.title", "Voce realmente quer fazer isso?", new Object[0]);
        String targetName = this.pendingDeleteProfileName == null ? "-" : this.pendingDeleteProfileName;
        class_2561 message = TuningWorkbenchScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.message", "Delete profile \"" + targetName + "\"?", targetName);
        String titleText = title.getString();
        String messageText = message.getString();
        int titleX = this.deleteModalLeft + (this.deleteModalRight - this.deleteModalLeft - this.field_22793.method_1727(titleText)) / 2;
        int messageX = this.deleteModalLeft + (this.deleteModalRight - this.deleteModalLeft - this.field_22793.method_1727(messageText)) / 2;
        guiGraphics.method_25303(this.field_22793, titleText, titleX, this.deleteModalTop + 12, -1);
        guiGraphics.method_25303(this.field_22793, messageText, messageX, this.deleteModalTop + 34, -3747624);
        boolean hoverCancel = TuningWorkbenchScreen.isMouseInside(mouseX, mouseY, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom);
        boolean hoverConfirm = TuningWorkbenchScreen.isMouseInside(mouseX, mouseY, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom);
        this.drawModalButton(guiGraphics, this.deleteCancelButtonLeft, this.deleteCancelButtonTop, this.deleteCancelButtonRight, this.deleteCancelButtonBottom, hoverCancel, false, TuningWorkbenchScreen.nonBlankTranslatable("gui.cancel", "Cancel", new Object[0]));
        this.drawModalButton(guiGraphics, this.deleteConfirmButtonLeft, this.deleteConfirmButtonTop, this.deleteConfirmButtonRight, this.deleteConfirmButtonBottom, hoverConfirm, true, TuningWorkbenchScreen.nonBlankTranslatable("punchy.tuning.ui.confirm_delete_profile.confirm", "Delete", new Object[0]));
        guiGraphics.method_51448().method_22909();
    }

    private void drawModalButton(class_332 guiGraphics, int left, int top, int right, int bottom, boolean hovered, boolean destructive, class_2561 label) {
        int fill = destructive ? (hovered ? -7459282 : -9690590) : (hovered ? -12958126 : -13682107);
        int border = destructive ? -3048067 : -7429710;
        guiGraphics.method_25294(left, top, right, bottom, fill);
        guiGraphics.method_25294(left, top, right, top + 1, border);
        guiGraphics.method_25294(left, bottom - 1, right, bottom, border);
        guiGraphics.method_25294(left, top, left + 1, bottom, border);
        guiGraphics.method_25294(right - 1, top, right, bottom, border);
        String labelText = label.getString();
        int textX = left + (right - left - this.field_22793.method_1727(labelText)) / 2;
        int textY = top + (bottom - top - 8) / 2;
        guiGraphics.method_25303(this.field_22793, labelText, textX, textY, -1);
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

    private void layoutProfileRowButtons() {
        int rowHeight = 14;
        int rowButtonHeight = rowHeight - 1;
        int startY = this.profileListY + 4;
        int rowWidth = Math.max(10, this.profileListW);
        for (int i = 0; i < this.profileRowButtons.size(); ++i) {
            class_4185 rowButton = this.profileRowButtons.get(i);
            rowButton.method_46421(this.profileListX);
            rowButton.method_46419(startY + i * rowHeight);
            rowButton.method_25358(rowWidth);
            rowButton.method_53533(rowButtonHeight);
        }
        this.updateProfileRowButtons();
    }

    private void updateProfileRowButtons() {
        int visible = this.visibleProfileRows();
        int startIndex = class_3532.method_15340((int)this.profileListScroll, (int)0, (int)Math.max(0, this.profiles.size() - visible));
        for (int i = 0; i < this.profileRowButtons.size(); ++i) {
            class_4185 rowButton = this.profileRowButtons.get(i);
            int index = startIndex + i;
            if (index >= this.profiles.size() || i >= visible) {
                rowButton.field_22764 = false;
                rowButton.field_22763 = false;
                rowButton.method_25355((class_2561)class_2561.method_43473());
                continue;
            }
            String name = this.profiles.get(index);
            boolean selected = index == this.selectedProfile;
            rowButton.field_22764 = true;
            rowButton.field_22763 = true;
            rowButton.method_25355((class_2561)class_2561.method_43470((String)((selected ? "> " : "") + name)));
        }
    }

    private void selectProfileAtVisibleRow(int row) {
        int visible = this.visibleProfileRows();
        if (row < 0 || row >= visible) {
            return;
        }
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

    private boolean openProfilesFolder() {
        try {
            Path dir = TuningProfileManager.profilesDirectoryPath();
            class_156.method_668().method_60932(dir);
            return true;
        }
        catch (Exception ignored) {
            try {
                Path dir = TuningProfileManager.profilesDirectoryPath();
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

    private void place(class_339 widget, int x, int baseY, int width) {
        widget.method_46421(x);
        widget.method_46419(baseY - this.scrollOffset);
        widget.method_25358(width);
    }

    private void applyFocusVisibility() {
        boolean hidePivotInCurrentMode;
        boolean hideArmGlobalRotation = this.shouldHideArmGlobalRotationControls();
        boolean bl = hidePivotInCurrentMode = this.isArmEditor() || this.isFishingLineEditor();
        if (this.activeSlider == null) {
            FishingLineProxyTuning.Profile fishingLineProfile;
            for (class_339 widget : this.uiWidgets) {
                widget.field_22764 = true;
                widget.field_22763 = true;
            }
            ToolTuning.Profile profile = this.isItemEditor() ? this.getCurrentProfile() : null;
            ArmToolTuning.Profile armProfile = this.isArmEditor() ? this.getCurrentArmProfile() : null;
            FishingLineProxyTuning.Profile profile2 = fishingLineProfile = this.isFishingLineEditor() ? this.getCurrentFishingLineProfile() : null;
            boolean has = this.isArmEditor() ? armProfile != null : (this.isFishingLineEditor() ? fishingLineProfile != null : profile != null);
            for (FloatSlider slider : this.sliders) {
                slider.field_22763 = has;
                if (hidePivotInCurrentMode && slider.isPivotSlider()) {
                    slider.field_22764 = false;
                    slider.field_22763 = false;
                    continue;
                }
                if (hideArmGlobalRotation && slider.isRotationSlider()) {
                    slider.field_22764 = false;
                    slider.field_22763 = false;
                    continue;
                }
                slider.field_22764 = true;
            }
            if (this.kindButton != null) {
                this.kindButton.field_22763 = false;
            }
            return;
        }
        for (class_339 widget : this.uiWidgets) {
            boolean keep;
            widget.field_22764 = keep = widget == this.activeSlider;
            widget.field_22763 = keep;
        }
    }

    private <T extends class_339> T addUiWidget(T widget) {
        class_339 added = (class_339)this.method_37063((class_364)widget);
        this.uiWidgets.add(added);
        return (T)added;
    }

    private void normalizeScopeForTarget() {
        if (!this.isArmEditor()) {
            this.scope = Scope.SPECIFIC;
        }
        if (this.isFishingLineEditor() && !this.isHeldFishingRod()) {
            this.editorTarget = EditorTarget.ITEM;
        }
    }

    private void cycleEditorTarget() {
        this.editorTarget = this.editorTarget == EditorTarget.ITEM ? EditorTarget.ARM : (this.editorTarget == EditorTarget.ARM ? (this.isHeldFishingRod() ? EditorTarget.FISHING_LINE : EditorTarget.ITEM) : EditorTarget.ITEM);
    }

    private boolean isItemEditor() {
        return this.editorTarget == EditorTarget.ITEM;
    }

    private boolean isArmEditor() {
        return this.editorTarget == EditorTarget.ARM;
    }

    private boolean isFishingLineEditor() {
        return this.editorTarget == EditorTarget.FISHING_LINE;
    }

    private FishingLineProxyTuning.Profile getCurrentFishingLineProfile() {
        if (!this.isHeldFishingRod()) {
            return null;
        }
        return FishingLineProxyTuning.get(FishingLineProxyTuning.Kind.ROD, this.selectedHandTargetsLeftArm());
    }

    private boolean isHeldFishingRod() {
        return this.isFishingRodStack(this.heldItemStack());
    }

    private boolean isFishingRodStack(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        return id != null && "minecraft".equals(id.method_12836()) && "fishing_rod".equals(id.method_12832());
    }

    private static FishingLineProxyTuning.Profile copyFishingLineProfile(FishingLineProxyTuning.Profile src) {
        if (src == null) {
            return null;
        }
        return new FishingLineProxyTuning.Profile(src.offX, src.offY, src.offZ, src.rotX, src.rotY, src.rotZ, src.scale);
    }

    private boolean isArmSpecificEmptyHandTarget() {
        return this.isArmEditor() && this.scope == Scope.SPECIFIC && this.heldItemId() == null;
    }

    private boolean shouldHideArmGlobalRotationControls() {
        return this.isArmEditor() && this.scope == Scope.GLOBAL;
    }

    private PlayerArmModelLayer.ToolKind currentArmKind() {
        PlayerArmModelLayer.ToolKind specificKind;
        class_2960 carryId = this.selectedCarryTuningId();
        if (carryId != null && (specificKind = ArmToolTuning.resolveSpecificKind(carryId)) != null) {
            return specificKind;
        }
        PlayerArmModelLayer.ToolKind mappedKind = this.armKindForSelectedItemKind();
        if (mappedKind != null) {
            return mappedKind;
        }
        class_310 mc = class_310.method_1551();
        class_1799 stack = class_1799.field_8037;
        if (mc != null && mc.field_1724 != null) {
            stack = mc.field_1724.method_5998(this.leftHand ? class_1268.field_5810 : class_1268.field_5808);
        }
        return ToolKindResolver.resolveArmKind(stack, PlayerArmModelLayer.ToolKind.AXE);
    }

    private PlayerArmModelLayer.ToolKind armKindForSelectedItemKind() {
        try {
            return PlayerArmModelLayer.ToolKind.valueOf(this.selectedKind.name());
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private ArmToolTuning.Profile getCurrentArmProfile() {
        PlayerArmModelLayer.ToolKind armKind = this.currentArmKind();
        boolean targetLeftArm = this.selectedHandTargetsLeftArm();
        return switch (this.scope.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ArmToolTuning.getGlobal(targetLeftArm);
            case 1 -> {
                class_2960 id = this.heldItemId();
                if (id == null) {
                    yield ArmToolTuning.get(PlayerArmModelLayer.ToolKind.EMPTY_HAND, targetLeftArm);
                }
                yield ArmToolTuning.getSpecificProfile(id, armKind, targetLeftArm);
            }
        };
    }

    private ArmToolTuning.Profile armEditorBaseProfile() {
        if (!this.isArmEditor()) {
            return new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        boolean targetLeftArm = this.selectedHandTargetsLeftArm();
        return switch (this.scope.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PunchyTuningConfig.getDefaultGlobalArmBase(targetLeftArm);
            case 1 -> new ArmToolTuning.Profile(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        };
    }

    private boolean selectedHandTargetsLeftArm() {
        boolean mainArmIsLeft;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return this.leftHand;
        }
        boolean bl = mainArmIsLeft = mc.field_1724.method_6068() == class_1306.field_6182;
        return this.leftHand ? !mainArmIsLeft : mainArmIsLeft;
    }

    private void applyArmGlobalToAllSpecifics(boolean left, ArmToolTuning.Profile newGlobal) {
        if (newGlobal == null) {
            return;
        }
        Map<class_2960, ArmToolTuning.SpecificSnapshot> specifics = ArmToolTuning.snapshotSpecifics();
        for (Map.Entry<class_2960, ArmToolTuning.SpecificSnapshot> entry : specifics.entrySet()) {
            ArmToolTuning.Profile target;
            class_2960 id = entry.getKey();
            ArmToolTuning.SpecificSnapshot snap = entry.getValue();
            if (id == null || snap == null) continue;
            ArmToolTuning.Profile right = snap.right();
            ArmToolTuning.Profile leftProfile = snap.left();
            ArmToolTuning.Profile profile = target = left ? leftProfile : right;
            if (target != null) {
                target.offX = newGlobal.offX;
                target.offY = newGlobal.offY;
                target.offZ = newGlobal.offZ;
                target.rotX = newGlobal.rotX;
                target.rotY = newGlobal.rotY;
                target.rotZ = newGlobal.rotZ;
                target.scale = newGlobal.scale;
            }
            ArmToolTuning.applySpecific(id, snap.kind(), right, leftProfile);
        }
    }

    private static enum Scope {
        GLOBAL("punchy.tuning.ui.scope.global"),
        SPECIFIC("punchy.tuning.ui.scope.specific");

        final String key;

        private Scope(String key) {
            this.key = key;
        }
    }

    private static final class FloatSlider
    extends class_357 {
        private final String key;
        private float min;
        private float max;
        private final Consumer<Float> onChanged;
        private boolean draggingRelative;
        private double dragStartMouseX;
        private double dragStartValue;

        private FloatSlider(int x, int y, int width, int height, String key, float min, float max, Consumer<Float> onChanged) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), 0.0);
            this.key = key;
            this.min = min;
            this.max = max;
            this.onChanged = onChanged;
            this.setFloat(0.0f);
        }

        private float getFloat() {
            return class_3532.method_16439((float)((float)this.field_22753), (float)this.min, (float)this.max);
        }

        private void setFloat(float value) {
            float clamped = class_3532.method_15363((float)value, (float)this.min, (float)this.max);
            this.field_22753 = (clamped - this.min) / (this.max - this.min);
            this.method_25346();
        }

        private void setRange(float min, float max) {
            if (max <= min) {
                return;
            }
            float current = this.getFloat();
            this.min = min;
            this.max = max;
            this.setFloat(current);
        }

        public boolean method_25402(double mouseX, double mouseY, int button) {
            if (!(this.field_22764 && this.field_22763 && button == 0 && this.method_25405(mouseX, mouseY))) {
                return false;
            }
            this.draggingRelative = true;
            this.dragStartMouseX = mouseX;
            this.dragStartValue = this.field_22753;
            this.method_25365(true);
            return true;
        }

        public boolean method_25403(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!this.draggingRelative || button != 0) {
                return false;
            }
            int travel = Math.max(1, this.method_25368() - 8);
            double delta = (mouseX - this.dragStartMouseX) / (double)travel;
            this.field_22753 = class_3532.method_15350((double)(this.dragStartValue + delta), (double)0.0, (double)1.0);
            this.method_25346();
            this.method_25344();
            return true;
        }

        public boolean method_25406(double mouseX, double mouseY, int button) {
            if (button == 0 && this.draggingRelative) {
                this.draggingRelative = false;
                return true;
            }
            return false;
        }

        private void drawStyled(class_332 guiGraphics, int mouseX, int mouseY) {
            int textColor;
            int border;
            int fill;
            int maxLabelWidth;
            String clippedLabel;
            int trackLeft;
            if (!this.field_22764) {
                return;
            }
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.field_1772 == null) {
                return;
            }
            int left = this.method_46426();
            int top = this.method_46427();
            int right = left + this.method_25368();
            int bottom = top + this.method_25364();
            boolean hovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
            boolean focused = this.method_25370();
            String label = FloatSlider.displayKey(this.key);
            String valueText = String.format(Locale.ROOT, "%.3f", Float.valueOf(this.getFloat()));
            int textY = top + (this.method_25364() - 8) / 2;
            int labelX = left + 10;
            int valueX = right - 10 - mc.field_1772.method_1727(valueText);
            int trackRight = valueX - 8;
            if (trackRight <= (trackLeft = labelX + mc.field_1772.method_1727(clippedLabel = mc.field_1772.method_27523(label, maxLabelWidth = Math.max(10, valueX - labelX - 16))) + 10) + 12) {
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
                border = -7485697;
                textColor = -1;
            } else {
                fill = -13682107;
                border = -7429710;
                textColor = -1;
            }
            guiGraphics.method_25294(left, top, right, bottom, fill);
            guiGraphics.method_25294(left, top, right, top + 1, border);
            guiGraphics.method_25294(left, bottom - 1, right, bottom, border);
            guiGraphics.method_25294(left, top, left + 1, bottom, border);
            guiGraphics.method_25294(right - 1, top, right, bottom, border);
            int trackTop = top + this.method_25364() / 2 - 2;
            int trackBottom = trackTop + 4;
            guiGraphics.method_25294(trackLeft, trackTop, trackRight, trackBottom, -1441719774);
            int knobWidth = 8;
            int knobTravel = Math.max(0, trackWidth - knobWidth);
            int knobLeft = trackLeft + (int)Math.round(this.field_22753 * (double)knobTravel);
            int knobRight = knobLeft + knobWidth;
            int fillRight = knobLeft + knobWidth / 2;
            guiGraphics.method_25294(trackLeft, trackTop, fillRight, trackBottom, -13869680);
            int knobFill = !this.field_22763 ? -11641751 : (focused || hovered ? -7485697 : -9798515);
            guiGraphics.method_25294(knobLeft, top + 2, knobRight, bottom - 2, knobFill);
            guiGraphics.method_25294(knobLeft, top + 2, knobRight, top + 3, -4732456);
            guiGraphics.method_25294(knobLeft, bottom - 3, knobRight, bottom - 2, -14404542);
            guiGraphics.method_25303(mc.field_1772, clippedLabel, labelX, textY, textColor);
            guiGraphics.method_25303(mc.field_1772, valueText, valueX, textY, textColor);
        }

        protected void method_25346() {
            String v = String.format(Locale.ROOT, "%.3f", Float.valueOf(this.getFloat()));
            this.method_25355((class_2561)class_2561.method_43470((String)(this.key + ": " + v)));
        }

        protected void method_25344() {
            if (this.onChanged != null) {
                this.onChanged.accept(Float.valueOf(this.getFloat()));
            }
        }

        private boolean isPivotSlider() {
            return "pivotX".equals(this.key) || "pivotY".equals(this.key) || "pivotZ".equals(this.key);
        }

        private boolean isRotationSlider() {
            return "rotX".equals(this.key) || "rotY".equals(this.key) || "rotZ".equals(this.key);
        }

        private static String displayKey(String key) {
            if (key == null || key.isBlank()) {
                return "";
            }
            return switch (key) {
                case "offX" -> "Offset X";
                case "offY" -> "Offset Y";
                case "offZ" -> "Offset Z";
                case "rotX" -> "Rotation X";
                case "rotY" -> "Rotation Y";
                case "rotZ" -> "Rotation Z";
                case "pivotX" -> "Pivot X";
                case "pivotY" -> "Pivot Y";
                case "pivotZ" -> "Pivot Z";
                case "scale" -> "Scale";
                default -> key;
            };
        }
    }

    private static enum EditorTarget {
        ITEM,
        ARM,
        FISHING_LINE;

    }
}


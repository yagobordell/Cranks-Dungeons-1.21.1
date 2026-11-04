package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.GuiTextureResolver;
import com.cranks.dungeons.client.HorseInlineSelectorWidget;
import com.cranks.dungeons.client.HorseGeneticsClientData;
import com.cranks.dungeons.client.StatLine;
import com.cranks.dungeons.mixin.HandledScreenAccessor;
import com.cranks.dungeons.mixin.ScreenAccessor;
import com.cranks.dungeons.horse.HorseBreedingFocus;
import com.cranks.dungeons.horse.HorseBreedingRules;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.network.SetHorseBreedingFocusPayload;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import com.cranks.dungeons.network.SetMountSpecializationPayload;
import com.cranks.dungeons.network.ReleaseMountPayload;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountAccessMode;
import com.cranks.dungeons.client.MountClientData;
import com.cranks.dungeons.network.MountDataSyncPacket;
import com.cranks.dungeons.network.SetMountAccessPayload;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// Render the information panels from the screen render pass so they remain
// present even when the cargo mixin replaces/cancels vanilla drawBackground.
@Mixin(HorseScreen.class)
public abstract class HorseScreenStatsMixin {
    @Unique private static final int PANEL_WIDTH = 162;
    @Unique private static final int PANEL_GAP = 4;
    @Unique private static final int HEADER_HEIGHT = 22;
    @Unique private static final int LINE_STEP = 7;
    @Unique private static final int GENETIC_LINE_STEP = 11;
    @Unique private static final int GENETIC_BAR_HEIGHT = 3;
    @Unique private static final float TEXT_SCALE = 0.75f;
    @Unique private static final double SPEED_TO_BLOCKS_PER_SECOND = 42.16;
    @Unique private static final double ZERO_THRESHOLD = 0.0001;
    // The advancement title box is a vanilla nine-slice panel with an active
    // gold border. Input-field sprites make the stat panels look disabled
    // when stretched to fit their contents.
    @Unique private static final Identifier VANILLA_PANEL = Identifier.ofVanilla("advancements/title_box");
    @Unique private static Identifier cranks$statsPanelTexture = VANILLA_PANEL;
    @Unique private static Identifier cranks$genesPanelTexture = VANILLA_PANEL;
    @Unique private static Identifier cranks$familyPanelTexture = VANILLA_PANEL;
    @Unique private static Identifier cranks$pairingPanelTexture = VANILLA_PANEL;
    @Unique private static boolean cranks$texturesResolved;
    @Unique private HorseBreedingFocus cranks$selectedFocus = HorseBreedingFocus.NONE;
    @Unique private HorseBreedingFocus cranks$lastServerFocus;
    @Unique private boolean cranks$focusRowVisible;
    @Unique private HorseInlineSelectorWidget cranks$focusSelector;
    @Unique private HorseInlineSelectorWidget cranks$accessSelector;
    @Unique private boolean cranks$accessRowVisible;
    @Unique private MountAccessMode cranks$selectedAccessMode = MountAccessMode.OWNER_ONLY;
    @Unique private MountAccessMode cranks$lastServerAccessMode;
    @Unique private int cranks$accessEntityId = -1;
    @Unique private HorseInlineSelectorWidget cranks$mateSelector;
    @Unique private List<HorseEntity> cranks$nearbyMates = List.of();
    @Unique private UUID cranks$selectedMateId;
    @Unique private final List<ButtonWidget> cranks$specializationButtons = new ArrayList<>();
    @Unique private ButtonWidget cranks$releaseOwnershipButton;

    @Shadow @Final private AbstractHorseEntity entity;

    // The controls are painted at render tail over the panels. Hide the
    // previous frame's widget before HandledScreen renders its normal children.
    @Inject(method = "render", at = @At("HEAD"))
    private void cranks$hideReleaseOwnershipButton(DrawContext context, int mouseX, int mouseY,
                                                   float delta, CallbackInfo ci) {
        if (cranks$releaseOwnershipButton != null) {
            cranks$releaseOwnershipButton.visible = false;
        }
    }

    // Draw after HandledScreen has painted its background and slot contents.
    // The cargo screen mixin replaces/cancels drawBackground, so drawing at
    // render tail keeps these external panels visible for every mount screen.
    @Inject(method = "render", at = @At("TAIL"))
    private void cranks$ensureBreedingFocusSelector(DrawContext context, int mouseX, int mouseY,
                                                     float delta, CallbackInfo ci) {
        if (entity instanceof SkeletonHorseEntity) {
            cranks$ensureSpecializationButtons();
            return;
        }
        if (!(entity instanceof HorseEntity)) return;
        HorseScreen screen = (HorseScreen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) screen;
        if (cranks$focusSelector == null || !accessor.getChildren().contains(cranks$focusSelector)) {
            cranks$focusSelector = new HorseInlineSelectorWidget(
                    PANEL_WIDTH - 16, LINE_STEP + 2, cranks$focusNarration(), this::cranks$cycleFocusBy);
            cranks$focusSelector.visible = false;
            accessor.invokeAddDrawableChild(cranks$focusSelector);
        }
        if (cranks$mateSelector == null || !accessor.getChildren().contains(cranks$mateSelector)) {
            cranks$mateSelector = new HorseInlineSelectorWidget(
                    PANEL_WIDTH - 16, LINE_STEP + 2, Text.literal("Pairing candidate"),
                    this::cranks$cycleMateBy);
            cranks$mateSelector.visible = false;
            accessor.invokeAddDrawableChild(cranks$mateSelector);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void cranks$drawHorseInformation(DrawContext context, int mouseX, int mouseY, float delta,
                                             CallbackInfo ci) {
        cranks$focusRowVisible = false;
        cranks$accessRowVisible = false;
        if (cranks$accessSelector != null) cranks$accessSelector.visible = false;
        if (cranks$mateSelector != null) cranks$mateSelector.visible = false;
        for (ButtonWidget button : cranks$specializationButtons) button.visible = false;
        cranks$ensureReleaseOwnershipButton();
        cranks$ensureAccessSelector();
        if (!(entity instanceof HorseEntity horse)) {
            if (entity instanceof MountDataProvider) cranks$drawOtherMountInformation(context);
            cranks$renderPanelControls(context, mouseX, mouseY, delta);
            return;
        }
        cranks$resolveTextures();

        HorseScreen screen = (HorseScreen) (Object) this;
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        HorseGeneticsSyncPacket data = HorseGeneticsClientData.get(horse.getId());
        cranks$syncSelectedFocus(data);

        List<StatLine> stats = cranks$buildStats(horse, data);
        boolean canEditAccess = data != null && data.tamed()
                && (MinecraftClient.getInstance().player != null
                && (MinecraftClient.getInstance().player.isCreative()
                || (data.ownerUuid() != null && data.ownerUuid().equals(
                MinecraftClient.getInstance().player.getUuid()))));
        boolean canReleaseOwnership = data != null && data.tamed()
                && cranks$isCurrentOwner(data.ownerUuid());
        if (data != null && data.tamed() && canEditAccess) {
            MountAccessMode accessMode = cranks$syncSelectedAccessMode(data.accessMode(), horse.getId());
            String accessText = cranks$accessText(accessMode, canEditAccess);
            stats.add(new StatLine("Mount Access", accessText, cranks$accessColor(accessMode)));
            cranks$accessRowVisible = true;
        }
        List<StatLine> genes = cranks$buildGenes(data, cranks$selectedFocus);
        List<StatLine> family = cranks$buildFamily(horse, data);
        cranks$nearbyMates = cranks$findNearbyMates(horse, data);
        HorseEntity mate = cranks$selectCurrentMate();
        HorseGeneticsSyncPacket mateData = mate == null ? null : HorseGeneticsClientData.get(mate.getId());
        int mateIndex = mate == null ? -1 : cranks$nearbyMates.indexOf(mate);
        String mateSelectorText = mate == null ? "" : cranks$selectorText(
                renderer, mate.getName().getString());
        List<StatLine> pairing = mate == null ? List.of() : cranks$buildPairingPreview(
                horse, data, mate, mateData, mateSelectorText, mateIndex, cranks$nearbyMates.size());

        int baseX = accessor.getX();
        int baseY = accessor.getY();
        int statsX = Math.max(4, baseX - PANEL_WIDTH - PANEL_GAP);
        // The cargo grid is rendered as a fixed five-column viewport and is
        // scrolled for large genetic inventories.  Do not use the mount's
        // total getInventoryColumns() here: donkeys report their full
        // capacity (up to 33 slots), which would push the right panels far
        // away from the visible horse inventory.
        int inventoryWidth = accessor.getBackgroundWidth();
        int rightX = Math.min(screen.width - PANEL_WIDTH - 4,
                baseX + inventoryWidth + PANEL_GAP);
        int releaseButtonSpace = canReleaseOwnership ? 22 : 0;
        int statsHeight = cranks$panelHeight(stats) + releaseButtonSpace;
        int pairingHeight = pairing.isEmpty() ? 0 : cranks$panelHeight(pairing);
        int genesHeight = cranks$panelHeight(genes);
        int familyHeight = cranks$panelHeight(family);
        int leftStackHeight = statsHeight
                + (pairing.isEmpty() ? 0 : PANEL_GAP + pairingHeight);
        int rightStackHeight = genesHeight + PANEL_GAP + familyHeight;
        int leftY = Math.max(4, Math.min(baseY, screen.height - leftStackHeight - 4) - 3);
        int rightY = Math.max(4, Math.min(baseY, screen.height - rightStackHeight - 4) - 3);
        int familyY = rightY + genesHeight + PANEL_GAP;

        cranks$drawPanel(context, renderer, cranks$statsPanelTexture, "Horse Stats",
                statsX, leftY, stats, releaseButtonSpace);
        if (canReleaseOwnership) {
            cranks$positionReleaseOwnershipButton(statsX, leftY, stats);
        }
        if (!pairing.isEmpty()) {
            cranks$drawPanel(context, renderer, cranks$pairingPanelTexture, "Pairing Preview",
                    statsX, leftY + statsHeight + PANEL_GAP, pairing);
            if (cranks$mateSelector != null && cranks$nearbyMates.size() > 1) {
                int pairingY = leftY + statsHeight + PANEL_GAP;
                cranks$positionSelector(cranks$mateSelector, renderer, statsX, pairingY,
                        pairing, 0, mateSelectorText);
                cranks$mateSelector.visible = true;
                cranks$mateSelector.setMessage(Text.literal("Pairing candidate "
                        + (mateIndex + 1) + " of " + cranks$nearbyMates.size() + ": "
                        + mate.getName().getString()));
            }
        }
        cranks$drawPanel(context, renderer, cranks$genesPanelTexture, "Genetics",
                rightX, rightY, genes);
        cranks$drawPanel(context, renderer, cranks$familyPanelTexture, "Breeding",
                rightX, familyY, family);

        cranks$focusRowVisible = data != null;
        if (cranks$focusSelector != null) {
            String selectorText = cranks$focusDisplayText(cranks$selectedFocus);
            cranks$positionSelector(cranks$focusSelector, renderer, rightX, rightY,
                    genes, genes.size() - 1, selectorText);
            cranks$focusSelector.visible = cranks$focusRowVisible;
            cranks$focusSelector.setMessage(cranks$focusNarration());
        }
        if (cranks$accessSelector != null && cranks$accessRowVisible) {
            MountAccessMode accessMode = cranks$selectedAccessMode;
            String accessText = cranks$accessText(accessMode, canEditAccess);
            cranks$positionSelector(cranks$accessSelector, renderer, statsX, leftY,
                    stats, stats.size() - 1, accessText);
            cranks$accessSelector.visible = canEditAccess;
            cranks$accessSelector.active = canEditAccess;
            cranks$accessSelector.setMessage(Text.literal("Mount access: " + accessMode.displayName()));
        }

        // HandledScreen renders its children before this tail injection. The
        // panels must be painted last so the vanilla horse background cannot
        // wash them out; repaint our controls on top of the panels as well.
        cranks$renderPanelControls(context, mouseX, mouseY, delta);
    }

    @Unique
    private void cranks$renderPanelControls(DrawContext context, int mouseX, int mouseY, float delta) {
        if (cranks$focusSelector != null && cranks$focusSelector.visible) {
            cranks$focusSelector.render(context, mouseX, mouseY, delta);
        }
        if (cranks$mateSelector != null && cranks$mateSelector.visible) {
            cranks$mateSelector.render(context, mouseX, mouseY, delta);
        }
        if (cranks$accessSelector != null && cranks$accessSelector.visible) {
            cranks$accessSelector.render(context, mouseX, mouseY, delta);
        }
        for (ButtonWidget button : cranks$specializationButtons) {
            if (button.visible) button.render(context, mouseX, mouseY, delta);
        }
        if (cranks$releaseOwnershipButton != null && cranks$releaseOwnershipButton.visible) {
            cranks$releaseOwnershipButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Unique
    private void cranks$drawOtherMountInformation(DrawContext context) {
        cranks$resolveTextures();
        HorseScreen screen = (HorseScreen) (Object) this;
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        MountDataSyncPacket data = MountClientData.get(entity.getId());
        MountKind kind = MountKind.of(entity);
        boolean canReleaseOwnership = data != null && cranks$isCurrentOwner(data.ownerUuid());
        List<StatLine> stats = new ArrayList<>();
        List<StatLine> genes = new ArrayList<>();
        List<StatLine> breeding = new ArrayList<>();
        if (data == null) {
            stats.addAll(cranks$syncingLines());
            if (kind.hasGenetics() || kind == MountKind.SKELETON_HORSE) {
                genes.addAll(cranks$syncingLines());
            }
        } else {
            String xp = data.experienceRequired() <= 0 ? "MAX"
                    : data.experience() + "/" + data.experienceRequired();
            stats.add(new StatLine("Level", data.level() + " " + xp, 0xFFFFD966));
            stats.add(new StatLine("Health", format(entity.getHealth()) + "/" + format(entity.getMaxHealth()), 0xFFB8B8B8));
            stats.add(new StatLine("Movement Speed", String.format(Locale.US, "%.1f blocks/s",
                    cranks$getLivingAttribute(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            * SPEED_TO_BLOCKS_PER_SECOND), 0xFFB8B8B8));
            cranks$addIfNonZero(stats, "Armor", cranks$getLivingAttribute(entity, EntityAttributes.GENERIC_ARMOR), "");
            stats.add(new StatLine("Cargo", data.cargoCapacity() + " slots", 0xFFA3D5FF));
            if (data.maximumResource() > 0.0) stats.add(new StatLine(cranks$resourceName(kind),
                    format(data.resource()) + "/" + format(data.maximumResource()),
                    data.abilityActive() ? 0xFFFFD966 : 0xFFB8B8B8));
            if (!"None".equals(data.trait())) stats.add(new StatLine("Trait", data.trait(), 0xFFA8E6A3));

            if (entity instanceof AbstractHorseEntity
                    && (data.tamed() || data.ownerUuid() != null)) {
                boolean canEdit = cranks$canEditAccess(data.ownerUuid(),
                        data.tamed() || data.ownerUuid() != null);
                if (canEdit) {
                    MountAccessMode accessMode = cranks$syncSelectedAccessMode(data.accessMode(), entity.getId());
                    stats.add(new StatLine("Mount Access", cranks$accessText(accessMode, true),
                            cranks$accessColor(accessMode)));
                    cranks$accessRowVisible = true;
                }
            }

            if (kind.hasGenetics()) {
                String[] labels = com.cranks.dungeons.mount.MountManager.geneLabels(kind);
                double[] values = kind == MountKind.LLAMA
                        ? new double[]{data.health(), data.speed(), data.power(), data.utility(), data.armor()}
                        : kind.hasArmorGene()
                        ? new double[]{data.health(), data.speed(), data.power(), data.endurance(),
                                data.utility(), data.armor()}
                        : new double[]{data.health(), data.speed(), data.power(), data.endurance(),
                                data.utility()};
                String overallGrade = MountGeneticRatings.overall(kind, values, data.generation());
                genes.add(new StatLine("Overall Grade", overallGrade, gradeColor(overallGrade)));
                genes.add(new StatLine("Sex", data.male() ? "Male" : "Female", 0xFFB8B8B8));
                for (int i = 0; i < labels.length; i++) {
                    String statValue = labels[i].equals("Pack Instinct")
                            ? String.format(Locale.US, "%.1f%%", values[i] * 100.0)
                            : labels[i].contains("Speed")
                            ? String.format(Locale.US, "%.1f blocks/s", values[i] * SPEED_TO_BLOCKS_PER_SECOND)
                            : format(values[i]);
                    String grade = MountGeneticRatings.grade(kind, i, values[i], data.generation());
                    genes.add(new StatLine(labels[i], statValue + " [" + grade + "]", gradeColor(grade),
                            cranks$geneProgress(kind, i, values[i])));
                }
            } else {
                if (kind == MountKind.SKELETON_HORSE) {
                    genes.add(new StatLine("Soul Bond", data.soulBond() + "/100", 0xFFA3D5FF));
                    genes.add(new StatLine("Specialization", data.specialization(), 0xFFFFD966));
                }
            }

            if (kind != MountKind.SKELETON_HORSE) {
                breeding.add(new StatLine("Generation", Integer.toString(data.generation()), 0xFFB8B8B8));
                breeding.add(new StatLine("Attempts", Integer.toString(data.breedsRemaining()),
                        data.breedsRemaining() > 0 ? 0xFFA8E6A3 : 0xFFFF8A80));
                breeding.add(new StatLine("Cooldown", formatTime(data.breedingCooldownTicks()), 0xFFB8B8B8));
                if (kind == MountKind.MULE) breeding.add(new StatLine("Status", "Sterile hybrid", 0xFFFFD966));
            } else {
                if (data.soulBond() >= 10 && "None".equals(data.specialization())) {
                    breeding.add(new StatLine("Choose one permanently", "", 0xFFFFD966));
                    breeding.add(new StatLine("", "", 0xFFFFFFFF));
                }
            }
        }

        int baseX = accessor.getX();
        int baseY = accessor.getY();
        int leftX = Math.max(4, baseX - PANEL_WIDTH - PANEL_GAP);
        // Keep this anchor tied to the visible horse UI, not the donkey's
        // full genetic cargo capacity (which is represented by scrolling).
        int inventoryWidth = accessor.getBackgroundWidth();
        int rightX = Math.min(screen.width - PANEL_WIDTH - 4,
                baseX + inventoryWidth + PANEL_GAP);
        int genesHeight = genes.isEmpty() ? 0 : cranks$panelHeight(genes);
        int breedingHeight = breeding.isEmpty() ? 0 : cranks$panelHeight(breeding);
        int rightHeight = genesHeight + (genesHeight > 0 && breedingHeight > 0 ? PANEL_GAP : 0)
                + breedingHeight;
        int rightY = Math.max(4, Math.min(baseY, screen.height - rightHeight - 4) - 3);
        int releaseButtonSpace = canReleaseOwnership ? 22 : 0;
        int leftY = Math.max(4, Math.min(baseY,
                screen.height - cranks$panelHeight(stats) - releaseButtonSpace - 4) - 3);
        int breedingY = rightY + genesHeight + (genesHeight > 0 ? PANEL_GAP : 0);
        cranks$drawPanel(context, renderer, cranks$statsPanelTexture,
                kind.displayName() + " Stats", leftX, leftY, stats, releaseButtonSpace);
        if (canReleaseOwnership) {
            cranks$positionReleaseOwnershipButton(leftX, leftY, stats);
        }
        if (cranks$accessSelector != null && cranks$accessRowVisible && data != null) {
            boolean canEdit = cranks$canEditAccess(data.ownerUuid(), data.tamed());
            String accessText = cranks$accessText(cranks$selectedAccessMode, canEdit);
            cranks$positionSelector(cranks$accessSelector, renderer, leftX, leftY,
                    stats, stats.size() - 1, accessText);
            cranks$accessSelector.visible = canEdit;
            cranks$accessSelector.active = canEdit;
            cranks$accessSelector.setMessage(Text.literal("Mount access: "
                    + cranks$selectedAccessMode.displayName()));
        }
        if (!genes.isEmpty()) {
            cranks$drawPanel(context, renderer, cranks$genesPanelTexture,
                    kind.hasGenetics() ? "Genetics" : "Soul Bond", rightX, rightY, genes);
        }
        if (!breeding.isEmpty()) {
            cranks$drawPanel(context, renderer, cranks$familyPanelTexture,
                    kind == MountKind.SKELETON_HORSE ? "Specialization" : "Breeding",
                    rightX, breedingY, breeding);
        }
        if (data != null && kind == MountKind.SKELETON_HORSE && data.soulBond() >= 10
                && "None".equals(data.specialization()) && cranks$specializationButtons.size() == 3) {
            int buttonY = breedingY + cranks$panelHeight(breeding) - 19;
            for (int i = 0; i < cranks$specializationButtons.size(); i++) {
                ButtonWidget button = cranks$specializationButtons.get(i);
                button.setX(rightX + 10 + i * 47);
                button.setY(buttonY);
                button.visible = true;
                button.active = true;
            }
        }
    }

    @Unique
    private void cranks$ensureAccessSelector() {
        if (!(entity instanceof AbstractHorseEntity)) return;
        HorseScreen screen = (HorseScreen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) screen;
        if (cranks$accessSelector == null || !accessor.getChildren().contains(cranks$accessSelector)) {
            cranks$accessSelector = new HorseInlineSelectorWidget(
                    PANEL_WIDTH - 16, LINE_STEP + 2, Text.literal("Mount access"),
                    this::cranks$cycleAccessModeBy);
            cranks$accessSelector.visible = false;
            accessor.invokeAddDrawableChild(cranks$accessSelector);
        }
    }

    @Unique
    private void cranks$ensureReleaseOwnershipButton() {
        HorseScreen screen = (HorseScreen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) screen;
        if (cranks$releaseOwnershipButton != null
                && accessor.getChildren().contains(cranks$releaseOwnershipButton)) return;
        cranks$releaseOwnershipButton = ButtonWidget.builder(Text.literal("Release Mount"), button -> {
            if (ClientPlayNetworking.canSend(ReleaseMountPayload.ID)) {
                ClientPlayNetworking.send(new ReleaseMountPayload(entity.getId()));
            }
        }).dimensions(0, 0, 100, 16).build();
        cranks$releaseOwnershipButton.visible = false;
        accessor.invokeAddDrawableChild(cranks$releaseOwnershipButton);
    }

    @Unique
    private void cranks$positionReleaseOwnershipButton(int panelX, int panelY,
                                                       List<StatLine> lines) {
        if (cranks$releaseOwnershipButton == null) return;
        cranks$releaseOwnershipButton.setX(panelX + (PANEL_WIDTH - 100) / 2);
        cranks$releaseOwnershipButton.setY(panelY + cranks$panelHeight(lines) - 2);
        cranks$releaseOwnershipButton.visible = true;
        cranks$releaseOwnershipButton.active = true;
    }

    @Unique
    private static boolean cranks$isCurrentOwner(UUID ownerUuid) {
        MinecraftClient client = MinecraftClient.getInstance();
        return ownerUuid != null && client.player != null
                && ownerUuid.equals(client.player.getUuid());
    }

    @Unique
    private void cranks$cycleAccessModeBy(int direction) {
        cranks$selectedAccessMode = cranks$selectedAccessMode.cycle(direction);
        if (ClientPlayNetworking.canSend(SetMountAccessPayload.ID)) {
            ClientPlayNetworking.send(new SetMountAccessPayload(entity.getId(),
                    cranks$selectedAccessMode.name()));
        }
    }

    @Unique
    private MountAccessMode cranks$syncSelectedAccessMode(String serverMode, int entityId) {
        MountAccessMode received = MountAccessMode.fromId(serverMode);
        if (cranks$accessEntityId != entityId || cranks$lastServerAccessMode != received) {
            cranks$selectedAccessMode = received;
        }
        cranks$accessEntityId = entityId;
        cranks$lastServerAccessMode = received;
        return cranks$selectedAccessMode;
    }

    @Unique
    private static boolean cranks$canEditAccess(UUID ownerUuid, boolean tamed) {
        MinecraftClient client = MinecraftClient.getInstance();
        return tamed && client.player != null
                && (client.player.isCreative() || (ownerUuid != null
                && ownerUuid.equals(client.player.getUuid())));
    }

    @Unique
    private static String cranks$accessText(MountAccessMode mode, boolean editable) {
        return editable ? "< " + mode.displayName() + " >" : mode.displayName();
    }

    @Unique
    private static int cranks$accessColor(MountAccessMode mode) {
        return switch (mode) {
            case OWNER_ONLY -> 0xFFFF8A80;
            case PARTY -> 0xFF70D6FF;
            case EVERYONE -> 0xFFA8E6A3;
        };
    }

    @Unique
    private void cranks$ensureSpecializationButtons() {
        HorseScreen screen = (HorseScreen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) screen;
        if (!cranks$specializationButtons.isEmpty()
                && accessor.getChildren().containsAll(cranks$specializationButtons)) return;
        cranks$specializationButtons.clear();
        cranks$addSpecializationButton(accessor, "Tide", "Tidebound");
        cranks$addSpecializationButton(accessor, "Guard", "Graveguard");
        cranks$addSpecializationButton(accessor, "Storm", "Stormrunner");
    }

    @Unique
    private void cranks$addSpecializationButton(ScreenAccessor accessor, String label, String value) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(label), ignored -> {
            if (ClientPlayNetworking.canSend(SetMountSpecializationPayload.ID)) {
                ClientPlayNetworking.send(new SetMountSpecializationPayload(entity.getId(), value));
            }
        }).dimensions(0, 0, 45, 14).build();
        button.visible = false;
        cranks$specializationButtons.add(button);
        accessor.invokeAddDrawableChild(button);
    }

    @Unique private static String cranks$resourceName(MountKind kind) {
        return kind == MountKind.STRIDER ? "Heat" : "Endurance";
    }

    @Unique
    private void cranks$cycleFocusBy(int direction) {
        HorseBreedingFocus[] focuses = HorseBreedingFocus.values();
        int next = Math.floorMod(cranks$selectedFocus.ordinal() + direction, focuses.length);
        cranks$selectedFocus = focuses[next];
        if (cranks$focusSelector != null) cranks$focusSelector.setMessage(cranks$focusNarration());
        if (ClientPlayNetworking.canSend(SetHorseBreedingFocusPayload.ID)) {
            ClientPlayNetworking.send(new SetHorseBreedingFocusPayload(
                    entity.getId(), cranks$selectedFocus.name()));
        }
    }

    @Unique
    private void cranks$cycleMateBy(int direction) {
        if (cranks$nearbyMates.size() < 2) return;
        HorseEntity current = cranks$selectCurrentMate();
        int index = Math.max(0, cranks$nearbyMates.indexOf(current));
        int next = Math.floorMod(index + direction, cranks$nearbyMates.size());
        cranks$selectedMateId = cranks$nearbyMates.get(next).getUuid();
    }

    @Unique
    private HorseEntity cranks$selectCurrentMate() {
        if (cranks$nearbyMates.isEmpty()) {
            cranks$selectedMateId = null;
            return null;
        }
        if (cranks$selectedMateId != null) {
            for (HorseEntity candidate : cranks$nearbyMates) {
                if (candidate.getUuid().equals(cranks$selectedMateId)) return candidate;
            }
        }
        HorseEntity selected = cranks$nearbyMates.getFirst();
        cranks$selectedMateId = selected.getUuid();
        return selected;
    }

    @Unique
    private Text cranks$focusNarration() {
        return Text.literal("Breeding focus: " + cranks$selectedFocus.displayName());
    }

    @Unique
    private void cranks$syncSelectedFocus(HorseGeneticsSyncPacket data) {
        HorseBreedingFocus serverFocus = data == null
                ? HorseBreedingFocus.NONE : HorseBreedingFocus.fromId(data.breedingFocus());
        if (cranks$lastServerFocus != serverFocus) {
            cranks$lastServerFocus = serverFocus;
            cranks$selectedFocus = serverFocus;
        }
    }

    @Unique
    private static void cranks$resolveTextures() {
        if (cranks$texturesResolved) return;
        cranks$texturesResolved = true;
        cranks$statsPanelTexture = GuiTextureResolver.customOrVanilla(
                "horse/stats_panel_background", VANILLA_PANEL);
        cranks$genesPanelTexture = GuiTextureResolver.customOrVanilla(
                "horse/genes_panel_background", VANILLA_PANEL);
        cranks$familyPanelTexture = GuiTextureResolver.customOrVanilla(
                "horse/family_panel_background", VANILLA_PANEL);
        cranks$pairingPanelTexture = GuiTextureResolver.customOrVanilla(
                "horse/pairing_panel_background", VANILLA_PANEL);
    }

    @Unique
    private static int cranks$panelHeight(List<StatLine> lines) {
        int contentHeight = 0;
        for (StatLine line : lines) contentHeight += cranks$lineStep(line);
        return HEADER_HEIGHT + contentHeight + 9;
    }

    @Unique
    private static int cranks$lineStep(StatLine line) {
        return line.progress >= 0.0 ? GENETIC_LINE_STEP : LINE_STEP;
    }

    @Unique
    private static int cranks$lineOffset(List<StatLine> lines, int lineIndex) {
        int offset = 0;
        for (int i = 0; i < lineIndex && i < lines.size(); i++) {
            offset += cranks$lineStep(lines.get(i));
        }
        return offset;
    }

    @Unique
    private static void cranks$positionSelector(HorseInlineSelectorWidget selector,
                                                 TextRenderer renderer, int panelX, int panelY,
                                                 List<StatLine> lines, int lineIndex,
                                                 String selectorText) {
        float logicalWidth = (PANEL_WIDTH - 20) / TEXT_SCALE;
        int logicalX = (int) logicalWidth - renderer.getWidth(selectorText);
        int selectorX = panelX + 10 + Math.round(logicalX * TEXT_SCALE) - 2;
        int selectorWidth = Math.round(renderer.getWidth(selectorText) * TEXT_SCALE) + 4;
        selector.setX(selectorX);
        selector.setY(panelY + HEADER_HEIGHT + cranks$lineOffset(lines, lineIndex) - 1);
        selector.setWidth(selectorWidth);
    }

    @Unique
    private static String cranks$selectorText(TextRenderer renderer, String value) {
        int decorationWidth = renderer.getWidth("<  >");
        String trimmed = cranks$trimToWidth(renderer, value, Math.max(1, 100 - decorationWidth));
        return "< " + trimmed + " >";
    }

    @Unique
    private static void cranks$drawPanel(DrawContext context, TextRenderer renderer, Identifier texture,
                                         String title, int x, int y, List<StatLine> lines) {
        cranks$drawPanel(context, renderer, texture, title, x, y, lines, 0);
    }

    @Unique
    private static void cranks$drawPanel(DrawContext context, TextRenderer renderer, Identifier texture,
                                         String title, int x, int y, List<StatLine> lines,
                                         int extraHeight) {
        int height = cranks$panelHeight(lines) + extraHeight;
        context.drawGuiTexture(texture, x, y, PANEL_WIDTH, height);
        context.drawCenteredTextWithShadow(renderer, Text.literal(title), x + PANEL_WIDTH / 2, y + 10,
                0xFFFFD966);

        float logicalWidth = (PANEL_WIDTH - 20) / TEXT_SCALE;
        int lineY = y + HEADER_HEIGHT;
        for (StatLine line : lines) {
            context.getMatrices().push();
            context.getMatrices().translate(x + 10, lineY, 0.0f);
            context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);

            String value = cranks$trimToWidth(renderer, line.value, 100);
            int valueX = (int) logicalWidth - renderer.getWidth(value);
            String name = cranks$trimToWidth(renderer, line.name, Math.max(1, valueX - 5));
            int nameColor = line.color == 0xFFB8B8B8 ? 0xFFFFFFFF : line.color;
            context.drawText(renderer, Text.literal(name), 0, 0, nameColor, false);
            context.drawText(renderer, Text.literal(value), valueX, 0,
                    line.color == 0xFFB8B8B8 ? 0xFFFFFFFF : line.color, false);
            context.getMatrices().pop();
            if (line.progress >= 0.0) {
                int barLeft = x + 10;
                int barRight = x + PANEL_WIDTH - 10;
                int barTop = lineY + 7;
                context.fill(barLeft, barTop, barRight, barTop + GENETIC_BAR_HEIGHT,
                        0x55303030);
                int fillRight = barLeft + Math.round((barRight - barLeft)
                        * (float) Math.max(0.0, Math.min(1.0, line.progress)));
                if (fillRight > barLeft) {
                    context.fill(barLeft, barTop, fillRight, barTop + GENETIC_BAR_HEIGHT,
                            line.color);
                }
            }
            lineY += cranks$lineStep(line);
        }
    }

    @Unique
    private static List<StatLine> cranks$buildStats(HorseEntity horse, HorseGeneticsSyncPacket data) {
        List<StatLine> lines = new ArrayList<>();
        if (data == null) return cranks$syncingLines();

        String xp = data.experienceRequired() <= 0 ? "MAX"
                : data.experience() + "/" + data.experienceRequired();
        lines.add(new StatLine("Level", data.level() + " " + xp, 0xFFFFD966));
        lines.add(new StatLine("Health", format(horse.getHealth()) + "/" + format(horse.getMaxHealth()), 0xFFB8B8B8));
        lines.add(new StatLine("Movement Speed", String.format(Locale.US, "%.1f blocks/s",
                cranks$getAttribute(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        * SPEED_TO_BLOCKS_PER_SECOND), 0xFFB8B8B8));
        lines.add(new StatLine("Jump Strength", format(cranks$getAttribute(horse,
                EntityAttributes.GENERIC_JUMP_STRENGTH)), 0xFFB8B8B8));
        lines.add(new StatLine("Stamina", format(data.stamina()) + "/" + format(data.maxStamina()), 0xFFB8B8B8));
        lines.add(new StatLine("Mounted Recovery", format(data.mountedStaminaRegen()) + "/s", 0xFFB8B8B8));
        lines.add(new StatLine("Gallop", "+" + Math.round(data.gallopSpeedBonus() * 100.0) + "%", 0xFFB8B8B8));
        lines.add(new StatLine("Gallop Cost", format(data.gallopDrain()) + "/s", 0xFFB8B8B8));
        lines.add(new StatLine("Jump Cost", format(data.jumpStaminaCost()), 0xFFB8B8B8));
        cranks$addIfNonZero(lines, "Armor", cranks$getAttribute(horse, EntityAttributes.GENERIC_ARMOR), "");
        cranks$addIfNonZero(lines, "Evasion", cranks$getAttribute(horse, ModAttributes.EVASION_RATING), "");
        cranks$addIfNonZero(lines, "Energy Shield", cranks$getAttribute(horse, ModAttributes.ENERGY_SHIELD), "");
        cranks$addIfNonZero(lines, "Life Regeneration", cranks$getAttribute(horse, ModAttributes.LIFE_REGENERATION), "/s");
        cranks$addIfNonZero(lines, "Knockback Resistance",
                cranks$getAttribute(horse, ModAttributes.KNOCKBACK_RESISTANCE) * 100.0, "%");
        cranks$addTrainingMilestones(lines, data.level());
        return lines;
    }

    @Unique
    private static List<StatLine> cranks$buildGenes(HorseGeneticsSyncPacket data,
                                                     HorseBreedingFocus selectedFocus) {
        if (data == null) return cranks$syncingLines();
        List<StatLine> lines = new ArrayList<>();
        lines.add(new StatLine("Overall Grade", data.grade(), gradeColor(data.grade())));
        lines.add(new StatLine("Sex", data.male() ? "Male" : "Female", 0xFFB8B8B8));
        lines.add(cranks$geneLine("Health Bonus Gene", "+" + format(data.geneticHealth()), MountKind.HORSE,
                0, data.geneticHealth(), data.generation()));
        lines.add(cranks$geneLine("Speed Gene", String.format(Locale.US, "%.1f blocks/s",
                data.geneticSpeed() * SPEED_TO_BLOCKS_PER_SECOND), MountKind.HORSE,
                1, data.geneticSpeed(), data.generation()));
        lines.add(cranks$geneLine("Jump Gene", format(data.geneticJump()), MountKind.HORSE,
                2, data.geneticJump(), data.generation()));
        lines.add(cranks$geneLine("Evasion Gene", format(data.geneticEvasion()), MountKind.HORSE,
                3, data.geneticEvasion(), data.generation()));
        lines.add(cranks$geneLine("Endurance Gene", format(data.geneticEndurance()), MountKind.HORSE,
                4, data.geneticEndurance(), data.generation()));
        lines.add(cranks$geneLine("Armor Bonus Gene", "+" + format(data.geneticArmor()), MountKind.HORSE,
                5, data.geneticArmor(), data.generation()));
        lines.add(new StatLine("Trait", data.trait(), 0xFFA8E6A3));
        cranks$addTraitBonusLines(lines, data.trait());
        lines.add(new StatLine("Breeding Focus", cranks$focusDisplayText(selectedFocus),
                0xFFA3D5FF));
        return lines;
    }

    @Unique
    private static StatLine cranks$geneLine(String name, String value, MountKind kind,
                                             int geneIndex, double geneValue, int generation) {
        String grade = MountGeneticRatings.grade(kind, geneIndex, geneValue, generation);
        return new StatLine(name, value + " [" + grade + "]", gradeColor(grade),
                cranks$geneProgress(kind, geneIndex, geneValue));
    }

    @Unique
    private static double cranks$geneProgress(MountKind kind, int geneIndex, double value) {
        double minimum = MountGeneticRatings.breedingMinimum(kind, geneIndex);
        double maximum = MountGeneticRatings.breedingMaximum(kind, geneIndex);
        if (maximum <= minimum) return value >= maximum ? 1.0 : 0.0;
        return Math.max(0.0, Math.min(1.0, (value - minimum) / (maximum - minimum)));
    }

    @Unique
    private static String cranks$focusDisplayText(HorseBreedingFocus focus) {
        return "< " + focus.displayName() + " >";
    }

    @Unique
    private static void cranks$addTraitBonusLines(List<StatLine> lines, String trait) {
        switch (trait) {
            case "Swift" -> {
                lines.add(new StatLine("Movement Speed", "+5%", 0xFFA8E6A3));
                lines.add(new StatLine("Gallop Speed", "+5%", 0xFFA8E6A3));
                lines.add(new StatLine("Terrain Efficiency", "+10%", 0xFFA8E6A3));
            }
            case "Hardy" -> {
                lines.add(new StatLine("Maximum Health", "+10%", 0xFFA8E6A3));
                lines.add(new StatLine("Maximum Stamina", "+15%", 0xFFA8E6A3));
                lines.add(new StatLine("Stamina Recovery", "+25%", 0xFFA8E6A3));
            }
            case "Sure-Footed" -> {
                lines.add(new StatLine("Jump Strength", "+8%", 0xFFA8E6A3));
                lines.add(new StatLine("Knockback Resistance", "+15%", 0xFFA8E6A3));
                lines.add(new StatLine("Step Height", "+0.15", 0xFFA8E6A3));
                lines.add(new StatLine("Safe Fall Distance", "+2", 0xFFA8E6A3));
                lines.add(new StatLine("Jump Stamina Cost", "-2", 0xFFA8E6A3));
            }
            case "Warhorse" -> {
                lines.add(new StatLine("Armor", "+30% of base", 0xFFA8E6A3));
                lines.add(new StatLine("Evasion", "+20", 0xFFA8E6A3));
                lines.add(new StatLine("Knockback Resistance", "+20%", 0xFFA8E6A3));
            }
            default -> {
            }
        }
    }

    @Unique
    private static List<StatLine> cranks$buildFamily(HorseEntity horse, HorseGeneticsSyncPacket data) {
        if (data == null) return cranks$syncingLines();
        List<StatLine> lines = new ArrayList<>();
        lines.add(new StatLine("Generation", Integer.toString(data.generation()), 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Attempts", data.breedsRemaining() + "/3", 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Cooldown", formatTime(data.breedingCooldownTicks()), 0xFFB8B8B8));
        if (data.imprintProgress() > 0.0 || !"None".equals(data.imprintOwnerName())) {
            lines.add(new StatLine("Imprint", Math.round(data.imprintProgress() * 100)
                    + "% - " + data.imprintOwnerName(), 0xFFA3D5FF));
        }
        if (horse.isBaby()) {
            lines.add(new StatLine("Next Care", formatTime(data.careCooldownTicks())
                    + " (Sugar)", 0xFFA3D5FF));
        }
        return lines;
    }

    @Unique
    private static List<HorseEntity> cranks$findNearbyMates(HorseEntity horse,
                                                            HorseGeneticsSyncPacket data) {
        List<HorseEntity> candidates = horse.getEntityWorld().getEntitiesByClass(
                HorseEntity.class, horse.getBoundingBox().expand(8.0),
                candidate -> candidate != horse && candidate.isTame() && !candidate.isBaby());
        candidates.sort((first, second) -> {
            int firstPriority = cranks$matePriority(horse, data, first);
            int secondPriority = cranks$matePriority(horse, data, second);
            if (firstPriority != secondPriority) return Integer.compare(firstPriority, secondPriority);
            return Double.compare(horse.squaredDistanceTo(first), horse.squaredDistanceTo(second));
        });
        return List.copyOf(candidates);
    }

    @Unique
    private static int cranks$matePriority(HorseEntity horse, HorseGeneticsSyncPacket data,
                                            HorseEntity candidate) {
        HorseGeneticsSyncPacket candidateData = HorseGeneticsClientData.get(candidate.getId());
        if (data == null || candidateData == null) return 3;
        if (cranks$pairingBlockReason(horse, data, candidate, candidateData) == null) return 0;
        return data.male() != candidateData.male() ? 1 : 2;
    }

    @Unique
    private static List<StatLine> cranks$buildPairingPreview(
            HorseEntity horse, HorseGeneticsSyncPacket data,
            HorseEntity mate, HorseGeneticsSyncPacket mateData, String selectorText,
            int selectedIndex, int candidateCount) {
        List<StatLine> lines = new ArrayList<>();
        String mateLabel = candidateCount > 1
                ? "Mate " + (selectedIndex + 1) + "/" + candidateCount : "Mate";
        lines.add(new StatLine(mateLabel,
                candidateCount > 1 ? selectorText : mate.getName().getString(), 0xFFA3D5FF));
        if (data == null || mateData == null) {
            lines.addAll(cranks$syncingLines());
            return lines;
        }

        String blocked = cranks$pairingBlockReason(horse, data, mate, mateData);
        lines.add(new StatLine("Compatibility", blocked == null ? "Ready" : blocked,
                blocked == null ? 0xFFA8E6A3 : 0xFFFF8A80));
        HorseBreedingFocus firstFocus = HorseBreedingFocus.fromId(data.breedingFocus());
        HorseBreedingFocus secondFocus = HorseBreedingFocus.fromId(mateData.breedingFocus());
        cranks$addPairGene(lines, "Health", data.geneticHealth(), mateData.geneticHealth(),
                cranks$isFocused(firstFocus, secondFocus, 0), false);
        cranks$addPairGene(lines, "Speed", data.geneticSpeed(), mateData.geneticSpeed(),
                cranks$isFocused(firstFocus, secondFocus, 1), true);
        cranks$addPairGene(lines, "Jump", data.geneticJump(), mateData.geneticJump(),
                cranks$isFocused(firstFocus, secondFocus, 2), false);
        cranks$addPairGene(lines, "Evasion", data.geneticEvasion(), mateData.geneticEvasion(),
                cranks$isFocused(firstFocus, secondFocus, 3), false);
        cranks$addPairGene(lines, "Endurance", data.geneticEndurance(), mateData.geneticEndurance(),
                cranks$isFocused(firstFocus, secondFocus, 4), false);
        cranks$addPairGene(lines, "Armor Bonus", data.geneticArmor(), mateData.geneticArmor(),
                cranks$isFocused(firstFocus, secondFocus, 5), false);
        if (firstFocus != HorseBreedingFocus.NONE || secondFocus != HorseBreedingFocus.NONE) {
            lines.add(new StatLine("* Focus", "Guaranteed better", 0xFFA8E6A3));
        }
        cranks$addLineagePreview(lines, data, mateData, firstFocus, secondFocus);
        cranks$addTraitPreview(lines, data.trait(), mateData.trait());
        return lines;
    }

    @Unique
    private static String cranks$pairingBlockReason(
            HorseEntity horse, HorseGeneticsSyncPacket data,
            HorseEntity mate, HorseGeneticsSyncPacket mateData) {
        if (horse.isBaby()) return "Current is a foal";
        if (!horse.isTame()) return "Current is untamed";
        if (data.male() == mateData.male()) return "Same sex";
        if (data.breedsRemaining() <= 0) return "No attempts left";
        if (mateData.breedsRemaining() <= 0) return "Mate has no attempts";
        if (data.breedingCooldownTicks() > 0) return "Current cooldown";
        if (mateData.breedingCooldownTicks() > 0) return "Mate cooldown";
        if (cranks$areRelated(horse.getUuid(), data.ancestry(), mate.getUuid(), mateData.ancestry())) {
            return "Related bloodlines";
        }
        return null;
    }

    @Unique
    private static boolean cranks$areRelated(UUID firstId, List<UUID> firstAncestry,
                                              UUID secondId, List<UUID> secondAncestry) {
        if (firstId.equals(secondId)) return true;
        if (firstAncestry.contains(secondId) || secondAncestry.contains(firstId)) return true;
        for (UUID ancestor : firstAncestry) if (secondAncestry.contains(ancestor)) return true;
        return false;
    }

    @Unique
    private static void cranks$addPairGene(List<StatLine> lines, String name,
                                           double first, double second,
                                           boolean guaranteed, boolean precise) {
        double lower = Math.min(first, second);
        double upper = Math.max(first, second);
        String firstValue = precise
                ? String.format(Locale.US, "%.1f", lower * SPEED_TO_BLOCKS_PER_SECOND)
                : format(lower);
        String secondValue = precise
                ? String.format(Locale.US, "%.1f", upper * SPEED_TO_BLOCKS_PER_SECOND)
                : format(upper);
        String values = Math.abs(first - second) <= ZERO_THRESHOLD
                ? firstValue : firstValue + " - " + secondValue;
        lines.add(new StatLine(name + (guaranteed ? " *" : ""), values,
                guaranteed ? 0xFFA8E6A3 : 0xFFB8B8B8));
    }

    @Unique
    private static boolean cranks$isFocused(HorseBreedingFocus first,
                                             HorseBreedingFocus second, int gene) {
        return cranks$focusIndex(first) == gene || cranks$focusIndex(second) == gene;
    }

    @Unique
    private static void cranks$addLineagePreview(List<StatLine> lines,
                                                  HorseGeneticsSyncPacket firstData,
                                                  HorseGeneticsSyncPacket secondData,
                                                  HorseBreedingFocus firstFocus,
                                                  HorseBreedingFocus secondFocus) {
        int firstIndex = cranks$focusIndex(firstFocus);
        int secondIndex = cranks$focusIndex(secondFocus);
        if (firstIndex < 0 && secondIndex < 0) {
            lines.add(new StatLine("Lineage Gain", "Random +0.5-1.5%", 0xFFFFD966));
            return;
        }
        if (firstIndex >= 0 && secondIndex >= 0 && firstIndex != secondIndex) {
            lines.add(new StatLine("Lineage Option", cranks$lineageText(
                    firstIndex, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
            lines.add(new StatLine("Or", cranks$lineageText(
                    secondIndex, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
            return;
        }
        int target = firstIndex >= 0 ? firstIndex : secondIndex;
        lines.add(new StatLine("Lineage Gain", cranks$lineageText(
                target, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
    }

    @Unique
    private static String cranks$lineageText(int gene, HorseGeneticsSyncPacket first,
                                             HorseGeneticsSyncPacket second,
                                             HorseBreedingFocus firstFocus,
                                             HorseBreedingFocus secondFocus) {
        double better = Math.max(cranks$gene(first, gene), cranks$gene(second, gene));
        double quality = (better - cranks$geneMin(gene))
                / (cranks$geneMax(gene) - cranks$geneMin(gene));
        double rate = HorseBreedingRules.lineageImprovement(firstFocus, secondFocus, quality);
        return cranks$geneName(gene) + " +" + formatPercent(rate * 100.0) + "%";
    }

    @Unique
    private static void cranks$addTraitPreview(List<StatLine> lines, String first, String second) {
        if (first.equals(second)) {
            lines.add(new StatLine("Trait",
                    "No Trait".equals(first) ? "No Trait" : first + " 100%", 0xFFA8E6A3));
        } else if ("No Trait".equals(first) || "No Trait".equals(second)) {
            String positive = "No Trait".equals(first) ? second : first;
            lines.add(new StatLine("Trait", positive + " 70%", 0xFFA8E6A3));
            lines.add(new StatLine("No Trait", "30%", 0xFFB8B8B8));
        } else {
            lines.add(new StatLine(first, "45%", 0xFFA8E6A3));
            lines.add(new StatLine(second, "45%", 0xFFA8E6A3));
            lines.add(new StatLine("No Trait", "10%", 0xFFB8B8B8));
        }
        lines.add(new StatLine("Trait Mutation", "3% random", 0xFFA3D5FF));
    }

    @Unique
    private static int cranks$focusIndex(HorseBreedingFocus focus) {
        return switch (focus) {
            case HEALTH -> 0;
            case SPEED -> 1;
            case JUMP -> 2;
            case EVASION -> 3;
            case ENDURANCE -> 4;
            case ARMOR -> 5;
            case NONE -> -1;
        };
    }

    @Unique
    private static double cranks$gene(HorseGeneticsSyncPacket data, int gene) {
        return switch (gene) {
            case 0 -> data.geneticHealth();
            case 1 -> data.geneticSpeed();
            case 2 -> data.geneticJump();
            case 3 -> data.geneticEvasion();
            case 4 -> data.geneticEndurance();
            default -> data.geneticArmor();
        };
    }

    @Unique
    private static double cranks$geneMin(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MIN_HEALTH;
            case 1 -> HorseBreedingRules.MIN_SPEED;
            case 2 -> HorseBreedingRules.MIN_JUMP;
            case 3 -> HorseBreedingRules.MIN_EVASION;
            case 4 -> HorseBreedingRules.MIN_ENDURANCE;
            default -> HorseBreedingRules.MIN_ARMOR;
        };
    }

    @Unique
    private static double cranks$geneMax(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MAX_HEALTH;
            case 1 -> HorseBreedingRules.MAX_SPEED;
            case 2 -> HorseBreedingRules.MAX_JUMP;
            case 3 -> HorseBreedingRules.MAX_EVASION;
            case 4 -> HorseBreedingRules.MAX_ENDURANCE;
            default -> HorseBreedingRules.MAX_ARMOR;
        };
    }

    @Unique
    private static String cranks$geneName(int gene) {
        return switch (gene) {
            case 0 -> "Health";
            case 1 -> "Speed";
            case 2 -> "Jump";
            case 3 -> "Evasion";
            case 4 -> "Endurance";
            default -> "Armor";
        };
    }

    @Unique
    private static String formatPercent(double value) {
        return Math.abs(value - Math.rint(value)) < ZERO_THRESHOLD
                ? Integer.toString((int) Math.rint(value))
                : String.format(Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Unique
    private static List<StatLine> cranks$syncingLines() {
        return List.of(new StatLine("Status", "Syncing...", 0xFFB8B8B8));
    }

    @Unique
    private static void cranks$addTrainingMilestones(List<StatLine> lines, int level) {
        if (level >= 5) lines.add(new StatLine("Lv. 5 Perk", "Trail Training", 0xFFA3D5FF));
        if (level >= 10) lines.add(new StatLine("Lv. 10 Perk", "Second Wind", 0xFFA3D5FF));
        if (level >= 15) lines.add(new StatLine("Lv. 15 Perk", "Efficient Jumper", 0xFFA3D5FF));
        if (level >= 20) lines.add(new StatLine("Lv. 20 Perk", "Sure Stride", 0xFFA3D5FF));
        if (level >= 25) lines.add(new StatLine("Lv. 25 Perk", "Endurance Training", 0xFFA3D5FF));
        if (level >= 30) lines.add(new StatLine("Lv. 30 Perk", "Mastered Gallop", 0xFFA3D5FF));
    }

    @Unique
    private static double cranks$getAttribute(HorseEntity horse, RegistryEntry<EntityAttribute> attribute) {
        var instance = horse.getAttributeInstance(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    @Unique
    private static double cranks$getLivingAttribute(net.minecraft.entity.LivingEntity entity,
                                                     RegistryEntry<EntityAttribute> attribute) {
        var instance = entity.getAttributeInstance(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }

    @Unique
    private static void cranks$addIfNonZero(List<StatLine> lines, String name, double value, String suffix) {
        if (Math.abs(value) > ZERO_THRESHOLD) {
            lines.add(new StatLine(name, format(value) + suffix, 0xFFB8B8B8));
        }
    }

    @Unique
    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < ZERO_THRESHOLD) return Integer.toString((int) Math.rint(value));
        if (Math.abs(value) < 10.0) return String.format(Locale.US, "%.2f", value);
        return String.format(Locale.US, "%.1f", value);
    }

    @Unique
    private static String formatTime(long ticks) {
        if (ticks <= 0) return "Ready";
        long seconds = ticks / 20;
        return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60);
    }

    @Unique
    private static int gradeColor(String grade) {
        if (grade == null || grade.isEmpty()) return 0xFFFFFFFF;
        return switch (grade.charAt(0)) {
            case 'S' -> 0xFFFF70FF;
            case 'A' -> 0xFFFFD966;
            case 'B' -> 0xFF70D6FF;
            case 'C' -> 0xFF9BE28F;
            case 'D' -> 0xFFFF8A80;
            default -> 0xFFFFFFFF;
        };
    }

    @Unique
    private static String cranks$trimToWidth(TextRenderer renderer, String value, int maxWidth) {
        if (renderer.getWidth(value) <= maxWidth) return value;
        return renderer.trimToWidth(value, Math.max(1, maxWidth - renderer.getWidth("..."))) + "...";
    }

}

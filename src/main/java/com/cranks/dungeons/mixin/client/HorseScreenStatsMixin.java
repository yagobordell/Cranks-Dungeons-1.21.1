package com.cranks.dungeons.mixin.client;
import static com.cranks.dungeons.client.HorseInformation.*;

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

        List<StatLine> stats = buildStats(horse, data);
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
        List<StatLine> genes = buildGenes(data, cranks$selectedFocus);
        List<StatLine> family = buildFamily(horse, data);
        cranks$nearbyMates = findNearbyMates(horse, data);
        HorseEntity mate = cranks$selectCurrentMate();
        HorseGeneticsSyncPacket mateData = mate == null ? null : HorseGeneticsClientData.get(mate.getId());
        int mateIndex = mate == null ? -1 : cranks$nearbyMates.indexOf(mate);
        String mateSelectorText = mate == null ? "" : cranks$selectorText(
                renderer, mate.getName().getString());
        List<StatLine> pairing = mate == null ? List.of() : buildPairingPreview(
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
            String selectorText = focusDisplayText(cranks$selectedFocus);
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
            stats.addAll(syncingLines());
            if (kind.hasGenetics() || kind == MountKind.SKELETON_HORSE) {
                genes.addAll(syncingLines());
            }
        } else {
            String xp = data.experienceRequired() <= 0 ? "MAX"
                    : data.experience() + "/" + data.experienceRequired();
            stats.add(new StatLine("Level", data.level() + " " + xp, 0xFFFFD966));
            stats.add(new StatLine("Health", format(entity.getHealth()) + "/" + format(entity.getMaxHealth()), 0xFFB8B8B8));
            stats.add(new StatLine("Movement Speed", String.format(Locale.US, "%.1f blocks/s",
                    getLivingAttribute(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            * SPEED_TO_BLOCKS_PER_SECOND), 0xFFB8B8B8));
            addIfNonZero(stats, "Armor", getLivingAttribute(entity, EntityAttributes.GENERIC_ARMOR), "");
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
                            geneProgress(kind, i, values[i])));
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
        String trimmed = trimToWidth(renderer, value, Math.max(1, 100 - decorationWidth));
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

            String value = trimToWidth(renderer, line.value, 100);
            int valueX = (int) logicalWidth - renderer.getWidth(value);
            String name = trimToWidth(renderer, line.name, Math.max(1, valueX - 5));
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

}

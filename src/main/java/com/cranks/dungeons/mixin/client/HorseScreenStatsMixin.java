package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.GuiTextureResolver;
import com.cranks.dungeons.client.HorseInlineSelectorWidget;
import com.cranks.dungeons.client.HorseGeneticsClientData;
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
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.client.MountClientData;
import com.cranks.dungeons.network.MountDataSyncPacket;
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
    @Unique private static Identifier cranks$birthPanelTexture = VANILLA_PANEL;
    @Unique private static boolean cranks$texturesResolved;
    @Unique private HorseBreedingFocus cranks$selectedFocus = HorseBreedingFocus.NONE;
    @Unique private HorseBreedingFocus cranks$lastServerFocus;
    @Unique private boolean cranks$focusRowVisible;
    @Unique private HorseInlineSelectorWidget cranks$focusSelector;
    @Unique private HorseInlineSelectorWidget cranks$mateSelector;
    @Unique private List<HorseEntity> cranks$nearbyMates = List.of();
    @Unique private UUID cranks$selectedMateId;
    @Unique private final List<ButtonWidget> cranks$specializationButtons = new ArrayList<>();

    @Shadow @Final private AbstractHorseEntity entity;

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
        if (cranks$mateSelector != null) cranks$mateSelector.visible = false;
        for (ButtonWidget button : cranks$specializationButtons) button.visible = false;
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
        List<StatLine> genes = cranks$buildGenes(data, cranks$selectedFocus);
        List<StatLine> family = cranks$buildFamily(horse, data);
        List<StatLine> birth = cranks$buildBirthRecord(data);
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
        int statsHeight = cranks$panelHeight(stats);
        int pairingHeight = pairing.isEmpty() ? 0 : cranks$panelHeight(pairing);
        int genesHeight = cranks$panelHeight(genes);
        int familyHeight = cranks$panelHeight(family);
        int birthHeight = cranks$panelHeight(birth);
        int leftStackHeight = statsHeight
                + (pairing.isEmpty() ? 0 : PANEL_GAP + pairingHeight);
        int rightStackHeight = genesHeight + PANEL_GAP + familyHeight + PANEL_GAP + birthHeight;
        int leftY = Math.max(4, Math.min(baseY, screen.height - leftStackHeight - 4) - 3);
        int rightY = Math.max(4, Math.min(baseY, screen.height - rightStackHeight - 4) - 3);
        int familyY = rightY + genesHeight + PANEL_GAP;
        int birthY = familyY + familyHeight + PANEL_GAP;

        cranks$drawPanel(context, renderer, cranks$statsPanelTexture, "Horse Stats",
                statsX, leftY, stats);
        if (!pairing.isEmpty()) {
            cranks$drawPanel(context, renderer, cranks$pairingPanelTexture, "Pairing Preview",
                    statsX, leftY + statsHeight + PANEL_GAP, pairing);
            if (cranks$mateSelector != null && cranks$nearbyMates.size() > 1) {
                int pairingY = leftY + statsHeight + PANEL_GAP;
                cranks$positionSelector(cranks$mateSelector, renderer, statsX, pairingY,
                        0, mateSelectorText);
                cranks$mateSelector.visible = true;
                cranks$mateSelector.setMessage(Text.literal("Pairing candidate "
                        + (mateIndex + 1) + " of " + cranks$nearbyMates.size() + ": "
                        + mate.getName().getString()));
            }
        }
        cranks$drawPanel(context, renderer, cranks$genesPanelTexture, "Genetics",
                rightX, rightY, genes);
        cranks$drawPanel(context, renderer, cranks$familyPanelTexture, "Family",
                rightX, familyY, family);
        cranks$drawPanel(context, renderer, cranks$birthPanelTexture, "Birth Record",
                rightX, birthY, birth);

        cranks$focusRowVisible = data != null;
        if (cranks$focusSelector != null) {
            String selectorText = cranks$focusDisplayText(cranks$selectedFocus);
            cranks$positionSelector(cranks$focusSelector, renderer, rightX, rightY,
                    genes.size() - 1, selectorText);
            cranks$focusSelector.visible = cranks$focusRowVisible;
            cranks$focusSelector.setMessage(cranks$focusNarration());
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
        for (ButtonWidget button : cranks$specializationButtons) {
            if (button.visible) button.render(context, mouseX, mouseY, delta);
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
        List<StatLine> stats = new ArrayList<>();
        List<StatLine> genes = new ArrayList<>();
        List<StatLine> breeding = new ArrayList<>();
        if (data == null) {
            stats.addAll(cranks$syncingLines());
            genes.addAll(cranks$syncingLines());
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

            if (kind.hasGenetics()) {
                String[] labels = com.cranks.dungeons.mount.MountManager.geneLabels(kind);
                double[] values = {data.health(), data.speed(), data.power(), data.endurance(), data.utility()};
                genes.add(new StatLine("Sex", data.male() ? "Male" : "Female", 0xFFB8B8B8));
                for (int i = 0; i < labels.length; i++) {
                    String value = labels[i].contains("Speed")
                            ? String.format(Locale.US, "%.1f blocks/s", values[i] * SPEED_TO_BLOCKS_PER_SECOND)
                            : format(values[i]);
                    genes.add(new StatLine(labels[i], value, 0xFFB8B8B8));
                }
            } else {
                genes.add(new StatLine("Travel Training", "2 XP / 10 blocks", 0xFFB8B8B8));
                genes.add(new StatLine("Maximum Level", "30", 0xFFB8B8B8));
                if (kind == MountKind.SKELETON_HORSE) {
                    genes.add(new StatLine("Soul Bond", data.soulBond() + "/100", 0xFFA3D5FF));
                    genes.add(new StatLine("Specialization", data.specialization(), 0xFFFFD966));
                }
            }

            if (kind != MountKind.SKELETON_HORSE) {
                breeding.add(new StatLine("Generation", Integer.toString(data.generation()), 0xFFB8B8B8));
                breeding.add(new StatLine("Lineage Mutations", Integer.toString(data.mutations()), 0xFFB8B8B8));
                if (!"Wild".equals(data.sireName())) breeding.add(new StatLine("Sire", data.sireName(), 0xFFB8B8B8));
                if (!"Wild".equals(data.damName())) breeding.add(new StatLine("Dam", data.damName(), 0xFFB8B8B8));
                breeding.add(new StatLine("Attempts", Integer.toString(data.breedsRemaining()),
                        data.breedsRemaining() > 0 ? 0xFFA8E6A3 : 0xFFFF8A80));
                breeding.add(new StatLine("Cooldown", formatTime(data.breedingCooldownTicks()), 0xFFB8B8B8));
                breeding.add(new StatLine("Purpose", cranks$roleName(kind), 0xFFA3D5FF));
                if (kind == MountKind.MULE) breeding.add(new StatLine("Breeding", "Sterile hybrid", 0xFFFFD966));
            } else {
                breeding.add(new StatLine("Purpose", cranks$roleName(kind), 0xFFA3D5FF));
                breeding.add(new StatLine("Soul Bond", data.soulBond() + "/100", 0xFFA3D5FF));
                breeding.add(new StatLine("Specialization", data.specialization(), 0xFFFFD966));
                if (data.soulBond() >= 10 && "None".equals(data.specialization())) {
                    breeding.add(new StatLine("Choose one permanently", "", 0xFFFFD966));
                    breeding.add(new StatLine("Tidebound", "Water travel", 0xFFA3D5FF));
                    breeding.add(new StatLine("Graveguard", "Defence", 0xFFA8E6A3));
                    breeding.add(new StatLine("Stormrunner", "Speed", 0xFFFFD966));
                    breeding.add(new StatLine("", "", 0xFFFFFFFF));
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
        int rightHeight = cranks$panelHeight(genes) + PANEL_GAP + cranks$panelHeight(breeding);
        int rightY = Math.max(4, Math.min(baseY, screen.height - rightHeight - 4) - 3);
        int leftY = Math.max(4, Math.min(baseY, screen.height - cranks$panelHeight(stats) - 4) - 3);
        cranks$drawPanel(context, renderer, cranks$statsPanelTexture,
                kind.displayName() + " Stats", leftX, leftY, stats);
        cranks$drawPanel(context, renderer, cranks$genesPanelTexture,
                kind.hasGenetics() ? "Genetics" : "Training", rightX, rightY, genes);
        cranks$drawPanel(context, renderer, cranks$familyPanelTexture,
                kind == MountKind.SKELETON_HORSE ? "Soul Bond" : "Breeding",
                rightX, rightY + cranks$panelHeight(genes) + PANEL_GAP, breeding);
        if (data != null && kind == MountKind.SKELETON_HORSE && data.soulBond() >= 10
                && "None".equals(data.specialization()) && cranks$specializationButtons.size() == 3) {
            int panelY = rightY + cranks$panelHeight(genes) + PANEL_GAP;
            int buttonY = panelY + cranks$panelHeight(breeding) - 19;
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

    @Unique private static String cranks$roleName(MountKind kind) {
        return switch (kind) {
            case DONKEY -> "Pack logistics";
            case MULE -> "Hybrid expedition";
            case CAMEL -> "Two-rider journey";
            case STRIDER -> "Lava travel";
            case SKELETON_HORSE -> "Undead specialist";
            default -> "Mobility";
        };
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
        cranks$birthPanelTexture = GuiTextureResolver.customOrVanilla(
                "horse/birth_record_panel_background", VANILLA_PANEL);
    }

    @Unique
    private static int cranks$panelHeight(List<StatLine> lines) {
        return HEADER_HEIGHT + lines.size() * LINE_STEP + 9;
    }

    @Unique
    private static void cranks$positionSelector(HorseInlineSelectorWidget selector,
                                                 TextRenderer renderer, int panelX, int panelY,
                                                 int lineIndex, String selectorText) {
        float logicalWidth = (PANEL_WIDTH - 20) / TEXT_SCALE;
        int logicalX = (int) logicalWidth - renderer.getWidth(selectorText);
        int selectorX = panelX + 10 + Math.round(logicalX * TEXT_SCALE) - 2;
        int selectorWidth = Math.round(renderer.getWidth(selectorText) * TEXT_SCALE) + 4;
        selector.setX(selectorX);
        selector.setY(panelY + HEADER_HEIGHT + lineIndex * LINE_STEP - 1);
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
        int height = cranks$panelHeight(lines);
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
            lineY += LINE_STEP;
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
        if (data.galloping()) lines.add(new StatLine("Travel State", data.onRoad() ? "Road Gallop" : "Galloping", 0xFFFFD966));
        else if (data.exhausted()) lines.add(new StatLine("Travel State", "Recovering", 0xFFFF8A80));
        else if (data.onRoad()) lines.add(new StatLine("Travel State", "On Road", 0xFFA8E6A3));
        cranks$addIfNonZero(lines, "Armor", cranks$getAttribute(horse, EntityAttributes.GENERIC_ARMOR), "");
        cranks$addIfNonZero(lines, "Evasion", cranks$getAttribute(horse, ModAttributes.EVASION_RATING), "");
        cranks$addIfNonZero(lines, "Energy Shield", cranks$getAttribute(horse, ModAttributes.ENERGY_SHIELD), "");
        cranks$addIfNonZero(lines, "Life Regeneration", cranks$getAttribute(horse, ModAttributes.LIFE_REGENERATION), "/s");
        cranks$addIfNonZero(lines, "Knockback Resistance",
                cranks$getAttribute(horse, ModAttributes.KNOCKBACK_RESISTANCE) * 100.0, "%");
        if (!"No Trait".equals(data.trait())) lines.add(new StatLine("Trait", data.trait(), 0xFFA8E6A3));
        if (data.imprintProgress() > 0) lines.add(new StatLine("Imprint",
                Math.round(data.imprintProgress() * 100) + "%", 0xFFA3D5FF));
        cranks$addTrainingMilestones(lines, data.level());
        return lines;
    }

    @Unique
    private static List<StatLine> cranks$buildGenes(HorseGeneticsSyncPacket data,
                                                     HorseBreedingFocus selectedFocus) {
        if (data == null) return cranks$syncingLines();
        List<StatLine> lines = new ArrayList<>();
        lines.add(new StatLine("Overall Grade", data.grade(), gradeColor(data.grade())));
        lines.add(new StatLine("Mobility Grade", data.mobilityGrade(), gradeColor(data.mobilityGrade())));
        lines.add(new StatLine("Survival Grade", data.survivalGrade(), gradeColor(data.survivalGrade())));
        lines.add(new StatLine("Sex", data.male() ? "Male" : "Female", 0xFFB8B8B8));
        lines.add(new StatLine("Health Gene", format(data.geneticHealth()), 0xFFB8B8B8));
        lines.add(new StatLine("Speed Gene", String.format(Locale.US, "%.1f blocks/s",
                data.geneticSpeed() * SPEED_TO_BLOCKS_PER_SECOND), 0xFFB8B8B8));
        lines.add(new StatLine("Jump Gene", format(data.geneticJump()), 0xFFB8B8B8));
        lines.add(new StatLine("Evasion Gene", format(data.geneticEvasion()), 0xFFB8B8B8));
        lines.add(new StatLine("Endurance Gene", format(data.geneticEndurance()), 0xFFB8B8B8));
        lines.add(new StatLine("Trait", data.trait(), 0xFFA8E6A3));
        cranks$addTraitBonusLines(lines, data.trait());
        lines.add(new StatLine("Breeding Focus", cranks$focusDisplayText(selectedFocus),
                0xFFA3D5FF));
        return lines;
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
                lines.add(new StatLine("Armor", "+30", 0xFFA8E6A3));
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
        lines.add(new StatLine("Sire", data.sireName(), 0xFFB8B8B8));
        lines.add(new StatLine("Dam", data.damName(), 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Attempts", data.breedsRemaining() + "/3", 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Cooldown", formatTime(data.breedingCooldownTicks()), 0xFFB8B8B8));
        lines.add(new StatLine("Imprinter", data.imprintOwnerName(), 0xFFA3D5FF));
        lines.add(new StatLine("Imprint", Math.round(data.imprintProgress() * 100) + "%", 0xFFA3D5FF));
        if (horse.isBaby()) {
            lines.add(new StatLine("Next Care", formatTime(data.careCooldownTicks()), 0xFFA3D5FF));
            lines.add(new StatLine("Care Item", "Sugar", 0xFFA3D5FF));
        }
        return lines;
    }

    @Unique
    private static List<StatLine> cranks$buildBirthRecord(HorseGeneticsSyncPacket data) {
        if (data == null) return cranks$syncingLines();
        List<StatLine> lines = new ArrayList<>();
        int personalCount = Integer.bitCount(data.personalGeneMutationMask())
                + (data.traitMutated() ? 1 : 0) + (data.coatMutated() ? 1 : 0);
        lines.add(new StatLine("Personal Mutations", Integer.toString(personalCount),
                personalCount > 0 ? 0xFFFFD966 : 0xFFB8B8B8));
        cranks$addMutationLine(lines, data, HorseBreedingRules.HEALTH_MUTATION,
                "Health Mut.", data.healthMutation(), false);
        cranks$addMutationLine(lines, data, HorseBreedingRules.SPEED_MUTATION,
                "Speed Mut.", data.speedMutation(), true);
        cranks$addMutationLine(lines, data, HorseBreedingRules.JUMP_MUTATION,
                "Jump Mut.", data.jumpMutation(), false);
        cranks$addMutationLine(lines, data, HorseBreedingRules.EVASION_MUTATION,
                "Evasion Mut.", data.evasionMutation(), false);
        cranks$addMutationLine(lines, data, HorseBreedingRules.ENDURANCE_MUTATION,
                "Endurance Mut.", data.enduranceMutation(), false);
        if (data.traitMutated()) {
            lines.add(new StatLine("Trait Mut. From", data.traitMutationFrom(), 0xFFFFD966));
            lines.add(new StatLine("Trait Mut. To", data.traitMutationTo(), 0xFFFFD966));
        }
        if (data.coatMutated()) {
            lines.add(new StatLine("Coat Mutation", "Cosmetic", 0xFFA3D5FF));
        }
        lines.add(new StatLine("Lineage Mutations", Integer.toString(data.mutations()), 0xFFB8B8B8));
        return lines;
    }

    @Unique
    private static void cranks$addMutationLine(List<StatLine> lines, HorseGeneticsSyncPacket data,
                                                int mask, String name, double delta,
                                                boolean precise) {
        if ((data.personalGeneMutationMask() & mask) == 0) return;
        boolean positive = (data.personalPositiveMutationMask() & mask) != 0;
        String value;
        if (Math.abs(delta) <= ZERO_THRESHOLD) {
            value = positive ? "+0 (capped)" : "-0 (capped)";
        } else {
            value = String.format(Locale.US, precise ? "%+.4f" : "%+.2f", delta);
        }
        lines.add(new StatLine(name, value, positive ? 0xFFA8E6A3 : 0xFFFF8A80));
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
            default -> data.geneticEndurance();
        };
    }

    @Unique
    private static double cranks$geneMin(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MIN_HEALTH;
            case 1 -> HorseBreedingRules.MIN_SPEED;
            case 2 -> HorseBreedingRules.MIN_JUMP;
            case 3 -> HorseBreedingRules.MIN_EVASION;
            default -> HorseBreedingRules.MIN_ENDURANCE;
        };
    }

    @Unique
    private static double cranks$geneMax(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MAX_HEALTH;
            case 1 -> HorseBreedingRules.MAX_SPEED;
            case 2 -> HorseBreedingRules.MAX_JUMP;
            case 3 -> HorseBreedingRules.MAX_EVASION;
            default -> HorseBreedingRules.MAX_ENDURANCE;
        };
    }

    @Unique
    private static String cranks$geneName(int gene) {
        return switch (gene) {
            case 0 -> "Health";
            case 1 -> "Speed";
            case 2 -> "Jump";
            case 3 -> "Evasion";
            default -> "Endurance";
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
        return switch (grade) {
            case "S" -> 0xFFFF70FF;
            case "A" -> 0xFFFFD966;
            case "B" -> 0xFF70D6FF;
            case "C" -> 0xFF9BE28F;
            default -> 0xFFFFFFFF;
        };
    }

    @Unique
    private static String cranks$trimToWidth(TextRenderer renderer, String value, int maxWidth) {
        if (renderer.getWidth(value) <= maxWidth) return value;
        return renderer.trimToWidth(value, Math.max(1, maxWidth - renderer.getWidth("..."))) + "...";
    }

    @Unique
    private record StatLine(String name, String value, int color) {
    }
}

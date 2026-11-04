package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.mount.MountAccessMode;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountStandaloneCargoSlot;
import com.cranks.dungeons.mount.SlotPositionAccess;
import com.cranks.dungeons.screen.MountCargoScreenHandler;
import com.cranks.dungeons.client.HorseInlineSelectorWidget;
import com.cranks.dungeons.network.SetMountAccessPayload;
import com.cranks.dungeons.network.ReleaseMountPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.cranks.dungeons.client.MountClientData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MountCargoScreen extends HandledScreen<MountCargoScreenHandler> {
    private static final int INFO_PANEL_WIDTH = 162;
    private static final int INFO_PANEL_GAP = 4;
    private static final int INFO_PANEL_HEADER_HEIGHT = 22;
    private static final int RELEASE_BUTTON_WIDTH = 100;
    private static final int RELEASE_BUTTON_HEIGHT = 16;
    private static final int INFO_LINE_STEP = 7;
    private static final int GENETIC_LINE_STEP = 11;
    private static final float INFO_TEXT_SCALE = 0.75f;
    private static final Identifier HORSE_TEXTURE =
            Identifier.ofVanilla("textures/gui/container/horse.png");
    private static final Identifier SADDLE_SLOT =
            Identifier.ofVanilla("container/horse/saddle_slot");
    private static final Identifier PANEL = Identifier.ofVanilla("advancements/title_box");
    private static final Identifier SLOT = Identifier.ofVanilla("container/slot");
    private static final Identifier SCROLLER = Identifier.ofVanilla("container/villager/scroller");
    private static final int CARGO_COLUMNS = 5;
    private static final int VISIBLE_CARGO_ROWS = 3;
    private HorseInlineSelectorWidget accessSelector;
    private MountAccessMode selectedAccessMode = MountAccessMode.OWNER_ONLY;
    private MountAccessMode lastServerAccessMode;
    private int accessEntityId = -1;
    private ButtonWidget releaseOwnershipButton;
    private int cargoScrollRow;

    public MountCargoScreen(MountCargoScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = 72;
    }

    @Override
    protected void init() {
        super.init();
        if (handler.mount() instanceof MountDataProvider) {
            accessSelector = addDrawableChild(new HorseInlineSelectorWidget(
                    72, 10, Text.literal("Mount access"), this::cycleAccessMode));
            accessSelector.visible = false;
        }
        releaseOwnershipButton = addDrawableChild(ButtonWidget.builder(
                Text.literal("Release Mount"), button -> sendReleaseRequest())
                .dimensions(0, 0, RELEASE_BUTTON_WIDTH, RELEASE_BUTTON_HEIGHT)
                .build());
        releaseOwnershipButton.visible = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        layoutLlamaCargoSlots();
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(HORSE_TEXTURE, x, y, 0, 0,
                backgroundWidth, backgroundHeight, 256, 256);

        if (handler.mount() != null) {
            MountKind kind = MountKind.of(handler.mount());
            context.drawGuiTexture(SADDLE_SLOT, x + 7, y + 17, 18, 18);
            context.drawGuiTexture(SLOT, x + 7, y + 53, 18, 18);
            if (kind == MountKind.LLAMA) {
                context.drawGuiTexture(Identifier.ofVanilla("container/horse/armor_slot"),
                        x + 7, y + 35, 18, 18);
            }
            boolean hasCargoAccess = kind == MountKind.LLAMA ? handler.activeCapacity() > 0
                    : ((MountDataProvider) handler.mount()).cranksdungeons$hasMountChest();
            if (hasCargoAccess) {
                for (int slot = 0; slot < handler.activeCapacity(); slot++) {
                    int column = slot % CARGO_COLUMNS;
                    int row = slot / CARGO_COLUMNS;
                    if (kind == MountKind.LLAMA
                            && (row < cargoScrollRow || row >= cargoScrollRow + VISIBLE_CARGO_ROWS)) continue;
                    context.drawGuiTexture(SLOT,
                            x + 79 + column * 18,
                            y + 17 + (kind == MountKind.LLAMA ? row - cargoScrollRow : row) * 18,
                            18, 18);
                }
                if (kind == MountKind.LLAMA && maxCargoScrollRow() > 0) {
                    int totalRows = Math.max(VISIBLE_CARGO_ROWS,
                            (handler.activeCapacity() + CARGO_COLUMNS - 1) / CARGO_COLUMNS);
                    int trackHeight = VISIBLE_CARGO_ROWS * 18;
                    int thumbHeight = Math.max(10, trackHeight * VISIBLE_CARGO_ROWS / totalRows);
                    int thumbY = y + 17 + (trackHeight - thumbHeight) * cargoScrollRow
                            / maxCargoScrollRow();
                    context.drawGuiTexture(SCROLLER, x + 169, thumbY, 6, thumbHeight);
                }
            }
            InventoryScreen.drawEntity(context, x + 26, y + 18, x + 78, y + 70,
                    17, 0.25f, mouseX, mouseY, handler.mount());
            drawStatsPanel(context, kind);
        }
    }

    private void drawStatsPanel(DrawContext context, MountKind kind) {
        var data = MountClientData.get(handler.mount().getId());
        if (accessSelector != null) accessSelector.visible = false;
        if (releaseOwnershipButton != null) releaseOwnershipButton.visible = false;
        List<MountInfoLine> stats = new ArrayList<>();
        List<MountInfoLine> genes = new ArrayList<>();
        List<MountInfoLine> breeding = new ArrayList<>();
        int accessLineIndex = -1;
        String accessValue = "";
        boolean canEditAccess = false;
        boolean canReleaseOwnership = false;
        if (data == null) {
            addLine(stats, "Synchronizing...");
            if (kind.hasGenetics()) {
                addLine(genes, "Synchronizing...");
                addLine(breeding, "Synchronizing...");
            }
        } else {
            canReleaseOwnership = clientIsOwner(data.ownerUuid());
            String xp = data.experienceRequired() <= 0 ? "MAX"
                    : data.experience() + "/" + data.experienceRequired();
            addLine(stats, "Level: " + data.level() + " " + xp);
            addLine(stats, "Health: " + number(handler.mount().getHealth()) + "/" + number(handler.mount().getMaxHealth()));
            addLine(stats, "Movement Speed: " + String.format(Locale.US, "%.1f blocks/s", data.speed() * 42.16));
            if (handler.mount().getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR) != null
                    && handler.mount().getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR) > 0.0) {
                addLine(stats, "Armor: " + number(handler.mount().getAttributeValue(
                        net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR)));
            }
            int cargoCapacity = kind == MountKind.LLAMA
                    ? handler.activeCapacity() : data.cargoCapacity();
            addLine(stats, "Cargo: " + cargoCapacity + " slots");
            if (kind == MountKind.LLAMA) {
                addLine(stats, "Pack: " + handler.packSize() + " llamas");
                double packBonus = com.cranks.dungeons.mount.LlamaPackManager.speedBonusForSize(
                        handler.packSize(), data.power(), data.trait());
                addLine(stats, "Pack speed: +" + Math.round(packBonus * 100.0) + "%");
            }
            if (data.maximumResource() > 0) addLine(stats, (kind == MountKind.STRIDER ? "Heat: " : "Endurance: ")
                    + number(data.resource()) + "/" + number(data.maximumResource()));
            if (!"None".equals(data.trait())) addLine(stats, "Trait: " + data.trait());
            if (data.ownerUuid() != null && clientCanEditAccess(data.ownerUuid())) {
                MountAccessMode serverMode = MountAccessMode.fromId(data.accessMode());
                if (accessEntityId != handler.mount().getId() || lastServerAccessMode != serverMode) {
                    selectedAccessMode = serverMode;
                }
                accessEntityId = handler.mount().getId();
                lastServerAccessMode = serverMode;
                canEditAccess = clientCanEditAccess(data.ownerUuid());
                accessValue = canEditAccess ? "< " + selectedAccessMode.displayName() + " >"
                        : selectedAccessMode.displayName();
                accessLineIndex = stats.size();
                addLine(stats, "Mount Access: " + accessValue);
            }
            if (kind.hasGenetics()) {
                String[] labels = MountManager.geneLabels(kind);
                double[] values = kind == MountKind.LLAMA
                        ? new double[]{data.health(), data.speed(), data.power(), data.utility(), data.armor()}
                        : new double[]{data.health(), data.speed(), data.power(), data.endurance(),
                                data.utility(), data.armor()};
                String overallGrade = MountGeneticRatings.overall(kind, values, data.generation());
                addLine(genes, "Overall Grade: " + overallGrade, -1.0, gradeColor(overallGrade));
                addLine(genes, data.male() ? "Sex: Male" : "Sex: Female");
                for (int i = 0; i < labels.length; i++) {
                    String value = labels[i].equals("Pack Instinct")
                            ? String.format(Locale.US, "%.1f%%", values[i] * 100.0)
                            : labels[i].contains("Speed")
                            ? String.format(Locale.US, "%.1f blocks/s", values[i] * 42.16)
                            : number(values[i]);
                    String grade = MountGeneticRatings.grade(kind, i, values[i], data.generation());
                    addLine(genes, labels[i] + ": " + value + " [" + grade + "]",
                            geneProgress(kind, i, values[i]), gradeColor(grade));
                }
                addLine(breeding, "Generation: " + data.generation());
                if (kind == MountKind.MULE) {
                    addLine(breeding, "Status: Sterile hybrid");
                } else {
                    addLine(breeding, "Attempts: " + data.breedsRemaining());
                    addLine(breeding, "Cooldown: " + formatCooldown(data.breedingCooldownTicks()));
                }
            }
        }
        int statsX = Math.max(4, x - INFO_PANEL_WIDTH - INFO_PANEL_GAP);
        int genesX = Math.min(width - INFO_PANEL_WIDTH - 4,
                x + backgroundWidth + INFO_PANEL_GAP);
        int releaseButtonSpace = canReleaseOwnership ? 22 : 0;
        int statsHeight = panelHeight(stats) + releaseButtonSpace;
        int genesHeight = genes.isEmpty() ? 0 : panelHeight(genes);
        int breedingHeight = breeding.isEmpty() ? 0 : panelHeight(breeding);
        int rightHeight = genesHeight + (genesHeight > 0 && breedingHeight > 0 ? INFO_PANEL_GAP : 0)
                + breedingHeight;
        int statsY = Math.max(4, Math.min(y, height - statsHeight - 4) - 3);
        int rightY = Math.max(4, Math.min(y, height - rightHeight - 4) - 3);
        int breedingY = rightY + genesHeight + (genesHeight > 0 ? INFO_PANEL_GAP : 0);
        drawInfoPanel(context, kind.displayName() + " Stats", stats, statsX, statsY,
                releaseButtonSpace);
        if (accessSelector != null && accessLineIndex >= 0) {
            int prefixWidth = Math.round(textRenderer.getWidth("Mount Access: ") * INFO_TEXT_SCALE);
            int valueWidth = Math.round(textRenderer.getWidth(accessValue) * INFO_TEXT_SCALE);
            accessSelector.setX(statsX + 10 + prefixWidth - 2);
            accessSelector.setY(statsY + INFO_PANEL_HEADER_HEIGHT
                    + lineOffset(stats, accessLineIndex) - 1);
            accessSelector.setWidth(valueWidth + 4);
            accessSelector.visible = canEditAccess;
            accessSelector.active = canEditAccess;
            accessSelector.setMessage(Text.literal("Mount access: "
                    + selectedAccessMode.displayName()));
        }
        if (releaseOwnershipButton != null && canReleaseOwnership) {
            releaseOwnershipButton.setX(statsX
                    + (INFO_PANEL_WIDTH - releaseOwnershipButton.getWidth()) / 2);
            releaseOwnershipButton.setY(statsY + panelHeight(stats) - 2);
            releaseOwnershipButton.visible = true;
            releaseOwnershipButton.active = true;
        }
        if (genesHeight > 0) drawInfoPanel(context, "Genetics", genes, genesX, rightY);
        if (breedingHeight > 0) drawInfoPanel(context, "Breeding", breeding, genesX, breedingY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        if (handler.mount() != null && MountKind.of(handler.mount()) == MountKind.LLAMA
                && maxCargoScrollRow() > 0 && mouseX >= x + 79 && mouseX < x + 176
                && mouseY >= y + 17 && mouseY < y + 71 && verticalAmount != 0.0) {
            cargoScrollRow = Math.max(0, Math.min(maxCargoScrollRow(),
                    cargoScrollRow - (verticalAmount > 0 ? 1 : -1)));
            layoutLlamaCargoSlots();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int maxCargoScrollRow() {
        int rows = (handler.activeCapacity() + CARGO_COLUMNS - 1) / CARGO_COLUMNS;
        return Math.max(0, rows - VISIBLE_CARGO_ROWS);
    }

    private void layoutLlamaCargoSlots() {
        if (handler.mount() == null || MountKind.of(handler.mount()) != MountKind.LLAMA) return;
        cargoScrollRow = Math.min(cargoScrollRow, maxCargoScrollRow());
        for (var slot : handler.slots) {
            if (!(slot instanceof MountStandaloneCargoSlot cargo)
                    || !(slot instanceof SlotPositionAccess position)) continue;
            int index = cargo.getIndex();
            int row = index / CARGO_COLUMNS;
            int column = index % CARGO_COLUMNS;
            boolean visible = index < handler.activeCapacity() && row >= cargoScrollRow
                    && row < cargoScrollRow + VISIBLE_CARGO_ROWS;
            position.cranksdungeons$setPosition(80 + column * 18,
                    visible ? 18 + (row - cargoScrollRow) * 18 : -1000);
        }
    }

    private int panelHeight(List<MountInfoLine> lines) {
        int contentHeight = 0;
        for (MountInfoLine line : lines) contentHeight += lineStep(line);
        return INFO_PANEL_HEADER_HEIGHT + contentHeight + 9;
    }

    private void drawInfoPanel(DrawContext context, String title, List<MountInfoLine> lines,
                               int panelX, int panelY) {
        drawInfoPanel(context, title, lines, panelX, panelY, 0);
    }

    private void drawInfoPanel(DrawContext context, String title, List<MountInfoLine> lines,
                               int panelX, int panelY, int extraHeight) {
        int panelHeight = panelHeight(lines) + extraHeight;
        context.drawGuiTexture(PANEL, panelX, panelY, INFO_PANEL_WIDTH, panelHeight);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(title), panelX + 81, panelY + 10, 0xFFFFD966);
        int lineY = panelY + INFO_PANEL_HEADER_HEIGHT;
        for (MountInfoLine line : lines) {
            context.getMatrices().push();
            context.getMatrices().translate(panelX + 10, lineY, 0);
            context.getMatrices().scale(INFO_TEXT_SCALE, INFO_TEXT_SCALE, 1.0f);
            context.drawText(textRenderer, textRenderer.trimToWidth(line.text,
                            Math.round((INFO_PANEL_WIDTH - 20) / INFO_TEXT_SCALE)),
                    0, 0, line.color, false);
            context.getMatrices().pop();
            if (line.progress >= 0.0) {
                int barLeft = panelX + 10;
                int barRight = panelX + INFO_PANEL_WIDTH - 10;
                int barTop = lineY + 7;
                context.fill(barLeft, barTop, barRight, barTop + 3, 0x55303030);
                int fillRight = barLeft + Math.round((barRight - barLeft)
                        * (float) Math.max(0.0, Math.min(1.0, line.progress)));
                if (fillRight > barLeft) {
                    context.fill(barLeft, barTop, fillRight, barTop + 3, line.color);
                }
            }
            lineY += lineStep(line);
        }
    }

    private static void addLine(List<MountInfoLine> lines, String text) {
        lines.add(new MountInfoLine(text, -1.0, 0xFFFFFFFF));
    }

    private static void addLine(List<MountInfoLine> lines, String text, double progress) {
        lines.add(new MountInfoLine(text, progress, 0xFFFFFFFF));
    }

    private static void addLine(List<MountInfoLine> lines, String text,
                                double progress, int color) {
        lines.add(new MountInfoLine(text, progress, color));
    }

    private static int lineStep(MountInfoLine line) {
        return line.progress >= 0.0 ? GENETIC_LINE_STEP : INFO_LINE_STEP;
    }

    private static int lineOffset(List<MountInfoLine> lines, int lineIndex) {
        int offset = 0;
        for (int i = 0; i < lineIndex && i < lines.size(); i++) offset += lineStep(lines.get(i));
        return offset;
    }

    private static double geneProgress(MountKind kind, int geneIndex, double value) {
        double minimum = MountGeneticRatings.breedingMinimum(kind, geneIndex);
        double maximum = MountGeneticRatings.breedingMaximum(kind, geneIndex);
        if (maximum <= minimum) return value >= maximum ? 1.0 : 0.0;
        return Math.max(0.0, Math.min(1.0, (value - minimum) / (maximum - minimum)));
    }

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

    private record MountInfoLine(String text, double progress, int color) {
    }

    private static String formatCooldown(long ticks) {
        if (ticks <= 0) return "Ready";
        long seconds = Math.max(1L, ticks / 20L);
        long minutes = seconds / 60L;
        long remainder = seconds % 60L;
        return minutes > 0 ? minutes + "m " + remainder + "s" : seconds + "s";
    }

    private static String number(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) return Integer.toString((int) Math.rint(value));
        return String.format(Locale.US, "%.1f", value);
    }

    private boolean clientCanEditAccess(java.util.UUID ownerUuid) {
        var player = net.minecraft.client.MinecraftClient.getInstance().player;
        return player != null && (player.isCreative()
                || (ownerUuid != null && ownerUuid.equals(player.getUuid())));
    }

    private boolean clientIsOwner(java.util.UUID ownerUuid) {
        var player = net.minecraft.client.MinecraftClient.getInstance().player;
        return player != null && ownerUuid != null && ownerUuid.equals(player.getUuid());
    }

    private void sendReleaseRequest() {
        if (handler.mount() != null && ClientPlayNetworking.canSend(ReleaseMountPayload.ID)) {
            ClientPlayNetworking.send(new ReleaseMountPayload(handler.mount().getId()));
        }
    }

    private void cycleAccessMode(int direction) {
        if (accessSelector == null || !accessSelector.active) return;
        selectedAccessMode = selectedAccessMode.cycle(direction);
        if (ClientPlayNetworking.canSend(SetMountAccessPayload.ID)) {
            ClientPlayNetworking.send(new SetMountAccessPayload(handler.mount().getId(),
                    selectedAccessMode.name()));
        }
    }

}

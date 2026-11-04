package com.cranks.dungeons.client;

import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.network.MountDataSyncPacket;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Displays potential ratings for genetic mounts while they are scouted through a spyglass. */
public final class SpyglassMountScoutHudRenderer implements HudRenderCallback {
    private static final Identifier PANEL = Identifier.ofVanilla("advancements/title_box");
    private static final int PANEL_WIDTH = 162;
    private static final double SCOUT_RANGE = 32.0;
    private static final double SPEED_TO_BLOCKS_PER_SECOND = 42.16;
    private static final String[] HORSE_GENE_LABELS = {
            "Health Bonus", "Speed", "Jump", "Evasion", "Endurance", "Armor Bonus"
    };

    private SpyglassMountScoutHudRenderer() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new SpyglassMountScoutHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null || client.options.hudHidden
                || client.currentScreen != null || !player.isAlive() || !player.isUsingItem()
                || !player.getActiveItem().isOf(Items.SPYGLASS)) {
            return;
        }

        Entity target = findAimedEntity(client, player);
        if (target instanceof HorseEntity horse) {
            renderHorse(context, client, player, horse);
        } else if (target instanceof LivingEntity living
                && living instanceof MountDataProvider
                && MountKind.of(living).hasGenetics()) {
            renderMount(context, client, player, living, MountKind.of(living));
        }
    }

    /**
     * Spyglass scouting uses a longer, block-occluded ray than ordinary entity
     * interaction, so distant candidates can be evaluated without seeing mounts
     * through walls or through an intervening entity.
     */
    private static Entity findAimedEntity(MinecraftClient client, PlayerEntity player) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(SCOUT_RANGE));
        HitResult blockHit = client.world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        Vec3d rayEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getPos();
        Box searchBox = player.getBoundingBox().stretch(rayEnd.subtract(start)).expand(1.0);
        double closestDistanceSquared = start.squaredDistanceTo(rayEnd);
        Entity closest = null;

        for (Entity candidate : client.world.getOtherEntities(player, searchBox,
                entity -> entity.isAlive() && !entity.isSpectator() && entity.canHit())) {
            var hit = candidate.getBoundingBox().expand(candidate.getTargetingMargin())
                    .raycast(start, rayEnd);
            if (hit.isEmpty()) continue;
            double distanceSquared = start.squaredDistanceTo(hit.get());
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = candidate;
            }
        }
        return closest;
    }

    private static void renderHorse(DrawContext context, MinecraftClient client, PlayerEntity player,
                                    HorseEntity horse) {
        HorseGeneticsSyncPacket data = HorseGeneticsClientData.get(horse.getId());
        String ownership = ownershipTextForMount(horse, player,
                data != null && data.tamed(), data == null ? null : data.ownerUuid(),
                data == null ? "" : data.ownerName());
        if (data == null) {
            renderWaitingCard(context, client, MountKind.HORSE, ownership);
            return;
        }
        double[] genes = {data.geneticHealth(), data.geneticSpeed(), data.geneticJump(),
                data.geneticEvasion(), data.geneticEndurance(), data.geneticArmor()};
        List<ScoutLine> rows = new ArrayList<>(6);
        for (int i = 0; i < genes.length; i++) {
            String value = formatValue(HORSE_GENE_LABELS[i], genes[i]);
            String grade = MountGeneticRatings.grade(MountKind.HORSE, i, genes[i], data.generation());
            rows.add(new ScoutLine(HORSE_GENE_LABELS[i] + ": " + value + " [" + grade + "]",
                    gradeColor(grade)));
        }
        drawCard(context, client, MountKind.HORSE, ownership, data.grade(), rows);
    }

    private static void renderMount(DrawContext context, MinecraftClient client, PlayerEntity player,
                                   LivingEntity mount, MountKind kind) {
        MountDataSyncPacket data = MountClientData.get(mount.getId());
        String ownership = ownershipTextForMount(mount, player,
                data != null && data.tamed(), data == null ? null : data.ownerUuid(),
                data == null ? "" : data.ownerName());
        if (data == null) {
            renderWaitingCard(context, client, kind, ownership);
            return;
        }
        double[] values = kind == MountKind.LLAMA
                ? new double[]{data.health(), data.speed(), data.power(), data.utility(), data.armor()}
                : new double[]{data.health(), data.speed(), data.power(), data.endurance(),
                        data.utility(), data.armor()};
        String[] labels = MountManager.geneLabels(kind);
        List<ScoutLine> rows = new ArrayList<>(labels.length);
        for (int i = 0; i < labels.length; i++) {
            String value = formatValue(labels[i], values[i]);
            String grade = MountGeneticRatings.grade(kind, i, values[i], data.generation());
            rows.add(new ScoutLine(labels[i] + ": " + value + " [" + grade + "]",
                    gradeColor(grade)));
        }
        String overall = MountGeneticRatings.overall(kind, values, data.generation());
        drawCard(context, client, kind, ownership, overall, rows);
    }

    private static String ownershipText(Entity mount, PlayerEntity player, boolean packetTamed,
                                       UUID ownerUuid, String ownerName) {
        if (!(mount instanceof AbstractHorseEntity horse)) return "Wild / unowned";
        if (!horse.isTame()) return "Wild";
        if (!packetTamed) return "Tamed · checking owner";
        if (ownerUuid == null) return "Tamed · owner unknown";
        if (ownerUuid.equals(player.getUuid())) return "Tamed by you";
        return ownerName == null || ownerName.isBlank()
                ? "Tamed by another player" : "Tamed by " + ownerName;
    }

    private static String ownershipTextForMount(Entity mount, PlayerEntity player, boolean packetTamed,
                                                UUID ownerUuid, String ownerName) {
        boolean vanillaTamed = mount instanceof AbstractHorseEntity horse && horse.isTame();
        if (ownerUuid == null) {
            if (vanillaTamed && !packetTamed) return "Tamed - checking owner";
            if (vanillaTamed) return "Tamed - owner unknown";
            return "Wild / unowned";
        }
        if (ownerUuid.equals(player.getUuid())) return vanillaTamed ? "Tamed by you" : "Owned by you";
        String prefix = vanillaTamed ? "Tamed by " : "Owned by ";
        return ownerName == null || ownerName.isBlank()
                ? prefix + "another player" : prefix + ownerName;
    }

    private static void renderWaitingCard(DrawContext context, MinecraftClient client,
                                          MountKind kind, String ownership) {
        drawInfoPanel(context, client, kind.displayName() + " Potential", List.of(
                new ScoutLine(ownership, ownership.startsWith("Wild") ? 0xFFFFD966 : 0xFFFFFFFF),
                new ScoutLine("Reading genetics...", 0xFFFFFFFF)));
    }

    private static void drawCard(DrawContext context, MinecraftClient client, MountKind kind,
                                 String ownership, String overall, List<ScoutLine> genes) {
        List<ScoutLine> lines = new ArrayList<>(genes.size() + 2);
        lines.add(new ScoutLine(ownership, ownership.startsWith("Wild") ? 0xFFFFD966 : 0xFFFFFFFF));
        String overallLine = overall == null || overall.isBlank() ? "?" : overall;
        lines.add(new ScoutLine("Overall Grade: " + overallLine, gradeColor(overallLine)));
        lines.addAll(genes);
        drawInfoPanel(context, client, kind.displayName() + " Potential", lines);
    }

    private static void drawInfoPanel(DrawContext context, MinecraftClient client,
                                      String title, List<ScoutLine> lines) {
        int panelHeight = 28 + lines.size() * 8;
        int panelX = Math.max(4, client.getWindow().getScaledWidth() - PANEL_WIDTH - 4);
        int panelY = Math.max(4, (client.getWindow().getScaledHeight() - panelHeight) / 2);
        context.drawGuiTexture(PANEL, panelX, panelY, PANEL_WIDTH, panelHeight);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(title),
                panelX + PANEL_WIDTH / 2, panelY + 10, 0xFFFFD966);
        int lineY = panelY + 23;
        for (ScoutLine line : lines) {
            context.getMatrices().push();
            context.getMatrices().translate(panelX + 8, lineY, 0);
            context.getMatrices().scale(0.8f, 0.8f, 1.0f);
            context.drawText(client.textRenderer,
                    client.textRenderer.trimToWidth(line.text(), 182), 0, 0,
                    line.color(), false);
            context.getMatrices().pop();
            lineY += 8;
        }
    }

    private static String formatValue(String label, double value) {
        if (label.equals("Pack Instinct")) {
            return String.format(Locale.US, "%.1f%%", value * 100.0);
        }
        if (label.toLowerCase(Locale.ROOT).contains("speed")) {
            return String.format(Locale.US, "%.1f b/s", value * SPEED_TO_BLOCKS_PER_SECOND);
        }
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static int gradeColor(String grade) {
        if (grade == null || grade.isBlank()) return 0xFFFFFFFF;
        return switch (grade.charAt(0)) {
            case 'S' -> 0xFFFF70FF;
            case 'A' -> 0xFFFFD966;
            case 'B' -> 0xFF70D6FF;
            case 'C' -> 0xFF9BE28F;
            case 'D' -> 0xFFFF8A80;
            default -> 0xFFFFFFFF;
        };
    }

    private record ScoutLine(String text, int color) {
    }
}

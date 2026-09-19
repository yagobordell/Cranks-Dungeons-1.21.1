package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.screen.MountCargoScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.cranks.dungeons.client.MountClientData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MountCargoScreen extends HandledScreen<MountCargoScreenHandler> {
    private static final Identifier HORSE_TEXTURE =
            Identifier.ofVanilla("textures/gui/container/horse.png");
    private static final Identifier SADDLE_SLOT =
            Identifier.ofVanilla("container/horse/saddle_slot");
    private static final Identifier PANEL = Identifier.ofVanilla("advancements/title_box");
    private static final Identifier SLOT = Identifier.ofVanilla("container/slot");

    public MountCargoScreen(MountCargoScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = 72;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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
            if (((com.cranks.dungeons.mount.MountDataProvider) handler.mount())
                    .cranksdungeons$hasMountChest()) {
                for (int slot = 0; slot < handler.capacity(); slot++) {
                    int column = slot % 5;
                    int row = slot / 5;
                    context.drawGuiTexture(SLOT,
                            x + 79 + column * 18, y + 17 + row * 18, 18, 18);
                }
            }
            InventoryScreen.drawEntity(context, x + 26, y + 18, x + 78, y + 70,
                    17, 0.25f, mouseX, mouseY, handler.mount());
            drawStatsPanel(context, kind);
        }
    }

    private void drawStatsPanel(DrawContext context, MountKind kind) {
        var data = MountClientData.get(handler.mount().getId());
        List<String> overview = new ArrayList<>();
        List<String> breeding = new ArrayList<>();
        if (data == null) {
            overview.add("Synchronizing...");
        } else {
            String xp = data.experienceRequired() <= 0 ? "MAX"
                    : data.experience() + "/" + data.experienceRequired();
            overview.add("Level: " + data.level() + " " + xp);
            overview.add("Health: " + number(handler.mount().getHealth()) + "/" + number(handler.mount().getMaxHealth()));
            overview.add("Speed: " + String.format(Locale.US, "%.1f blocks/s", data.speed() * 42.16));
            overview.add("Cargo: " + data.cargoCapacity() + " slots");
            if (data.maximumResource() > 0) overview.add((kind == MountKind.STRIDER ? "Heat: " : "Endurance: ")
                    + number(data.resource()) + "/" + number(data.maximumResource()));
            if (!"None".equals(data.trait())) overview.add("Trait: " + data.trait());
            if (kind.hasGenetics()) {
                breeding.add(data.male() ? "Sex: Male" : "Sex: Female");
                String[] labels = MountManager.geneLabels(kind);
                double[] values = {data.health(), data.speed(), data.power(), data.endurance(), data.utility()};
                for (int i = 0; i < labels.length; i++) {
                    String value = labels[i].contains("Speed")
                            ? String.format(Locale.US, "%.1f b/s", values[i] * 42.16)
                            : number(values[i]);
                    breeding.add(labels[i] + ": " + value);
                }
                breeding.add("Generation: " + data.generation());
                breeding.add("Mutations: " + data.mutations());
                if (!"Wild".equals(data.sireName())) breeding.add("Sire: " + data.sireName());
                if (!"Wild".equals(data.damName())) breeding.add("Dam: " + data.damName());
                if (kind == MountKind.MULE) {
                    breeding.add("Breeding: Sterile hybrid");
                } else {
                    breeding.add("Breeds: " + data.breedsRemaining());
                    breeding.add("Cooldown: " + formatCooldown(data.breedingCooldownTicks()));
                }
            }
        }
        int panelX = Math.min(width - 166, x + backgroundWidth + 4);
        int overviewHeight = panelHeight(overview);
        int breedingHeight = breeding.isEmpty() ? 0 : panelHeight(breeding);
        int totalHeight = overviewHeight + (breedingHeight == 0 ? 0 : 4 + breedingHeight);
        int panelY = Math.max(4, Math.min(y, height - totalHeight - 4) - 3);
        drawInfoPanel(context, kind.displayName() + " Overview", overview, panelX, panelY);
        if (breedingHeight > 0) {
            drawInfoPanel(context, "Genetics & Breeding", breeding,
                    panelX, panelY + overviewHeight + 4);
        }
    }

    private int panelHeight(List<String> lines) {
        return 28 + lines.size() * 8;
    }

    private void drawInfoPanel(DrawContext context, String title, List<String> lines,
                               int panelX, int panelY) {
        int panelHeight = panelHeight(lines);
        context.drawGuiTexture(PANEL, panelX, panelY, 162, panelHeight);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(title), panelX + 81, panelY + 10, 0xFFFFD966);
        int lineY = panelY + 23;
        for (String line : lines) {
            context.getMatrices().push();
            context.getMatrices().translate(panelX + 8, lineY, 0);
            context.getMatrices().scale(0.8f, 0.8f, 1.0f);
            context.drawText(textRenderer, textRenderer.trimToWidth(line, 182),
                    0, 0, 0xFFFFFFFF, false);
            context.getMatrices().pop();
            lineY += 8;
        }
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

}

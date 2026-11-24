package com.cranks.dungeons.client.render;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.InventoryStyle;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public final class HotbarHudRenderer {
    private static final int SLOT_PITCH = InventoryStyle.HUD_SLOT_SIZE;
    private static final int STRIP_WIDTH = PlayerLoadout.HOTBAR_COUNT * SLOT_PITCH;
    private static final int SOCKET_SIZE = 22;
    private static final int BOTTOM_MARGIN = 5;
    // Keep the same layout bounds so saved hotbar positions retain their center.
    public static final int WIDTH = 266;
    public static final int HEIGHT = 27;
    public static final int DEFAULT_LIFT = HEIGHT + BOTTOM_MARGIN - 23;
    private static final int BAR_INSET = (WIDTH - STRIP_WIDTH) / 2;

    private HotbarHudRenderer() {}

    public static int defaultX(int screenWidth) {
        return Math.max(0, screenWidth / 2 - WIDTH / 2);
    }

    public static int defaultY(int screenHeight) {
        return Math.max(0, screenHeight - HEIGHT - BOTTOM_MARGIN);
    }

    public static int resolveX(int screenWidth) {
        var config = CranksDungeons.CONFIG;
        int reference = config.statBarLayoutReferenceWidth();
        return config.hotbarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.hotbarX(), screenWidth, WIDTH,
                        defaultX(reference > 0 ? reference : screenWidth), defaultX(screenWidth))
                : defaultX(screenWidth);
    }

    public static int resolveY(int screenHeight) {
        var config = CranksDungeons.CONFIG;
        int reference = config.statBarLayoutReferenceHeight();
        return config.hotbarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.hotbarY(), screenHeight, HEIGHT,
                        defaultY(reference > 0 ? reference : screenHeight), defaultY(screenHeight))
                : defaultY(screenHeight);
    }

    @FunctionalInterface
    public interface ItemRenderer {
        void draw(int index, ItemStack stack, int x, int y);
    }

    public static void drawBar(DrawContext context, int x, int y, PlayerInventory inventory, ItemRenderer items) {
        int bar = x + BAR_INSET;
        int slotY = y + 3;
        int selectedSlot = inventory == null ? 0 : inventory.selectedSlot;
        int selectionX = bar;
        boolean hasSelection = false;
        for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) {
            int slot = PlayerLoadout.getHotbarSlot(i);
            int slotX = bar + i * SLOT_PITCH;
            InventoryStyle.drawHudSlot(context, slotX, slotY, i);
            if (slot == selectedSlot) {
                selectionX = slotX;
                hasSelection = true;
            }
            ItemStack stack = inventory == null ? ItemStack.EMPTY : inventory.getStack(slot);
            if (stack.isEmpty()) {
                InventoryStyle.drawHudNumber(context, slotX, slotY, i);
            } else {
                items.draw(i, stack, slotX + 2, slotY + 2);
            }
        }
        if (hasSelection) InventoryStyle.drawHudSelection(context, selectionX, slotY);
    }

    public static int drawSideSocket(DrawContext context, int x, int y, boolean right) {
        int left = x + (right ? BAR_INSET + STRIP_WIDTH + 3 : BAR_INSET - SOCKET_SIZE - 3);
        InventoryStyle.drawHudSlot(context, left + 1, y + 3, 0);
        return left;
    }

    public static void drawAttackIndicator(DrawContext context, int x, int y, boolean right, float cooldown) {
        int left = drawSideSocket(context, x, y, right);
        int fill = Math.round(14 * cooldown);
        context.fill(left + 4, y + 21 - fill, left + 18, y + 21, CompendiumStyle.TEXT_MUTED);
    }

    public static void renderPreview(DrawContext context, MinecraftClient client) {
        var player = client.player;
        drawBar(context, 0, 0, player == null ? null : player.getInventory(),
                (index, stack, x, y) -> drawPreviewStack(context, client, stack, x, y));
        boolean offhandRight = player != null && player.getMainArm() == Arm.LEFT;
        int offhandX = drawSideSocket(context, 0, 0, offhandRight);
        if (player != null) drawPreviewStack(context, client, player.getOffHandStack(), offhandX + 3, 5);
        drawAttackIndicator(context, 0, 0, !offhandRight, 0.65f);
    }

    private static void drawPreviewStack(DrawContext context, MinecraftClient client, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            context.drawItem(stack, x, y);
            context.drawItemInSlot(client.textRenderer, stack, x, y);
        }
    }
}

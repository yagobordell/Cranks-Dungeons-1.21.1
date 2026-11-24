package com.cranks.dungeons.client;

import com.cranks.dungeons.client.render.HudStyle;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.component.WorldTreeDataComponent;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationHudOverlay implements HudRenderCallback {

    private static class TimedNotification {
        Text message;
        int ticksRemaining;

        TimedNotification(Text message, int ticks) {
            this.message = message;
            this.ticksRemaining = ticks;
        }
    }

    private static final List<TimedNotification> notifications = new ArrayList<>();
    private static TimedNotification coinNotification;
    private static long coinTotal;

    public static void addCoinGain(long coins) {
        if (coins <= 0) return;
        coinTotal += Math.min(coins, Long.MAX_VALUE - coinTotal);
        Text message = Text.literal("+" + String.format(java.util.Locale.ROOT, "%,d", coinTotal) + " coins");
        if (coinNotification == null) {
            coinNotification = new TimedNotification(message, 50);
            notifications.add(coinNotification);
        } else {
            coinNotification.message = message;
            coinNotification.ticksRemaining = 50;
        }
    }

    public static void addNotification(Text message, int durationTicks) {
        for (TimedNotification n : notifications) {
            if (n.message.getString().equals(message.getString())) {
                n.ticksRemaining = durationTicks;
                return;
            }
        }
        notifications.add(new TimedNotification(message, durationTicks));
    }

    public static void clearNotifications() {
        notifications.clear();
        coinNotification = null;
        coinTotal = 0;
    }

    public static void tick() {
        Iterator<TimedNotification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            TimedNotification n = iterator.next();
            n.ticksRemaining--;
            if (n.ticksRemaining <= 0) {
                iterator.remove();
                if (n == coinNotification) {
                    coinNotification = null;
                    coinTotal = 0;
                }
            }
        }
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new NotificationHudOverlay());
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.currentScreen instanceof com.cranks.dungeons.client.screen.HudLayoutEditScreen)return;
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || !player.isAlive() || com.cranks.dungeons.CranksDungeonsClient.isCinematicRespawnActive()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int yOffset = screenHeight - 85;

        List<Text> linesToDraw = new ArrayList<>();

        for (TimedNotification n : notifications) {
            linesToDraw.add(n.message);
        }

        PassiveDataComponent passiveData = ModComponents.PASSIVE_DATA.get(player);
        if (passiveData != null && passiveData.getUnspentPoints() > 0) {
            linesToDraw.add(Text.literal("You have " + passiveData.getUnspentPoints() + " unallocated passive points!").formatted(Formatting.AQUA));
        }

        WorldTreeDataComponent worldData = ModComponents.WORLD_TREE_DATA.get(player);
        if (worldData != null && worldData.getUnspentPoints() > 0) {
            linesToDraw.add(Text.literal("You have " + worldData.getUnspentPoints() + " unallocated world tree points!").formatted(Formatting.GOLD));
        }

        if (linesToDraw.isEmpty()) return;

        float pulse = (float) ((Math.sin(Util.getMeasuringTimeMs() / 250.0) + 1.0) / 2.0);
        int alpha = (int) (120 + (pulse * 135));
        int alphaMask = alpha << 24;
        int plateHeight = textRenderer.fontHeight + 6;

        for (int i = linesToDraw.size() - 1; i >= 0; i--) {
            Text line = linesToDraw.get(i);
            int textWidth = textRenderer.getWidth(line);
            int x = (screenWidth - textWidth) / 2;
            HudStyle.plate(drawContext, x - 4, yOffset - 4, textWidth + 8, plateHeight, false);
            drawContext.drawTextWithShadow(textRenderer, line, x, yOffset, 0xFFFFFF | alphaMask);
            yOffset -= plateHeight + 1;
        }
    }
}

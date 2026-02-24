package com.cranks.dungeons.event;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class EnchantmentDisableTickHandler {

    // How long to show "All Enchantments Enabled", in ticks (20 = 1 second)
    private static final int ENABLED_MESSAGE_TICKS = 40;

    private static final Map<UUID, Boolean> showingDisabled = new HashMap<>();
    private static final Map<UUID, Boolean> showingEnabled = new HashMap<>();
    private static final Map<UUID, Integer> enabledMessageTimer = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                ItemStack tool = player.getMainHandStack();

                List<Identifier> relevant = EnchantmentDisableManager.getRelevantEnchantments(tool);
                String label = EnchantmentDisableManager.getDisabledLabel(uuid, relevant);

                if (label != null) {
                    // Show disabled message continuously
                    player.sendMessage(
                            Text.literal("⚠ " + label + " Disabled ⚠")
                                    .formatted(Formatting.RED),
                            true
                    );
                    showingDisabled.put(uuid, true);
                    showingEnabled.put(uuid, false);
                    enabledMessageTimer.remove(uuid);
                } else {
                    int timer = enabledMessageTimer.getOrDefault(uuid, 0);

                    if (timer > 0) {
                        // Show "all enabled" briefly
                        player.sendMessage(
                                Text.literal("✔ All Enchantments Enabled")
                                        .formatted(Formatting.GREEN),
                                true
                        );
                        enabledMessageTimer.put(uuid, timer - 1);
                        showingEnabled.put(uuid, true);
                        showingDisabled.put(uuid, false);
                    } else {
                        // Timer expired or never set: clear any lingering messages
                        boolean wasDisabled = showingDisabled.getOrDefault(uuid, false);
                        boolean wasEnabled = showingEnabled.getOrDefault(uuid, false);

                        if (wasDisabled || wasEnabled) {
                            // Force-clear the action bar so the message really disappears now
                            player.sendMessage(Text.empty(), true);
                        }

                        showingDisabled.put(uuid, false);
                        showingEnabled.put(uuid, false);
                        enabledMessageTimer.remove(uuid);
                    }
                }
            }
        });
    }

    /**
     * Trigger the short "All Enchantments Enabled" message.
     */
    public static void triggerEnabledMessage(UUID playerId) {
        enabledMessageTimer.put(playerId, ENABLED_MESSAGE_TICKS);
    }

    public static void removePlayer(UUID playerId) {
        showingDisabled.remove(playerId);
        showingEnabled.remove(playerId);
        enabledMessageTimer.remove(playerId);
    }
}
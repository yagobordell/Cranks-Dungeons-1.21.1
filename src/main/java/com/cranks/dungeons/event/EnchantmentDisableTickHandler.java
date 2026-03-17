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

    private static final int ENABLED_MESSAGE_TICKS = 40;

    private static final Map<UUID, PlayerEnchantmentState> playerStates = new HashMap<>();

    private static class PlayerEnchantmentState {
        List<Identifier> currentEnchantments;
        boolean showingDisabled = false;
        boolean showingEnabled = false;
        int enabledMessageTimer = 0;

        PlayerEnchantmentState(List<Identifier> enchantments) {
            this.currentEnchantments = enchantments;
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (playerStates.isEmpty()) {
                return;
            }

            Iterator<Map.Entry<UUID, PlayerEnchantmentState>> iterator = playerStates.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, PlayerEnchantmentState> entry = iterator.next();
                ServerPlayerEntity player = server.getPlayerManager().getPlayerList().stream()
                        .filter(p -> p.getUuid().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);

                if (player == null) {
                    iterator.remove();
                    continue;
                }

                PlayerEnchantmentState state = entry.getValue();
                List<Identifier> currentEnchantments = state.currentEnchantments;

                String label = EnchantmentDisableManager.getDisabledLabel(entry.getKey(), currentEnchantments);

                if (label != null) {
                    player.sendMessage(
                            Text.literal("⚠ " + label + " Disabled ⚠")
                                    .formatted(Formatting.RED),
                            true
                    );
                    state.showingDisabled = true;
                    state.showingEnabled = false;
                    state.enabledMessageTimer = 0;
                } else {
                    if (state.enabledMessageTimer > 0) {
                        player.sendMessage(
                                Text.literal("✔ All Enchantments Enabled")
                                        .formatted(Formatting.GREEN),
                                true
                        );
                        state.enabledMessageTimer--;
                        state.showingEnabled = true;
                        state.showingDisabled = false;
                    } else if (state.showingDisabled || state.showingEnabled) {
                        player.sendMessage(Text.empty(), true);
                        state.showingDisabled = false;
                        state.showingEnabled = false;
                    }
                }
            }
        });
    }

    public static void onMainHandChanged(ServerPlayerEntity player, ItemStack newStack) {
        UUID uuid = player.getUuid();
        List<Identifier> relevant = EnchantmentDisableManager.getRelevantEnchantments(newStack);

        PlayerEnchantmentState state = playerStates.computeIfAbsent(uuid, k -> new PlayerEnchantmentState(relevant));
        state.currentEnchantments = relevant;
    }

    public static void triggerEnabledMessage(UUID playerId) {
        PlayerEnchantmentState state = playerStates.get(playerId);
        if (state != null) {
            state.enabledMessageTimer = ENABLED_MESSAGE_TICKS;
        } else {
            PlayerEnchantmentState newState = new PlayerEnchantmentState(Collections.emptyList());
            newState.enabledMessageTimer = ENABLED_MESSAGE_TICKS;
            playerStates.put(playerId, newState);
        }
    }

    public static void removePlayer(UUID playerId) {
        playerStates.remove(playerId);
    }
}
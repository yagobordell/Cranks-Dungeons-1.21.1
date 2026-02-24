// src/main/java/com/cranks/dungeons/network/ModNetworking.java
package com.cranks.dungeons.network;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.event.EnchantmentDisableTickHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(
                ToggleEnchantmentDisablePayload.ID,
                ToggleEnchantmentDisablePayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(DamageNumberPayload.ID, DamageNumberPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                ToggleEnchantmentDisablePayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        ItemStack tool = player.getMainHandStack();

                        List<Identifier> relevant = EnchantmentDisableManager.getRelevantEnchantments(tool);

                        if (relevant.isEmpty()) {
                            player.sendMessage(
                                    Text.literal("No toggleable enchantments on this tool")
                                            .formatted(Formatting.GRAY),
                                    true
                            );
                            return;
                        }

                        EnchantmentDisableManager.toggleNext(player.getUuid(), relevant);

                        String label = EnchantmentDisableManager.getDisabledLabel(player.getUuid(), relevant);
                        if (label == null) {
                            // Toggled to "all enabled"
                            EnchantmentDisableTickHandler.triggerEnabledMessage(player.getUuid());
                        }
                        // If something is disabled, the tick handler will show it continuously
                    });
                }
        );
    }
}
package com.cranks.dungeons.client;

import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.registry.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class CombatAudioHelper {

    public static void playCombatSound(DamageElement element, double damage, boolean isCrit) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        SoundEvent sound = getSoundForElement(element, isCrit);
        if (sound == null) return;
        
        // Pitch logic: Higher damage = slightly higher pitch (sharper hit)
        // Range: 0.95 to 1.5, capped
        float pitch = 0.95f + (float) Math.min(damage / 150.0, 0.55);
        
        // Crits get an extra boost in pitch and volume
        float volume = isCrit ? 0.6f : 0.4f;
        if (isCrit) {
            pitch += 0.1f;
        }

        client.world.playSound(
                client.player,
                client.player.getX(), client.player.getY(), client.player.getZ(),
                sound,
                SoundCategory.PLAYERS,
                volume,
                pitch
        );
    }

    private static SoundEvent getSoundForElement(DamageElement element, boolean isCrit) {
        if (isCrit) return ModSounds.CRITICAL_HIT;

        return switch (element) {
            case FIRE -> ModSounds.ELEMENTAL_FIRE_HIT;
            case COLD -> ModSounds.ELEMENTAL_COLD_HIT;
            case LIGHTNING -> ModSounds.ELEMENTAL_LIGHTNING_HIT;
            case VOID -> ModSounds.ELEMENTAL_VOID_HIT;
            case POISON -> ModSounds.POISON_TICK; // Using tick as hit sound for now
            case DROWN -> null; // Silence drown hits if no custom sound
            default -> null;
        };
    }
}

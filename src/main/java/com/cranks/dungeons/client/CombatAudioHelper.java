package com.cranks.dungeons.client;

import com.cranks.dungeons.damage.DamageElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class CombatAudioHelper {

    public static void playCombatSound(DamageElement element, double damage, boolean isCrit) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        SoundEvent sound = getSoundForElement(element, isCrit);
        
        // Pitch logic: Higher damage = slightly higher pitch (sharper hit)
        // Range: 0.95 to 1.5, capped
        float pitch = 0.95f + (float) Math.min(damage / 150.0, 0.55);
        
        // Crits get an extra boost in pitch and volume
        float volume = isCrit ? 1.0f : 0.7f;
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
        if (isCrit) return SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;

        return switch (element) {
            case FIRE -> SoundEvents.ITEM_FIRECHARGE_USE;
            case COLD -> SoundEvents.BLOCK_POWDER_SNOW_BREAK;
            case LIGHTNING -> SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
            case VOID -> SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
            case POISON -> SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE; 
            case DROWN -> SoundEvents.ENTITY_PLAYER_HURT_DROWN;
            default -> SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
        };
    }
}

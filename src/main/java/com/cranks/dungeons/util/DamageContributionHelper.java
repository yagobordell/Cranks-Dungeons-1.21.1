package com.cranks.dungeons.util;

import net.minecraft.entity.LivingEntity;
import java.util.UUID;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public final class DamageContributionHelper {
    private DamageContributionHelper() {}

    public static void record(LivingEntity target, DamageSource source, float actualDamage) {
        if (actualDamage <= 0.0f || target.getWorld().isClient()
                || !(target instanceof com.cranks.dungeons.progression.CombatEncounterAccess)) {
            return;
        }
        PlayerEntity player = source.getAttacker() instanceof PlayerEntity attacker ? attacker : null;
        if (player != null) com.cranks.dungeons.progression.CombatExperience.damage(target, player.getUuid(), actualDamage);
    }

    public static void recordAilment(LivingEntity target, UUID source, float damage) {
        if (damage > 0 && source != null && target instanceof com.cranks.dungeons.progression.CombatEncounterAccess
                && target.getWorld() instanceof net.minecraft.server.world.ServerWorld world
                && world.getServer().getPlayerManager().getPlayer(source) != null) {
            com.cranks.dungeons.progression.CombatExperience.damage(target, source, damage);
        }
    }
}

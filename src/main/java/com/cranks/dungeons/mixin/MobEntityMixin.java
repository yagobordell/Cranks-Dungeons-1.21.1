package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.MobDamageTracker;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements MobDamageTracker {
    @Unique
    private final Map<UUID, Float> damageContributions = new HashMap<>();

    @Override
    public Map<UUID, Float> cranks$getDamageContributions() {
        return damageContributions;
    }

    @Override
    public void cranks$addDamageContribution(UUID playerUuid, float damage) {
        damageContributions.merge(playerUuid, damage, Float::sum);
    }

    @Override
    public void cranks$clearDamageContributions() {
        damageContributions.clear();
    }
}

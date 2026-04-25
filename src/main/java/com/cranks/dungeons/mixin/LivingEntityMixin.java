package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import com.cranks.dungeons.util.DamageBreakdownProvider;
import com.cranks.dungeons.util.DamageConversionHelper;
import com.cranks.dungeons.util.ReactionCooldownProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DamageBreakdownProvider, AilmentHistoryProvider, ReactionCooldownProvider {

    @Unique
    private final Map<String, Long> cranks$reactionCooldowns = new HashMap<>();

    @Override
    public boolean cranks$canTriggerReaction(String type) {
        long currentTime = ((LivingEntity) (Object) this).getEntityWorld().getTime();
        return currentTime >= cranks$reactionCooldowns.getOrDefault(type, 0L);
    }

    @Override
    public void cranks$setReactionCooldown(String type, int ticks) {
        long currentTime = ((LivingEntity) (Object) this).getEntityWorld().getTime();
        cranks$reactionCooldowns.put(type, currentTime + ticks);
    }

    @Unique
    private DamageConversionHelper.DamageBreakdown cranks$lastDamageBreakdown;

    @Unique
    private float cranks$accumulatedHeal = 0;
    @Unique
    private int cranks$healTimer = 0;

    @Unique
    private final Set<UUID> cranks$recentBurnChains = new HashSet<>();

    @Override
    public DamageConversionHelper.DamageBreakdown cranks$getLastDamageBreakdown() {
        return cranks$lastDamageBreakdown;
    }

    @Override
    public void cranks$setLastDamageBreakdown(DamageConversionHelper.DamageBreakdown breakdown) {
        this.cranks$lastDamageBreakdown = breakdown;
    }

    @Override
    public void cranks$recordBurnChain(UUID chainUuid) {
        if (chainUuid != null) {
            cranks$recentBurnChains.add(chainUuid);
        }
    }

    @Override
    public boolean cranks$wasHitByChain(UUID chainUuid) {
        return chainUuid != null && cranks$recentBurnChains.contains(chainUuid);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        AilmentManager.onEntityDeath((LivingEntity) (Object) this);
    }

    /**
     * Prevents players from dropping XP on death (Yarn 1.21.1 signature).
     */
    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void preventPlayerXpDrop(ServerWorld world, Entity attacker, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "heal", at = @At("HEAD"))
    private void onHeal(float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient() || amount <= 0) return;

        float actualHeal = Math.min(amount, entity.getMaxHealth() - entity.getHealth());
        if (actualHeal <= 0) return;

        cranks$accumulatedHeal += actualHeal;
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"), cancellable = true)
    private static void addCustomAttributesToAllEntities(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();

        builder
                // Defensive
                .add(ModAttributes.FIRE_RESISTANCE)
                .add(ModAttributes.COLD_RESISTANCE)
                .add(ModAttributes.LIGHTNING_RESISTANCE)
                .add(ModAttributes.VOID_RESISTANCE)
                .add(ModAttributes.KNOCKBACK_RESISTANCE)
                .add(ModAttributes.BONUS_ARMOR)
                .add(ModAttributes.BONUS_ARMOR_TOUGHNESS)
                .add(ModAttributes.LIFE_REGENERATION)
                .add(ModAttributes.MAX_HEALTH_BONUS)
                .add(ModAttributes.FEATHER_FALLING)
                .add(ModAttributes.ENERGY_SHIELD)
                .add(ModAttributes.EVASION_RATING)

                // Offensive
                .add(ModAttributes.CRIT_CHANCE)
                .add(ModAttributes.CRIT_MULTIPLIER)
                .add(ModAttributes.BONUS_ATTACK_DAMAGE)
                .add(ModAttributes.FIRE_DAMAGE)
                .add(ModAttributes.COLD_DAMAGE)
                .add(ModAttributes.LIGHTNING_DAMAGE)
                .add(ModAttributes.VOID_DAMAGE)
                .add(ModAttributes.LIFE_STEAL)
                .add(ModAttributes.CHANCE_TO_BURN)
                .add(ModAttributes.BURN_BASE_DAMAGE)
                .add(ModAttributes.BURN_DAMAGE_PERCENT)
                .add(ModAttributes.CHANCE_TO_POISON)
                .add(ModAttributes.POISON_BASE_DAMAGE)
                .add(ModAttributes.POISON_DAMAGE_PERCENT)
                .add(ModAttributes.CHANCE_TO_FREEZE)
                .add(ModAttributes.ATTACK_RANGE)
                .add(ModAttributes.ATTACK_SPEED)
                .add(ModAttributes.KNOCKBACK)

                // Utility
                .add(ModAttributes.MINING_EFFICIENCY)
                .add(ModAttributes.FORTUNE)
                .add(ModAttributes.DURABILITY_BONUS)
                .add(ModAttributes.EXPERIENCE_BONUS)
                .add(ModAttributes.PRECISION_MINING)
                .add(ModAttributes.BREAKING_RANGE)
                .add(ModAttributes.MOVEMENT_SPEED)
                .add(ModAttributes.FLYING_SPEED)
                .add(ModAttributes.LUCK)
                .add(ModAttributes.CROP_FORTUNE)
                .add(ModAttributes.LOG_FORTUNE)
                .add(ModAttributes.DIG_FORTUNE)
                .add(ModAttributes.CROP_EXPERIENCE)

                // Percentage damage
                .add(ModAttributes.ATTACK_DAMAGE_PERCENT)
                .add(ModAttributes.FIRE_DAMAGE_PERCENT)
                .add(ModAttributes.COLD_DAMAGE_PERCENT)
                .add(ModAttributes.LIGHTNING_DAMAGE_PERCENT)
                .add(ModAttributes.VOID_DAMAGE_PERCENT)
                .add(ModAttributes.SHOCK_DURATION)
                .add(ModAttributes.REACTION_DAMAGE_PERCENT)
                .add(ModAttributes.REACTION_COOLDOWN_PERCENT)
                .add(ModAttributes.REACTION_RADIUS_PERCENT)
                .add(ModAttributes.RELOAD_SPEED);

        cir.setReturnValue(builder);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickLivingEntity(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getEntityWorld().isClient()) return;

        AilmentManager.tickAilments(entity);

        // Show accumulated healing
        if (cranks$accumulatedHeal > 0) {
            cranks$healTimer++;
            if (cranks$accumulatedHeal >= 1.0f || cranks$healTimer >= 10) {
                DamageNumberSender.broadcastHeal((ServerWorld) entity.getEntityWorld(), entity, (double)Math.round(cranks$accumulatedHeal * 10.0) / 10.0);
                cranks$accumulatedHeal = 0;
                cranks$healTimer = 0;
            }
        }
    }

}

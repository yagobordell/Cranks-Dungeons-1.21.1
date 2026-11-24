package com.cranks.dungeons.mixin;

import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.DamageHit;
import com.cranks.dungeons.util.DamageContributionHelper;
import com.cranks.dungeons.network.EntityShieldSyncPacket;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.ailment.AilmentManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 900)
public class EnergyShieldDamageMixin {

    @WrapOperation(method = "damage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void cranks$applyResolvedHit(LivingEntity target, DamageSource source, float amount,
                                        Operation<Void> original) {
        DamageHit hit = DamageHit.forTarget(target);
        if (hit == null || !(target.getWorld() instanceof ServerWorld world)) {
            original.call(target, source, amount);
            return;
        }

        hit.finishBlock();
        float remaining = absorbShield(world, target, source, amount, hit);
        if (remaining > 0) {
            float healthBefore = target.getHealth();
            float absorptionBefore = target.getAbsorptionAmount();
            original.call(target, source, remaining);
            float applied = Math.max(0, healthBefore - target.getHealth())
                    + Math.max(0, absorptionBefore - target.getAbsorptionAmount());
            hit.recordDamageToLife(applied);
            DamageContributionHelper.record(target, source, applied);
        }
        if (!AilmentManager.isSecondaryDamage(target)
                && (hit.damageToLife() > 0 || hit.damageToShield() > 0)) reflectThorns(target, source);
    }

    @Unique
    private float absorbShield(ServerWorld world, LivingEntity target, DamageSource source, float amount, DamageHit hit) {
        if (amount <= 0 || AilmentManager.bypassesShield(target) || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.isOf(DamageTypes.DROWN) || source.isOf(DamageTypes.STARVE)
                || AilmentManager.isPoisonDamageInProgress(target.getUuid())
                || AilmentManager.isBleedDamageInProgress(target.getUuid())
                || target.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) return amount;

        EnergyShieldProvider provider = (EnergyShieldProvider) target;
        provider.cranksdungeons$setLastDamageTime(world.getTime());
        float current = provider.cranksdungeons$getCurrentEnergyShield();
        float max = (float) target.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (current <= 0 || max <= 0) return amount;

        var breakdown = hit.breakdown();
        float total = breakdown == null ? 0 : breakdown.getTotal();
        float voidFraction = total > 0 ? breakdown.get(DamageElement.VOID) / total : 0;
        float cost = amount * (1.0f + voidFraction);
        float spent = Math.min(current, cost);
        float absorbed = amount * (spent / cost);
        float left = current - spent;
        provider.cranksdungeons$setCurrentEnergyShield(left);

        hit.recordDamageToShield(absorbed);
        DamageContributionHelper.record(target, source, spent);
        triggerShieldVisuals(world, target, current, left);
        if (target instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, new EnergyShieldSyncPacket(left, max));
        }
        var packet = new EntityShieldSyncPacket(target.getId(), left, max);
        PlayerLookup.tracking(target).forEach(player -> ServerPlayNetworking.send(player, packet));
        return Math.max(0, amount - absorbed);
    }

    @Unique
    private void reflectThorns(LivingEntity target, DamageSource source) {
        if (source.isOf(DamageTypes.THORNS) || !(source.getSource() instanceof LivingEntity attacker)
                || attacker == target) return;
        double thorns = cranks$getTotalThorns(target);
        if (thorns <= 0) return;
        int timeUntilRegen = attacker.timeUntilRegen;
        try {
            attacker.timeUntilRegen = 0;
            attacker.damage(target.getDamageSources().thorns(target), (float) thorns);
        } finally {
            attacker.timeUntilRegen = timeUntilRegen;
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cranks$stopZeroDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount > 0) return;
        DamageHit hit = DamageHit.forTarget((LivingEntity) (Object) this);
        if (hit != null) hit.finishBlock();
        cir.setReturnValue(false);
    }

    @Unique
    private void triggerShieldVisuals(ServerWorld world, LivingEntity self, float prevShield, float newShield) {
        SoundCategory category = self instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
        world.playSoundFromEntity(null, self, ModSounds.ENERGY_SHIELD_HIT, category, 1.0f, 1.0f);

        if (prevShield >= 1.0f && newShield < 1.0f) {
            world.playSoundFromEntity(null, self, ModSounds.ENERGY_SHIELD_BREAK, category, 1.0f, 1.0f);
            world.spawnParticles(ParticleTypes.END_ROD, self.getX(), self.getY() + self.getHeight() / 2.0, self.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Unique
    private static double cranks$getTotalThorns(LivingEntity entity) {
        return entity.getAttributeValue(ModAttributes.THORNS_DAMAGE);
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.effect.PotionCloudAccess;
import com.cranks.dungeons.effect.PotionRules;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(AreaEffectCloudEntity.class)
abstract class PotionCloudMixin implements PotionCloudAccess {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean failedMixtureProtection(LivingEntity target, StatusEffectInstance effect, Entity source, Operation<Boolean> original) {
        var cloud = (AreaEffectCloudEntity) (Object) this;
        if (com.cranks.dungeons.alchemy.AlchemyPotions.isFailure(potionContentsComponent)
                && !com.cranks.dungeons.alchemy.AlchemyPotions.canApplyFailure(cloud.getOwner(), target)) return false;
        return original.call(target, effect, source);
    }
    @Shadow private int reapplicationDelay;
    @Shadow private PotionContentsComponent potionContentsComponent;
    @Unique private boolean cranks$potionCloud;
    @Unique private final Set<UUID> cranks$affected = new HashSet<>();

    @Override
    public void cranks$configurePotionCloud() {
        cranks$potionCloud = true;
        var cloud = (AreaEffectCloudEntity) (Object) this;
        cloud.setDuration(PotionRules.CLOUD_DURATION);
        cloud.setWaitTime(PotionRules.CLOUD_WAIT);
        cloud.setRadiusOnUse(0);
        cloud.setRadiusGrowth(Math.min(0, (0.5f - cloud.getRadius()) / PotionRules.CLOUD_DURATION));
        reapplicationDelay = PotionRules.CLOUD_DURATION + PotionRules.CLOUD_WAIT + 1;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"))
    private boolean alreadyAffected(Map<Entity, Integer> map, Object target, Operation<Boolean> original) {
        return cranks$potionCloud && target instanceof Entity entity && cranks$affected.contains(entity.getUuid())
                || original.call(map, target);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object rememberAffected(Map<Entity, Integer> map, Object target, Object nextTime, Operation<Object> original) {
        if (cranks$potionCloud && target instanceof Entity entity) cranks$affected.add(entity.getUuid());
        return original.call(map, target, nextTime);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V"))
    private void applyCloudInstant(StatusEffect effect, Entity source, Entity attacker, LivingEntity target,
                                    int amplifier, double proximity, Operation<Void> original) {
        original.call(effect, source, attacker, target, amplifier,
                cranks$potionCloud ? (double) PotionRules.CLOUD_INSTANT_MULTIPLIER : proximity);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean scaleCustomEffects(List<StatusEffectInstance> list, Collection<StatusEffectInstance> effects,
                                       Operation<Boolean> original) {
        if (!cranks$potionCloud) return original.call(list, effects);
        var scaled = effects.stream().map(effect -> new StatusEffectInstance(effect.getEffectType(),
                effect.mapDuration(duration -> Math.max(1, (int) (duration * PotionRules.CLOUD_DURATION_MULTIPLIER))),
                effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon())).toList();
        return original.call(list, scaled);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void saveApplications(NbtCompound nbt, CallbackInfo ci) {
        if (!cranks$potionCloud) return;
        nbt.putBoolean("CranksPotionCloud", true);
        var targets = new NbtList();
        for (UUID uuid : cranks$affected) {
            var target = new NbtCompound();
            target.putUuid("Id", uuid);
            targets.add(target);
        }
        nbt.put("CranksPotionTargets", targets);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void loadApplications(NbtCompound nbt, CallbackInfo ci) {
        cranks$potionCloud = nbt.getBoolean("CranksPotionCloud");
        if (!nbt.contains("CranksPotionCloud") && potionContentsComponent.potion().isPresent()) cranks$configurePotionCloud();
        cranks$affected.clear();
        var targets = nbt.getList("CranksPotionTargets", NbtElement.COMPOUND_TYPE);
        for (int index = 0; index < targets.size(); index++) {
            var target = targets.getCompound(index);
            if (target.containsUuid("Id")) cranks$affected.add(target.getUuid("Id"));
        }
    }
}

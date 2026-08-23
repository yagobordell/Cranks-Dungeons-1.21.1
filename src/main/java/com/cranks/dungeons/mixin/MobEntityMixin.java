package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.MobEquipmentManager;
import com.cranks.dungeons.util.MobDamageTracker;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "initialize", at = @At("TAIL"))
    private void cranksDungeons$onInitialize(
            ServerWorldAccess world,
            LocalDifficulty difficulty,
            SpawnReason spawnReason,
            @Nullable EntityData entityData,
            CallbackInfoReturnable<EntityData> cir
    ) {
        MobEquipmentManager.equipAct1Mob((MobEntity) (Object) this, world.getRandom(), difficulty);
    }

    @Inject(method = "enchantEquipment(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/world/LocalDifficulty;)V", at = @At("HEAD"), cancellable = true)
    private void cranksDungeons$cancelEnchantEquipment(ServerWorldAccess world, net.minecraft.util.math.random.Random random, LocalDifficulty localDifficulty, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "enchantEquipment(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/util/math/random/Random;FLnet/minecraft/world/LocalDifficulty;)V", at = @At("HEAD"), cancellable = true)
    private void cranksDungeons$cancelEnchantEquipmentSlot(ServerWorldAccess world, net.minecraft.entity.EquipmentSlot slot, net.minecraft.util.math.random.Random random, float power, LocalDifficulty localDifficulty, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "enchantMainHandEquipment", at = @At("HEAD"), cancellable = true)
    private void cranksDungeons$cancelEnchantMainHand(ServerWorldAccess world, net.minecraft.util.math.random.Random random, LocalDifficulty localDifficulty, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ci.cancel();
    }
}

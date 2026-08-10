package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ManaProvider;
import com.cranks.dungeons.network.ManaSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ManaDataMixin implements ManaProvider {

    @Unique
    private float currentMana = 100.0f;

    @Unique
    private float lastSyncedMana = -1.0f;

    @Unique
    private int syncTimer = 0;

    @Override
    public float cranksdungeons$getCurrentMana() {
        return currentMana;
    }

    @Override
    public void cranksdungeons$setCurrentMana(float mana) {
        this.currentMana = mana;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void syncManaToClient(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getEntityWorld().isClient()) return;

        syncTimer++;
        if (syncTimer >= 10) {
            syncTimer = 0;
            if (Math.abs(currentMana - lastSyncedMana) > 0.05f) {
                lastSyncedMana = currentMana;

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    double maxMana = player.getAttributeValue(ModAttributes.MAX_MANA);
                    ServerPlayNetworking.send(serverPlayer, new ManaSyncPacket(currentMana, (float) maxMana));
                }
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    protected void writeManaData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("Mana", currentMana);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void readManaData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Mana")) {
            this.currentMana = nbt.getFloat("Mana");
        }
    }
}

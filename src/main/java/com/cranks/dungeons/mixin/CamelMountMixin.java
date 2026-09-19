package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds persistent mount progression, chest state, and cargo to camels. */
@Mixin(CamelEntity.class)
public abstract class CamelMountMixin implements MountDataProvider {
    @Unique private static final TrackedData<Boolean> CRANKS_CHEST = DataTracker.registerData(
            CamelEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private MountData cranks$mountData = new MountData();

    @Override public MountData cranksdungeons$getMountData() { return cranks$mountData; }

    @Override public boolean cranksdungeons$hasMountChest() {
        return ((CamelEntity) (Object) this).getDataTracker().get(CRANKS_CHEST);
    }

    @Override public void cranksdungeons$setMountChest(boolean equipped) {
        cranks$mountData.chestEquipped = equipped;
        ((CamelEntity) (Object) this).getDataTracker().set(CRANKS_CHEST, equipped);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void cranks$trackChest(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CRANKS_CHEST, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void cranks$writeMountData(NbtCompound nbt, CallbackInfo ci) {
        CamelEntity self = (CamelEntity) (Object) this;
        cranks$mountData.chestEquipped = cranksdungeons$hasMountChest();
        nbt.put("CranksMountData", cranks$mountData.toNbt(self.getRegistryManager(), true));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void cranks$readMountData(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("CranksMountData")) return;
        CamelEntity self = (CamelEntity) (Object) this;
        cranks$mountData.readNbt(nbt.getCompound("CranksMountData"), self.getRegistryManager(), true);
        cranksdungeons$setMountChest(cranks$mountData.chestEquipped);
    }

    @Inject(method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/CamelEntity;",
            at = @At("RETURN"))
    private void cranks$inheritGenetics(ServerWorld world, PassiveEntity mate,
                                        CallbackInfoReturnable<CamelEntity> cir) {
        if (mate instanceof CamelEntity other && cir.getReturnValue() != null) {
            MountManager.inheritSameKind((CamelEntity) (Object) this, other, cir.getReturnValue());
        }
    }
}

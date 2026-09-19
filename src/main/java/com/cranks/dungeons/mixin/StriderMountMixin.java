package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountSaddleAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(StriderEntity.class)
public abstract class StriderMountMixin implements MountDataProvider, MountSaddleAccess {
    @Unique private static final TrackedData<Boolean> CRANKS_CHEST = DataTracker.registerData(
            StriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private MountData cranks$mountData = new MountData();

    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final
    private SaddledComponent saddledComponent;

    @Override public boolean cranksdungeons$isSaddled() {
        return ((StriderEntity) (Object) this).isSaddled();
    }

    @Override public void cranksdungeons$setSaddled(boolean saddled) {
        saddledComponent.setSaddled(saddled);
    }

    @Override public MountData cranksdungeons$getMountData() { return cranks$mountData; }
    @Override public boolean cranksdungeons$hasMountChest() {
        return ((StriderEntity) (Object) this).getDataTracker().get(CRANKS_CHEST);
    }
    @Override public void cranksdungeons$setMountChest(boolean equipped) {
        cranks$mountData.chestEquipped = equipped;
        ((StriderEntity) (Object) this).getDataTracker().set(CRANKS_CHEST, equipped);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void cranks$trackChest(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CRANKS_CHEST, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void cranks$writeMountData(NbtCompound nbt, CallbackInfo ci) {
        StriderEntity self = (StriderEntity) (Object) this;
        cranks$mountData.chestEquipped = cranksdungeons$hasMountChest();
        nbt.put("CranksMountData", cranks$mountData.toNbt(self.getRegistryManager(), true));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void cranks$readMountData(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("CranksMountData")) return;
        StriderEntity self = (StriderEntity) (Object) this;
        cranks$mountData.readNbt(nbt.getCompound("CranksMountData"), self.getRegistryManager(), true);
        cranksdungeons$setMountChest(cranks$mountData.chestEquipped);
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void cranks$dropCargo(CallbackInfo ci) {
        StriderEntity self = (StriderEntity) (Object) this;
        for (ItemStack stack : cranks$mountData.cargo.clearToList()) self.dropStack(stack);
        if (cranksdungeons$hasMountChest()) self.dropStack(new ItemStack(net.minecraft.item.Items.CHEST));
    }

    @Inject(method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/StriderEntity;",
            at = @At("RETURN"))
    private void cranks$inheritGenetics(ServerWorld world, PassiveEntity mate,
                                        CallbackInfoReturnable<StriderEntity> cir) {
        if (mate instanceof StriderEntity other && cir.getReturnValue() != null) {
            com.cranks.dungeons.mount.MountManager.inheritSameKind(
                    (StriderEntity) (Object) this, other, cir.getReturnValue());
        }
    }
}

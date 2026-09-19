package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.AbstractHorseInventoryAccess;
import com.cranks.dungeons.mount.MountData;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountSaddleAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseMountMixin
        implements MountDataProvider, AbstractHorseInventoryAccess, MountSaddleAccess {
    @Unique
    private static final TrackedData<Boolean> CRANKS_MOUNT_CHEST = DataTracker.registerData(
            AbstractHorseEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique
    private static final TrackedData<Integer> CRANKS_CARGO_CAPACITY = DataTracker.registerData(
            AbstractHorseEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique private MountData cranks$mountData = new MountData();

    @Shadow protected SimpleInventory items;

    @Shadow protected abstract void onChestedStatusChanged();

    @Shadow protected abstract void updateSaddledFlag();

    @Shadow protected abstract void setHorseFlag(int flag, boolean value);

    @Override
    public boolean cranksdungeons$isSaddled() {
        return ((AbstractHorseEntity) (Object) this).isSaddled();
    }

    @Override
    public void cranksdungeons$setSaddled(boolean saddled) {
        // Camel and other generic AbstractHorse mounts keep the real saddle
        // in inventory slot 0, just like the vanilla horse screen does.
        if (items != null) {
            items.setStack(0, saddled
                    ? new ItemStack(net.minecraft.item.Items.SADDLE)
                    : ItemStack.EMPTY);
        } else {
            setHorseFlag(4, saddled);
        }
        updateSaddledFlag();
    }

    @Override
    public MountData cranksdungeons$getMountData() {
        return cranks$mountData;
    }

    @Override
    public boolean cranksdungeons$hasMountChest() {
        if ((Object) this instanceof AbstractDonkeyEntity donkey) return donkey.hasChest();
        return ((AbstractHorseEntity) (Object) this).getDataTracker().get(CRANKS_MOUNT_CHEST);
    }

    @Override
    public void cranksdungeons$setMountChest(boolean equipped) {
        cranks$mountData.chestEquipped = equipped;
        if ((Object) this instanceof AbstractDonkeyEntity donkey) {
            donkey.setHasChest(equipped);
        } else {
            ((AbstractHorseEntity) (Object) this).getDataTracker().set(CRANKS_MOUNT_CHEST, equipped);
        }
    }

    @Override public int cranksdungeons$getSyncedCargoCapacity() {
        return ((AbstractHorseEntity) (Object) this).getDataTracker().get(CRANKS_CARGO_CAPACITY);
    }

    @Override public void cranksdungeons$setSyncedCargoCapacity(int capacity) {
        ((AbstractHorseEntity) (Object) this).getDataTracker().set(CRANKS_CARGO_CAPACITY, capacity);
    }

    @Override
    public SimpleInventory cranksdungeons$getHorseItems() {
        return items;
    }

    @Override
    public void cranksdungeons$resizeHorseInventory() {
        onChestedStatusChanged();
    }

    /**
     * The stable backing inventory bypasses vanilla's inventory replacement
     * path.  Refresh the tracked saddle flag explicitly so saddle insertion
     * still updates riding controls and the saddle model immediately.
     */
    @Inject(method = "onInventoryChanged", at = @At("TAIL"))
    private void cranks$refreshSaddleFlag(Inventory sender, CallbackInfo ci) {
        if (MountKind.of((AbstractHorseEntity) (Object) this) != MountKind.UNSUPPORTED) {
            updateSaddledFlag();
        }
    }

    /**
     * Keep a stable, maximum-sized backing inventory for supported mounts.
     * Vanilla replaces the inventory object whenever a chest is toggled;
     * replacing it while HorseScreenHandler is open causes the screen to be
     * discarded. Disabled slots keep the unused genetic capacity inaccessible
     * without changing the handler shape.
     */
    @Inject(method = "onChestedStatusChanged", at = @At("HEAD"), cancellable = true)
    private void cranks$stableMountInventory(CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        if (MountKind.of(self) == MountKind.UNSUPPORTED) return;
        int desiredSize = 1 + MountManager.maxHorseCargoCapacity(self);
        if (items == null || items.size() != desiredSize) {
            SimpleInventory replacement = new SimpleInventory(desiredSize);
            if (items != null) {
                int count = Math.min(items.size(), replacement.size());
                for (int index = 0; index < count; index++) {
                    ItemStack stack = items.getStack(index);
                    if (!stack.isEmpty()) replacement.setStack(index, stack.copy());
                }
            }
            // Vanilla wires the original inventory to AbstractHorseEntity's
            // InventoryChangedListener in its constructor.  Replacing that
            // inventory without restoring the listener leaves saddles (and
            // their riding controls/model flag) stale.
            replacement.addListener((InventoryChangedListener) (Object) self);
            items = replacement;
        }
        ci.cancel();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void cranks$trackChest(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CRANKS_MOUNT_CHEST, false);
        builder.add(CRANKS_CARGO_CAPACITY, 0);
    }

    @Inject(method = "getInventoryColumns", at = @At("HEAD"), cancellable = true)
    private void cranks$customCargoColumns(CallbackInfoReturnable<Integer> cir) {
        if (cranks$mountData == null
                || MountKind.of((AbstractHorseEntity) (Object) this) == MountKind.UNSUPPORTED) return;
        int capacity = MountManager.maxHorseCargoCapacity((AbstractHorseEntity) (Object) this);
        cir.setReturnValue((capacity + 2) / 3);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void cranks$writeMountData(NbtCompound nbt, CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        cranks$mountData.chestEquipped = cranksdungeons$hasMountChest();
        NbtCompound data = cranks$mountData.toNbt(self.getRegistryManager(), false);
        if (!(self instanceof AbstractDonkeyEntity) && items != null && items.size() > 1) {
            data.put("HorseCargo", items.toNbtList(self.getRegistryManager()));
        }
        nbt.put("CranksMountData", data);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void cranks$prepareMountInventory(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("CranksMountData")) return;
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        NbtCompound data = nbt.getCompound("CranksMountData");
        cranks$mountData.readNbt(data, self.getRegistryManager(), false);
        cranksdungeons$setSyncedCargoCapacity(MountManager.cargoCapacity(self));
        if (!(self instanceof AbstractDonkeyEntity)) {
            cranksdungeons$setMountChest(cranks$mountData.chestEquipped);
            onChestedStatusChanged();
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void cranks$readMountCargo(NbtCompound nbt, CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        if (self instanceof AbstractDonkeyEntity || !nbt.contains("CranksMountData")) return;
        NbtCompound data = nbt.getCompound("CranksMountData");
        if (data.contains("HorseCargo") && items != null) {
            items.readNbtList(data.getList("HorseCargo", net.minecraft.nbt.NbtElement.COMPOUND_TYPE),
                    self.getRegistryManager());
        }
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void cranks$dropAttachedChest(CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        if (!(self instanceof AbstractDonkeyEntity) && cranksdungeons$hasMountChest()) {
            self.dropStack(new ItemStack(net.minecraft.item.Items.CHEST));
        }
    }
}

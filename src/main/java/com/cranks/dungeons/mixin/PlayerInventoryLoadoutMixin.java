package com.cranks.dungeons.mixin;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.inventory.PlayerLoadoutAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerInventory.class)
abstract class PlayerInventoryLoadoutMixin implements PlayerLoadoutAccess {
    @Shadow @Final @Mutable private List<DefaultedList<ItemStack>> combinedInventory;
    @Shadow @Final public PlayerEntity player;
    @Shadow public int selectedSlot;
    @Shadow public abstract ItemStack getStack(int slot);
    @Shadow public abstract void setStack(int slot, ItemStack stack);
    @Unique private PlayerLoadout cranks$loadout;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addLoadout(PlayerEntity player, CallbackInfo ci) {
        cranks$loadout = new PlayerLoadout((PlayerInventory) (Object) this);
        List<DefaultedList<ItemStack>> all = new ArrayList<>(combinedInventory);
        all.add(cranks$loadout.stacks());
        combinedInventory = List.copyOf(all);
    }

    @Override
    public PlayerLoadout cranks$getLoadout() {
        return cranks$loadout;
    }

    @Inject(method = "setStack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;",
            shift = At.Shift.AFTER))
    private void recordAcquiredItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity && !stack.isEmpty()) {
            var discoveries = ModComponents.ITEM_DISCOVERIES.getNullable(player);
            if (discoveries != null) discoveries.record(stack);
        }
    }

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    private void includeLoadoutInSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(PlayerLoadout.END);
    }

    @Inject(method = "clone", at = @At("RETURN"))
    private void copyArmorSet(PlayerInventory previous, CallbackInfo ci) {
        cranks$loadout.setSecondSetActive(PlayerLoadout.of(previous).secondSetActive());
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void insertInStorageOrder(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot != -1) return;
        boolean inserted = cranks$loadout.insert(stack);
        if (!inserted && !stack.isEmpty() && player.isInCreativeMode()) {
            stack.setCount(0);
            inserted = true;
        }
        cir.setReturnValue(inserted);
    }

    @Inject(method = "isValidHotbarIndex", at = @At("HEAD"), cancellable = true)
    private static void allowTenthSlot(int index, CallbackInfoReturnable<Boolean> cir) {
        if (index == PlayerLoadout.EXTRA_HOTBAR) cir.setReturnValue(true);
    }

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void scrollTenSlots(double amount, CallbackInfo ci) {
        int offset = (int) Math.signum(amount);
        int index = PlayerLoadout.getHotbarIndex(selectedSlot);
        selectedSlot = PlayerLoadout.getHotbarSlot(Math.floorMod(index - offset, PlayerLoadout.HOTBAR_COUNT));
        ci.cancel();
    }

    @Inject(method = "getSwappableHotbarSlot", at = @At("HEAD"), cancellable = true)
    private void pickBlockCanUseTenthSlot(CallbackInfoReturnable<Integer> cir) {
        int start = PlayerLoadout.getHotbarIndex(selectedSlot);
        for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) {
            int slot = PlayerLoadout.getHotbarSlot((start + i) % PlayerLoadout.HOTBAR_COUNT);
            if (getStack(slot).isEmpty()) {
                cir.setReturnValue(slot);
                return;
            }
        }
        for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) {
            int slot = PlayerLoadout.getHotbarSlot((start + i) % PlayerLoadout.HOTBAR_COUNT);
            if (!getStack(slot).hasEnchantments()) {
                cir.setReturnValue(slot);
                return;
            }
        }
        cir.setReturnValue(selectedSlot);
    }

    @Inject(method = "getSlotWithStack", at = @At("RETURN"), cancellable = true)
    private void findPickedStackInTenthSlot(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() < 0 && ItemStack.areItemsAndComponentsEqual(getStack(PlayerLoadout.EXTRA_HOTBAR), stack)) {
            cir.setReturnValue(PlayerLoadout.EXTRA_HOTBAR);
        }
    }

    @Redirect(method = {"getMainHandStack", "getBlockBreakingSpeed", "addPickBlock", "swapSlotWithHotbar"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;"))
    private Object readHotbarSlot(DefaultedList<ItemStack> slots, int slot) {
        return slot == PlayerLoadout.EXTRA_HOTBAR ? getStack(slot) : slots.get(slot);
    }

    @Redirect(method = {"addPickBlock", "swapSlotWithHotbar"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private Object writeHotbarSlot(DefaultedList<ItemStack> slots, int slot, Object value) {
        if (slot != PlayerLoadout.EXTRA_HOTBAR) return slots.set(slot, (ItemStack) value);
        ItemStack previous = getStack(slot);
        setStack(slot, (ItemStack) value);
        return previous;
    }

    @Redirect(method = "updateItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"))
    private void tickLoadoutItems(ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.Entity entity,
                                  int slot, boolean selected, @Local DefaultedList<ItemStack> group) {
        if (group == cranks$loadout.stacks()) {
            int index = PlayerLoadout.EXTRA_HOTBAR + slot;
            stack.inventoryTick(world, entity, index, index == selectedSlot);
        } else {
            stack.inventoryTick(world, entity, slot, selected);
        }
    }
}

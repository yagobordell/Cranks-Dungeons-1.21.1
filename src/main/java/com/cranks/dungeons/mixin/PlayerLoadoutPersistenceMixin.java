package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
abstract class PlayerLoadoutPersistenceMixin {
    @Inject(method = "dropInventory", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V", shift = At.Shift.AFTER), cancellable = true)
    private void dropOnlyBagContents(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            PlayerLoadout.of(player.getInventory()).dropBagContents();
            ci.cancel();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeLoadout(NbtCompound tag, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        tag.put("CranksLoadout", PlayerLoadout.of(player.getInventory()).write(player.getRegistryManager()));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readLoadout(NbtCompound tag, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerLoadout.of(player.getInventory()).read(tag.getCompound("CranksLoadout"), player.getRegistryManager());
        if (!PlayerInventory.isValidHotbarIndex(player.getInventory().selectedSlot)) {
            player.getInventory().selectedSlot = 0;
        }
    }

    @Redirect(method = "equipStack", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private Object replaceSelectedExtraStack(DefaultedList<ItemStack> main, int slot, Object value) {
        if (slot != PlayerLoadout.EXTRA_HOTBAR) return main.set(slot, (ItemStack) value);
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack previous = player.getInventory().getStack(slot);
        player.getInventory().setStack(slot, (ItemStack) value);
        return previous;
    }
}

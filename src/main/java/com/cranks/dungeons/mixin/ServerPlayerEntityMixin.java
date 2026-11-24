package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.mount.MountManager;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onPlayerDisconnect(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        EquipmentStatApplier.onPlayerDisconnect(player);
        com.cranks.dungeons.stat.passive.PassiveSkillApplier.onPlayerDisconnect(player);
    }

    @Inject(method = "openHorseInventory", at = @At("HEAD"), cancellable = true)
    private void cranks$routeMountInventory(AbstractHorseEntity mount, Inventory inventory,
                                             CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (MountManager.usesCustomCargoScreen(mount)) {
            MountManager.openCargo(player, mount);
            ci.cancel();
        } else if (!MountManager.canAccessStorage(player, mount)) {
            MountManager.denyMountAccess(player);
            ci.cancel();
        }
    }
}

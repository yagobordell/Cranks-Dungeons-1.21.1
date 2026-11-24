package com.cranks.dungeons.mixin;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.horse.HorseBreedingManager;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayerEntity.class)
public abstract class MountInventoryLoadoutMixin {
    @Inject(method="openHorseInventory",at=@At("HEAD"),cancellable=true)
    private void openSharedMountInventory(AbstractHorseEntity mount,Inventory inventory,CallbackInfo ci){
        if(MountKind.of(mount)==MountKind.UNSUPPORTED)return;
        ci.cancel();var player=(ServerPlayerEntity)(Object)this;
        if(mount instanceof HorseEntity horse)HorseBreedingManager.syncToPlayer(player,horse);
        MountManager.openCargo(player,mount);
    }
}

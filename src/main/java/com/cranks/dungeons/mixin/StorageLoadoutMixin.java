package com.cranks.dungeons.mixin;
import com.cranks.dungeons.screen.LoadoutStorageHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ServerPlayerEntity.class)
public abstract class StorageLoadoutMixin {
    @ModifyExpressionValue(method="openHandledScreen",at=@At(value="INVOKE",target="Lnet/minecraft/screen/NamedScreenHandlerFactory;createMenu(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/screen/ScreenHandler;"))
    private ScreenHandler adaptStorage(ScreenHandler original){
        var inventory=((ServerPlayerEntity)(Object)this).getInventory();
        if(original instanceof net.minecraft.screen.CrafterScreenHandler crafter && crafter.getInputInventory() instanceof net.minecraft.block.entity.CrafterBlockEntity block)
            return new com.cranks.dungeons.screen.LoadoutCrafterHandler(original.syncId,inventory,block);
        return LoadoutStorageHandler.wrap(original,inventory);
    }
}

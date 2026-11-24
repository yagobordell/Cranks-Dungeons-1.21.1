package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.PlayerLoadoutScreen;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
abstract class ClientInventoryLoadoutMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen openLoadoutScreen(Screen screen) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (screen != null && (screen.getClass() == InventoryScreen.class
                ||screen.getClass()==net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.class) && client.player != null
                && client.interactionManager != null) {
            return new PlayerLoadoutScreen(client.player);
        }
        return screen;
    }

    @ModifyArg(method = "doItemPick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickCreativeStack(Lnet/minecraft/item/ItemStack;I)V"), index = 1)
    private int routeTenthCreativeSlot(int slot) {
        return slot == 36 + PlayerLoadout.EXTRA_HOTBAR ? PlayerLoadout.FIRST_HANDLER_SLOT : slot;
    }
}

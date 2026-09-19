package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.StatsPanelScrollHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void hideStatusEffectsWhenStatsPanelOpen(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if ((Object) this instanceof InventoryScreen && StatsPanelScrollHandler.isStatsVisible()) {
            ci.cancel();
        }
    }

    /**
     * Keep the inventory status-effect panel compact. Vanilla switches to a
     * wide layout whenever there is room, which permanently renders the name
     * and duration beside each icon. The compact layout keeps only the icons
     * and uses the normal hover tooltip instead.
     */
    @ModifyVariable(
        method = "drawStatusEffects",
        at = @At("STORE"),
        ordinal = 0,
        require = 1
    )
    private boolean forceCompactStatusEffectLayout(boolean wide) {
        return false;
    }
}

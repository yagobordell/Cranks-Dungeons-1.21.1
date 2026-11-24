package com.cranks.dungeons.mixin;

import com.cranks.dungeons.client.InventoryStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractInventoryScreen.class)
public abstract class StatusEffectDisplayMixin extends HandledScreen<ScreenHandler> {
    @Unique
    private final InventoryStatusEffects cranks$effects = new InventoryStatusEffects();

    protected StatusEffectDisplayMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return cranks$effects.click(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return cranks$effects.scroll(mouseX, mouseY, vertical) || super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return !cranks$effects.contains(mouseX, mouseY) && super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void renderEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        cranks$effects.render(context, client);
        cranks$effects.tooltip(context, client, mouseX, mouseY);
        ci.cancel();
    }
}

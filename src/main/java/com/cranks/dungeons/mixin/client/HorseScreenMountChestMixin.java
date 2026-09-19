package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseScreen.class)
public abstract class HorseScreenMountChestMixin {
    private static final Identifier CRANKS_CHEST_SLOT =
            Identifier.ofVanilla("container/slot");

    @Shadow @Final private AbstractHorseEntity entity;

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void cranks$drawChestEquipmentSlot(DrawContext context, float delta,
                                                int mouseX, int mouseY, CallbackInfo ci) {
        if (!(entity instanceof MountDataProvider)
                || MountKind.of(entity) == MountKind.UNSUPPORTED) return;
        com.cranks.dungeons.mixin.HandledScreenAccessor accessor =
                (com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this;
        context.drawGuiTexture(CRANKS_CHEST_SLOT,
                accessor.getX() + 7, accessor.getY() + 53, 18, 18);
    }
}

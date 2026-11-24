package com.cranks.dungeons.compat.punchy;

import com.cranks.dungeons.ranged.RangedTiming;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import punchy.client.animation.PoseHandler;
import punchy.client.animation.data.AnimationClip;

/** Punchy 3.2's fixed-speed clip path bypasses clipSpeedMultiplier. */
@Mixin(value=PoseHandler.class,remap=false)
public class PunchyRangedPoseMixin {
    @Shadow private float timelineSpeedScale;
    @Inject(method="resolveTimelineSpeed",at=@At("RETURN"),cancellable=true)
    private void cranks$matchGameplayCharge(AnimationClip clip,float time,
                                           CallbackInfoReturnable<Float> cir) {
        var player=MinecraftClient.getInstance().player;
        if(clip==null || player==null || !player.isUsingItem())return;
        String name=clip.getName().toLowerCase(java.util.Locale.ROOT);
        if(!name.contains("use_") || name.contains("shot"))return;
        var weapon=player.getActiveItem();
        int ticks;
        if(weapon.getItem() instanceof CrossbowItem && name.contains("crossbow"))ticks=RangedTiming.crossbowReloadTicks(player,weapon);
        else if(weapon.getItem() instanceof BowItem && name.contains("bow"))ticks=RangedTiming.bowDrawTicks(player,weapon);
        else return;
        cir.setReturnValue(clip.getLength()/Math.max(.05f,ticks/20f)/Math.max(.0001f,timelineSpeedScale));
    }
}

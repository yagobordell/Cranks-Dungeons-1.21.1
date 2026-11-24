package com.cranks.dungeons.mixin;
import com.cranks.dungeons.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityXpMixin {
    @Inject(method={"addExperience","addExperienceLevels"},at=@At("HEAD"),cancellable=true)
    private void rejectUntypedXp(int amount,CallbackInfo ci) { ci.cancel(); }
    @Inject(method="getNextLevelExperience",at=@At("HEAD"),cancellable=true)
    private void threshold(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(1,ModComponents.PROGRESSION.get((PlayerEntity)(Object)this).required()));
    }
}

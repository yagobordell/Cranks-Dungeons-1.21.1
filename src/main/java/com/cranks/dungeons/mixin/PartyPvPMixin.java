package com.cranks.dungeons.mixin;

import com.cranks.dungeons.party.PartyManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PartyPvPMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventPartyPvP(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity victim = (PlayerEntity) (Object) this;
        
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) {
            return;
        }
        
        if (victim != attacker && PartyManager.areInSameParty(victim.getUuid(), attacker.getUuid())) {
            cir.setReturnValue(false);
        }
    }
}

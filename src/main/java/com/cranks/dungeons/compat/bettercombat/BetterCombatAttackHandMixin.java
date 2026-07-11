package com.cranks.dungeons.compat.bettercombat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.bettercombat.api.AttackHand", remap = false)
public class BetterCombatAttackHandMixin {

    @Inject(method = "upswingRate", at = @At("RETURN"), cancellable = true)
    private void cranks$forceFastUpswingForPenalizedPlayers(CallbackInfoReturnable<Double> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        var attrEntry = Registries.ATTRIBUTE.getEntry(Identifier.of("minecraft", "attack_speed")).orElse(null);
        if (attrEntry == null) return;
        EntityAttributeInstance attr = client.player.getAttributeInstance(attrEntry);
        if (attr != null) {
            for (EntityAttributeModifier mod : attr.getModifiers()) {
                if (mod.id().getPath().contains("req_penalty_minecraft_attack_speed")) {
                    // Force the physical upswing to be extremely short so the damage hits instantly,
                    // without affecting the long cooldown delay enforced between swings.
                    cir.setReturnValue(0.01);
                    return;
                }
            }
        }
    }
}

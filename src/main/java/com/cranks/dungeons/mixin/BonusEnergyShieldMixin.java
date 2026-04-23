package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class BonusEnergyShieldMixin {

    @Unique
    private double cranks$lastESPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusEnergyShield(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        double esPercent = player.getAttributeValue(ModAttributes.ENERGY_SHIELD_PERCENT);

        var esAttr = player.getAttributeInstance(ModAttributes.ENERGY_SHIELD);
        if (esAttr == null) return;

        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "energy_shield_passive_percent");

        // Apply Percentage Bonus
        double currentTotal = esAttr.getValue();
        double currentPercentModifier = 0;
        var mod = esAttr.getModifier(percentModifierId);
        if (mod != null) currentPercentModifier = mod.value();

        double totalWithoutThis = currentTotal - currentPercentModifier;
        double newPercentBonus = totalWithoutThis * esPercent;

        if (Math.abs(newPercentBonus - cranks$lastESPercentBonus) > 0.01) {
            esAttr.removeModifier(percentModifierId);
            if (newPercentBonus > 0) {
                esAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        newPercentBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastESPercentBonus = newPercentBonus;
        }
    }
}

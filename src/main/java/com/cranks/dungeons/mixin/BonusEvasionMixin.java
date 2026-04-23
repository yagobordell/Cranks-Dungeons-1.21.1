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
public abstract class BonusEvasionMixin {

    @Unique
    private double cranks$lastEvasionPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusEvasion(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        double evasionPercent = player.getAttributeValue(ModAttributes.EVASION_RATING_PERCENT);

        var evasionAttr = player.getAttributeInstance(ModAttributes.EVASION_RATING);
        if (evasionAttr == null) return;

        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "evasion_rating_passive_percent");

        // Apply Percentage Bonus
        double currentTotal = evasionAttr.getValue();
        double currentPercentModifier = 0;
        var mod = evasionAttr.getModifier(percentModifierId);
        if (mod != null) currentPercentModifier = mod.value();

        double totalWithoutThis = currentTotal - currentPercentModifier;
        double newPercentBonus = totalWithoutThis * evasionPercent;

        if (Math.abs(newPercentBonus - cranks$lastEvasionPercentBonus) > 0.01) {
            evasionAttr.removeModifier(percentModifierId);
            if (newPercentBonus > 0) {
                evasionAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        newPercentBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastEvasionPercentBonus = newPercentBonus;
        }
    }
}

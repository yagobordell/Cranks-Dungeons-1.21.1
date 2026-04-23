package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class BonusArmorToughnessMixin {

    @Unique
    private double cranks$lastBonusToughness = Double.NaN;
    @Unique
    private double cranks$lastToughnessPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusArmorToughness(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        double toughnessFlatBonus = player.getAttributeValue(ModAttributes.BONUS_ARMOR_TOUGHNESS);
        double toughnessPercent = player.getAttributeValue(ModAttributes.ARMOR_TOUGHNESS_PERCENT);

        var toughnessAttr = player.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS);
        if (toughnessAttr == null) return;

        Identifier flatModifierId = Identifier.of(CranksDungeons.MOD_ID, "bonus_armor_toughness");
        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "armor_toughness_passive_percent");

        // Apply Flat Bonus
        if (toughnessFlatBonus != cranks$lastBonusToughness) {
            toughnessAttr.removeModifier(flatModifierId);
            if (toughnessFlatBonus > 0) {
                toughnessAttr.addTemporaryModifier(new EntityAttributeModifier(
                        flatModifierId,
                        toughnessFlatBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastBonusToughness = toughnessFlatBonus;
        }

        // Apply Percentage Bonus
        double currentTotal = toughnessAttr.getValue();
        double currentPercentModifier = 0;
        var mod = toughnessAttr.getModifier(percentModifierId);
        if (mod != null) currentPercentModifier = mod.value();

        double totalWithoutThis = currentTotal - currentPercentModifier;
        double newPercentBonus = totalWithoutThis * toughnessPercent;

        if (Math.abs(newPercentBonus - cranks$lastToughnessPercentBonus) > 0.01) {
            toughnessAttr.removeModifier(percentModifierId);
            if (newPercentBonus > 0) {
                toughnessAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        newPercentBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastToughnessPercentBonus = newPercentBonus;
        }
    }
}
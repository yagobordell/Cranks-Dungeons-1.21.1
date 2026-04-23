package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class BonusArmorMixin {

    @Unique
    private double cranks$lastBonusArmor = Double.NaN;
    @Unique
    private double cranks$lastLeggingsBonus = Double.NaN;
    @Unique
    private double cranks$lastGlobalArmorPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusArmor(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR);
        double armorPercent = player.getAttributeValue(ModAttributes.ARMOR_PERCENT);
        
        // Calculate Leggings Armor Multiplier
        double leggingsMultiplier = player.getAttributeValue(ModAttributes.LEGGINGS_ARMOR_PERCENT);
        double leggingsBonus = 0;
        if (leggingsMultiplier > 0) {
            ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
            if (!leggings.isEmpty()) {
                double leggingsBaseArmor = 0;
                var comp = leggings.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (comp != null) {
                    for (var entry : comp.modifiers()) {
                        if (entry.attribute().equals(EntityAttributes.ARMOR)) {
                            leggingsBaseArmor += entry.modifier().value();
                        }
                    }
                }
                leggingsBonus = leggingsBaseArmor * leggingsMultiplier;
            }
        }

        var armorAttr = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (armorAttr == null) return;

        Identifier flatModifierId = Identifier.of(CranksDungeons.MOD_ID, "bonus_armor");
        Identifier leggingsModifierId = Identifier.of(CranksDungeons.MOD_ID, "leggings_armor_bonus");
        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "armor_passive_percent");

        // Apply Flat Bonus Armor
        if (bonusArmor != cranks$lastBonusArmor) {
            armorAttr.removeModifier(flatModifierId);
            if (bonusArmor > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        flatModifierId,
                        bonusArmor,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastBonusArmor = bonusArmor;
        }

        // Apply Leggings Percentage Bonus
        if (leggingsBonus != cranks$lastLeggingsBonus) {
            armorAttr.removeModifier(leggingsModifierId);
            if (leggingsBonus > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        leggingsModifierId,
                        leggingsBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastLeggingsBonus = leggingsBonus;
        }

        // Apply Global Percentage Bonus
        double currentTotal = armorAttr.getValue();
        double currentPercentModifier = 0;
        var mod = armorAttr.getModifier(percentModifierId);
        if (mod != null) currentPercentModifier = mod.value();

        double totalWithoutThis = currentTotal - currentPercentModifier;
        double newPercentBonus = totalWithoutThis * armorPercent;

        if (Math.abs(newPercentBonus - cranks$lastGlobalArmorPercentBonus) > 0.01) {
            armorAttr.removeModifier(percentModifierId);
            if (newPercentBonus > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        newPercentBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastGlobalArmorPercentBonus = newPercentBonus;
        }
    }
}
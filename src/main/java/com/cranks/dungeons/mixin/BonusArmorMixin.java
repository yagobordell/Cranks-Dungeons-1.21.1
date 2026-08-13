package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.CombatUtility;
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

        var armorAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttr == null) return;

        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR);
        Identifier flatModifierId = Identifier.of(CranksDungeons.MOD_ID, "bonus_armor");

        // Apply Flat Bonus Armor (Legacy/Misc)
        if (Double.isNaN(cranks$lastBonusArmor) || bonusArmor != cranks$lastBonusArmor) {
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

        // Apply Global Armor Percent
        double globalArmorPercent = player.getAttributeValue(ModAttributes.ARMOR_PERCENT);
        Identifier percentModifierId = Identifier.of(CranksDungeons.MOD_ID, "global_armor_percent");
        if (Double.isNaN(cranks$lastGlobalArmorPercentBonus) || globalArmorPercent != cranks$lastGlobalArmorPercentBonus) {
            armorAttr.removeModifier(percentModifierId);
            if (globalArmorPercent > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        percentModifierId,
                        globalArmorPercent,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
            cranks$lastGlobalArmorPercentBonus = globalArmorPercent;
        }

        // Apply Leggings Armor Percent
        double leggingsPercent = player.getAttributeValue(ModAttributes.LEGGINGS_ARMOR_PERCENT);
        Identifier leggingsModifierId = Identifier.of(CranksDungeons.MOD_ID, "leggings_armor_percent");
        if (Double.isNaN(cranks$lastLeggingsBonus) || leggingsPercent != cranks$lastLeggingsBonus) {
            armorAttr.removeModifier(leggingsModifierId);
            if (leggingsPercent > 0) {
                // Leggings percent only applies to the armor provided by leggings
                double leggingsBaseArmor = 0;
                ItemStack leggings = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS);
                if (!leggings.isEmpty()) {
                    var comp = leggings.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if (comp != null) {
                        for (var modEntry : comp.modifiers()) {
                            if (CombatUtility.attributesMatch(modEntry.attribute(), EntityAttributes.GENERIC_ARMOR)) {
                                leggingsBaseArmor += modEntry.modifier().value();
                            }
                        }
                    }
                }
                
                double bonusFromLeggings = leggingsBaseArmor * leggingsPercent;
                
                if (bonusFromLeggings > 0) {
                    armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                            leggingsModifierId,
                            bonusFromLeggings,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }
            }
            cranks$lastLeggingsBonus = leggingsPercent;
        }
    }
}
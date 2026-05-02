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

        // Calculate Local Armor Bonuses from all slots
        double totalLocalBonus = 0;
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            if (!slot.isArmorSlot() && slot != net.minecraft.entity.EquipmentSlot.OFFHAND) continue;
            
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            double baseArmorOnItem = 0;
            double itemPercent = 0;
            
            var comp = stack.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (comp != null) {
                for (var entry : comp.modifiers()) {
                    if (entry.attribute().equals(EntityAttributes.ARMOR)) {
                        baseArmorOnItem += entry.modifier().value();
                    } else if (entry.attribute().equals(ModAttributes.ARMOR_PERCENT)) {
                        itemPercent += entry.modifier().value();
                    }
                }
            }
            
            if (itemPercent > 0 && baseArmorOnItem > 0) {
                totalLocalBonus += baseArmorOnItem * itemPercent;
            }
        }

        var armorAttr = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (armorAttr == null) return;

        Identifier localModifierId = Identifier.of(CranksDungeons.MOD_ID, "local_armor_bonus");
        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR);
        Identifier flatModifierId = Identifier.of(CranksDungeons.MOD_ID, "bonus_armor");

        // Apply Local Bonus
        if (Double.isNaN(cranks$lastGlobalArmorPercentBonus) || Math.abs(totalLocalBonus - cranks$lastGlobalArmorPercentBonus) > 0.01) {
            armorAttr.removeModifier(localModifierId);
            if (totalLocalBonus > 0) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                        localModifierId,
                        totalLocalBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastGlobalArmorPercentBonus = totalLocalBonus;
        }

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
    }
}
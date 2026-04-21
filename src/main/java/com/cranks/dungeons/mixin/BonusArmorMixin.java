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

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusArmor(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR);
        
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

        // Apply Flat Bonus Armor
        if (bonusArmor != cranks$lastBonusArmor) {
            var armorAttr = player.getAttributeInstance(EntityAttributes.ARMOR);
            if (armorAttr != null) {
                Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "bonus_armor");
                armorAttr.removeModifier(modifierId);
                if (bonusArmor > 0) {
                    armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                            modifierId,
                            bonusArmor,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }
            }
            cranks$lastBonusArmor = bonusArmor;
        }

        // Apply Leggings Percentage Bonus
        if (leggingsBonus != cranks$lastLeggingsBonus) {
            var armorAttr = player.getAttributeInstance(EntityAttributes.ARMOR);
            if (armorAttr != null) {
                Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "leggings_armor_bonus");
                armorAttr.removeModifier(modifierId);
                if (leggingsBonus > 0) {
                    armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                            modifierId,
                            leggingsBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ));
                }
            }
            cranks$lastLeggingsBonus = leggingsBonus;
        }
    }
}
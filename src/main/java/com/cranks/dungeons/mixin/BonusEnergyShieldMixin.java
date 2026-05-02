package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

        // Calculate Local ES Bonuses from all slots
        double totalLocalBonus = 0;
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            if (!slot.isArmorSlot() && slot != net.minecraft.entity.EquipmentSlot.OFFHAND) continue;
            
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            double baseESOnItem = 0;
            double itemPercent = 0;
            
            var comp = stack.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (comp != null) {
                for (var entry : comp.modifiers()) {
                    if (entry.attribute().equals(ModAttributes.ENERGY_SHIELD)) {
                        baseESOnItem += entry.modifier().value();
                    } else if (entry.attribute().equals(ModAttributes.ENERGY_SHIELD_PERCENT)) {
                        itemPercent += entry.modifier().value();
                    }
                }
            }
            
            if (itemPercent > 0 && baseESOnItem > 0) {
                totalLocalBonus += baseESOnItem * itemPercent;
            }
        }

        var esAttr = player.getAttributeInstance(ModAttributes.ENERGY_SHIELD);
        if (esAttr == null) return;

        Identifier localModifierId = Identifier.of(CranksDungeons.MOD_ID, "local_energy_shield_bonus");

        // Apply Local Bonus
        if (Double.isNaN(cranks$lastESPercentBonus) || Math.abs(totalLocalBonus - cranks$lastESPercentBonus) > 0.01) {
            esAttr.removeModifier(localModifierId);
            if (totalLocalBonus > 0) {
                esAttr.addTemporaryModifier(new EntityAttributeModifier(
                        localModifierId,
                        totalLocalBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastESPercentBonus = totalLocalBonus;
        }
    }
}

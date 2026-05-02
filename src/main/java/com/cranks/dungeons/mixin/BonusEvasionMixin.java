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
public abstract class BonusEvasionMixin {

    @Unique
    private double cranks$lastEvasionPercentBonus = Double.NaN;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBonusEvasion(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        // Calculate Local Evasion Bonuses from all slots
        double totalLocalBonus = 0;
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            if (!slot.isArmorSlot()) continue;
            
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            double baseEvasionOnItem = 0;
            double itemPercent = 0;
            
            var comp = stack.get(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (comp != null) {
                for (var entry : comp.modifiers()) {
                    if (entry.attribute().equals(ModAttributes.EVASION_RATING)) {
                        baseEvasionOnItem += entry.modifier().value();
                    } else if (entry.attribute().equals(ModAttributes.EVASION_RATING_PERCENT)) {
                        itemPercent += entry.modifier().value();
                    }
                }
            }
            
            if (itemPercent > 0 && baseEvasionOnItem > 0) {
                totalLocalBonus += baseEvasionOnItem * itemPercent;
            }
        }

        var evasionAttr = player.getAttributeInstance(ModAttributes.EVASION_RATING);
        if (evasionAttr == null) return;

        Identifier localModifierId = Identifier.of(CranksDungeons.MOD_ID, "local_evasion_bonus");

        // Apply Local Bonus
        if (Double.isNaN(cranks$lastEvasionPercentBonus) || Math.abs(totalLocalBonus - cranks$lastEvasionPercentBonus) > 0.01) {
            evasionAttr.removeModifier(localModifierId);
            if (totalLocalBonus > 0) {
                evasionAttr.addTemporaryModifier(new EntityAttributeModifier(
                        localModifierId,
                        totalLocalBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
            cranks$lastEvasionPercentBonus = totalLocalBonus;
        }
    }
}

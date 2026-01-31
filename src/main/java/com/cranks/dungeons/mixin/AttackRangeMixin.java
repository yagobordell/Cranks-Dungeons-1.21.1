package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class AttackRangeMixin {

    @Unique
    private static final Identifier RANGE_SYNC_ID = Identifier.of("cranks-dungeons", "range_sync");

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void modifyAttackRange(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        net.minecraft.util.Identifier syncId = net.minecraft.util.Identifier.of("cranks-dungeons", "range_sync");
        var vanillaRange = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ENTITY_INTERACTION_RANGE);

        double weaponBonus = EquipmentStatApplier.getItemStatValue(player.getMainHandStack(), "attack_range");
        double armorBonus = EquipmentStatApplier.getItemStatValue(player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST), "attack_range");
        double attrBonus = player.getAttributeValue(ModAttributes.ATTACK_RANGE);

        double totalBonus = weaponBonus + armorBonus + attrBonus;

        if (vanillaRange != null) {
            vanillaRange.removeModifier(syncId);
        }

        if (totalBonus > 0) {
            double baseRange = cir.getReturnValue();
            cir.setReturnValue(baseRange + totalBonus);

            if (vanillaRange != null) {
                vanillaRange.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        syncId, totalBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }
}
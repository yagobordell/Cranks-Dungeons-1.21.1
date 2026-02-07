package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
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

        var vanillaRange = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ENTITY_INTERACTION_RANGE);

        ItemStack weapon = player.getMainHandStack();

        double weaponBonus = EquipmentStatApplier.getItemStatValue(weapon, "attack_range");
        double armorBonus = EquipmentStatApplier.getItemStatValue(player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST), "attack_range");
        double attrBonus = player.getAttributeValue(ModAttributes.ATTACK_RANGE);

        // Add enchantment bonus from weapon
        double enchantBonus = getEnchantmentValue(weapon, "minecraft:attack_range", 1.0);

        double totalBonus = weaponBonus + armorBonus + attrBonus + enchantBonus;

        if (vanillaRange != null) {
            vanillaRange.removeModifier(RANGE_SYNC_ID);
        }

        if (totalBonus > 0) {
            double baseRange = cir.getReturnValue();
            cir.setReturnValue(baseRange + totalBonus);

            if (vanillaRange != null) {
                vanillaRange.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        RANGE_SYNC_ID, totalBonus, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    @Unique
    private double getEnchantmentValue(ItemStack stack, String enchantmentId, double baseValue) {
        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantmentEntries()) {
            String id = entry.getKey().getKey().get().getValue().toString();
            if (id.equals(enchantmentId)) {
                return baseValue;
            }
        }
        return 0.0;
    }
}
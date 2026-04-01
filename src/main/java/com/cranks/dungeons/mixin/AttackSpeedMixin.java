package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentStatApplier;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class AttackSpeedMixin {

    @Inject(method = "getAttackCooldownProgress", at = @At("RETURN"), cancellable = true)
    private void modifyAttackSpeed(float baseTime, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        ItemStack weapon = player.getMainHandStack();
        ItemStack chestplate = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);

        double weaponBonus = EquipmentStatApplier.getItemStatValue(weapon, "attack_speed");
        double armorBonus = EquipmentStatApplier.getItemStatValue(chestplate, "attack_speed");
        double attrBonus = player.getAttributeValue(ModAttributes.ATTACK_SPEED);

        double enchantWeaponBonus = getEnchantmentValue(weapon, "minecraft:attack_speed", 0.2);
        double enchantChestBonus = getEnchantmentValue(chestplate, "minecraft:attack_speed", 0.2);

        double totalBonus = weaponBonus + armorBonus + attrBonus + enchantWeaponBonus + enchantChestBonus;

        if (totalBonus > 0) {
            float currentProgress = cir.getReturnValue();
            cir.setReturnValue(Math.min(currentProgress * (float) (1.0 + totalBonus), 1.0f));
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
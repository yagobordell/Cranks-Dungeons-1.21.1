package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.EquipmentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyVariable(
            method = "applyAttributeModifiers",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2 // 0 = stack, 1 = slot/group, 2 = consumer
    )
    private static BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> filterAttributes(
            BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> original,
            ItemStack stack) {

        return (attr, modifier) -> {
            boolean isWeaponAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeaponAttribute(attr);
            boolean isArmorAttr = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmorAttribute(attr);
            
            boolean isWeapon = com.cranks.dungeons.util.EnchantmentFilterUtil.isWeapon(stack);
            boolean isArmor = com.cranks.dungeons.util.EnchantmentFilterUtil.isArmor(stack);

            if (isWeaponAttr && !isWeapon) {
                return;
            }
            if (isArmorAttr && !isArmor) {
                return;
            }

            original.accept(attr, modifier);
        };
    }
}

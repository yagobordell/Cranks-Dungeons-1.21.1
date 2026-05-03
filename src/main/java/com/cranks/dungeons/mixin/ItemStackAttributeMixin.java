package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.EnchantmentFilterUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackAttributeMixin {

    @ModifyVariable(
            method = "applyAttributeModifiers",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2
    )
    private BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> filterAttributes(
            BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> original) {
        
        ItemStack stack = (ItemStack) (Object) this;
        
        return (attr, modifier) -> {
            boolean isWeaponAttr = EnchantmentFilterUtil.isWeaponAttribute(attr);
            boolean isArmorAttr = EnchantmentFilterUtil.isArmorAttribute(attr);
            
            boolean isWeapon = EnchantmentFilterUtil.isWeapon(stack);
            boolean isArmor = EnchantmentFilterUtil.isArmor(stack);

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

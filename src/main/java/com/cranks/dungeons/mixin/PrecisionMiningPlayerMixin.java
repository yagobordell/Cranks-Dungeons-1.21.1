package com.cranks.dungeons.mixin;

import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class PrecisionMiningPlayerMixin {

    @Inject(
            method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyPrecisionMining(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        // Only run on server-side players
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack self = (ItemStack) (Object) this;

        // Only for relevant tools
        if (!(self.isIn(ItemTags.PICKAXES) ||
                self.isIn(ItemTags.AXES) ||
                self.isIn(ItemTags.HOES) ||
                self.isIn(ItemTags.SHOVELS))) {
            return;
        }

        // 1) Prefer attribute (this should already include tool + armor, etc.)
        double precisionChance = 0.0;
        var inst = player.getAttributeInstance(ModAttributes.PRECISION_MINING);
        if (inst != null) {
            precisionChance = inst.getValue();
        }

        // 2) If attribute isn't wired, fall back to the raw stat stored on the tool
        if (precisionChance <= 0.0) {
            precisionChance = ToolStatHelper.getPrecisionMining(self);
        }

        // Clamp just in case some value is misconfigured
        if (precisionChance < 0.0) precisionChance = 0.0;
        if (precisionChance > 1.0) precisionChance = 1.0;

        if (precisionChance > 0.0 && player.getRandom().nextDouble() < precisionChance) {
            // Cancel the whole damage call = no durability lost this time
            ci.cancel();
        }
    }
}
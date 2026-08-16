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

        // Check precision mining for tools
        boolean isGatheringTool = self.isIn(ItemTags.PICKAXES) || self.isIn(ItemTags.AXES) || self.isIn(ItemTags.HOES) || self.isIn(ItemTags.SHOVELS);
        if (isGatheringTool) {
            double precisionChance = 0.0;
            var inst = player.getAttributeInstance(ModAttributes.PRECISION_MINING);
            if (inst != null) {
                precisionChance = inst.getValue();
            }
            if (precisionChance <= 0.0) {
                precisionChance = ToolStatHelper.getPrecisionMining(self);
            }
            precisionChance = Math.clamp(precisionChance, 0.0, 1.0);
            if (precisionChance > 0.0 && player.getRandom().nextDouble() < precisionChance) {
                ci.cancel();
                return;
            }
        }

        // Check repair chance (Preservation enchantment / rune stat) for any gear
        double repairChance = 0.0;
        var repairInst = player.getAttributeInstance(ModAttributes.REPAIR_CHANCE);
        if (repairInst != null) {
            repairChance = repairInst.getValue();
        }
        if (repairChance <= 0.0) {
            repairChance = ToolStatHelper.getRepairChance(self);
        }
        repairChance = Math.clamp(repairChance, 0.0, 1.0);
        if (repairChance > 0.0 && player.getRandom().nextDouble() < repairChance) {
            ci.cancel();
        }
    }
}
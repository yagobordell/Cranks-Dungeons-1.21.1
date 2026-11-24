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

        if(amount<=0)return;
        boolean gathering=self.isIn(ItemTags.PICKAXES)||self.isIn(ItemTags.AXES)||self.isIn(ItemTags.HOES)||self.isIn(ItemTags.SHOVELS);
        double precision=gathering?player.getAttributeValue(ModAttributes.PRECISION_MINING):0;
        double repair=player.getAttributeValue(ModAttributes.REPAIR_CHANCE);
        if(precision<=0&&gathering)precision=ToolStatHelper.getPrecisionMining(self);
        if(repair<=0)repair=ToolStatHelper.getRepairChance(self);
        if(player.getRandom().nextDouble()<com.cranks.dungeons.gathering.GatheringRules.preservation(precision,repair))ci.cancel();
    }
}
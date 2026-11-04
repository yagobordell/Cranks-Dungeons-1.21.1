package com.cranks.dungeons.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import com.cranks.dungeons.registry.ModAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyPlayerAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();


        builder.add(com.cranks.dungeons.registry.ModAttributes.MINING_EFFICIENCY, 1.0);

        cir.setReturnValue(builder);
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyBlockBreakingSpeed(net.minecraft.block.BlockState block, CallbackInfoReturnable<Float> cir) {
        float originalSpeed = cir.getReturnValue();
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        if (block.isIn(net.minecraft.registry.tag.BlockTags.LOGS)) {
            var attr = player.getAttributeInstance(com.cranks.dungeons.registry.ModAttributes.WOODCUTTING_SPEED_PERCENT);
            if (attr != null) originalSpeed *= (1.0f + attr.getValue() / 100.0f); // Assuming it's a percentage stat 
        } else if (block.isIn(net.minecraft.registry.tag.BlockTags.PICKAXE_MINEABLE)) {
            var attr = player.getAttributeInstance(com.cranks.dungeons.registry.ModAttributes.MINING_SPEED_PERCENT);
            if (attr != null) originalSpeed *= (1.0f + attr.getValue() / 100.0f);
        } else if (block.isIn(net.minecraft.registry.tag.BlockTags.CROPS) || block.isOf(net.minecraft.block.Blocks.DIRT)) {
            var attr = player.getAttributeInstance(com.cranks.dungeons.registry.ModAttributes.FARMING_SPEED_PERCENT);
            if (attr != null) originalSpeed *= (1.0f + attr.getValue() / 100.0f);
        }

        cir.setReturnValue(originalSpeed);
    }

    @Inject(method = "updateTurtleHelmet", at = @At("HEAD"), cancellable = true)
    private void disableVanillaTurtleHelmet(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        // Disable vanilla water breathing from turtle helmet
        ci.cancel();
    }

}

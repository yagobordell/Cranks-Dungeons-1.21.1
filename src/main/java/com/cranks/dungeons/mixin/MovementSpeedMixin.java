package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MovementSpeedMixin {

    @Unique
    private static final Identifier MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "movement_speed_bonus");

    @Unique
    private double lastSpeedBonus = -1.0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyMovementSpeed(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        if (player.age % 20 != 0) return;

        double speedBonus = player.getAttributeValue(ModAttributes.MOVEMENT_SPEED);

        if (Math.abs(speedBonus - lastSpeedBonus) < 0.001) return;

        lastSpeedBonus = speedBonus;

        var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(MODIFIER_ID);

            if (speedBonus > 0.001) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        MODIFIER_ID,
                        speedBonus,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                speedAttr.addTemporaryModifier(modifier);
            }
        }
    }
}
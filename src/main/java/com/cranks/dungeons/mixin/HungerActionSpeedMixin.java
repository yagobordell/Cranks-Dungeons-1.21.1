package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.entity.attribute.EntityAttributeInstance;
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
public class HungerActionSpeedMixin {

    @Unique
    private static final Identifier HUNGER_SPEED_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "hunger_action_speed");

    @Unique
    private double cranks$lastHungerMultiplier = 0.0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyHungerModifiers(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(player instanceof ServerPlayerEntity)) return;

        if (player.age % 20 != 0) return;

        int foodLevel = player.getHungerManager().getFoodLevel();
        double multiplier = 0.0;
        
        if (foodLevel >= 16) {
            multiplier = 0.1; // +10%
        } else if (foodLevel <= 6) {
            multiplier = -0.1; // -10%
        }

        if (Math.abs(multiplier - cranks$lastHungerMultiplier) < 0.001) return;

        cranks$lastHungerMultiplier = multiplier;

        updateAttributeModifier(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, multiplier);
        updateAttributeModifier(player, EntityAttributes.GENERIC_ATTACK_SPEED, multiplier);
    }

    @Unique
    private void updateAttributeModifier(PlayerEntity player, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute, double multiplier) {
        EntityAttributeInstance attr = player.getAttributeInstance(attribute);
        if (attr != null) {
            attr.removeModifier(HUNGER_SPEED_MODIFIER_ID);
            if (Math.abs(multiplier) > 0.001) {
                attr.addTemporaryModifier(new EntityAttributeModifier(
                        HUNGER_SPEED_MODIFIER_ID,
                        multiplier,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }
}

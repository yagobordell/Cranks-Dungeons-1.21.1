package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class FreezeSlowdownMixin {

    @Unique
    private static final Identifier FREEZE_MOVEMENT_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "freeze_movement_slow");
    @Unique
    private static final Identifier FREEZE_ATTACK_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "freeze_attack_slow");
    @Unique
    private static final Identifier FREEZE_RELOAD_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "freeze_reload_slow");

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyFreezeSlowdown(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getEntityWorld().isClient()) return;

        boolean hasFreeze = entity.hasStatusEffect(ModEffects.FREEZE);
        
        // Apply or remove movement speed modifier
        applyModifier(entity, net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED, 
                FREEZE_MOVEMENT_MODIFIER_ID, -0.50, hasFreeze);
        
        // Apply or remove attack speed modifier  
        applyModifier(entity, net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED,
                FREEZE_ATTACK_MODIFIER_ID, -0.35, hasFreeze);
                
        // Apply or remove reload speed modifier
        EntityAttributeInstance reloadAttr = entity.getAttributeInstance(ModAttributes.RELOAD_SPEED);
        if (reloadAttr != null) {
            applyModifier(entity, ModAttributes.RELOAD_SPEED,
                    FREEZE_RELOAD_MODIFIER_ID, -0.35, hasFreeze);
        }
    }

    @Unique
    private void applyModifier(LivingEntity entity, 
                               net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                               Identifier modifierId, double modifierValue, boolean shouldApply) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance == null) return;
        
        // Remove existing modifier
        instance.removeModifier(modifierId);
        
        // Apply new modifier if freeze is active
        if (shouldApply) {
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,
                modifierValue,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            instance.addTemporaryModifier(modifier);
        }
    }
}

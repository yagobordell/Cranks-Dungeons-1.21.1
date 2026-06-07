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

    // Snapshot of the entity's pose at the moment it was frozen
    @Unique private boolean cranks$wasFrozenLastTick = false;
    @Unique private int cranks$frozenAge = 0;
    @Unique private float cranks$frozenBodyYaw = 0;
    @Unique private float cranks$frozenHeadYaw = 0;
    @Unique private float cranks$frozenYaw = 0;
    @Unique private float cranks$frozenPitch = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyFreezeSlowdown(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getEntityWorld().isClient()) return;

        boolean isStunned = entity.hasStatusEffect(ModEffects.FREEZE) || entity.hasStatusEffect(ModEffects.ELECTROCUTE);
        float buildup = com.cranks.dungeons.ailment.AilmentManager.getFreezeBuildup(entity.getUuid());
        
        double moveSlow = isStunned ? -1.0 : (buildup > 0 ? buildup * -0.25 : 0);
        double attackSlow = isStunned ? -1.0 : (buildup > 0 ? buildup * -0.20 : 0);
        
        boolean shouldApplyMove = moveSlow != 0;
        boolean shouldApplyAttack = attackSlow != 0;
        
        // Apply or remove movement speed modifier
        applyModifier(entity, net.minecraft.entity.attribute.EntityAttributes.MOVEMENT_SPEED, 
                FREEZE_MOVEMENT_MODIFIER_ID, moveSlow, shouldApplyMove);
        
        // Apply or remove attack speed modifier  
        applyModifier(entity, net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED,
                FREEZE_ATTACK_MODIFIER_ID, attackSlow, shouldApplyAttack);
                
        // Apply or remove reload speed modifier
        EntityAttributeInstance reloadAttr = entity.getAttributeInstance(ModAttributes.RELOAD_SPEED);
        if (reloadAttr != null) {
            applyModifier(entity, ModAttributes.RELOAD_SPEED,
                    FREEZE_RELOAD_MODIFIER_ID, attackSlow, shouldApplyAttack);
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

    // Cancel tickMovement entirely when frozen — this stops ALL head tracking,
    // body rotation, AI-driven movement, and entity-specific animation updates
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void cancelMovementWhenStunned(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isStunned = entity.hasStatusEffect(ModEffects.FREEZE) || entity.hasStatusEffect(ModEffects.ELECTROCUTE);
        if (isStunned) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void freezeAnimationState(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isStunned = entity.hasStatusEffect(ModEffects.FREEZE) || entity.hasStatusEffect(ModEffects.ELECTROCUTE);
        
        if (isStunned) {
            // On the first frozen tick, snapshot the current pose
            if (!cranks$wasFrozenLastTick) {
                cranks$wasFrozenLastTick = true;
                cranks$frozenAge = entity.age;
                cranks$frozenBodyYaw = entity.getBodyYaw();
                cranks$frozenHeadYaw = entity.getHeadYaw();
                cranks$frozenYaw = entity.getYaw();
                cranks$frozenPitch = entity.getPitch();
            }

            // Restore the exact frozen pose every tick — both current AND prev
            // so the renderer interpolation produces zero movement
            entity.age = cranks$frozenAge;
            
            // Lock rotation to the exact frozen values
            entity.setBodyYaw(cranks$frozenBodyYaw);
            entity.setHeadYaw(cranks$frozenHeadYaw);
            entity.setYaw(cranks$frozenYaw);
            entity.setPitch(cranks$frozenPitch);

            // Freeze all limb and hand animations
            entity.limbAnimator.setSpeed(0.0f);
            entity.handSwingProgress = 0;
            entity.handSwingTicks = 0;
            
            // Kill all movement input
            entity.sidewaysSpeed = 0;
            entity.forwardSpeed = 0;
            entity.upwardSpeed = 0;
            
            // Zero out velocity to prevent sliding
            entity.setVelocity(net.minecraft.util.math.Vec3d.ZERO);
        } else {
            cranks$wasFrozenLastTick = false;
        }
    }
}

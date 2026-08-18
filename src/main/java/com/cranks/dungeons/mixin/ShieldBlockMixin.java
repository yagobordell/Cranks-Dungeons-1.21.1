package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 700)
public abstract class ShieldBlockMixin {


    @Unique
    private boolean cranks$forceSkipVanillaBlock = false;

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyShieldBlock(float amount, DamageSource source) {
        if (amount <= 0 || source.isIn(DamageTypeTags.BYPASSES_SHIELD)) return amount;

        LivingEntity entity = (LivingEntity) (Object) this;
        net.minecraft.world.World world = entity.getEntityWorld();
        cranks$forceSkipVanillaBlock = false;

        // Resolute Technique prevents blocking
        if (source.getAttacker() instanceof LivingEntity attacker) {
            if (attacker.getAttributeValue(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE) > 0) {
                return amount;
            }
        }

        if (!entity.isBlocking()) return amount;

        boolean isVanillaBlocking = false;
        net.minecraft.util.math.Vec3d attackPos = source.getPosition();
        if (attackPos != null) {
            net.minecraft.util.math.Vec3d viewVec = entity.getRotationVec(1.0F);
            net.minecraft.util.math.Vec3d entityPos = new net.minecraft.util.math.Vec3d(entity.getX(), entity.getY(), entity.getZ());
            net.minecraft.util.math.Vec3d attackDir = attackPos.subtract(entityPos).normalize();
            attackDir = new net.minecraft.util.math.Vec3d(attackDir.x, 0.0, attackDir.z);
            if (attackDir.lengthSquared() > 0 && viewVec.dotProduct(attackDir) > 0.0) {
                isVanillaBlocking = true;
            }
        } else if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            // Projectiles with no explicit position usually still get blocked if they hit the front, but for safety let's say true
            isVanillaBlocking = true;
        } else {
            isVanillaBlocking = true;
        }

        if (isVanillaBlocking) {
            double blockValue = entity.getAttributeValue(ModAttributes.BLOCK_VALUE);
            double blockValuePercent = entity.getAttributeValue(ModAttributes.BLOCK_VALUE_PERCENT);
            blockValue *= (1.0 + blockValuePercent);

            if (blockValue <= 0) {
                return amount;
            }

            // Blocking always succeeds when shield is raised
            float remainingDamage = (float) Math.max(0, amount - blockValue);



            if (amount >= 3.0f) {
                net.minecraft.item.ItemStack activeStack = entity.getActiveItem();
                if (!activeStack.isEmpty() && activeStack.getItem() instanceof net.minecraft.item.ShieldItem) {
                    int shieldDamage = 1 + (int) Math.floor(amount);
                    activeStack.damage(shieldDamage, entity, net.minecraft.entity.EquipmentSlot.OFFHAND);
                    if (activeStack.isEmpty()) {
                        entity.clearActiveItem();
                        net.minecraft.sound.SoundEvent sound = net.minecraft.sound.SoundEvents.ITEM_SHIELD_BREAK;
                        entity.playSound(sound, 0.8F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                    }
                }
            }

            world.sendEntityStatus(entity, (byte) 29);
            cranks$forceSkipVanillaBlock = true;
            return remainingDamage;
        }

        return amount;
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void overrideIsBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (cranks$forceSkipVanillaBlock) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void overrideBlockedByShield(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cranks$forceSkipVanillaBlock) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Reset the force-skip flag after the damage() call completes.
     * This prevents isBlocking()/blockedByShield() from staying false
     * during idle periods between incoming hits.
     */
    @Inject(method = "damage", at = @At("TAIL"))
    private void resetForceSkipAfterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        cranks$forceSkipVanillaBlock = false;
    }
}

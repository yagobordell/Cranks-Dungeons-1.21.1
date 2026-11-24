package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.DamageHit;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 700)
public abstract class ShieldBlockMixin {
    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float applyShieldBlock(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        DamageHit hit = DamageHit.forTarget(entity);
        if (com.cranks.dungeons.ailment.AilmentManager.isResolvingAilment(entity)) {
            if (hit != null) hit.bypassBlock();
            return amount;
        }
        if (hit == null || amount <= 0 || source.isIn(DamageTypeTags.BYPASSES_SHIELD)
                || !entity.isBlocking()) return amount;

        if (source.getAttacker() instanceof LivingEntity attacker
                && attacker.getAttributeValue(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE) > 0) {
            hit.bypassBlock();
            return amount;
        }

        Vec3d attackPos = source.getPosition();
        if (attackPos != null) {
            Vec3d direction = attackPos.subtract(entity.getPos());
            Vec3d facing = entity.getRotationVec(1.0f);
            if (direction.x * facing.x + direction.z * facing.z <= 0) return amount;
        }

        double blockValue = entity.getAttributeValue(ModAttributes.BLOCK_VALUE)
                * (1 + entity.getAttributeValue(ModAttributes.BLOCK_VALUE_PERCENT));
        if (blockValue <= 0) return amount;
        hit.block(() -> cranks$damageActiveShield(entity, amount));
        return (float) (amount - com.cranks.dungeons.stat.CombatStatRules.blockedDamage(blockValue, amount));
    }

    @Unique
    private static void cranks$damageActiveShield(LivingEntity entity, float amount) {
        ItemStack shield = entity.getActiveItem();
        if (amount >= 3 && shield.getItem() instanceof ShieldItem) {
            EquipmentSlot slot = entity.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            shield.damage(1 + (int) Math.floor(amount), entity, slot);
            if (shield.isEmpty()) {
                entity.clearActiveItem();
                entity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8f, 1.0f);
            }
        }
        entity.getWorld().sendEntityStatus(entity, (byte) 29);
    }

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void cranks$skipVanillaBlock(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        DamageHit hit = DamageHit.forTarget((LivingEntity) (Object) this);
        if (hit != null && hit.shieldHandled()) cir.setReturnValue(false);
    }
}

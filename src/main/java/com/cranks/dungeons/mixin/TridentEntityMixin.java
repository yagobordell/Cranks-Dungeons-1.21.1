package com.cranks.dungeons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends net.minecraft.entity.projectile.PersistentProjectileEntity {

    @Shadow private boolean dealtDamage;
    @Shadow protected abstract boolean tryPickup(net.minecraft.entity.player.PlayerEntity player);
    @Unique private int customReturnTimer = 0;
    @Unique private Boolean customHasReturn = null;

    protected TridentEntityMixin(net.minecraft.entity.EntityType<? extends net.minecraft.entity.projectile.PersistentProjectileEntity> entityType, net.minecraft.world.World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleCustomReturn(CallbackInfo ci) {
        TridentEntity trident = (TridentEntity) (Object) this;
        if (trident.getWorld().isClient) return;

        if (this.customHasReturn == null) {
            ItemStack stack = trident.getWeaponStack();
            if (stack != null && !stack.isEmpty()) {
                this.customHasReturn = com.cranks.dungeons.equipment.ItemStatManager.getStats(stack).stream()
                        .anyMatch(s -> "trident_return".equals(s.statId));
            } else {
                this.customHasReturn = false;
            }
        }

        if (this.customHasReturn) {
            Entity owner = trident.getOwner();
            if (owner != null) {
                if (this.inGroundTime > 0) {
                    this.customReturnTimer++;
                } else if (trident.getY() < trident.getWorld().getBottomY() - 10) {
                    this.customReturnTimer += 20; 
                }

                if (this.dealtDamage || this.customReturnTimer > 20) {
                    if (!trident.isNoClip()) {
                        trident.setNoClip(true);
                        trident.getWorld().playSound(null, trident.getX(), trident.getY(), trident.getZ(), net.minecraft.sound.SoundEvents.ITEM_TRIDENT_RETURN, net.minecraft.sound.SoundCategory.NEUTRAL, 10.0F, 1.0F);
                    }
                }

                if (trident.isNoClip()) {
                    Vec3d vec3d = owner.getEyePos().subtract(trident.getPos());
                    trident.setPos(trident.getX(), trident.getY() + vec3d.y * 0.015 * 3.0, trident.getZ());
                    double d = 0.05 * 3.0;
                    trident.setVelocity(trident.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                    
                    if (trident.getBoundingBox().intersects(owner.getBoundingBox().expand(0.5))) {
                        if (owner instanceof net.minecraft.entity.player.PlayerEntity player) {
                            if (this.tryPickup(player)) {
                                trident.discard();
                            }
                        }
                    }
                }
            }
        }
    }
}

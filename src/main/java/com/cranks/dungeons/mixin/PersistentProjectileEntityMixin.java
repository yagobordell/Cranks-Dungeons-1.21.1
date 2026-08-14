package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.WeaponProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements WeaponProvider {
    
    @Shadow
    public abstract ItemStack getItemStack();

    @Shadow
    public abstract ItemStack getWeaponStack();

    @Shadow
    public abstract byte getPierceLevel();

    @Shadow
    public abstract void setPierceLevel(byte level);

    @Unique
    private int cranks$ricochetsRemaining = 0;

    @Unique
    private boolean cranks$initializedModifiers = false;

    @Override
    public ItemStack cranks$getWeapon() {
        if ((Object)this instanceof TridentEntity) {
            return this.getItemStack();
        }
        ItemStack weapon = this.getWeaponStack();
        if (weapon != null && !weapon.isEmpty()) {
            return weapon;
        }
        return ItemStack.EMPTY;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ensureModifiersInitialized(CallbackInfo ci) {
        if (!this.cranks$initializedModifiers) {
            this.cranks$initializedModifiers = true;
            PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
            if (projectile.getOwner() instanceof LivingEntity livingOwner) {
                if (this.cranks$ricochetsRemaining == 0) {
                    double ricochetChance = livingOwner.getAttributeValue(ModAttributes.PROJECTILE_RICOCHET_CHANCE);
                    if (ricochetChance > 0) {
                        int guaranteed = (int) Math.floor(ricochetChance);
                        double remainder = ricochetChance - guaranteed;
                        int extra = guaranteed + (livingOwner.getRandom().nextFloat() < remainder ? 1 : 0);
                        this.cranks$ricochetsRemaining = extra;
                    }
                }
                if (this.getPierceLevel() == 0) {
                    double pierceChance = livingOwner.getAttributeValue(ModAttributes.PROJECTILE_PIERCE_CHANCE);
                    if (pierceChance > 0) {
                        int guaranteed = (int) Math.floor(pierceChance);
                        double remainder = pierceChance - guaranteed;
                        int extra = guaranteed + (livingOwner.getRandom().nextFloat() < remainder ? 1 : 0);
                        if (extra > 0) {
                            this.setPierceLevel((byte) Math.min(127, this.getPierceLevel() + extra));
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void handleRicochet(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (this.cranks$ricochetsRemaining > 0) {
            this.cranks$ricochetsRemaining--;

            PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
            Vec3d vel = projectile.getVelocity();
            Direction side = blockHitResult.getSide();
            Vec3d normal = Vec3d.of(side.getVector());

            // Reflect velocity across normal: v' = v - 2*(v.n)*n
            double dot = vel.dotProduct(normal);
            if (dot < 0) {
                Vec3d reflected = vel.subtract(normal.multiply(2.0 * dot));
                double speed = Math.max(vel.length() * 0.85, 0.75);
                reflected = reflected.normalize().multiply(speed);

                projectile.setVelocity(reflected);
                projectile.setPosition(projectile.getPos().add(normal.multiply(0.15)));
                projectile.velocityModified = true;

                if (!projectile.getWorld().isClient()) {
                    if (projectile.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
                    }
                    projectile.getWorld().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(), ModSounds.CRITICAL_HIT, SoundCategory.PLAYERS, 0.5f, 1.8f);
                }

                ci.cancel();
            }
        }
    }
}


package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.util.WeaponProvider;
import com.cranks.dungeons.party.PartyManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements WeaponProvider, com.cranks.dungeons.ranged.RangedProjectile {
    @Shadow public abstract ItemStack getItemStack();
    @Shadow public abstract ItemStack getWeaponStack();
    @Shadow public abstract byte getPierceLevel();
    @Shadow public abstract void setPierceLevel(byte level);
    @Shadow protected boolean inGround;
    @Unique private int cranks$ricochetsRemaining;
    @Unique private boolean cranks$initializedModifiers;
    @Unique private final Set<UUID> cranks$hitTargets = new HashSet<>();
    @Unique private Vec3d cranks$redirectVelocity, cranks$redirectPosition;

    @Unique private double cranks$fixedRangedDamage = -1;
    @Unique private float cranks$fixedRangedPower = 1;
    @Override public void cranks$configureRanged(double damage, float power) {
        if (Double.isFinite(damage) && damage > 0 && Float.isFinite(power)) {
            cranks$fixedRangedDamage = Math.clamp(damage, 0.01, 1_000_000);
            cranks$fixedRangedPower = Math.clamp(power, 0, 1);
        }
    }
    @Override public double cranks$rangedDamage() { return cranks$fixedRangedDamage; }
    @Override public float cranks$rangedPower() { return cranks$fixedRangedPower; }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="onEntityHit",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean cranks$consistentRangedHit(net.minecraft.entity.Entity victim, net.minecraft.entity.damage.DamageSource source,
            float amount, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        return original.call(victim, source, cranks$fixedRangedDamage > 0 && victim instanceof LivingEntity
                ? (float)cranks$fixedRangedDamage : amount);
    }

    @Override public ItemStack cranks$getWeapon() {
        if ((Object)this instanceof TridentEntity) return getItemStack();
        ItemStack weapon=getWeaponStack();
        return weapon==null?ItemStack.EMPTY:weapon;
    }

    @Unique private static int cranks$roll(LivingEntity owner, double chance) {
        double bounded=Math.clamp(chance,0,4);
        int whole=(int)Math.floor(bounded);
        return whole+(owner.getRandom().nextDouble()<bounded-whole?1:0);
    }

    @Inject(method="tick",at=@At("HEAD"))
    private void initializeModifiers(CallbackInfo ci) {
        var projectile=(PersistentProjectileEntity)(Object)this;
        if(projectile.getWorld().isClient()||cranks$initializedModifiers)return;
        if(inGround){cranks$initializedModifiers=true;return;}
        if(!(projectile.getOwner() instanceof LivingEntity owner))return;
        cranks$initializedModifiers=true;
        cranks$ricochetsRemaining=cranks$roll(owner,owner.getAttributeValue(ModAttributes.PROJECTILE_RICOCHET_CHANCE));
        // Tridents have their own one-hit/return behaviour, not arrow piercing.
        if(!(projectile instanceof TridentEntity)) {
            int extra=cranks$roll(owner,owner.getAttributeValue(ModAttributes.PROJECTILE_PIERCE_CHANCE));
            setPierceLevel((byte)Math.min(127,getPierceLevel()+extra));
        }
    }

    @Inject(method="onEntityHit",at=@At("HEAD"))
    private void rememberTarget(EntityHitResult hit,CallbackInfo ci) {
        if(cranks$hitTargets.size()<128)cranks$hitTargets.add(hit.getEntity().getUuid());
    }

    @Unique private boolean cranks$validTarget(LivingEntity target,LivingEntity owner) {
        if(!target.isAlive()||target.isSpectator()||target==owner||!target.isAttackable()
                ||target.isTeammate(owner)||cranks$hitTargets.contains(target.getUuid()))return false;
        if(target instanceof TameableEntity tame&&tame.isTamed())return false;
        if(target instanceof net.minecraft.entity.passive.AbstractHorseEntity horse&&horse.isTame())return false;
        if(target instanceof com.cranks.dungeons.mount.MountDataProvider mount
                &&mount.cranksdungeons$getMountData().ownerUuid!=null)return false;
        if(target instanceof PlayerEntity player) {
            if(owner instanceof PlayerEntity shooter)return owner.getServer().isPvpEnabled()
                    && shooter.shouldDamagePlayer(player)&&!PartyManager.areInSameParty(shooter.getUuid(),player.getUuid());
            return owner instanceof MobEntity mob&&mob.getTarget()==player||owner.getAttacker()==player;
        }
        return target instanceof HostileEntity||target instanceof SlimeEntity||target==owner.getAttacker()
                ||target.getAttacker()==owner||target instanceof MobEntity mob&&mob.getTarget()==owner;
    }

    @Inject(method="onBlockHit",at=@At("HEAD"),cancellable=true)
    private void redirectRicochet(BlockHitResult hit,CallbackInfo ci) {
        var projectile=(PersistentProjectileEntity)(Object)this;
        if(!(projectile.getWorld() instanceof ServerWorld world)||cranks$ricochetsRemaining<=0
                ||!(projectile.getOwner() instanceof LivingEntity owner))return;
        cranks$ricochetsRemaining--;
        Vec3d normal=Vec3d.of(hit.getSide().getVector());
        Vec3d exit=hit.getPos().add(normal.multiply(0.1));
        LivingEntity chosen=null;double nearest=144;
        for(var target:world.getEntitiesByClass(LivingEntity.class,new Box(exit,exit).expand(12),entity->cranks$validTarget(entity,owner))) {
            Vec3d point=target.getBoundingBox().getCenter();
            double distance=point.squaredDistanceTo(exit);
            if(distance>=nearest||point.subtract(exit).dotProduct(normal)<=0)continue;
            var ray=world.raycast(new RaycastContext(exit,point,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,projectile));
            if(ray.getType()!=HitResult.Type.MISS)continue;
            chosen=target;nearest=distance;
        }
        if(chosen==null)return; // No target: the ordinary block impact still happens.
        double speed=Math.max(0.5,projectile.getVelocity().length()*0.85);
        Vec3d point=chosen.getBoundingBox().getCenter();
        double flight=point.distanceTo(exit)/speed;
        Vec3d aim=point.add(0,0.025*flight*flight,0).subtract(exit);
        cranks$redirectVelocity=aim.normalize().multiply(speed);cranks$redirectPosition=exit;
        projectile.setVelocity(Vec3d.ZERO);projectile.setPosition(exit);
        world.spawnParticles(ParticleTypes.CRIT,exit.x,exit.y,exit.z,3,0.04,0.04,0.04,0.01);
        world.playSound(null,exit.x,exit.y,exit.z,SoundEvents.ENTITY_ARROW_HIT,SoundCategory.PLAYERS,0.35f,1.6f);
        ci.cancel();
    }

    @Inject(method="tick",at=@At("TAIL"))
    private void finishRedirect(CallbackInfo ci) {
        if(cranks$redirectVelocity==null)return;
        var projectile=(PersistentProjectileEntity)(Object)this;
        // Start the new flight on the next tick so a close target cannot be skipped.
        projectile.setPosition(cranks$redirectPosition);projectile.setVelocity(cranks$redirectVelocity);
        projectile.velocityModified=true;cranks$redirectVelocity=cranks$redirectPosition=null;
    }

    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void saveModifiers(NbtCompound nbt,CallbackInfo ci) {
        if(cranks$fixedRangedDamage>0){nbt.putDouble("CranksRangedDamage",cranks$fixedRangedDamage);nbt.putFloat("CranksRangedPower",cranks$fixedRangedPower);}
        nbt.putBoolean("CranksProjectileInitialized",cranks$initializedModifiers);
        nbt.putInt("CranksRicochets",cranks$ricochetsRemaining);
        var targets=new NbtList();cranks$hitTargets.forEach(id->targets.add(NbtHelper.fromUuid(id)));nbt.put("CranksProjectileTargets",targets);
    }

    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void loadModifiers(NbtCompound nbt,CallbackInfo ci) {
        if(nbt.contains("CranksRangedDamage"))cranks$configureRanged(nbt.getDouble("CranksRangedDamage"),nbt.getFloat("CranksRangedPower"));
        cranks$initializedModifiers=nbt.getBoolean("CranksProjectileInitialized");
        cranks$ricochetsRemaining=Math.clamp(nbt.getInt("CranksRicochets"),0,4);cranks$hitTargets.clear();
        var targets=nbt.getList("CranksProjectileTargets",NbtElement.INT_ARRAY_TYPE);
        for(int i=0;i<Math.min(128,targets.size());i++)try{cranks$hitTargets.add(NbtHelper.toUuid(targets.get(i)));}catch(IllegalArgumentException ignored){}
    }
}

package com.cranks.dungeons.mixin;

import com.cranks.dungeons.ranged.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;

@Mixin(RangedWeaponItem.class)
abstract class RangedLaunchMixin {
    @WrapMethod(method="shootAll")
    private void cranks$launchStrength(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack weapon,
                                      List<ItemStack> ammunition, float speed, float divergence, boolean critical,
                                      LivingEntity target, Operation<Void> original) {
        RangedLaunch.withPower(weapon.getItem() instanceof BowItem ? RangedRules.power(speed) : 1,
                () -> original.call(world,shooter,hand,weapon,ammunition,speed,divergence,critical,target));
    }
    @WrapOperation(method="shootAll",at=@At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;createArrowEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
    private ProjectileEntity cranks$preparePlayerArrow(RangedWeaponItem item, World world, LivingEntity shooter,
            ItemStack weapon, ItemStack ammunition, boolean critical, Operation<ProjectileEntity> original) {
        var projectile=original.call(item,world,shooter,weapon,ammunition,critical);
        if (shooter instanceof PlayerEntity player && (weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)
                && projectile instanceof PersistentProjectileEntity arrow) {
            ((RangedProjectile)arrow).cranks$configureRanged(RangedLaunch.damage(player,weapon),RangedLaunch.power());
            arrow.setCritical(false);
        }
        return projectile;
    }
}

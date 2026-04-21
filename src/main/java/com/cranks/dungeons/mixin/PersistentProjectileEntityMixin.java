package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.WeaponProvider;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements WeaponProvider {
    
    @Shadow
    public abstract ItemStack getItemStack();

    @Shadow
    public abstract ItemStack getWeaponStack();

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
}

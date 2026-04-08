package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.TridentStackProvider;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements TridentStackProvider {
    
    @Shadow
    public abstract ItemStack getItemStack();

    @Override
    public ItemStack cranks$getTridentStack() {
        return this.getItemStack();
    }
}

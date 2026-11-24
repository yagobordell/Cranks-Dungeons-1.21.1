package com.cranks.dungeons.mixin;

import com.cranks.dungeons.mount.*;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MountOrderMixin implements MountOrderAccess {
    @Shadow @Final protected GoalSelector goalSelector;
    @Unique private MountOrder cranks$order;
    @Override public MountOrder cranks$getOrder(){if(cranks$order==null)cranks$order=new MountOrder();return cranks$order;}
    @Inject(method="<init>",at=@At("TAIL"))
    private void installMountOrder(CallbackInfo ci){var mount=(MobEntity)(Object)this;if(MountKind.of(mount)!=MountKind.UNSUPPORTED)goalSelector.add(3,new MountOrderGoal(mount));}
    @Inject(method="tick",at=@At("HEAD"))
    private void updateOrder(CallbackInfo ci){var mount=(MobEntity)(Object)this;if(!mount.getWorld().isClient()&&MountKind.of(mount)!=MountKind.UNSUPPORTED)MountOrders.tick(mount);}
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void saveOrder(NbtCompound nbt,CallbackInfo ci){if(MountKind.of((MobEntity)(Object)this)!=MountKind.UNSUPPORTED)nbt.put("CranksMountOrder",cranks$getOrder().toNbt());}
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void loadOrder(NbtCompound nbt,CallbackInfo ci){if(MountKind.of((MobEntity)(Object)this)!=MountKind.UNSUPPORTED)cranks$getOrder().readNbt(nbt.getCompound("CranksMountOrder"));}
}

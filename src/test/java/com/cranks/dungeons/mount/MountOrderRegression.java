package com.cranks.dungeons.mount;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public final class MountOrderRegression {
    public static void run(){
        var order=new MountOrder();order.mode=MountOrder.Mode.STAY;order.issuer=UUID.randomUUID();order.destination=new Vec3d(12.5,70,-4.5);order.dimension="minecraft:overworld";order.running=true;
        var loaded=new MountOrder();loaded.readNbt(order.toNbt());
        require(loaded.mode==order.mode&&loaded.issuer.equals(order.issuer)&&loaded.destination.equals(order.destination)&&loaded.dimension.equals(order.dimension),"Saved command changed");
        require(!loaded.running,"Loaded command retained a running navigation goal");
        var invalid=order.toNbt();invalid.putDouble("X",Double.NaN);loaded.readNbt(invalid);require(loaded.mode==MountOrder.Mode.NONE,"Invalid saved destination reached navigation");
        invalid=order.toNbt();invalid.putString("Mode","unknown");loaded.readNbt(invalid);require(loaded.mode==MountOrder.Mode.NONE,"Unknown saved command was accepted");
        loaded.readNbt(new NbtCompound());require(loaded.mode==MountOrder.Mode.NONE&&loaded.issuer==null,"Old saves did not default to free roaming");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

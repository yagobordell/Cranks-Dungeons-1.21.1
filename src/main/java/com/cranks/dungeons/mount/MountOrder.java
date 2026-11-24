package com.cranks.dungeons.mount;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

/** Saved intent only. Paths and running goal state are always rebuilt after loading. */
public final class MountOrder {
    public enum Mode { NONE,COME,FOLLOW,STAY }
    public Mode mode=Mode.NONE;
    public UUID issuer;
    public Vec3d destination=Vec3d.ZERO;
    public String dimension="";
    public boolean running;
    public long revision;

    public void clear(){mode=Mode.NONE;issuer=null;revision++;}
    public NbtCompound toNbt(){
        var tag=new NbtCompound();tag.putString("Mode",mode.name());if(issuer!=null)tag.putUuid("Issuer",issuer);
        tag.putDouble("X",destination.x);tag.putDouble("Y",destination.y);tag.putDouble("Z",destination.z);tag.putString("Dimension",dimension);return tag;
    }
    public void readNbt(NbtCompound tag){
        clear();running=false;
        try{mode=Mode.valueOf(tag.getString("Mode"));}catch(IllegalArgumentException ignored){mode=Mode.NONE;}
        issuer=tag.containsUuid("Issuer")?tag.getUuid("Issuer"):null;
        destination=new Vec3d(tag.getDouble("X"),tag.getDouble("Y"),tag.getDouble("Z"));dimension=tag.getString("Dimension");
        if(issuer==null||!Double.isFinite(destination.x)||!Double.isFinite(destination.y)||!Double.isFinite(destination.z))clear();
    }
}

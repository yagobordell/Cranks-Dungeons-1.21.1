package com.cranks.dungeons.mount;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import java.util.*;

/** Bounded, nearest-first pedigree shared by every breedable mount. */
public final class MountLineage {
    public static final int LIMIT=16;
    private MountLineage(){}

    public static LinkedHashMap<UUID,Integer> known(UUID sire,UUID dam,Collection<UUID> legacy,Map<UUID,Integer> depths){
        var result=new LinkedHashMap<UUID,Integer>();
        if(sire!=null)result.put(sire,1);
        if(dam!=null)result.put(dam,1);
        for(UUID id:legacy)result.putIfAbsent(id,Math.clamp(depths.getOrDefault(id,2),1,LIMIT));
        return result;
    }

    public static LinkedHashMap<UUID,Integer> inherit(UUID first,Map<UUID,Integer> a,UUID second,Map<UUID,Integer> b){
        var candidates=new LinkedHashMap<UUID,Integer>();candidates.put(first,1);candidates.put(second,1);
        // Interleave parents within each generation before applying the cap.
        for(int depth=1;depth<LIMIT;depth++){
            final int generation=depth;
            var left=a.entrySet().stream().filter(e->e.getValue()==generation).iterator();
            var right=b.entrySet().stream().filter(e->e.getValue()==generation).iterator();
            while(left.hasNext()||right.hasNext()){
                if(left.hasNext()){var e=left.next();candidates.merge(e.getKey(),depth+1,Math::min);}
                if(right.hasNext()){var e=right.next();candidates.merge(e.getKey(),depth+1,Math::min);}
            }
        }
        var result=new LinkedHashMap<UUID,Integer>();
        candidates.entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(LIMIT)
                .forEach(e->result.put(e.getKey(),e.getValue()));
        return result;
    }

    public static void write(NbtCompound nbt,Map<UUID,Integer> depths){
        var list=new NbtList();
        depths.entrySet().stream().limit(LIMIT).forEach(e->{var tag=new NbtCompound();tag.putUuid("Id",e.getKey());tag.putInt("Depth",e.getValue());list.add(tag);});
        nbt.put("AncestryDepths",list);
    }

    public static void read(NbtCompound nbt,Map<UUID,Integer> depths){
        depths.clear();var list=nbt.getList("AncestryDepths",NbtElement.COMPOUND_TYPE);
        for(int i=0;i<Math.min(LIMIT,list.size());i++){var entry=list.getCompound(i);if(entry.containsUuid("Id"))depths.put(entry.getUuid("Id"),Math.clamp(entry.getInt("Depth"),1,LIMIT));}
    }
}

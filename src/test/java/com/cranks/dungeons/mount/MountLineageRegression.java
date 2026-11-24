package com.cranks.dungeons.mount;

import java.util.*;

public final class MountLineageRegression {
    public static void run(){
        UUID sire=UUID.randomUUID(),dam=UUID.randomUUID();
        var a=new LinkedHashMap<UUID,Integer>();var b=new LinkedHashMap<UUID,Integer>();
        UUID paternalGrandparent=UUID.randomUUID(),maternalGrandparent=UUID.randomUUID();
        a.put(paternalGrandparent,1);b.put(maternalGrandparent,1);
        for(int i=0;i<16;i++){a.put(UUID.randomUUID(),2+i/4);b.put(UUID.randomUUID(),2+i/4);}
        var child=MountLineage.inherit(sire,a,dam,b);
        require(child.size()==16,"Pedigree cap failed");
        require(child.get(sire)==1&&child.get(dam)==1,"A direct parent was lost");
        require(child.get(paternalGrandparent)==2&&child.get(maternalGrandparent)==2,"One side crowded out close relatives");
        require(child.values().stream().noneMatch(d->d>4),"Distant ancestors displaced closer relatives");
        UUID shared=UUID.randomUUID();a.put(shared,1);b.put(shared,3);
        require(MountLineage.inherit(sire,a,dam,b).get(shared)==2,"Shared ancestor did not keep nearest distance");
        var old=MountLineage.known(sire,dam,List.of(shared),Map.of());
        require(old.keySet().containsAll(List.of(sire,dam,shared)),"Legacy pedigree lost known family");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

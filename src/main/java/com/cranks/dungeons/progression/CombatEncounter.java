package com.cranks.dungeons.progression;

import java.util.*;
/** Runtime participation expires; only spawn provenance and the paid flag are saved with the mob. */
public final class CombatEncounter {
    public int origin; // 0: unknown/legacy (ineligible), 1: eligible, 2: excluded
    public boolean paid;
    public record Hit(double damage,long tick) {}
    public final Map<UUID,Hit> hits=new HashMap<>();
    public final Map<UUID,Long> support=new HashMap<>();
    public void damage(UUID player,double damage,long now,double budget,int window) {
        if (!Double.isFinite(damage)||damage<=0) return;
        hits.entrySet().removeIf(e->now-e.getValue().tick()>window);
        Hit old=hits.get(player);
        double used=hits.values().stream().mapToDouble(Hit::damage).sum();
        double accepted=Math.min(damage,Math.max(0,budget-used));
        hits.put(player,new Hit((old==null?0:old.damage())+accepted,now));
    }
}

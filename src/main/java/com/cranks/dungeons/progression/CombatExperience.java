package com.cranks.dungeons.progression;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.config.ProgressionConfig;
import com.cranks.dungeons.mixin.MobEntityAccessor;
import com.cranks.dungeons.party.PartyManager;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import java.util.*;

public final class CombatExperience {
    private record Pending(MobEntity mob,CombatEncounter encounter) {}
    private static final Map<MinecraftServer,List<Pending>> pending=new IdentityHashMap<>();
    private CombatExperience() {}
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server->{
            var list=pending.remove(server);
            if(list!=null)for(var death:list)pay(death.mob,death.encounter);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(pending::remove);
    }
    public static boolean enemy(LivingEntity mob) {
        return mob instanceof Monster || mob instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity
            || mob instanceof Angerable angry && angry.getAngryAt()!=null;
    }
    private static boolean eligible(ServerPlayerEntity p,MobEntity mob) {
        return p!=null && p.isAlive() && !p.isSpectator() && !p.isCreative() && p.getWorld()==mob.getWorld()
            && p.squaredDistanceTo(mob)<=Math.pow(ProgressionConfig.RULES.shareRadius,2);
    }
    public static void damage(LivingEntity target,UUID playerId,float amount) {
        if (!(target instanceof MobEntity mob) || !(target instanceof CombatEncounterAccess access) || target.getWorld().isClient()) return;
        var server=target.getServer(); if(server==null)return;
        var player=server.getPlayerManager().getPlayer(playerId);
        if(!eligible(player,mob)||!enemy(mob))return;
        long now=mob.getWorld().getTime();
        // Actual health + absorption + energy shield damage only. Overkill never increases shares.
        double budget=mob.getMaxHealth()+mob.getAbsorptionAmount()+Math.max(0,mob.getAttributeValue(ModAttributes.ENERGY_SHIELD));
        access.cranks$encounter().damage(playerId,amount,now,budget,ProgressionConfig.RULES.activityTicks);
        ModComponents.PROGRESSION.get(player).recentEnemies.put(mob.getUuid(),now);
    }
    private static boolean active(ServerPlayerEntity player) {
        return net.minecraft.util.Util.getMeasuringTimeMs()-player.getLastActionTime()<=ProgressionConfig.RULES.activityTicks*50L;
    }
    public static void attacked(LivingEntity target,DamageSource source,float amount) {
        if(amount<=0||!(target instanceof ServerPlayerEntity player)||!(source.getAttacker() instanceof MobEntity mob)
                || !enemy(mob)||!eligible(player,mob)||!active(player)) return;
        long now=mob.getWorld().getTime();
        ((CombatEncounterAccess)mob).cranks$encounter().support.put(player.getUuid(),now);
        ModComponents.PROGRESSION.get(player).recentEnemies.put(mob.getUuid(),now);
    }
    public static void support(PlayerEntity helper,LivingEntity ally) {
        if(!(helper instanceof ServerPlayerEntity player)||!(ally instanceof ServerPlayerEntity friend)
                ||player==friend||!active(player)||!PartyManager.areInSameParty(player.getUuid(),friend.getUuid()))return;
        long now=player.getWorld().getTime();
        for(var entry:ModComponents.PROGRESSION.get(friend).recentEnemies.entrySet()) {
            if(now-entry.getValue()>ProgressionConfig.RULES.activityTicks)continue;
            var entity=friend.getServerWorld().getEntity(entry.getKey());
            if(entity instanceof MobEntity mob && eligible(player,mob))
                ((CombatEncounterAccess)mob).cranks$encounter().support.put(player.getUuid(),now);
        }
    }
    public static void death(LivingEntity entity) {
        if(!(entity instanceof MobEntity mob)||entity.getWorld().isClient()||!enemy(entity))return;
        var state=((CombatEncounterAccess)mob).cranks$encounter();
        if(state.paid)return;
        state.paid=true;
        if(state.origin!=1)return;
        pending.computeIfAbsent(mob.getServer(),s->new ArrayList<>()).add(new Pending(mob,state));
    }
    private static void pay(MobEntity mob,CombatEncounter encounter) {
        var rules=ProgressionConfig.RULES;
        long now=mob.getWorld().getTime();
        var server=mob.getServer(); if(server==null)return;
        Map<Object,Double> damage=new HashMap<>();
        Map<Object,List<ServerPlayerEntity>> members=new HashMap<>();
        double threshold=Math.max(.1,mob.getMaxHealth()*rules.minimumDamageFraction);
        for(var entry:encounter.hits.entrySet()) {
            var player=server.getPlayerManager().getPlayer(entry.getKey());
            if(!eligible(player,mob)||now-entry.getValue().tick()>rules.activityTicks||entry.getValue().damage()<threshold)continue;
            Object group=PartyManager.getParty(entry.getKey()).<Object>map(p->p).orElse(entry.getKey());
            damage.merge(group,entry.getValue().damage(),Double::sum);
            members.computeIfAbsent(group,k->new ArrayList<>()).add(player);
        }
        for(var entry:encounter.support.entrySet()) {
            var player=server.getPlayerManager().getPlayer(entry.getKey());
            if(!eligible(player,mob)||now-entry.getValue()>rules.activityTicks)continue;
            var party=PartyManager.getParty(entry.getKey());
            if(party.isPresent()&&members.containsKey(party.get())&&!members.get(party.get()).contains(player))members.get(party.get()).add(player);
        }
        double total=damage.values().stream().mapToDouble(Double::doubleValue).sum();
        if(total<=0)return;
        int base=((MobEntityAccessor)mob).cranks$baseExperience();
        if(mob instanceof net.minecraft.entity.boss.WitherEntity)base=1000;
        if(mob instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity)base=1200;
        if(mob instanceof net.minecraft.entity.mob.WardenEntity)base=500;
        if(base<=0)return;
        com.cranks.dungeons.economy.EconomyRewards.combat(mob,damage,members,total,base);
        for(var entry:damage.entrySet()) {
            var players=members.get(entry.getKey()); players.sort(Comparator.comparing(PlayerEntity::getUuid));
            long pool=(long)(base*ProgressionMath.UNIT*entry.getValue()/total*ProgressionMath.partyMultiplier(players.size()));
            long[] shares=ProgressionMath.split(pool,players.size(),mob.getId());
            for(int i=0;i<players.size();i++) {
                var player=players.get(i);
                double bonus=player.getAttributeValue(ModAttributes.EXPERIENCE_BONUS)+player.getAttributeValue(ModAttributes.COMBAT_EXPERIENCE);
                ModComponents.PROGRESSION.get(player).addCombat(ProgressionMath.boosted(shares[i],bonus));
            }
        }
    }
}

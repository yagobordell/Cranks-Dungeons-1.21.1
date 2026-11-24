package com.cranks.dungeons.economy;

import com.cranks.dungeons.mixin.CoinDoubleInventoryAccess;
import com.cranks.dungeons.network.CoinGainPayload;
import com.cranks.dungeons.progression.ProgressionMath;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

public final class EconomyRewards {
    public static void notifyGain(ServerPlayerEntity player,long coins){
        if(coins>0)ServerPlayNetworking.send(player,new CoinGainPayload(coins));
    }
    public static void combat(MobEntity mob,Map<Object,Double> damage,Map<Object,List<ServerPlayerEntity>> members,double total,int baseXp){
        var rules=EconomyConfig.RULES;
        long pool=Math.clamp(Math.round(baseXp*rules.coinsPerBaseXp),rules.minimumMobCoins,rules.maximumMobCoins);
        if(pool<=0||total<=0)return;
        var groups=new ArrayList<>(damage.keySet());
        groups.sort(Comparator.comparing(group->members.get(group).stream().map(p->p.getUuid().toString()).min(String::compareTo).orElse("")));
        long[] awards=new long[groups.size()];long assigned=0;
        for(int i=0;i<groups.size();i++){awards[i]=(long)Math.floor(pool*damage.get(groups.get(i))/total);assigned+=awards[i];}
        for(long i=assigned;i<pool;i++)awards[Math.floorMod(mob.getId()+(int)(i-assigned),awards.length)]++;
        var state=EconomyState.get(mob.getServer());
        for(int i=0;i<groups.size();i++){
            var players=new ArrayList<>(members.get(groups.get(i)));players.sort(Comparator.comparing(p->p.getUuid().toString()));
            var shares=ProgressionMath.split(awards[i],players.size(),mob.getId());
            for(int j=0;j<players.size();j++)notifyGain(players.get(j),state.credit(players.get(j),shares[j]));
        }
    }
    public static void chest(Inventory inventory,ServerPlayerEntity player){
        if(player.isCreative()||player.isSpectator()||!player.isAlive())return;
        if(inventory instanceof DoubleInventory){var pair=(CoinDoubleInventoryAccess)inventory;chest(pair.cranks$first(),player);chest(pair.cranks$second(),player);return;}
        if(!(inventory instanceof ChestBlockEntity||inventory instanceof BarrelBlockEntity||inventory instanceof ShulkerBoxBlockEntity||inventory instanceof ChestMinecartEntity))return;
        var metadata=(CoinContainer)inventory;
        if(inventory instanceof LootableContainerBlockEntity container)metadata.cranks$markCoins(container.getLootTable());
        if(inventory instanceof ChestMinecartEntity cart)metadata.cranks$markCoins(cart.getLootTable());
        if(metadata.cranks$coinId()==null)return;
        var state=EconomyState.get(player.getServer());
        if(!state.claimChest(metadata.cranks$coinId(),player.getUuid()))return;
        var rules=EconomyConfig.RULES;
        long seed=metadata.cranks$coinId().getMostSignificantBits()^metadata.cranks$coinId().getLeastSignificantBits()^player.getUuid().getMostSignificantBits()^player.getUuid().getLeastSignificantBits();
        long amount=rules.minimumChestCoins+new Random(seed).nextInt(rules.maximumChestCoins-rules.minimumChestCoins+1);
        notifyGain(player,state.credit(player,amount));
    }
}

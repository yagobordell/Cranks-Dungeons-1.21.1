package com.cranks.dungeons.component;

import com.cranks.dungeons.config.ProgressionConfig;
import com.cranks.dungeons.progression.ProgressionMath;
import com.cranks.dungeons.progression.WorldDiscovery;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import java.util.*;

/** Persisted authority. Native XP fields are only a compatibility mirror. Claims never leave the server. */
public final class PlayerProgressionComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final PlayerEntity player;
    private boolean initialized;
    private int level, highestLevel, clientRequired = 50;
    private long xp, worldRemainder;
    private final Set<String> claims = new HashSet<>();
    public final Map<UUID,Long> recentEnemies = new HashMap<>();
    public PlayerProgressionComponent(PlayerEntity player) { this.player=player; }
    public int level() { return level; }
    public long units() { return xp; }
    public int required() { return player.getWorld().isClient() ? clientRequired : ProgressionConfig.RULES.required(true,level); }
    public boolean initialized() { return initialized; }
    public boolean claim(String key) { return claims.add(key); }
    public boolean hasClaim(String key) { return claims.contains(key); }
    public void initialize() {
        if (initialized || !(player instanceof ServerPlayerEntity server)) return;
        level=Math.max(0,player.experienceLevel); highestLevel=level;
        xp=(long)(Math.clamp(player.experienceProgress,0,1)*ProgressionConfig.RULES.required(true,level)*ProgressionMath.UNIT);
        initialized=true;
        WorldDiscovery.baseline(server);
        mirror(); sync();
    }
    public void addCombat(long units) {
        if (!(player instanceof ServerPlayerEntity) || units<=0) return;
        initialize();
        if (required()<=0) return;
        xp=Math.min(Long.MAX_VALUE/4,xp+units);
        int gained=0, need;
        while ((need=required())>0 && xp>=need*ProgressionMath.UNIT) {
            xp-=need*ProgressionMath.UNIT; level++;
            if (level>highestLevel) { gained++; highestLevel=level; }
        }
        if (required()==0) xp=0;
        if (gained>0) {
            ModComponents.PASSIVE_DATA.get(player).addUnspentPoints(gained);
            com.cranks.dungeons.entity.LevelUpVfxEntity.celebrate(player,false);
        }
        mirror(); sync();
    }
    public void addWorld(long units) {
        if (!(player instanceof ServerPlayerEntity) || units<=0) return;
        long total=worldRemainder+units;
        worldRemainder=total%ProgressionMath.UNIT;
        ModComponents.WORLD_TREE_DATA.get(player).addGatheringXp((int)Math.min(Integer.MAX_VALUE,total/ProgressionMath.UNIT));
    }
    public void deathPenalty() {
        xp=ProgressionMath.deathPenalty(xp,required()); mirror(); sync();
    }
    public void mirror() {
        if (!initialized) return;
        player.experienceLevel=level;
        player.experienceProgress=required()==0?1:(float)((double)xp/(required()*ProgressionMath.UNIT));
        player.totalExperience=(int)Math.min(Integer.MAX_VALUE,xp/ProgressionMath.UNIT);
    }
    private void sync() { ModComponents.PROGRESSION.sync(player); }
    @Override public void serverTick() {
        if (!initialized) return; // JOIN restores legacy backups first.
        mirror();
        if (player.age%20==0 && player instanceof ServerPlayerEntity server) {
            recentEnemies.entrySet().removeIf(e->server.getWorld().getTime()-e.getValue()>ProgressionConfig.RULES.activityTicks);
            WorldDiscovery.tick(server);
        }
    }
    @Override public boolean shouldSyncWith(ServerPlayerEntity viewer) { return viewer==player; }
    @Override public void writeSyncPacket(RegistryByteBuf buf,ServerPlayerEntity recipient) {
        buf.writeInt(level); buf.writeLong(xp); buf.writeInt(required()); buf.writeBoolean(initialized);
    }
    @Override public void applySyncPacket(RegistryByteBuf buf) {
        level=buf.readInt(); xp=buf.readLong(); clientRequired=buf.readInt(); initialized=buf.readBoolean(); mirror();
    }
    @Override public void readFromNbt(NbtCompound tag,RegistryWrapper.WrapperLookup registries) {
        initialized=tag.getBoolean("initialized"); level=Math.max(0,tag.getInt("level"));
        highestLevel=Math.max(level,tag.getInt("highestLevel")); xp=Math.clamp(tag.getLong("xpUnits"),0,100000000L*ProgressionMath.UNIT);
        worldRemainder=Math.floorMod(tag.getLong("worldRemainder"),ProgressionMath.UNIT);
        claims.clear(); var list=tag.getList("claims",NbtElement.STRING_TYPE);
        for(int i=0;i<list.size();i++) claims.add(list.getString(i));
    }
    @Override public void writeToNbt(NbtCompound tag,RegistryWrapper.WrapperLookup registries) {
        tag.putBoolean("initialized",initialized); tag.putInt("level",level); tag.putInt("highestLevel",highestLevel);
        tag.putLong("xpUnits",xp); tag.putLong("worldRemainder",worldRemainder);
        var list=new NbtList(); claims.stream().sorted().forEach(key->list.add(NbtString.of(key))); tag.put("claims",list);
    }
}

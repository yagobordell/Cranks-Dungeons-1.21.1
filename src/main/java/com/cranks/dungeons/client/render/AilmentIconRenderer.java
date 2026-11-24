package com.cranks.dungeons.client.render;
import com.cranks.dungeons.network.AilmentSyncPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.entity.LivingEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/** World-scoped icon/detail cache. Rendering belongs to the projected nameplate pass. */
public final class AilmentIconRenderer {
    private record Snapshot(List<AilmentSyncPayload.AilmentEntry> entries, long receivedTick) {}
    private record Resisted(UUID entityUuid, long expiresTick) {}
    private static final Map<Integer,Snapshot> CACHE=new ConcurrentHashMap<>();
    private static final Map<Integer,Resisted> RESISTED=new ConcurrentHashMap<>();
    private static ClientWorld world;
    private static long lastPrunedTick=Long.MIN_VALUE;
    private AilmentIconRenderer() {
    }
    private static void checkWorld() {
        ClientWorld current=MinecraftClient.getInstance().world;
        if(current!=world) {
            CACHE.clear();
            RESISTED.clear();
            lastPrunedTick=Long.MIN_VALUE;
            world=current;
        }
        if(world!=null && world.getTime()!=lastPrunedTick) {
            lastPrunedTick=world.getTime();
            RESISTED.values().removeIf(value -> value.expiresTick()<=lastPrunedTick);
        }
    }
    public static void clear() {
        CACHE.clear();
        RESISTED.clear();
        lastPrunedTick=Long.MIN_VALUE;
        world=null;
    }
    public static void remove(int entityId) {
        CACHE.remove(entityId);
        RESISTED.remove(entityId);
    }
    public static void updateAilments(int entityId,List<AilmentSyncPayload.AilmentEntry> ailments) {
        checkWorld();
        if(ailments.isEmpty())CACHE.remove(entityId);
        else if(world!=null)CACHE.put(entityId,new Snapshot(List.copyOf(ailments),world.getTime()));
    }
    public static List<AilmentSyncPayload.AilmentEntry> getAilments(int entityId) {
        checkWorld();
        var snapshot=CACHE.get(entityId);
        return snapshot==null?List.of():snapshot.entries();
    }
    public static float remainingSeconds(int entityId,AilmentSyncPayload.AilmentEntry entry) {
        checkWorld();
        var snapshot=CACHE.get(entityId);
        return snapshot==null || world==null ? entry.remainingSeconds()
                : AilmentPresentation.remaining(entry.remainingSeconds(),snapshot.receivedTick(),world.getTime());
    }
    public static void resisted(int entityId,UUID entityUuid) {
        checkWorld();
        var entity=world==null?null:world.getEntityById(entityId);
        if(entity==null || !entity.getUuid().equals(entityUuid))return;
        RESISTED.put(entityId,new Resisted(entityUuid,world.getTime()+AilmentPresentation.RESISTED_TICKS));
    }
    public static int resistedTicks(LivingEntity entity) {
        checkWorld();
        var feedback=RESISTED.get(entity.getId());
        return feedback==null || world==null || entity.getWorld()!=world || !feedback.entityUuid().equals(entity.getUuid())
                ? 0 : (int)Math.max(0,feedback.expiresTick()-world.getTime());
    }
    public static Set<Integer> getTrackedEntityIds() {
        checkWorld();
        return Set.copyOf(CACHE.keySet());
    }
    public static AilmentSyncPayload.AilmentEntry details(int entityId,StatusEffectInstance effect) {
        var id=Registries.STATUS_EFFECT.getId(effect.getEffectType().value());
        if(!id.getNamespace().equals("cranks-dungeons"))return null;
        String path="textures/mob_effect/"+id.getPath()+".png";
        return getAilments(entityId).stream().filter(a->!a.buildup()&&a.texture().getPath().equals(path)).findFirst().orElse(null);
    }
}

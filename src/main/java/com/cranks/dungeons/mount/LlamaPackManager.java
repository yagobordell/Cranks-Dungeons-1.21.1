package com.cranks.dungeons.mount;

import com.cranks.dungeons.party.PartyManager;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Finds the connected group of nearby, owner-compatible llamas. */
public final class LlamaPackManager {
    private static final java.util.Map<net.minecraft.world.World, PackCache> CACHES =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final class PackCache {
        long tick = Long.MIN_VALUE;
        final java.util.Map<UUID,List<java.lang.ref.WeakReference<LlamaEntity>>> groups = new java.util.HashMap<>();
    }
    public static void invalidate() { CACHES.clear(); }

    /** Recheck membership at interaction boundaries; slot reads reuse the resulting snapshot. */
    public static List<LlamaEntity> freshMembers(LlamaEntity anchor) {
        CACHES.remove(anchor.getWorld());
        return members(anchor);
    }
    // In 1.21.1 the carpet is BODY equipment, outside the horse-items inventory.
    public static final int CARGO_START = 1;
    public static int memberCapacity(LlamaEntity llama) {
        int slots=AbstractHorseInventoryAccess.of(llama).cranksdungeons$getHorseItems().size()-CARGO_START;
        return Math.max(0,Math.min(slots,llama.getStrength()*3));
    }
    public static final double PACK_RADIUS = 16.0;
    private static final double PACK_RADIUS_SQUARED = PACK_RADIUS * PACK_RADIUS;
    private static final double MAX_PACK_SPEED_BONUS = 0.50;

    private LlamaPackManager() { }

    public static List<LlamaEntity> members(LlamaEntity anchor) {
        var world=anchor.getWorld();
        var cache=CACHES.computeIfAbsent(world, ignored->new PackCache());
        long tick=world.getTime();
        if(cache.tick!=tick){cache.tick=tick;cache.groups.clear();}
        var saved=cache.groups.get(anchor.getUuid());
        if(saved!=null){
            var live=saved.stream().map(java.lang.ref.WeakReference::get).toList();
            if(live.stream().allMatch(e->e!=null&&e.isAlive()&&e.getWorld()==world))return live;
            cache.groups.clear();
        }
        ArrayDeque<LlamaEntity> pending = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        List<LlamaEntity> result = new ArrayList<>();
        pending.add(anchor);
        visited.add(anchor.getUuid());
        while (!pending.isEmpty()) {
            LlamaEntity current = pending.removeFirst();
            result.add(current);
            Box search = current.getBoundingBox().expand(PACK_RADIUS);
            for (LlamaEntity candidate : current.getEntityWorld().getEntitiesByClass(
                    LlamaEntity.class, search, llama -> llama.isAlive()
                            && !visited.contains(llama.getUuid()))) {
                if (current.squaredDistanceTo(candidate) > PACK_RADIUS_SQUARED
                        || !ownersCompatible(current, candidate)) continue;
                visited.add(candidate.getUuid());
                pending.addLast(candidate);
            }
        }
        result.sort(Comparator.comparing(llama -> llama.getUuid().toString()));
        var snapshot=List.copyOf(result);
        var weak=snapshot.stream().map(java.lang.ref.WeakReference::new).toList();
        for(var member:snapshot)cache.groups.put(member.getUuid(),weak);
        return snapshot;
    }

    public static int cargoCapacity(LlamaEntity anchor) {
        int capacity = 0;
        for (LlamaEntity llama : members(anchor)) {
            if (!((MountDataProvider) llama).cranksdungeons$hasMountChest()) continue;
            capacity += memberCapacity(llama);
        }
        return capacity;
    }

    /** Reserves each current pack member's cargo slots for live chest changes. */
    public static int screenCapacity(LlamaEntity anchor) {
        int capacity = cargoCapacity(anchor);
        for (LlamaEntity llama : members(anchor)) {
            if (!((MountDataProvider) llama).cranksdungeons$hasMountChest()) {
                capacity += llama.getStrength() * 3;
            }
        }
        return capacity;
    }

    public static double speedBonus(LlamaEntity llama, double packInstinct, String trait) {
        return speedBonusForSize(members(llama).size(), packInstinct, trait);
    }

    public static double speedBonusForSize(int packSize, double packInstinct, String trait) {
        int companions = Math.max(0, packSize - 1);
        if (companions == 0) return 0.0;
        double bonus = companions * (0.04 + Math.max(0.0, packInstinct));
        if ("Pack Leader".equals(trait)) bonus += 0.05;
        return Math.min(MAX_PACK_SPEED_BONUS, bonus);
    }

    public static boolean samePack(LlamaEntity anchor, List<UUID> expectedMembers) {
        List<UUID> current = freshMembers(anchor).stream().map(LlamaEntity::getUuid).toList();
        return current.equals(expectedMembers);
    }

    public static boolean canPlayerAccessPack(PlayerEntity player, LlamaEntity anchor) {
        for (LlamaEntity llama : members(anchor)) {
            if (!((MountDataProvider) llama).cranksdungeons$hasMountChest()) continue;
            if (!MountManager.canAccessStorage(player, llama)) return false;
        }
        return true;
    }

    private static boolean ownersCompatible(LlamaEntity first, LlamaEntity second) {
        UUID firstOwner = first.isTame() ? first.getOwnerUuid() : null;
        UUID secondOwner = second.isTame() ? second.getOwnerUuid() : null;
        if (firstOwner == null || secondOwner == null) return firstOwner == null && secondOwner == null;
        return firstOwner.equals(secondOwner)
                || PartyManager.areInSameParty(firstOwner, secondOwner);
    }
}

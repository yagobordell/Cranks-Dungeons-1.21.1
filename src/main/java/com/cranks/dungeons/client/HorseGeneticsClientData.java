package com.cranks.dungeons.client;

import com.cranks.dungeons.network.HorseGeneticsSyncPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HorseGeneticsClientData {
    private static final Map<Integer, HorseGeneticsSyncPacket> HORSES = new ConcurrentHashMap<>();

    private HorseGeneticsClientData() {
    }

    public static void update(HorseGeneticsSyncPacket packet) {
        HORSES.put(packet.entityId(), packet);
    }

    public static HorseGeneticsSyncPacket get(int entityId) {
        return HORSES.get(entityId);
    }

    public static void clear() {
        HORSES.clear();
    }
}

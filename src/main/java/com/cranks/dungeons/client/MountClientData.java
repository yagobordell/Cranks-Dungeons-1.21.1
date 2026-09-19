package com.cranks.dungeons.client;

import com.cranks.dungeons.network.MountDataSyncPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MountClientData {
    private static final Map<Integer, MountDataSyncPacket> DATA = new ConcurrentHashMap<>();
    private MountClientData() { }
    public static MountDataSyncPacket get(int entityId) { return DATA.get(entityId); }
    public static void update(MountDataSyncPacket packet) { DATA.put(packet.entityId(), packet); }
    public static void clear() { DATA.clear(); }
}

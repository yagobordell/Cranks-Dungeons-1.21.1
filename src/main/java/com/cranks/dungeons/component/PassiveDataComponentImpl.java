package com.cranks.dungeons.component;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashSet;
import java.util.Set;

public class PassiveDataComponentImpl implements PassiveDataComponent, AutoSyncedComponent {
    private final PlayerEntity player;
    private String playerClass = "";
    private int unspentPoints = 0;
    private final Set<String> unlockedNodes = new HashSet<>();
    private boolean checkedRetroactive = false;

    public PassiveDataComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void copyFrom(PassiveDataComponent other, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        this.playerClass = other.getPlayerClass();
        this.unspentPoints = other.getUnspentPoints();
        this.unlockedNodes.clear();
        this.unlockedNodes.addAll(other.getUnlockedNodes());
        System.out.println("[CranksDungeons] Copied Passive Data for " + player.getName().getString() + " from old instance.");
    }

    @Override
    public String getPlayerClass() {
        return playerClass;
    }

    @Override
    public void setPlayerClass(String playerClass) {
        this.playerClass = playerClass;
        ModComponents.PASSIVE_DATA.sync(this.player);
    }

    @Override
    public int getUnspentPoints() {
        return unspentPoints;
    }

    @Override
    public void setUnspentPoints(int points) {
        this.unspentPoints = points;
        ModComponents.PASSIVE_DATA.sync(this.player);
    }

    @Override
    public void addUnspentPoints(int points) {
        this.unspentPoints += points;
        ModComponents.PASSIVE_DATA.sync(this.player);
    }

    @Override
    public Set<String> getUnlockedNodes() {
        return unlockedNodes;
    }

    @Override
    public void unlockNode(String nodeId) {
        if (nodeId == null) return;
        if (unlockedNodes.add(nodeId.trim())) {
            ModComponents.PASSIVE_DATA.sync(this.player);
        }
    }

    @Override
    public boolean hasNode(String nodeId) {
        return nodeId != null && unlockedNodes.contains(nodeId.trim());
    }

    @Override
    public void checkRetroactivePoints(int currentLevel) {
        if (!checkedRetroactive) {
            checkedRetroactive = true;
            if (unlockedNodes.isEmpty() && unspentPoints == 0) {
                this.unspentPoints = currentLevel;
                ModComponents.PASSIVE_DATA.sync(this.player);
            }
        }
    }

    // STANDARD NBT METHODS (V3/V4 Compatibility)
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        internal_read(tag);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        internal_write(tag);
    }

    // LEGACY NBT METHODS (V2 Compatibility/Fallback)
    // Some versions of CCA or older API definitions use these. 
    // Providing both ensure persistence works in many environments.
    public void readFromNbt(NbtCompound tag) {
        internal_read(tag);
    }

    public void writeToNbt(NbtCompound tag) {
        internal_write(tag);
    }

    private void internal_read(NbtCompound tag) {
        this.playerClass = tag.getString("playerClass").orElse("");
        this.unspentPoints = tag.getInt("unspentPoints").orElse(0);
        this.checkedRetroactive = tag.getBoolean("checkedRetroactive").orElse(false);
        
        this.unlockedNodes.clear();
        NbtList list = tag.getList("unlockedNodes").orElse(new NbtList());
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i).orElse("").trim();
            if (!id.isEmpty()) {
                this.unlockedNodes.add(id);
            }
        }
        System.out.println("[CranksDungeons] Loaded Passive Data for " + player.getName().getString() + " (Class: " + playerClass + ")");
    }

    private void internal_write(NbtCompound tag) {
        tag.putString("playerClass", this.playerClass == null ? "" : this.playerClass);
        tag.putInt("unspentPoints", this.unspentPoints);
        tag.putBoolean("checkedRetroactive", this.checkedRetroactive);

        NbtList list = new NbtList();
        for (String node : this.unlockedNodes) {
            list.add(NbtString.of(node));
        }
        tag.put("unlockedNodes", list);
        System.out.println("[CranksDungeons] Saved Passive Data for " + player.getName().getString());
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeString(this.playerClass == null ? "" : this.playerClass);
        buf.writeVarInt(this.unspentPoints);
        buf.writeCollection(this.unlockedNodes, (b, s) -> b.writeString(s));
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.playerClass = buf.readString();
        this.unspentPoints = buf.readVarInt();
        this.unlockedNodes.clear();
        buf.readCollection(HashSet::new, (b) -> b.readString().trim()).forEach(this.unlockedNodes::add);
        System.out.println("[CranksDungeons] Synced Passive Data from Server");
    }

    // BRIDGE METHODS FOR CCA 7.3.1 (Injected via ASM in build.gradle)
    // These handle the bit-stream serialization required by newer versions of the API.
    public void cca_bridge_writeData(Object buf) {
        try {
            // We use reflection to handle any RegistryWrapper/Buf variant passed by production
            java.lang.reflect.Method writeNbt = buf.getClass().getMethod("writeNbt", NbtCompound.class);
            NbtCompound tag = new NbtCompound();
            this.internal_write(tag);
            writeNbt.invoke(buf, tag);
        } catch (Exception e) {
            // Fallback to simpler cast if reflection fails
            if (buf instanceof RegistryByteBuf rbb) {
                NbtCompound tag = new NbtCompound();
                this.internal_write(tag);
                rbb.writeNbt(tag);
            }
        }
    }

    public void cca_bridge_readData(Object buf) {
        try {
            java.lang.reflect.Method readNbt = buf.getClass().getMethod("readNbt");
            NbtCompound tag = (NbtCompound) readNbt.invoke(buf);
            if (tag != null) this.internal_read(tag);
        } catch (Exception e) {
            if (buf instanceof RegistryByteBuf rbb) {
                NbtCompound tag = rbb.readNbt();
                if (tag != null) this.internal_read(tag);
            }
        }
    }
}

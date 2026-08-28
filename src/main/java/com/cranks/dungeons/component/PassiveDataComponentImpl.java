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
    private final java.util.Map<String, String> nodeChoices = new java.util.HashMap<>();
    private boolean checkedRetroactive = false;
    private int batchDepth = 0;
    private boolean syncPending = false;

    public PassiveDataComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void copyFrom(PassiveDataComponent other, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        this.playerClass = other.getPlayerClass();
        this.unspentPoints = other.getUnspentPoints();
        this.unlockedNodes.clear();
        this.unlockedNodes.addAll(other.getUnlockedNodes());
        this.nodeChoices.clear();
        this.nodeChoices.putAll(other.getNodeChoices());
        this.checkedRetroactive = other instanceof PassiveDataComponentImpl impl && impl.checkedRetroactive;
        System.out.println("[CranksDungeons] Copied Passive Data for " + player.getName().getString() + " from old instance.");
    }

    @Override
    public String getPlayerClass() {
        return playerClass;
    }

    @Override
    public void setPlayerClass(String playerClass) {
        this.playerClass = playerClass;
        requestSync();
    }

    @Override
    public int getUnspentPoints() {
        return unspentPoints;
    }

    @Override
    public void setUnspentPoints(int points) {
        int newValue = Math.max(0, points);
        if (this.unspentPoints != newValue) {
            this.unspentPoints = newValue;
            requestSync();
        }
    }

    @Override
    public void addUnspentPoints(int points) {
        if (points > 0) {
            this.unspentPoints += points;
            if (!this.player.getEntityWorld().isClient()) {
                this.player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                // this.player.sendMessage(net.minecraft.text.Text.literal("You have " + this.unspentPoints + " unallocated passive tree points!").formatted(net.minecraft.util.Formatting.GOLD), false);
            }
            requestSync();
        }
    }

    @Override
    public Set<String> getUnlockedNodes() {
        return java.util.Collections.unmodifiableSet(unlockedNodes);
    }

    @Override
    public void unlockNode(String nodeId) {
        if (nodeId == null) return;
        if (unlockedNodes.add(nodeId.trim())) {
            requestSync();
        }
    }

    @Override
    public boolean hasNode(String nodeId) {
        return nodeId != null && unlockedNodes.contains(nodeId.trim());
    }

    @Override
    public java.util.Map<String, String> getNodeChoices() {
        return java.util.Collections.unmodifiableMap(nodeChoices);
    }

    @Override
    public void setNodeChoice(String nodeId, String choice) {
        if (nodeId != null && choice != null) {
            String id = nodeId.trim();
            String selected = choice.trim();
            boolean changed = selected.isEmpty()
                    ? nodeChoices.remove(id) != null
                    : !selected.equals(nodeChoices.put(id, selected));
            if (changed) requestSync();
        }
    }

    @Override
    public String getNodeChoice(String nodeId) {
        if (nodeId == null) return "";
        return nodeChoices.getOrDefault(nodeId.trim(), "");
    }

    @Override
    public void checkRetroactivePoints(int currentLevel) {
        if (!checkedRetroactive) {
            checkedRetroactive = true;
            if (unlockedNodes.isEmpty() && unspentPoints == 0) {
                this.unspentPoints = currentLevel;
                requestSync();
            }
        }
    }

    @Override
    public boolean hasCheckedRetroactive() {
        return checkedRetroactive;
    }

    @Override
    public void setCheckedRetroactive(boolean checked) {
        if (checkedRetroactive != checked) {
            checkedRetroactive = checked;
            requestSync();
        }
    }

    @Override
    public void beginBatch() {
        batchDepth++;
    }

    @Override
    public void endBatch() {
        if (batchDepth <= 0) throw new IllegalStateException("Passive component batch was not started");
        batchDepth--;
        if (batchDepth == 0 && syncPending) {
            syncPending = false;
            ModComponents.PASSIVE_DATA.sync(this.player);
        }
    }

    private void requestSync() {
        if (batchDepth > 0) {
            syncPending = true;
        } else {
            ModComponents.PASSIVE_DATA.sync(this.player);
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

    private void internal_read(NbtCompound tag) {
        this.playerClass = tag.getString("playerClass");
        this.unspentPoints = tag.getInt("unspentPoints");
        this.checkedRetroactive = tag.getBoolean("checkedRetroactive");
        
        this.unlockedNodes.clear();
        NbtList list = tag.getList("unlockedNodes", 8);
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i).trim();
            if (!id.isEmpty()) {
                this.unlockedNodes.add(id);
            }
        }
        
        this.nodeChoices.clear();
        if (tag.contains("nodeChoices")) {
            NbtCompound choicesTag = tag.getCompound("nodeChoices");
            if (choicesTag != null) {
                for (String key : choicesTag.getKeys()) {
                    this.nodeChoices.put(key, choicesTag.getString(key));
                }
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

        NbtCompound choicesTag = new NbtCompound();
        for (java.util.Map.Entry<String, String> entry : this.nodeChoices.entrySet()) {
            choicesTag.putString(entry.getKey(), entry.getValue());
        }
        tag.put("nodeChoices", choicesTag);


    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeString(this.playerClass == null ? "" : this.playerClass);
        buf.writeVarInt(this.unspentPoints);
        buf.writeCollection(this.unlockedNodes, (b, s) -> b.writeString(s));
        buf.writeMap(this.nodeChoices, (b, s) -> b.writeString(s), (b, s) -> b.writeString(s));
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        this.playerClass = buf.readString();
        this.unspentPoints = buf.readVarInt();
        this.unlockedNodes.clear();
        buf.readCollection(HashSet::new, (b) -> b.readString().trim()).forEach(this.unlockedNodes::add);
        this.nodeChoices.clear();
        buf.readMap(java.util.HashMap::new, (b) -> b.readString().trim(), (b) -> b.readString().trim()).forEach(this.nodeChoices::put);
        System.out.println("[CranksDungeons] Synced Passive Data from Server");
    }
}

package com.cranks.dungeons.component;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldTreeDataComponentImpl implements WorldTreeDataComponent, AutoSyncedComponent {
    private final PlayerEntity player;
    private int unspentPoints = 0;
    private final Set<String> unlockedNodes = new HashSet<>();
    private final Map<String, String> nodeChoices = new HashMap<>();
    private int gatheringXp = 0;
    private int gatheringLevel = 1;
    private int highestRewardedLevel = 1;
    private int clientRequired = 81;
    private int batchDepth = 0;
    private boolean syncPending = false;

    public WorldTreeDataComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void copyFrom(WorldTreeDataComponent other, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        this.unspentPoints = other.getUnspentPoints();
        this.unlockedNodes.clear();
        this.unlockedNodes.addAll(other.getUnlockedNodes());
        this.nodeChoices.clear();
        this.nodeChoices.putAll(other.getAllNodeChoices());
        this.gatheringXp = other.getGatheringXp();
        this.gatheringLevel = other.getGatheringLevel();
        this.highestRewardedLevel = other instanceof WorldTreeDataComponentImpl impl ? impl.highestRewardedLevel : this.gatheringLevel;
        System.out.println("[CranksDungeons] Copied World Tree Data for " + player.getName().getString() + " from old instance.");
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
                // this.player.sendMessage(net.minecraft.text.Text.literal("You have " + this.unspentPoints + " unallocated world tree points!").formatted(net.minecraft.util.Formatting.GOLD), false);
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
    public int getGatheringXp() {
        return gatheringXp;
    }

    @Override
    public int getGatheringLevel() {
        return gatheringLevel;
    }

    @Override
    public void setGatheringXp(int xp, int level) {
        this.gatheringXp = Math.max(0,xp);
        this.gatheringLevel = Math.max(1,level);
        this.highestRewardedLevel = Math.max(this.highestRewardedLevel,this.gatheringLevel);
        requestSync();
    }

    @Override
    public int getNextLevelXp() {
        return player.getWorld().isClient() ? clientRequired : com.cranks.dungeons.config.ProgressionConfig.RULES.required(false,gatheringLevel);
    }

    @Override
    public void addGatheringXp(int xp) {
        if (xp <= 0 || player.getWorld().isClient()) return;
        int maxLevel = com.cranks.dungeons.config.ProgressionConfig.RULES.worldCap;
        if (this.gatheringLevel >= maxLevel) {
            this.gatheringXp = 0;
            return;
        }

        this.gatheringXp = (int)Math.min(Integer.MAX_VALUE,(long)this.gatheringXp+xp);
        
        boolean leveledUp = false;
        int nextLevelXp = getNextLevelXp();
        
        while (this.gatheringXp >= nextLevelXp && this.gatheringLevel < maxLevel) {
            this.gatheringXp -= nextLevelXp;
            this.gatheringLevel++;
            leveledUp = true;
            if(this.gatheringLevel > highestRewardedLevel) { this.unspentPoints++; highestRewardedLevel=this.gatheringLevel; }
            
            if (this.gatheringLevel >= maxLevel) {
                this.gatheringXp = 0;
                break;
            }
            nextLevelXp = getNextLevelXp();
        }
        
        if (leveledUp) {
            if (!this.player.getEntityWorld().isClient()) {
                com.cranks.dungeons.entity.LevelUpVfxEntity.celebrate(player,true);
            }
        }
        requestSync();
    }

    @Override
    public String getNodeChoice(String nodeId) {
        return nodeChoices.get(nodeId);
    }

    @Override
    public Map<String, String> getAllNodeChoices() {
        return new HashMap<>(nodeChoices);
    }

    @Override
    public void setNodeChoice(String nodeId, String choice) {
        if (choice == null || choice.isEmpty()) {
            if (nodeChoices.remove(nodeId) != null) {
                requestSync();
            }
        } else {
            if (!choice.equals(nodeChoices.get(nodeId))) {
                nodeChoices.put(nodeId, choice);
                requestSync();
            }
        }
    }

    @Override
    public void beginBatch() {
        batchDepth++;
    }

    @Override
    public void endBatch() {
        if (batchDepth <= 0) throw new IllegalStateException("World tree component batch was not started");
        batchDepth--;
        if (batchDepth == 0 && syncPending) {
            syncPending = false;
            ModComponents.WORLD_TREE_DATA.sync(this.player);
        }
    }

    private void requestSync() {
        if (batchDepth > 0) {
            syncPending = true;
        } else {
            ModComponents.WORLD_TREE_DATA.sync(this.player);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        internal_read(tag);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        internal_write(tag);
    }

    private void internal_read(NbtCompound tag) {
        this.unspentPoints = tag.getInt("unspentPoints");
        this.gatheringXp = tag.getInt("gatheringXp");
        this.gatheringLevel = tag.getInt("gatheringLevel");
        if (this.gatheringLevel < 1) this.gatheringLevel = 1;
        highestRewardedLevel=Math.max(gatheringLevel,tag.getInt("highestRewardedLevel"));
        if(!tag.contains("progressionVersion")) {
            double oldRequired=200+75.0*gatheringLevel+.5*gatheringLevel*gatheringLevel;
            gatheringXp=(int)(Math.clamp(gatheringXp/oldRequired,0,1)*com.cranks.dungeons.config.ProgressionConfig.RULES.required(false,gatheringLevel));
        }
        
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
            for (String key : choicesTag.getKeys()) {
                this.nodeChoices.put(key, choicesTag.getString(key));
            }
        }
        System.out.println("[CranksDungeons] Loaded World Tree Data for " + player.getName().getString());
    }

    private void internal_write(NbtCompound tag) {
        tag.putInt("unspentPoints", this.unspentPoints);
        tag.putInt("gatheringXp", this.gatheringXp);
        tag.putInt("gatheringLevel", this.gatheringLevel);
        tag.putInt("highestRewardedLevel",highestRewardedLevel);
        tag.putInt("progressionVersion",1);

        NbtList list = new NbtList();
        for (String node : this.unlockedNodes) {
            list.add(NbtString.of(node));
        }
        tag.put("unlockedNodes", list);

        NbtCompound choicesTag = new NbtCompound();
        for (Map.Entry<String, String> entry : nodeChoices.entrySet()) {
            choicesTag.putString(entry.getKey(), entry.getValue());
        }
        tag.put("nodeChoices", choicesTag);


    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf, net.minecraft.server.network.ServerPlayerEntity recipient) {
        buf.writeInt(unspentPoints);
        buf.writeInt(gatheringXp);
        buf.writeInt(gatheringLevel);
        buf.writeInt(getNextLevelXp());
        buf.writeInt(unlockedNodes.size());
        for (String nodeId : unlockedNodes) {
            String choice = nodeChoices.get(nodeId);
            buf.writeString(nodeId);
            buf.writeBoolean(choice != null);
            if (choice != null) {
                buf.writeString(choice);
            }
        }
    }

    @Override
    public void applySyncPacket(net.minecraft.network.RegistryByteBuf buf) {
        this.unspentPoints = buf.readInt();
        this.gatheringXp = buf.readInt();
        this.gatheringLevel = buf.readInt();
        this.clientRequired = buf.readInt();
        int size = buf.readInt();
        this.unlockedNodes.clear();
        this.nodeChoices.clear();
        for (int i = 0; i < size; i++) {
            String nodeId = buf.readString();
            unlockedNodes.add(nodeId);
            if (buf.readBoolean()) {
                nodeChoices.put(nodeId, buf.readString());
            }
        }
        System.out.println("[CranksDungeons] Synced World Tree Data from Server");
    }
}

package com.cranks.dungeons.party;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class PartyPersistentState extends PersistentState {
    private static final String STATE_KEY = "cranks_dungeons_parties";
    private final List<Party> parties = new ArrayList<>();

    List<Party> getParties() {
        return List.copyOf(parties);
    }

    void replaceParties(Collection<Party> currentParties) {
        parties.clear();
        parties.addAll(currentParties);
        markDirty();
    }

    static PartyPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PartyPersistentState state = new PartyPersistentState();
        NbtList list = nbt.getList("parties", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            try {
                NbtCompound entry = list.getCompound(i);
                UUID leader = UUID.fromString(entry.getString("leader"));
                Set<UUID> members = new LinkedHashSet<>();
                members.add(leader);
                NbtList memberList = entry.getList("members", NbtElement.STRING_TYPE);
                for (int j = 0; j < memberList.size() && members.size() < Party.MAX_MEMBERS; j++) {
                    members.add(UUID.fromString(memberList.getString(j)));
                }
                state.parties.add(new Party(leader, members, entry.getLong("creationTime")));
            } catch (Exception e) {
                CranksDungeons.LOGGER.error("[Party] Skipping corrupt persisted party at index {}", i, e);
            }
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (Party party : parties) {
            NbtCompound entry = new NbtCompound();
            entry.putString("leader", party.getLeader().toString());
            entry.putLong("creationTime", party.getCreationTime());
            NbtList members = new NbtList();
            party.getMembers().forEach(member -> members.add(NbtString.of(member.toString())));
            entry.put("members", members);
            list.add(entry);
        }
        nbt.put("parties", list);
        return nbt;
    }

    static PartyPersistentState get(MinecraftServer server) {
        PersistentState.Type<PartyPersistentState> type = new PersistentState.Type<>(
                PartyPersistentState::new,
                PartyPersistentState::fromNbt,
                null
        );
        return server.getOverworld().getPersistentStateManager().getOrCreate(type, STATE_KEY);
    }
}

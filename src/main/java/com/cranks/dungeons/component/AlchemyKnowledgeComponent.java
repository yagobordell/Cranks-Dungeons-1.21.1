package com.cranks.dungeons.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashSet;
import java.util.Set;

/** Formulas belong to the experimenter, survive death, and are never shared by the server. */
public final class AlchemyKnowledgeComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private final Set<String> formulas = new HashSet<>();
    private final Set<String> failures = new HashSet<>();
    private int revision;
    private boolean loadedData;

    public AlchemyKnowledgeComponent(PlayerEntity player) { this.player = player; }
    public Set<String> knownFormulas() { return Set.copyOf(formulas); }
    public boolean knows(String formula) { return formulas.contains(formula); }
    public boolean tried(String combination) { return failures.contains(combination); }
    public int revision() { return revision; }
    public boolean hasLoadedData() { return loadedData; }

    public void learn(String formula, boolean successful) {
        if (!(successful ? formulas : failures).add(formula)) return;
        revision++;
        if (successful && player instanceof ServerPlayerEntity server) com.cranks.dungeons.progression.WorldDiscovery.formula(server,formula);
        ModComponents.ALCHEMY_KNOWLEDGE.sync(player);
    }

    @Override public boolean shouldSyncWith(ServerPlayerEntity viewer) { return viewer == player; }

    @Override public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        loadedData = true;
        read(tag, "formulas", formulas);
        read(tag, "failures", failures);
        revision++;
    }

    private static void read(NbtCompound tag, String key, Set<String> target) {
        target.clear();
        NbtList list = tag.getList(key, NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) target.add(list.getString(i));
    }

    @Override public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        write(tag, "formulas", formulas);
        write(tag, "failures", failures);
    }

    private static void write(NbtCompound tag, String key, Set<String> source) {
        NbtList list = new NbtList();
        source.stream().sorted().forEach(value -> list.add(NbtString.of(value)));
        tag.put(key, list);
    }
}

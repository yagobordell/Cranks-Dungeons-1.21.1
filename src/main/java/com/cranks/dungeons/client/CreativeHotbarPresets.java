package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.collection.DefaultedList;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Profile-local presets. Registry-bound item stacks are decoded afresh for each connection. */
public final class CreativeHotbarPresets {
    static final int COUNT = 9;
    private final MinecraftClient client;
    private final Path path;
    private final List<DefaultedList<ItemStack>> entries = new ArrayList<>();
    private final boolean[] saved = new boolean[COUNT];
    private boolean readable = true;
    private static int pendingShortcut;

    public static void clearPending() { pendingShortcut = 0; }

    public static void shortcut(MinecraftClient client, int preset, boolean load, boolean save) {
        if (client.player == null || !client.player.isCreative() || client.player.currentScreenHandler != client.player.playerScreenHandler
                || preset < 0 || preset >= COUNT || !load && !save) return;
        var storage = new CreativeHotbarPresets(client);
        if (load) {
            if (pendingShortcut != 0) return;
            if (!storage.has(preset)) { notify("This hotbar preset is empty"); return; }
            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) { notify("Put down the held item first"); return; }
            var expected = new ArrayList<ItemStack>();
            for (int i = 0; i < PlayerLoadout.HOTBAR_COUNT; i++) expected.add(client.player.getInventory().getStack(PlayerLoadout.getHotbarSlot(i)).copy());
            pendingShortcut = MaterialCraftingPanel.nextRequest();
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new com.cranks.dungeons.network.CreativeHotbarPayload(pendingShortcut, storage.get(preset), expected));
        } else {
            notify(storage.save(preset) ? "Hotbar preset " + (preset + 1) + " saved" : "Could not save hotbar preset");
        }
    }

    public static void receiveResult(com.cranks.dungeons.network.CraftMaterialsResultPayload result) {
        if (pendingShortcut == 0 || result.request() != pendingShortcut) return;
        pendingShortcut = 0;
        notify(result.result() > 0 ? "Hotbar preset loaded"
                : result.result() == com.cranks.dungeons.network.CraftMaterialsResultPayload.INVENTORY_CHANGED
                ? "Inventory changed; try loading again" : "Could not load hotbar preset");
    }

    private static void notify(String text) {
        NotificationHudOverlay.addNotification(net.minecraft.text.Text.literal(text), 50);
    }

    CreativeHotbarPresets(MinecraftClient client) {
        this.client = client;
        path = client.runDirectory.toPath().resolve("cranks-dungeons-hotbars.dat");
        for (int i = 0; i < COUNT; i++) entries.add(DefaultedList.ofSize(PlayerLoadout.HOTBAR_COUNT, ItemStack.EMPTY));
        if (Files.exists(path)) {
            try {
                var root = NbtIo.readCompressed(path, NbtSizeTracker.of(4 * 1024 * 1024));
                if (root.getInt("Format") != 1) throw new IOException("Unknown hotbar preset format");
                var list = root.getList("Presets", NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < Math.min(COUNT, list.size()); i++) {
                    var tag = list.getCompound(i);
                    saved[i] = tag.getBoolean("Saved");
                    Inventories.readNbt(tag, entries.get(i), client.world.getRegistryManager());
                }
            } catch (Exception error) {
                readable = false; // Keep a damaged/unknown file intact rather than overwriting it.
                CranksDungeons.LOGGER.warn("Could not read creative hotbar presets", error);
            }
        } else {
            // Existing vanilla presets keep their original nine slots; the new tenth slot starts empty.
            for (int i = 0; i < COUNT; i++) {
                var legacy = client.getCreativeHotbarStorage().getSavedHotbar(i).deserialize(client.world.getRegistryManager());
                for (int slot = 0; slot < Math.min(9, legacy.size()); slot++) {
                    entries.get(i).set(slot, legacy.get(slot).copy());
                    saved[i] |= !legacy.get(slot).isEmpty();
                }
            }
        }
    }

    boolean has(int preset) { return saved[preset]; }
    List<ItemStack> get(int preset) { return entries.get(preset).stream().map(ItemStack::copy).toList(); }

    boolean save(int preset) {
        if (!readable) return false;
        var replacement = DefaultedList.ofSize(PlayerLoadout.HOTBAR_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < replacement.size(); slot++) {
            replacement.set(slot, client.player.getInventory().getStack(PlayerLoadout.getHotbarSlot(slot)).copy());
        }
        var root = new NbtCompound();
        root.putInt("Format", 1);
        var list = new NbtList();
        for (int i = 0; i < COUNT; i++) {
            var tag = Inventories.writeNbt(new NbtCompound(), i == preset ? replacement : entries.get(i), client.world.getRegistryManager());
            tag.putBoolean("Saved", i == preset || saved[i]);
            list.add(tag);
        }
        root.put("Presets", list);
        var temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            NbtIo.writeCompressed(root, temporary);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            entries.set(preset, replacement);
            saved[preset] = true;
            return true;
        } catch (Exception error) {
            CranksDungeons.LOGGER.warn("Could not save creative hotbar preset", error);
            return false;
        }
    }
}

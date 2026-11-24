package com.cranks.dungeons.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.util.collection.DefaultedList;

public final class PlayerLoadout {
    public static final int EXTRA_HOTBAR = 41;
    public static final int HOTBAR_COUNT = 10;
    public static final int SPARE_ARMOR = 42;
    public static final int BAG_COUNT = 5;
    public static final int BAG_WIDTH = 12;
    public static final int BAG_STORAGE = 46;
    public static final int EQUIPPED_BAGS = BAG_STORAGE + BAG_COUNT * BAG_WIDTH;
    public static final int END = EQUIPPED_BAGS + BAG_COUNT;
    public static final int FIRST_HANDLER_SLOT = 46;
    public static final int SWAP_BUTTON = 0;

    public static final EquipmentSlot[] ARMOR_ORDER = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(END - EXTRA_HOTBAR, ItemStack.EMPTY);
    private final PlayerInventory inventory;
    private boolean secondSetActive;

    public PlayerLoadout(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    public static PlayerLoadout of(PlayerInventory inventory) {
        return ((PlayerLoadoutAccess) inventory).cranks$getLoadout();
    }

    public static int getHotbarSlot(int index) {
        return index == HOTBAR_COUNT - 1 ? EXTRA_HOTBAR : index;
    }

    public static int getHotbarIndex(int slot) {
        return slot == EXTRA_HOTBAR ? HOTBAR_COUNT - 1 : slot;
    }

    public static int handlerSlot(int slot) {
        return FIRST_HANDLER_SLOT + slot - EXTRA_HOTBAR;
    }

    public DefaultedList<ItemStack> stacks() {
        return stacks;
    }

    public static int capacity(ItemStack stack) {
        if (!stack.isOf(Items.BUNDLE)) return 0;
        return stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT).isEmpty() ? 6 : 0;
    }

    public int capacity(int row) {
        return capacity(inventory.getStack(EQUIPPED_BAGS + row));
    }

    public boolean hasBagContents(int row) {
        for (int column = 0; column < BAG_WIDTH; column++) {
            if (!inventory.getStack(BAG_STORAGE + row * BAG_WIDTH + column).isEmpty()) return true;
        }
        return false;
    }

    public void dropBagContents() {
        for (int slot = BAG_STORAGE; slot < EQUIPPED_BAGS; slot++) {
            ItemStack stack = inventory.removeStack(slot);
            if (!stack.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(
                    stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                inventory.player.dropItem(stack, true, false);
            }
        }
        inventory.markDirty();
    }

    public boolean secondSetActive() {
        return secondSetActive;
    }

    public boolean swapArmor(PlayerEntity player) {
        if (!player.isAlive() || player.isSpectator()) return false;
        if (!player.isCreative()) {
            for (EquipmentSlot slot : ARMOR_ORDER) {
                if (EnchantmentHelper.hasAnyEnchantmentsWith(player.getEquippedStack(slot), EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                    return false;
                }
            }
        }

        for (int i = 0; i < ARMOR_ORDER.length; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];
            ItemStack previous = player.getEquippedStack(slot);
            ItemStack next = inventory.getStack(SPARE_ARMOR + i);
            player.equipStack(slot, next);
            inventory.setStack(SPARE_ARMOR + i, previous);
        }
        secondSetActive = !secondSetActive;
        inventory.markDirty();
        return true;
    }

    public void setSecondSetActive(boolean value) {
        secondSetActive = value;
    }

    public boolean insert(ItemStack stack) {
        int before = stack.getCount();
        insertInOrder(stack, true);
        insertInOrder(stack, false);
        if (stack.getCount() >= before) return false;
        inventory.markDirty();
        return true;
    }

    private void insertInOrder(ItemStack stack, boolean merge) {
        fill(stack, 0, HOTBAR_COUNT - 1, merge);
        fill(stack, EXTRA_HOTBAR, EXTRA_HOTBAR + 1, merge);
        fill(stack, HOTBAR_COUNT - 1, PlayerInventory.MAIN_SIZE, merge);
        for (int row = 0; row < BAG_COUNT && !stack.isEmpty(); row++) {
            int start = BAG_STORAGE + row * BAG_WIDTH;
            fill(stack, start, start + capacity(row), merge);
        }
    }

    private void fill(ItemStack stack, int start, int end, boolean merge) {
        for (int slot = start; slot < end && !stack.isEmpty(); slot++) {
            ItemStack existing = inventory.getStack(slot);
            if (merge) {
                if (existing.isEmpty() || !ItemStack.areItemsAndComponentsEqual(existing, stack)) continue;
                int moved = Math.min(stack.getCount(), inventory.getMaxCount(existing) - existing.getCount());
                if (moved <= 0) continue;
                existing.increment(moved);
                existing.setBobbingAnimationTime(5);
                stack.decrement(moved);
            } else if (existing.isEmpty()) {
                ItemStack moved = stack.split(inventory.getMaxCount(stack));
                moved.setBobbingAnimationTime(5);
                inventory.setStack(slot, moved);
            }
        }
    }

    public NbtCompound write(RegistryWrapper.WrapperLookup registries) {
        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, stacks, registries);
        tag.putBoolean("SecondSetActive", secondSetActive);
        return tag;
    }

    public void read(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        stacks.clear();
        Inventories.readNbt(tag, stacks, registries);
        secondSetActive = tag.getBoolean("SecondSetActive");
    }
}

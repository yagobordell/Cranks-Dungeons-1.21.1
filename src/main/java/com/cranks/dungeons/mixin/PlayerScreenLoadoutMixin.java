package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
abstract class PlayerScreenLoadoutMixin extends ScreenHandler {
    protected PlayerScreenLoadoutMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addLoadoutSlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        PlayerLoadout loadout = PlayerLoadout.of(inventory);
        for (int i = PlayerLoadout.EXTRA_HOTBAR; i < PlayerLoadout.END; i++) {
            final int index = i;
            Slot matchingArmorSlot = i >= PlayerLoadout.SPARE_ARMOR && i < PlayerLoadout.BAG_STORAGE
                    ? slots.get(5 + i - PlayerLoadout.SPARE_ARMOR) : null;
            addSlot(new Slot(inventory, i, 0, 0) {
                @Override
                public Pair<Identifier, Identifier> getBackgroundSprite() {
                    return matchingArmorSlot == null ? super.getBackgroundSprite() : matchingArmorSlot.getBackgroundSprite();
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                    if (index >= PlayerLoadout.EQUIPPED_BAGS) {
                        return PlayerLoadout.capacity(stack) > 0;
                    }
                    if (index >= PlayerLoadout.BAG_STORAGE) {
                        int cell = index - PlayerLoadout.BAG_STORAGE;
                        return cell % PlayerLoadout.BAG_WIDTH < loadout.capacity(cell / PlayerLoadout.BAG_WIDTH);
                    }
                    if (index >= PlayerLoadout.SPARE_ARMOR) {
                        return owner.getPreferredEquipmentSlot(stack) == PlayerLoadout.ARMOR_ORDER[index - PlayerLoadout.SPARE_ARMOR];
                    }
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return index < PlayerLoadout.BAG_STORAGE || index >= PlayerLoadout.EQUIPPED_BAGS
                            || hasStack() || canInsert(ItemStack.EMPTY);
                }

                @Override
                public boolean canTakeItems(PlayerEntity player) {
                    return index < PlayerLoadout.EQUIPPED_BAGS
                            || !loadout.hasBagContents(index - PlayerLoadout.EQUIPPED_BAGS);
                }

                @Override
                public int getMaxItemCount() {
                    return index >= PlayerLoadout.EQUIPPED_BAGS
                            || index >= PlayerLoadout.SPARE_ARMOR && index < PlayerLoadout.BAG_STORAGE
                            ? 1 : super.getMaxItemCount();
                }
            });
        }
        addProperty(new Property() {
            @Override
            public int get() {
                return loadout.secondSetActive() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                loadout.setSecondSetActive(value == 1);
            }
        });
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int button) {
        return button == PlayerLoadout.SWAP_BUTTON && PlayerLoadout.of(player.getInventory()).swapArmor(player);
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void moveBetweenLoadoutSlots(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemStack.EMPTY);
        if (index < 5 || index >= slots.size()) return;
        Slot source = slots.get(index);
        if (!source.hasStack() || !source.canTakeItems(player)) return;
        ItemStack stack = source.getStack();
        ItemStack original = stack.copy();
        int bagStart = PlayerLoadout.handlerSlot(PlayerLoadout.EQUIPPED_BAGS);
        int cargoStart = PlayerLoadout.handlerSlot(PlayerLoadout.BAG_STORAGE);
        int tenth = PlayerLoadout.FIRST_HANDLER_SLOT;
        if (index < bagStart && PlayerLoadout.capacity(stack) > 0) {
            insertItem(stack, bagStart, slots.size(), false);
        }
        if (index >= 9 && index < 45 || index == tenth || index >= cargoStart && index < bagStart) {
            for (int armor = 5; armor < 9 && !stack.isEmpty(); armor++) {
                Slot equip = slots.get(armor);
                if (!equip.hasStack() && equip.canInsert(stack)) insertItem(stack, armor, armor + 1, false);
            }
        }
        if (index < 9 || index >= 36) insertItem(stack, 9, 36, false);
        if (index < cargoStart || index >= bagStart) insertItem(stack, cargoStart, bagStart, false);
        if (index < 36 || index >= 45) insertItem(stack, 36, 45, false);
        if (index != tenth) insertItem(stack, tenth, tenth + 1, false);

        if (stack.getCount() == original.getCount()) return;
        if (stack.isEmpty()) source.setStack(ItemStack.EMPTY);
        else source.markDirty();
        source.onTakeItem(player, stack);
        cir.setReturnValue(original);
    }
}

package com.cranks.dungeons.screen;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.inventory.StorageSorting;
import com.cranks.dungeons.mixin.ShulkerInventoryAccessor;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import java.util.List;

/** Full player loadout backed by the original container inventory and lifecycle. */
public final class LoadoutStorageHandler extends LoadoutStationHandler {
    public static final int STORAGE_START=PLAYER_SLOT_END;
    private final Inventory storage;
    private final ScreenHandler nativeHandler;
    private final int rows;
    private final int columns;
    private final Property icon;
    public LoadoutStorageHandler(int id,PlayerInventory player,int rows,boolean shulker){this(id,player,9,rows,shulker);}
    public LoadoutStorageHandler(int id,PlayerInventory player,int columns,int rows,boolean shulker){this(id,player,new SimpleInventory(rows*columns),null,columns,rows,shulker,shulker?Items.SHULKER_BOX.getDefaultStack():columns==5?Items.HOPPER.getDefaultStack():columns==3?Items.DISPENSER.getDefaultStack():Items.CHEST.getDefaultStack());}
    private LoadoutStorageHandler(int id,PlayerInventory player,Inventory storage,ScreenHandler nativeHandler,int columns,int rows,boolean shulker,ItemStack iconStack){
        super(shulker?ModScreenHandlers.LOADOUT_SHULKER:columns==5?ModScreenHandlers.LOADOUT_HOPPER:columns==3?ModScreenHandlers.LOADOUT_DISPENSER:rows==6?ModScreenHandlers.LOADOUT_STORAGE_6:ModScreenHandlers.LOADOUT_STORAGE_3,id,player);
        checkSize(storage,rows*columns);this.storage=storage;this.nativeHandler=nativeHandler;this.columns=columns;this.rows=rows;
        if(player.player instanceof net.minecraft.server.network.ServerPlayerEntity server)com.cranks.dungeons.economy.EconomyRewards.chest(storage,server);
        // The native constructor has already opened the inventory exactly once.
        for(int i=0;i<storage.size();i++)addSlot(nativeHandler!=null?nativeHandler.getSlot(i):shulker?new ShulkerBoxSlot(storage,i,0,0):new Slot(storage,i,0,0));
        icon=addProperty(new Property(){private int value=Registries.ITEM.getRawId(iconStack.getItem());@Override public int get(){return value;}@Override public void set(int next){value=next;}});
    }
    public static ScreenHandler wrap(ScreenHandler original,PlayerInventory player){
        Inventory storage;int rows;int columns=9;boolean shulker=false;
        if(original instanceof GenericContainerScreenHandler container){storage=container.getInventory();rows=container.getRows();}
        else if(original instanceof ShulkerBoxScreenHandler){storage=((ShulkerInventoryAccessor)original).cranksdungeons$inventory();rows=3;shulker=true;}
        else if(original instanceof HopperScreenHandler){storage=original.getSlot(0).inventory;rows=1;columns=5;}
        else if(original instanceof Generic3x3ContainerScreenHandler){storage=original.getSlot(0).inventory;rows=3;columns=3;}
        else return original;
        if(columns==9&&rows!=3&&rows!=6)return original;
        if(!(storage instanceof ChestBlockEntity||storage instanceof BarrelBlockEntity||storage instanceof ShulkerBoxBlockEntity||storage instanceof DoubleInventory||storage instanceof EnderChestInventory||storage instanceof HopperBlockEntity||storage instanceof DispenserBlockEntity||storage instanceof ChestMinecartEntity||storage instanceof HopperMinecartEntity||storage instanceof ChestBoatEntity))return original;
        ItemStack icon=storage instanceof EnderChestInventory?Items.ENDER_CHEST.getDefaultStack():storage instanceof BlockEntity block?block.getCachedState().getBlock().asItem().getDefaultStack():storage instanceof HopperMinecartEntity?Items.HOPPER_MINECART.getDefaultStack():storage instanceof ChestMinecartEntity?Items.CHEST_MINECART.getDefaultStack():storage instanceof ChestBoatEntity boat?boat.asItem().getDefaultStack():Items.CHEST.getDefaultStack();
        return new LoadoutStorageHandler(original.syncId,player,storage,original,columns,rows,shulker,icon);
    }
    public int rows(){return rows;}
    public int columns(){return columns;}
    public int storageSize(){return storage.size();}
    public ItemStack icon(){return Registries.ITEM.get(icon.get()).getDefaultStack();}
    public ItemStack stack(int index){return storage.getStack(index);}
    public List<ItemStack> snapshot(){return java.util.stream.IntStream.range(0,storageSize()).mapToObj(i->stack(i).copy()).toList();}
    public boolean matches(List<ItemStack> expected){if(expected.size()!=storageSize())return false;for(int i=0;i<storageSize();i++)if(!ItemStack.areEqual(expected.get(i),stack(i)))return false;return true;}
    public boolean views(BlockEntity block){
        if(block instanceof Inventory inventory&&(storage==inventory||storage instanceof DoubleInventory pair&&pair.isPart(inventory)))return true;
        return block instanceof EnderChestBlockEntity ender&&storage instanceof EnderChestInventory inventory&&inventory.isActiveBlockEntity(ender);
    }
    @Override public boolean canUse(PlayerEntity player){return nativeHandler!=null?nativeHandler.canUse(player):storage.canPlayerUse(player);}
    @Override public void onClosed(PlayerEntity player){super.onClosed(player);if(nativeHandler!=null)nativeHandler.onClosed(player);}
    public int sort(PlayerEntity player,List<ItemStack> expected){
        if(player.currentScreenHandler!=this||!player.isAlive()||player.isSpectator()||!canUse(player))return CraftMaterialsResultPayload.UNAVAILABLE;
        if(!getCursorStack().isEmpty())return CraftMaterialsResultPayload.LOCKED;
        if(!matches(expected))return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        var sorted=StorageSorting.plan(expected);
        if(sorted==null)return CraftMaterialsResultPayload.UNAVAILABLE;
        for(int i=0;i<storageSize();i++){var slot=getSlot(STORAGE_START+i);var stack=sorted.get(i);if(!stack.isEmpty()&&(!slot.canInsert(stack)||stack.getCount()>slot.getMaxItemCount(stack)))return CraftMaterialsResultPayload.UNAVAILABLE;}
        if(!matches(expected))return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if(!matches(sorted)){for(int i=0;i<storageSize();i++)storage.setStack(i,sorted.get(i));storage.markDirty();}
        sendContentUpdates();return 1;
    }
    @Override public ItemStack quickMove(PlayerEntity player,int index){
        if(index<5||index>=slots.size()||!canUse(player))return ItemStack.EMPTY;
        var slot=getSlot(index);if(!slot.hasStack()||!slot.canTakeItems(player))return ItemStack.EMPTY;
        var stack=slot.getStack();var before=stack.copy();
        if(index>=STORAGE_START){if(!PlayerLoadout.of(player.getInventory()).insert(stack))return ItemStack.EMPTY;}
        else if(!insertItem(stack,STORAGE_START,slots.size(),false))return ItemStack.EMPTY;
        if(stack.getCount()==before.getCount())return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setStack(ItemStack.EMPTY);else slot.markDirty();
        slot.onTakeItem(player,stack);return before;
    }
}

package com.cranks.dungeons.economy;
import com.cranks.dungeons.inventory.PlayerLoadout;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import java.util.List;

/** Simulate both complete post-trade inventories before changing either player's items. */
public final class TradeInventory {
    public static boolean sourceSlot(PlayerInventory inventory,int slot){
        if(slot>=0&&slot<36||slot==PlayerLoadout.EXTRA_HOTBAR)return true;
        if(slot<PlayerLoadout.BAG_STORAGE||slot>=PlayerLoadout.EQUIPPED_BAGS)return false;
        int row=(slot-PlayerLoadout.BAG_STORAGE)/PlayerLoadout.BAG_WIDTH,column=(slot-PlayerLoadout.BAG_STORAGE)%PlayerLoadout.BAG_WIDTH;
        return column<PlayerLoadout.of(inventory).capacity(row);
    }
    public static DefaultedList<ItemStack> snapshot(PlayerInventory inventory){var copy=DefaultedList.ofSize(PlayerLoadout.END,ItemStack.EMPTY);for(int i=0;i<copy.size();i++)copy.set(i,inventory.getStack(i).copy());return copy;}
    public static boolean insert(DefaultedList<ItemStack> inventory,PlayerInventory owner,List<ItemStack> received){
        var slots=new java.util.ArrayList<Integer>();for(int i=0;i<9;i++)slots.add(i);slots.add(PlayerLoadout.EXTRA_HOTBAR);for(int i=9;i<36;i++)slots.add(i);
        for(int row=0;row<PlayerLoadout.BAG_COUNT;row++)for(int col=0;col<PlayerLoadout.of(owner).capacity(row);col++)slots.add(PlayerLoadout.BAG_STORAGE+row*PlayerLoadout.BAG_WIDTH+col);
        for(var source:received){
            var stack=source.copy();
            for(boolean merge:new boolean[]{true,false})for(int slot:slots){
                if(stack.isEmpty())break;var existing=inventory.get(slot);
                if(merge){if(existing.isEmpty()||!ItemStack.areItemsAndComponentsEqual(existing,stack))continue;int moved=Math.min(stack.getCount(),owner.getMaxCount(existing)-existing.getCount());if(moved>0){existing.increment(moved);stack.decrement(moved);}}
                else if(existing.isEmpty())inventory.set(slot,stack.split(owner.getMaxCount(stack)));
            }
            if(!stack.isEmpty())return false;
        }
        return true;
    }
}

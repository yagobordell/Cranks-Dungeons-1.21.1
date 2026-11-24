package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.GatheringLoot;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.*;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class WorldTreeMobLootMixin {
    @Unique private static final ThreadLocal<Integer> cranks$yieldDepth=ThreadLocal.withInitial(()->0);
    @WrapMethod(method="generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V")
    private void yield(LootContext context,Consumer<ItemStack> output,Operation<Void> original){
        int depth=cranks$yieldDepth.get();cranks$yieldDepth.set(depth+1);
        try{original.call(context,depth==0?GatheringLoot.output(context,output):output);}
        finally{if(depth==0)cranks$yieldDepth.remove();else cranks$yieldDepth.set(depth);}
    }
}

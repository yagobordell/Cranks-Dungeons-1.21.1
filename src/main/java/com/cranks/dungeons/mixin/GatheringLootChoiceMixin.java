package com.cranks.dungeons.mixin;

import com.cranks.dungeons.gathering.*;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import java.util.function.Consumer;

/** Change weighted selection before loot is chosen; never duplicate ordinary loot as a rare-drop bonus. */
@Mixin(LeafEntry.class)
public abstract class GatheringLootChoiceMixin {
    @Shadow @Final protected int quality;
    @ModifyVariable(method="expand",at=@At("HEAD"),argsOnly=true)
    private Consumer<LootChoice> weigh(Consumer<LootChoice> output,LootContext context,Consumer<LootChoice> ignored){
        boolean rare=(Object)this instanceof GatheringItemEntryAccessor item&&item.cranks$lootItem().isIn(GatheringTags.RARE_LOOT);
        if(quality<=0&&!rare)return output;
        double bonus=GatheringLoot.findBonus(context,rare,quality);
        if(bonus==0)return output;
        return choice->output.accept(GatheringLoot.weighted(choice,bonus,context));
    }
}

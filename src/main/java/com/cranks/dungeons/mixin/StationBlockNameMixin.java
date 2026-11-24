package com.cranks.dungeons.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
abstract class StationBlockNameMixin {
    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    private void useStationName(CallbackInfoReturnable<String> result) {
        if ((Object) this == Blocks.LOOM) result.setReturnValue("block.cranks-dungeons.tailoring_bench");
        else if ((Object) this == Blocks.SMOKER) result.setReturnValue("block.cranks-dungeons.cookstove");
        else if ((Object) this == Blocks.BLAST_FURNACE) result.setReturnValue("block.cranks-dungeons.foundry");
        else if ((Object) this == Blocks.STONECUTTER) result.setReturnValue("block.cranks-dungeons.cutting_bench");
        else if ((Object) this == Blocks.SMITHING_TABLE) result.setReturnValue("block.cranks-dungeons.upgrade_bench");
    }
}

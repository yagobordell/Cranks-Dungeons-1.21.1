package com.cranks.dungeons.mixin;

import com.cranks.dungeons.screen.LoadoutFurnaceHandler;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FurnaceBlockEntity.class, BlastFurnaceBlockEntity.class, SmokerBlockEntity.class})
abstract class FurnaceLoadoutMixin extends AbstractFurnaceBlockEntity {
    protected FurnaceLoadoutMixin(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(type, pos, state, recipeType);
    }

    @Inject(method = "createScreenHandler", at = @At("HEAD"), cancellable = true)
    private void openFurnacePanel(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> result) {
        LoadoutFurnaceHandler.Station station = switch ((Object) this) {
            case BlastFurnaceBlockEntity ignored -> LoadoutFurnaceHandler.Station.BLAST_FURNACE;
            case SmokerBlockEntity ignored -> LoadoutFurnaceHandler.Station.SMOKER;
            default -> LoadoutFurnaceHandler.Station.FURNACE;
        };
        result.setReturnValue(new LoadoutFurnaceHandler(syncId, playerInventory, this, propertyDelegate, station));
    }

    @Inject(method = "getContainerName", at = @At("HEAD"), cancellable = true)
    private void useStationName(CallbackInfoReturnable<Text> result) {
        if ((Object) this instanceof SmokerBlockEntity || (Object) this instanceof BlastFurnaceBlockEntity) {
            result.setReturnValue(getCachedState().getBlock().getName());
        }
    }
}

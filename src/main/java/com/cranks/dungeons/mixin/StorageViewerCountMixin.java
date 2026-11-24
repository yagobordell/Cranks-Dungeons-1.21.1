package com.cranks.dungeons.mixin;
import com.cranks.dungeons.screen.LoadoutStorageHandler;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.*;
@Mixin(ViewerCountManager.class)
public abstract class StorageViewerCountMixin {
    @Shadow private double maxBlockInteractionRange;
    @Inject(method="getViewingPlayers",at=@At("RETURN"),cancellable=true)
    private void includeLoadoutViewers(World world,BlockPos pos,CallbackInfoReturnable<List<PlayerEntity>> ci){
        var block=world.getBlockEntity(pos);if(block==null)return;
        var viewers=new LinkedHashSet<>(ci.getReturnValue());var bounds=new Box(pos).expand(maxBlockInteractionRange+4.0);
        for(var player:world.getPlayers())if(!player.isSpectator()&&bounds.intersects(player.getBoundingBox())&&player.currentScreenHandler instanceof LoadoutStorageHandler storage&&storage.views(block))viewers.add(player);
        if(viewers.size()!=ci.getReturnValue().size())ci.setReturnValue(new ArrayList<>(viewers));
    }
}

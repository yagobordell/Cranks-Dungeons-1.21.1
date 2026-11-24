package com.cranks.dungeons.mixin;
import com.cranks.dungeons.economy.CoinContainer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;
@Mixin(BlockEntity.class)
abstract class CoinBlockMetadataMixin implements CoinContainer {
    @Unique private UUID cranks$coinId;
    @Unique private String cranks$coinSource="";
    @Override public UUID cranks$coinId(){return cranks$coinId;}
    @Override public String cranks$coinSource(){return cranks$coinSource;}
    @Override public void cranks$markCoins(RegistryKey<LootTable> table){
        if(table!=null&&table.getValue().getPath().startsWith("chests/")&&cranks$coinId==null){cranks$coinId=UUID.randomUUID();cranks$coinSource=table.getValue().toString();((BlockEntity)(Object)this).markDirty();}
    }
    @Inject(method="writeNbt",at=@At("TAIL"))
    private void writeCoins(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){
        if(cranks$coinId!=null){nbt.putUuid("CranksCoinChest",cranks$coinId);nbt.putString("CranksCoinSource",cranks$coinSource);}
    }
    @Inject(method="readNbt",at=@At("TAIL"))
    private void readCoins(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){cranks$coinId=nbt.containsUuid("CranksCoinChest")?nbt.getUuid("CranksCoinChest"):null;cranks$coinSource=nbt.getString("CranksCoinSource");}
}

package com.cranks.dungeons.mixin;
import com.cranks.dungeons.economy.CoinContainer;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;
@Mixin(StorageMinecartEntity.class)
abstract class CoinCartMetadataMixin implements CoinContainer {
    @Unique private UUID cranks$coinId;
    @Unique private String cranks$coinSource="";
    @Override public UUID cranks$coinId(){return cranks$coinId;}
    @Override public String cranks$coinSource(){return cranks$coinSource;}
    @Override public void cranks$markCoins(RegistryKey<LootTable> table){if(table!=null&&table.getValue().getPath().startsWith("chests/")&&cranks$coinId==null){cranks$coinId=UUID.randomUUID();cranks$coinSource=table.getValue().toString();}}
    @Inject(method="setLootTable(Lnet/minecraft/registry/RegistryKey;)V",at=@At("HEAD"))
    private void markTable(RegistryKey<LootTable> table,CallbackInfo ci){cranks$markCoins(table);}
    @Inject(method="setLootTable(Lnet/minecraft/registry/RegistryKey;J)V",at=@At("HEAD"))
    private void markSeededTable(RegistryKey<LootTable> table,long seed,CallbackInfo ci){cranks$markCoins(table);}
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void writeCoins(NbtCompound nbt,CallbackInfo ci){if(cranks$coinId!=null){nbt.putUuid("CranksCoinChest",cranks$coinId);nbt.putString("CranksCoinSource",cranks$coinSource);}}
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void readCoins(NbtCompound nbt,CallbackInfo ci){
        cranks$coinId=nbt.containsUuid("CranksCoinChest")?nbt.getUuid("CranksCoinChest"):null;cranks$coinSource=nbt.getString("CranksCoinSource");
        cranks$markCoins(((StorageMinecartEntity)(Object)this).getLootTable());
    }
}

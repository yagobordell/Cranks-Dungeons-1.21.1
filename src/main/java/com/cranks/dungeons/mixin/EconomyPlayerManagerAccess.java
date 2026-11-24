package com.cranks.dungeons.mixin;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(PlayerManager.class)
public interface EconomyPlayerManagerAccess {
    @Invoker("savePlayerData") void cranks$savePlayer(ServerPlayerEntity player);
}

package com.cranks.dungeons.economy;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import java.util.UUID;
public interface CoinContainer {
    UUID cranks$coinId();
    String cranks$coinSource();
    void cranks$markCoins(RegistryKey<LootTable> table);
}

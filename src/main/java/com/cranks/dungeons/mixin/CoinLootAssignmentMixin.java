package com.cranks.dungeons.mixin;
import com.cranks.dungeons.economy.CoinContainer;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LootableContainerBlockEntity.class)
abstract class CoinLootAssignmentMixin {
    @Inject(method="setLootTable",at=@At("HEAD"))
    private void markChest(RegistryKey<LootTable> table,CallbackInfo ci){((CoinContainer)this).cranks$markCoins(table);}
}

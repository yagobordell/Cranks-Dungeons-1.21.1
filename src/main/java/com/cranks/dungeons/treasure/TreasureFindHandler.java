package com.cranks.dungeons.treasure;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class TreasureFindHandler {

    public static boolean tryFindTreasure(ServerPlayerEntity player,
                                          ServerWorld world,
                                          BlockPos pos,
                                          BlockState state,
                                          ItemStack tool) {

        // Debug: confirm method is being hit
        System.out.println("[TreasureFind] tryFindTreasure called");

        if (!(tool.getItem() instanceof ShovelItem)) {
            System.out.println("[TreasureFind] Tool is not a shovel, skipping");
            return false;
        }

        Block block = state.getBlock();
        if (!TreasureFindConfig.isValidBlock(block)) {
            System.out.println("[TreasureFind] Block not valid for treasure: " + block);
            return false;
        }

        // Get attribute instance safely
        var attrInstance = player.getAttributeInstance(ModAttributes.TREASURE_FIND);
        if (attrInstance == null) {
            System.out.println("[TreasureFind] Player has no TREASURE_FIND attribute instance");
            return false;
        }

        double chance = attrInstance.getValue();
        System.out.println("[TreasureFind] Chance: " + chance);

        if (chance <= 0.0) {
            System.out.println("[TreasureFind] Chance <= 0, skipping");
            return false;
        }

        double roll = world.getRandom().nextDouble();
        System.out.println("[TreasureFind] Roll: " + roll);

        if (roll >= chance) {
            System.out.println("[TreasureFind] Roll failed");
            return false;
        }

        Identifier tableId = TreasureFindConfig.getLootTableForBlock(block);
        System.out.println("[TreasureFind] Using loot table: " + tableId);

        RegistryKey<LootTable> key = RegistryKey.of(RegistryKeys.LOOT_TABLE, tableId);
        LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(key);

        if (lootTable == LootTable.EMPTY) {
            System.out.println("[TreasureFind] Loot table is EMPTY (not found): " + tableId);
            return false;
        }

        LootWorldContext context = new LootWorldContext.Builder(world)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .add(LootContextParameters.TOOL, tool)
                .addOptional(LootContextParameters.THIS_ENTITY, player)
                .addOptional(LootContextParameters.BLOCK_STATE, state)
                .build(LootContextTypes.BLOCK);

        List<ItemStack> loot = lootTable.generateLoot(context);

        System.out.println("[TreasureFind] Generated " + loot.size() + " stacks");

        if (loot.isEmpty()) {
            return false;
        }

        for (ItemStack drop : loot) {
            if (drop.isEmpty()) continue;

            double ox = world.getRandom().nextDouble() * 0.5 + 0.25;
            double oy = world.getRandom().nextDouble() * 0.5 + 0.25;
            double oz = world.getRandom().nextDouble() * 0.5 + 0.25;

            ItemEntity entity = new ItemEntity(
                    world,
                    pos.getX() + ox,
                    pos.getY() + oy,
                    pos.getZ() + oz,
                    drop.copy()
            );
            entity.setPickupDelay(10);
            world.spawnEntity(entity);

            System.out.println("[TreasureFind] Dropped " + drop.getCount() + "x " + drop.getItem());
        }

        return true;
    }
}
package com.cranks.dungeons.mixin;

import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentTableMixin extends ScreenHandler {

    @Shadow @Final public int[] enchantmentPower;
    @Shadow @Final public int[] enchantmentId;
    @Shadow @Final public int[] enchantmentLevel;
    @Shadow @Final private ScreenHandlerContext context;
    @Shadow @Final private Property seed;
    @Shadow @Final private Inventory inventory;

    @Unique
    private final EnchantmentLevelEntry[] dungeons$offers = new EnchantmentLevelEntry[3];

    protected EnchantmentTableMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Unique
    private int dungeons$countBookshelves(World world, BlockPos tablePos) {
        int count = 0;
        for (BlockPos offset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (EnchantingTableBlock.canAccessPowerProvider(world, tablePos, offset)) {
                count++;
            }
        }
        // No cap - let it count all available positions
        return count;
    }

    @Unique
    private int dungeons$getSlotCount(int bookshelves) {
        if (bookshelves >= 22) return 3;
        if (bookshelves >= 11) return 2;
        return 1;
    }

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void dungeons$overrideOffers(Inventory inv, CallbackInfo ci) {
        Arrays.fill(enchantmentPower, 0);
        Arrays.fill(enchantmentId, -1);
        Arrays.fill(enchantmentLevel, -1);
        Arrays.fill(dungeons$offers, null);

        ItemStack stack = inv.getStack(0);
        if (stack.isEmpty()) return;

        ItemStack lapis = inv.getStack(1);
        if (lapis.isEmpty() || !lapis.isOf(Items.LAPIS_LAZULI)) return;

        if (stack.hasEnchantments()) return;

        context.run((world, pos) -> {
            var registry = world.getRegistryManager()
                    .getOrThrow(RegistryKeys.ENCHANTMENT);

            int bookshelves = dungeons$countBookshelves(world, pos);
            int maxSlots = dungeons$getSlotCount(bookshelves);

            List<RegistryEntry<Enchantment>> pool = registry.streamEntries()
                    .filter(e -> e.isIn(EnchantmentTags.IN_ENCHANTING_TABLE))
                    .filter(e -> e.value().isAcceptableItem(stack))
                    .sorted(Comparator.comparing(e ->
                            e.getKey().map(k -> k.getValue().toString()).orElse("")))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (pool.isEmpty()) return;

            maxSlots = Math.min(maxSlots, pool.size());

            int enchSeed = seed.get();
            java.util.Random rng = new java.util.Random((long) enchSeed * 48271L);
            Collections.shuffle(pool, rng);

            int poolIdx = 0;
            for (int slot = 0; slot < maxSlots; slot++) {
                if (poolIdx >= pool.size()) break;

                RegistryEntry<Enchantment> chosen = pool.get(poolIdx++);
                int rawId = registry.getRawId(chosen.value());
                int max = chosen.value().getMaxLevel();
                int lvl = max > 1 ? 1 + rng.nextInt(max) : 1;

                enchantmentPower[slot] = (slot + 1) * 10;
                enchantmentId[slot] = rawId;
                enchantmentLevel[slot] = lvl;
                dungeons$offers[slot] = new EnchantmentLevelEntry(chosen, lvl);
            }
        });
    }

    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void dungeons$onButtonClick(PlayerEntity player, int id,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (id < 0 || id > 2) return;
        if (dungeons$offers[id] == null) return;
        if (enchantmentPower[id] <= 0) return;

        ItemStack stack = inventory.getStack(0);
        ItemStack lapis = inventory.getStack(1);

        if (stack.isEmpty() || stack.getCount() != 1) {
            cir.setReturnValue(false);
            return;
        }

        int lapisCost = 32;

        if (!player.getAbilities().creativeMode) {
            if (!lapis.isOf(Items.LAPIS_LAZULI) || lapis.getCount() < lapisCost) {
                cir.setReturnValue(false);
                return;
            }
        }

        context.run((world, pos) -> {
            if (world.isClient()) return;

            EnchantmentLevelEntry offer = dungeons$offers[id];
            stack.addEnchantment(offer.enchantment(), offer.level());

            if (!player.getAbilities().creativeMode) {
                lapis.decrement(lapisCost);
                if (lapis.isEmpty()) {
                    inventory.setStack(1, ItemStack.EMPTY);
                }
            }

            int newSeed = world.random.nextInt();
            ((PlayerEnchantSeedAccessor) player).setEnchantingTableSeed(newSeed);
            seed.set(newSeed);

            inventory.markDirty();
            this.onContentChanged(inventory);

            world.playSound(null, pos,
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.BLOCKS, 1.0F,
                    world.random.nextFloat() * 0.1F + 0.9F);
        });

        player.incrementStat(Stats.ENCHANT_ITEM);
        cir.setReturnValue(true);
    }
}
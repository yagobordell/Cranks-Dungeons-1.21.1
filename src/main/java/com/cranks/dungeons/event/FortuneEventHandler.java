package com.cranks.dungeons.event;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.equipment.ToolStatHelper;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModBlockTags;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class FortuneEventHandler {

    private static final Identifier SILK_TOUCH_ID = Identifier.of("minecraft", "silk_touch");

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return true;

            // 1) Preferred: tag-based detection (data pack configurable)
            boolean isOre = state.isIn(ModBlockTags.ORE_FORTUNE_ORES);

            // 2) Fallback: old heuristic based on tag path containing "ores"
            if (!isOre) {
                isOre = state.streamTags().anyMatch(tag -> {
                    String path = tag.id().getPath();
                    return path.contains("ores"); // same behavior as before
                });
            }

            if (!isOre) return true;

            ItemStack tool = player.getMainHandStack();
            if (tool.isEmpty()) return true;

            var enchants = tool.get(DataComponentTypes.ENCHANTMENTS);
            boolean hasSilkTouch = enchants != null && enchants.getEnchantments().stream()
                    .anyMatch(e -> e.getKey().get().getValue().toString().equals("minecraft:silk_touch"));

            // If Silk Touch is toggled disabled, treat it as not present
            if (EnchantmentDisableManager.isDisabled(player.getUuid(), SILK_TOUCH_ID)) {
                hasSilkTouch = false;
            }

            if (!hasSilkTouch) {
                double fortuneChance = ToolStatHelper.getFortune(tool) + player.getAttributeValue(ModAttributes.FORTUNE);

                if (fortuneChance > 0 && player.getRandom().nextDouble() < fortuneChance) {
                    // Duplicate the normal ore drops (extra drop on top of vanilla)
                    Block.dropStacks(state, serverWorld, pos, blockEntity, player, tool);
                    serverWorld.playSound(
                            null,
                            pos,
                            net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                            net.minecraft.sound.SoundCategory.BLOCKS,
                            0.5f,
                            1.5f
                    );
                }
            }
            return true;
        });
    }
}
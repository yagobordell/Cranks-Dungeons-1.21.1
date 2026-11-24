package com.cranks.dungeons.mixin;

import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.registry.ModBlockTags;
import com.cranks.dungeons.util.PlacedBlockTracker;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerInteractionManager.class)
public class SilkTouchBreakMixin {
    @Shadow @org.spongepowered.asm.mixin.Final protected ServerPlayerEntity player;

    @WrapMethod(method = "tryBreakBlock")
    private boolean cranks$useSilkTouchSetting(BlockPos pos, Operation<Boolean> original) {
        var world = player.getServerWorld();
        var state = world.getBlockState(pos);
        var harvestTool=player.getMainHandStack().copy();
        try(var context=com.cranks.dungeons.gathering.GatheringBreakContext.begin(world,pos)) {
            boolean broken=cranks$breakWithSelectedEnchantments(pos,original);
            if(broken)com.cranks.dungeons.gathering.GatheringDrops.replant(player,world,pos,state,harvestTool);
            return broken;
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean cranks$breakWithSelectedEnchantments(BlockPos pos, Operation<Boolean> original) {
        ItemStack tool = player.getMainHandStack();
        var enchantments = tool.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null || !EnchantmentDisableManager.isDisabled(player.getUuid(), Enchantments.SILK_TOUCH.getValue())) {
            return original.call(pos);
        }

        var silkTouch = player.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH);
        if (silkTouch.isEmpty() || enchantments.getLevel(silkTouch.get()) == 0) return original.call(pos);

        var builder = new ItemEnchantmentsComponent.Builder(enchantments);
        builder.set(silkTouch.get(), 0);
        tool.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        try {
            return original.call(pos);
        } finally {
            if (!tool.isEmpty()) tool.set(DataComponentTypes.ENCHANTMENTS, enchantments);
        }
    }
}

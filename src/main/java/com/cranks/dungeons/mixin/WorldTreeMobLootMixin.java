package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.registry.RegistryWrapper;
import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.registry.RegistryWrapper;
import java.util.ArrayList;
import java.util.List;

@Mixin(LootTable.class)
public class WorldTreeMobLootMixin {

    @Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"), cancellable = true)
    private void modifyMobLoot(LootWorldContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (!context.getParameters().contains(LootContextParameters.ATTACKING_ENTITY) || !context.getParameters().contains(LootContextParameters.THIS_ENTITY)) return;
        
        Entity killerEntity = context.getParameters().getNullable(LootContextParameters.ATTACKING_ENTITY);
        Entity thisEntity = context.getParameters().getNullable(LootContextParameters.THIS_ENTITY);

        if (!(killerEntity instanceof PlayerEntity player) || !(thisEntity instanceof LivingEntity victim)) return;

        List<ItemStack> originalLoot = cir.getReturnValue();
        if (originalLoot == null || originalLoot.isEmpty()) return;

        ObjectArrayList<ItemStack> modifiedLoot = new ObjectArrayList<>();
        boolean isNether = player.getEntityWorld().getRegistryKey() == World.NETHER;

        for (ItemStack stack : originalLoot) {
            ItemStack newStack = stack.copy();

            double yield = 0;

            // 1. Monster Drops
            if (victim instanceof Monster) {
                yield += getAttr(player, ModAttributes.MONSTER_DROP_YIELD);
            }
            
            // 2. Animal Drops
            if (victim instanceof AnimalEntity) {
                yield += getAttr(player, ModAttributes.ANIMAL_DROP_YIELD);
            }
            
            // 3. Meat Yield
            if (newStack.getItem().getComponents().contains(net.minecraft.component.DataComponentTypes.FOOD)) {
                if (newStack.getItem().toString().toLowerCase().contains("beef") || 
                    newStack.getItem().toString().toLowerCase().contains("pork") || 
                    newStack.getItem().toString().toLowerCase().contains("chicken") || 
                    newStack.getItem().toString().toLowerCase().contains("mutton") || 
                    newStack.getItem().toString().toLowerCase().contains("rabbit")) {
                    yield += getAttr(player, ModAttributes.MEAT_YIELD);
                }
            }

            // Apply yield
            applyYield(newStack, yield, player.getRandom());
            
            modifiedLoot.add(newStack);
        }

        cir.setReturnValue(modifiedLoot);
    }

    private static double getAttr(PlayerEntity player, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr) {
        var instance = player.getAttributeInstance(attr);
        return instance != null ? instance.getValue() : 0.0;
    }

    private static void applyYield(ItemStack stack, double yieldAmount, net.minecraft.util.math.random.Random random) {
        if (yieldAmount <= 0) return;
        
        int extraItems = (int) yieldAmount;
        double fractional = yieldAmount - extraItems;
        
        if (random.nextDouble() < fractional) {
            extraItems++;
        }
        
        if (extraItems > 0) {
            int max = stack.getMaxCount();
            int newCount = Math.min(max, stack.getCount() + (stack.getCount() * extraItems));
            stack.setCount(newCount);
        }
    }
}

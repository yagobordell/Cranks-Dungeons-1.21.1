package com.cranks.dungeons.mixin;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(
            method = "getName",
            at = @At("RETURN"),
            cancellable = true
    )
    private void applyRarityColorToHeldName(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        CustomRarity rarity = CustomRarity.getRarity(stack);
        if (rarity == CustomRarity.NORMAL || rarity.getColor() == Formatting.WHITE) {
            return;
        }

        Text original = cir.getReturnValue();

        MutableText coloredName = original.copy().formatted(rarity.getColor());

        cir.setReturnValue(coloredName);
    }

    @Unique
    private static final ThreadLocal<Boolean> IS_INITIALIZING_UNIQUE = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "inventoryTick",
            at = @At("HEAD")
    )
    private void initUniqueRunesTick(net.minecraft.world.World world, net.minecraft.entity.Entity entity, int slot, boolean selected, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (world.isClient()) return;
        if (IS_INITIALIZING_UNIQUE.get()) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty()) return;

        net.minecraft.item.Item item = stack.getItem();
        boolean isUnique = com.cranks.dungeons.equipment.ItemStatManager.isUnique(stack);

        if (isUnique) {
            if (com.cranks.dungeons.equipment.ItemStatManager.getStats(stack).isEmpty()) {
                IS_INITIALIZING_UNIQUE.set(true);
                try {
                    if (item == net.minecraft.item.Items.ELYTRA) {
                        int rechargeTier = world.getRandom().nextBetween(3, 4);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_energy_shield_recharge_rate_percent", rechargeTier);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_flying_speed", 1);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_energy_shield", 1);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_local_energy_shield_percent", 1);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_durability_bonus", 1);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "elytra_experience_bonus", 1);
                    } else if (item == net.minecraft.item.Items.TRIDENT) {
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_return", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_attack_damage", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_attack_speed", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_local_lightning_damage", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_chance_to_shock", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "trident_shock_magnitude_percent", 5);
                    } else if (item == net.minecraft.item.Items.MACE) {
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_falling_damage", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_wind_bounce", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_attack_damage", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_attack_speed", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_local_attack_damage_percent", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "mace_armor_penetration", 5);
                    } else if (item == net.minecraft.item.Items.TURTLE_HELMET) {
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_respiration", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_aqua_affinity", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_cold_resistance", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_movement_speed", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_armor", 5);
                        com.cranks.dungeons.equipment.ItemStatManager.addSpecificStat(stack, "turtle_helmet_local_armor_percent", 5);
                    }
                } finally {
                    IS_INITIALIZING_UNIQUE.set(false);
                }
            }
        }
    }

    @Unique
    private static final ThreadLocal<Boolean> IS_UPDATING_ATTRIBUTES = ThreadLocal.withInitial(() -> false);

    @Inject(method = "set", at = @At("RETURN"))
    private <T> void onComponentSet(net.minecraft.component.ComponentType<? super T> type, T value, CallbackInfoReturnable<T> cir) {
        if (type == net.minecraft.component.DataComponentTypes.ENCHANTMENTS) {
            ItemStack stack = (ItemStack) (Object) this;
            if (!stack.isEmpty() && !IS_UPDATING_ATTRIBUTES.get()) {
                IS_UPDATING_ATTRIBUTES.set(true);
                try {
                    com.cranks.dungeons.equipment.EquipmentStatApplier.updateItemAttributes(stack);
                } finally {
                    IS_UPDATING_ATTRIBUTES.set(false);
                }
            }
        }
    }
}
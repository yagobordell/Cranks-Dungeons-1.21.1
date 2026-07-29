package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class AttackRangeMixin {

    @Unique
    private static final Identifier RANGE_SYNC_ID = Identifier.of("cranks-dungeons", "range_sync");

    /**
     * Remove our previous temporary modifier so vanilla calculates the base range
     * from the clean attribute (plus any other mods' modifiers).
     */
    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"))
    private void removeOldRangeModifier(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        EntityAttributeInstance rangeAttr = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (rangeAttr != null) {
            rangeAttr.removeModifier(RANGE_SYNC_ID);
        }
    }

    /**
     * After vanilla has computed the base range, add our bonus once and
     * re-apply a temporary modifier for compatibility with other mods.
     */
    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void applyAttackRangeBonus(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        double baseRange = cir.getReturnValue();

        // 1) Bonus from your custom ATTACK_RANGE attribute (includes runes, etc.)
        double attrBonus = 0.0;
        var inst = player.getAttributeInstance(ModAttributes.ATTACK_RANGE);
        if (inst != null) {
            attrBonus = inst.getValue();
        }

        // 2) Optional enchantment bonus from the weapon
        ItemStack weapon = player.getMainHandStack();
        double enchantBonus = com.cranks.dungeons.util.CombatUtility.getEnchantmentValue(player, weapon, "minecraft:attack_range", 1.0);

        double totalBonus = attrBonus + enchantBonus;
        if (totalBonus <= 0.0) {
            return;
        }

        // Update the method's return value
        cir.setReturnValue(baseRange + totalBonus);

        // Re-apply a temporary modifier on ENTITY_INTERACTION_RANGE so other mods see
        // the same value
        EntityAttributeInstance rangeAttr = player.getAttributeInstance(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
        if (rangeAttr != null) {
            rangeAttr.addTemporaryModifier(new EntityAttributeModifier(
                    RANGE_SYNC_ID,
                    totalBonus,
                    EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }
}
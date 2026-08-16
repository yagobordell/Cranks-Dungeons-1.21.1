package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class LuckMixin {

    @Unique
    private static final Identifier LUCK_MODIFIER_ID = Identifier.of(CranksDungeons.MOD_ID, "luck_bonus");

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyCustomLuck(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Only run on server side
        if (!(player instanceof ServerPlayerEntity)) return;

        // Get luck bonus from custom attribute (Item Rarity) + dimension specific rarity
        double luckBonus = player.getAttributeValue(ModAttributes.ITEM_RARITY_PERCENT);
        var worldKey = player.getEntityWorld().getRegistryKey();
        if (worldKey == World.NETHER) {
            luckBonus += player.getAttributeValue(ModAttributes.ITEM_RARITY_IN_NETHER);
        } else if (worldKey == World.END) {
            luckBonus += player.getAttributeValue(ModAttributes.ITEM_RARITY_IN_END);
        } else {
            luckBonus += player.getAttributeValue(ModAttributes.ITEM_RARITY_IN_OVERWORLD);
        }

        var luckAttr = player.getAttributeInstance(EntityAttributes.GENERIC_LUCK);
        if (luckAttr != null) {
            EntityAttributeModifier existing = luckAttr.getModifier(LUCK_MODIFIER_ID);
            if (luckBonus > 0) {
                if (existing != null && Math.abs(existing.value() - luckBonus) < 1e-5) {
                    return; // Value unchanged, avoid re-allocating modifier and mutating attribute container
                }
                if (existing != null) {
                    luckAttr.removeModifier(LUCK_MODIFIER_ID);
                }
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        LUCK_MODIFIER_ID,
                        luckBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                luckAttr.addTemporaryModifier(modifier);
            } else if (existing != null) {
                luckAttr.removeModifier(LUCK_MODIFIER_ID);
            }
        }
    }
}
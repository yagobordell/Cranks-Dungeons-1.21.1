package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ReloadSpeedMixin {

    @Shadow
    private int itemUseTimeLeft;

    /**
     * Runs at the start of every active-item tick.
     * Decreases itemUseTimeLeft by extra ticks so bows/crossbows
     * reach full charge faster.
     *
     * Bow:      onStoppedUsing reads  i = maxUseTime - remaining  →  higher i = more pull progress
     * Crossbow: usageTick reads       (maxUseTime - remaining) / pullTime  →  same effect
     */
    @Inject(method = "tickActiveItemStack", at = @At("HEAD"))
    private void cranks$applyReloadSpeed(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack activeStack = self.getActiveItem();

        if (activeStack.isEmpty()) return;
        if (!(activeStack.getItem() instanceof BowItem)
                && !(activeStack.getItem() instanceof CrossbowItem)) return;

        EntityAttributeInstance inst = self.getAttributeInstance(ModAttributes.RELOAD_SPEED);
        double speed = 0.0;
        if (inst != null) speed += inst.getValue();
        
        // Add rune value from the item itself (allows offhand to work without being global)
        speed += com.cranks.dungeons.equipment.ItemStatManager.getTotalStatValue(activeStack, "reload_speed");

        if (speed <= 0.0) return;

        // speed = 0.30  →  30 % faster
        // Whole part:      guaranteed extra ticks each game tick
        // Fractional part: probabilistic extra tick (averages out perfectly)
        int extraTicks = (int) speed;
        double fractional = speed - extraTicks;

        if (fractional > 0.0 && self.getRandom().nextDouble() < fractional) {
            extraTicks++;
        }

        if (extraTicks > 0) {
            this.itemUseTimeLeft = Math.max(0, this.itemUseTimeLeft - extraTicks);
        }
    }
}
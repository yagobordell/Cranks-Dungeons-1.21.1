package com.cranks.dungeons.mixin;

import com.cranks.dungeons.util.StatusEffectStatManager;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(PotionContentsComponent.class)
public abstract class PotionContentsComponentMixin {

    @Shadow
    public abstract Iterable<StatusEffectInstance> getEffects();

    @Inject(method = "buildTooltip", at = @At("HEAD"), cancellable = true)
    private void customPotionTooltip(Consumer<Text> textConsumer, float durationMultiplier, float tickRate, CallbackInfo ci) {
        Iterable<StatusEffectInstance> effects = this.getEffects();
        boolean hasAny = false;
        for (StatusEffectInstance instance : effects) {
            hasAny = true;
            Text statName = StatusEffectStatManager.getFormattedEffectName(instance);
            
            if (instance.getEffectType().value().isInstant() || instance.getDuration() <= 20) {
                textConsumer.accept(statName);
            } else {
                Text durationText = StatusEffectUtil.getDurationText(instance, durationMultiplier, tickRate);
                MutableText line = Text.empty()
                        .append(statName)
                        .append(Text.literal(" (").formatted(Formatting.GRAY))
                        .append(durationText.copy().formatted(Formatting.GRAY))
                        .append(Text.literal(")").formatted(Formatting.GRAY));
                textConsumer.accept(line);
            }

            List<Text> descriptions = StatusEffectStatManager.getDescriptionsByTitle(statName.getString());
            if (descriptions != null) {
                for (Text descLine : descriptions) {
                    textConsumer.accept(descLine);
                }
            }
        }

        if (hasAny) {
            ci.cancel();
        }
    }
}

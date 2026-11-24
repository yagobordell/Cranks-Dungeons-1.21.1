package com.cranks.dungeons.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EffectDescriptions {
    private static final String PREFIX = "status.cranks-dungeons.";

    private EffectDescriptions() {}

    public static Text name(StatusEffectInstance instance) {
        var type = instance.getEffectType();
        Text name = type.value().getName();
        if (type.equals(ModEffects.POISON)) return Text.translatable(PREFIX + "stacks", name, instance.getAmplifier() + 1);
        if (StatusEffectRules.of(type) != null || type.equals(ModEffects.SHOCK)) return name;
        return instance.getAmplifier() > 0 && Registries.STATUS_EFFECT.getId(type.value()).getNamespace().equals("minecraft")
                ? Text.translatable(PREFIX + "tier", name, instance.getAmplifier() + 1) : name;
    }

    public static String number(double number) {
        return String.format(Locale.ROOT, "%.2f", number).replaceAll("\\.?0+$", "");
    }

    public static List<Text> descriptions(StatusEffectInstance instance) {
        return descriptions(instance, 1);
    }

    private static List<Text> descriptions(StatusEffectInstance instance, float instantMultiplier) {
        var effect = instance.getEffectType();
        var id = Registries.STATUS_EFFECT.getId(effect.value());
        String path = id.getPath();
        int amplifier = instance.getAmplifier();
        int tier = amplifier + 1;
        var lines = new ArrayList<Text>();
        StatusEffectRules rule = StatusEffectRules.of(effect);
        if (rule != null) {
            lines.add(line(path, number(Math.abs(rule.amount(amplifier)))));
            if(rule==StatusEffectRules.REGENERATION)lines.add(Text.literal("Total regeneration is limited to 10% of maximum health per second.").formatted(Formatting.GRAY));
            addExtra(lines, path);
            return lines;
        }
        if (id.getNamespace().equals("minecraft")) {
            switch (path) {
                case "instant_health" -> lines.add(line(path, number(StatusEffectRules.instantHealth(amplifier) * instantMultiplier)));
                case "instant_damage" -> lines.add(line(path, number(StatusEffectRules.instantDamage(amplifier) * instantMultiplier)));
                case "poison" -> lines.add(line(path, StatusEffectRules.poisonStacks(amplifier)));
                case "wither" -> lines.add(line(path, StatusEffectRules.witherReduction(amplifier)));
                case "jump_boost" -> lines.add(line(path, number(0.1 * tier), tier));
                case "levitation" -> lines.add(line(path, number(tier)));
                case "hunger" -> lines.add(line(path, number(0.1 * tier), number(40.0 / tier)));
                case "saturation" -> lines.add(line(path, tier));
                case "conduit_power" -> lines.add(line(path, number(StatusEffectRules.HASTE.amount(amplifier))));
                case "hero_of_the_village" -> lines.add(line(path, number(30 + 6.25 * amplifier)));
                default -> {
                    if (Language.getInstance().hasTranslation(PREFIX + path)) lines.add(line(path));
                }
            }
            addExtra(lines, path);
        } else if (id.getNamespace().equals("cranks-dungeons")) {
            switch (path) {
                case "cold_resistance", "lightning_resistance", "void_resistance" -> lines.add(line(path, 15 * tier));
                case "poison" -> lines.add(line("ailment_poison", tier));
                case "shock" -> lines.add(line("ailment_shock", amplifier));
                default -> {
                    if (Language.getInstance().hasTranslation(PREFIX + path)) lines.add(line(path));
                }
            }
        }
        if (lines.isEmpty()) {
            effect.value().forEachAttributeModifier(amplifier, (attribute, modifier) ->
                    lines.add(Text.translatable(PREFIX + "attribute", Text.translatable(attribute.value().getTranslationKey()),
                            number(modifier.value())).formatted(Formatting.GRAY)));
        }
        return lines;
    }

    private static Text line(String path, Object... values) {
        return Text.translatable(PREFIX + path, values).formatted(Formatting.GRAY);
    }

    private static void addExtra(List<Text> lines, String path) {
        if (Language.getInstance().hasTranslation(PREFIX + path + ".extra")) lines.add(line(path + ".extra"));
    }

    public static List<Text> tooltip(StatusEffectInstance effect, float durationMultiplier, float tickRate) {
        return tooltip(effect, durationMultiplier, tickRate, 1);
    }

    /** Active-player detail comes from the authoritative snapshot; item/potion previews stay generic. */
    public static List<Text> activeTooltip(StatusEffectInstance effect, float tickRate,
                                          com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry detail) {
        if(detail==null)return tooltip(effect,1,tickRate);
        var lines=new ArrayList<Text>();lines.add(name(effect));
        if(effect.isInfinite())lines.add(line("permanent"));
        else lines.add(line("remaining",StatusEffectUtil.getDurationText(effect,1,tickRate)));
        String path=detail.texture().getPath();
        if(path.endsWith("/burn.png")||path.endsWith("/poison.png")){
            lines.add(line("ailment_dps",number(detail.damagePerSecond())));
            lines.add(line(path.endsWith("/burn.png")?"burn_rules":"poison_rules"));
        }else if(path.endsWith("/bleed.png")){
            lines.add(line("bleed_fraction",number(detail.magnitude()*100)));
            lines.add(line("bleed_stored",number(detail.storedDamage())));
        }else if(path.endsWith("/shock.png"))lines.add(line("ailment_shock",number(detail.magnitude()*100)));
        else lines.addAll(descriptions(effect));
        lines.add(line("ailment_cleanse"));
        return lines;
    }

    public static List<Text> tooltip(StatusEffectInstance effect, float durationMultiplier, float tickRate, float instantMultiplier) {
        var lines = new ArrayList<Text>();
        lines.add(name(effect));
        if (!effect.getEffectType().value().isInstant()) {
            if (effect.isInfinite()) lines.add(line("permanent"));
            else if (effect.getDuration() > 0) {
                Text duration = StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate);
                if (!duration.getString().isBlank()) lines.add(line("remaining", duration));
            }
        }
        lines.addAll(descriptions(effect, instantMultiplier));
        return lines;
    }
}

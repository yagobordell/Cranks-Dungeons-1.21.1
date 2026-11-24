package com.cranks.dungeons.client;

import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class CharacterStats {
    enum Category {
        ATTRIBUTES("Attributes"), RESOURCES("Health, mana & shield"), DEFENCE("Defence"),
        OFFENCE("Offence"), GATHERING("Gathering"), UTILITY("Utility"), KEYSTONES("Keystones");

        final String title;

        Category(String title) {
            this.title = title;
        }
    }

    record Stat(String name, String search, ToDoubleFunction<Values> value, String unit, String detail,String guideId) {
        String display(double number) {
            return formatValue(number, unit);
        }
    }

    record Values(PlayerEntity player, Map<String, Double> draft) {
        double get(RegistryEntry<EntityAttribute> attribute) {
            double current = player.getAttributeValue(attribute);
            if (draft.isEmpty()) return current;
            double gain = draft.getOrDefault(attribute.getIdAsString(), 0.0);
            boolean health=com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,EntityAttributes.GENERIC_MAX_HEALTH);
            boolean evasion=com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,ModAttributes.EVASION_RATING);
            boolean armour=com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,EntityAttributes.GENERIC_ARMOR);
            boolean shield=com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,ModAttributes.ENERGY_SHIELD);
            if (health) gain += 2 * draft.getOrDefault(ModAttributes.STRENGTH.getIdAsString(), 0.0)
                    +draft.getOrDefault(ModAttributes.MAX_HEALTH_BONUS.getIdAsString(),0.0);
            if (evasion) gain += 2 * draft.getOrDefault(ModAttributes.DEXTERITY.getIdAsString(), 0.0);
            if (armour) gain += draft.getOrDefault(ModAttributes.BONUS_ARMOR.getIdAsString(),0.0);
            if (com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,ModAttributes.MAX_MANA)) gain += 2 * draft.getOrDefault(ModAttributes.INTELLIGENCE.getIdAsString(), 0.0);
            EntityAttributeInstance instance = player.getAttributeInstance(attribute);
            if (instance == null) return current;
            double base = instance.getBaseValue();
            double baseMultiplier = 1;
            double multiplier = 1;
            for (EntityAttributeModifier modifier : instance.getModifiers()) {
                switch (modifier.operation()) {
                    case ADD_VALUE -> base += modifier.value();
                    case ADD_MULTIPLIED_BASE -> baseMultiplier += modifier.value();
                    case ADD_MULTIPLIED_TOTAL -> multiplier *= 1 + modifier.value();
                }
            }
            if(com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,ModAttributes.MINING_EFFICIENCY)) {baseMultiplier+=gain;gain=0;}
            if(evasion)baseMultiplier+=draft.getOrDefault(ModAttributes.EVASION_RATING_PERCENT.getIdAsString(),0.0);
            if(armour)baseMultiplier+=draft.getOrDefault(ModAttributes.ARMOR_PERCENT.getIdAsString(),0.0);
            if(shield)baseMultiplier+=draft.getOrDefault(ModAttributes.ENERGY_SHIELD_PERCENT.getIdAsString(),0.0);
            if(health)multiplier*=(1+player.getAttributeValue(ModAttributes.MAX_HEALTH_PERCENT)
                    +draft.getOrDefault(ModAttributes.MAX_HEALTH_PERCENT.getIdAsString(),0.0))/(1+player.getAttributeValue(ModAttributes.MAX_HEALTH_PERCENT));
            return attribute.value().clamp((base + gain) * baseMultiplier * multiplier);
        }

        double shield() {
            return get(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0 ? 0 : get(ModAttributes.ENERGY_SHIELD);
        }

        double armor() {
            double armor = get(EntityAttributes.GENERIC_ARMOR);
            return get(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0 ? armor + get(ModAttributes.EVASION_RATING) : armor;
        }

        double evasion() {
            return get(ModAttributes.KEYSTONE_IRON_REFLEXES) > 0 ? 0 : get(ModAttributes.EVASION_RATING);
        }
    }

    private final Map<Category, List<Stat>> groups = new EnumMap<>(Category.class);

    CharacterStats() {
        for (Category category : Category.values()) groups.put(category, new ArrayList<>());
        add(Category.ATTRIBUTES, ModAttributes.STRENGTH, "Strength", "", "Each point grants 2 maximum health.");
        add(Category.ATTRIBUTES, ModAttributes.DEXTERITY, "Dexterity", "", "Each point grants 2 evasion rating.");
        add(Category.ATTRIBUTES, ModAttributes.INTELLIGENCE, "Intelligence", "", "Each point grants 2 maximum mana.");
        add(Category.RESOURCES, EntityAttributes.GENERIC_MAX_HEALTH, "Maximum Health", "", "");
        add(Category.RESOURCES, ModAttributes.MAX_MANA, "Maximum Mana", "", "");
        add(Category.RESOURCES, "Health Regen", "health regeneration", v -> v.get(ModAttributes.KEYSTONE_VAAL_PACT)>0||v.get(ModAttributes.KEYSTONE_ZEALOTS_OATH)>0?0:
                com.cranks.dungeons.stat.CombatStatRules.healthRegeneration(v.get(EntityAttributes.GENERIC_MAX_HEALTH),v.get(ModAttributes.LIFE_REGENERATION),v.get(ModAttributes.MAX_HEALTH_REGENERATION_PERCENT),
                        v.player.getVelocity().horizontalLengthSquared()>0.0001?v.get(ModAttributes.REGENERATION_RATE_WHILE_MOVING_PERCENT):0), "/s", "Effective health regeneration; capped at 10% of maximum health per second. Regeneration redirected to shield is excluded.");
        add(Category.RESOURCES, "Mana Recovery", "mana regeneration", v -> com.cranks.dungeons.stat.CombatStatRules.manaRegeneration(v.get(ModAttributes.MAX_MANA),v.get(ModAttributes.MANA_REGENERATION),
                v.get(ModAttributes.MANA_REGENERATION_PERCENT),v.get(ModAttributes.MANA_REGENERATION_RATE),v.get(ModAttributes.MANA_RECOVERY_RATE)), "/s", "Flat plus percentage regeneration, scaled by regeneration and recovery bonuses.");
        add(Category.RESOURCES, "Energy Shield", "maximum energy shield es", Values::shield, "", "Absorbs hit damage before health. Void uses twice as much shield. Ailment damage, drowning, starvation and invulnerability-bypassing damage bypass it.");
        add(Category.RESOURCES,"ES Regeneration","zealot energy shield regeneration",v->v.get(ModAttributes.KEYSTONE_ZEALOTS_OATH)>0
                &&v.get(ModAttributes.KEYSTONE_VAAL_PACT)<=0&&v.get(ModAttributes.KEYSTONE_CRIMSON_PACT)<=0
                ?com.cranks.dungeons.stat.CombatStatRules.healthRegeneration(v.get(EntityAttributes.GENERIC_MAX_HEALTH),v.get(ModAttributes.LIFE_REGENERATION),
                        v.get(ModAttributes.MAX_HEALTH_REGENERATION_PERCENT),v.player.getVelocity().horizontalLengthSquared()>0.0001?v.get(ModAttributes.REGENERATION_RATE_WHILE_MOVING_PERCENT):0):0,
                "/s","Health regeneration redirected to shield by Zealot's Oath. Separate from delayed shield recharge.");
        add(Category.RESOURCES, "ES Recharge Rate", "energy shield recharge rate", v -> v.get(ModAttributes.KEYSTONE_GHOST_REAVER)>0?0:v.shield()
                * com.cranks.dungeons.stat.CombatStatRules.shieldRechargeFraction(v.get(ModAttributes.ENERGY_SHIELD_RECHARGE_RATE_PERCENT)), "/s", "Recharge restores at most 50% of maximum shield per second. Ghost Reaver disables recharge.");
        add(Category.RESOURCES, "ES Recharge Delay", "energy shield recharge delay", v -> com.cranks.dungeons.stat.CombatStatRules.shieldRechargeDelay(
                v.get(ModAttributes.FASTER_START_OF_ENERGY_SHIELD_RECHARGE)), "s", "Time without an eligible hit before recharge starts; minimum 1 second.");

        add(Category.DEFENCE, "Armour", "armor armour", Values::armor, "", "Reduces physical damage from hits.");
        add(Category.DEFENCE, "Physical Reduction", "armour armor physical damage reduction",
                v -> com.cranks.dungeons.stat.CombatStatRules.armourReduction(v.armor(),50), "%", "Estimated against a 50-damage hit. Larger hits receive less reduction; maximum 90%.");
        add(Category.DEFENCE, "Evasion Rating", "evasion rating", Values::evasion, "", "");
        add(Category.DEFENCE, "Evade Chance", "evasion dodge chance", v -> com.cranks.dungeons.stat.CombatStatRules.evasionChance(v.evasion(),50),
                "%", "Estimated against a 50-damage eligible hit. Automatic avoidance is capped at 75%.");
        add(Category.DEFENCE,"Critical Protection","critical damage mitigation",v->com.cranks.dungeons.stat.CombatStatRules.criticalProtection(v.armor(),
                v.get(ModAttributes.ARMOR_CRIT_MITIGATION),v.get(ModAttributes.REDUCED_CRIT_DAMAGE_TAKEN_PERCENT)),"%","Reduces the extra damage from critical hits. Includes specialist armour-based protection.");
        add(Category.DEFENCE, ModAttributes.FIRE_RESISTANCE, "Fire Resistance", "%", "");
        add(Category.DEFENCE, ModAttributes.COLD_RESISTANCE, "Cold Resistance", "%", "");
        add(Category.DEFENCE, ModAttributes.LIGHTNING_RESISTANCE, "Lightning Resistance", "%", "");
        add(Category.DEFENCE, ModAttributes.VOID_RESISTANCE, "Void Resistance", "%", "");
        add(Category.OFFENCE, EntityAttributes.GENERIC_ATTACK_DAMAGE, "Attack Damage", "", "");
        add(Category.OFFENCE, "Attack Speed", "attack speed", v->v.get(EntityAttributes.GENERIC_ATTACK_SPEED)*(1+v.get(ModAttributes.ATTACK_SPEED)), "/s", "Effective melee attacks per second with the held item, including passive attack speed.");
        add(Category.OFFENCE, "Critical Chance", "crit critical chance local global", v ->
                v.get(ModAttributes.KEYSTONE_RESOLUTE_TECHNIQUE)>0?0:com.cranks.dungeons.ailment.AilmentRules.criticalChance(
                        v.get(ModAttributes.CRIT_CHANCE),
                        com.cranks.dungeons.equipment.EquipmentStatApplier.getItemStatValue(v.player().getMainHandStack(),"local_crit_chance"),
                        v.get(ModAttributes.GLOBAL_CRIT_CHANCE_PERCENT),0), "%",
                "Effective chance with the held weapon, including local/global increases. Conditional chance against afflicted targets is listed separately. Maximum 100%.");
        add(Category.OFFENCE, "Critical Multiplier", "crit critical multiplier damage", v ->
                v.get(ModAttributes.KEYSTONE_ELEMENTAL_OVERLOAD)>0?1:v.get(ModAttributes.CRIT_MULTIPLIER), "×",
                "Damage multiplier on a critical hit before target critical protection. Elemental Overload removes the extra critical damage.");
        add(Category.UTILITY, "Movement Speed", "walk movement move speed", v ->
                v.get(EntityAttributes.GENERIC_MOVEMENT_SPEED) / 0.1 - 1, "%", "Relative to standard walking speed.");
        add(Category.UTILITY,"Block Reach","block reach gathering range",v->v.get(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE)
                +com.cranks.dungeons.gathering.GatheringRules.bounded(v.get(ModAttributes.BREAKING_RANGE),2),"m","Block interaction distance; custom reach adds at most 2 metres.");

        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.ALL_ATTRIBUTES) {
            String id = attribute.getKey().orElseThrow().getValue().getPath();
            if (switch (id) {
                case "strength", "dexterity", "intelligence", "fire_resistance", "cold_resistance",
                        "lightning_resistance", "void_resistance", "max_mana", "energy_shield",
                        "evasion_rating", "crit_chance", "crit_multiplier", "armor_crit_mitigation", "reduced_crit_damage_taken_percent" -> true;
                default -> false;
            }) continue;
            if(com.cranks.dungeons.gathering.GatheringStats.LEGACY.contains(id))continue;
            String key = attribute.value().getTranslationKey();
            String name = Text.translatable(key).getString();
            if (name.equals(key)) name = PassiveStatHelper.getReadableName(attribute);
            add(category(id),name,attribute.getIdAsString(),v->{
                Double effective=com.cranks.dungeons.gathering.GatheringStats.display(id,v.player(),v::get);
                return effective==null?v.get(attribute):effective;
            },unit(id),com.cranks.dungeons.stat.StatDescriptions.description(id));
        }
    }

    Map<Category, List<Stat>> groups() {
        return groups;
    }

    private void add(Category category, RegistryEntry<EntityAttribute> attribute, String name, String unit, String detail) {
        add(category, name, attribute.getIdAsString(), v -> v.get(attribute), unit, detail);
    }

    private void add(Category category, String name, String search, ToDoubleFunction<Values> value, String unit, String detail) {
        String guideId=search.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")?search:"derived/"+com.cranks.dungeons.client.guide.GuideReferences.slug(name);
        groups.get(category).add(new Stat(name, normalize(name + " " + search + " " + detail + " " + category.title), value, unit, detail,guideId));
    }

    public record Reference(String id,String name,String description,String unit){}
    public static List<Reference> references(){return new CharacterStats().groups.values().stream().flatMap(List::stream)
            .map(stat->new Reference(stat.guideId(),stat.name(),stat.detail(),stat.unit())).toList();}

    static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace("armour", "armor").replace('_', ' ').strip();
    }

    private static Category category(String id) {
        if (id.startsWith("keystone_")) return Category.KEYSTONES;
        if (id.contains("yield") || id.contains("fortune") || id.contains("mining") || id.contains("farming")
                || id.contains("woodcutting") || id.contains("experience") || id.contains("rarity") || id.contains("drop")) return Category.GATHERING;
        if (id.contains("penetration") || id.startsWith("attack_") || id.startsWith("crit_") || id.contains("damage")
                && !id.contains("damage_taken") || id.contains("projectile")) return Category.OFFENCE;
        if (id.contains("arm") || id.contains("resistance") || id.contains("evasion") || id.contains("threshold")
                || id.startsWith("block_") || id.startsWith("reduced_")) return Category.DEFENCE;
        if (id.contains("mana") || id.contains("health") || id.contains("regeneration") || id.contains("shield")) return Category.RESOURCES;
        return Category.UTILITY;
    }

    static String formatValue(double number, String unit) {
        if (unit.equals("%")) number *= 100;
        if (unit.equals("toggle")) return number > 0 ? "Active" : "Inactive";
        return String.format(Locale.ROOT, "%.2f", number).replaceAll("\\.?0+$", "") + unit;
    }

    static String unit(String id) {
        if (id.equals("riding_speed") || id.equals("mount_endurance") || id.equals("mount_recovery")
                || id.equals("riding_agility") || id.equals("sure_footing")) return "%";
        if (id.startsWith("keystone_") || id.equals("ailment_threshold_from_boots_lowest_armor_evasion")) return "toggle";
        if (id.endsWith("_multiplier")) return "×";
        if (id.startsWith("reduced_")&&id.endsWith("_duration_on_you") || id.equals("armor_applies_to_elemental") || id.equals("physical_taken_as_void"))return "%";
        if (id.contains("percent") || id.contains("chance") || id.contains("resistance") || id.contains("yield")
                || id.contains("fortune") || id.endsWith("experience") || id.contains("rarity") || id.contains("leeched")
                || id.equals("knockback") || id.startsWith("faster_") || id.endsWith("_speed") && !id.equals("attack_speed")
                || id.equals("mana_recovery_rate") || id.equals("mana_regeneration_rate")) return "%";
        return "";
    }
}

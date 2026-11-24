package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.stat.CustomStat;
import java.util.*;
import java.util.stream.Collectors;

/** Equipment categories with individually clickable versions, without repeated rows for armour slots. */
public final class GuideEquipmentGroups {
    private GuideEquipmentGroups() {}

    public static List<String> describe(Collection<CustomStat> versions) {
        var categories = new LinkedHashMap<String,List<CustomStat>>();
        var fixed=new LinkedHashSet<String>();for(var version:versions)if(GuideTomes.bases(version).isEmpty())for(String item:GuideRuneArticles.fixed(version).keySet())fixed.add(GuideText.reference(GuideIllustrations.stack(item).getName().getString(),GuideTarget.item(item)));
        versions.stream().filter(version->!GuideTomes.bases(version).isEmpty()).sorted(Comparator.comparing(CustomStat::getEquipmentType).thenComparing(CustomStat::getId))
                .forEach(stat -> categories.computeIfAbsent(category(stat.getEquipmentType()), key -> new ArrayList<>()).add(stat));
        var rows = new ArrayList<String>();
        categories.forEach((category, stats) -> {
            var affinities = stats.stream().map(GuideEquipmentGroups::affinity).distinct().toList();
            boolean shared = affinities.size() == 1;
            String equipment = stats.stream().map(stat -> GuideText.reference(label(stat.getEquipmentType()), GuideTopics.equipment(stat.getEquipmentType()))
                    + (!shared && !affinity(stat).isBlank() ? " (" + affinity(stat) + ")" : ""))
                    .distinct().collect(Collectors.joining(", "));
            String prefix=stats.size()==1&&category.equals(label(stats.getFirst().getEquipmentType()))?"":category+": ";
            rows.add(prefix + equipment + (shared && !affinities.getFirst().isBlank() ? " — " + affinities.getFirst() : ""));
        });
        if(!fixed.isEmpty())rows.add("Fixed Unique: "+String.join(", ",fixed));
        return List.copyOf(rows);
    }

    private static String category(EquipmentType type) {
        return switch(type) {
            case HELMET,CHESTPLATE,LEGGINGS,BOOTS -> "Armour";
            case SHIELD -> "Shields";
            case ELYTRA -> "Gliding equipment";
            default -> "Weapons";
        };
    }

    private static String label(EquipmentType type) {
        return switch(type) {
            case HELMET -> "Helmets"; case CHESTPLATE -> "Chestplates"; case LEGGINGS -> "Leggings"; case BOOTS -> "Boots";
            case SWORD -> "Swords"; case SPEAR -> "Spears"; case BOW -> "Bows"; case CROSSBOW -> "Crossbows";
            case SHIELD -> "Shields"; case MACE -> "Maces"; case TRIDENT -> "Tridents"; case ELYTRA -> "Elytra";
            default -> type.getName();
        };
    }

    private static String affinity(CustomStat stat) {
        return switch(stat.getEquipmentType()) {
            case HELMET,CHESTPLATE,LEGGINGS,BOOTS -> switch(stat.getArmorBaseType()) {
                case STR -> "requires an Armour base"; case DEX -> "requires an Evasion base"; case INT -> "requires an Energy Shield base";
                case STR_DEX -> "requires Armour and Evasion"; case STR_INT -> "requires Armour and Energy Shield";
                case DEX_INT -> "requires Evasion and Energy Shield"; default -> "";
            };
            default -> "";
        };
    }
}

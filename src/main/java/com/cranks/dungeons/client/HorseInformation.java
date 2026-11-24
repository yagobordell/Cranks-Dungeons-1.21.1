package com.cranks.dungeons.client;

import com.cranks.dungeons.horse.HorseBreedingFocus;
import com.cranks.dungeons.horse.HorseBreedingRules;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.MountGeneticRatings;
import com.cranks.dungeons.network.HorseGeneticsSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.font.TextRenderer;
import java.util.*;

/** Shared horse training, genetics, family and pairing descriptions. */
public final class HorseInformation {
    private static final double SPEED_TO_BLOCKS_PER_SECOND=42.16,ZERO_THRESHOLD=0.0001;
    private HorseInformation(){}
    public static List<StatLine> buildStats(HorseEntity horse, HorseGeneticsSyncPacket data) {
        List<StatLine> lines = new ArrayList<>();
        if (data == null) return syncingLines();

        String xp = data.experienceRequired() <= 0 ? "MAX"
                : data.experience() + "/" + data.experienceRequired();
        lines.add(new StatLine("Level", data.level() + " " + xp, 0xFFFFD966));
        lines.add(new StatLine("Health", format(horse.getHealth()) + "/" + format(horse.getMaxHealth()), 0xFFB8B8B8));
        lines.add(new StatLine("Movement Speed", String.format(Locale.US, "%.1f blocks/s",
                getAttribute(horse, EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        * SPEED_TO_BLOCKS_PER_SECOND), 0xFFB8B8B8));
        lines.add(new StatLine("Jump Strength", format(getAttribute(horse,
                EntityAttributes.GENERIC_JUMP_STRENGTH)), 0xFFB8B8B8));
        lines.add(new StatLine("Stamina", format(data.stamina()) + "/" + format(data.maxStamina()), 0xFFB8B8B8));
        lines.add(new StatLine("Mounted Recovery", format(data.mountedStaminaRegen()) + "/s", 0xFFB8B8B8));
        lines.add(new StatLine("Gallop", "+" + Math.round(data.gallopSpeedBonus() * 100.0) + "%", 0xFFB8B8B8));
        lines.add(new StatLine("Gallop Cost", format(data.gallopDrain()) + "/s", 0xFFB8B8B8));
        lines.add(new StatLine("Jump Cost", format(data.jumpStaminaCost()), 0xFFB8B8B8));
        addIfNonZero(lines, "Armor", getAttribute(horse, EntityAttributes.GENERIC_ARMOR), "");
        addIfNonZero(lines, "Evasion", getAttribute(horse, ModAttributes.EVASION_RATING), "");
        addIfNonZero(lines, "Energy Shield", getAttribute(horse, ModAttributes.ENERGY_SHIELD), "");
        addIfNonZero(lines, "Life Regeneration", getAttribute(horse, ModAttributes.LIFE_REGENERATION), "/s");
        addIfNonZero(lines, "Knockback Resistance",
                getAttribute(horse, ModAttributes.KNOCKBACK_RESISTANCE) * 100.0, "%");
        addTrainingMilestones(lines, data.level());
        lines.add(new StatLine("Training", "Riding, long jumps, and defeating enemies while mounted", 0xFFA3D5FF));
        lines.add(new StatLine("Travel XP", "2 per 10 blocks; 4 while galloping", 0xFFB8B8B8));
        lines.add(new StatLine("Jump XP","Awarded after a rider-requested jump travels at least 2 blocks and lands; stationary jumps give none",0xFFB8B8B8));
        lines.add(new StatLine("Combat XP", "5-50 per enemy, based on maximum health; passive animals give none", 0xFFB8B8B8));
        lines.add(new StatLine("Per Level", "+10 base health, +0.5% base speed, +0.35% base jump, +2 evasion", 0xFFB8B8B8));
        lines.add(new StatLine("Level Cap", "30", 0xFFB8B8B8));
        return lines;
    }
    public static List<StatLine> buildGenes(HorseGeneticsSyncPacket data,
                                                     HorseBreedingFocus selectedFocus) {
        if (data == null) return syncingLines();
        List<StatLine> lines = new ArrayList<>();
        lines.add(new StatLine("Overall Grade", data.grade(), gradeColor(data.grade())));
        lines.add(new StatLine("Sex", data.male() ? "Male" : "Female", 0xFFB8B8B8));
        lines.add(geneLine("Health Bonus Gene", "+" + format(data.geneticHealth()), MountKind.HORSE,
                0, data.geneticHealth(), data.generation()));
        lines.add(geneLine("Speed Gene", String.format(Locale.US, "%.1f blocks/s",
                data.geneticSpeed() * SPEED_TO_BLOCKS_PER_SECOND), MountKind.HORSE,
                1, data.geneticSpeed(), data.generation()));
        lines.add(geneLine("Jump Gene", format(data.geneticJump()), MountKind.HORSE,
                2, data.geneticJump(), data.generation()));
        lines.add(geneLine("Evasion Gene", format(data.geneticEvasion()), MountKind.HORSE,
                3, data.geneticEvasion(), data.generation()));
        lines.add(geneLine("Endurance Gene", format(data.geneticEndurance()), MountKind.HORSE,
                4, data.geneticEndurance(), data.generation()));
        lines.add(geneLine("Armor Bonus Gene", "+" + format(data.geneticArmor()), MountKind.HORSE,
                5, data.geneticArmor(), data.generation()));
        lines.add(new StatLine("Trait", data.trait(), 0xFFA8E6A3));
        addTraitBonusLines(lines, data.trait());
        lines.add(new StatLine("Breeding Focus", focusDisplayText(selectedFocus),
                0xFFA3D5FF));
        return lines;
    }
    public static StatLine geneLine(String name, String value, MountKind kind,
                                             int geneIndex, double geneValue, int generation) {
        String grade = MountGeneticRatings.grade(kind, geneIndex, geneValue, generation);
        return new StatLine(name, value + " [" + grade + "]", gradeColor(grade),
                geneProgress(kind, geneIndex, geneValue));
    }
    public static double geneProgress(MountKind kind, int geneIndex, double value) {
        double minimum = MountGeneticRatings.breedingMinimum(kind, geneIndex);
        double maximum = MountGeneticRatings.breedingMaximum(kind, geneIndex);
        if (maximum <= minimum) return value >= maximum ? 1.0 : 0.0;
        return Math.max(0.0, Math.min(1.0, (value - minimum) / (maximum - minimum)));
    }
    public static String focusDisplayText(HorseBreedingFocus focus) {
        return "< " + focus.displayName() + " >";
    }
    public static void addTraitBonusLines(List<StatLine> lines, String trait) {
        switch (trait) {
            case "Swift" -> {
                lines.add(new StatLine("Movement Speed", "+5%", 0xFFA8E6A3));
                lines.add(new StatLine("Gallop Speed", "+5%", 0xFFA8E6A3));
                lines.add(new StatLine("Terrain Efficiency", "+10%", 0xFFA8E6A3));
            }
            case "Hardy" -> {
                lines.add(new StatLine("Maximum Health", "+10%", 0xFFA8E6A3));
                lines.add(new StatLine("Maximum Stamina", "+15%", 0xFFA8E6A3));
                lines.add(new StatLine("Stamina Recovery", "+25%", 0xFFA8E6A3));
            }
            case "Sure-Footed" -> {
                lines.add(new StatLine("Jump Strength", "+8%", 0xFFA8E6A3));
                lines.add(new StatLine("Knockback Resistance", "+15%", 0xFFA8E6A3));
                lines.add(new StatLine("Step Height", "+0.15", 0xFFA8E6A3));
                lines.add(new StatLine("Safe Fall Distance", "+2", 0xFFA8E6A3));
                lines.add(new StatLine("Jump Stamina Cost", "-2", 0xFFA8E6A3));
            }
            case "Warhorse" -> {
                lines.add(new StatLine("Armor", "+30% of base", 0xFFA8E6A3));
                lines.add(new StatLine("Evasion", "+20", 0xFFA8E6A3));
                lines.add(new StatLine("Knockback Resistance", "+20%", 0xFFA8E6A3));
            }
            default -> {
            }
        }
    }
    public static List<StatLine> buildFamily(HorseEntity horse, HorseGeneticsSyncPacket data) {
        if (data == null) return syncingLines();
        List<StatLine> lines = new ArrayList<>();
        lines.add(new StatLine("Generation", Integer.toString(data.generation()), 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Attempts", data.breedsRemaining() + "/3", 0xFFB8B8B8));
        lines.add(new StatLine("Breeding Cooldown", formatTime(data.breedingCooldownTicks()), 0xFFB8B8B8));
        if (data.imprintProgress() > 0.0 || !"None".equals(data.imprintOwnerName())) {
            lines.add(new StatLine("Imprint", Math.round(data.imprintProgress() * 100)
                    + "% - " + data.imprintOwnerName(), 0xFFA3D5FF));
        }
        if (horse.isBaby()) {
            if(data.imprintProgress()<1&&!"None".equals(data.imprintOwnerName())){
                lines.add(new StatLine("Next Care",data.careCooldownTicks()>0?formatTime(data.careCooldownTicks()):"Ready — feed Sugar",0xFFA3D5FF));
                lines.add(new StatLine("Care","Only "+data.imprintOwnerName()+" can imprint this foal; each care adds 25%",0xFFB8B8B8));
            }else lines.add(new StatLine("Care",data.imprintProgress()>=1?"Imprinting complete":"No breeder imprint",0xFFB8B8B8));
        }
        if(data.imprintProgress()>0){
            lines.add(new StatLine("Imprint Bonus","+"+Math.round(data.imprintProgress()*20)+"% base health, speed and jump while ridden by "+data.imprintOwnerName(),0xFFA8E6A3));
            lines.add(new StatLine("Ownership","Imprint belongs to the breeder; transferring ownership does not transfer it",0xFFB8B8B8));
        }
        return lines;
    }
    public static List<HorseEntity> findNearbyMates(HorseEntity horse,
                                                            HorseGeneticsSyncPacket data) {
        List<HorseEntity> candidates = horse.getEntityWorld().getEntitiesByClass(
                HorseEntity.class, horse.getBoundingBox().expand(8.0),
                candidate -> candidate != horse && candidate.isTame() && !candidate.isBaby());
        candidates.sort((first, second) -> {
            int firstPriority = matePriority(horse, data, first);
            int secondPriority = matePriority(horse, data, second);
            if (firstPriority != secondPriority) return Integer.compare(firstPriority, secondPriority);
            return Double.compare(horse.squaredDistanceTo(first), horse.squaredDistanceTo(second));
        });
        return List.copyOf(candidates);
    }
    public static int matePriority(HorseEntity horse, HorseGeneticsSyncPacket data,
                                            HorseEntity candidate) {
        HorseGeneticsSyncPacket candidateData = HorseGeneticsClientData.get(candidate.getId());
        if (data == null || candidateData == null) return 3;
        if (pairingBlockReason(horse, data, candidate, candidateData) == null) return 0;
        return data.male() != candidateData.male() ? 1 : 2;
    }
    public static List<StatLine> buildPairingPreview(
            HorseEntity horse, HorseGeneticsSyncPacket data,
            HorseEntity mate, HorseGeneticsSyncPacket mateData, String selectorText,
            int selectedIndex, int candidateCount) {
        List<StatLine> lines = new ArrayList<>();
        String mateLabel = candidateCount > 1
                ? "Mate " + (selectedIndex + 1) + "/" + candidateCount : "Mate";
        lines.add(new StatLine(mateLabel,
                candidateCount > 1 ? selectorText : mate.getName().getString(), 0xFFA3D5FF));
        if (data == null || mateData == null) {
            lines.addAll(syncingLines());
            return lines;
        }

        String blocked = pairingBlockReason(horse, data, mate, mateData);
        lines.add(new StatLine("Compatibility", blocked == null ? "Compatible" : blocked,
                blocked == null ? 0xFFA8E6A3 : 0xFFFF8A80));
        if(blocked==null){String ready=MountBreedingPreview.readiness(horse,mate);lines.add(new StatLine("Breeding",ready==null?"Ready to breed":ready,ready==null?0xFFA8E6A3:0xFFB8B8B8));}
        HorseBreedingFocus firstFocus = HorseBreedingFocus.fromId(data.breedingFocus());
        HorseBreedingFocus secondFocus = HorseBreedingFocus.fromId(mateData.breedingFocus());
        addPairGene(lines, "Health", data.geneticHealth(), mateData.geneticHealth(),
                isFocused(firstFocus, secondFocus, 0), false);
        addPairGene(lines, "Speed", data.geneticSpeed(), mateData.geneticSpeed(),
                isFocused(firstFocus, secondFocus, 1), true);
        addPairGene(lines, "Jump", data.geneticJump(), mateData.geneticJump(),
                isFocused(firstFocus, secondFocus, 2), false);
        addPairGene(lines, "Evasion", data.geneticEvasion(), mateData.geneticEvasion(),
                isFocused(firstFocus, secondFocus, 3), false);
        addPairGene(lines, "Endurance", data.geneticEndurance(), mateData.geneticEndurance(),
                isFocused(firstFocus, secondFocus, 4), false);
        addPairGene(lines, "Armor Bonus", data.geneticArmor(), mateData.geneticArmor(),
                isFocused(firstFocus, secondFocus, 5), false);
        if (firstFocus != HorseBreedingFocus.NONE || secondFocus != HorseBreedingFocus.NONE) {
            lines.add(new StatLine("* Focus", "Guaranteed better", 0xFFA8E6A3));
        }
        addLineagePreview(lines, data, mateData, firstFocus, secondFocus);
        addTraitPreview(lines, data.trait(), mateData.trait());
        return lines;
    }
    public static String pairingBlockReason(
            HorseEntity horse, HorseGeneticsSyncPacket data,
            HorseEntity mate, HorseGeneticsSyncPacket mateData) {
        if (horse.isBaby()) return "Current is a foal";
        if (!horse.isTame()) return "Current is untamed";
        if (data.male() == mateData.male()) return "Same sex";
        if (data.breedsRemaining() <= 0) return "No attempts left";
        if (mateData.breedsRemaining() <= 0) return "Mate has no attempts";
        if (data.breedingCooldownTicks() > 0) return "Current cooldown";
        if (mateData.breedingCooldownTicks() > 0) return "Mate cooldown";
        if (areRelated(horse.getUuid(), data.ancestry(), mate.getUuid(), mateData.ancestry())) {
            return "Related bloodlines";
        }
        return null;
    }
    public static boolean areRelated(UUID firstId, List<UUID> firstAncestry,
                                              UUID secondId, List<UUID> secondAncestry) {
        if (firstId.equals(secondId)) return true;
        if (firstAncestry.contains(secondId) || secondAncestry.contains(firstId)) return true;
        for (UUID ancestor : firstAncestry) if (secondAncestry.contains(ancestor)) return true;
        return false;
    }
    public static void addPairGene(List<StatLine> lines, String name,
                                           double first, double second,
                                           boolean guaranteed, boolean precise) {
        double lower = Math.min(first, second);
        double upper = Math.max(first, second);
        String firstValue = precise
                ? String.format(Locale.US, "%.1f", lower * SPEED_TO_BLOCKS_PER_SECOND)
                : format(lower);
        String secondValue = precise
                ? String.format(Locale.US, "%.1f", upper * SPEED_TO_BLOCKS_PER_SECOND)
                : format(upper);
        String values = Math.abs(first - second) <= ZERO_THRESHOLD
                ? firstValue : firstValue + " - " + secondValue;
        lines.add(new StatLine(name + (guaranteed ? " *" : ""), values,
                guaranteed ? 0xFFA8E6A3 : 0xFFB8B8B8));
    }
    public static boolean isFocused(HorseBreedingFocus first,
                                             HorseBreedingFocus second, int gene) {
        return focusIndex(first) == gene || focusIndex(second) == gene;
    }
    public static void addLineagePreview(List<StatLine> lines,
                                                  HorseGeneticsSyncPacket firstData,
                                                  HorseGeneticsSyncPacket secondData,
                                                  HorseBreedingFocus firstFocus,
                                                  HorseBreedingFocus secondFocus) {
        int firstIndex = focusIndex(firstFocus);
        int secondIndex = focusIndex(secondFocus);
        if (firstIndex < 0 && secondIndex < 0) {
            lines.add(new StatLine("Lineage Gain", "Random +0.5-1.5%", 0xFFFFD966));
            return;
        }
        if (firstIndex >= 0 && secondIndex >= 0 && firstIndex != secondIndex) {
            lines.add(new StatLine("Lineage Option", lineageText(
                    firstIndex, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
            lines.add(new StatLine("Or", lineageText(
                    secondIndex, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
            return;
        }
        int target = firstIndex >= 0 ? firstIndex : secondIndex;
        lines.add(new StatLine("Lineage Gain", lineageText(
                target, firstData, secondData, firstFocus, secondFocus), 0xFFFFD966));
    }
    public static String lineageText(int gene, HorseGeneticsSyncPacket first,
                                             HorseGeneticsSyncPacket second,
                                             HorseBreedingFocus firstFocus,
                                             HorseBreedingFocus secondFocus) {
        double better = Math.max(gene(first, gene), gene(second, gene));
        double quality = (better - geneMin(gene))
                / (geneMax(gene) - geneMin(gene));
        double rate = HorseBreedingRules.lineageImprovement(firstFocus, secondFocus, quality);
        return geneName(gene) + " +" + formatPercent(rate * 100.0) + "%";
    }
    public static void addTraitPreview(List<StatLine> lines, String first, String second) {
        if (first.equals(second)) {
            lines.add(new StatLine("Trait",
                    "No Trait".equals(first) ? "No Trait" : first + " 100%", 0xFFA8E6A3));
        } else if ("No Trait".equals(first) || "No Trait".equals(second)) {
            String positive = "No Trait".equals(first) ? second : first;
            lines.add(new StatLine("Trait", positive + " 70%", 0xFFA8E6A3));
            lines.add(new StatLine("No Trait", "30%", 0xFFB8B8B8));
        } else {
            lines.add(new StatLine(first, "45%", 0xFFA8E6A3));
            lines.add(new StatLine(second, "45%", 0xFFA8E6A3));
            lines.add(new StatLine("No Trait", "10%", 0xFFB8B8B8));
        }
        lines.add(new StatLine("Trait Mutation", "3% random", 0xFFA3D5FF));
    }
    public static int focusIndex(HorseBreedingFocus focus) {
        return switch (focus) {
            case HEALTH -> 0;
            case SPEED -> 1;
            case JUMP -> 2;
            case EVASION -> 3;
            case ENDURANCE -> 4;
            case ARMOR -> 5;
            case NONE -> -1;
        };
    }
    public static double gene(HorseGeneticsSyncPacket data, int gene) {
        return switch (gene) {
            case 0 -> data.geneticHealth();
            case 1 -> data.geneticSpeed();
            case 2 -> data.geneticJump();
            case 3 -> data.geneticEvasion();
            case 4 -> data.geneticEndurance();
            default -> data.geneticArmor();
        };
    }
    public static double geneMin(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MIN_HEALTH;
            case 1 -> HorseBreedingRules.MIN_SPEED;
            case 2 -> HorseBreedingRules.MIN_JUMP;
            case 3 -> HorseBreedingRules.MIN_EVASION;
            case 4 -> HorseBreedingRules.MIN_ENDURANCE;
            default -> HorseBreedingRules.MIN_ARMOR;
        };
    }
    public static double geneMax(int gene) {
        return switch (gene) {
            case 0 -> HorseBreedingRules.MAX_HEALTH;
            case 1 -> HorseBreedingRules.MAX_SPEED;
            case 2 -> HorseBreedingRules.MAX_JUMP;
            case 3 -> HorseBreedingRules.MAX_EVASION;
            case 4 -> HorseBreedingRules.MAX_ENDURANCE;
            default -> HorseBreedingRules.MAX_ARMOR;
        };
    }
    public static String geneName(int gene) {
        return switch (gene) {
            case 0 -> "Health";
            case 1 -> "Speed";
            case 2 -> "Jump";
            case 3 -> "Evasion";
            case 4 -> "Endurance";
            default -> "Armor";
        };
    }
    public static String formatPercent(double value) {
        return Math.abs(value - Math.rint(value)) < ZERO_THRESHOLD
                ? Integer.toString((int) Math.rint(value))
                : String.format(Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    public static List<StatLine> syncingLines() {
        return List.of(new StatLine("Status", "Syncing...", 0xFFB8B8B8));
    }
    public static void addTrainingMilestones(List<StatLine> lines, int level) {
        if (level >= 5) lines.add(new StatLine("Lv. 5 Perk", "Trail Training", 0xFFA3D5FF));
        if (level >= 10) lines.add(new StatLine("Lv. 10 Perk", "Second Wind", 0xFFA3D5FF));
        if (level >= 15) lines.add(new StatLine("Lv. 15 Perk", "Efficient Jumper", 0xFFA3D5FF));
        if (level >= 20) lines.add(new StatLine("Lv. 20 Perk", "Sure Stride", 0xFFA3D5FF));
        if (level >= 25) lines.add(new StatLine("Lv. 25 Perk", "Endurance Training", 0xFFA3D5FF));
        if (level >= 30) lines.add(new StatLine("Lv. 30 Perk", "Mastered Gallop", 0xFFA3D5FF));
    }
    public static double getAttribute(HorseEntity horse, RegistryEntry<EntityAttribute> attribute) {
        var instance = horse.getAttributeInstance(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }
    public static double getLivingAttribute(net.minecraft.entity.LivingEntity entity,
                                                     RegistryEntry<EntityAttribute> attribute) {
        var instance = entity.getAttributeInstance(attribute);
        return instance == null ? 0.0 : instance.getValue();
    }
    public static void addIfNonZero(List<StatLine> lines, String name, double value, String suffix) {
        if (Math.abs(value) > ZERO_THRESHOLD) {
            lines.add(new StatLine(name, format(value) + suffix, 0xFFB8B8B8));
        }
    }
    public static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < ZERO_THRESHOLD) return Integer.toString((int) Math.rint(value));
        if (Math.abs(value) < 10.0) return String.format(Locale.US, "%.2f", value);
        return String.format(Locale.US, "%.1f", value);
    }
    public static String formatTime(long ticks) {
        if (ticks <= 0) return "Ready";
        long seconds = ticks / 20;
        return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60);
    }
    public static int gradeColor(String grade) {
        if (grade == null || grade.isEmpty()) return 0xFFFFFFFF;
        return switch (grade.charAt(0)) {
            case 'S' -> 0xFFFF70FF;
            case 'A' -> 0xFFFFD966;
            case 'B' -> 0xFF70D6FF;
            case 'C' -> 0xFF9BE28F;
            case 'D' -> 0xFFFF8A80;
            default -> 0xFFFFFFFF;
        };
    }
    public static String trimToWidth(TextRenderer renderer, String value, int maxWidth) {
        if (renderer.getWidth(value) <= maxWidth) return value;
        return renderer.trimToWidth(value, Math.max(1, maxWidth - renderer.getWidth("..."))) + "...";
    }

}

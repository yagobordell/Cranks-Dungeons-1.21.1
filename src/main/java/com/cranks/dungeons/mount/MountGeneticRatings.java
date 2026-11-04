package com.cranks.dungeons.mount;

import com.cranks.dungeons.horse.HorseBreedingRules;

/** Per-gene potential grades, calibrated to each mount's natural rolls and breeding rules. */
public final class MountGeneticRatings {
    private static final String[] GRADES = {
            "D-", "D", "D+", "C-", "C", "C+", "B-", "B", "B+",
            "A-", "A", "A+", "S-", "S", "S+"
    };

    // Same-kind mount breeding adds an average 1.25% of the gene range before
    // mutation. The net expected signed mutation gain is another 0.12%.
    private static final double MOUNT_EXPECTED_GAIN_PER_GENERATION = 0.0137;

    private static final GeneRange[][] RANGES = {
            // Horse: health bonus, speed, jump, evasion, endurance, armor bonus.
            {
                    new GeneRange(HorseBreedingRules.MIN_HEALTH, HorseBreedingRules.MIN_HEALTH, 25.0,
                            HorseBreedingRules.MAX_HEALTH, Growth.HORSE_LINEAGE),
                    new GeneRange(HorseBreedingRules.MIN_SPEED, HorseBreedingRules.MIN_SPEED, 0.3375,
                            HorseBreedingRules.MAX_SPEED, Growth.HORSE_LINEAGE),
                    new GeneRange(HorseBreedingRules.MIN_JUMP, HorseBreedingRules.MIN_JUMP, 1.0,
                            HorseBreedingRules.MAX_JUMP, Growth.HORSE_LINEAGE),
                    new GeneRange(HorseBreedingRules.MIN_EVASION, HorseBreedingRules.MIN_EVASION, 40.0,
                            HorseBreedingRules.MAX_EVASION, Growth.HORSE_LINEAGE),
                    new GeneRange(HorseBreedingRules.MIN_ENDURANCE, 70.0, 110.0,
                            HorseBreedingRules.MAX_ENDURANCE, Growth.HORSE_LINEAGE),
                    new GeneRange(HorseBreedingRules.MIN_ARMOR, HorseBreedingRules.MIN_ARMOR, 30.0,
                            HorseBreedingRules.MAX_ARMOR, Growth.HORSE_LINEAGE)
            },
            // Donkey: health bonus, travel speed, endurance reserve, trail skill, pack capacity, armor bonus.
            {
                    new GeneRange(0.0, 0.0, 25.0, 50.0, Growth.MOUNT),
                    new GeneRange(0.15, 0.15, 0.24, 0.30, Growth.MOUNT),
                    new GeneRange(70.0, 70.0, 120.0, 160.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 100.0, 100.0, Growth.MOUNT),
                    new GeneRange(15.0, 15.0, 21.0, 33.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 24.0, 66.0, Growth.MOUNT)
            },
            // Mule: health bonus, travel speed, jump, endurance, pack capacity, armor bonus.
            // Mules are sterile, so their rating uses the full hybrid range at every generation.
            {
                    new GeneRange(0.0, 0.0, 31.25, 56.25, Growth.NONE),
                    new GeneRange(0.18, 0.18, 0.28, 0.34, Growth.NONE),
                    new GeneRange(0.45, 0.45, 0.85, 1.05, Growth.NONE),
                    new GeneRange(75.0, 75.0, 130.0, 155.0, Growth.NONE),
                    new GeneRange(15.0, 15.0, 18.0, 24.0, Growth.NONE),
                    new GeneRange(0.0, 0.0, 36.0, 90.0, Growth.NONE)
            },
            // Camel: health bonus, cruise speed, dash power, endurance, dash efficiency, armor bonus.
            {
                    new GeneRange(0.0, 0.0, 37.5, 68.75, Growth.MOUNT),
                    new GeneRange(0.14, 0.14, 0.22, 0.28, Growth.MOUNT),
                    new GeneRange(0.45, 0.45, 0.75, 0.90, Growth.MOUNT),
                    new GeneRange(75.0, 75.0, 135.0, 170.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 100.0, 100.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 48.0, 120.0, Growth.MOUNT)
            },
            // Strider: health bonus, lava speed, heat capacity, heat retention, stability, armor bonus.
            {
                    new GeneRange(0.0, 0.0, 25.0, 50.0, Growth.MOUNT),
                    new GeneRange(0.18, 0.18, 0.30, 0.38, Growth.MOUNT),
                    new GeneRange(75.0, 75.0, 135.0, 170.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 100.0, 100.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 100.0, 100.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 30.0, 72.0, Growth.MOUNT)
            },
            // Llama: health bonus, base speed, pack instinct, load strength, armor bonus.
            {
                    new GeneRange(0.0, 0.0, 25.0, 50.0, Growth.MOUNT),
                    new GeneRange(0.15, 0.15, 0.24, 0.30, Growth.MOUNT),
                    new GeneRange(0.02, 0.02, 0.08, 0.12, Growth.MOUNT),
                    new GeneRange(1.0, 1.0, 3.0, 5.0, Growth.MOUNT),
                    new GeneRange(0.0, 0.0, 30.0, 72.0, Growth.MOUNT)
            }
    };

    private MountGeneticRatings() {
    }

    /**
     * Returns a gene's rank relative to its natural roll range and modeled
     * breeding progression for its current generation.
     */
    public static String grade(MountKind kind, int geneIndex, double value, int generation) {
        return gradeForQuality(quality(kind, geneIndex, value, generation));
    }

    public static double quality(MountKind kind, int geneIndex, double value, int generation) {
        GeneRange range = range(kind, geneIndex);
        if (!Double.isFinite(value)) return 0.0;
        double stageMaximum = stageMaximum(kind, range, generation);
        double span = stageMaximum - range.naturalMinimum;
        if (span <= 0.0) return value >= stageMaximum ? 1.0 : 0.0;
        return clamp((value - range.naturalMinimum) / span, 0.0, 1.0);
    }

    public static String overall(MountKind kind, double[] genes, int generation) {
        if (genes == null || genes.length == 0) return gradeForQuality(0.0);
        double total = 0.0;
        int count = Math.min(genes.length, RANGES[kind.ordinal()].length);
        for (int i = 0; i < count; i++) total += quality(kind, i, genes[i], generation);
        return gradeForQuality(count == 0 ? 0.0 : total / count);
    }

    /** Stable lower and natural-roll bounds shared with the mount initializer and breeder. */
    public static double minimum(MountKind kind, int geneIndex) {
        return range(kind, geneIndex).naturalMinimum;
    }

    public static double breedingMinimum(MountKind kind, int geneIndex) {
        return range(kind, geneIndex).breedingMinimum;
    }

    public static double naturalMaximum(MountKind kind, int geneIndex) {
        return range(kind, geneIndex).naturalMaximum;
    }

    /** Breeding cap used by MountManager's inheritance implementation. */
    public static double breedingMaximum(MountKind kind, int geneIndex) {
        return range(kind, geneIndex).breedingMaximum;
    }

    public static double[][] breedingRanges(MountKind kind) {
        GeneRange[] ranges = RANGES[kind.ordinal()];
        double[][] result = new double[ranges.length][2];
        for (int i = 0; i < ranges.length; i++) {
            result[i][0] = ranges[i].breedingMinimum;
            result[i][1] = ranges[i].breedingMaximum;
        }
        return result;
    }

    public static int geneCount(MountKind kind) {
        if (kind == MountKind.LLAMA) return 5;
        return kind != null && kind.hasArmorGene() ? 6 : 5;
    }

    public static String gradeForQuality(double quality) {
        double bounded = clamp(quality, 0.0, 1.0);
        int index = Math.min(GRADES.length - 1, (int) (bounded * GRADES.length));
        return GRADES[index];
    }

    private static GeneRange range(MountKind kind, int geneIndex) {
        if (kind == null || kind.ordinal() >= RANGES.length
                || geneIndex < 0 || geneIndex >= RANGES[kind.ordinal()].length) {
            throw new IllegalArgumentException("No genetic rating range for " + kind + " gene " + geneIndex);
        }
        return RANGES[kind.ordinal()][geneIndex];
    }

    private static double stageMaximum(MountKind kind, GeneRange range, int generation) {
        if (range.growth == Growth.NONE) return range.breedingMaximum;
        double maximum = range.naturalMaximum;
        double span = range.breedingMaximum - range.breedingMinimum;
        int stages = Math.max(0, generation);
        for (int stage = 1; stage <= stages && maximum < range.breedingMaximum; stage++) {
            double gain = MOUNT_EXPECTED_GAIN_PER_GENERATION;
            if (range.growth == Growth.HORSE_LINEAGE) {
                double quality = clamp((maximum - range.breedingMinimum) / span, 0.0, 1.0);
                double focusedLineageGain = quality < 0.25 ? 0.03
                        : quality < 0.60 ? 0.025
                        : quality < 0.85 ? 0.02 : 0.01;
                double expectedSignedMutationGain = 0.03
                        * (0.035 + Math.min(0.02, stage * 0.001));
                gain = focusedLineageGain + expectedSignedMutationGain;
            }
            maximum = Math.min(range.breedingMaximum, maximum + span * gain);
        }
        return maximum;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum Growth {
        NONE,
        MOUNT,
        HORSE_LINEAGE
    }

    private record GeneRange(double breedingMinimum, double naturalMinimum, double naturalMaximum,
                             double breedingMaximum, Growth growth) {
    }
}

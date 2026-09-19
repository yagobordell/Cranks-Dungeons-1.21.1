package com.cranks.dungeons.horse;

/** Pure breeding and training rules shared by server logic, client previews, and tests. */
public final class HorseBreedingRules {
    public static final double MIN_HEALTH = 50.0;
    public static final double MAX_HEALTH = 90.0;
    public static final double MIN_SPEED = 0.1125;
    public static final double MAX_SPEED = 0.42;
    public static final double MIN_JUMP = 0.4;
    public static final double MAX_JUMP = 1.2;
    public static final double MIN_EVASION = 0.0;
    public static final double MAX_EVASION = 200.0;
    public static final double MIN_ENDURANCE = 50.0;
    public static final double MAX_ENDURANCE = 200.0;
    public static final int HEALTH_MUTATION = 1;
    public static final int SPEED_MUTATION = 1 << 1;
    public static final int JUMP_MUTATION = 1 << 2;
    public static final int EVASION_MUTATION = 1 << 3;
    public static final int ENDURANCE_MUTATION = 1 << 4;

    private HorseBreedingRules() {
    }

    public static double lineageImprovement(HorseBreedingFocus first, HorseBreedingFocus second,
                                            double geneQuality) {
        double matchingRate;
        if (geneQuality < 0.25) matchingRate = 0.03;
        else if (geneQuality < 0.60) matchingRate = 0.025;
        else if (geneQuality < 0.85) matchingRate = 0.02;
        else matchingRate = 0.01;

        boolean firstSelected = first != null && first != HorseBreedingFocus.NONE;
        boolean secondSelected = second != null && second != HorseBreedingFocus.NONE;
        if (firstSelected && first == second) return matchingRate;
        if (firstSelected || secondSelected) return matchingRate * 0.75;
        return matchingRate * 0.50;
    }

    public static int experienceForNextLevel(int level, int maximumLevel) {
        if (level >= maximumLevel) return 0;
        int original = 100 + level * 50;
        if (level >= 20) return original;
        double progress = Math.max(0.0, level - 1.0) / 19.0;
        double scale = 0.60 + 0.40 * progress;
        return Math.max(10, (int) Math.round(original * scale / 10.0) * 10);
    }

    public static int travelExperience(boolean galloping) {
        return galloping ? 4 : 2;
    }

    public static int longJumpExperience(double horizontalDistance) {
        if (horizontalDistance < 4.0) return 0;
        return Math.min(10, Math.max(0, ((int) Math.floor(horizontalDistance / 2.0) - 1) * 2));
    }
}

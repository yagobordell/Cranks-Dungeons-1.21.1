package com.cranks.dungeons.damage;

import java.util.ArrayList;
import java.util.List;

public class DamageNumberManager {
    private static final List<DamageNumber> activeNumbers = new ArrayList<>();

    public static void addDamageNumber(DamageNumber number) {
        // Check if this is a drowning damage number (cyan-blue color 0x0088AA)
        boolean isDrowning = (number.color == 0x0088AA);
        
        // If it's a drowning number, always use row 0 (don't stack vertically)
        if (isDrowning) {
            number.setRowIndex(0);
            activeNumbers.add(number);
            return;
        }
        // We no longer manually stack rows here because it conflicts with the float-up animation.
        // Instead, we let numbers naturally stagger based on their spawn time.
        // Drowning still uses row 0 always.
        if (isDrowning) {
            number.setRowIndex(0);
        }
        
        activeNumbers.add(number);
    }

    public static List<DamageNumber> getActiveNumbers() {
        return activeNumbers;
    }

    public static void tick() {
        activeNumbers.removeIf(DamageNumber::isExpired);
    }

    public static void clear() {
        activeNumbers.clear();
    }
}
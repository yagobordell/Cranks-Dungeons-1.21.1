package com.cranks.dungeons.damage;

import java.util.ArrayList;
import java.util.List;

public class DamageNumberManager {
    private static final List<DamageNumber> activeNumbers = new ArrayList<>();

    public static void addDamageNumber(DamageNumber number) {
        // Automatically adjust rowIndex based on existing numbers near this one
        int maxRow = -1;
        long now = System.currentTimeMillis();
        for (DamageNumber existing : activeNumbers) {
            if (now - existing.spawnTime < 500) { // Only stack with recent numbers
                double distSq = existing.position.squaredDistanceTo(number.position);
                if (distSq < 1.0) { // Close enough to consider stacking
                    maxRow = Math.max(maxRow, existing.rowIndex);
                }
            }
        }
        
        // If we found existing numbers, place this one above them
        // If not, use its original rowIndex
        int finalRow = (maxRow == -1) ? number.rowIndex : maxRow + 1;
        
        // We need to make rowIndex non-final to change it here, 
        // or recreate the number. non-final is easier.
        number.setRowIndex(finalRow);
        
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
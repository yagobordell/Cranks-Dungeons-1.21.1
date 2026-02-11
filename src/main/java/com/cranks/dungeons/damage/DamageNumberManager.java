package com.cranks.dungeons.damage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageNumberManager {
    private static final List<DamageNumber> activeNumbers = new ArrayList<>();

    public static void addDamageNumber(DamageNumber number) {
        activeNumbers.add(number);
    }

    public static List<DamageNumber> getActiveNumbers() {
        return activeNumbers;
    }

    public static void tick() {
        Iterator<DamageNumber> iterator = activeNumbers.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired()) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        activeNumbers.clear();
    }
}
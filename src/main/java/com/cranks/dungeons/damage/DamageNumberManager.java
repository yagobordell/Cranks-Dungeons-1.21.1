package com.cranks.dungeons.damage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DamageNumberManager {
    private static final List<DamageNumber> activeNumbers = new ArrayList<>();

    private record VictimKey(UUID uuid, int entityId) {}

    public static void addDamageNumber(DamageNumber number) {
        // Suppress 'crazy values' (e.g. from revival mod death triggers)
        if (number.damage > 999999 && !number.isHeal) {
            return;
        }

        activeNumbers.add(number);
        assignStackRows();
    }

    public static List<DamageNumber> getActiveNumbers() {
        return activeNumbers;
    }

    public static void tick() {
        activeNumbers.removeIf(DamageNumber::isExpired);
        assignStackRows();
    }

    public static void clear() {
        activeNumbers.clear();
    }

    private static void assignStackRows() {
        Map<VictimKey, Integer> nextRows = new HashMap<>();
        for (DamageNumber number : activeNumbers) {
            VictimKey key = getVictimKey(number);
            if (key == null) {
                number.setRowIndex(0);
                continue;
            }

            int row = nextRows.getOrDefault(key, 0);
            number.setRowIndex(row);
            nextRows.put(key, row + 1);
        }
    }

    private static VictimKey getVictimKey(DamageNumber number) {
        if (number.victimUuid != null) {
            return new VictimKey(number.victimUuid, -1);
        }
        return number.victimEntityId >= 0 ? new VictimKey(null, number.victimEntityId) : null;
    }
}

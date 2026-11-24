package com.cranks.dungeons.client.compendium;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;

/** Follow the distance map back to the current allocation, including any draft. */
final class TreePath {
    private TreePath() {}

    static void traceHover(Set<String> result, String start, Map<String, Set<String>> neighbors,
                           Map<String, Integer> distances) {
        ArrayDeque<String> remaining = new ArrayDeque<>();
        if (distances.containsKey(start) && result.add(start)) remaining.add(start);
        while (!remaining.isEmpty()) {
            String current = remaining.removeFirst();
            int nextDistance = distances.get(current) - 1;
            for (String neighbor : neighbors.getOrDefault(current, Set.of())) {
                if (distances.getOrDefault(neighbor, -2) == nextDistance && result.add(neighbor)) {
                    remaining.add(neighbor);
                }
            }
        }
    }

}

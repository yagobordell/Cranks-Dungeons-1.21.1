package com.cranks.dungeons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class TreeUnlockPlanner {
    public enum Failure {
        NONE,
        NOT_ENOUGH_POINTS,
        DISCONNECTED
    }

    public record Result(List<String> unlockOrder, Failure failure) {
        public boolean successful() {
            return failure == Failure.NONE;
        }
    }

    private TreeUnlockPlanner() {}

    public static Result plan(Set<String> currentlyUnlocked,
                              Collection<String> requestedNodes,
                              int availablePoints,
                              Function<String, ? extends Collection<String>> neighborProvider) {
        Set<String> simulatedUnlocked = new HashSet<>(currentlyUnlocked);
        Set<String> remaining = new LinkedHashSet<>(requestedNodes);
        remaining.removeAll(simulatedUnlocked);

        if (remaining.size() > availablePoints) {
            return new Result(List.of(), Failure.NOT_ENOUGH_POINTS);
        }

        List<String> order = new ArrayList<>(remaining.size());
        boolean progressed = true;
        while (!remaining.isEmpty() && progressed) {
            progressed = false;
            var iterator = remaining.iterator();
            while (iterator.hasNext()) {
                String nodeId = iterator.next();
                Collection<String> neighbors = neighborProvider.apply(nodeId);
                if (neighbors != null && neighbors.stream().anyMatch(simulatedUnlocked::contains)) {
                    iterator.remove();
                    simulatedUnlocked.add(nodeId);
                    order.add(nodeId);
                    progressed = true;
                }
            }
        }

        if (!remaining.isEmpty()) {
            return new Result(List.of(), Failure.DISCONNECTED);
        }
        return new Result(List.copyOf(order), Failure.NONE);
    }
}

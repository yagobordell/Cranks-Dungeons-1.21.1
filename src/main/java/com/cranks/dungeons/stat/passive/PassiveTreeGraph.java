package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class PassiveTreeGraph {
    private PassiveTreeGraph() {}

    static Map<String, Set<String>> index(Map<String, PassiveNode> nodes, Collection<String> starts, String treeName) {
        Map<String, Set<String>> edges = new HashMap<>();
        nodes.keySet().forEach(id -> edges.put(id, new HashSet<>()));
        for (PassiveNode node : nodes.values()) {
            for (String raw : node.connections()) {
                String other = raw.trim();
                if (!nodes.containsKey(other)) continue;
                edges.get(node.id()).add(other);
                edges.get(other).add(node.id());
            }
        }

        Set<String> reached = new HashSet<>();
        var queue = new ArrayDeque<String>();
        for (String start : starts) {
            if (edges.containsKey(start)) queue.add(start);
            else CranksDungeons.LOGGER.warn("[{}] Missing start node '{}'.", treeName, start);
        }
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            if (reached.add(id)) queue.addAll(edges.get(id));
        }
        if (reached.size() < nodes.size()) {
            CranksDungeons.LOGGER.warn("[{}] {} nodes are disconnected from the starting nodes.",
                    treeName, nodes.size() - reached.size());
        }
        edges.replaceAll((id, neighbors) -> Set.copyOf(neighbors));
        return Map.copyOf(edges);
    }
}

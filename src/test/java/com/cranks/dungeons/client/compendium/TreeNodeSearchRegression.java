package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.stat.passive.PassiveNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TreeNodeSearchRegression {
    private TreeNodeSearchRegression() {}

    public static void run() {
        List<PassiveNode> nodes = List.of(
                new PassiveNode("defense_17", "§6Régalia", "Made for the front line.", 0, 0,
                        List.of(), Map.of(), null, "diamond", false, 0),
                new PassiveNode("defense_18", "§6Régalia", "Made for the front line.", 4, 2,
                        List.of(), Map.of(), null, "diamond", false, 0),
                new PassiveNode("hidden_path", "", "A route through the front line.", 0, 0,
                        List.of(), Map.of(), null, "circle", false, 0),
                new PassiveNode("choose_4", "", "", 0, 0,
                        List.of(), Map.of(), null, "square", true, 5));
        var linkedNote=new PassiveNode("note","","Read [[article/stats|the guide]].",0,0,List.of(),Map.of(),null,"square",false,0);
        require(com.cranks.dungeons.client.guide.TreeNodeDescriptions.plain(linkedNote,false).equals("Read the guide."),"authored note link markup leaked into search");
        TreeNodeSearchIndex index = new TreeNodeSearchIndex(nodes, true);
        Set<String> duplicates = Set.of("defense_17", "defense_18");
        require(index.find("REGALIA").equals(duplicates), "a repeated formatted or accented title was skipped");
        require(index.find("regalia front line").equals(duplicates), "title and description terms did not combine");
        require(index.find("hidden_path").equals(Set.of("hidden_path")), "unnamed node could not be found by ID");
        require(index.find("dexterity").equals(Set.of("choose_4")), "attribute choice could not be found");
        require(index.find(" ").isEmpty(), "empty search kept matching nodes highlighted");
        require(new TreeNodeSearchIndex(nodes, false).find("dexterity").isEmpty(),
                "world tree search included passive-only choices");

        Map<String, Set<String>> neighbors = Map.of(
                "root", Set.of("left", "right"),
                "left", Set.of("root", "end"),
                "right", Set.of("root", "end"),
                "end", Set.of("left", "right"));
        Map<String, Integer> distances = Map.of("root", 0, "left", 1, "right", 1, "end", 2);
        Set<String> path = new HashSet<>();
        TreePath.traceHover(path, "end", neighbors, distances);
        require(path.equals(neighbors.keySet()), "hover no longer traces both short routes");
        path.clear();
        TreePath.traceHover(path, "island", neighbors, distances);
        require(path.isEmpty(), "hover traced a route for a disconnected node");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

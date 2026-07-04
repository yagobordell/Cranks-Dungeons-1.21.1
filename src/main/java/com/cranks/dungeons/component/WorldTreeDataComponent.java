package com.cranks.dungeons.component;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import java.util.Set;

public interface WorldTreeDataComponent extends Component, CopyableComponent<WorldTreeDataComponent> {
    int getUnspentPoints();
    void setUnspentPoints(int points);
    void addUnspentPoints(int points);

    Set<String> getUnlockedNodes();
    void unlockNode(String nodeId);
    boolean hasNode(String nodeId);

    int getGatheringXp();
    int getGatheringLevel();
    void addGatheringXp(int xp);
    int getNextLevelXp();
    void setGatheringXp(int xp, int level);
    
    String getNodeChoice(String nodeId);
    java.util.Map<String, String> getAllNodeChoices();
    void setNodeChoice(String nodeId, String choice);
}

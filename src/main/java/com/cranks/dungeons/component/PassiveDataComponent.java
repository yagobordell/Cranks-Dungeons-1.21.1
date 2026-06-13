package com.cranks.dungeons.component;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import java.util.Set;

public interface PassiveDataComponent extends Component, CopyableComponent<PassiveDataComponent> {
    String getPlayerClass();
    void setPlayerClass(String playerClass);

    int getUnspentPoints();
    void setUnspentPoints(int points);
    void addUnspentPoints(int points);

    Set<String> getUnlockedNodes();
    void unlockNode(String nodeId);
    boolean hasNode(String nodeId);

    java.util.Map<String, String> getNodeChoices();
    void setNodeChoice(String nodeId, String choice);
    String getNodeChoice(String nodeId);
    
    // Called to retroactively grant points if the user was leveled before the update
    void checkRetroactivePoints(int currentLevel);
}

package com.cranks.dungeons.component;

import org.ladysnake.cca.api.v3.component.Component;
import java.util.Set;

public interface PassiveDataComponent extends Component {
    String getPlayerClass();
    void setPlayerClass(String playerClass);

    int getUnspentPoints();
    void setUnspentPoints(int points);
    void addUnspentPoints(int points);

    Set<String> getUnlockedNodes();
    void unlockNode(String nodeId);
    boolean hasNode(String nodeId);
    
    // Called to retroactively grant points if the user was leveled before the update
    void checkRetroactivePoints(int currentLevel);
}

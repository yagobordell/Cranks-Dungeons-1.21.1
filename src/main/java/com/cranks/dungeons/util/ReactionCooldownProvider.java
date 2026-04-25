package com.cranks.dungeons.util;

public interface ReactionCooldownProvider {
    boolean cranks$canTriggerReaction(String type);
    void cranks$setReactionCooldown(String type, int ticks);
}

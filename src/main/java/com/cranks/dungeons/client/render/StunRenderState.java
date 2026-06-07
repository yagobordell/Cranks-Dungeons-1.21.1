package com.cranks.dungeons.client.render;

public interface StunRenderState {
    boolean cranks$isFrozen();
    void cranks$setFrozen(boolean frozen);
    
    boolean cranks$isElectrocuted();
    void cranks$setElectrocuted(boolean electrocuted);
    
    float cranks$getWidth();
    void cranks$setWidth(float width);
    
    float cranks$getHeight();
    void cranks$setHeight(float height);
}

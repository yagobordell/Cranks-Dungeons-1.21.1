package com.cranks.dungeons.mixin.client;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements com.cranks.dungeons.client.render.StunRenderState {
    @Unique
    private boolean cranks$isFrozen;
    @Unique
    private boolean cranks$isElectrocuted;
    @Unique
    private float cranks$entityWidth;
    @Unique
    private float cranks$entityHeight;

    @Override
    public boolean cranks$isFrozen() {
        return cranks$isFrozen;
    }

    @Override
    public void cranks$setFrozen(boolean frozen) {
        this.cranks$isFrozen = frozen;
    }

    @Override
    public boolean cranks$isElectrocuted() {
        return cranks$isElectrocuted;
    }

    @Override
    public void cranks$setElectrocuted(boolean electrocuted) {
        this.cranks$isElectrocuted = electrocuted;
    }
    
    @Override
    public float cranks$getWidth() {
        return cranks$entityWidth;
    }
    
    @Override
    public void cranks$setWidth(float width) {
        this.cranks$entityWidth = width;
    }
    
    @Override
    public float cranks$getHeight() {
        return cranks$entityHeight;
    }
    
    @Override
    public void cranks$setHeight(float height) {
        this.cranks$entityHeight = height;
    }
}

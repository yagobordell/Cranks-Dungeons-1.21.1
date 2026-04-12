package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.FrozenRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements FrozenRenderState {
    @Unique
    private boolean cranks$frozen;

    @Override
    public void cranks$setFrozen(boolean frozen) {
        this.cranks$frozen = frozen;
    }

    @Override
    public boolean cranks$isFrozen() {
        return this.cranks$frozen;
    }
}

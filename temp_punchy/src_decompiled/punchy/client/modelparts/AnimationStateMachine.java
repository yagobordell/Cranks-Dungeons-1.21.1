/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package punchy.client.modelparts;

import net.minecraft.class_310;
import punchy.client.animation.ItemAnimationManager;

final class AnimationStateMachine {
    AnimationStateMachine() {
    }

    void tick(class_310 client) {
        ItemAnimationManager.tick(client);
    }
}


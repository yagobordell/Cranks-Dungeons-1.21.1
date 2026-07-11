/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1688$class_1689
 *  net.minecraft.class_1808
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1688;
import net.minecraft.class_1808;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1808.class})
public interface MinecartItemAccessor {
    @Accessor(value="field_8897")
    public class_1688.class_1689 punchy$getType();
}


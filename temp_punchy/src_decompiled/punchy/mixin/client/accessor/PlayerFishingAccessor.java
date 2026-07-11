/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1536
 *  net.minecraft.class_1657
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1536;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1657.class})
public interface PlayerFishingAccessor {
    @Accessor(value="field_7513")
    public class_1536 punchy$getFishing();
}


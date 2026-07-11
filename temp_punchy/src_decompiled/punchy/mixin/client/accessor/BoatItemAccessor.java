/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1690$class_1692
 *  net.minecraft.class_1749
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1690;
import net.minecraft.class_1749;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1749.class})
public interface BoatItemAccessor {
    @Accessor(value="field_7902")
    public class_1690.class_1692 punchy$getType();

    @Accessor(value="field_38211")
    public boolean punchy$hasChest();
}


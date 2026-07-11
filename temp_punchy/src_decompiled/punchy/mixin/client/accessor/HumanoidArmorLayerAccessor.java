/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1059
 *  net.minecraft.class_572
 *  net.minecraft.class_970
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1059;
import net.minecraft.class_572;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_970.class})
public interface HumanoidArmorLayerAccessor {
    @Accessor(value="field_4830")
    public class_572<?> punchy$getInnerModel();

    @Accessor(value="field_4831")
    public class_572<?> punchy$getOuterModel();

    @Accessor(value="field_42074")
    public class_1059 punchy$getArmorTrimAtlas();
}


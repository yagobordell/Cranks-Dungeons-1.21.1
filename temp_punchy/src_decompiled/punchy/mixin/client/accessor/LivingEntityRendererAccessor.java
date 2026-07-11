/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package punchy.mixin.client.accessor;

import java.util.List;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_922.class})
public interface LivingEntityRendererAccessor {
    @Accessor(value="field_4738")
    public List<Object> punchy$getLayers();
}


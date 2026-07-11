/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_759
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_759.class})
public interface ItemInHandRendererInvoker {
    @Invoker(value="method_3219")
    public void punchy$renderPlayerArm(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

    @Invoker(value="method_3223")
    public void punchy$renderMap(class_4587 var1, class_4597 var2, int var3, class_1799 var4);
}


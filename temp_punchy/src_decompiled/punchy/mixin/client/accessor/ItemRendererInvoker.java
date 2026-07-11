/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1087
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_918
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package punchy.mixin.client.accessor;

import net.minecraft.class_1087;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_918;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_918.class})
public interface ItemRendererInvoker {
    @Invoker(value="method_23182")
    public void punchy$renderModelLists(class_1087 var1, class_1799 var2, int var3, int var4, class_4587 var5, class_4588 var6);
}


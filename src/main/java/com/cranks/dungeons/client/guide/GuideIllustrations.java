package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class GuideIllustrations {
    private GuideIllustrations() {}
    public static ItemStack stack(String key){
        String[] variant=key.split("~",2);Identifier id=Identifier.tryParse(variant[0]);
        if(id==null||!Registries.ITEM.containsId(id))return ItemStack.EMPTY;
        var result=Registries.ITEM.get(id).getDefaultStack();
        if(variant.length>1){Identifier potion=Identifier.tryParse(variant[1]);if(potion!=null)Registries.POTION.getEntry(potion).ifPresent(entry->result.set(DataComponentTypes.POTION_CONTENTS,new PotionContentsComponent(entry)));}
        return result;
    }
    public static void draw(DrawContext context,String key,int x,int y,int width){
        draw(context,key,x,y,width,stack(key).getName().getString());
    }
    public static String caption(GuideDocument.Block block){
        return block.style()!=null&&block.style().startsWith("caption=")?block.style().substring(8):stack(block.value()).getName().getString();
    }
    public static void draw(DrawContext context,String key,int x,int y,int width,String caption){
        var stack=stack(key);if(stack.isEmpty())return;var font=MinecraftClient.getInstance().textRenderer;
        CompendiumStyle.drawInsetPanel(context,x,y,40,40);
        context.getMatrices().push();context.getMatrices().translate(x+4,y+4,0);context.getMatrices().scale(2,2,1);
        context.drawItem(stack,0,0);context.getMatrices().pop();
        context.drawText(font,font.trimToWidth(caption,Math.max(20,width-52)),x+50,y+15,CompendiumStyle.TEXT,false);
    }
}

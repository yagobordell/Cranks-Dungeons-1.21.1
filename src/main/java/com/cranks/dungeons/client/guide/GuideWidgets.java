package com.cranks.dungeons.client.guide;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class GuideWidgets {
    private GuideWidgets(){}
    public static void header(DrawContext context,TextRenderer font,Text title,int x,int y,int room,int color,GuideTarget target){
        String label=font.trimToWidth(title.getString(),Math.max(12,room));
        context.drawText(font,label,x,y,color,false);
        GuideInteraction.region(x-2,y-3,Math.min(room,font.getWidth(label))+4,14,target);
    }
}

package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

public final class GuideNodeTooltip {
    private GuideNodeTooltip(){}
    public static List<Text> of(PassiveNode node,String title,boolean world){
        if(title==null||net.minecraft.util.Formatting.strip(title).isBlank())title=world?"World Tree Node":"Passive Skill Node";
        var lines=new ArrayList<Text>();lines.add(GuideText.link(Text.literal(title),GuideTarget.article(world?"world_tree":"passive_tree")));
        var font=net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        int wrapWidth=Math.max(80,Math.min(360,net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth()-40));
        for(var description:TreeNodeDescriptions.lines(node,world)){
            for(var wrapped:font.getTextHandler().wrapLines(description.copy().formatted(Formatting.GRAY),wrapWidth,net.minecraft.text.Style.EMPTY)){
                var rich=Text.empty();
                wrapped.visit((style,value)->{rich.append(Text.literal(value).setStyle(style));return java.util.Optional.empty();},net.minecraft.text.Style.EMPTY);
                lines.add(rich);
            }
        }
        return lines;
    }
}

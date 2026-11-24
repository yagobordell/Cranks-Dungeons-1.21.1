package com.cranks.dungeons.client.tooltip;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Wrap logical text before conversion to tooltip components, preserving styles and guide links. */
public final class TooltipWrapping {
    private TooltipWrapping() {}
    public static List<Text> fit(TextRenderer font,List<Text> lines,int width){
        if(lines.isEmpty())return lines;
        String marker=lines.getFirst().getStyle().getInsertion();
        if(marker!=null&&marker.startsWith(TooltipLines.MARKER))return lines; // Equipment cards own their layout.
        int available=Math.max(40,width);
        if(lines.stream().noneMatch(line->font.getWidth(line)>available))return lines;
        var result=new ArrayList<Text>();
        for(var line:lines){
            if(font.getWidth(line)<=available){result.add(line);continue;}
            for(var fragment:font.getTextHandler().wrapLines(line,available,Style.EMPTY)){
                var rich=Text.empty();
                fragment.visit((style,value)->{rich.append(Text.literal(value).setStyle(style));return Optional.empty();},Style.EMPTY);
                result.add(rich);
            }
        }
        return List.copyOf(result);
    }
}

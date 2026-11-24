package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.stat.passive.NodeBonusFormat;
import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/** Live descriptions shared by hover help, authoring previews and tree search. */
public final class TreeNodeDescriptions {
    private TreeNodeDescriptions() {}
    public static boolean automatic(PassiveNode node) { return node.isAttributeSelection() || !node.modifiers().isEmpty(); }
    private record Bonus(String value, String id) {}
    private static List<Bonus> bonuses(PassiveNode node, boolean world) {
        var result=new ArrayList<Bonus>();
        if(node.isAttributeSelection()&&!world){
            for(String primary:List.of("strength","dexterity","intelligence")){
                String id="cranks-dungeons:"+primary;
                double value=node.modifiers().entrySet().stream().filter(e->e.getKey().getIdAsString().equals(id))
                        .mapToDouble(java.util.Map.Entry::getValue).findFirst().orElse(node.attributeSelectionAmount());
                if(Double.isFinite(value))result.add(new Bonus(NodeBonusFormat.format(id,value),id));
            }
            return result;
        }
        for(var entry:node.modifiers().entrySet()){
            double value=entry.getValue();if(!Double.isFinite(value))continue;
            String id=entry.getKey().getIdAsString(),description=NodeBonusFormat.format(id,value);
            if(id.contains(":keystone_")||id.endsWith(":ailment_threshold_from_boots_lowest_armor_evasion"))
                description+=": "+GuideStatText.description(id);
            result.add(new Bonus(description,id));
        }
        return result;
    }
    public static List<Text> lines(PassiveNode node,boolean world){
        var result=new ArrayList<Text>();
        if(node.isAttributeSelection())result.add(Text.literal(world?"Choose one bonus:":"Choose one attribute:"));
        for(var bonus:bonuses(node,world))result.add(GuideText.link(bonus.value(),GuideTarget.stat(bonus.id())));
        if(!automatic(node)&&node.description()!=null)for(String line:node.description().split("\n"))
            if(!line.isBlank())result.add(GuideText.parse(line));
        return List.copyOf(result);
    }
    public static String plain(PassiveNode node,boolean world){
        if(!automatic(node))return node.description()==null?"":node.description().replaceAll("\\[\\[[^|]+\\|([^\\]]+)\\]\\]","$1");
        String header=node.isAttributeSelection()?(world?"Choose one bonus:\n":"Choose one attribute:\n"):"";
        return header+bonuses(node,world).stream().map(Bonus::value).collect(java.util.stream.Collectors.joining("\n"));
    }
}

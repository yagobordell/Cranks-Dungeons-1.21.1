package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.inventory.EquipmentEnchanting;
import com.cranks.dungeons.stat.passive.NodeBonusFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;

public final class GuideEnchantmentArticles {
    private GuideEnchantmentArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    public static GuideDocument create(String id){
        var registry=MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);var first=registry.getEntry(Identifier.of(id)).orElseThrow();
        var entries=new ArrayList<net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment>>();entries.add(first);
        var key=Identifier.of(id);registry.getEntry(Identifier.of(key.getNamespace(),"greater_"+key.getPath())).filter(com.cranks.dungeons.util.EnchantmentFilterUtil::isApproved).ifPresent(entries::add);
        var blocks=new ArrayList<GuideDocument.Block>();String title=first.value().description().getString().replaceFirst("^Lesser ","");
        blocks.add(p(title+" is applied to compatible equipment at the "+GuideText.reference("Enchanting Table",GuideTarget.article("enchantments"))+". Each copy occupies one enchantment slot."));
        var costs=new ArrayList<List<String>>();for(var entry:entries)costs.add(List.of(entry.value().description().getString(),""+EquipmentEnchanting.lapisCost(entry)));blocks.add(GuideTables.table(List.of("Tier","Lapis per copy"),costs,2,1));
        for(int i=0;i<entries.size();i++){
            var entry=entries.get(i);blocks.add(new GuideDocument.Block("heading",entry.value().description().getString(),"anchor="+(i==0?"lesser":"regular")));
            var rows=effects(entry);
            if(!rows.isEmpty())blocks.add(GuideTables.table(List.of("Equipment","Effect per copy"),rows,1,2.4));
        }
        blocks.add(h("Application and removal"));blocks.add(p(EnchantmentSlotHelper.isStackable(first)?"Multiple copies may occupy separate slots; their compatible bonuses add together. Enchantment Effectiveness scales compatible bonuses.":"This utility enchantment permits one copy per item."));
        blocks.add(p("The Enchanting Table removes a copy for free or replaces it for the new enchantment's Lapis cost. The "+GuideText.reference("Upgrade Bench",GuideTarget.article("quality"))+" adds slots up to the equipment-type limit."));
        return new GuideDocument(title,List.copyOf(blocks));
    }
    private static List<List<String>> effects(net.minecraft.registry.entry.RegistryEntry<net.minecraft.enchantment.Enchantment> entry){
        var groups=new LinkedHashMap<String,Set<EquipmentType>>();var compatible=EnumSet.noneOf(EquipmentType.class);
        for(var item:Registries.ITEM){
            var stack=item.getDefaultStack();var type=EquipmentType.getTypeForItem(stack);
            if(type.isEmpty()||!EquipmentEnchanting.supports(stack,entry))continue;
            compatible.add(type.get());var probe=stack.copy();probe.set(DataComponentTypes.ENCHANTMENTS,ItemEnchantmentsComponent.DEFAULT);probe.addEnchantment(entry,1);
            var values=new TreeSet<String>();
            for(var slot:type.get().getApplicableSlots()){
                Set<Identifier> ids=new HashSet<>();EnchantmentHelper.applyAttributeModifiers(probe,slot,(attribute,modifier)->ids.add(modifier.id()));
                probe.applyAttributeModifiers(slot,(attribute,modifier)->{
                    if(!ids.contains(modifier.id())||modifier.value()==0)return;
                    String label=modifier.operation()==net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE
                        ?(attribute.getIdAsString().startsWith("minecraft:")?(modifier.value()>=0?"+":"")+NodeBonusFormat.number(GuideWriting.displayValue(modifier.value()))+" to "+GuideBook.statLabel(attribute.getIdAsString()):NodeBonusFormat.format(attribute.getIdAsString(),GuideWriting.displayValue(modifier.value())))
                        :NodeBonusFormat.number(GuideWriting.displayValue(modifier.value()*100))+"% "+(modifier.operation()==net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL?"more ":"increased ")+GuideBook.statLabel(attribute.getIdAsString());
                    values.add(GuideText.reference(label,GuideTarget.stat(attribute.getIdAsString())));
                });
            }
            if(!values.isEmpty())groups.computeIfAbsent(String.join("\n",values),unused->EnumSet.noneOf(EquipmentType.class)).add(type.get());
        }
        var rows=new ArrayList<List<String>>();
        groups.forEach((bonus,types)->rows.add(List.of(equipment(types),bonus)));
        if(groups.isEmpty()){
            var id=entry.getKey().orElseThrow().getValue();var descriptions=new ArrayList<String>();
            for(String suffix:List.of(".desc",".desc2")){
                String translation="enchantment."+id.getNamespace()+"."+id.getPath()+suffix;Text description=Text.translatable(translation);
                if(!description.getString().equals(translation))descriptions.add(description.getString());
            }
            if(id.getPath().equals("silk_touch"))descriptions.add("Harvests blocks as blocks instead of their ordinary resource drops where Silk Touch is supported.");
            if(id.getPath().contains("vein"))descriptions.add("Mines matching connected ores using normal harvest, durability and protection rules. The tool must still satisfy the ore's harvest requirement.");
            if(!descriptions.isEmpty())rows.add(List.of(equipment(compatible),String.join("\n",descriptions)));
        }
        return rows;
    }
    private static String equipment(Set<EquipmentType> types){
        return types.stream().sorted().map(type->GuideText.reference(GuideTopics.equipmentTitle(type),GuideTopics.equipment(type))).collect(java.util.stream.Collectors.joining(", "));
    }
}

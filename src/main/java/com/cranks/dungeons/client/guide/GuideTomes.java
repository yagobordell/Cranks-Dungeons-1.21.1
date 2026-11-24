package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.ArmorBaseType;
import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.inventory.RunePool;
import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

/** The guide reads the same targeted outcomes and tier limits as the altar. */
public final class GuideTomes {
    private GuideTomes() {}
    private static final Map<String,List<ItemStack>> BASES=new HashMap<>();
    private static final Map<EquipmentType,List<ItemStack>> EQUIPMENT=new EnumMap<>(EquipmentType.class);
    private static final Set<String> TARGETED=new HashSet<>();
    private static Object world;
    public static void reset(){BASES.clear();EQUIPMENT.clear();TARGETED.clear();world=null;}
    public static List<CustomStat> outcomes(AbstractTomeItem tome) {
        return Arrays.stream(EquipmentType.values()).map(tome::getStatIdForEquipment).filter(Objects::nonNull)
                .map(StatRegistry::getStat).filter(Objects::nonNull)
                .filter(stat->!bases(stat).isEmpty()).toList();
    }
    public static List<ItemStack> bases(CustomStat rune) {
        Object current=net.minecraft.client.MinecraftClient.getInstance().world;
        if(world!=current||EQUIPMENT.isEmpty()){
            BASES.clear();EQUIPMENT.clear();TARGETED.clear();world=current;
            for(var item:Registries.ITEM){if(item instanceof AbstractTomeItem tome)for(var type:EquipmentType.values()){String id=tome.getStatIdForEquipment(type);if(id!=null)TARGETED.add(id);}
                var stack=item.getDefaultStack();if(RunePool.supports(stack))
                EquipmentType.getTypeForItem(stack).ifPresent(type->EQUIPMENT.computeIfAbsent(type,key->new ArrayList<>()).add(stack));}
        }
        if(!StatRegistry.isRandomlyRollable(rune)&&!TARGETED.contains(rune.getId()))return List.of();
        String key=rune.getEquipmentType().name()+"/"+rune.getArmorBaseType().name();
        return BASES.computeIfAbsent(key,unused->EQUIPMENT.getOrDefault(rune.getEquipmentType(),List.of()).stream()
                .filter(stack->ArmorBaseType.getFromItem(stack).canRoll(rune.getArmorBaseType()))
                .sorted(Comparator.comparing(stack->stack.getName().getString())).toList());
    }

    public static List<AbstractTomeItem> forRune(CustomStat rune) {
        if(bases(rune).isEmpty())return List.of();
        return Registries.ITEM.stream().filter(AbstractTomeItem.class::isInstance).map(AbstractTomeItem.class::cast)
                .filter(tome->rune.getId().equals(tome.getStatIdForEquipment(rune.getEquipmentType())))
                .sorted(Comparator.comparing(tome->tome.getName().getString())).toList();
    }
    public static String tiers(Collection<Integer> tiers) {
        return tiers.stream().map(tier->"T"+tier).collect(Collectors.joining(" / "));
    }
    public static String name(CustomStat rune) {
        if(rune.getGroupId().equals("local_crit_chance"))return "Local Critical Strike Chance";
        if(rune.getGroupId().equals("global_crit_chance"))return "Global Critical Strike Chance";
        return rune.getComponents().stream().map(CustomStat.StatComponent::getDisplayLabel).distinct().collect(Collectors.joining(" + "));
    }
    public static void tooltip(ItemStack stack,List<Text> lines) {
        if(!(stack.getItem() instanceof AbstractTomeItem tome)||lines.isEmpty())return;
        Text title=lines.getFirst();lines.clear();lines.add(title);
        tome.description().forEach(line->lines.add(Text.literal(line).formatted(Formatting.GRAY)));
        for(var summary:tome.summaries())lines.add(Text.literal(summary.label()+": ").formatted(Formatting.GRAY)
                .append(GuideText.link(name(summary.stat()),GuideTopics.resolve(GuideTarget.rune(summary.stat().getId()))).formatted(Formatting.WHITE)));
    }
}

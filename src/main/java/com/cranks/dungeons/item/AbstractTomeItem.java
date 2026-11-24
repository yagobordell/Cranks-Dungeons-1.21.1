package com.cranks.dungeons.item;

import com.cranks.dungeons.equipment.EquipmentType;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

/**
 * Base class for all tome items (Elemental, Swiftness, Energy, Evasion).
 * Provides shared logic for stat application, tooltips, and rarity display.
 */
public abstract class AbstractTomeItem extends Item {

    private final CustomRarity customRarity;

    protected AbstractTomeItem(CustomRarity customRarity, Settings settings) {
        super(settings);
        this.customRarity = customRarity;
    }

    public CustomRarity getCustomRarity() {
        return customRarity;
    }

    public int getMinStatLevel() {
        return 1;
    }

    /**
     * Returns the stat ID this tome applies for the given equipment type,
     * or null if the tome cannot be applied to this equipment type.
     */
    public abstract String getStatIdForEquipment(EquipmentType equipType);

    /**
     * Check if equipment can accept this tome.
     * Validates equipment type compatibility, stat slot availability,
     * and prevents duplicate stats/attributes.
     */
    public boolean canApplyTo(ItemStack equipment) {
        return com.cranks.dungeons.inventory.RunicAltarRules.reason(equipment,getDefaultStack(),ItemStack.EMPTY)==null;
    }

    /**
     * Helper to add a standardized description to specialized tomes.
     */
    public java.util.List<String> description(){
        return java.util.List.of("Adds or replaces a guaranteed rune.","Upgrades Magic gear to Rare; also works on Rare gear.");
    }
    protected void addTomeDescription(Consumer<Text> textConsumer) {
        description().forEach(line->textConsumer.accept(Text.literal(line).formatted(Formatting.GRAY)));
    }

    public record Summary(String label,CustomStat stat) {}
    public java.util.List<Summary> summaries(){
        var rows=new java.util.ArrayList<Summary>();
        var armour=new java.util.LinkedHashMap<String,java.util.List<EquipmentType>>();
        var armourStats=new java.util.LinkedHashMap<String,CustomStat>();
        for(var type:java.util.List.of(EquipmentType.CHESTPLATE,EquipmentType.HELMET,EquipmentType.LEGGINGS,EquipmentType.BOOTS)){
            String id=getStatIdForEquipment(type);CustomStat stat=id==null?null:StatRegistry.getStat(id);if(stat==null)continue;
            armour.computeIfAbsent(stat.getGroupId(),unused->new java.util.ArrayList<>()).add(type);armourStats.putIfAbsent(stat.getGroupId(),stat);
        }
        armour.forEach((family,types)->{
            String label=types.size()==4?"Armour":types.stream().map(type->switch(type){case HELMET->"Helmets";case CHESTPLATE->"Chestplates";case LEGGINGS->"Leggings";case BOOTS->"Boots";default->throw new IllegalArgumentException();}).collect(java.util.stream.Collectors.joining(", "));
            rows.add(new Summary(label,armourStats.get(family)));
        });
        for(var group:java.util.List.of(java.util.List.of(EquipmentType.SWORD),java.util.List.of(EquipmentType.SPEAR,EquipmentType.BOW,EquipmentType.CROSSBOW,EquipmentType.MACE,EquipmentType.TRIDENT))){
            String label=group.getFirst()==EquipmentType.SWORD?"1H Martial Weapons":"2H Martial Weapons";
            group.stream().map(this::getStatIdForEquipment).filter(java.util.Objects::nonNull).map(StatRegistry::getStat)
                    .filter(java.util.Objects::nonNull).findFirst().ifPresent(stat->rows.add(new Summary(label,stat)));
        }
        return java.util.List.copyOf(rows);
    }
    /**
     * Check if the equipment type is a weapon.
     */
    public static boolean isWeapon(EquipmentType type) {
        return switch (type) {
            case SWORD, SPEAR, MACE, BOW, CROSSBOW, TRIDENT -> true;
            default -> false;
        };
    }

    /**
     * Check if the equipment type is armor (excluding elytra/shield).
     */
    public static boolean isArmor(EquipmentType type) {
        return switch (type) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> true;
            default -> false;
        };
    }
}

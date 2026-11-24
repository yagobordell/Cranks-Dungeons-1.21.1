package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.inventory.*;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.stat.*;
import com.cranks.dungeons.stat.passive.NodeBonusFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import java.util.*;
import java.util.stream.Collectors;

/** The equipment article owns its material variants, compatible affixes and upgrade facts. */
public final class GuideEquipmentArticles {
    private GuideEquipmentArticles() {}
    private static GuideDocument.Block p(String s){return new GuideDocument.Block("text",s);}
    private static GuideDocument.Block h(String s){return new GuideDocument.Block("heading",s);}
    private static final Map<EquipmentType,List<ItemStack>> VARIANTS=new EnumMap<>(EquipmentType.class);private static Object world;
    public static void reset(){VARIANTS.clear();world=null;}
    public static List<ItemStack> variants(EquipmentType type){
        Object current=MinecraftClient.getInstance().world;if(current!=world||VARIANTS.isEmpty()){
            VARIANTS.clear();world=current;for(var item:Registries.ITEM){var stack=item.getDefaultStack();if(AnimalArmorHelper.isSupported(stack))continue;
                EquipmentType.getTypeForItem(stack).ifPresent(t->VARIANTS.computeIfAbsent(t,unused->new ArrayList<>()).add(stack));}
            VARIANTS.values().forEach(list->list.sort(Comparator.comparingInt(RuneTiers::best).reversed().thenComparing(stack->stack.getName().getString())));
        }return VARIANTS.getOrDefault(type,List.of());
    }
    public static GuideDocument create(EquipmentType type){
        var items=variants(type);var blocks=new ArrayList<GuideDocument.Block>();String title=GuideTopics.equipmentTitle(type);
        if(!items.isEmpty())blocks.add(new GuideDocument.Block("item_image",Registries.ITEM.getId(items.getFirst().getItem()).toString(),"caption="+title));
        blocks.add(p(switch(type){
            case HELMET->"A helmet is equipment worn in the head slot. Its material determines its native defences, requirements and strongest rune tier.";
            case CHESTPLATE->"A chestplate is equipment worn in the chest slot. Material variants provide different combinations of Armour, Evasion and Energy Shield.";
            case LEGGINGS->"Leggings are equipment worn in the leg slot. They supply material-specific defences and accept compatible leg-slot upgrades.";
            case BOOTS->"Boots are equipment worn in the feet slot. They supply defences and can roll supported movement-related affixes.";
            case SWORD->"A sword is a one-handed melee weapon. Two compatible one-handed weapons support dual wielding; a Shield can occupy the other hand.";
            case BOW->"A bow is a two-handed ranged weapon. Hold Use to draw, press Attack to fire, and release Use to cancel. Continuing to hold Use begins the next draw.";
            case CROSSBOW->"A crossbow is a two-handed ranged weapon that stores a loaded shot. Hold Use to load; Attack fires the bolt. After loading, release and hold Use again to zoom.";
            case SHIELD->"A shield blocks eligible frontal hits while raised. Block Value is subtracted before Armour and resistances, up to 85% of the hit.";
            case SPEAR->"A spear is a two-handed melee weapon. Its compatible affixes and two-handed bonuses apply while wielded.";
            case ELYTRA->"Elytra are chest-slot equipment used for gliding. Their fixed Unique properties are separate from ordinary armour rune crafting.";
            case TRIDENT->"A trident is a Unique weapon usable in melee or as a thrown projectile. Its fixed rune properties cannot be rewritten with ordinary tomes.";
            case MACE->"A mace is a Unique melee weapon with falling-attack mechanics and fixed rune properties.";
            default->"A "+title.toLowerCase(Locale.ROOT)+" is a gathering tool. The tool and material must satisfy the block's harvest requirements. Tools use compatible enchantments and slot upgrades, rather than rune affixes.";
        }));
        blocks.add(h("Types and statistics"));var rows=new ArrayList<List<String>>();
        for(var stack:items){String id=Registries.ITEM.getId(stack.getItem()).toString();String details=baseStats(stack);
            rows.add(List.of(GuideText.reference(stack.getName().getString(),new GuideTarget(GuideTarget.Kind.ARTICLE,GuideTopics.equipment(type).key()+"#"+GuideReferences.slug(id))),details,
                    stack.isDamageable()?""+stack.getMaxDamage():"—",RunePool.supports(stack)?"T"+RuneTiers.best(stack):"Fixed / no rolls"));}
        if(!rows.isEmpty())blocks.add(GuideTables.table(List.of("Type","Native statistics","Durability","Rune cap"),rows,1.5,2, .8,.8));
        blocks.add(h("Obtaining"));
        String source=switch(type){case HELMET,CHESTPLATE,LEGGINGS,BOOTS->"Leather equipment is crafted at the Tailoring Bench. Iron, gold, chainmail, diamond and Netherite equipment is crafted at the Anvil. Supported creatures and treasure can also supply equipment.";
            case BOW,CROSSBOW->"Craft ranged equipment at the Fletching Table. Skeletons can also drop their carried ranged weapon.";
            case SWORD,SPEAR->"Craft early wooden and stone equipment through its assembly recipe; advanced-material equipment belongs at the Anvil. Equipped creatures can drop their carried gear.";
            case SHIELD,MACE->"Craft the item at the Anvil.";case ELYTRA->"Found aboard End Ships.";case TRIDENT->"Dropped by Drowned carrying a Trident.";
            default->"Craft wooden and stone tools through the Crafting Table. Advanced-material tools are assembled at the Anvil.";};
        blocks.add(p(source));
        var runeBlocks=runes(type);if(!runeBlocks.isEmpty()){blocks.add(h("Runes"));blocks.addAll(runeBlocks);}
        var client=MinecraftClient.getInstance();
        if(client.world!=null){var enchants=new ArrayList<List<String>>();
            for(var entry:client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getIndexedEntries()){
                if(!com.cranks.dungeons.util.EnchantmentFilterUtil.isApproved(entry))continue;
                if(items.stream().anyMatch(stack->EquipmentEnchanting.supports(stack,entry)))enchants.add(List.of(GuideText.reference(entry.value().description().getString(),new GuideTarget(GuideTarget.Kind.ENCHANTMENT,entry.getKey().orElseThrow().getValue().toString())),""+EquipmentEnchanting.lapisCost(entry)));
            }
            enchants.sort(Comparator.comparingInt((List<String> row)->Integer.parseInt(row.get(1)))
                    .thenComparing(row->GuideText.plain(row.getFirst()),String.CASE_INSENSITIVE_ORDER));
            if(!enchants.isEmpty()){blocks.add(h("Enchantments"));blocks.add(GuideTables.table(List.of("Compatible enchantment","Lapis per copy"),enchants,2,1));}
        }
        blocks.add(h("Repairing and upgrades"));blocks.add(p("Restore durability at the "+GuideText.reference("Repair Bench",GuideTarget.article("repairing"))+". The "+GuideText.reference("Upgrade Bench",GuideTarget.article("quality"))+" adds supported quality and enchantment slots. The material and slot limits for each type are listed below."));
        for(var stack:items){String id=Registries.ITEM.getId(stack.getItem()).toString();blocks.add(new GuideDocument.Block("heading",stack.getName().getString(),"anchor="+GuideReferences.slug(id)));
            var facts=new ArrayList<List<String>>();int[] req=ItemRequirementManager.getRequirements(stack);var requirements=new ArrayList<String>();
            for(int i=0;i<3;i++)if(req[i]>0)requirements.add(req[i]+" "+List.of("Strength","Dexterity","Intelligence").get(i));
            facts.add(List.of("Requirements",requirements.isEmpty()?"None":String.join(", ",requirements)));
            facts.add(List.of("Maximum enchantment slots",""+EnchantmentSlotHelper.getMaxEnchantmentSlots(stack)));
            var repair=EquipmentRepair.materials(stack);if(repair!=null){String materials=Arrays.stream(repair.ingredient().getMatchingStacks()).map(material->GuideText.reference(material.getName().getString(),GuideText.itemTarget(material))).distinct().collect(Collectors.joining(" or "));
                facts.add(List.of("Repair material",materials));facts.add(List.of("Material units for full durability",""+repair.fullCost()));}
            if(CustomRarity.getRarity(stack)==CustomRarity.UNIQUE)facts.add(List.of("Special properties",GuideText.reference("Unique item details",GuideTarget.item(id))));
            blocks.add(GuideTables.table(List.of("Property","Value"),facts,1,2));
        }
        return new GuideDocument(title,List.copyOf(blocks));
    }
    private static String baseStats(ItemStack stack){
        var rows=new ArrayList<String>();
        for(var entry:List.of(EntityAttributes.GENERIC_ARMOR,ModAttributes.EVASION_RATING,ModAttributes.ENERGY_SHIELD,EntityAttributes.GENERIC_ATTACK_DAMAGE,EntityAttributes.GENERIC_ATTACK_SPEED,ModAttributes.BLOCK_VALUE)){
            double[] total={0};var slots=EquipmentType.getTypeForItem(stack).orElseThrow()==EquipmentType.SHIELD?Set.of(net.minecraft.entity.EquipmentSlot.MAINHAND):EquipmentType.getTypeForItem(stack).orElseThrow().getApplicableSlots();
            for(var slot:slots)stack.applyAttributeModifiers(slot,(attribute,modifier)->{if(com.cranks.dungeons.util.CombatUtility.attributesMatch(attribute,entry)&&modifier.operation()==EntityAttributeModifier.Operation.ADD_VALUE)total[0]+=modifier.value();});
            if(total[0]!=0){if(com.cranks.dungeons.util.CombatUtility.attributesMatch(entry,EntityAttributes.GENERIC_ATTACK_SPEED))total[0]+=4;if(com.cranks.dungeons.util.CombatUtility.attributesMatch(entry,EntityAttributes.GENERIC_ATTACK_DAMAGE))total[0]+=1;rows.add(GuideBook.statLabel(entry.getIdAsString())+" "+NodeBonusFormat.number(GuideWriting.displayValue(total[0])));}
        }return rows.isEmpty()?"—":String.join("; ",rows);
    }
    public static List<GuideDocument.Block> runes(EquipmentType type){
        var stats=StatRegistry.getStatsByEquipmentType(type).stream().filter(s->!GuideTomes.bases(s).isEmpty()).sorted(Comparator.comparing(CustomStat::getAffixType).thenComparing(GuideTomes::name)).toList();
        if(stats.isEmpty())return List.of();var result=new ArrayList<GuideDocument.Block>();
        for(var side:CustomStat.AffixType.values()){
            var rows=new ArrayList<List<String>>();for(var stat:stats)if(stat.getAffixType()==side){
                var sources=new ArrayList<String>();if(StatRegistry.isRandomlyRollable(stat))sources.add(GuideText.reference("Random tomes",GuideTarget.article("tomes")));
                for(var tome:GuideTomes.forRune(stat))sources.add(GuideText.reference(tome.getName().getString(),GuideText.itemTarget(tome.getDefaultStack())));
                rows.add(List.of(GuideText.reference(GuideTomes.name(stat),GuideTarget.rune(stat.getId())),stat.getArmorBaseType()==ArmorBaseType.ANY||stat.getArmorBaseType()==ArmorBaseType.ALL?"Any base":stat.getArmorBaseType().name().replace("STR","Armour").replace("DEX","Evasion").replace("INT","Energy Shield").replace("_"," + "),String.join(", ",sources)));
            }
            if(!rows.isEmpty()){result.add(new GuideDocument.Block("subheading",side==CustomStat.AffixType.PREFIX?"Prefixes":"Suffixes"));result.add(GuideTables.table(List.of("Rune","Base affinity","Source"),rows,1.6,1,1.4));}
        }return List.copyOf(result);
    }
}

package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.item.AbstractTomeItem;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.stat.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

/** One destination per subject. Old tooltip and article links remain valid aliases. */
public final class GuideTopics {
    private GuideTopics() {}
    public static String base(String key){return key.split("#",2)[0];}
    public static GuideTarget root(GuideTarget target){return new GuideTarget(target.kind(),base(target.key()));}
    public static GuideTarget resolve(GuideTarget target){
        if(target==null)return null;String key=base(target.key()),anchor=target.key().contains("#")?target.key().substring(target.key().indexOf('#')+1):"";
        GuideTarget result=target;
        switch(target.kind()){
            // Vanilla poison is converted into the same stacking ailment when applied.
            case EFFECT->{if(key.equals("minecraft:poison"))result=new GuideTarget(GuideTarget.Kind.EFFECT,"cranks-dungeons:poison");}
            case ARTICLE->{if(key.startsWith("rune_")&&!key.equals("rune_tiers"))try{var type=EquipmentType.valueOf(key.substring(5).toUpperCase(Locale.ROOT));result=equipment(type);}catch(IllegalArgumentException ignored){}}
            case STAT->{String id=switch(key){
                case "minecraft:generic.armor","derived/armour","cranks-dungeons:armor_percent"->"cranks-dungeons:bonus_armor";
                case "derived/evasion_rating","cranks-dungeons:evasion_rating_percent"->"cranks-dungeons:evasion_rating";
                case "derived/energy_shield","cranks-dungeons:energy_shield_percent"->"cranks-dungeons:energy_shield";
                case "minecraft:generic.max_health","cranks-dungeons:max_health_percent"->"cranks-dungeons:max_health_bonus";
                case "minecraft:generic.movement_speed","derived/movement_speed"->"cranks-dungeons:movement_speed";
                case "cranks-dungeons:bonus_attack_damage","cranks-dungeons:physical_damage_percent"->"minecraft:generic.attack_damage";
                case "cranks-dungeons:global_crit_chance_percent","derived/critical_chance"->"cranks-dungeons:crit_chance";
                case "cranks-dungeons:fire_damage_percent"->"cranks-dungeons:fire_damage";
                case "cranks-dungeons:cold_damage_percent"->"cranks-dungeons:cold_damage";
                case "cranks-dungeons:lightning_damage_percent"->"cranks-dungeons:lightning_damage";
                case "cranks-dungeons:void_damage_percent"->"cranks-dungeons:void_damage";
                case "derived/health_regen"->"cranks-dungeons:life_regeneration";
                case "derived/mana_recovery"->"cranks-dungeons:mana_regeneration";
                case "derived/physical_reduction"->"cranks-dungeons:bonus_armor";
                case "derived/attack_speed","cranks-dungeons:attack_speed"->"minecraft:generic.attack_speed";
                case "derived/critical_multiplier"->"cranks-dungeons:crit_multiplier";
                case "derived/critical_protection","cranks-dungeons:armor_crit_mitigation"->"cranks-dungeons:reduced_crit_damage_taken_percent";
                case "derived/es_recharge_rate"->"cranks-dungeons:energy_shield_recharge_rate_percent";
                case "derived/es_recharge_delay"->"cranks-dungeons:faster_start_of_energy_shield_recharge";
                case "derived/block_reach","cranks-dungeons:breaking_range"->"minecraft:player.block_interaction_range";
                default->key;};result=GuideTarget.stat(id);}
            case RUNE->{var rune=StatRegistry.getStat(key);if(rune!=null){
                if(rune.getGroupId().equals("local_crit_chance"))result=GuideTarget.stat("cranks-dungeons:crit_chance");
                else if(rune.getComponents().size()==1&&rune.getComponents().getFirst().getAttributes().size()==1)
                    result=resolve(GuideTarget.stat(rune.getComponents().getFirst().getAttributes().getFirst().getIdAsString()));
                else result=GuideTarget.rune(GuideRuneArticles.family(rune).stream().filter(GuideRuneArticles::visible).map(CustomStat::getId).sorted().findFirst().orElse(key));}}
            case ITEM->{var stack=GuideIllustrations.stack(key);if(!stack.isEmpty()){
                var potion=stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
                var formula=potion==null||stack.getItem() instanceof net.minecraft.item.ArrowItem?null:com.cranks.dungeons.alchemy.AlchemyPotions.FORMULAS.stream().filter(f->potion.matches(f.potion())).findFirst().orElse(null);
                if(formula!=null){var regular=com.cranks.dungeons.alchemy.AlchemyPotions.FORMULAS.stream().filter(f->f.family().equals(formula.family())&&!f.lesser()).findFirst().orElse(formula);
                    result=GuideText.itemTarget(regular.stack(com.cranks.dungeons.alchemy.AlchemyItems.Delivery.DRINK));if(anchor.isBlank())anchor=formula.lesser()?"lesser":"regular";
                }
                var type=EquipmentType.getTypeForItem(stack);
                if(type.isPresent()&&CustomRarity.getRarity(stack)!=CustomRarity.UNIQUE&&!AnimalArmorHelper.isSupported(stack)){
                    result=equipment(type.get());if(anchor.isBlank())anchor=GuideReferences.slug(Registries.ITEM.getId(stack.getItem()).toString());
                }else if(stack.getItem() instanceof AbstractTomeItem){
                    var id=Registries.ITEM.getId(stack.getItem());if(id.getPath().startsWith("greater_")){
                        var normal=Identifier.of(id.getNamespace(),id.getPath().substring(8));
                        if(Registries.ITEM.get(normal) instanceof AbstractTomeItem){result=GuideTarget.item(normal.toString());if(anchor.isBlank())anchor="greater";}
                    }
                }
            }}
            case ENCHANTMENT->{var client=net.minecraft.client.MinecraftClient.getInstance();if(client.world!=null&&Identifier.of(key).getPath().startsWith("greater_")){
                var id=Identifier.of(key);var normal=Identifier.of(id.getNamespace(),id.getPath().substring(8));
                if(client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT).containsId(normal)){result=new GuideTarget(target.kind(),normal.toString());if(anchor.isBlank())anchor="regular";}
            }}
            default->{}
        }
        return new GuideTarget(result.kind(),base(result.key())+(anchor.isBlank()?"":"#"+anchor));
    }
    public static GuideTarget equipment(EquipmentType type){return GuideTarget.article("equipment_"+type.name().toLowerCase(Locale.ROOT));}
    public static String equipmentTitle(EquipmentType type){return switch(type){case HELMET->"Helmet";case CHESTPLATE->"Chestplate";case LEGGINGS->"Leggings";case BOOTS->"Boots";case PICKAXE->"Pickaxe";default->type.getName();};}
    public static String title(GuideTarget target,String fallback){
        target=resolve(target);
        String key=base(target.key());return switch(target.kind()){
            case EFFECT->effectName(key)+" (effect)";
            case STAT->GuideBook.statLabel(key);
            case ENCHANTMENT->{var client=net.minecraft.client.MinecraftClient.getInstance();if(client.world!=null){var enchant=client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getEntry(Identifier.of(key));if(enchant.isPresent())yield enchant.get().value().description().getString().replaceFirst("^Lesser ","");}yield fallback;}
            case RUNE->{var stat=StatRegistry.getStat(key);yield stat==null?fallback:GuideTomes.name(stat);}
            case ARTICLE->{if(key.startsWith("equipment_"))try{yield equipmentTitle(EquipmentType.valueOf(key.substring(10).toUpperCase(Locale.ROOT)));}catch(IllegalArgumentException ignored){}yield fallback;}
            case ITEM->{var stack=GuideIllustrations.stack(key);if(stack.isEmpty())yield fallback;
                if(stack.getItem() instanceof AbstractTomeItem){var id=Registries.ITEM.getId(stack.getItem());
                    if(Registries.ITEM.get(Identifier.of(id.getNamespace(),"greater_"+id.getPath())) instanceof AbstractTomeItem)yield stack.getName().getString().replace("Tome of ","Tomes of ");}
                yield stack.getName().getString();}
            default->fallback;
        };
    }
    public static String effectName(String key){
        var id=Identifier.of(base(key));var effect=Registries.STATUS_EFFECT.get(id);
        String label=effect.getName().getString();
        if(!label.startsWith("effect."))return label;
        // A missing translation from an optional mod must not expose a registry key in the directory.
        return Arrays.stream(id.getPath().replace('_',' ').split(" ")).filter(word->!word.isEmpty())
                .map(word->Character.toUpperCase(word.charAt(0))+word.substring(1)).collect(java.util.stream.Collectors.joining(" "));
    }
}

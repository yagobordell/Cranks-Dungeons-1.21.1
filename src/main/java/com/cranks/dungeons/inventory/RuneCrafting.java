package com.cranks.dungeons.inventory;

import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.item.*;
import com.cranks.dungeons.stat.*;
import com.cranks.dungeons.util.RandomUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import java.util.*;

/** Server-side crafting plus a durable, single paid proposal; unrelated item data is preserved. */
public final class RuneCrafting {
    private RuneCrafting() {}
    private static final String PROPOSAL="RunicProposal";
    private static final List<String> KEYS=List.of("CustomStats","ArtificialStat","CrypticAffix","CrypticOptions","Rarity");
    public static NbtCompound data(ItemStack gear){return gear.getOrDefault(DataComponentTypes.CUSTOM_DATA,NbtComponent.DEFAULT).copyNbt();}
    private static void write(ItemStack gear,NbtCompound nbt){gear.set(DataComponentTypes.CUSTOM_DATA,NbtComponent.of(nbt));}
    private static NbtCompound state(ItemStack gear){var all=data(gear);var result=new NbtCompound();for(var key:KEYS)if(all.contains(key))result.put(key,all.get(key).copy());return result;}
    private static void state(ItemStack gear,NbtCompound state){var all=data(gear);for(var key:KEYS){all.remove(key);if(state.contains(key))all.put(key,state.get(key).copy());}write(gear,all);EquipmentStatApplier.updateItemAttributes(gear);}
    public static boolean pending(ItemStack gear){return data(gear).contains(PROPOSAL,NbtElement.COMPOUND_TYPE);}
    public static ItemStack proposal(ItemStack gear){
        if(!pending(gear))return ItemStack.EMPTY;
        var saved=data(gear).getCompound(PROPOSAL);
        if(!state(gear).equals(saved.getCompound("Before")))return ItemStack.EMPTY;
        var result=gear.copy();var nbt=data(result);nbt.remove(PROPOSAL);write(result,nbt);state(result,saved.getCompound("After"));return result;
    }
    public static void stage(ItemStack gear,ItemStack candidate){
        var saved=new NbtCompound();saved.put("Before",state(gear));saved.put("After",state(candidate));
        var all=data(gear);all.put(PROPOSAL,saved);write(gear,all);
    }
    public static boolean resolve(ItemStack gear,boolean accept){
        if(!pending(gear))return false;
        ItemStack candidate=accept?proposal(gear):ItemStack.EMPTY;
        if(accept&&candidate.isEmpty())return false;
        if(accept)state(gear,state(candidate));
        var nbt=data(gear);nbt.remove(PROPOSAL);write(gear,nbt);return true;
    }
    private static int rawIndex(ItemStack gear,int index){
        var list=data(gear).getList("CustomStats",10);int valid=0;
        for(int i=0;i<list.size();i++){var row=list.getCompound(i);if(!row.getString("id").isEmpty()&&(row.contains("values")||row.contains("value"))){if(valid++==index)return i;}}
        return -1;
    }
    public static boolean protectedRune(ItemStack gear,int index){var list=data(gear).getList("CustomStats",10);int raw=rawIndex(gear,index);return raw>=0&&list.getCompound(raw).getBoolean("protected");}
    public static boolean editable(ItemStack gear,int index){return index>=0&&index<ItemStatManager.getStats(gear).size()&&!protectedRune(gear,index);}
    public static boolean remove(ItemStack gear,int index){
        if(!editable(gear,index))return false;var nbt=data(gear);var list=nbt.getList("CustomStats",10);int raw=rawIndex(gear,index);if(raw<0)return false;list.remove(raw);nbt.put("CustomStats",list);write(gear,nbt);EquipmentStatApplier.updateItemAttributes(gear);return true;
    }
    private static void clearEditable(ItemStack gear){
        var nbt=data(gear);var old=nbt.getList("CustomStats",10);var keep=new NbtList();
        for(int i=0;i<old.size();i++)if(old.getCompound(i).getBoolean("protected"))keep.add(old.getCompound(i).copy());
        nbt.put("CustomStats",keep);nbt.remove("ArtificialStat");nbt.remove("CrypticAffix");nbt.remove("CrypticOptions");write(gear,nbt);
    }
    public static int strength(RunicTomeItem tome){return tome.getMinStatLevel()>=60?2:tome.getMinStatLevel()>=30?1:0;}
    public static List<Integer> targetedTiers(ItemStack gear,AbstractTomeItem tome){
        // Specialized tomes choose the family; the equipment determines its tier.
        return List.of(RuneTiers.best(gear));
    }
    public static boolean needsTarget(ItemStack gear,ItemStack tome){
        if(tome.getItem() instanceof ArtificialTomeItem)return !RunicAltarRules.hasRoom(gear,CustomStat.AffixType.PREFIX)&&!RunicAltarRules.hasRoom(gear,CustomStat.AffixType.SUFFIX);
        if(!(tome.getItem() instanceof RunicTomeItem item))return false;
        return switch(item.getTomeType()){case CHAOS,GREATER_CHAOS,PERFECT_CHAOS,DIVINE,ANNULMENT->true;default->false;};
    }
    public static boolean allowsTarget(ItemStack tome){return tome.getItem() instanceof AbstractTomeItem||tome.getItem() instanceof ArtificialTomeItem
            ||tome.getItem() instanceof RunicTomeItem item&&switch(item.getTomeType()){case CHAOS,GREATER_CHAOS,PERFECT_CHAOS,DIVINE,ANNULMENT->true;default->false;};}
    public static boolean comparison(ItemStack gear,ItemStack tome,int target){return target>=0||tome.getItem() instanceof RunicTomeItem runic
            &&runic.getTomeType()==RunicTomeItem.TomeType.ALCHEMY&&(!ItemStatManager.getStats(gear).isEmpty()||ItemStatManager.hasCrypticStat(gear));}
    public static List<CustomStat> candidates(ItemStack gear,CustomStat.AffixType side){
        var groups=new HashSet<String>();for(var roll:ItemStatManager.getStats(gear)){var stat=StatRegistry.getStat(roll.statId);if(stat!=null)groups.add(stat.getGroupId());}
        var type=EquipmentType.getTypeForItem(gear).orElse(null);var armor=ArmorBaseType.getFromItem(gear);
        return StatRegistry.getAllStats().stream().filter(StatRegistry::isRandomlyRollable).filter(s->s.getEquipmentType()==type
                &&s.getAffixType()==side&&armor.canRoll(s.getArmorBaseType())&&!groups.contains(s.getGroupId())).toList();
    }
    private static boolean add(ItemStack gear,CustomStat.AffixType side,List<Integer> tiers){
        if(!RunicAltarRules.hasRoom(gear,side))return false;
        var roll=StatRegistry.rollFamilyAndTier(candidates(gear,side),tiers);
        return roll!=null&&ItemStatManager.addSpecificStat(gear,roll.stat.getId(),roll.tier);
    }
    private static boolean add(ItemStack gear,List<Integer> tiers){
        var side=RandomUtil.nextBoolean()?CustomStat.AffixType.PREFIX:CustomStat.AffixType.SUFFIX;
        return add(gear,side,tiers)||add(gear,side==CustomStat.AffixType.PREFIX?CustomStat.AffixType.SUFFIX:CustomStat.AffixType.PREFIX,tiers);
    }
    public static String reason(ItemStack gear,ItemStack tome,int target){
        if(gear.isEmpty())return "Insert equipment";
        if(gear.getCount()!=1||!RunePool.supports(gear))return "This equipment cannot receive runes";
        if(pending(gear))return "Choose which result to keep first";
        if(tome.isEmpty()||!RunicAltarRules.isTome(tome))return "Choose a tome";
        if(target < -1 || target>=ItemStatManager.getStats(gear).size())return "Choose a valid rune";
        if(target>=0&&(!allowsTarget(tome)||!editable(gear,target)))return "This rune is protected";
        if(target>=0&&StatRegistry.getStat(ItemStatManager.getStats(gear).get(target).statId)==null)return "Rune definition is unavailable";
        if(needsTarget(gear,tome)&&target<0)return "Choose a rune to change";
        var work=gear.copy();if(target>=0)remove(work,target);
        var rarity=CustomRarity.getRarity(gear);
        if(tome.getItem() instanceof AbstractTomeItem targeted){
            if(rarity!=CustomRarity.MAGIC&&rarity!=CustomRarity.RARE)return "Requires Magic or Rare gear";
            var stat=StatRegistry.getStat(targeted.getStatIdForEquipment(EquipmentType.getTypeForItem(gear).orElseThrow()));
            if(stat==null||!ArmorBaseType.getFromItem(gear).canRoll(stat.getArmorBaseType()))return "No matching rune for this base";
            ItemStatManager.setRarity(work,CustomRarity.RARE);
            if(!RunicAltarRules.hasRoom(work,stat.getAffixType()))return stat.getAffixType()==CustomStat.AffixType.PREFIX?"Prefix slots full":"Suffix slots full";
            if(ItemStatManager.getStats(work).stream().map(s->StatRegistry.getStat(s.statId)).filter(Objects::nonNull).anyMatch(s->s.getGroupId().equals(stat.getGroupId())))return "Already has this rune family";
            return null;
        }
        if(tome.getItem() instanceof ArtificialTomeItem){
            if(rarity!=CustomRarity.RARE)return "Requires Rare gear";
            if(ItemStatManager.hasArtificialStat(work)||ItemStatManager.hasCrypticStat(work))return "Already has an artificial rune";
        }else if(tome.getItem() instanceof RunicTomeItem runic){
            switch(runic.getTomeType()){
                case TRANSMUTATION,GREATER_TRANSMUTATION,PERFECT_TRANSMUTATION->{if(rarity!=CustomRarity.NORMAL)return "Requires Normal gear";ItemStatManager.setRarity(work,CustomRarity.MAGIC);}
                case AUGMENTATION,GREATER_AUGMENTATION,PERFECT_AUGMENTATION->{if(rarity!=CustomRarity.MAGIC)return "Requires Magic gear";}
                case REGAL,GREATER_REGAL,PERFECT_REGAL->{if(rarity!=CustomRarity.MAGIC)return "Requires Magic gear";ItemStatManager.setRarity(work,CustomRarity.RARE);}
                case EXALTED,GREATER_EXALTED,PERFECT_EXALTED,CHAOS,GREATER_CHAOS,PERFECT_CHAOS->{if(rarity!=CustomRarity.RARE)return "Requires Rare gear";}
                case ALCHEMY->{clearEditable(work);ItemStatManager.setRarity(work,CustomRarity.RARE);}
                case ANNULMENT,DIVINE->{return target>=0&&StatRegistry.getStat(ItemStatManager.getStats(gear).get(target).statId)!=null?null:"Choose a valid rune";}
            }
        }
        for(var side:CustomStat.AffixType.values()){
            if(tome.getItem() instanceof ArtificialTomeItem&&target>=0&&StatRegistry.getStat(ItemStatManager.getStats(gear).get(target).statId).getAffixType()!=side)continue;
            if(tome.getItem() instanceof RunicTomeItem runic&&target>=0&&switch(runic.getTomeType()){case CHAOS,GREATER_CHAOS,PERFECT_CHAOS->true;default->false;}
                    &&StatRegistry.getStat(ItemStatManager.getStats(gear).get(target).statId).getAffixType()!=side)continue;
            if(RunicAltarRules.hasRoom(work,side)&&!candidates(work,side).isEmpty())return null;
        }
        return "No eligible rune or free affix slot";
    }
    public static ItemStack roll(ItemStack gear,ItemStack tome,int target){
        if(reason(gear,tome,target)!=null)return ItemStack.EMPTY;
        var result=gear.copy();var original=target>=0?ItemStatManager.getStats(gear).get(target):null;
        if(target>=0)remove(result,target);
        boolean success=false;
        if(tome.getItem() instanceof AbstractTomeItem targeted){
            ItemStatManager.setRarity(result,CustomRarity.RARE);var tiers=targetedTiers(result,targeted);
            success=ItemStatManager.addSpecificStat(result,targeted.getStatIdForEquipment(EquipmentType.getTypeForItem(gear).orElseThrow()),tiers.get(RandomUtil.nextInt(tiers.size())));
        }else if(tome.getItem() instanceof ArtificialTomeItem){
            var sides=Arrays.stream(CustomStat.AffixType.values()).filter(side->(original==null||StatRegistry.getStat(original.statId).getAffixType()==side)&&RunicAltarRules.hasRoom(result,side)&&!candidates(result,side).isEmpty()).toList();
            if(!sides.isEmpty())success=ItemStatManager.addCrypticStat(result,sides.get(RandomUtil.nextInt(sides.size())));
        }else if(tome.getItem() instanceof RunicTomeItem runic){
            var tiers=RuneTiers.random(result,strength(runic));
            switch(runic.getTomeType()){
                case TRANSMUTATION,GREATER_TRANSMUTATION,PERFECT_TRANSMUTATION->{ItemStatManager.setRarity(result,CustomRarity.MAGIC);success=add(result,tiers);}
                case REGAL,GREATER_REGAL,PERFECT_REGAL->{ItemStatManager.setRarity(result,CustomRarity.RARE);success=add(result,tiers);}
                case AUGMENTATION,GREATER_AUGMENTATION,PERFECT_AUGMENTATION,EXALTED,GREATER_EXALTED,PERFECT_EXALTED->success=add(result,tiers);
                case CHAOS,GREATER_CHAOS,PERFECT_CHAOS->success=add(result,StatRegistry.getStat(original.statId).getAffixType(),tiers);
                case ALCHEMY->{clearEditable(result);ItemStatManager.setRarity(result,CustomRarity.RARE);for(var side:CustomStat.AffixType.values())while(RunicAltarRules.occupied(result,side)<2){if(!add(result,side,tiers))break;success=true;}}
                case ANNULMENT->success=true;
                case DIVINE->{
                    // Restore the selected entry in place, preserving artificial/protected metadata and order.
                    var nbt=data(result);var list=data(gear).getList("CustomStats",10).copy();var row=list.getCompound(rawIndex(gear,target));
                    int tier=RuneTiers.clamp(gear,original.tier);row.putInt("tier",tier);var values=new NbtList();
                    for(double value:StatRegistry.getStat(original.statId).rollValues(tier))values.add(NbtDouble.of(value));
                    row.put("values",values);row.remove("value");nbt.put("CustomStats",list);write(result,nbt);success=true;
                }
            }
        }
        if(!success)return ItemStack.EMPTY;
        if(target>=0&&!(tome.getItem() instanceof RunicTomeItem r&&r.getTomeType()==RunicTomeItem.TomeType.DIVINE)
                &&ItemStatManager.getStats(result).size()==ItemStatManager.getStats(gear).size()){
            var nbt=data(result);var list=nbt.getList("CustomStats",10);var added=list.removeLast();list.add(Math.min(rawIndex(gear,target),list.size()),added);nbt.put("CustomStats",list);write(result,nbt);
        }
        EquipmentStatApplier.updateItemAttributes(result);return result;
    }
}

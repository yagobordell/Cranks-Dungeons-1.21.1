package com.cranks.dungeons.inventory;

import com.cranks.dungeons.block.entity.RunicEnhancementAltarBlockEntity;
import com.cranks.dungeons.equipment.*;
import com.cranks.dungeons.item.*;
import com.cranks.dungeons.stat.CustomStat;
import com.cranks.dungeons.stat.StatRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

/** Shared, non-random explanations and eligibility. No prospective rolls are generated. */
public final class RunicAltarRules {
    private RunicAltarRules() {}
    public static boolean isTome(ItemStack stack) {
        return stack.getItem() instanceof RunicTomeItem || stack.getItem() instanceof AbstractTomeItem
                || stack.getItem() instanceof ArtificialTomeItem;
    }
    public static int occupied(ItemStack gear, CustomStat.AffixType side) {
        return ItemStatManager.getAffixCount(gear, side) + (ItemStatManager.hasCrypticStat(gear)
                && ItemStatManager.getCrypticAffixType(gear) == side ? 1 : 0);
    }
    public static boolean hasRoom(ItemStack gear, CustomStat.AffixType side) {
        var rarity = CustomRarity.getRarity(gear);
        return occupied(gear, side) < (side == CustomStat.AffixType.PREFIX ? rarity.getMaxPrefixes() : rarity.getMaxSuffixes());
    }
    public static String reason(ItemStack gear,ItemStack tome,ItemStack oldResult) {
        if(!oldResult.isEmpty())return "Collect the previous result";
        String reason=RuneCrafting.reason(gear,tome,-1);
        if(reason!=null&&RuneCrafting.allowsTarget(tome))for(int i=0;i<ItemStatManager.getStats(gear).size();i++){
            if(RuneCrafting.reason(gear,tome,i)==null)return null;
        }
        return reason;
    }
    public static String reason(ItemStack gear,ItemStack tome,ItemStack oldResult,int target) {
        return !oldResult.isEmpty()?"Collect the previous result":RuneCrafting.reason(gear,tome,target);
    }
    public static boolean destructive(ItemStack gear,ItemStack tome) {
        return RuneCrafting.allowsTarget(tome)||tome.getItem() instanceof RunicTomeItem runic
                &&runic.getTomeType()==RunicTomeItem.TomeType.ALCHEMY&&!ItemStatManager.getStats(gear).isEmpty();
    }
    public static boolean destructive(ItemStack gear,ItemStack tome,int target){return RuneCrafting.comparison(gear,tome,target);}
    public static Text effect(ItemStack tome) {
        String key = tome.getItem() instanceof ArtificialTomeItem ? "artificial"
                : tome.getItem() instanceof AbstractTomeItem ? "targeted"
                : tome.getItem() instanceof RunicTomeItem runic ? switch (runic.getTomeType()) {
                    case TRANSMUTATION, GREATER_TRANSMUTATION, PERFECT_TRANSMUTATION -> "transmutation";
                    case AUGMENTATION, GREATER_AUGMENTATION, PERFECT_AUGMENTATION -> "augmentation";
                    case REGAL, GREATER_REGAL, PERFECT_REGAL -> "regal";
                    case ALCHEMY -> "alchemy";
                    case EXALTED, GREATER_EXALTED, PERFECT_EXALTED -> "exalted";
                    case CHAOS, GREATER_CHAOS, PERFECT_CHAOS -> "chaos";
                    case DIVINE -> "divine";
                    case ANNULMENT -> "annulment";
                } : "choose";
        return text("effect_" + key);
    }
    public static Text text(String key, Object... args) { return Text.translatable("runic.cranks-dungeons." + key, args); }
}

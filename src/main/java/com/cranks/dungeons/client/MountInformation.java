package com.cranks.dungeons.client;

import com.cranks.dungeons.mount.*;
import com.cranks.dungeons.screen.MountCargoScreenHandler;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.*;
import static com.cranks.dungeons.client.HorseInformation.*;

/** Shared descriptions for the non-horse training/genetics systems. */
final class MountInformation {
    private MountInformation(){}
    static List<StatLine> lines(MountCargoScreenHandler handler,int tab){
        var mount=handler.mount();var data=MountClientData.get(mount.getId());var kind=MountKind.of(mount);
        if(data==null)return syncingLines();var out=new ArrayList<StatLine>();
        if(tab==1){
            out.add(new StatLine("Level",data.level()+" "+(data.experienceRequired()<=0?"MAX":data.experience()+"/"+data.experienceRequired()),0xffbbbbbb));
            out.add(new StatLine("Health",format(mount.getHealth())+"/"+format(mount.getMaxHealth()),0xffbbbbbb));
            out.add(new StatLine("Movement Speed",String.format(Locale.US,"%.1f blocks/s",getLivingAttribute(mount,EntityAttributes.GENERIC_MOVEMENT_SPEED)*42.16),0xffbbbbbb));
            addIfNonZero(out,"Armor",getLivingAttribute(mount,EntityAttributes.GENERIC_ARMOR),"");
            out.add(new StatLine("Cargo Capacity",Integer.toString(handler.activeCapacity()),0xffbbbbbb));
            if(kind==MountKind.LLAMA){out.add(new StatLine("Pack",handler.packSize()+" llamas",0xffbbbbbb));out.add(new StatLine("Pack Speed","+"+Math.round(LlamaPackManager.speedBonusForSize(handler.packSize(),data.power(),data.trait())*100)+"%",0xffbbbbbb));}
            if(data.maximumResource()>0)out.add(new StatLine(kind==MountKind.STRIDER?"Heat":"Endurance",format(data.resource())+"/"+format(data.maximumResource()),0xffbbbbbb));
            out.add(new StatLine("Training",kind==MountKind.LLAMA?"Travel in a caravan led by a moving player":"Travel while actively ridden; passive transport gives no XP",0xffa3d5ff));
            out.add(new StatLine("Travel XP",kind==MountKind.LLAMA||kind==MountKind.PIG||kind==MountKind.SKELETON_HORSE
                    ?"2 per 10 blocks":"2 per 10 blocks; 4 while using a speed ability",0xffbbbbbb));
            if(kind.hasDurabilityProfile())out.add(new StatLine("Per Level","+10 base health, +0.3% base speed",0xffbbbbbb));
            else out.add(new StatLine("Per Level","+0.3% base speed",0xffbbbbbb));
            out.add(new StatLine("Level Cap","30",0xffbbbbbb));
            if(kind==MountKind.MULE)out.add(new StatLine("Breeding","Sterile hybrid",0xff999999));
            if(!"None".equals(data.trait())){
                out.add(new StatLine("Trait",data.trait(),0xffa8e6a3));
                traitBonuses(out,kind,data.trait());
            }
        }else if(tab==2){
            if(kind.hasGenetics()){
                var labels=MountManager.geneLabels(kind);double[] values=kind==MountKind.LLAMA?new double[]{data.health(),data.speed(),data.power(),data.utility(),data.armor()}:new double[]{data.health(),data.speed(),data.power(),data.endurance(),data.utility(),data.armor()};
                String grade=MountGeneticRatings.overall(kind,values,data.generation());out.add(new StatLine("Overall Grade",grade,gradeColor(grade)));out.add(new StatLine("Sex",data.male()?"Male":"Female",0xffbbbbbb));
                for(int i=0;i<labels.length;i++){String value=labels[i].equals("Pack Instinct")?String.format(Locale.US,"%.1f%%",values[i]*100):labels[i].contains("Speed")?String.format(Locale.US,"%.1f blocks/s",values[i]*42.16):format(values[i]);out.add(geneLine(labels[i],value,kind,i,values[i],data.generation()));}
            }else if(kind==MountKind.SKELETON_HORSE){
                out.add(new StatLine("Soul Bond",data.soulBond()+"/100",0xffbbbbbb));
                out.add(new StatLine("Bonding","+1 bond per 10 blocks ridden",0xffbbbbbb));
                out.add(new StatLine("Specialization",data.specialization(),0xffbbbbbb));
                out.add(new StatLine("Unlock","Choose permanently at 10 Soul Bond",0xffa3d5ff));
                out.add(new StatLine("Tidebound","+0.75 water movement efficiency; grants rider Water Breathing",0xffbbbbbb));
                out.add(new StatLine("Graveguard","+20% base health; +50% of species base armor",0xffbbbbbb));
                out.add(new StatLine("Stormrunner","+15% base speed; +40% lightning resistance",0xffbbbbbb));
            }
            else out.add(new StatLine("Genetics","Not applicable",0xff999999));
        }else{
            if(kind==MountKind.SKELETON_HORSE){out.add(new StatLine("Specialization",data.specialization(),0xffbbbbbb));out.add(new StatLine("Soul Bond",data.soulBond()+"/100",0xffbbbbbb));}
            else{
                out.add(new StatLine("Generation",Integer.toString(data.generation()),0xffbbbbbb));out.add(new StatLine("Breeding Attempts",Integer.toString(data.breedsRemaining()),0xffbbbbbb));out.add(new StatLine("Breeding Cooldown",formatTime(data.breedingCooldownTicks()),0xffbbbbbb));
                if(kind==MountKind.MULE)out.add(new StatLine("Status","Sterile hybrid",0xffbbbbbb));
                if(!data.sireName().isBlank())out.add(new StatLine("Sire",data.sireName(),0xffbbbbbb));if(!data.damName().isBlank())out.add(new StatLine("Dam",data.damName(),0xffbbbbbb));
            }
        }
        return out;
    }

    private static void traitBonuses(List<StatLine> out,MountKind kind,String trait){
        String detail=switch(trait){
            case "Packmaster" -> kind==MountKind.DONKEY?"+2 cargo slots (up to 33)":"";
            case "Tireless" -> "+15% maximum endurance; 20% lower speed-ability drain";
            case "Sure-Footed" -> "+0.25 step height, +20% knockback resistance, +2 safe fall distance"
                    +(kind==MountKind.DONKEY||kind==MountKind.MULE?"; jump cost reduced from 8 to 6":"");
            case "Sturdy" -> "+15% of species base armor";
            case "Hybrid Vigor" -> "+8% base health and species base armor; +10% maximum endurance; +3% speed while boosting";
            case "Long-Haul" -> "+15% maximum endurance";
            case "Powerful" -> "+10% base dash force";
            case "Efficient" -> "20% lower dash cost and speed-ability drain";
            case "Stalwart" -> "+10% base health; +12% of species base armor";
            case "Lava-Born" -> "+10% maximum heat";
            case "Heatkeeper" -> "20% lower speed-ability heat drain";
            case "Steady" -> "+20% knockback resistance";
            case "Blazing" -> "+5% speed while boosting";
            case "Pack Leader" -> "+5% pack speed with at least one companion (total cap 50%)";
            case "Swift" -> "+5% base movement speed";
            case "Hardy" -> "+10% base health";
            default -> "";
        };
        if(!detail.isEmpty())out.add(new StatLine("Trait Bonus",detail,0xffa8e6a3));
    }
}

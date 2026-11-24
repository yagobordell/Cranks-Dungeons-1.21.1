package com.cranks.dungeons.client;

import com.cranks.dungeons.mount.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import java.util.*;
import static com.cranks.dungeons.client.HorseInformation.*;

/** Read-only pairing previews from synchronized data; the server still decides breeding. */
final class MountBreedingPreview {
    private record Parent(boolean male, int attempts, long cooldown, List<UUID> ancestry,
                          double[] genes, String trait) {}

    static boolean breedable(MountKind kind) {
        return kind==MountKind.HORSE||kind==MountKind.DONKEY||kind==MountKind.CAMEL
                ||kind==MountKind.LLAMA||kind==MountKind.STRIDER;
    }

    static List<LivingEntity> nearby(LivingEntity mount) {
        var kind=MountKind.of(mount);
        var candidates=new ArrayList<LivingEntity>(mount.getWorld().getEntitiesByClass(LivingEntity.class,
                mount.getBoundingBox().expand(8), candidate->candidate!=mount&&candidate.isAlive()
                        &&!candidate.isBaby()&&compatibleKinds(kind,MountKind.of(candidate))
                        &&(!(candidate instanceof AbstractHorseEntity horse)||horse.isTame())));
        candidates.sort(Comparator.comparingInt((LivingEntity candidate)->blockReason(mount,candidate)==null?0:1)
                .thenComparingDouble(mount::squaredDistanceTo));
        return List.copyOf(candidates);
    }

    private static boolean compatibleKinds(MountKind a,MountKind b) {
        return breedable(a)&&(a==b||a==MountKind.HORSE&&b==MountKind.DONKEY||a==MountKind.DONKEY&&b==MountKind.HORSE);
    }

    private static Parent parent(LivingEntity mount) {
        if(mount instanceof HorseEntity){
            var d=HorseGeneticsClientData.get(mount.getId());
            return d==null?null:new Parent(d.male(),d.breedsRemaining(),d.breedingCooldownTicks(),d.ancestry(),
                    new double[]{d.geneticHealth(),d.geneticSpeed(),d.geneticJump(),d.geneticEvasion(),d.geneticEndurance(),d.geneticArmor()},d.trait());
        }
        var d=MountClientData.get(mount.getId());
        return d==null?null:new Parent(d.male(),d.breedsRemaining(),d.breedingCooldownTicks(),d.ancestry(),
                MountKind.of(mount)==MountKind.LLAMA?new double[]{d.health(),d.speed(),d.power(),d.utility(),d.armor()}
                        :new double[]{d.health(),d.speed(),d.power(),d.endurance(),d.utility(),d.armor()},d.trait());
    }

    static String blockReason(LivingEntity first,LivingEntity second) {
        var a=parent(first);var b=parent(second);
        if(a==null||b==null)return "Syncing partner information";
        if(first.isBaby()||second.isBaby())return "Both mounts must be adults";
        if(first instanceof AbstractHorseEntity horse&&!horse.isTame())return "Current mount is untamed";
        if(second instanceof AbstractHorseEntity horse&&!horse.isTame())return "Partner is untamed";
        if(a.male==b.male)return "Same sex";
        if(a.attempts<=0)return "Current mount has no attempts left";
        if(b.attempts<=0)return "Partner has no attempts left";
        if(a.cooldown>0)return "Current cooldown: "+formatTime(a.cooldown);
        if(b.cooldown>0)return "Partner cooldown: "+formatTime(b.cooldown);
        if(areRelated(first.getUuid(),a.ancestry,second.getUuid(),b.ancestry))return "Related bloodlines";
        return null;
    }

    static List<StatLine> lines(LivingEntity first,LivingEntity second,int index,int count) {
        var out=new ArrayList<StatLine>();
        out.add(new StatLine("Partner "+(index+1)+"/"+count,second.getName().getString(),0xffa3d5ff));
        var a=parent(first);var b=parent(second);
        if(a==null||b==null){out.addAll(syncingLines());return out;}
        var reason=blockReason(first,second);
        out.add(new StatLine("Compatibility",reason==null?"Compatible":reason,reason==null?0xffa8e6a3:0xffff8a80));
        if(reason==null){var ready=readiness(first,second);out.add(new StatLine("Breeding",ready==null?"Ready to breed":ready,ready==null?0xffa8e6a3:0xffbbbbbb));}
        out.add(new StatLine("Partner Sex",b.male?"Male":"Female",0xffbbbbbb));
        out.add(new StatLine("Partner Attempts",Integer.toString(b.attempts),0xffbbbbbb));
        out.add(new StatLine("Partner Cooldown",formatTime(b.cooldown),0xffbbbbbb));
        if(first instanceof HorseEntity horse&&second instanceof HorseEntity mate){
            out.addAll(buildPairingPreview(horse,HorseGeneticsClientData.get(first.getId()),mate,
                    HorseGeneticsClientData.get(second.getId()),second.getName().getString(),index,count).stream()
                    .filter(line->!line.name.startsWith("Mate")&&!line.name.equals("Compatibility")&&!line.name.equals("Breeding")).toList());
            return out;
        }
        if(MountKind.of(first)!=MountKind.of(second)){
            var horse=first instanceof HorseEntity?a:b;
            var donkey=first instanceof HorseEntity?b:a;
            double[] genes={(horse.genes[0]+donkey.genes[0])*.5+3.75,(horse.genes[1]+donkey.genes[1])*.5,
                    horse.genes[2]*.92,(horse.genes[4]+donkey.genes[2])*.5+8,donkey.genes[4]*.75,
                    (horse.genes[5]+donkey.genes[5])*.5+6};
            out.add(new StatLine("Offspring","Mule (sterile hybrid)",0xffa3d5ff));
            var labels=MountManager.geneLabels(MountKind.MULE);
            for(int i=0;i<genes.length;i++)out.add(new StatLine(labels[i],value(MountKind.MULE,i,clamp(MountKind.MULE,i,genes[i])),0xffbbbbbb));
            out.add(new StatLine("Trait","20% Hybrid Vigor; otherwise either parent's trait",0xffbbbbbb));
            return out;
        }
        var kind=MountKind.of(first);var labels=MountManager.geneLabels(kind);
        out.add(new StatLine("Offspring Genes","Potential range before mutations",0xffa3d5ff));
        for(int i=0;i<labels.length;i++){
            double span=MountGeneticRatings.breedingMaximum(kind,i)-MountGeneticRatings.breedingMinimum(kind,i);
            double low=clamp(kind,i,Math.min(a.genes[i],b.genes[i])+span*.005);
            double high=clamp(kind,i,Math.max(a.genes[i],b.genes[i])+span*.02);
            out.add(new StatLine(labels[i],value(kind,i,low)+" - "+value(kind,i,high),0xffbbbbbb));
        }
        out.add(new StatLine("Inheritance","70% better-parent chance per gene; at least three favor the better parent",0xffbbbbbb));
        out.add(new StatLine("Mutation","5% per gene; may raise or lower the previewed values",0xffbbbbbb));
        out.add(new StatLine("Parent Traits",a.trait+" / "+b.trait,0xffa8e6a3));
        return out;
    }

    private static double clamp(MountKind kind,int index,double value){
        return Math.clamp(value,MountGeneticRatings.breedingMinimum(kind,index),MountGeneticRatings.breedingMaximum(kind,index));
    }

    static String readiness(LivingEntity first,LivingEntity second){
        for(var mount:List.of(first,second)){
            boolean horse=mount instanceof HorseEntity;
            var h=horse?HorseGeneticsClientData.get(mount.getId()):null;
            var d=horse?null:MountClientData.get(mount.getId());
            if(h==null&&d==null)return "Syncing readiness";
            String which=mount==first?"Current mount":"Partner";
            if(!breedingPermission(horse?h.ownerUuid():d.ownerUuid(),horse?h.breedingAccessMode():d.breedingAccessMode()))return which+": breeding permission required";
            if((horse?h.vanillaBreedingCooldown():d.vanillaBreedingCooldown())>0)return which+": recovering after breeding";
            if(mount.hasPassengers()||mount.hasVehicle())return which+": dismount before breeding";
            if(mount instanceof AbstractHorseEntity&&mount.getHealth()<mount.getMaxHealth())return which+": needs healing";
        }
        for(var mount:List.of(first,second)){
            boolean love=mount instanceof HorseEntity?HorseGeneticsClientData.get(mount.getId()).inLove():MountClientData.get(mount.getId()).inLove();
            if(!love)return (mount==first?"Current mount":"Partner")+": feed "+food(MountKind.of(mount));
        }
        return first.squaredDistanceTo(second)>9?"Bring the parents together":null;
    }
    private static boolean breedingPermission(UUID owner,String mode){
        var player=net.minecraft.client.MinecraftClient.getInstance().player;
        return player!=null&&(player.isCreative()||owner==null||owner.equals(player.getUuid())||MountAccessMode.fromId(mode)==MountAccessMode.EVERYONE
                ||MountAccessMode.fromId(mode)==MountAccessMode.PARTY&&com.cranks.dungeons.client.party.PartyClientData.getMembers().stream().anyMatch(member->member.uuid().equals(owner)));
    }
    private static String food(MountKind kind){return switch(kind){case HORSE,DONKEY -> "a golden carrot or golden apple";case CAMEL -> "cactus";case LLAMA -> "a hay bale";case STRIDER -> "warped fungus";default -> "breeding food";};}
    private static String value(MountKind kind,int index,double value){
        if(index==1)return String.format(Locale.US,"%.1f blocks/s",value*42.16);
        if(kind==MountKind.LLAMA&&index==2)return String.format(Locale.US,"%.1f%%",value*100);
        if(kind==MountKind.LLAMA&&index==3)return Long.toString(Math.round(value));
        return format(value);
    }
}

package com.cranks.dungeons.progression;
import java.util.*;
public final class ProgressionRegression {
    public static void run() {
        for(int n=1;n<=50;n++) {
            double multiplier=ProgressionMath.partyMultiplier(n);
            require(multiplier>=1&&multiplier<=2,"Party bonus out of bounds");
            if(n>1)require(multiplier/n<1,"Party individual XP exceeds solo");
            long pool=(long)(5*ProgressionMath.UNIT*multiplier);
            for(int rotation=0;rotation<n;rotation++) {
                var shares=ProgressionMath.split(pool,n,rotation);
                require(Arrays.stream(shares).sum()==pool,"Party split created or lost fixed-point XP");
                require(Arrays.stream(shares).max().orElseThrow()-Arrays.stream(shares).min().orElseThrow()<=1,"Unfair party split");
            }
        }
        require(ProgressionMath.split(5_000_000,2,0)[0]==2_500_000,"Small XP fractions disappeared");
        require(ProgressionMath.boosted(1_000_000,.125)==1_125_000,"Personal XP bonus rounded away");
        require(ProgressionMath.boosted(1_000_000,Double.NaN)==1_000_000,"Invalid bonus corrupted XP");
        require(ProgressionMath.deathPenalty(25_000_000,100)==0,"Death caused negative XP");
        require(ProgressionMath.deathPenalty(80_000_000,100)==50_000_000,"Death penalty changed level or wrong fraction");
        require(DiscoveryFamilies.resource("minecraft:iron_ingot").equals(DiscoveryFamilies.resource("minecraft:deepslate_iron_ore")),"Ore/ingot discovery paid twice");
        require(DiscoveryFamilies.resource("minecraft:wheat").equals(DiscoveryFamilies.resource("minecraft:wheat_seeds")),"Crop/seed discovery paid twice");
        require(DiscoveryFamilies.recipe("minecraft:oak_stairs").equals(DiscoveryFamilies.recipe("minecraft:cherry_stairs")),"Wood variant recipe XP multiplied");
        require(DiscoveryFamilies.recipe("minecraft:red_wool").equals(DiscoveryFamilies.recipe("minecraft:blue_wool")),"Colour variant recipe XP multiplied");
        var encounter=new CombatEncounter(); var one=UUID.randomUUID(); var two=UUID.randomUUID();
        encounter.damage(one,4,0,10,600); encounter.damage(two,100,1,10,600);
        require(encounter.hits.get(two).damage()==6,"Overkill inflated damage share");
        encounter.damage(one,3,602,10,600);
        require(encounter.hits.size()==1&&encounter.hits.get(one).damage()==3,"Expired damage claimed a new fight");
        encounter.damage(one,Float.NaN,603,10,600);
        require(encounter.hits.get(one).damage()==3,"Invalid damage contaminated encounter");
    }
    private static void require(boolean condition,String error) { if(!condition)throw new AssertionError(error); }
}

package com.cranks.dungeons.equipment;

import java.util.List;
import java.util.stream.IntStream;

/** Pure tier arithmetic shared by crafting, catalogues and regression checks. */
public final class RuneRollRules {
    private RuneRollRules() {}
    public static List<Integer> tiers(int cap,int strength){
        int best=Math.clamp(cap,1,10),worst=Math.max(best,10-Math.clamp(strength,0,2));
        return IntStream.rangeClosed(best,worst).boxed().toList();
    }
    public static double weight(int tier){return switch(tier){
        case 1->.02;case 2->.10;case 3->.25;case 4->.45;case 5->.65;
        case 6->.85;case 7->1;case 8->.85;case 9->.65;case 10->.40;default->0;};}
}

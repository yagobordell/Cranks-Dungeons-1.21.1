package com.cranks.dungeons.equipment;

import java.util.List;

public final class RuneRollRulesRegression {
    public static void run(){
        for(int cap=1;cap<=10;cap++)for(int strength=0;strength<=2;strength++){
            var tiers=RuneRollRules.tiers(cap,strength);
            require(!tiers.isEmpty()&&tiers.getFirst()==cap,"cap disappeared");
            for(int tier:tiers)require(tier>=cap&&tier<=10&&RuneRollRules.weight(tier)>0,"tier escaped bounds");
        }
        require(RuneRollRules.tiers(10,2).equals(List.of(10)),"Perfect tome bypassed T10 cap");
        require(RuneRollRules.tiers(9,2).equals(List.of(9)),"Perfect tome bypassed crossbow/iron cap");
        require(RuneRollRules.tiers(8,0).equals(List.of(8,9,10)),"ordinary diamond range");
        require(RuneRollRules.tiers(8,1).equals(List.of(8,9)),"Greater diamond range");
        require(RuneRollRules.tiers(8,2).equals(List.of(8)),"Perfect diamond range");
        require(RuneRollRules.weight(0)==0&&RuneRollRules.weight(11)==0,"invalid tier weighted");
        System.out.println("Rune cap and tome-strength regression checks passed");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

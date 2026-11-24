package com.cranks.dungeons.gathering;

public final class GatheringRulesRegression {
    private GatheringRulesRegression() {}
    public static void run(){
        require(GatheringRules.extra(64,1,.9)==64,"full-stack yield was discarded");
        require(GatheringRules.extra(4,.25,.99)==1,"yield must scale item quantity, not duplicate the whole stack randomly");
        require(GatheringRules.extra(1,.25,.24)==1&&GatheringRules.extra(1,.25,.25)==0,"fractional yield rounding");
        require(GatheringRules.extra(64,Double.POSITIVE_INFINITY,0)==0,"non-finite yield");
        require(GatheringRules.extra(64,1000,.5)==192,"yield safety ceiling");
        require(Math.abs(GatheringRules.speed(.1)-1.1)<1e-9,"fraction-based gathering speed");
        require(GatheringRules.speed(-1)==1,"negative speed");
        require(GatheringRules.preservation(.5,.5)==.8,"preservation must retain repair costs");
        require(GatheringRules.weight(0,2)==0,"zero-weight excluded loot must remain excluded");
        require(GatheringRules.weight(100,.25)==125,"rare loot relative weights");
        require(GatheringRules.weight(100,-.5)==50,"unluck relative weights");
        require(GatheringRules.weight(100,100)==400,"bounded weighted loot");
        require(GatheringRules.weight(1,.1,.05)==2&&GatheringRules.weight(1,.1,.5)==1,"small positive weights must not lose their bonus");
        require(GatheringRules.weight(0,3,0)==0,"fractional weighting resurrected a zero-weight choice");
        System.out.println("Gathering arithmetic regression checks passed");
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}

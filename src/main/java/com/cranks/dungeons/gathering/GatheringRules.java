package com.cranks.dungeons.gathering;

/** Shared bounded arithmetic; all percentage attributes use fractions (0.1 = 10%). */
public final class GatheringRules {
    private GatheringRules() {}
    public static double bounded(double value,double maximum) {
        return Double.isFinite(value)?Math.clamp(value,0,maximum):0;
    }
    public static int extra(int count,double yield,double roll) {
        double expected=Math.max(0,count)*bounded(yield,3);
        int whole=(int)Math.min(Integer.MAX_VALUE-1,expected);
        return whole+(roll<expected-whole?1:0);
    }
    public static double speed(double bonus) {return 1+bounded(bonus,3);}
    public static double preservation(double precision,double repair) {return bounded(precision+repair,.8);}
    public static int weight(int original,double bonus) {
        if(original<=0)return 0;
        if(!Double.isFinite(bonus))return original;
        return (int)Math.clamp(Math.round(original*Math.clamp(1+bonus,0,4)),0,Integer.MAX_VALUE/16);
    }
    public static int weight(int original,double bonus,double roll) {
        if(original<=0)return 0;
        if(!Double.isFinite(bonus))return original;
        double expected=Math.clamp(original*Math.clamp(1+bonus,0,4),0,Integer.MAX_VALUE/16);
        int whole=(int)expected;
        return whole+(roll<expected-whole?1:0);
    }
}

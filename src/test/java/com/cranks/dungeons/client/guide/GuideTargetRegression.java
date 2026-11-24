package com.cranks.dungeons.client.guide;

public final class GuideTargetRegression {
    public static void run(){
        for(var kind:GuideTarget.Kind.values()){
            var target=new GuideTarget(kind,"cranks-dungeons:test/path#section");
            check(GuideTarget.parse(target.encode()).orElseThrow().equals(target),"typed target did not round-trip");
        }
        check(GuideTarget.parse("stat/derived/critical_chance").orElseThrow().key().equals("derived/critical_chance"),"derived stat ID changed");
        var potion=GuideTarget.item("minecraft:potion~minecraft:water");
        check(GuideTarget.parse(potion.encode()).orElseThrow().equals(potion),"potion variant target changed");
        for(String bad:new String[]{"", "unknown/x", "item/", "item/ ","stat"})check(GuideTarget.parse(bad).isEmpty(),"invalid target accepted: "+bad);
        check(GuideTarget.parse("item/"+"x".repeat(257)).isEmpty(),"unbounded target accepted");
        System.out.println("Contextual guide target regression checks passed");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}

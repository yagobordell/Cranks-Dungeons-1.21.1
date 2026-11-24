package com.cranks.dungeons.stat.passive;

import java.util.Set;

public final class TreeAttributeRulesRegression {
    public static void run() {
        var allowed=Set.of("strength","riding_speed");
        require(TreeAttributeRules.supported("cranks-dungeons","strength",allowed),"Supported mod stat rejected");
        require(!TreeAttributeRules.supported("minecraft","strength",allowed),"Vanilla namespace reused a mod path");
        require(!TreeAttributeRules.supported("minecraft","generic.max_health",allowed),"Vanilla max health accepted");
        require(!TreeAttributeRules.supported("another_mod","riding_speed",allowed),"Foreign namespace accepted");
        require(!TreeAttributeRules.supported("cranks-dungeons","unknown",allowed),"Unsupported mod stat accepted");
        require("strength".equals(TreeAttributeRules.normalizeChoice("strength")),"Bare choice rejected");
        require("riding_speed".equals(TreeAttributeRules.normalizeChoice(" Cranks-Dungeons:riding_speed ")),"Qualified mod choice rejected");
        for(String raw:new String[]{"minecraft:strength","minecraft:riding_speed","another_mod:riding_speed",":strength","cranks-dungeons:","cranks-dungeons:minecraft:strength"}){
            require(TreeAttributeRules.normalizeChoice(raw)==null,"Bad choice namespace accepted: "+raw);
        }
        require("".equals(TreeAttributeRules.normalizeChoice(" ")),"Clearing choice rejected");
        System.out.println("Tree attribute namespace and choice validation regression checks passed");
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}

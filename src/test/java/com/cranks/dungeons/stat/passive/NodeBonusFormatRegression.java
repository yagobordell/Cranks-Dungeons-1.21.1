package com.cranks.dungeons.stat.passive;

public final class NodeBonusFormatRegression {
    public static void run() {
        check("bonus_armor",10,"+10 to Armor");
        check("energy_shield",5,"+5 to maximum Energy Shield");
        check("attack_speed",0.03,"3% increased Attack Speed");
        check("evasion_rating_percent",0.12,"12% increased Evasion Rating");
        check("movement_speed",-0.04,"4% reduced Movement Speed");
        check("max_health_regeneration_percent",0.002,"Regenerate 0.2% of maximum Health per second");
        check("cold_extra_percent",0.05,"Gain 5% of Physical Damage as Extra Cold Damage");
        check("crit_chance",0.05,"+5% to Critical Hit Chance");
        check("global_crit_chance_percent",0.1,"10% increased Critical Hit Chance");
        check("crit_multiplier",0.15,"+15% to Critical Damage Bonus");
        check("riding_speed",0.125,"12.5% increased Mount Movement Speed");
        check("mount_endurance",0.2,"20% reduced Mount Sprint Resource Drain");
        check("breaking_range",0.5,"+0.5 metres to Block Interaction Reach");
        check("strength",0,"+0 to Strength");
        check("dexterity",-5,"-5 to Dexterity");
        if(!NodeBonusFormat.number(0.00001).equals("0.00001"))throw new AssertionError("Small bonuses lost precision");
        try{NodeBonusFormat.format("strength",Double.NaN);throw new AssertionError("Invalid value formatted");}
        catch(IllegalArgumentException expected){}
        System.out.println("Automatic tree bonus description regression checks passed");
    }
    private static void check(String id,double value,String expected){
        String actual=NodeBonusFormat.format("cranks-dungeons:"+id,value);
        if(!expected.equals(actual))throw new AssertionError(id+": "+actual);
    }
}

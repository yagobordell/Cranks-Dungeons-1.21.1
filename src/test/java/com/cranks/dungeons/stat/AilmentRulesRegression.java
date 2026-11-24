package com.cranks.dungeons.stat;

import com.cranks.dungeons.ailment.Ailment;
import com.cranks.dungeons.ailment.AilmentRules;
import com.cranks.dungeons.ailment.AilmentType;
import java.util.UUID;

public final class AilmentRulesRegression {
    public static void run() {
        near(AilmentRules.penetratedResistance(.5,.5),.25,"relative penetration");
        near(AilmentRules.penetratedResistance(.5,2),0,"penetration cap");
        near(AilmentRules.penetratedResistance(-.2,.5),-.2,"negative resistance");
        near(AilmentRules.criticalChance(.05,.5,1,.1),.25,"effective crit chance");
        near(AilmentRules.criticalChance(1,1,1,1),1,"crit cap");
        near(AilmentRules.bleedFraction(.5),.6,"bleed magnitude");
        near(AilmentRules.bleedFraction(100),1,"bleed cap");
        near(AilmentRules.chillSlow(.65,1),.65,"single chill calculation");
        near(AilmentRules.chillSlow(.9,10),.9,"chill cap");
        near(.25*AilmentRules.FIRE_BUILDUP_PER_HEALTH_FRACTION,1,"Burn threshold at 25% health damage");
        UUID first=UUID.randomUUID(),second=UUID.randomUUID();
        Ailment older=new Ailment(AilmentType.POISON,2,10,first,null,null,0,1);
        double damage=0;
        for(int i=0;i<20;i++)damage+=older.advance(.05,false);
        Ailment newer=new Ailment(AilmentType.POISON,2,20,second,null,null,0,1);
        for(int i=0;i<20;i++){damage+=older.advance(.05,false);newer.advance(.05,false);}
        require(older.isExpired()&&!newer.isExpired(),"new poison refreshed older stack");
        near(damage,20,"poison DPS and exact expiry");
        require(older.getSourceUuid().equals(first)&&newer.getSourceUuid().equals(second),"poison owner merged");
        Ailment shortBurn=new Ailment(AilmentType.BURN,.2,10,first,null,null,0,1);
        double partial=0;for(int i=0;i<4;i++)partial+=shortBurn.advance(.05,false);
        near(partial,2,"partial final damage tick");
        Ailment wetBurn=new Ailment(AilmentType.BURN,4,10,first,null,null,0,1);
        double wet=0;for(int i=0;i<40;i++)wet+=wetBurn.advance(.05,true);
        near(wet,10,"wet burn damage and duration");require(wetBurn.isExpired(),"wet burn expiry");
        System.out.println("Ailment regression checks passed");
    }
    private static void near(double actual,double expected,String label){require(Math.abs(actual-expected)<.00001,label+": "+actual+" != "+expected);}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

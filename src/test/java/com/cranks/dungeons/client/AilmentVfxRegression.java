package com.cranks.dungeons.client;

import com.cranks.dungeons.client.render.AilmentVfxRules;

public final class AilmentVfxRegression {
    public static void run() {
        int coincident=0;
        for(int id=0;id<50;id++) {
            int count=0;
            for(int tick=0;tick<280;tick++) if(AilmentVfxRules.due(tick,id,28,7)) count++;
            require(count==10,"ambient cadence depends on entity ID");
            if(AilmentVfxRules.due(100,id,28,7))coincident++;
        }
        require(coincident<5,"crowd sparks are synchronized");
        require(AilmentVfxRules.buildupInterval(10)>AilmentVfxRules.buildupInterval(50),"weak buildup is too noisy");
        require(AilmentVfxRules.buildupInterval(50)>AilmentVfxRules.buildupInterval(90),"near threshold has no extra cue");
        require(AilmentVfxRules.applicationFade(-1)==0,"pulse before application");
        require(AilmentVfxRules.applicationFade(12)==0,"pulse did not end");
        require(AilmentVfxRules.applicationFade(6)>AilmentVfxRules.applicationFade(0),"pulse has no gentle onset");
        for(int age=0;age<12;age++) require(AilmentVfxRules.applicationFade(age)>=0&&AilmentVfxRules.applicationFade(age)<=1,"unbounded edge pulse");
        System.out.println("Ailment VFX cadence and feedback regression checks passed");
    }
    private static void require(boolean value,String message) { if(!value)throw new AssertionError(message); }
}

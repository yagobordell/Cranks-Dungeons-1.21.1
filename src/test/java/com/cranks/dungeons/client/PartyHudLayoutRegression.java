package com.cranks.dungeons.client;
import com.cranks.dungeons.client.party.PartyHudLayout;
public final class PartyHudLayoutRegression {
    public static void run(){
        for(int[] screen:new int[][]{{320,240},{427,240},{342,256},{480,270},{800,450},{1280,720}})
            for(float scale:new float[]{.5f,1,2})for(int members:new int[]{1,10,20,50}){
                var layout=PartyHudLayout.fit(screen[0],screen[1],1900,1900,5,2,members,scale);
                check(layout.x()>=0&&layout.y()>=0&&layout.x()+layout.width()<=screen[0]&&layout.y()+layout.height()<=screen[1],"party leaves viewport");
                check(layout.rows()*layout.columns()>=members,"party member hidden by configured grid limit");
                check(layout.scale()>0&&layout.scale()<=scale,"fit enlarges requested HUD scale");
            }
        var normal=PartyHudLayout.fit(1920,1080,17,190,5,2,10,1);
        check(normal.x()==17&&normal.y()==190&&normal.scale()==1,"fitting moves a HUD that already fits");
        var defaults=PartyHudLayout.fit(427,240,-1,-1,5,2,10,1);
        check(defaults.x()==5&&defaults.y()+defaults.height()<=240,"default party clips on high GUI scale");
        System.out.println("Responsive party HUD bounds and full-member visibility regression checks passed");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}

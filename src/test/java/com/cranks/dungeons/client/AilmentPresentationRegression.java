package com.cranks.dungeons.client;

import com.cranks.dungeons.client.render.AilmentPresentation;

public final class AilmentPresentationRegression {
    public static void run() {
        require(AilmentPresentation.hasTimer(false,4),"finite timer hidden");
        require(!AilmentPresentation.hasTimer(true,4),"buildup has a duration timer");
        require(!AilmentPresentation.hasTimer(false,-1),"infinite timer shown");
        require(!AilmentPresentation.hasTimer(false,Float.POSITIVE_INFINITY),"infinite duration shown");
        require(AilmentPresentation.tileHeight(false,12,4)==32,"stack and timer overlap");
        require(AilmentPresentation.tileHeight(false,12,-1)==24,"infinite stack reserves timer space");
        require(AilmentPresentation.tileHeight(false,1,-1)==16,"infinite single reserves footer space");
        require(AilmentPresentation.tileHeight(true,67,0)==24,"buildup footer size");
        require(AilmentPresentation.remaining(4,100,120)==3,"snapshot did not count down");
        require(AilmentPresentation.remaining(4,100,100)==4,"paused clock advanced");
        require(AilmentPresentation.remaining(4,100,90)==4,"clock reset advanced timer");
        require(AilmentPresentation.remaining(1,100,140)==0,"negative timer");
        require(AilmentPresentation.remaining(-1,100,140)==-1,"infinity was decremented");
        equal(AilmentPresentation.timer(.01f),"0.1s");
        equal(AilmentPresentation.timer(.5f),"0.5s");
        equal(AilmentPresentation.timer(4),"4.0s");
        equal(AilmentPresentation.timer(10),"10s");
        equal(AilmentPresentation.timer(61),"1:01");
        equal(AilmentPresentation.timer(3600),"1h");
        equal(AilmentPresentation.timer(-1),"");
        System.out.println("Ailment presentation regression checks passed");
    }
    private static void equal(String actual,String expected){require(actual.equals(expected),actual+" != "+expected);}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

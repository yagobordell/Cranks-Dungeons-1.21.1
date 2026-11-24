package com.cranks.dungeons.mount;

public final class MountMotionRegression {
    public static void run(){
        var state=new MountMotionState();
        double last=0;
        for(int i=0;i<16;i++){double value=state.approach(1,.065,.13);require(value>=last&&value-last<=.06500001,"Acceleration jumped");last=value;}
        require(state.throttle==1,"Full forward pace never reached");
        for(int i=0;i<8;i++)state.approach(0,.065,.13);
        require(state.throttle==0,"Release failed to brake");
        state.throttle=.8;boolean reachedZero=false;
        for(int i=0;i<20;i++){double value=state.approach(-1,.065,.13);if(value==0)reachedZero=true;if(value<0)require(reachedZero,"Reversed without stopping");}
        require(state.throttle==-.25,"Reverse speed is not bounded");
        state.approach(Double.NaN,.065,.13);require(Double.isFinite(state.throttle),"Invalid input corrupted motion");
        state.jumping=true;state.jumpGrace=8;state.turn=2;state.reset();
        require(state.throttle==0&&state.turn==0&&!state.jumping&&state.jumpGrace==0,"Rider reset retained motion state");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}

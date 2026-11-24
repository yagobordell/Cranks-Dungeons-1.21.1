package com.cranks.dungeons.mount;

import java.util.UUID;

/** Runtime input state only; never changes a mount's genes or saved progression. */
public final class MountMotionState {
    public UUID rider;
    public double throttle;
    public float turn;
    public boolean jumping;
    public int jumpGrace;
    public int lastAge=-1;

    public void reset(){rider=null;throttle=0;turn=0;jumping=false;jumpGrace=0;lastAge=-1;}

    public double approach(double requested,double acceleration,double brake){
        requested=Double.isFinite(requested)?Math.clamp(requested,-.25,1):0;
        boolean reversing=throttle*requested<0;
        double target=reversing?0:requested;
        double change=Math.abs(target)>Math.abs(throttle)&&!reversing?acceleration:brake;
        throttle+=Math.clamp(target-throttle,-change,change);
        if(Math.abs(throttle)<.0001)throttle=0;
        return throttle;
    }
}

package com.cranks.dungeons.client.guide;

public record GuideHit(int x,int y,int width,int height,GuideTarget target) {
    public boolean contains(double px,double py){return px>=x&&px<x+width&&py>=y&&py<y+height;}
}

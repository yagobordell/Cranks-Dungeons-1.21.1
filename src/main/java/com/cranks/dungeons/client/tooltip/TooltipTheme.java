package com.cranks.dungeons.client.tooltip;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;

public final class TooltipTheme {
    private TooltipTheme() {}
    public static int color(CustomRarity rarity) {
        return switch (rarity) {
            case NORMAL -> 0xDFDFE3;
            case MAGIC -> 0x799FE9;
            case RARE -> 0xDBC65E;
            case UNIQUE -> 0xD68A4A;
        };
    }

    public static void frame(DrawContext ctx, int x, int y, int width, int height, int depth, CustomRarity rarity) {
        x -= 6; y -= 6; width += 12; height += 12;
        int[][] points = height<32||width<70
                ? new int[][]{{4,0},{width-4,0},{width,4},{width,height-4},{width-4,height},{0,height},{0,4},{4,4},{4,0}}
                : new int[][]{{12,0},{width-4,0},{width,4},{width,height-11},{width-2,height-11},
                {width-2,height-6},{width-6,height-6},{width-6,height},{11,height},{11,height-2},
                {0,height-2},{0,12},{-2,12},{-2,4},{2,4},{2,0},{12,0}};
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x,y,depth);
        // Fill the actual contour, so the stepped corners never leave a rectangular black fringe.
        for(int row=0;row<height;row++) {
            var intersections=new ArrayList<Integer>();
            for(int i=1;i<points.length;i++) {
                int[] a=points[i-1],b=points[i];
                if((a[1]<=row && b[1]>row)||(b[1]<=row && a[1]>row))
                    intersections.add((int)Math.floor(a[0]+(row+.5-a[1])*(b[0]-a[0])/(double)(b[1]-a[1])));
            }
            Collections.sort(intersections);
            for(int i=1;i<intersections.size();i+=2)ctx.fill(intersections.get(i-1),row,intersections.get(i)+1,row+1,0xFF161719);
        }
        stroke(ctx,points,0xFF000000,3);
        stroke(ctx,points,0xFF000000|color(rarity),1);
        ctx.getMatrices().pop();
    }

    private static void stroke(DrawContext ctx,int[][] points,int color,int size) {
        for(int i=1;i<points.length;i++) {
            int x=points[i-1][0],y=points[i-1][1],tx=points[i][0],ty=points[i][1];
            if(x==tx||y==ty) {
                ctx.fill(Math.min(x,tx)-size/2,Math.min(y,ty)-size/2,
                        Math.max(x,tx)+size-size/2,Math.max(y,ty)+size-size/2,color);
            } else {
                int dx=Math.abs(tx-x),dy=-Math.abs(ty-y),sx=x<tx?1:-1,sy=y<ty?1:-1,error=dx+dy;
                while(true){ctx.fill(x-size/2,y-size/2,x+size-size/2,y+size-size/2,color);
                    if(x==tx&&y==ty)break;int e=2*error;if(e>=dy){error+=dy;x+=sx;}if(e<=dx){error+=dx;y+=sy;}}
            }
        }
    }
}

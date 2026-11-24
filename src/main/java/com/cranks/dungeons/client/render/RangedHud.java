package com.cranks.dungeons.client.render;

import com.cranks.dungeons.client.RangedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Identifier;

public final class RangedHud {
    // Native GUI pixels: feathered tail, two-pixel shaft and a broad arrowhead.
    private static final String[] ARROW = {
            "##..........#...",
            ".##.........##..",
            "..##############",
            "..##############",
            ".##.........##..",
            "##..........#..."
    };
    private static final int ARROW_WIDTH=ARROW[0].length();
    private RangedHud() {}
    public static float progress(MinecraftClient client) {
        if(client.player==null || client.player.isSpectator() || client.currentScreen!=null)return -1;
        var player=client.player;
        if(player.isUsingItem()) {
            var weapon=player.getActiveItem();
            if(weapon.getItem() instanceof BowItem)return Math.clamp(player.getItemUseTime()/20f,0,1);
            if(weapon.getItem() instanceof CrossbowItem)return CrossbowItem.isCharged(weapon)?1:
                    Math.clamp(player.getItemUseTime()/(float)CrossbowItem.getPullTime(weapon,player),0,0.99f);
        }
        return player.getMainHandStack().getItem() instanceof CrossbowItem && CrossbowItem.isCharged(player.getMainHandStack())?1:-1;
    }
    public static void crosshair(DrawContext context, MinecraftClient client) {
        float progress=progress(client);
        if(progress<0 || client.options.hudHidden)return;
        int x=context.getScaledWindowWidth()/2,y=context.getScaledWindowHeight()/2;
        context.drawGuiTexture(Identifier.ofVanilla("hud/crosshair"),x-7,y-7,15,15);
        if(client.options.getAttackIndicator().getValue()!=AttackIndicator.CROSSHAIR)return;
        drawArrow(context,x-8,y+10,progress,false);
        if(progress>=1 && RangedClient.readyFlash())context.fill(x-8,y+17,x+8,y+18,0xB3DADADA);
    }

    /** Translucent dark silhouette and pale fill, matching the sword indicator's softer appearance. */
    public static void drawArrow(DrawContext context, int left, int top, float progress, boolean vertical) {
        int filled=Math.clamp((int)(Math.clamp(progress,0,1)*ARROW_WIDTH),0,ARROW_WIDTH);
        for(int row=0;row<ARROW.length;row++) {
            for(int column=0;column<ARROW_WIDTH;column++) {
                if(ARROW[row].charAt(column)!='#')continue;
                int color=column<filled ? (column<3?0xB3D7D9EA:0xB3FFFFFF) : 0xB33A3B3C;
                int px=left+(vertical?row:column), py=top+(vertical?ARROW_WIDTH-1-column:row);
                context.fill(px,py,px+1,py+1,color);
            }
        }
    }
}

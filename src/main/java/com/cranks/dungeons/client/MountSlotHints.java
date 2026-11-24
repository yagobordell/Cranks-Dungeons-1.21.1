package com.cranks.dungeons.client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import java.util.*;

/** Monochrome silhouettes based on Minecraft's native item shapes. */
final class MountSlotHints {
    private static final Map<String,boolean[]> MASKS=new HashMap<>();
    static void draw(DrawContext c,String kind,int x,int y){
        if(kind.equals("carpet")){
            int color=0xff555555;c.fill(x+3,y+6,x+13,y+11,color);c.fill(x+1,y+8,x+15,y+12,color);c.fill(x+3,y+12,x+13,y+13,color);return;
        }
        var mask=MASKS.computeIfAbsent(kind,MountSlotHints::load);
        for(int row=0;row<16;row++)for(int col=0;col<16;col++)if(mask[row*16+col])c.fill(x+col,y+row,x+col+1,y+row+1,0xff555555);
    }
    private static boolean[] load(String kind){
        boolean[] pixels=new boolean[256];var id=Identifier.ofVanilla("textures/item/"+(kind.equals("saddle")?"saddle":"iron_horse_armor")+".png");
        try(var input=MinecraftClient.getInstance().getResourceManager().getResource(id).orElseThrow().getInputStream();var image=NativeImage.read(input)){
            for(int y=0;y<16;y++)for(int x=0;x<16;x++)pixels[y*16+x]=(image.getColor(x*image.getWidth()/16,y*image.getHeight()/16)>>>24)>127;
        }catch(java.io.IOException|NoSuchElementException ignored){}
        return pixels;
    }
}

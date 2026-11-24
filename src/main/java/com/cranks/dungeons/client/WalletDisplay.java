package com.cranks.dungeons.client;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import com.cranks.dungeons.component.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;

/** A compact wallet control, separate from all item slots. */
final class WalletDisplay {
    static final int WIDTH = 64;
    static final int HEIGHT = 22;
    private static final Identifier COIN=Identifier.of("cranks-dungeons","textures/gui/wallet_coin.png");
    private int x,y;
    static String full(long coins){return String.format(Locale.ROOT,"%,d",coins);}
    static String compact(long coins){
        if(coins<1_000_000)return full(coins);
        String[] units={"M","B","T","Q"};double value=coins/1_000_000.0;int unit=0;
        while(value>=1000&&unit<units.length-1){value/=1000;unit++;}
        return String.format(Locale.ROOT,value>=100||value==(long)value?"%.0f%s":"%.1f%s",value,units[unit]);
    }
    void render(DrawContext context,TextRenderer font,int left,int top,int mx,int my){
        var player=MinecraftClient.getInstance().player;if(player==null)return;
        String text=compact(ModComponents.WALLET.get(player).balance());x=left;y=top;
        InventoryStyle.drawPanel(context,x,y,WIDTH,HEIGHT,false,InventoryStyle.Panel.JOURNAL);
        if(contains(mx,my))context.fill(x+4,y+4,x+WIDTH-4,y+HEIGHT-4,0xFF222222);
        context.drawTexture(COIN,x+5,y+5,2,2,12,12,16,16);
        context.drawText(font,text,x+21,y+7,TradeClient.state().phase()==2?CompendiumStyle.TEXT_GOLD:CompendiumStyle.TEXT,false);
    }
    boolean contains(double mx,double my){return mx>=x&&mx<x+WIDTH&&my>=y&&my<y+HEIGHT;}
    void tooltip(DrawContext context,TextRenderer font,int mx,int my){
        var player=MinecraftClient.getInstance().player;
        if(player!=null&&contains(mx,my))context.drawTooltip(font,List.of(Text.literal(full(ModComponents.WALLET.get(player).balance())+" coins"),Text.literal("Kept on death").formatted(net.minecraft.util.Formatting.GRAY),Text.literal("Click to trade with a nearby player").formatted(net.minecraft.util.Formatting.GRAY)),mx,my);
    }
}

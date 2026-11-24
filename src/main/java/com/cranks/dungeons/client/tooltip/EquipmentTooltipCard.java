package com.cranks.dungeons.client.tooltip;

import com.cranks.dungeons.item.CustomRarity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** The card owns row layout; vanilla still positions the tooltip and renders foreign components. */
public final class EquipmentTooltipCard implements TooltipComponent {
    private record Row(EquipmentTooltipPart part,TooltipComponent foreign,List<OrderedText> lines,int y,int height) {}
    private final List<Row> rows=new ArrayList<>();
    public final CustomRarity rarity;
    private final ItemStack icon;
    private final int width,height,contentHeight;
    private final String identity;
    private static String scrollIdentity="";
    private static int scrollOffset,scrollMaximum;
    private static long lastRendered;

    public EquipmentTooltipCard(List<TooltipComponent> parts,TextRenderer renderer,ItemStack stack) {
        var title=(EquipmentTooltipPart)parts.getFirst();rarity=title.rarity;
        icon=!stack.isEmpty()?stack.copy():fallbackIcon(title.itemId);
        int natural=210;
        var key=new StringBuilder(title.itemId);
        for(var component:parts){natural=Math.max(natural,component.getWidth(renderer)+12);if(component instanceof EquipmentTooltipPart p)key.append(p.left.getString()).append(p.right.getString());}
        identity=key.toString();
        int maximum=Math.max(80,Math.min(350,MinecraftClient.getInstance().getWindow().getScaledWidth()-28));
        width=Math.min(natural,maximum);
        int y=4;
        for(var component:parts) {
            if(!(component instanceof EquipmentTooltipPart p)){int h=component.getHeight();rows.add(new Row(null,component,List.of(),y,h));y+=h;continue;}
            int right=renderer.getWidth(p.right);
            int available=width-12-(right==0?0:right+12)-(p.kind.equals("title")||p.kind.equals("rarity")?22:p.groupedRune()?8:0);
            List<OrderedText> lines=p.kind.equals("separator")?List.of():renderer.wrapLines(p.left,Math.max(24,available));
            int h=p.kind.equals("separator")?11:Math.max(1,lines.size())*10+2;
            if(p.kind.equals("section"))h+=4;
            if(p.kind.equals("rune")||p.kind.equals("rune_end"))h+=3;
            rows.add(new Row(p,null,lines,y,h));y+=h;
        }
        contentHeight=y+2;
        height=Math.min(contentHeight,Math.max(40,MinecraftClient.getInstance().getWindow().getScaledHeight()-34));
    }
    private static ItemStack fallbackIcon(String id) {
        Identifier parsed=Identifier.tryParse(id);
        return parsed==null?ItemStack.EMPTY:new ItemStack(Registries.ITEM.get(parsed));
    }
    @Override public int getWidth(TextRenderer renderer){return width;}
    @Override public int getHeight(){return height+2;}
    public String guideIdentity(){return identity;}

    @Override public void drawItems(TextRenderer renderer,int x,int y,DrawContext context) {
        boolean overflow=contentHeight>height;
        int viewport=height-(overflow?13:0);
        if(!identity.equals(scrollIdentity)){scrollIdentity=identity;scrollOffset=0;}
        scrollMaximum=Math.max(0,contentHeight-viewport);scrollOffset=Math.clamp(scrollOffset,0,scrollMaximum);
        lastRendered=net.minecraft.util.Util.getMeasuringTimeMs();
        context.enableScissor(x,y,x+width,y+viewport);
        try {
            for(var row:rows) {
                int ry=y+row.y()-scrollOffset;
                if(ry+row.height()<y||ry>y+viewport)continue;
                if(row.foreign()!=null){row.foreign().drawText(renderer,x+6,ry,context.getMatrices().peek().getPositionMatrix(),context.getVertexConsumers());row.foreign().drawItems(renderer,x+6,ry,context);continue;}
                var p=row.part();
                if(p.kind.equals("separator")) {
                    int mid=x+width/2;
                    context.fill(x+6,ry+4,mid-6,ry+5,0xFF424449);context.fill(mid+6,ry+4,x+width-6,ry+5,0xFF424449);
                    context.fill(mid-6,ry+4,mid-2,ry+5,0xFF77787C);context.fill(mid-2,ry+5,mid+3,ry+6,0xFF77787C);context.fill(mid+3,ry+4,mid+6,ry+5,0xFF77787C);
                    continue;
                }
                int textX=x+6, textY=ry+(p.kind.equals("section")?4:0);
                if(p.kind.equals("title")){if(!icon.isEmpty()&&ry>=y&&ry+16<=y+viewport){context.drawItem(icon,x+6,ry);
                    com.cranks.dungeons.client.guide.GuideInteraction.recordRegion(x+6,ry,16,16,com.cranks.dungeons.client.guide.GuideText.itemTarget(icon));}textX+=22;}
                if(p.kind.equals("rarity"))textX+=22;
                if(p.groupedRune()){
                    textX+=8;
                    int top=ry+(p.kind.equals("rune_start")?1:0);
                    int bottom=ry+row.height()-(p.kind.equals("rune_end")?5:0);
                    context.fill(x+7,top,x+8,bottom,0xFF626570);
                    if(p.kind.equals("rune_start"))context.fill(x+7,top,x+11,top+1,0xFF626570);
                    if(p.kind.equals("rune_end"))context.fill(x+7,bottom-1,x+11,bottom,0xFF626570);
                }
                for(int i=0;i<row.lines().size();i++){
                    int lineY=textY+10*i;
                    if(lineY>=y&&lineY+renderer.fontHeight<=y+viewport)context.drawTextWithShadow(renderer,row.lines().get(i),textX,lineY,0xFFE4E4E9);
                }
                int rightY=textY+10*Math.max(0,row.lines().size()-1);
                if(!p.right.getString().isBlank()&&rightY>=y&&rightY+renderer.fontHeight<=y+viewport)
                    context.drawTextWithShadow(renderer,p.right,x+width-6-renderer.getWidth(p.right),rightY,0xFFE4E4E9);
            }
        } finally {context.disableScissor();}
        if(overflow){context.fill(x+6,y+viewport+1,x+width-6,y+viewport+2,0xFF424449);
            context.drawTextWithShadow(renderer,"Alt + wheel to scroll",x+6,y+viewport+4,0xFF93959D);}
    }

    public static boolean scroll(double amount) {
        if(amount==0||scrollMaximum==0||net.minecraft.util.Util.getMeasuringTimeMs()-lastRendered>150)return false;
        scrollOffset=Math.clamp(scrollOffset-(int)Math.signum(amount)*24,0,scrollMaximum);return true;
    }
    public static void clearScrollTarget(){lastRendered=0;scrollMaximum=0;}
}

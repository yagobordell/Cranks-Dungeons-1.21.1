package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.client.RarityTooltipContext;
import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.client.tooltip.TooltipDetails;
import com.cranks.dungeons.mixin.client.GuideTooltipInvoker;
import com.cranks.dungeons.mixin.client.GuideTextTooltipAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Client-only interaction. It never opens/closes a screen handler or sends an inventory action. */
public final class GuideInteraction {
    private static Screen owner;
    private static int mx,my,windowWidth,windowHeight;
    private static boolean captured,replaying;
    private static Snapshot tooltip,recording;
    private static final List<GuideHit> regions=new ArrayList<>();
    private static int consumedButtons;
    private static final class Snapshot {
        final List<TooltipComponent> components;final TextRenderer font;final int anchorX,anchorY;final CustomRarity rarity;final String key;
        final List<GuideHit> hits=new ArrayList<>();
        int x,y,width,height;long touched;boolean pinned;
        Snapshot(List<TooltipComponent> components,TextRenderer font,int ax,int ay,String key){this.components=List.copyOf(components);this.font=font;anchorX=ax;anchorY=ay;this.key=key;rarity=RarityTooltipContext.get();touched=Util.getMeasuringTimeMs();}
        boolean inside(double px,double py){return px>=x-7&&px<x+width+7&&py>=y-7&&py<y+height+7;}
        boolean bridge(double px,double py){
            int nearX=Math.clamp(anchorX,x,x+width),nearY=Math.clamp(anchorY,y,y+height);
            return px>=Math.min(anchorX,nearX)-5&&px<=Math.max(anchorX,nearX)+5&&py>=Math.min(anchorY,nearY)-6&&py<=Math.max(anchorY,nearY)+6;
        }
    }
    private GuideInteraction(){}
    public static void register(){
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack,context,type,lines)->{
            GuideTomes.tooltip(stack,lines);
            if(!lines.isEmpty())lines.set(0,GuideText.link(lines.getFirst(),GuideText.itemTarget(stack)));
        });
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.resource.ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener(){
                    public net.minecraft.util.Identifier getFabricId(){return net.minecraft.util.Identifier.of("cranks-dungeons","contextual_guide");}
                    public void reload(net.minecraft.resource.ResourceManager manager){GuideBook.reload();reset();}
                });
        ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{
            ScreenMouseEvents.allowMouseClick(screen).register((s,x,y,b)->!click(s,x,y,b));
            ScreenMouseEvents.allowMouseRelease(screen).register((s,x,y,b)->{
                GuideOverlay.release(b);
                int mask=1<<Math.clamp(b,0,15);boolean consumed=(consumedButtons&mask)!=0;consumedButtons&=~mask;
                return !GuideOverlay.isOpen()&&!consumed;
            });
            ScreenMouseEvents.allowMouseScroll(screen).register((s,x,y,horizontal,vertical)->{
                if(GuideOverlay.isOpen()){GuideOverlay.scroll(vertical);return false;}
                if(TooltipDetails.isHeld()&&tooltip!=null&&tooltip.pinned&&tooltip.inside(x,y)){com.cranks.dungeons.client.tooltip.EquipmentTooltipCard.scroll(vertical);return false;}
                return true;
            });
            ScreenKeyboardEvents.allowKeyPress(screen).register((s,key,scan,modifiers)->{
                if(GuideOverlay.isOpen()){GuideOverlay.keyPressed(key,scan,modifiers);return false;}
                if(key==GLFW.GLFW_KEY_ESCAPE&&tooltip!=null&&tooltip.pinned){tooltip=null;return false;}
                return true;
            });
            ScreenKeyboardEvents.allowKeyRelease(screen).register((s,key,scan,modifiers)->!GuideOverlay.isOpen());
            ScreenEvents.remove(screen).register(s->{if(owner==s)reset();});
        });
    }
    public static void reset(){GuideOverlay.close();owner=null;tooltip=null;recording=null;regions.clear();consumedButtons=0;}
    public static void beginFrame(Screen screen,int mouseX,int mouseY){
        var window=MinecraftClient.getInstance().getWindow();
        if(owner!=screen||windowWidth!=window.getScaledWidth()||windowHeight!=window.getScaledHeight()){
            if(owner!=screen)GuideOverlay.close();tooltip=null;owner=screen;windowWidth=window.getScaledWidth();windowHeight=window.getScaledHeight();
        }
        mx=mouseX;my=mouseY;captured=false;regions.clear();
        if(GuideOverlay.isOpen()||!TooltipDetails.isHeld()){tooltip=null;return;}
        if(tooltip!=null){
            if(Math.abs(mx-tooltip.anchorX)<6&&Math.abs(my-tooltip.anchorY)<6){tooltip.pinned=false;tooltip.touched=Util.getMeasuringTimeMs();}
            else if(tooltip.inside(mx,my)||tooltip.bridge(mx,my)){tooltip.pinned=true;tooltip.touched=Util.getMeasuringTimeMs();}
            else if(Util.getMeasuringTimeMs()-tooltip.touched>220)tooltip=null;
        }
    }
    public static boolean maskUnderlyingPointer(){return GuideOverlay.isOpen()||TooltipDetails.isHeld()&&tooltip!=null&&tooltip.pinned&&(tooltip.inside(mx,my)||tooltip.bridge(mx,my));}
    public static int renderMouseX(int original){return GuideOverlay.isOpen()?-10000:maskUnderlyingPointer()?tooltip.anchorX:original;}
    public static int renderMouseY(int original){return GuideOverlay.isOpen()?-10000:maskUnderlyingPointer()?tooltip.anchorY:original;}
    public static boolean suppressTooltip(int x,int y){return !replaying&&(GuideOverlay.isOpen()||TooltipDetails.isHeld()&&tooltip!=null&&tooltip.pinned&&(tooltip.inside(mx,my)||tooltip.bridge(mx,my))
            &&(Math.abs(x-tooltip.anchorX)>3||Math.abs(y-tooltip.anchorY)>3));}
    public static void startTooltip(List<TooltipComponent> components,TextRenderer font,int x,int y){
        if(replaying||GuideOverlay.isOpen()||!TooltipDetails.isHeld())return;
        String key=key(components);boolean same=tooltip!=null&&tooltip.key.equals(key);
        recording=new Snapshot(components,font,same?tooltip.anchorX:x,same?tooltip.anchorY:y,key);
        recording.pinned=same&&tooltip.pinned;
    }
    private static String key(List<TooltipComponent> components){
        if(components.isEmpty())return "";var first=components.getFirst();
        if(first instanceof com.cranks.dungeons.client.tooltip.EquipmentTooltipCard card)return card.guideIdentity();
        if(first instanceof GuideTextTooltipAccessor accessor){var value=new StringBuilder();String[] target={null};accessor.cranks$guideText().accept((i,s,c)->{value.appendCodePoint(c);if(target[0]==null)GuideText.target(s).ifPresent(t->target[0]=t.encode());return true;});return target[0]!=null?target[0]:value.toString();}
        return first.getClass().getName()+":"+first.getWidth(MinecraftClient.getInstance().textRenderer);
    }
    public static int anchorX(int fallback){return recording==null?fallback:recording.anchorX;}
    public static int anchorY(int fallback){return recording==null?fallback:recording.anchorY;}
    public static void tooltipBounds(int x,int y,int width,int height){if(recording!=null){recording.x=x;recording.y=y;recording.width=width;recording.height=height;}}
    public static void finishTooltip(){
        if(recording!=null){if(!recording.hits.isEmpty()){tooltip=recording;captured=true;}recording=null;}
    }
    public static void abortTooltip(){recording=null;}
    public static void record(OrderedText text,TextRenderer font,int x,int y,DrawContext context){
        if(recording==null||replaying)return;
        collect(text,font,x,y,recording.hits,context);
    }
    public static void recordRegion(int x,int y,int width,int height,GuideTarget target){if(recording!=null&&!replaying)recording.hits.add(new GuideHit(x,y,width,height,target));}
    public static void collect(OrderedText text,TextRenderer font,int x,int y,List<GuideHit> destination,DrawContext context){
        float[] cursor={x};text.accept((index,style,codepoint)->{
            String character=new String(Character.toChars(codepoint));float advance=font.getTextHandler().getWidth(Text.literal(character).setStyle(style));
            int left=(int)Math.floor(cursor[0]),right=(int)Math.ceil(cursor[0]+advance),top=y,bottom=y+10;
            var target=GuideText.target(style);
            if(target.isPresent()&&advance>0&&(context==null||context.scissorContains((left+right)/2,y+4))){
                if(context!=null){while(top<bottom&&!context.scissorContains((left+right)/2,top))top++;while(bottom>top&&!context.scissorContains((left+right)/2,bottom-1))bottom--;}
                GuideHit last=destination.isEmpty()?null:destination.getLast();
                if(last!=null&&last.target().equals(target.get())&&last.y()==top&&last.height()==bottom-top&&last.x()+last.width()>=left)destination.set(destination.size()-1,new GuideHit(last.x(),top,right-last.x(),bottom-top,last.target()));
                else if(bottom>top)destination.add(new GuideHit(left,top,right-left,bottom-top,target.get()));
            }
            cursor[0]+=advance;return true;
        });
    }
    public static void region(int x,int y,int width,int height,GuideTarget target){if(target!=null&&!GuideOverlay.isOpen())regions.add(new GuideHit(x,y,width,height,target));}
    public static void drawLinked(DrawContext context,TextRenderer font,OrderedText text,int x,int y,int color){
        context.drawText(font,text,x,y,color,false);if(GuideOverlay.isOpen())return;
        var hits=new ArrayList<GuideHit>();collect(text,font,x,y,hits,context);regions.addAll(hits);
    }
    public static void endFrame(DrawContext context,float delta){
        if(GuideOverlay.isOpen()){GuideOverlay.render(context,mx,my,delta);return;}
        if(!TooltipDetails.isHeld())tooltip=null;
        if(tooltip!=null&&!captured&&(tooltip.pinned||Util.getMeasuringTimeMs()-tooltip.touched<=220)){
            var saved=RarityTooltipContext.get();replaying=true;RarityTooltipContext.set(tooltip.rarity);
            try{((GuideTooltipInvoker)context).cranks$drawGuideTooltip(tooltip.font,tooltip.components,tooltip.anchorX,tooltip.anchorY,
                    (sw,sh,ax,ay,w,h)->new Vector2i(tooltip.x,tooltip.y));}
            finally{replaying=false;RarityTooltipContext.set(saved);}
        }
        var hits=tooltip!=null?tooltip.hits:List.<GuideHit>of();
        context.getMatrices().push();context.getMatrices().translate(0,0,900);
        for(var hit:hits)if(hit.contains(mx,my))context.fill(hit.x(),hit.y()+hit.height()-1,hit.x()+hit.width(),hit.y()+hit.height(),0xFFD0D0D4);
        for(var hit:regions)if(hit.contains(mx,my))context.fill(hit.x(),hit.y()+hit.height()-1,hit.x()+hit.width(),hit.y()+hit.height(),0xFFD0D0D4);
        context.getMatrices().pop();
    }
    public static boolean click(Screen screen,double x,double y,int button){
        if(screen!=owner)return false;
        if(GuideOverlay.isOpen()){GuideOverlay.click(x,y,button);consumedButtons|=1<<Math.clamp(button,0,15);return true;}
        if(TooltipDetails.isHeld()&&tooltip!=null&&tooltip.pinned&&tooltip.inside(x,y)){
            if(button==0)for(var hit:tooltip.hits)if(hit.contains(x,y)){GuideOverlay.open(hit.target());tooltip=null;break;}
            consumedButtons|=1<<Math.clamp(button,0,15);return true;
        }
        if(button==0)for(var hit:regions)if(hit.contains(x,y)){GuideOverlay.open(hit.target());consumedButtons|=1;return true;}
        return false;
    }
    public static Screen owner(){return owner;}
}

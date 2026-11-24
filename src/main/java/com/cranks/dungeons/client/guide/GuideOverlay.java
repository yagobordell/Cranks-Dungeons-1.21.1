package com.cranks.dungeons.client.guide;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** A modal layer on the existing Screen: opening a guide never changes its server container. */
public final class GuideOverlay {
    private static final class Route {
        GuideTarget target;int scroll;String query="";
        Route(GuideTarget target){this.target=target;}
    }
    private static GuidePageLayout pageLayout;
    private static Screen owner;private static Element previousFocus;
    private static TextFieldWidget search;
    private static final List<Route> history=new ArrayList<>();private static int historyIndex;
    private static List<GuideBook.Entry> directory=List.of(),results=List.of();
    private static final List<GuideHit> links=new ArrayList<>();
    private static GuideDocument document;
    private static int x,y,width,height,bodyTop,bodyBottom,maxScroll,layoutWidth=-1,layoutHeight=-1,selectedLink=-1;
    private static boolean layoutDirty=true,settingSearch;
    private static int scrollbarGrab=-1;
    private GuideOverlay(){}
    public static boolean isOpen(){return owner!=null&&MinecraftClient.getInstance().currentScreen==owner;}
    public static void open(GuideTarget target){
        var client=MinecraftClient.getInstance();if(client.currentScreen==null)return;
        if(!isOpen()&&target!=null&&client.currentScreen instanceof com.cranks.dungeons.client.compendium.CompendiumScreen screen&&screen.openWikiReference(target))return;
        target=GuideTopics.resolve(target);
        if(!isOpen()){
            owner=client.currentScreen;previousFocus=owner.getFocused();history.clear();historyIndex=-1;
            directory=GuideBook.index().stream().sorted(Comparator.comparing(GuideBook.Entry::category).thenComparing(GuideBook.Entry::title,String.CASE_INSENSITIVE_ORDER)).toList();
            search=new TextFieldWidget(client.textRenderer,0,0,100,14,Text.literal("Search the Compendium"));
            search.setMaxLength(120);search.setDrawsBackground(false);search.setPlaceholder(Text.literal("Search the Compendium..."));
            search.setChangedListener(value->{if(!settingSearch){route().query=value;route().scroll=0;updateResults();layoutDirty=true;}});
            owner.setFocused(search);search.setFocused(false);
        }
        while(history.size()>historyIndex+1)history.removeLast();history.add(new Route(target));historyIndex++;
        activate();
    }
    public static void close(){
        Screen saved=owner;owner=null;document=null;links.clear();scrollbarGrab=-1;
        if(saved!=null&&MinecraftClient.getInstance().currentScreen==saved)saved.setFocused(previousFocus);
        previousFocus=null;search=null;history.clear();
    }
    private static Route route(){return history.get(historyIndex);}
    private static void activate(){
        document=route().target==null?null:GuideBook.load(route().target);settingSearch=true;search.setText(route().query);settingSearch=false;
        layoutDirty=true;layoutWidth=-1;selectedLink=-1;scrollbarGrab=-1;updateResults();
        search.setFocused(false);
    }
    private static void updateResults(){
        String query=route().query.toLowerCase(Locale.ROOT).replace('_',' ').strip();String[] terms=query.split("\\s+");
        results=directory.stream().filter(entry->{String text=(entry.title()+" "+entry.category()+" "+entry.target().key()).toLowerCase(Locale.ROOT).replace('_',' ');
            if(!query.isBlank())text+=" "+GuideSearch.text(entry.target());
            for(String term:terms)if(!term.isEmpty()&&!text.contains(term))return false;return true;}).sorted(Comparator.comparingInt((GuideBook.Entry entry)->GuideSearch.rank(entry,query)).thenComparing(GuideBook.Entry::title,String.CASE_INSENSITIVE_ORDER)).toList();
    }
    private static boolean searching(){return document==null||!route().query.isBlank();}
    private static void bounds(DrawContext context){
        width=Math.max(100,Math.min(610,context.getScaledWindowWidth()-24));height=Math.max(90,context.getScaledWindowHeight()-24);
        x=(context.getScaledWindowWidth()-width)/2;y=(context.getScaledWindowHeight()-height)/2;
        bodyTop=y+68;bodyBottom=y+height-12;
        search.setPosition(x+12,y+40);search.setWidth(width-30);
    }
    public static void render(DrawContext context,int mouseX,int mouseY,float delta){
        if(!isOpen())return;bounds(context);drag(mouseY);var font=MinecraftClient.getInstance().textRenderer;
        context.getMatrices().push();context.getMatrices().translate(0,0,1100);
        context.fill(0,0,context.getScaledWindowWidth(),context.getScaledWindowHeight(),0xB5000000);
        CompendiumStyle.drawPanel(context,x,y,width,height);
        button(context,"<",x+10,y+9,20,historyIndex>0,mouseX,mouseY);
        button(context,">",x+34,y+9,20,historyIndex+1<history.size(),mouseX,mouseY);
        button(context,"Index",x+58,y+9,42,true,mouseX,mouseY);
        button(context,"X",x+width-30,y+9,20,true,mouseX,mouseY);
        String title=document==null?"Compendium":document.title();
        context.drawTextWithShadow(font,font.trimToWidth(title,Math.max(20,width-148)),x+108,y+15,CompendiumStyle.TEXT);
        CompendiumStyle.drawTextFieldFrame(context,x+10,y+36,width-24,20,search.isFocused());search.render(context,mouseX,mouseY,delta);
        links.clear();context.enableScissor(x+9,bodyTop,x+width-13,bodyBottom);
        try {
            if(searching()){
                maxScroll=Math.max(0,results.size()*25-(bodyBottom-bodyTop));route().scroll=Math.clamp(route().scroll,0,maxScroll);
                for(int i=0;i<results.size();i++){
                    int ry=bodyTop+i*25-route().scroll;if(ry+24<bodyTop||ry>bodyBottom)continue;var entry=results.get(i);
                    if(mouseX>=x+12&&mouseX<x+width-18&&mouseY>=ry&&mouseY<ry+24)context.fill(x+11,ry,x+width-17,ry+24,0xFF2D2D31);
                    context.drawTextWithShadow(font,font.trimToWidth(entry.title(),width-40),x+16,ry+2,CompendiumStyle.TEXT);
                    context.drawText(font,entry.category(),x+16,ry+13,CompendiumStyle.TEXT_MUTED,false);
                    links.add(new GuideHit(x+11,Math.max(ry,bodyTop),width-28,Math.max(0,Math.min(ry+24,bodyBottom)-Math.max(ry,bodyTop)),entry.target()));
                }
                if(results.isEmpty())context.drawTextWithShadow(font,"No matching references",x+16,bodyTop+5,CompendiumStyle.TEXT_MUTED);
            }else{
                int textWidth=Math.min(430,width-40),textX=x+(width-textWidth)/2;
                if(layoutDirty||layoutWidth!=textWidth||layoutHeight!=bodyBottom-bodyTop)layout(textWidth);
                route().scroll=Math.clamp(route().scroll,0,maxScroll);
                pageLayout.draw(context,textX,bodyTop-route().scroll,bodyTop,bodyBottom,links);
            }
        }finally{context.disableScissor();}
        for(int i=0;i<links.size();i++){var hit=links.get(i);if(hit.height()>0&&(hit.contains(mouseX,mouseY)||i==selectedLink))context.fill(hit.x(),Math.min(bodyBottom-1,hit.y()+hit.height()-1),hit.x()+hit.width(),Math.min(bodyBottom,hit.y()+hit.height()),0xFFB9BEC9);}
        if(maxScroll>0){int view=bodyBottom-bodyTop,thumb=Math.max(12,view*view/(view+maxScroll));int top=bodyTop+(int)((long)route().scroll*(view-thumb)/maxScroll);
            CompendiumStyle.drawGuideScrollbar(context,x+width-11,bodyTop,view,top,thumb);}
        context.getMatrices().pop();
    }
    private static void layout(int textWidth){
        pageLayout=GuidePageLayout.build(document,route().target,textWidth);maxScroll=Math.max(0,pageLayout.height-(bodyBottom-bodyTop));
        String anchor=route().target.key().contains("#")?route().target.key().split("#",2)[1]:"";
        if(layoutWidth<0&&!anchor.isBlank()&&route().scroll==0)route().scroll=pageLayout.anchorY(anchor);
        layoutWidth=textWidth;layoutHeight=bodyBottom-bodyTop;layoutDirty=false;
    }
    private static void button(DrawContext context,String label,int bx,int by,int bw,boolean enabled,int mx,int my){
        var font=MinecraftClient.getInstance().textRenderer;boolean hover=mx>=bx&&mx<bx+bw&&my>=by&&my<by+18;
        CompendiumStyle.drawRailPlaque(context,bx,by,bw,18,false,hover&&enabled);
        context.drawCenteredTextWithShadow(font,label,bx+bw/2,by+5,enabled?CompendiumStyle.TEXT:CompendiumStyle.TEXT_DISABLED);
    }
    public static void click(double mx,double my,int button){
        if(!isOpen()||button!=0)return;
        if(maxScroll>0&&mx>=x+width-15&&mx<x+width-5&&my>=bodyTop&&my<bodyBottom){
            int view=bodyBottom-bodyTop,thumb=Math.max(12,view*view/(view+maxScroll));
            int top=bodyTop+(int)((long)route().scroll*(view-thumb)/maxScroll);
            scrollbarGrab=my>=top&&my<top+thumb?(int)my-top:thumb/2;drag(my);return;
        }
        if(my>=y+9&&my<y+27){if(mx>=x+10&&mx<x+30){back();return;}if(mx>=x+34&&mx<x+54){forward();return;}
            if(mx>=x+58&&mx<x+100){open(null);return;}if(mx>=x+width-30&&mx<x+width-10){close();return;}}
        if(my>=y+36&&my<y+56){search.setFocused(true);search.mouseClicked(mx,my,button);return;}
        if(my>=bodyTop&&my<bodyBottom)for(var hit:links)if(hit.contains(mx,my)){open(hit.target());return;}
        search.setFocused(false);
    }
    public static void drag(double mouseY){
        if(!isOpen()||scrollbarGrab<0||maxScroll<=0)return;
        int view=bodyBottom-bodyTop,thumb=Math.max(12,view*view/(view+maxScroll));
        if(view>thumb)route().scroll=(int)Math.clamp(Math.round((mouseY-bodyTop-scrollbarGrab)*maxScroll/(view-thumb)),0,maxScroll);
        selectedLink=-1;
    }
    public static void release(int button){if(button==0)scrollbarGrab=-1;}
    public static void scroll(double amount){if(isOpen()){route().scroll=Math.clamp(route().scroll-(int)Math.signum(amount)*30,0,maxScroll);selectedLink=-1;}}
    public static void keyPressed(int key,int scan,int modifiers){
        if(!isOpen())return;
        if(key==GLFW.GLFW_KEY_ESCAPE){close();return;}
        if((modifiers&GLFW.GLFW_MOD_ALT)!=0&&key==GLFW.GLFW_KEY_LEFT){back();return;}
        if((modifiers&GLFW.GLFW_MOD_ALT)!=0&&key==GLFW.GLFW_KEY_RIGHT){forward();return;}
        if((modifiers&GLFW.GLFW_MOD_CONTROL)!=0&&key==GLFW.GLFW_KEY_F){search.setFocused(true);return;}
        if(key==GLFW.GLFW_KEY_TAB){search.setFocused(false);selectedLink=links.isEmpty()?-1:Math.floorMod(selectedLink+((modifiers&GLFW.GLFW_MOD_SHIFT)!=0?-1:1),links.size());return;}
        if(key==GLFW.GLFW_KEY_ENTER&&selectedLink>=0&&selectedLink<links.size()){open(links.get(selectedLink).target());return;}
        if(search.isFocused()){search.keyPressed(key,scan,modifiers);return;}
        if(key==GLFW.GLFW_KEY_PAGE_DOWN)scroll(-5);else if(key==GLFW.GLFW_KEY_PAGE_UP)scroll(5);
        else if(key==GLFW.GLFW_KEY_HOME)route().scroll=0;else if(key==GLFW.GLFW_KEY_END)route().scroll=maxScroll;
    }
    public static void charTyped(int codepoint,int modifiers){if(isOpen()&&search.isFocused())for(char c:Character.toChars(codepoint))search.charTyped(c,modifiers);}
    public static void back(){if(isOpen()&&historyIndex>0){historyIndex--;activate();}}
    public static void forward(){if(isOpen()&&historyIndex+1<history.size()){historyIndex++;activate();}}
    public static GuideTarget currentTarget(){return isOpen()?route().target:null;}
}

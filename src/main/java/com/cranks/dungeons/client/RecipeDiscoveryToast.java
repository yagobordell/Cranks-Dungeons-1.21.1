package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import java.util.*;

/** One compact, mergeable toast for bursts of discoveries across the station system. */
public final class RecipeDiscoveryToast implements Toast {
    private static final int MAX_PREVIEWS=3;
    private final int generation=RecipeDiscoveryNotifications.generation();
    private final Map<String,RecipeDiscoveryNotifications.Notice> entries=new LinkedHashMap<>();
    private List<RecipeDiscoveryNotifications.Notice> visible=List.of();
    private long changedAt;
    private boolean changed=true,hidden;
    private RecipeDiscoveryToast(Collection<RecipeDiscoveryNotifications.Notice> discoveries){add(discoveries);}
    private void add(Collection<RecipeDiscoveryNotifications.Notice> discoveries){
        var newest=new ArrayList<RecipeDiscoveryNotifications.Notice>();
        for(var notice:discoveries)if(entries.putIfAbsent(notice.key(),notice)==null)newest.add(notice);
        if(newest.isEmpty())return;
        Collections.reverse(newest);
        newest.addAll(visible);
        visible=newest.stream().limit(MAX_PREVIEWS).toList();changed=true;hidden=false;
    }
    public static void show(ToastManager manager,Collection<RecipeDiscoveryNotifications.Notice> discoveries){
        if(discoveries.isEmpty())return;
        var existing=manager.getToast(RecipeDiscoveryToast.class,Toast.TYPE);
        if(existing==null||existing.hidden||existing.generation!=RecipeDiscoveryNotifications.generation())manager.add(new RecipeDiscoveryToast(discoveries));else existing.add(discoveries);
    }
    @Override public int getWidth(){return 244;}
    @Override public int getHeight(){return 64;}
    @Override public Visibility draw(DrawContext context,ToastManager manager,long time){
        if(generation!=RecipeDiscoveryNotifications.generation())return Visibility.HIDE;
        if(changed){changedAt=time;changed=false;}
        if(visible.isEmpty())return Visibility.HIDE;
        long age=time-changedAt;
        int index=(int)Math.min(age/1600,visible.size()-1);
        var notice=visible.get(index);
        var font=manager.getClient().textRenderer;
        CompendiumStyle.drawPanel(context,4,4,getWidth()-8,54);
        Text heading=Text.translatable(entries.size()==1?"notification.cranks-dungeons.recipe_discovered":"notification.cranks-dungeons.recipes_discovered",entries.size());
        line(context,font,heading,12,11,getWidth()-24,CompendiumStyle.TEXT);
        context.drawItem(notice.icon(),12,29);
        line(context,font,notice.name(),35,27,getWidth()-49,CompendiumStyle.TEXT);
        int more=entries.size()-visible.size();
        String page=more>0?Text.translatable("notification.cranks-dungeons.recipes_more",more).getString()
                :visible.size()>1?(index+1)+" / "+visible.size():"";
        int pageWidth=font.getWidth(page);
        line(context,font,notice.station(),35,41,getWidth()-51-(page.isEmpty()?0:pageWidth+9),CompendiumStyle.TEXT_MUTED);
        context.drawText(font,page,getWidth()-14-pageWidth,41,CompendiumStyle.TEXT_MUTED,false);
        hidden=age>=7000*manager.getNotificationDisplayTimeMultiplier();
        return hidden?Visibility.HIDE:Visibility.SHOW;
    }
    private static void line(DrawContext context,TextRenderer font,Text text,int x,int y,int width,int color){
        String value=text.getString();if(font.getWidth(value)>width)value=font.trimToWidth(value,Math.max(0,width-6))+"…";
        context.drawText(font,value,x,y,color,false);
    }
}

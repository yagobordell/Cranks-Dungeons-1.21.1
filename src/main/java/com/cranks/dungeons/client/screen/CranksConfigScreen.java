package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.StatsScrollbar;
import com.cranks.dungeons.client.StyledTextFieldWidget;
import com.cranks.dungeons.client.compendium.*;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import java.util.*;

/** Uses the existing typed owo options, constraints and persistence behind the shared UI. */
public class CranksConfigScreen extends Screen {
    private record Setting(Option<Object> option,String category,String name) {}
    private final Screen parent;
    private final List<Setting> settings=new ArrayList<>();
    private final Map<String,String> categories=new LinkedHashMap<>();
    private final List<ClickableWidget> rowWidgets=new ArrayList<>();
    private StyledTextFieldWidget search;
    private CompendiumButtonWidget resetCategoryButton;
    private final Map<Option<Object>,CompendiumButtonWidget> resets=new HashMap<>();
    private String category="hud";
    private int x,y,w,h,sidebar,top,bottom,scroll;
    private boolean compact;
    private boolean dragging;
    private double grab;
    private List<Setting> matches=List.of();
    public CranksConfigScreen(Screen parent){super(Text.literal("Mod Settings"));this.parent=parent;collect();}
    @SuppressWarnings("unchecked") private void collect(){
        String section="general";
        for(var field:com.cranks.dungeons.config.ModConfigModel.class.getDeclaredFields()){
            var header=field.getAnnotation(SectionHeader.class);if(header!=null)section=header.value();
            if(field.isAnnotationPresent(ExcludeFromScreen.class))continue;
            var option=CranksDungeons.CONFIG.allOptions().values().stream().filter(o->o.backingField().field().getName().equals(field.getName())).findFirst().orElse(null);
            if(option==null)continue;
            categories.putIfAbsent(section,sectionName(section));
            String translated=Text.translatable(option.translationKey()).getString();
            settings.add(new Setting((Option<Object>)option,section,translated.equals(option.translationKey())?readable(field.getName()):translated));
        }
    }
    private static String readable(String value){
        value=value.replaceAll("([a-z])([A-Z])","$1 $2").replace('_',' ').toLowerCase(Locale.ROOT);
        return value.isEmpty()?value:Character.toUpperCase(value.charAt(0))+value.substring(1);
    }
    private static String sectionName(String key){return switch(key){
        case "nameplate_self"->"Your nameplate";case "nameplate_other_players"->"Other players";
        case "nameplate_mobs"->"Creatures";case "party_hud"->"Party";case "status_effects"->"Effects";
        case "development"->"Tree designer";case "hud"->"HUD";default->readable(key);
    };}
    @Override protected void init(){
        String query=search==null?"":search.getText();
        clearChildren();rowWidgets.clear();
        w=Math.min(620,width-24);h=Math.min(430,height-20);x=(width-w)/2;y=(height-h)/2;
        compact=w<430||h<310;sidebar=compact?0:125;top=y+87;bottom=y+h-37;
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Settings"),b->{}).selected(true).dimensions(x+12,y+29,90,20).build());
        var layout=addDrawableChild(CompendiumButtonWidget.builder(Text.literal("HUD Layout"),b->{CranksDungeons.CONFIG.save();client.setScreen(new HudLayoutEditScreen(parent));}).dimensions(x+108,y+29,100,20).build());
        layout.active=client!=null&&client.player!=null;
        layout.setTooltip(Tooltip.of(Text.literal(layout.active?"Arrange HUD elements in the world.":"Join a world to arrange the HUD.")));
        search=addDrawableChild(new StyledTextFieldWidget(textRenderer,compact?x+138:x+sidebar+16,y+59,compact?w-150:w-sidebar-30,20,Text.literal("Search settings")));
        search.setPlaceholder(Text.literal("Find setting..."));search.setText(query);search.setChangedListener(text->{scroll=0;rebuildRows();});
        int cy=y+59;
        if(compact){
            var chooser=addDrawableChild(CompendiumButtonWidget.builder(Text.literal(categories.get(category)+" >"),b->{
                var keys=new ArrayList<>(categories.keySet());category=keys.get((keys.indexOf(category)+1)%keys.size());scroll=0;search.setText("");init();
            }).dimensions(x+12,cy,120,20).build());
            chooser.setTooltip(Tooltip.of(Text.literal("Change category. Search looks through every category.")));
        }else for(var entry:categories.entrySet()){
            String key=entry.getKey();
            addDrawableChild(CompendiumButtonWidget.builder(Text.literal(entry.getValue()),b->{category=key;scroll=0;search.setText("");init();}).selected(category.equals(key)).dimensions(x+10,cy,sidebar-8,20).build());cy+=23;
        }
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x+w-92,y+h-28,80,18).build());
        resetCategoryButton=addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Reset category"),b->{
            for(var setting:settings)if(search.getText().isBlank()?setting.category.equals(category):matches.contains(setting))setting.option.set(setting.option.defaultValue());rebuildRows();
        }).dimensions(x+sidebar+16,y+h-28,110,18).build());
        rebuildRows();
    }
    private StatsScrollbar scrollbar(){return new StatsScrollbar(top,bottom-top,matches.size()*38);}
    private void rebuildRows(){
        if(rowWidgets.contains(getFocused()))setFocused(null);
        for(var widget:rowWidgets)remove(widget);rowWidgets.clear();resets.clear();
        String query=search.getText().strip().toLowerCase(Locale.ROOT);
        if(resetCategoryButton!=null)resetCategoryButton.setMessage(Text.literal(query.isEmpty()?"Reset category":"Reset results"));
        matches=settings.stream().filter(s->query.isEmpty()?s.category.equals(category):(s.name+" "+s.category+" "+s.option.key().asString()).toLowerCase(Locale.ROOT).contains(query)).toList();
        scroll=Math.clamp(scroll,0,scrollbar().maxScroll());
        int right=x+w-21;
        for(int i=0;i<matches.size();i++){
            var setting=matches.get(i);var option=setting.option;int ry=top+i*38-scroll;
            if(ry<top||ry+34>bottom)continue;
            Object value=option.value();
            if(value instanceof Number){
                var field=new StyledTextFieldWidget(textRenderer,right-112,ry+7,68,20,Text.literal(setting.name));
                field.setText(value.toString());
                field.setChangedListener(text->{try{
                    double number=Double.parseDouble(text);var range=option.backingField().field().getAnnotation(RangeConstraint.class);
                    if(!Double.isFinite(number)||range!=null&&(number<range.min()||number>range.max()))throw new IllegalArgumentException();
                    Object parsed;
                    if(value instanceof Integer)parsed=Integer.parseInt(text);
                    else if(value instanceof Float)parsed=Float.parseFloat(text);
                    else parsed=Double.parseDouble(text);
                    option.set(parsed);if(resets.containsKey(option))resets.get(option).active=!Objects.equals(option.value(),option.defaultValue());field.setEditableColor(CompendiumStyle.TEXT);
                }catch(IllegalArgumentException invalid){field.setEditableColor(CompendiumStyle.TEXT_WARNING);}});
                var range=option.backingField().field().getAnnotation(RangeConstraint.class);
                if(range!=null)field.setTooltip(Tooltip.of(Text.literal("Range: "+range.min()+" to "+range.max())));
                row(field);
            }else{
                String label=displayValue(value);
                var control=CompendiumButtonWidget.builder(Text.literal(label),b->{
                    if(value instanceof Boolean flag)option.set(!flag);
                    else if(value instanceof Enum<?> current){Object[] values=current.getDeclaringClass().getEnumConstants();option.set(values[(current.ordinal()+1)%values.length]);}
                    rebuildRows();
                }).dimensions(right-112,ry+7,68,20).build();
                // Compact enum values stay readable; full values are also available on hover.
                control.setMessage(Text.literal(textRenderer.trimToWidth(label,58)));
                control.setTooltip(Tooltip.of(Text.literal(readable(value.toString()))));row(control);
            }
            var reset=CompendiumButtonWidget.builder(Text.literal("Reset"),b->{option.set(option.defaultValue());rebuildRows();}).dimensions(right-38,ry+7,38,20).build();
            resets.put(option,reset);reset.active=!Objects.equals(option.value(),option.defaultValue());reset.setTooltip(Tooltip.of(Text.literal("Default: "+option.defaultValue())));row(reset);
        }
    }
    private static String displayValue(Object value){
        if(value instanceof Boolean flag)return flag?"On":"Off";
        return switch(value.toString()){
            case "TOP_TO_BOTTOM"->"Down";case "BOTTOM_TO_TOP"->"Up";
            case "LEFT_TO_RIGHT"->"Right";case "RIGHT_TO_LEFT"->"Left";
            case "WHEN_DAMAGED"->"Damaged";default->readable(value.toString());
        };
    }
    private void row(ClickableWidget widget){rowWidgets.add(widget);addDrawableChild(widget);}
    @Override public void renderBackground(DrawContext context,int mouseX,int mouseY,float delta){}
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        context.fill(0,0,width,height,0xA0000000);CompendiumStyle.drawPanel(context,x,y,w,h);
        context.drawText(textRenderer,"Cranks Dungeons",x+14,y+12,CompendiumStyle.TEXT,false);
        CompendiumStyle.drawInsetPanel(context,x+sidebar+9,top-3,w-sidebar-20,bottom-top+7);
        context.enableScissor(x+sidebar+11,top,x+w-16,bottom);
        for(int i=0;i<matches.size();i++){
            int ry=top+i*38-scroll;if(ry<top||ry+34>bottom)continue;
            var setting=matches.get(i);int labelWidth=w-sidebar-151;
            var lines=textRenderer.wrapLines(Text.literal(setting.name),Math.max(35,labelWidth));
            for(int line=0;line<Math.min(2,lines.size());line++)context.drawText(textRenderer,lines.get(line),x+sidebar+17,ry+(lines.size()>1?7:12)+line*10,CompendiumStyle.TEXT_MUTED,false);
        }
        if(matches.isEmpty())context.drawText(textRenderer,"No matching settings",x+sidebar+17,top+10,CompendiumStyle.TEXT_MUTED,false);
        context.disableScissor();
        if(scrollbar().maxScroll()>0)CompendiumStyle.drawGuideScrollbar(context,x+w-10,top,bottom-top,(int)Math.round(scrollbar().thumbTop(scroll)),scrollbar().thumbHeight());
        super.render(context,mouseX,mouseY,delta);
        if(mouseX>=x+sidebar+16&&mouseX<x+w-138&&mouseY>=top&&mouseY<bottom){
            int index=(mouseY-top+scroll)/38;
            if(index>=0&&index<matches.size()&&top+index*38-scroll>=top&&top+index*38-scroll+34<=bottom){
                var setting=matches.get(index);var tooltip=new ArrayList<Text>();tooltip.add(Text.literal(setting.name));
                String key=setting.option.translationKey()+".tooltip";
                if(net.minecraft.client.resource.language.I18n.hasTranslation(key))tooltip.add(Text.translatable(key));
                context.drawTooltip(textRenderer,tooltip,mouseX,mouseY);
            }
        }
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(mx>=x+sidebar&&mx<x+w&&my>=top&&my<bottom){scroll=Math.clamp(scroll-(int)(vertical*38),0,scrollbar().maxScroll());rebuildRows();return true;}return super.mouseScrolled(mx,my,horizontal,vertical);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button==0&&mx>=x+w-13&&mx<x+w-5&&my>=top&&my<bottom&&scrollbar().maxScroll()>0){dragging=true;grab=scrollbar().grabOffset(my,scroll);scroll=scrollbar().scrollAt(my,grab);rebuildRows();return true;}boolean handled=super.mouseClicked(mx,my,button);if(getFocused()!=null&&!children().contains(getFocused()))setFocused(null);return handled;
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){if(dragging&&button==0){scroll=scrollbar().scrollAt(my,grab);rebuildRows();return true;}return super.mouseDragged(mx,my,button,dx,dy);}
    @Override public boolean mouseReleased(double mx,double my,int button){if(button==0)dragging=false;return super.mouseReleased(mx,my,button);}
    @Override public void close(){CranksDungeons.CONFIG.save();client.setScreen(parent);}
}

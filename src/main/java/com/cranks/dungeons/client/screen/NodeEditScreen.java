package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.client.*;
import com.cranks.dungeons.client.compendium.*;
import com.cranks.dungeons.stat.passive.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.function.Consumer;

/** Shared authoring form; tree storage, choices and graph connections retain their existing semantics. */
public abstract class NodeEditScreen extends Screen {
    private final Screen parent;
    private final PassiveNode original;
    private final boolean worldTree;
    private final double gridX,gridY;
    private final Runnable refresh;
    private final Consumer<PassiveNode> connect;
    private final Map<String,PassiveNode> nodes;
    private final List<ModifierRow> rows=new ArrayList<>();
    private final List<ClickableWidget> bodyWidgets=new ArrayList<>();
    private final List<Label> labels=new ArrayList<>();
    private record Label(String text,int x,int y){}
    private class ModifierRow {
        final StyledTextFieldWidget attribute,value;
        ModifierRow(String attr,String amount){attribute=field(attr,"Attribute",128);value=field(amount,"Value",32);}
    }
    private StyledTextFieldWidget idField,nameField,iconField,descField,amountField;
    private boolean choice;
    private boolean automaticDescription;
    private String descriptionSignature="",manualDescription="";
    private String shape="square",error="";
    private int x,y,w,h,top,bottom,scroll,contentHeight,columnWidth;
    private boolean twoColumns,dragging;
    private double grab;
    private List<String> attributes=List.of(),icons=List.of(),suggestions=List.of();
    private TextFieldWidget suggestionField;
    private int suggestionIndex,suggestionX,suggestionY,suggestionW;
    private static final String[] SHAPES={"square","circle","diamond","hexagon"};

    protected NodeEditScreen(Screen parent,PassiveNode node,double gx,double gy,boolean worldTree,Runnable refresh,Consumer<PassiveNode> connect){
        super(Text.literal(node==null?"Create Node":"Edit Node"));this.parent=parent;this.original=node;
        this.gridX=node==null?gx:node.gridX();this.gridY=node==null?gy:node.gridY();this.worldTree=worldTree;this.refresh=refresh;this.connect=connect;
        nodes=worldTree?WorldNodeRegistry.getNodes():PassiveNodeRegistry.getNodes();
    }
    private StyledTextFieldWidget field(String text,String title,int limit){
        var result=new StyledTextFieldWidget(textRenderer,0,0,100,20,Text.literal(title));result.setMaxLength(limit);result.setText(text==null?"":text);result.setCursorToStart(false);return result;
    }
    private Set<String> whitelist(){return worldTree?PassiveStatHelper.WHITELISTED_WORLD_ATTRIBUTES:PassiveStatHelper.WHITELISTED_PASSIVE_ATTRIBUTES;}
    private String nextNodeId() {
        long next = 1;
        for (String id : nodes.keySet()) {
            if (!id.startsWith("node_")) continue;
            try {
                long number = Long.parseLong(id.substring(5));
                if (number == Long.MAX_VALUE) return "node_" + UUID.randomUUID();
                next = Math.max(next, number + 1);
            } catch (NumberFormatException ignored) {
                // Named nodes do not participate in the automatic numeric sequence.
            }
        }
        return "node_" + next;
    }
    @Override protected void init(){
        if(idField==null){
            idField=field(original==null?nextNodeId():original.id(),"Node ID",64);idField.setEditable(false);
            nameField=field(original==null?"":original.name(),"Title",128);
            String initialIcon="";
            if(original!=null&&original.icon()!=null&&!original.icon().equals(PassiveIconManager.PLACEHOLDER_ICON)
                    &&!original.icon().equals(PassiveIconManager.getIcon(original.id(),original.name(),original.isAttributeSelection())))initialIcon=original.icon().toString();
            iconField=field(initialIcon,"Icon",128);
            descField=field(original==null||original.description()==null?"":original.description().replace("\n","\\n"),"Description",2048);
            manualDescription=original!=null&&!com.cranks.dungeons.client.guide.TreeNodeDescriptions.automatic(original)?descField.getText():"";
            amountField=field(original==null?"5":Double.toString(original.attributeSelectionAmount()),"Choice amount",32);
            choice=original!=null&&original.isAttributeSelection();shape=original==null?"square":original.shape();
            if(original!=null)for(var modifier:original.modifiers().entrySet())rows.add(new ModifierRow(Registries.ATTRIBUTE.getId(modifier.getKey().value()).toString(),modifier.getValue().toString()));
            if(rows.isEmpty())rows.add(new ModifierRow("",""));
            attributes=Registries.ATTRIBUTE.getIds().stream().filter(id->id.getNamespace().equals("cranks-dungeons")&&(worldTree?PassiveStatHelper.WORLD_EDITOR_ATTRIBUTES:whitelist()).contains(id.getPath())).map(Identifier::getPath).sorted().toList();
            icons=PassiveIconManager.getAllAvailableIcons(client.getResourceManager());
        }
        clearChildren();bodyWidgets.clear();labels.clear();
        w=Math.min(720,width-24);h=Math.min(460,height-20);x=(width-w)/2;y=(height-h)/2;
        top=y+49;bottom=y+h-42;twoColumns=w>=450;columnWidth=twoColumns?(w-40)/2:w-32;
        int footer=y+h-29;int bw=Math.min(94,(w-36)/4);
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal(original==null?"Create":"Apply"),b->{if(save())close();}).dimensions(x+12,footer,bw,20).build());
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Cancel"),b->close()).dimensions(x+18+bw,footer,bw,20).build());
        if(original!=null){
            addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Delete"),b->{nodes.remove(original.id());for(var other:nodes.values())other.connections().remove(original.id());if(refresh!=null)refresh.run();close();}).dimensions(x+w-18-bw*2,footer,bw,20).build());
            addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Link"),b->{if(save()){if(connect!=null)connect.accept(nodes.get(idField.getText()));close();}}).dimensions(x+w-12-bw,footer,bw,20).build());
        }
        layoutBody();
    }
    private void place(StyledTextFieldWidget field,int px,int offset,int width,String label){
        int py=top+offset-scroll;field.setWidth(Math.max(1,width-10));field.moveFrame(px,py+13);
        field.setVisible(py>=top&&py+34<=bottom);bodyWidgets.add(field);addDrawableChild(field);labels.add(new Label(label,px,py));
    }
    private void button(String text,int px,int offset,int width,CompendiumButtonWidget.PressAction action){
        int py=top+offset-scroll;var button=CompendiumButtonWidget.builder(Text.literal(text),action).dimensions(px,py,width,20).build();
        button.visible=py>=top&&py+20<=bottom;bodyWidgets.add(button);addDrawableChild(button);
    }
    private void layoutBody(){
        for(var widget:bodyWidgets)remove(widget);bodyWidgets.clear();labels.clear();
        int left=x+15,right=twoColumns?left+columnWidth+12:left,mods=twoColumns?0:270;
        place(idField,left,0,columnWidth,"Node ID");place(nameField,left,39,columnWidth,"Title");
        place(iconField,left,78,columnWidth,"Icon identifier (optional)");place(descField,left,117,columnWidth,"Description (automatic with bonuses)");
        button("Shape: "+shape,left,162,columnWidth,b->{int index=Arrays.asList(SHAPES).indexOf(shape);shape=SHAPES[(index+1)%SHAPES.length];layoutBody();});
        button((worldTree?"Modifier choice: ":"Attribute choice: ")+(choice?"On":"Off"),left,188,columnWidth,b->{choice=!choice;layoutBody();});
        if(choice&&!worldTree){
            place(amountField,right,mods,columnWidth,"Primary attribute amount");
            labels.add(new Label("Players choose one primary attribute.",right,top+mods+48-scroll));
            contentHeight=Math.max(216,mods+90);
        }else{
            labels.add(new Label(choice?"Selectable modifiers":"Modifiers",right,top+mods-scroll));
            int attrWidth=columnWidth-82;
            for(int i=0;i<rows.size();i++){
                var row=rows.get(i);int offset=mods+17+i*29;int py=top+offset-scroll;
                row.attribute.setWidth(attrWidth-10);row.attribute.moveFrame(right,py);row.value.setWidth(44);row.value.moveFrame(right+attrWidth+5,py);
                boolean visible=py>=top&&py+20<=bottom;row.attribute.setVisible(visible);row.value.setVisible(visible);
                bodyWidgets.add(row.attribute);bodyWidgets.add(row.value);addDrawableChild(row.attribute);addDrawableChild(row.value);
                button("-",right+columnWidth-20,offset,20,b->{rows.remove(row);layoutBody();});
            }
            button("+ Add modifier",right,mods+19+rows.size()*29,columnWidth,b->{rows.add(new ModifierRow("",""));layoutBody();});
            contentHeight=Math.max(216,mods+53+rows.size()*29);
        }
        // A compact live preview uses the same icon and node frame as the tree itself.
        contentHeight+=54;
        scroll=Math.clamp(scroll,0,scrollbar().maxScroll());
    }
    private StatsScrollbar scrollbar(){return new StatsScrollbar(top,bottom-top,contentHeight);}
    private boolean save(){
        error="";
        try{
            Map<RegistryEntry<EntityAttribute>,Double> modifiers=new LinkedHashMap<>();
            if(!choice||worldTree)for(var row:rows){
                String raw=row.attribute.getText().strip(),number=row.value.getText().strip();if(raw.isEmpty()&&number.isEmpty())continue;
                Identifier key=Identifier.tryParse(raw.contains(":")?raw:"cranks-dungeons:"+raw);
                if(key==null||!TreeAttributeRules.supported(key.getNamespace(),key.getPath(),whitelist()))throw new IllegalArgumentException("Use a supported mod stat. Vanilla attributes cannot be assigned to tree nodes.");
                double value=Double.parseDouble(number);if(!Double.isFinite(value))throw new IllegalArgumentException("Modifier values must be finite numbers.");
                var attribute=Registries.ATTRIBUTE.getOrEmpty(key).orElseThrow(()->new IllegalArgumentException("Unknown attribute: "+raw));
                var entry=Registries.ATTRIBUTE.getEntry(attribute);
                if(modifiers.put(entry,value)!=null)throw new IllegalArgumentException("A modifier is listed more than once.");
            }
            double amount=choice&&!worldTree?Double.parseDouble(amountField.getText().strip()):0;
            if(!Double.isFinite(amount))throw new IllegalArgumentException("Choice amount must be a finite number.");
            String custom=iconField.getText().strip();Identifier icon=custom.isEmpty()?PassiveIconManager.getIcon(idField.getText(),nameField.getText(),choice):Identifier.tryParse(custom.contains(":")?custom:"cranks-dungeons:"+custom);
            if(!custom.isEmpty()&&icon==null)throw new IllegalArgumentException("Enter a valid icon identifier or leave it blank.");
            var node=new PassiveNode(idField.getText(),nameField.getText().strip(),descField.getText().replace("\\n","\n"),gridX,gridY,
                    original==null?new ArrayList<>():new ArrayList<>(original.connections()),modifiers,icon,shape,choice,amount);
            if(com.cranks.dungeons.client.guide.TreeNodeDescriptions.automatic(node))node=new PassiveNode(node.id(),node.name(),
                    com.cranks.dungeons.client.guide.TreeNodeDescriptions.plain(node,worldTree),gridX,gridY,node.connections(),modifiers,icon,shape,choice,amount);
            nodes.put(node.id(),node);if(refresh!=null)refresh.run();return true;
        }catch(IllegalArgumentException invalid){error=invalid instanceof NumberFormatException?"Enter a number for each modifier and choice amount.":invalid.getMessage();return false;}
    }
    private PassiveNode descriptionPreview(){
        var modifiers=new LinkedHashMap<RegistryEntry<EntityAttribute>,Double>();
        if(!choice||worldTree)for(var row:rows){
            String raw=row.attribute.getText().strip();Identifier key=Identifier.tryParse(raw.contains(":")?raw:"cranks-dungeons:"+raw);
            if(key==null||!TreeAttributeRules.supported(key.getNamespace(),key.getPath(),whitelist()))continue;
            try{double value=Double.parseDouble(row.value.getText());if(Double.isFinite(value))Registries.ATTRIBUTE.getEntry(key).ifPresent(entry->modifiers.put(entry,value));}catch(NumberFormatException ignored){}
        }
        double amount=0;try{amount=Double.parseDouble(amountField.getText());}catch(NumberFormatException ignored){}
        if(!Double.isFinite(amount))amount=0;
        return new PassiveNode(idField.getText(),nameField.getText(),automaticDescription?"":descField.getText().replace("\\n","\n"),gridX,gridY,List.of(),modifiers,null,shape,choice,amount);
    }
    private void updateDescription(){
        String signature=choice+"|"+amountField.getText()+"|"+rows.stream().map(row->row.attribute.getText()+"="+row.value.getText()).collect(java.util.stream.Collectors.joining("|"));
        if(signature.equals(descriptionSignature))return;descriptionSignature=signature;
        boolean generated=choice||rows.stream().anyMatch(row->!row.attribute.getText().isBlank());
        if(generated&&!automaticDescription){if(original==null||!com.cranks.dungeons.client.guide.TreeNodeDescriptions.automatic(original))manualDescription=descField.getText();}
        boolean wasAutomatic=automaticDescription;automaticDescription=generated;descField.setEditable(!generated);
        if(generated){var preview=descriptionPreview();String value=com.cranks.dungeons.client.guide.TreeNodeDescriptions.plain(preview,worldTree);
            descField.setText(value.isBlank()?"Choose a supported stat and enter its value.":value.replace("\n","\\n"));descField.setCursorToStart(false);}
        else if(wasAutomatic)descField.setText(manualDescription);
    }
    @Override public void renderBackground(DrawContext context,int mx,int my,float delta){}
    @Override public void render(DrawContext context,int mx,int my,float delta){
        updateDescription();
        context.fill(0,0,width,height,0xA0000000);CompendiumStyle.drawPanel(context,x,y,w,h);
        context.drawText(textRenderer,(worldTree?"World Tree  /  ":"Skill Tree  /  ")+title.getString(),x+13,y+12,CompendiumStyle.TEXT,false);
        context.drawText(textRenderer,String.format(Locale.ROOT,"Position: %.2f, %.2f",gridX,gridY),x+13,y+27,CompendiumStyle.TEXT_MUTED,false);
        CompendiumStyle.drawInsetPanel(context,x+9,top-5,w-23,bottom-top+10);
        context.enableScissor(x+10,top,x+w-13,bottom);
        for(var label:labels)if(label.y>=top&&label.y+10<=bottom)context.drawText(textRenderer,textRenderer.trimToWidth(label.text,columnWidth),label.x,label.y,CompendiumStyle.TEXT_MUTED,false);
        int previewY=top+contentHeight-40-scroll;
        if(previewY>=top&&previewY+27<=bottom){
            int previewX=x+19;context.drawGuiTexture(CompendiumStyle.treeNodeTexture(shape,"unlocked"),previewX,previewY,24,24);
            String raw=iconField.getText().strip();Identifier icon=raw.isEmpty()?PassiveIconManager.getIcon(idField.getText(),nameField.getText(),choice):Identifier.tryParse(raw.contains(":")?raw:"cranks-dungeons:"+raw);
            if(icon!=null)context.drawGuiTexture(icon,previewX+4,previewY+4,16,16);
            context.drawText(textRenderer,"Node preview",previewX+33,previewY+8,CompendiumStyle.TEXT_MUTED,false);
        }
        context.disableScissor();
        if(!error.isEmpty())context.drawText(textRenderer,textRenderer.trimToWidth(error,w-28),x+13,y+h-42,CompendiumStyle.TEXT_WARNING,false);
        if(scrollbar().maxScroll()>0)CompendiumStyle.drawGuideScrollbar(context,x+w-10,top,bottom-top,(int)Math.round(scrollbar().thumbTop(scroll)),scrollbar().thumbHeight());
        super.render(context,mx,my,delta);renderSuggestions(context);
        if(mx>=x+15&&mx<x+15+columnWidth&&my>=top+117-scroll&&my<top+153-scroll&&my>=top&&my<bottom)
            context.drawTooltip(textRenderer,com.cranks.dungeons.client.guide.GuideNodeTooltip.of(descriptionPreview(),nameField.getText(),worldTree),mx,my);
    }
    private void renderSuggestions(DrawContext context){
        TextFieldWidget active=getFocused() instanceof TextFieldWidget field?field:null;
        boolean icon=active==iconField;
        if(active==null||!active.isVisible()||!icon&&rows.stream().noneMatch(r->r.attribute==active)){suggestions=List.of();suggestionField=null;return;}
        String query=active.getText().toLowerCase(Locale.ROOT).replace("cranks-dungeons:","");
        var next=(icon?icons:attributes).stream().filter(s->s.toLowerCase(Locale.ROOT).contains(query)).limit(60).toList();
        if(active!=suggestionField||!next.equals(suggestions))suggestionIndex=0;
        suggestions=next;suggestionField=active;if(next.isEmpty())return;
        int count=Math.min(5,next.size()),height=count*13+4;
        suggestionX=Math.clamp(active.getX()-5,x+10,x+w-170);suggestionW=Math.max(150,active.getWidth()+10);
        suggestionW=Math.min(suggestionW,x+w-10-suggestionX);suggestionY=active.getY()+16;
        if(suggestionY+height>bottom)suggestionY=active.getY()-height-6;
        context.getMatrices().push();context.getMatrices().translate(0,0,300);
        CompendiumStyle.drawInsetPanel(context,suggestionX,suggestionY,suggestionW,height);
        int first=Math.max(0,Math.min(suggestionIndex-4,next.size()-count));
        for(int i=0;i<count;i++){int index=first+i;if(index==suggestionIndex)context.fill(suggestionX+2,suggestionY+2+i*13,suggestionX+suggestionW-2,suggestionY+15+i*13,0xFF343434);
            context.drawText(textRenderer,textRenderer.trimToWidth(next.get(index),suggestionW-10),suggestionX+5,suggestionY+4+i*13,CompendiumStyle.TEXT,false);}
        context.getMatrices().pop();
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(suggestionField!=null&&!suggestions.isEmpty()){
            if(key==264||key==265){suggestionIndex=Math.floorMod(suggestionIndex+(key==264?1:-1),suggestions.size());return true;}
            if(key==257||key==258){suggestionField.setText(suggestions.get(suggestionIndex));setFocused(null);suggestions=List.of();return true;}
        }
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button==0&&suggestionField!=null&&!suggestions.isEmpty()&&mx>=suggestionX&&mx<suggestionX+suggestionW&&my>=suggestionY+2&&my<suggestionY+2+Math.min(5,suggestions.size())*13){
            int first=Math.max(0,Math.min(suggestionIndex-4,suggestions.size()-Math.min(5,suggestions.size())));suggestionField.setText(suggestions.get(first+(int)(my-suggestionY-2)/13));setFocused(null);suggestions=List.of();return true;}
        if(button==0&&mx>=x+w-13&&mx<x+w-5&&my>=top&&my<bottom&&scrollbar().maxScroll()>0){dragging=true;grab=scrollbar().grabOffset(my,scroll);scroll=scrollbar().scrollAt(my,grab);layoutBody();return true;}
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(my>=top&&my<bottom&&mx>=x&&mx<x+w){scroll=Math.clamp(scroll-(int)(vertical*29),0,scrollbar().maxScroll());layoutBody();return true;}return super.mouseScrolled(mx,my,horizontal,vertical);
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){if(dragging&&button==0){scroll=scrollbar().scrollAt(my,grab);layoutBody();return true;}return super.mouseDragged(mx,my,button,dx,dy);}
    @Override public boolean mouseReleased(double mx,double my,int button){dragging=false;return super.mouseReleased(mx,my,button);}
    @Override public void close(){client.setScreen(parent);}
}

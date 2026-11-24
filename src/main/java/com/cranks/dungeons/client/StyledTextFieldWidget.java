package com.cranks.dungeons.client;

import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Native editing/focus behavior with the shared frame and a padded text inset. */
public class StyledTextFieldWidget extends TextFieldWidget {
    private final int padY, frameHeight;
    public StyledTextFieldWidget(TextRenderer font,int x,int y,int width,int height,Text title) {
        super(font,x+5,y+(height-8)/2,Math.max(1,width-10),10,title);
        padY=(height-8)/2;frameHeight=height;
        setDrawsBackground(false);setEditableColor(CompendiumStyle.TEXT);setUneditableColor(CompendiumStyle.TEXT_DISABLED);
    }
    public int frameX(){return getX()-5;}
    public int frameY(){return getY()-padY;}
    public int frameWidth(){return getWidth()+10;}
    public int frameHeight(){return frameHeight;}
    public void moveFrame(int x,int y){setX(x+5);setY(y+padY);}
    @Override public void renderWidget(DrawContext context,int mouseX,int mouseY,float delta) {
        CompendiumStyle.drawTextFieldFrame(context,frameX(),frameY(),frameWidth(),frameHeight,isFocused());
        super.renderWidget(context,mouseX,mouseY,delta);
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(isVisible()&&active&&x>=frameX()&&x<frameX()+frameWidth()&&y>=frameY()&&y<frameY()+frameHeight)
            return super.mouseClicked(Math.clamp(x,getX(),getX()+getWidth()-1),Math.clamp(y,getY(),getY()+getHeight()-1),button);
        return super.mouseClicked(x,y,button);
    }
}

package com.cranks.dungeons.client.screen;
import com.cranks.dungeons.stat.passive.PassiveNode;
import net.minecraft.client.gui.screen.Screen;
import java.util.function.Consumer;
public class PassiveNodeEditScreen extends NodeEditScreen {
    public PassiveNodeEditScreen(Screen parent,PassiveNode node,Runnable refresh,Consumer<PassiveNode> connect){super(parent,node,0,0,false,refresh,connect);}
    public PassiveNodeEditScreen(Screen parent,double gx,double gy,Runnable refresh,Consumer<PassiveNode> connect){super(parent,null,gx,gy,false,refresh,connect);}
}

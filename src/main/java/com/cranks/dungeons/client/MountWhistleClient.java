package com.cranks.dungeons.client;

import com.cranks.dungeons.network.MountWhistleListPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class MountWhistleClient {
    private static boolean latched;
    private MountWhistleClient(){}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(MountWhistleListPayload.ID,(p,c)->c.client().execute(()->{if(c.client().currentScreen instanceof MountWhistleScreen screen)screen.update(p);}));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            boolean down=held(client);if(!down)latched=false;
            while(ModKeybinds.MOUNT_WHISTLE.wasPressed()){
                if(down&&!latched&&client.player!=null&&client.currentScreen==null&&!client.player.isSpectator()){
                    latched=true;client.setScreen(new MountWhistleScreen(KeyBindingHelper.getBoundKeyOf(ModKeybinds.MOUNT_WHISTLE)));
                }
            }
        });
    }
    private static boolean held(MinecraftClient client){
        var key=KeyBindingHelper.getBoundKeyOf(ModKeybinds.MOUNT_WHISTLE);long window=client.getWindow().getHandle();
        if(key.getCategory()==InputUtil.Type.MOUSE)return GLFW.glfwGetMouseButton(window,key.getCode())==GLFW.GLFW_PRESS;
        if(key.getCategory()==InputUtil.Type.KEYSYM)return key.getCode()>=0&&InputUtil.isKeyPressed(window,key.getCode());
        for(int code=GLFW.GLFW_KEY_SPACE;code<=GLFW.GLFW_KEY_LAST;code++)if(GLFW.glfwGetKeyScancode(code)==key.getCode())return InputUtil.isKeyPressed(window,code);
        return false;
    }
}

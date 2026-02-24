package com.cranks.dungeons.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static KeyBinding TOGGLE_ENCHANTMENT_DISABLE;

    public static void register() {
        TOGGLE_ENCHANTMENT_DISABLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cranks-dungeons.toggle_enchantment_disable",
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));
    }
}
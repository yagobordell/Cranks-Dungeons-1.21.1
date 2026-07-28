package com.cranks.dungeons.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static KeyBinding TOGGLE_ENCHANTMENT_DISABLE;
    public static KeyBinding OPEN_PASSIVE_TREE;
    public static KeyBinding OPEN_WORLD_TREE;
    public static KeyBinding OPEN_COMPENDIUM;



    public static final net.minecraft.client.option.KeyBinding.Category CATEGORY = new net.minecraft.client.option.KeyBinding.Category(net.minecraft.util.Identifier.of("cranks-dungeons", "keys"));

    public static void register() {
        TOGGLE_ENCHANTMENT_DISABLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cranks-dungeons.toggle_enchantment_disable",
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));
        
        OPEN_PASSIVE_TREE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cranks-dungeons.open_passive_tree",
                GLFW.GLFW_KEY_P,
                CATEGORY
        ));

        OPEN_WORLD_TREE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cranks-dungeons.open_world_tree",
                GLFW.GLFW_KEY_O,
                CATEGORY
        ));

        OPEN_COMPENDIUM = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cranks-dungeons.open_compendium",
                GLFW.GLFW_KEY_C,
                CATEGORY
        ));
    }
}
package com.cranks.dungeons.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static KeyBinding TOGGLE_ENCHANTMENT_DISABLE;
    public static KeyBinding OPEN_PASSIVE_TREE;
    public static KeyBinding OPEN_WORLD_TREE;
    public static KeyBinding OPEN_COMPENDIUM;
    public static KeyBinding TENTH_HOTBAR;
    public static KeyBinding SWAP_ARMOR;
    public static KeyBinding MOUNT_WHISTLE;
    public static KeyBinding SHOW_DETAILS;



    public static final String CATEGORY = "category.cranks-dungeons.keys";

    public static void register() {
        SHOW_DETAILS=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cranks-dungeons.show_details",GLFW.GLFW_KEY_LEFT_SHIFT,CATEGORY));
        MOUNT_WHISTLE=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cranks-dungeons.mount_whistle",GLFW.GLFW_KEY_V,CATEGORY));
        TENTH_HOTBAR = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cranks-dungeons.tenth_hotbar",
                GLFW.GLFW_KEY_0, CATEGORY));
        SWAP_ARMOR = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.cranks-dungeons.swap_armor",
                GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
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
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));
    }
}

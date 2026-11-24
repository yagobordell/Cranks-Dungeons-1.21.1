package com.cranks.dungeons.client.tooltip;

import com.cranks.dungeons.client.ModKeybinds;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** One input source for expanded equipment details and interactive tooltips. */
public final class TooltipDetails {
    private TooltipDetails() {}

    public static boolean isHeld() {
        var binding = ModKeybinds.SHOW_DETAILS;
        if (binding == null) return false;
        if (binding.isPressed()) return true;
        var key = KeyBindingHelper.getBoundKeyOf(binding);
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        if (key.getCategory() == InputUtil.Type.MOUSE)
            return GLFW.glfwGetMouseButton(window, key.getCode()) == GLFW.GLFW_PRESS;
        if (key.getCategory() != InputUtil.Type.KEYSYM || key.getCode() < 0) return false;
        if (key.getCode() == GLFW.GLFW_KEY_LEFT_SHIFT || key.getCode() == GLFW.GLFW_KEY_RIGHT_SHIFT)
            return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        return InputUtil.isKeyPressed(window, key.getCode());
    }

    public static Text keyName() {
        return ModKeybinds.SHOW_DETAILS == null ? Text.literal("Shift")
                : ModKeybinds.SHOW_DETAILS.getBoundKeyLocalizedText();
    }
}

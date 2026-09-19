package com.cranks.dungeons.client;

import com.cranks.dungeons.CranksDungeons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/** Selects an optional mod GUI sprite and falls back to a known vanilla sprite. */
public final class GuiTextureResolver {
    private GuiTextureResolver() {
    }

    public static Identifier customOrVanilla(String customSpritePath, Identifier vanillaFallback) {
        Identifier resource = Identifier.of(CranksDungeons.MOD_ID,
                "textures/gui/sprites/" + customSpritePath + ".png");
        if (MinecraftClient.getInstance().getResourceManager().getResource(resource).isPresent()) {
            return Identifier.of(CranksDungeons.MOD_ID, customSpritePath);
        }
        return vanillaFallback;
    }
}

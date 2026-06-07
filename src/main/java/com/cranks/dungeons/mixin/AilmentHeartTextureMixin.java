package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class AilmentHeartTextureMixin {

    // Ailment heart textures
    @Unique private static final Identifier POISON_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/full");
    @Unique private static final Identifier POISON_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/half");
    @Unique private static final Identifier POISON_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/full_blinking");
    @Unique private static final Identifier POISON_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/half_blinking");
    @Unique private static final Identifier POISON_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_full");
    @Unique private static final Identifier POISON_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_half");
    @Unique private static final Identifier POISON_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_full_blinking");
    @Unique private static final Identifier POISON_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/poison/hardcore_half_blinking");

    @Unique private static final Identifier BURN_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/full");
    @Unique private static final Identifier BURN_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/half");
    @Unique private static final Identifier BURN_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/full_blinking");
    @Unique private static final Identifier BURN_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/half_blinking");
    @Unique private static final Identifier BURN_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_full");
    @Unique private static final Identifier BURN_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_half");
    @Unique private static final Identifier BURN_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_full_blinking");
    @Unique private static final Identifier BURN_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/burn/hardcore_half_blinking");

    @Unique private static final Identifier FREEZE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/full");
    @Unique private static final Identifier FREEZE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/half");
    @Unique private static final Identifier FREEZE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/full_blinking");
    @Unique private static final Identifier FREEZE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/half_blinking");
    @Unique private static final Identifier FREEZE_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_full");
    @Unique private static final Identifier FREEZE_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_half");
    @Unique private static final Identifier FREEZE_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_full_blinking");
    @Unique private static final Identifier FREEZE_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/freeze/hardcore_half_blinking");

    @Unique private static final Identifier BLEED_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/full");
    @Unique private static final Identifier BLEED_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/half");
    @Unique private static final Identifier BLEED_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/full_blinking");
    @Unique private static final Identifier BLEED_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/half_blinking");
    @Unique private static final Identifier BLEED_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_full");
    @Unique private static final Identifier BLEED_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_half");
    @Unique private static final Identifier BLEED_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_full_blinking");
    @Unique private static final Identifier BLEED_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/bleed/hardcore_half_blinking");

    @Unique private static final Identifier SHOCK_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/full");
    @Unique private static final Identifier SHOCK_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/half");
    @Unique private static final Identifier SHOCK_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/full_blinking");
    @Unique private static final Identifier SHOCK_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/half_blinking");
    @Unique private static final Identifier SHOCK_HARDCORE_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/hardcore_full");
    @Unique private static final Identifier SHOCK_HARDCORE_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/hardcore_half");
    @Unique private static final Identifier SHOCK_HARDCORE_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/hardcore_full_blinking");
    @Unique private static final Identifier SHOCK_HARDCORE_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shock/hardcore_half_blinking");

    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void onDrawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, CallbackInfo ci) {
        // Only affect NORMAL health hearts
        if (type != InGameHud.HeartType.NORMAL) {
            return;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        // Get current ailment
        AilmentType ailment = getCurrentAilment(player);
        if (ailment == null) return;

        // Cancel vanilla and draw with ailment texture
        ci.cancel();
        Identifier texture = getTexture(ailment, hardcore, blinking, half);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 9, 9);
    }

    @Unique
    private AilmentType getCurrentAilment(PlayerEntity player) {
        if (AilmentManager.hasAilment(player, AilmentType.POISON)) return AilmentType.POISON;
        if (AilmentManager.hasAilment(player, AilmentType.BURN)) return AilmentType.BURN;
        if (AilmentManager.hasAilment(player, AilmentType.BLEED)) return AilmentType.BLEED;
        if (AilmentManager.hasAilment(player, AilmentType.FREEZE)) return AilmentType.FREEZE;
        if (AilmentManager.hasAilment(player, AilmentType.SHOCK)) return AilmentType.SHOCK;
        return null;
    }

    @Unique
    private Identifier getTexture(AilmentType ailment, boolean hardcore, boolean blinking, boolean half) {
        return switch (ailment) {
            case POISON -> selectTexture(hardcore, blinking, half, POISON_FULL, POISON_HALF, POISON_FULL_BLINKING, POISON_HALF_BLINKING, POISON_HARDCORE_FULL, POISON_HARDCORE_HALF, POISON_HARDCORE_FULL_BLINKING, POISON_HARDCORE_HALF_BLINKING);
            case BURN -> selectTexture(hardcore, blinking, half, BURN_FULL, BURN_HALF, BURN_FULL_BLINKING, BURN_HALF_BLINKING, BURN_HARDCORE_FULL, BURN_HARDCORE_HALF, BURN_HARDCORE_FULL_BLINKING, BURN_HARDCORE_HALF_BLINKING);
            case BLEED -> selectTexture(hardcore, blinking, half, BLEED_FULL, BLEED_HALF, BLEED_FULL_BLINKING, BLEED_HALF_BLINKING, BLEED_HARDCORE_FULL, BLEED_HARDCORE_HALF, BLEED_HARDCORE_FULL_BLINKING, BLEED_HARDCORE_HALF_BLINKING);
            case FREEZE -> selectTexture(hardcore, blinking, half, FREEZE_FULL, FREEZE_HALF, FREEZE_FULL_BLINKING, FREEZE_HALF_BLINKING, FREEZE_HARDCORE_FULL, FREEZE_HARDCORE_HALF, FREEZE_HARDCORE_FULL_BLINKING, FREEZE_HARDCORE_HALF_BLINKING);
            case SHOCK, ELECTROCUTE -> selectTexture(hardcore, blinking, half, SHOCK_FULL, SHOCK_HALF, SHOCK_FULL_BLINKING, SHOCK_HALF_BLINKING, SHOCK_HARDCORE_FULL, SHOCK_HARDCORE_HALF, SHOCK_HARDCORE_FULL_BLINKING, SHOCK_HARDCORE_HALF_BLINKING);
        };
    }

    @Unique
    private Identifier selectTexture(boolean hardcore, boolean blinking, boolean half,
                                      Identifier full, Identifier halfNormal, Identifier fullBlink, Identifier halfBlink,
                                      Identifier hcFull, Identifier hcHalf, Identifier hcFullBlink, Identifier hcHalfBlink) {
        if (hardcore) {
            return blinking ? (half ? hcHalfBlink : hcFullBlink) : (half ? hcHalf : hcFull);
        } else {
            return blinking ? (half ? halfBlink : fullBlink) : (half ? halfNormal : full);
        }
    }
}

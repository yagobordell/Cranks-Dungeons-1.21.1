package com.cranks.dungeons.mixin;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class EnergyShieldRenderMixin {

    @Unique
    private static final Identifier SHIELD_FULL = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full");
    @Unique
    private static final Identifier SHIELD_HALF = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half");
    @Unique
    private static final Identifier SHIELD_FULL_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/full_blinking");
    @Unique
    private static final Identifier SHIELD_HALF_BLINKING = Identifier.of(CranksDungeons.MOD_ID, "hud/heart/shield/half_blinking");

    @Shadow @Final private MinecraftClient client;
    @Unique private static final boolean OVERFLOWING_BARS_LOADED = FabricLoader.getInstance().isModLoaded("overflowingbars");
    @Unique private static int shieldRowCount = 0;
    @Unique private static float cranks$lastRenderedShield = -1;
    @Unique private static long cranks$lastShieldDamageTime = 0;
    @Unique private static final long SHIELD_BLINK_DURATION = 1000; // Blink for 1000ms (1 second) to allow 2 blinks

    @ModifyVariable(method = "renderHealthBar", at = @At(value = "STORE", ordinal = 0), ordinal = 9)
    private int modifyArmorY(int originalY) {
        return originalY - (shieldRowCount * 10);
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"))
    private void calculateShieldRows(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (client.player == null) { shieldRowCount = 0; return; }
        // Skip calculation if CustomAilmentHeartRenderMixin is handling rendering
        if (AilmentManager.hasAilment(client.player, AilmentType.POISON) ||
            AilmentManager.hasAilment(client.player, AilmentType.BURN) ||
            AilmentManager.hasAilment(client.player, AilmentType.BLEED) ||
            AilmentManager.hasAilment(client.player, AilmentType.FREEZE) ||
            OVERFLOWING_BARS_LOADED) {
            shieldRowCount = 0;
            return;
        }
        double maxShield = client.player.getAttributeValue(ModAttributes.ENERGY_SHIELD);
        if (client.player.getAttributeValue(ModAttributes.KEYSTONE_CRIMSON_PACT) > 0) {
            maxShield = 0;
        }
        if (maxShield <= 0) { shieldRowCount = 0; return; }

        int maxHealthHearts = (int) Math.ceil(maxHealth / 2.0f);
        int absorptionHearts = (int) Math.ceil(absorption / 2.0f);
        int totalShieldHearts = (int) Math.ceil(maxShield / 2.0);
        int totalHearts = maxHealthHearts + absorptionHearts + totalShieldHearts;
        int totalRows = (int) Math.ceil(totalHearts / 10.0);
        shieldRowCount = totalRows - lines;
    }

    @Inject(method = "renderHealthBar", at = @At("RETURN"))
    private void renderEnergyShieldHearts(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        // Energy shields are now rendered independently by EnergyShieldHudRenderer
        // This mixin is kept for the armor Y-offset modification only
    }
}

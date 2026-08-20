package com.cranks.dungeons.config;

import io.wispforest.owo.config.annotation.*;

@Config(name = "cranks-dungeons", wrapperName = "ModConfig")
@Modmenu(modId = "cranks-dungeons")
public class ModConfigModel {

    @SectionHeader("hud")
    public boolean useStatBars = false;
    public boolean showEntityHealthBars = true;
    public boolean showPlayerInfoAboveHead = true;
    
    @SectionHeader("nameplate_self")
    public boolean showSelfName = true;
    public boolean showSelfLevel = true;
    public boolean showSelfBars = true;
    public boolean showSelfParty = true;
    
    @SectionHeader("nameplate_other_players")
    public boolean showOtherPlayersName = true;
    public boolean showOtherPlayersLevel = true;
    public boolean showOtherPlayersBars = true;
    public boolean showOtherPlayersParty = true;
    
    @SectionHeader("nameplate_mobs")
    public HealthBarDisplayMode hostileMobHealthBars = HealthBarDisplayMode.ALWAYS;
    public HealthBarDisplayMode neutralMobHealthBars = HealthBarDisplayMode.WHEN_DAMAGED;
    public HealthBarDisplayMode friendlyMobHealthBars = HealthBarDisplayMode.WHEN_DAMAGED;
    public boolean showMobsName = true;
    public boolean showMobsLevel = true;
    public boolean showMobsBars = true;
    
    @SectionHeader("combat")
    public boolean showMyDealtDamage = true;
    public boolean showMyReceivedDamage = true;
    public boolean showOtherPlayerDamage = true;
    
    public boolean showMyReceivedHealing = true;
    public boolean showOtherPlayerHealing = true;
    
    @SectionHeader("party_hud")
    public boolean partyHudEnabled = true;
    public boolean partyHudShowSelf = true;
    
    @RangeConstraint(min = -1, max = 2000)
    @ExcludeFromScreen
    public int partyHudX = -1;
    
    @RangeConstraint(min = 0, max = 2000)
    @ExcludeFromScreen
    public int partyHudY = 190;
    
    @RangeConstraint(min = 0.5f, max = 2.0f)
    public float partyHudSize = 1.0f;
    
    @RangeConstraint(min = 1, max = 10)
    public int partyHudMaxRows = 5;
    
    @RangeConstraint(min = 1, max = 2)
    public int partyHudMaxColumns = 2;

    @SectionHeader("status_effects")
    public boolean customStatusEffectOverlay = true;
    public StatusEffectOrientation statusEffectsOrientation = StatusEffectOrientation.TOP_TO_BOTTOM;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int statusEffectsX = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int statusEffectsY = -1;

    @RangeConstraint(min = 0.5f, max = 2.0f)
    public float statusEffectsScale = 1.0f;

    @RangeConstraint(min = 1, max = 50)
    public int statusEffectsMaxPerLine = 10;

    @RangeConstraint(min = 18, max = 60)
    public int statusEffectsSpacing = 26;

    public boolean statusEffectsShowDuration = false;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int healthBarX = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int healthBarY = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int shieldBarX = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int shieldBarY = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int manaBarX = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int manaBarY = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int hungerBarX = -1;

    @RangeConstraint(min = -1, max = 3000)
    @ExcludeFromScreen
    public int hungerBarY = -1;
}

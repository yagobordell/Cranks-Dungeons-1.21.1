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
    public int partyHudX = -1;
    
    @RangeConstraint(min = 0, max = 2000)
    public int partyHudY = 190;
    
    @RangeConstraint(min = 0.5f, max = 2.0f)
    public float partyHudSize = 1.0f;
    
    @RangeConstraint(min = 1, max = 10)
    public int partyHudMaxRows = 5;
    
    @RangeConstraint(min = 1, max = 2)
    public int partyHudMaxColumns = 2;
}

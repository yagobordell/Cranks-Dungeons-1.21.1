package com.cranks.dungeons;

public interface EnergyShieldProvider {
    float cranksdungeons$getCurrentEnergyShield();
    void cranksdungeons$setCurrentEnergyShield(float shield);
    long cranksdungeons$getLastDamageTime();
    void cranksdungeons$setLastDamageTime(long time);
}
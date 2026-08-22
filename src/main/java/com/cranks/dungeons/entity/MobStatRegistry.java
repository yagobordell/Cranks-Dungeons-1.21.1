package com.cranks.dungeons.entity;

import com.cranks.dungeons.registry.ModAttributes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;

public class MobStatRegistry {

    public static void registerMobStats() {
        // --- ZOMBIES ---
        register(EntityType.ZOMBIE, ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_ARMOR, 30.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.15));

        register(EntityType.HUSK, HuskEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 75.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_ARMOR, 45.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.50)
                .add(ModAttributes.COLD_RESISTANCE, -0.50)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.25));

        register(EntityType.DROWNED, DrownedEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 65.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
                .add(EntityAttributes.GENERIC_ARMOR, 20.0)
                .add(ModAttributes.EVASION_RATING, 25.0)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.50)
                .add(ModAttributes.COLD_RESISTANCE, 0.30)
                .add(ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE, 0.10)
                .add(ModAttributes.SPEAR_DAMAGE_PERCENT, 0.20));

        register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_ARMOR, 25.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25));

        // --- SKELETONS ---
        register(EntityType.SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 45.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(ModAttributes.EVASION_RATING, 40.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25)
                .add(ModAttributes.PROJECTILE_SPEED, 0.20)
                .add(ModAttributes.CRIT_CHANCE, 0.15)
                .add(ModAttributes.CRIT_MULTIPLIER, 1.75));

        register(EntityType.STRAY, AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 55.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 15.0)
                .add(ModAttributes.EVASION_RATING, 35.0)
                .add(ModAttributes.COLD_RESISTANCE, 0.75)
                .add(ModAttributes.FIRE_RESISTANCE, -0.50)
                .add(ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT, 0.50)
                .add(ModAttributes.FREEZE_BUILDUP_PERCENT, 0.50)
                .add(ModAttributes.FREEZE_MAGNITUDE_PERCENT, 0.30));

        register(EntityType.BOGGED, BoggedEntity.createBoggedAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 13.0)
                .add(EntityAttributes.GENERIC_ARMOR, 15.0)
                .add(ModAttributes.EVASION_RATING, 30.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25)
                .add(ModAttributes.COLD_RESISTANCE, 0.25)
                .add(ModAttributes.CHANCE_TO_POISON, 0.60)
                .add(ModAttributes.POISON_MAGNITUDE_PERCENT, 0.30));

        register(EntityType.WITHER_SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 22.0)
                .add(EntityAttributes.GENERIC_ARMOR, 50.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.VOID_RESISTANCE, 0.50)
                .add(ModAttributes.COLD_RESISTANCE, -0.25)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 0.50)
                .add(ModAttributes.FLAT_ARMOR_PENETRATION, 25.0)
                .add(ModAttributes.CRIT_CHANCE, 0.15)
                .add(ModAttributes.CRIT_MULTIPLIER, 1.75));

        // --- ARTHROPODS ---
        register(EntityType.SPIDER, SpiderEntity.createSpiderAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(ModAttributes.EVASION_RATING, 65.0)
                .add(ModAttributes.COLD_RESISTANCE, -0.25)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.20)
                .add(ModAttributes.CRIT_CHANCE, 0.10));

        register(EntityType.CAVE_SPIDER, CaveSpiderEntity.createCaveSpiderAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_ARMOR, 5.0)
                .add(ModAttributes.EVASION_RATING, 80.0)
                .add(ModAttributes.COLD_RESISTANCE, -0.25)
                .add(ModAttributes.CHANCE_TO_POISON, 1.0)
                .add(ModAttributes.POISON_MAGNITUDE_PERCENT, 0.50));

        register(EntityType.SILVERFISH, SilverfishEntity.createSilverfishAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 95.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.50)
                .add(ModAttributes.ATTACK_SPEED, 0.20));

        register(EntityType.ENDERMITE, EndermiteEntity.createEndermiteAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 85.0)
                .add(ModAttributes.ENERGY_SHIELD, 15.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.75)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 0.50));

        // --- CREEPER ---
        register(EntityType.CREEPER, CreeperEntity.createCreeperAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_ARMOR, 15.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25)
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.50));

        // --- ILLAGERS & CASTERS ---
        register(EntityType.PILLAGER, PillagerEntity.createPillagerAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 65.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
                .add(EntityAttributes.GENERIC_ARMOR, 25.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.CROSSBOW_DAMAGE_PERCENT, 0.25)
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.15));

        register(EntityType.VINDICATOR, VindicatorEntity.createVindicatorAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 90.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 26.0)
                .add(EntityAttributes.GENERIC_ARMOR, 40.0)
                .add(ModAttributes.EVASION_RATING, 15.0)
                .add(ModAttributes.AXE_DAMAGE_PERCENT, 0.30)
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.20)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.20));

        register(EntityType.WITCH, WitchEntity.createWitchAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.ENERGY_SHIELD, 60.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.30)
                .add(ModAttributes.COLD_RESISTANCE, 0.30)
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.30)
                .add(ModAttributes.VOID_RESISTANCE, 0.30)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.REDUCED_BURN_DURATION_ON_YOU, 0.50)
                .add(ModAttributes.REDUCED_FREEZE_DURATION_ON_YOU, 0.50)
                .add(ModAttributes.REDUCED_POISON_DURATION_ON_YOU, 0.50)
                .add(ModAttributes.REDUCED_SHOCK_DURATION_ON_YOU, 0.50));

        register(EntityType.EVOKER, EvokerEntity.createEvokerAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 22.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(ModAttributes.EVASION_RATING, 15.0)
                .add(ModAttributes.ENERGY_SHIELD, 80.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.50)
                .add(ModAttributes.FIRE_RESISTANCE, 0.25)
                .add(ModAttributes.COLD_RESISTANCE, 0.25)
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.25)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.LIFE_REGENERATION, 2.0));

        register(EntityType.VEX, VexEntity.createVexAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 85.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.50)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 1.0));

        register(EntityType.RAVAGER, RavagerEntity.createRavagerAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 350.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 40.0)
                .add(EntityAttributes.GENERIC_ARMOR, 140.0)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.40)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.25));

        // --- NETHER MOBS ---
        register(EntityType.BLAZE, BlazeEntity.createBlazeAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 15.0)
                .add(ModAttributes.EVASION_RATING, 30.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.75)
                .add(ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.FLAMMABILITY_MAGNITUDE_PERCENT, 0.50)
                .add(ModAttributes.BURN_MAGNITUDE_PERCENT, 0.30));

        register(EntityType.MAGMA_CUBE, MagmaCubeEntity.createMagmaCubeAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.GENERIC_ARMOR, 60.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.50)
                .add(ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT, 0.60)
                .add(ModAttributes.FLAMMABILITY_MAGNITUDE_PERCENT, 0.30));

        register(EntityType.GHAST, GhastEntity.createGhastAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.50)
                .add(ModAttributes.COLD_RESISTANCE, -0.50)
                .add(ModAttributes.PHYSICAL_TO_FIRE_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.BURN_MAGNITUDE_PERCENT, 0.80));

        register(EntityType.ZOMBIFIED_PIGLIN, ZombifiedPiglinEntity.createZombifiedPiglinAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 85.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.GENERIC_ARMOR, 40.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.75));

        register(EntityType.PIGLIN, PiglinEntity.createPiglinAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 55.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_ARMOR, 25.0)
                .add(ModAttributes.EVASION_RATING, 25.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.30)
                .add(ModAttributes.CRIT_CHANCE, 0.10)
                .add(ModAttributes.SWORD_DAMAGE_PERCENT, 0.20)
                .add(ModAttributes.CROSSBOW_DAMAGE_PERCENT, 0.20));

        register(EntityType.PIGLIN_BRUTE, PiglinBruteEntity.createPiglinBruteAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 140.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 30.0)
                .add(EntityAttributes.GENERIC_ARMOR, 80.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.50)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 0.60)
                .add(ModAttributes.AXE_DAMAGE_PERCENT, 0.25)
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.20)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.15));

        register(EntityType.HOGLIN, HoglinEntity.createHoglinAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 110.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 24.0)
                .add(EntityAttributes.GENERIC_ARMOR, 40.0)
                .add(ModAttributes.FIRE_RESISTANCE, -0.25)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 0.50)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.20));

        register(EntityType.ZOGLIN, ZoglinEntity.createZoglinAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 120.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 26.0)
                .add(EntityAttributes.GENERIC_ARMOR, 35.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.50)
                .add(ModAttributes.VOID_RESISTANCE, 0.30)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 0.75)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.30));

        register(EntityType.STRIDER, StriderEntity.createStriderAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.75));

        // --- OCEAN & SKY ---
        register(EntityType.GUARDIAN, GuardianEntity.createGuardianAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 70.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.GENERIC_ARMOR, 50.0)
                .add(ModAttributes.COLD_RESISTANCE, 0.50)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.25)
                .add(ModAttributes.PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.THORNS_DAMAGE, 12.0)
                .add(ModAttributes.CHANCE_TO_SHOCK, 0.30));

        register(EntityType.PHANTOM, MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 35.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 70.0)
                .add(ModAttributes.ENERGY_SHIELD, 25.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.50)
                .add(ModAttributes.FIRE_RESISTANCE, -0.50)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 0.40)
                .add(ModAttributes.CRIT_CHANCE, 0.10));

        register(EntityType.BREEZE, BreezeEntity.createBreezeAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 75.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(ModAttributes.EVASION_RATING, 90.0)
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, 0.30)
                .add(ModAttributes.PHYSICAL_TO_LIGHTNING_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.ELECTROCUTE_BUILDUP_PERCENT, 0.40)
                .add(ModAttributes.PROJECTILE_SPEED, 0.30));

        // --- END MOBS ---
        register(EntityType.ENDERMAN, EndermanEntity.createEndermanAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 22.0)
                .add(EntityAttributes.GENERIC_ARMOR, 15.0)
                .add(ModAttributes.EVASION_RATING, 40.0)
                .add(ModAttributes.ENERGY_SHIELD, 50.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.50)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 0.40)
                .add(ModAttributes.CRIT_CHANCE, 0.15));

        register(EntityType.SHULKER, ShulkerEntity.createShulkerAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 100.0)
                .add(ModAttributes.ENERGY_SHIELD, 80.0)
                .add(ModAttributes.VOID_RESISTANCE, 0.75)
                .add(ModAttributes.PHYSICAL_TO_VOID_CONVERSION_PERCENT, 1.0));

        // --- SLIME ---
        register(EntityType.SLIME, MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 0.40));

        // --- ALLIES & WILDLIFE ---
        register(EntityType.IRON_GOLEM, IronGolemEntity.createIronGolemAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 350.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 45.0)
                .add(EntityAttributes.GENERIC_ARMOR, 180.0)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.30));

        register(EntityType.SNOW_GOLEM, SnowGolemEntity.createSnowGolemAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
                .add(ModAttributes.EVASION_RATING, 20.0)
                .add(ModAttributes.COLD_RESISTANCE, 0.75)
                .add(ModAttributes.FIRE_RESISTANCE, -1.0)
                .add(ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT, 1.0)
                .add(ModAttributes.FREEZE_BUILDUP_PERCENT, 0.60));

        register(EntityType.POLAR_BEAR, PolarBearEntity.createPolarBearAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 90.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20.0)
                .add(EntityAttributes.GENERIC_ARMOR, 30.0)
                .add(ModAttributes.EVASION_RATING, 15.0)
                .add(ModAttributes.COLD_RESISTANCE, 0.75)
                .add(ModAttributes.FIRE_RESISTANCE, -0.30)
                .add(ModAttributes.PHYSICAL_TO_COLD_CONVERSION_PERCENT, 0.25)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.20));

        register(EntityType.WOLF, WolfEntity.createWolfAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 20.0)
                .add(ModAttributes.EVASION_RATING, 45.0)
                .add(ModAttributes.CHANCE_TO_BLEED, 0.25)
                .add(ModAttributes.CRIT_CHANCE, 0.20)
                .add(ModAttributes.CRIT_MULTIPLIER, 1.75));

        register(EntityType.ARMADILLO, ArmadilloEntity.createArmadilloAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_ARMOR, 150.0)
                .add(ModAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(ModAttributes.REDUCED_CRIT_DAMAGE_TAKEN_PERCENT, 0.50));
    }

    private static <T extends LivingEntity> void register(EntityType<T> type, DefaultAttributeContainer.Builder builder) {
        FabricDefaultAttributeRegistry.register(type, builder);
    }
}

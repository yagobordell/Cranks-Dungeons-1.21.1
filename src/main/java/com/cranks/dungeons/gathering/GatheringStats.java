package com.cranks.dungeons.gathering;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class GatheringStats {
    private GatheringStats() {}
    public static final Set<String> LEGACY=Set.of("fortune","crop_fortune","log_fortune","dig_fortune","farming_speed_percent",
            "mining_experience","crop_experience","breeding_experience","fishing_experience","combat_experience");
    public static Double display(String id,PlayerEntity player,ToDoubleFunction<RegistryEntry<EntityAttribute>> value){
        var dimension=player.getWorld().getRegistryKey();
        return switch(id){
            case "ore_yield"->GatheringRules.bounded(value.applyAsDouble(ModAttributes.ORE_YIELD)+value.applyAsDouble(ModAttributes.FORTUNE)
                    +(dimension==World.OVERWORLD?value.applyAsDouble(ModAttributes.ORE_YIELD_IN_OVERWORLD):dimension==World.NETHER?value.applyAsDouble(ModAttributes.ORE_YIELD_IN_NETHER):0),3);
            case "log_yield"->GatheringRules.bounded(value.applyAsDouble(ModAttributes.LOG_YIELD)+value.applyAsDouble(ModAttributes.LOG_FORTUNE),3);
            case "dig_yield"->GatheringRules.bounded(value.applyAsDouble(ModAttributes.DIG_YIELD)+value.applyAsDouble(ModAttributes.DIG_FORTUNE),3);
            case "crop_yield"->GatheringRules.bounded(value.applyAsDouble(ModAttributes.CROP_YIELD)+value.applyAsDouble(ModAttributes.CROP_FORTUNE),3);
            case "seed_yield"->GatheringRules.bounded(value.applyAsDouble(ModAttributes.SEED_YIELD)+value.applyAsDouble(ModAttributes.CROP_FORTUNE),3);
            case "world_discovery_experience"->value.applyAsDouble(ModAttributes.WORLD_DISCOVERY_EXPERIENCE)+value.applyAsDouble(ModAttributes.EXPERIENCE_BONUS);
            case "precision_mining"->GatheringRules.preservation(value.applyAsDouble(ModAttributes.PRECISION_MINING),value.applyAsDouble(ModAttributes.REPAIR_CHANCE));
            case "repair_chance"->GatheringRules.preservation(0,value.applyAsDouble(ModAttributes.REPAIR_CHANCE));
            default->null;
        };
    }
}

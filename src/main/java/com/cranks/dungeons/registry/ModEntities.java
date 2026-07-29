package com.cranks.dungeons.registry;

import com.cranks.dungeons.entity.LevelUpVfxEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<LevelUpVfxEntity> LEVEL_UP_VFX = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.tryParse("cranks-dungeons:level_up_vfx"),
            FabricEntityTypeBuilder.<LevelUpVfxEntity>create(SpawnGroup.MISC, LevelUpVfxEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(20)
                    .build()
    );

    public static void register() {
        // Just loads the class
    }
}

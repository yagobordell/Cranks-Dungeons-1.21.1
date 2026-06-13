package com.cranks.dungeons.stat.passive;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveSkillApplier {

    private static final Map<UUID, Map<Identifier, RegistryEntry<EntityAttribute>>> appliedModifiers = new HashMap<>();

    public static void updatePlayerPassives(PlayerEntity player) {
        if (player.getEntityWorld().isClient()) return; // Server only normally

        removeAllModifiers(player);

        Map<RegistryEntry<EntityAttribute>, Double> totalStats = PassiveStatHelper.getPassiveTreeSummary(player);

        for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : totalStats.entrySet()) {
            applyStatModifier(player, entry.getKey(), entry.getValue());
        }
    }

    private static void applyStatModifier(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, double value) {
        if (value == 0) return;
        
        String attrKey = attribute.getKey().map(k -> k.getValue().getPath()).orElse("unknown_attr");
        Identifier modifierId = Identifier.of(CranksDungeons.MOD_ID, "passive_" + attrKey);
        
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,
                value,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        var attributeInstance = player.getAttributeInstance(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifierId);
            attributeInstance.addTemporaryModifier(modifier);
            appliedModifiers.computeIfAbsent(player.getUuid(), k -> new HashMap<>()).put(modifierId, attribute);
        }
    }

    public static void removeAllModifiers(PlayerEntity player) {
        Map<Identifier, RegistryEntry<EntityAttribute>> modifiers = appliedModifiers.get(player.getUuid());
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }

        for (Map.Entry<Identifier, RegistryEntry<EntityAttribute>> entry : modifiers.entrySet()) {
            Identifier modifierId = entry.getKey();
            RegistryEntry<EntityAttribute> attribute = entry.getValue();
            var attributeInstance = player.getAttributeInstance(attribute);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifierId);
            }
        }
        modifiers.clear();
    }

    public static void onPlayerDisconnect(PlayerEntity player) {
        appliedModifiers.remove(player.getUuid());
    }
}

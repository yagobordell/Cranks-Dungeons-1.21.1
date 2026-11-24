package com.cranks.dungeons.component;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<WalletComponent> WALLET = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "wallet"), WalletComponent.class);

    public static final ComponentKey<AlchemyKnowledgeComponent> ALCHEMY_KNOWLEDGE =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "alchemy_knowledge"), AlchemyKnowledgeComponent.class);

    public static final ComponentKey<PassiveDataComponent> PASSIVE_DATA = 
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "passive_data"), PassiveDataComponent.class);
            
    public static final ComponentKey<WorldTreeDataComponent> WORLD_TREE_DATA = 
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "world_tree_data"), WorldTreeDataComponent.class);

    public static final ComponentKey<ItemDiscoveryComponent> ITEM_DISCOVERIES =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "item_discoveries"), ItemDiscoveryComponent.class);

    public static final ComponentKey<PlayerProgressionComponent> PROGRESSION =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("cranks-dungeons", "progression"), PlayerProgressionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(WALLET, WalletComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PROGRESSION, PlayerProgressionComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ALCHEMY_KNOWLEDGE, AlchemyKnowledgeComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        // ALWAYS_COPY means it is kept upon death and changing dimensions, we manage clearing explicitly if needed
        registry.registerForPlayers(PASSIVE_DATA, PassiveDataComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(WORLD_TREE_DATA, WorldTreeDataComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(ITEM_DISCOVERIES, ItemDiscoveryComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

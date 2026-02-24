package com.cranks.dungeons;

import com.cranks.dungeons.block.ModBlocks;
import com.cranks.dungeons.block.entity.ModBlockEntities;
import com.cranks.dungeons.command.ApplyStatCommand;
import com.cranks.dungeons.command.CheckStatsCommand;
import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.event.CropXpEventHandler;
import com.cranks.dungeons.event.EnchantmentDisableTickHandler;
import com.cranks.dungeons.event.FortuneEventHandler;
import com.cranks.dungeons.item.ModItems;
import com.cranks.dungeons.loot.RunicTomeLootHandler;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.ModNetworking;
import com.cranks.dungeons.network.StatOptionsSyncPacket;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.screen.ModScreenHandlers;
import com.cranks.dungeons.stat.StatRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CranksDungeons implements ModInitializer {

	public static final String MOD_ID = "cranks-dungeons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModNetworking.registerPayloads();
		ModNetworking.registerServerReceivers();
		EnchantmentDisableTickHandler.register();
		ModBlockEntities.register();
		ModItems.registerModItems();
		ModScreenHandlers.register();
		ModAttributes.registerAttributes();
		ModSounds.registerSounds();
		PayloadTypeRegistry.playS2C().register(StatOptionsSyncPacket.ID, StatOptionsSyncPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(EnergyShieldSyncPacket.ID, EnergyShieldSyncPacket.CODEC);
		StatRegistry.registerStats();
		EnchantmentDisableManager.registerToggleable(Identifier.of("minecraft", "silk_touch"));
		EnchantmentDisableManager.registerToggleable(Identifier.of("cranks-dungeons", "vein_mining"));
		ApplyStatCommand.register();
		CheckStatsCommand.register();
		FortuneEventHandler.register();
		CropXpEventHandler.register();
		RunicTomeLootHandler.register();
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes()
						.add(ModAttributes.CROP_FORTUNE, 0.0)
						.add(ModAttributes.FLYING_SPEED)
						.add(ModAttributes.EVASION_RATING, 0.0)
						.add(ModAttributes.ENERGY_SHIELD, 0.0)
						.add(ModAttributes.RELOAD_SPEED, 0.0)
						.add(ModAttributes.CROP_EXPERIENCE, 0.0)
		);

		registerCreativeTabItems();

		LOGGER.info("Cranks Dungeons initialized!");
	}

	private void registerCreativeTabItems() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(ModBlocks.RUNIC_ENHANCEMENT_ALTAR);
			content.add(ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(ModItems.RUNIC_TOME_T1);
			content.add(ModItems.RUNIC_TOME_T2);
			content.add(ModItems.RUNIC_TOME_T3);
			content.add(ModItems.RUNIC_TOME_T4);
			content.add(ModItems.RUNIC_TOME_T5);
			content.add(ModItems.TOME_OF_FIRE);
			content.add(ModItems.TOME_OF_COLD);
			content.add(ModItems.TOME_OF_LIGHTNING);
			content.add(ModItems.TOME_OF_VOID);
			content.add(ModItems.TOME_OF_PHYSICALITY);
			content.add(ModItems.GREATER_TOME_OF_FIRE);
			content.add(ModItems.GREATER_TOME_OF_COLD);
			content.add(ModItems.GREATER_TOME_OF_LIGHTNING);
			content.add(ModItems.GREATER_TOME_OF_VOID);
			content.add(ModItems.GREATER_TOME_OF_PHYSICALITY);
			content.add(ModItems.ARTIFICIAL_TOME);
		});
	}
}
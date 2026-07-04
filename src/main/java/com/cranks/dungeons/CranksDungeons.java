package com.cranks.dungeons;

import com.cranks.dungeons.block.ModBlocks;
import com.cranks.dungeons.block.entity.ModBlockEntities;
import com.cranks.dungeons.command.AilmentCommand;
import com.cranks.dungeons.command.ApplyStatCommand;
import com.cranks.dungeons.command.CheckStatsCommand;
import com.cranks.dungeons.command.PartyCommand;
import com.cranks.dungeons.party.PartyHealthSyncHandler;
import com.cranks.dungeons.command.RuneCommand;
import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.enchantment.EnchantmentDisableManager;
import com.cranks.dungeons.event.*;
import com.cranks.dungeons.item.ModItems;
import com.cranks.dungeons.loot.RunicTomeLootHandler;
import com.cranks.dungeons.network.EnergyShieldSyncPacket;
import com.cranks.dungeons.network.ModNetworking;
import com.cranks.dungeons.network.StatOptionsSyncPacket;
import com.cranks.dungeons.particle.ModParticles;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.screen.ModScreenHandlers;
import com.cranks.dungeons.stat.StatRegistry;
import com.cranks.dungeons.util.EquipmentChangeHandler;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.stat.passive.PassiveSkillApplier;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import com.cranks.dungeons.config.PassiveTreeConfig;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CranksDungeons implements ModInitializer {

	public static final String MOD_ID = "cranks-dungeons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final com.cranks.dungeons.config.ModConfig CONFIG = com.cranks.dungeons.config.ModConfig
			.createAndLoad();

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModNetworking.registerPayloads();
		ModNetworking.registerServerReceivers();
		EnchantmentDisableTickHandler.register();
		EquipmentChangeHandler.register();
		com.cranks.dungeons.util.TwoHandedWeaponHandler.register();
		ModBlockEntities.register();
		ModItems.registerModItems();
		ModScreenHandlers.register();
		ModAttributes.registerAttributes();
		ModSounds.registerSounds();
		ModEffects.registerEffects();
		ModParticles.register();
		PayloadTypeRegistry.playS2C().register(StatOptionsSyncPacket.ID, StatOptionsSyncPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(EnergyShieldSyncPacket.ID, EnergyShieldSyncPacket.CODEC);
		StatRegistry.registerStats();
		EnchantmentDisableManager.registerToggleable(Identifier.of("minecraft", "silk_touch"));
		EnchantmentDisableManager.registerToggleable(Identifier.of("cranks-dungeons", "vein_mining"));
		ApplyStatCommand.register();
		CheckStatsCommand.register();
		AilmentCommand.register();
		RuneCommand.register();
		PartyCommand.register();
		PartyHealthSyncHandler.register();
		PlayerInfoSyncHandler.register();
		EntityTrackingSyncHandler.register();
		FortuneEventHandler.register();
		LogFortuneHandler.register();
		DigFortuneHandler.register();
		CropXpEventHandler.register();
		GatheringXpHandler.register();
		RunicTomeLootHandler.register();
		PassiveTreeConfig.load();
		PassiveNodeRegistry.loadFromConfig();
		com.cranks.dungeons.config.WorldTreeConfig.load();
		com.cranks.dungeons.stat.passive.WorldNodeRegistry.loadFromConfig();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            net.minecraft.server.network.ServerPlayerEntity player = handler.player;
            PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);

            com.cranks.dungeons.network.ModNetworking.syncPassiveTreeToPlayer(player);
            com.cranks.dungeons.network.ModNetworking.syncWorldTreeToPlayer(player);
            
            // Re-apply max health and attributes
            player.setHealth(player.getMaxHealth());
			com.cranks.dungeons.component.PassiveTreePersistentState worldState = com.cranks.dungeons.component.PassiveTreePersistentState
					.get(server);
			if (data.getPlayerClass() == null || data.getPlayerClass().isEmpty()) {
				worldState.loadIntoComponent(handler.player.getUuid(), data);
			} else {
				// CCA loaded fine this time, make sure PersistentState is up to date
				worldState.savePlayerData(handler.player.getUuid(), data);
			}

			com.cranks.dungeons.component.WorldTreeDataComponent worldTreeData = ModComponents.WORLD_TREE_DATA.get(player);
			com.cranks.dungeons.component.WorldTreePersistentState wtState = com.cranks.dungeons.component.WorldTreePersistentState.get(server);
			
			// We check if gathering level is 1 and 0 xp to indicate an empty component that CCA failed to persist
			if (worldTreeData.getGatheringLevel() <= 1 && worldTreeData.getGatheringXp() == 0 && worldTreeData.getUnlockedNodes().isEmpty()) {
				wtState.loadIntoComponent(handler.player.getUuid(), worldTreeData);
			} else {
				wtState.savePlayerData(handler.player.getUuid(), worldTreeData);
			}

			if (!worldTreeData.hasNode("world_start")) {
				worldTreeData.unlockNode("world_start");
			}

			data.checkRetroactivePoints(handler.player.experienceLevel);

			// Sync energy shield to client slightly delayed on join to ensure attributes
			// and client are fully loaded
			com.cranks.dungeons.util.EquipmentChangeHandler.scheduleSync(handler.player, 100);

			// If retroactive checking changed anything, save it
			worldState.savePlayerData(handler.player.getUuid(), data);
			wtState.savePlayerData(handler.player.getUuid(), worldTreeData);

			ModNetworking.syncPassiveTreeToPlayer(handler.player);
			PassiveSkillApplier.updatePlayerPassives(handler.player);

			// Retroactive fix for existing players
			var reloadInst = handler.player.getAttributeInstance(ModAttributes.RELOAD_SPEED);
			if (reloadInst != null && reloadInst.getBaseValue() == 0.0)
				reloadInst.setBaseValue(1.0);
			var miningInst = handler.player.getAttributeInstance(ModAttributes.MINING_EFFICIENCY);
			if (miningInst != null && miningInst.getBaseValue() == 0.0)
				miningInst.setBaseValue(1.0);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PassiveSkillApplier.updatePlayerPassives(newPlayer);
		});
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes()
						.add(ModAttributes.CROP_FORTUNE, 0.0)
						.add(ModAttributes.LOG_FORTUNE, 0.0)
						.add(ModAttributes.DIG_FORTUNE, 0.0)
						.add(ModAttributes.FLYING_SPEED, 0.0)
						.add(ModAttributes.EVASION_RATING, 0.0)
						.add(ModAttributes.ENERGY_SHIELD, 0.0)
						.add(ModAttributes.ARMOR_CRIT_MITIGATION, 0.0)
						.add(ModAttributes.RELOAD_SPEED, 1.0)
						.add(ModAttributes.CROP_EXPERIENCE, 0.0)
						// Offensive
						.add(ModAttributes.BONUS_ATTACK_DAMAGE, 0.0)
						.add(ModAttributes.ATTACK_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.SWORD_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.AXE_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.MACE_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.SPEAR_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.BOW_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.CROSSBOW_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.TRIDENT_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.LEGGINGS_ARMOR_PERCENT, 0.0)
						.add(ModAttributes.FIRE_DAMAGE, 0.0)
						.add(ModAttributes.FIRE_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.COLD_DAMAGE, 0.0)
						.add(ModAttributes.COLD_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.LIGHTNING_DAMAGE, 0.0)
						.add(ModAttributes.LIGHTNING_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.VOID_DAMAGE, 0.0)
						.add(ModAttributes.VOID_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.DAMAGE_AGAINST_AFFLICTED_PERCENT, 0.0)
						.add(ModAttributes.DAMAGE_WHILE_AFFLICTED_PERCENT, 0.0)
						.add(ModAttributes.CRIT_CHANCE_AGAINST_AFFLICTED, 0.0)
						.add(ModAttributes.ELEMENTAL_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.MELEE_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.RANGED_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.ONE_HANDED_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.DUAL_WIELDING_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.TWO_HANDING_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.MAX_HEALTH_PERCENT, 0.0)
						.add(ModAttributes.ARMOR_PERCENT, 0.0)
						.add(ModAttributes.ENERGY_SHIELD_PERCENT, 0.0)
						.add(ModAttributes.EVASION_RATING_PERCENT, 0.0)
						.add(ModAttributes.ARMOR_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.FIRE_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.COLD_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.LIGHTNING_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.VOID_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.ELEMENTAL_PENETRATION_PERCENT, 0.0)
						.add(ModAttributes.CRIT_CHANCE, 0.05)
						.add(ModAttributes.CRIT_MULTIPLIER, 1.5)
						.add(ModAttributes.PHYSICAL_DAMAGE_LEECHED_AS_LIFE, 0.0)
						.add(ModAttributes.ATTACK_RANGE, 0.0)
						.add(ModAttributes.ATTACK_SPEED, 0.0)
						// Ailments
						.add(ModAttributes.CHANCE_TO_BURN, 0.0)
						.add(ModAttributes.CHANCE_TO_POISON, 0.0)
						.add(ModAttributes.CHANCE_TO_FREEZE, 0.0)
						.add(ModAttributes.CHANCE_TO_BLEED, 0.0)
						.add(ModAttributes.CHANCE_TO_SHOCK, 0.0)
						.add(ModAttributes.BURN_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.POISON_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.FREEZE_MAGNITUDE_PERCENT, 0.0)
						.add(ModAttributes.BLEED_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.SHOCK_MAGNITUDE_PERCENT, 0.0)
						.add(ModAttributes.SHOCK_DURATION, 3.0)
						.add(ModAttributes.ELECTROCUTE_MAGNITUDE_PERCENT, 0.0)
						.add(ModAttributes.ELECTROCUTE_DURATION, 0.0)
						.add(ModAttributes.SHOCK_THRESHOLD_REDUCTION, 0.0)
						.add(ModAttributes.FREEZE_THRESHOLD_REDUCTION, 0.0)
						.add(ModAttributes.ELECTROCUTE_THRESHOLD_REDUCTION, 0.0)
						.add(ModAttributes.BURN_DURATION, 0.0)
						.add(ModAttributes.POISON_DURATION, 0.0)
						.add(ModAttributes.POISON_MAX_STACKS, 0.0)
						.add(ModAttributes.FREEZE_DURATION, 0.0)
						.add(ModAttributes.BLEED_DURATION, 0.0)
						// Defensive
						.add(ModAttributes.FIRE_RESISTANCE, 0.0)
						.add(ModAttributes.COLD_RESISTANCE, 0.0)
						.add(ModAttributes.LIGHTNING_RESISTANCE, 0.0)
						.add(ModAttributes.VOID_RESISTANCE, 0.0)
						.add(ModAttributes.BONUS_ARMOR, 0.0)
						.add(ModAttributes.LIFE_REGENERATION, 0.0)
						.add(ModAttributes.MAX_HEALTH_BONUS, 0.0)
						.add(ModAttributes.FEATHER_FALLING, 0.0)
						// Extra
						.add(ModAttributes.PHYSICAL_EXTRA_PERCENT, 0.0)
						.add(ModAttributes.FIRE_EXTRA_PERCENT, 0.0)
						.add(ModAttributes.COLD_EXTRA_PERCENT, 0.0)
						.add(ModAttributes.LIGHTNING_EXTRA_PERCENT, 0.0)
						.add(ModAttributes.VOID_EXTRA_PERCENT, 0.0)
						.add(ModAttributes.PHYSICAL_AS_EXTRA_FIRE_PERCENT, 0.0)
						.add(ModAttributes.PHYSICAL_AS_EXTRA_COLD_PERCENT, 0.0)
						.add(ModAttributes.PHYSICAL_AS_EXTRA_LIGHTNING_PERCENT, 0.0)
						.add(ModAttributes.PHYSICAL_AS_EXTRA_VOID_PERCENT, 0.0)
						// Misc
						.add(ModAttributes.KNOCKBACK, 0.0)
						.add(ModAttributes.KNOCKBACK_RESISTANCE, 0.0)
						.add(ModAttributes.ITEM_RARITY_PERCENT, 0.0)
						.add(ModAttributes.MINING_EFFICIENCY, 1.0)
						.add(ModAttributes.FORTUNE, 0.0)
						.add(ModAttributes.DURABILITY_BONUS, 0.0)
						.add(ModAttributes.EXPERIENCE_BONUS, 0.0)
						.add(ModAttributes.PRECISION_MINING, 0.0)
						.add(ModAttributes.BREAKING_RANGE, 0.0)
						.add(ModAttributes.MOVEMENT_SPEED, 0.0));

		registerCreativeTabItems();

		net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, environment) -> {
					dispatcher.register(net.minecraft.server.command.CommandManager.literal("reloadpassivetree")
							.requires(source -> true) // Temporary while fixing permission check method
							.executes(context -> {
								com.cranks.dungeons.config.PassiveTreeConfig.load();
								com.cranks.dungeons.stat.passive.PassiveNodeRegistry.loadFromConfig();
								com.cranks.dungeons.config.WorldTreeConfig.load();
								com.cranks.dungeons.stat.passive.WorldNodeRegistry.loadFromConfig();
								com.cranks.dungeons.network.ModNetworking
										.syncPassiveTreeToAll(context.getSource().getServer());
								context.getSource().sendFeedback(
										() -> net.minecraft.text.Text
												.literal("Passive skill tree reloaded and synced to all players."),
										true);
								return 1;
							}));
				});

		LOGGER.info("Cranks Dungeons initialized!");
	}

	private void registerCreativeTabItems() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(ModBlocks.RUNIC_ENHANCEMENT_ALTAR);
			content.add(ModBlocks.ARTIFICIAL_ENHANCEMENT_ALTAR);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(ModItems.TOME_OF_TRANSMUTATION);
			content.add(ModItems.GREATER_TOME_OF_TRANSMUTATION);
			content.add(ModItems.PERFECT_TOME_OF_TRANSMUTATION);
			content.add(ModItems.TOME_OF_AUGMENTATION);
			content.add(ModItems.GREATER_TOME_OF_AUGMENTATION);
			content.add(ModItems.PERFECT_TOME_OF_AUGMENTATION);
			content.add(ModItems.REGAL_TOME);
			content.add(ModItems.GREATER_REGAL_TOME);
			content.add(ModItems.PERFECT_REGAL_TOME);
			content.add(ModItems.TOME_OF_ALCHEMY);
			content.add(ModItems.EXALTED_TOME);
			content.add(ModItems.GREATER_EXALTED_TOME);
			content.add(ModItems.PERFECT_EXALTED_TOME);
			// Elemental Tomes
			content.add(ModItems.TOME_OF_FIRE);
			content.add(ModItems.GREATER_TOME_OF_FIRE);

			content.add(ModItems.TOME_OF_COLD);
			content.add(ModItems.GREATER_TOME_OF_COLD);

			content.add(ModItems.TOME_OF_LIGHTNING);
			content.add(ModItems.GREATER_TOME_OF_LIGHTNING);

			content.add(ModItems.TOME_OF_VOID);
			content.add(ModItems.GREATER_TOME_OF_VOID);

			content.add(ModItems.TOME_OF_PHYSICALITY);
			content.add(ModItems.GREATER_TOME_OF_PHYSICALITY);

			// Utility Tomes
			content.add(ModItems.TOME_OF_SWIFTNESS);
			content.add(ModItems.TOME_OF_ENERGY);
			content.add(ModItems.TOME_OF_EVASION);
			content.add(ModItems.TOME_OF_HEALTH_BONUS);
			content.add(ModItems.ARTIFICIAL_TOME);
		});

		// Filter enchanted books in creative to only show allowed enchantments
		filterCreativeEnchantedBooks();
	}

	private void filterCreativeEnchantedBooks() {
		// Filter ALL item groups that might contain enchanted books
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(this::filterEnchantedBooks);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SEARCH).register(this::filterEnchantedBooks);
	}

	/**
	 * Enchantment IDs allowed through the creative tab filter.
	 * Any enchanted book not in this list is hidden from creative tabs.
	 */
	private static final List<String> ALLOWED_ENCHANTMENT_IDS = List.of(
			// Vanilla utility enchantments kept as-is
			"minecraft:aqua_affinity",
			"minecraft:channeling",
			"minecraft:infinity",
			"minecraft:multishot",
			"minecraft:silk_touch",
			"minecraft:frost_walker",
			"minecraft:loyalty",
			"minecraft:soul_speed",
			"minecraft:wind_burst",
			"minecraft:riptide",
			"minecraft:respiration",
			"minecraft:depth_strider",
			"minecraft:lunge",
			"cranks-dungeons:vein_mining",

			// New Hybrid Stat Enchantments
			"minecraft:body",
			"minecraft:greater_body",
			"minecraft:desert",
			"minecraft:greater_desert",
			"minecraft:glacial",
			"minecraft:greater_glacial",
			"minecraft:storm",
			"minecraft:greater_storm",
			"minecraft:iron",
			"minecraft:greater_iron",
			"minecraft:vision",
			"minecraft:greater_vision",
			"minecraft:alacrity",
			"minecraft:greater_alacrity",

			// Gathering & Utility
			"minecraft:sundering",
			"minecraft:greater_sundering",
			"minecraft:avarice",
			"minecraft:greater_avarice",
			"minecraft:preservation",
			"minecraft:greater_preservation",
			"minecraft:reach",
			"minecraft:greater_reach"
	);

	/** Cached set of allowed enchantment registry keys, built once on first use. */
	private Set<RegistryKey<Enchantment>> allowedEnchantmentKeys;

	private Set<RegistryKey<Enchantment>> getAllowedEnchantmentKeys() {
		if (allowedEnchantmentKeys == null) {
			allowedEnchantmentKeys = new HashSet<>();
			for (String id : ALLOWED_ENCHANTMENT_IDS) {
				Identifier identifier = Identifier.tryParse(id);
				if (identifier != null) {
					RegistryKey<net.minecraft.registry.Registry<Enchantment>> registryKey = RegistryKey
							.ofRegistry(Identifier.of("minecraft", "enchantment"));
					allowedEnchantmentKeys.add(RegistryKey.of(registryKey, identifier));
				}
			}
		}
		return allowedEnchantmentKeys;
	}

	private void filterEnchantedBooks(FabricItemGroupEntries content) {
		Set<RegistryKey<Enchantment>> allowed = getAllowedEnchantmentKeys();

		// Remove from both display stacks and search tab stacks
		var displayStacks = content.getDisplayStacks();
		var searchStacks = content.getSearchTabStacks();

		displayStacks.removeIf(stack -> shouldRemoveBook(stack, allowed));
		searchStacks.removeIf(stack -> shouldRemoveBook(stack, allowed));
	}

	private boolean shouldRemoveBook(net.minecraft.item.ItemStack stack,
			Set<RegistryKey<Enchantment>> allowedEnchantments) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) {
			return false;
		}
		var enchantments = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
		if (enchantments == null) {
			return false;
		}
		for (var enchantmentEntry : enchantments.getEnchantmentEntries()) {
			var key = enchantmentEntry.getKey().getKey().orElse(null);
			if (key == null || !allowedEnchantments.contains(key)) {
				return true; // Remove this book - has disallowed enchantment
			}
		}
		return false;
	}
}

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
import java.util.Set;

public class CranksDungeons implements ModInitializer {

	public static final String MOD_ID = "cranks-dungeons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModNetworking.registerPayloads();
		ModNetworking.registerServerReceivers();
		EnchantmentDisableTickHandler.register();
		EquipmentChangeHandler.register();
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
		FortuneEventHandler.register();
		LogFortuneHandler.register();
		DigFortuneHandler.register();
		CropXpEventHandler.register();
		RunicTomeLootHandler.register();
		FabricDefaultAttributeRegistry.register(
				EntityType.PLAYER,
				PlayerEntity.createPlayerAttributes()
						.add(ModAttributes.CROP_FORTUNE, 0.0)
						.add(ModAttributes.LOG_FORTUNE, 0.0)
						.add(ModAttributes.DIG_FORTUNE, 0.0)
						.add(ModAttributes.FLYING_SPEED)
						.add(ModAttributes.EVASION_RATING, 0.0)
						.add(ModAttributes.ENERGY_SHIELD, 0.0)
						.add(ModAttributes.RELOAD_SPEED, 0.0)
						.add(ModAttributes.CROP_EXPERIENCE, 0.0)
						.add(ModAttributes.CHANCE_TO_BURN, 0.0)
						.add(ModAttributes.BURN_BASE_DAMAGE, 3.0)
						.add(ModAttributes.BURN_DAMAGE_PERCENT, 100.0)
						.add(ModAttributes.CHANCE_TO_POISON, 0.0)
						.add(ModAttributes.POISON_BASE_DAMAGE, 1.0)
						.add(ModAttributes.POISON_DAMAGE_PERCENT, 0.0)
						.add(ModAttributes.CHANCE_TO_FREEZE, 0.0)
						.add(ModAttributes.CHANCE_TO_BLEED, 0.0)
						.add(ModAttributes.CHANCE_TO_SHOCK, 0.0)
						.add(ModAttributes.SHOCK_DURATION, 3.0)
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
			content.add(ModItems.TOME_OF_SWIFTNESS);
			content.add(ModItems.TOME_OF_ENERGY);
			content.add(ModItems.TOME_OF_EVASION);
			content.add(ModItems.GREATER_TOME_OF_FIRE);
			content.add(ModItems.GREATER_TOME_OF_COLD);
			content.add(ModItems.GREATER_TOME_OF_LIGHTNING);
			content.add(ModItems.GREATER_TOME_OF_VOID);
			content.add(ModItems.GREATER_TOME_OF_PHYSICALITY);
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

	private void filterEnchantedBooks(FabricItemGroupEntries content) {
		// Build set of allowed enchantment registry keys
		Set<RegistryKey<Enchantment>> allowedEnchantments = new HashSet<>();

		// Add all allowed enchantments from your json
		addAllowedEnchantment(allowedEnchantments, "minecraft:aqua_affinity");
		addAllowedEnchantment(allowedEnchantments, "minecraft:channeling");
		addAllowedEnchantment(allowedEnchantments, "minecraft:infinity");
		addAllowedEnchantment(allowedEnchantments, "minecraft:multishot");
		addAllowedEnchantment(allowedEnchantments, "minecraft:silk_touch");
		addAllowedEnchantment(allowedEnchantments, "minecraft:frost_walker");
		addAllowedEnchantment(allowedEnchantments, "minecraft:loyalty");
		addAllowedEnchantment(allowedEnchantments, "minecraft:soul_speed");
		addAllowedEnchantment(allowedEnchantments, "minecraft:wind_burst");
		addAllowedEnchantment(allowedEnchantments, "minecraft:riptide");
		addAllowedEnchantment(allowedEnchantments, "minecraft:respiration");
		addAllowedEnchantment(allowedEnchantments, "minecraft:depth_strider");
		addAllowedEnchantment(allowedEnchantments, "minecraft:lunge");
		addAllowedEnchantment(allowedEnchantments, "minecraft:fire_damage");
		addAllowedEnchantment(allowedEnchantments, "minecraft:cold_damage");
		addAllowedEnchantment(allowedEnchantments, "minecraft:lightning_damage");
		addAllowedEnchantment(allowedEnchantments, "minecraft:void_damage");
		addAllowedEnchantment(allowedEnchantments, "minecraft:attack_damage");
		addAllowedEnchantment(allowedEnchantments, "minecraft:attack_speed");
		addAllowedEnchantment(allowedEnchantments, "minecraft:crit_chance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:chance_to_burn");
		addAllowedEnchantment(allowedEnchantments, "minecraft:chance_to_poison");
		addAllowedEnchantment(allowedEnchantments, "minecraft:chance_to_freeze");
		addAllowedEnchantment(allowedEnchantments, "minecraft:chance_to_shock");
		addAllowedEnchantment(allowedEnchantments, "minecraft:life_steal");
		addAllowedEnchantment(allowedEnchantments, "minecraft:bonus_knockback");
		addAllowedEnchantment(allowedEnchantments, "minecraft:attack_range");
		addAllowedEnchantment(allowedEnchantments, "minecraft:max_health_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:life_regeneration");
		addAllowedEnchantment(allowedEnchantments, "minecraft:bonus_armor");
		addAllowedEnchantment(allowedEnchantments, "minecraft:bonus_armor_toughness");
		addAllowedEnchantment(allowedEnchantments, "minecraft:knockback_resistance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:fire_resistance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:cold_resistance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:lightning_resistance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:void_resistance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:experience_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:luck_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:movement_speed_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:feather_falling_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:mining_efficiency");
		addAllowedEnchantment(allowedEnchantments, "minecraft:ore_fortune");
		addAllowedEnchantment(allowedEnchantments, "minecraft:log_fortune");
		addAllowedEnchantment(allowedEnchantments, "minecraft:energy_shield");
		addAllowedEnchantment(allowedEnchantments, "minecraft:evasion_rating");
		addAllowedEnchantment(allowedEnchantments, "minecraft:durability_bonus");
		addAllowedEnchantment(allowedEnchantments, "minecraft:repair_chance");
		addAllowedEnchantment(allowedEnchantments, "minecraft:breaking_range");
		addAllowedEnchantment(allowedEnchantments, "minecraft:crop_fortune");
		addAllowedEnchantment(allowedEnchantments, "minecraft:dig_fortune");
		addAllowedEnchantment(allowedEnchantments, "minecraft:reload_speed");
		addAllowedEnchantment(allowedEnchantments, "cranks-dungeons:vein_mining");

		// Remove from both display stacks and search tab stacks
		var displayStacks = content.getDisplayStacks();
		var searchStacks = content.getSearchTabStacks();
		
		displayStacks.removeIf(stack -> shouldRemoveBook(stack, allowedEnchantments));
		searchStacks.removeIf(stack -> shouldRemoveBook(stack, allowedEnchantments));
	}

	private void addAllowedEnchantment(Set<RegistryKey<Enchantment>> set, String id) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier != null) {
			RegistryKey<net.minecraft.registry.Registry<Enchantment>> registryKey = RegistryKey.ofRegistry(Identifier.of("minecraft", "enchantment"));
			set.add(RegistryKey.of(registryKey, identifier));
		}
	}

	private boolean shouldRemoveBook(net.minecraft.item.ItemStack stack, Set<RegistryKey<Enchantment>> allowedEnchantments) {
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

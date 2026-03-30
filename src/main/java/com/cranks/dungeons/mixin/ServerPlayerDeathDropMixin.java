package com.cranks.dungeons.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to ensure player inventory drops properly on death in multiplayer.
 * This fixes the issue where items disappear instead of dropping when a player dies on a server.
 * 
 * The vanilla dropInventory() should be called during ServerPlayerEntity's onDeath,
 * but if something interferes (like exceptions from other mixins), items may not drop.
 * This mixin runs at TAIL to ensure drops happen even if something went wrong earlier.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerDeathDropMixin {

	/**
	 * Invoker for the protected dropInventory method in PlayerEntity
	 */
	@Mixin(PlayerEntity.class)
	public interface PlayerEntityDropInvoker {
		@Invoker("dropInventory")
		void invokeDropInventory(ServerWorld world);
	}

	/**
	 * Inject at TAIL of onDeath to ensure inventory drops.
	 * This runs after all vanilla death logic but before the player respawns.
	 */
	@Inject(method = "onDeath", at = @At("TAIL"))
	private void ensureInventoryDropsOnDeath(DamageSource source, CallbackInfo ci) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		
		try {
			// Get the server world
			if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
				return;
			}
			
			// Don't drop items for creative or spectator mode players
			if (player.isCreative() || player.isSpectator()) {
				return;
			}
			
			// Use the invoker to call the protected dropInventory method
			// This will only drop items if they haven't been dropped yet
			// (items are removed from inventory when dropped)
			((PlayerEntityDropInvoker) player).invokeDropInventory(serverWorld);
			
			System.out.println("[CranksDungeons] Ensured inventory drop for player: " + player.getName().getString());
		} catch (Exception e) {
			// Log error but don't crash - we want the player to at least respawn
			System.err.println("[CranksDungeons] Error ensuring inventory drop for player " + player.getName().getString() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
}

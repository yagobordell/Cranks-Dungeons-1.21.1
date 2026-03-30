package com.cranks.dungeons.mixin;

import com.cranks.dungeons.client.StatsPanelScrollHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to handle mouse scroll events for the stats panel in InventoryScreen.
 * This overrides the mouseScrolled method instead of injecting.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenScrollMixin extends Screen {

	protected InventoryScreenScrollMixin() {
		super(null);
	}

	/**
	 * Override mouseScrolled to handle stats panel scrolling before passing to super.
	 * In Minecraft 1.21.x, signature is: mouseScrolled(mouseX, mouseY, amount, verticalAmount)
	 * or mouseScrolled(mouseX, mouseY, verticalAmount)
	 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		// Check if stats panel is visible and handle scroll
		if (StatsPanelScrollHandler.isStatsVisible()) {
			boolean handled = StatsPanelScrollHandler.handleScroll((InventoryScreen) (Object) this, mouseX, mouseY, horizontalAmount, verticalAmount);
			if (handled) {
				return true;
			}
		}
		
		// Pass to parent class
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	/**
	 * Alternative signature for older/newer mappings that might use 3 parameters
	 */
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		return mouseScrolled(mouseX, mouseY, 0.0, verticalAmount);
	}
}

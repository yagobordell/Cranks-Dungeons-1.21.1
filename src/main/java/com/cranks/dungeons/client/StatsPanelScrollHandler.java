package com.cranks.dungeons.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * Handles scroll events for the stats panel in InventoryScreen.
 * This class provides a clean interface between the ScreenScrollMixin and the stats panel logic.
 */
public class StatsPanelScrollHandler {
	
	private static boolean statsVisible = false;
	private static int scrollOffset = 0;
	private static int maxScrollOffset = 0;
	
	/**
	 * Handles mouse scroll events for the stats panel.
	 * 
	 * @param screen The InventoryScreen instance
	 * @param mouseX Mouse X position
	 * @param mouseY Mouse Y position  
	 * @param horizontalAmount Horizontal scroll amount (may be 0 if not supported)
	 * @param verticalAmount Vertical scroll amount (usually from mouse wheel)
	 * @return true if the scroll event was handled by the stats panel
	 */
	public static boolean handleScroll(InventoryScreen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!statsVisible) {
			return false;
		}
		
		try {
			// Get screen dimensions using the HandledScreen accessor
			int x = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getX();
			int y = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getY();
			int backgroundWidth = ((com.cranks.dungeons.mixin.HandledScreenAccessor) screen).getBackgroundWidth();
			
			int panelX = x + backgroundWidth + 2;
			int panelY = y;
			int panelWidth = 147;
			int panelHeight = 166;
			
			// Check if mouse is within the stats panel bounds
			if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
				mouseY >= panelY && mouseY <= panelY + panelHeight) {
				
				// Only respond to vertical scrolling
				if (verticalAmount != 0) {
					// Update scroll offset (negative because scrolling down should show lower content)
					scrollOffset -= (int) (verticalAmount * 10);
					scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
					
					System.out.println("[StatsPanelScrollHandler] Scrolled: verticalAmount=" + verticalAmount 
						+ " scrollOffset=" + scrollOffset + " maxScrollOffset=" + maxScrollOffset);
				}
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Sets whether the stats panel is currently visible.
	 */
	public static void setStatsVisible(boolean visible) {
		statsVisible = visible;
		if (!visible) {
			scrollOffset = 0;
		}
	}
	
	/**
	 * Gets whether the stats panel is currently visible.
	 */
	public static boolean isStatsVisible() {
		return statsVisible;
	}
	
	/**
	 * Gets the current scroll offset.
	 */
	public static int getScrollOffset() {
		return scrollOffset;
	}
	
	/**
	 * Sets the current scroll offset.
	 */
	public static void setScrollOffset(int offset) {
		scrollOffset = Math.max(0, offset);
	}
	
	/**
	 * Gets the maximum scroll offset.
	 */
	public static int getMaxScrollOffset() {
		return maxScrollOffset;
	}
	
	/**
	 * Sets the maximum scroll offset.
	 */
	public static void setMaxScrollOffset(int maxOffset) {
		maxScrollOffset = Math.max(0, maxOffset);
		// Clamp current offset to new max
		scrollOffset = Math.min(scrollOffset, maxScrollOffset);
	}
}

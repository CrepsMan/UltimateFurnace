package com.crepsman.ultimate_furnace.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UltimateFurnaceFuelSlot extends Slot {

	public UltimateFurnaceFuelSlot(Container container, int index, int x, int y) {
		super(container, index, x, y);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 0;  // No fuel, so max count is zero
	}

	public boolean isHighlightable() {
		// Override this to prevent the highlight from showing on this slot
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;  // Prevent any item insertion
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;  // Prevent item removal
	}

	@Override
	public boolean isActive() {
		return false;  // Make it visibly disabled
	}
}

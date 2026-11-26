package com.crepsman.ultimate_furnace.util;

import com.crepsman.ultimate_furnace.registry.ModBlocks;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
	public static ItemStack createConfiguredFurnaceItem(int level, int smeltCount, int storedPower) {
		ItemStack stack = new ItemStack(ModBlocks.ULTIMATE_FURNACE);
		CompoundTag tag = new CompoundTag();
		tag.putInt("Level", level);
		tag.putInt("SmeltCount", smeltCount);
		tag.putInt("StoredPower", storedPower);
		//? if >=1.20.5 {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		//?} else {
		/*stack.setTag(tag);
		*///?}
		return stack;
	}
}

package com.crepsman.ultimate_furnace.screen;

import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.util.FurnaceConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
//? if >=1.21.2 {
import net.minecraft.world.item.crafting.RecipePropertySet;
//?}
import net.minecraft.world.item.crafting.RecipeType;

public class UltimateFurnaceScreenHandler extends AbstractFurnaceMenu {
	private final ContainerData customPropertyDelegate;
	private final Container container;

	//? if >=26.2 {
	public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  RecipeBookType category,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		super(type, RecipePropertySet.FURNACE_INPUT, category, syncId, playerInventory, container, propertyDelegate);
		this.container = container;
		this.customPropertyDelegate = propertyDelegate;
		this.slots.set(1, new UltimateFurnaceFuelSlot(this.container, 1, 56, 53));
	}

	public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		this(type, recipeType, RecipeBookType.FURNACE, syncId, playerInventory, container, propertyDelegate);
	}
	//?} else if >=1.21.2 {
	/*public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  RecipeBookType category,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		super(type, recipeType, RecipePropertySet.FURNACE_INPUT, category, syncId, playerInventory, container, propertyDelegate);
		this.container = container;
		this.customPropertyDelegate = propertyDelegate;
		this.slots.set(1, new UltimateFurnaceFuelSlot(this.container, 1, 56, 53));
	}

	public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		this(type, recipeType, RecipeBookType.FURNACE, syncId, playerInventory, container, propertyDelegate);
	}
	*///?} else {
	/*public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  RecipeBookType category,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		super(type, recipeType, category, syncId, playerInventory, container, propertyDelegate);
		this.container = container;
		this.customPropertyDelegate = propertyDelegate;
		this.slots.set(1, new UltimateFurnaceFuelSlot(this.container, 1, 56, 53));
	}

	public UltimateFurnaceScreenHandler(MenuType<?> type,
									  RecipeType<? extends AbstractCookingRecipe> recipeType,
									  int syncId,
									  Inventory playerInventory,
									  Container container,
									  ContainerData propertyDelegate) {
		this(type, recipeType, RecipeBookType.FURNACE, syncId, playerInventory, container, propertyDelegate);
	}
	*///?}

	public int getMaxSmeltCountForLevel() {
		int level = getLevel();
		int itemsPerLevel = FurnaceConfig.getItemsPerLevel();
		int maxLevel = FurnaceConfig.getMaxLevel();
		return level < maxLevel ? itemsPerLevel * level : itemsPerLevel * maxLevel;
	}

	public int getLevel() { return Math.max(1, customPropertyDelegate.get(1)); }
	public int getSmeltCount() { return customPropertyDelegate.get(0); }
	public int getBurnTime() { return customPropertyDelegate.get(2); }
	public int getCookTime() { return customPropertyDelegate.get(4); }
	public int getSunlightPercent() { return customPropertyDelegate.get(6); }

	public int getRawStoredPower() {
		int low = customPropertyDelegate.get(3) & 0xFFFF;
		int high = customPropertyDelegate.get(7) & 0xFFFF;
		return (high << 16) | low;
	}

	@Override
	public boolean isLit() {
		return getBurnTime() > 0;
	}

	@Override
	protected boolean isFuel(ItemStack itemStack) { return false; }

	public int getSmeltCountProgress() {
		int smeltCount = customPropertyDelegate.get(0);
		int level = getLevel();
		if (level >= FurnaceConfig.getMaxLevel()) {
			return 100;
		}
		int itemsPerLevel = FurnaceConfig.getItemsPerLevel();
		int maxSmeltCount = itemsPerLevel * level;
		return maxSmeltCount > 0 ? Math.min(100, Math.max(0, smeltCount * 100 / maxSmeltCount)) : 0;
	}

	public int getStoredPower() {
		int storedPower = getRawStoredPower();
		int maxPower = UltimateFurnaceBlockEntity.getMaxStoredPower(getLevel());
		return maxPower > 0 ? Math.min(100, (int) (storedPower * 100.0f / maxPower)) : 0;
	}

	public int getCookingProgress() {
		int cookTime = customPropertyDelegate.get(4);
		int cookTimeTotal = customPropertyDelegate.get(5);
		return cookTimeTotal != 0 ? (cookTime * 100) / cookTimeTotal : 0;
	}

	@Override
	public boolean stillValid(Player player) { return this.container.stillValid(player); }
}

package com.crepsman.ultimate_furnace.registry;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.screen.UltimateFurnaceScreenHandler;
import net.minecraft.world.SimpleContainer;
//? if >=1.19.3 {
import net.minecraft.world.flag.FeatureFlagSet;
//?}
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipeType;
//? if fabric {
import net.minecraft.core.Registry;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?}
//?}
//? if forge {
/*import net.minecraftforge.registries.RegisterEvent;
*///?}
//? if neoforge {
/*import net.neoforged.neoforge.registries.RegisterEvent;
*///?}

public class ModScreenHandlers {
	public static MenuType<UltimateFurnaceScreenHandler> ULTIMATE_FURNACE_SCREEN_HANDLER;

	private static MenuType<UltimateFurnaceScreenHandler> createScreenHandlerType() {
		//? if >=1.19.3 {
		return new MenuType<>(
			(syncId, playerInventory) -> new UltimateFurnaceScreenHandler(
				ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER,
				RecipeType.SMELTING,
				syncId,
				playerInventory,
				new SimpleContainer(3),
				new SimpleContainerData(8)
			),
			FeatureFlagSet.of()
		);
		//?} else {
		/*return new MenuType<>(
			(syncId, playerInventory) -> new UltimateFurnaceScreenHandler(
				ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER,
				RecipeType.SMELTING,
				syncId,
				playerInventory,
				new SimpleContainer(3),
				new SimpleContainerData(8)
			)
		);
		*///?}
	}

	//? if fabric {
	public static void registerModScreenHandlers() {
		ULTIMATE_FURNACE_SCREEN_HANDLER = Registry.register(
			//? if >=1.19.3 {
			BuiltInRegistries.MENU,
			//?} else {
			/*Registry.MENU,
			*///?}
			UltimateFurnaceMod.id("ultimate_furnace"),
			createScreenHandlerType()
		);
	}
	//?}

	//? if forgeLike {
	/*public static void registerScreenHandlers(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
		ULTIMATE_FURNACE_SCREEN_HANDLER = createScreenHandlerType();
		helper.register(UltimateFurnaceMod.id("ultimate_furnace"), ULTIMATE_FURNACE_SCREEN_HANDLER);
	}
	*///?}
}

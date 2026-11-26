package com.crepsman.ultimate_furnace.registry;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.blocks.CopperPlateBlock;
import com.crepsman.ultimate_furnace.blocks.UltimateFurnaceBlock;
import com.crepsman.ultimate_furnace.blocks.item.UltimateFurnaceBlockItem;
import com.crepsman.ultimate_furnace.util.ModProperties;
import net.minecraft.world.item.BlockItem;
//? if <1.19.3 {
/*import net.minecraft.world.item.CreativeModeTab;
*///?}
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
//? if <1.20 {
/*import net.minecraft.world.level.material.Material;
*///?}
//? if >=1.21.2 {
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
//?}
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

import java.util.function.Function;

public class ModBlocks {
	public static Block ULTIMATE_FURNACE;
	public static Block COPPER_PLATE;

	private static BlockBehaviour.Properties createUltimateFurnaceSettings() {
		//? if >=1.20 {
		return BlockBehaviour.Properties.of()
		//?} else {
		/*return BlockBehaviour.Properties.of(Material.STONE)
		*///?}
			.requiresCorrectToolForDrops()
			.strength(3.5F)
			.lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0);
	}

	private static BlockBehaviour.Properties createCopperPlateSettings() {
		//? if >=26.2 {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected())
		//?} else if >=1.20.3 {
		/*return BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
		*///?} else {
		/*return BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
		*///?}
			.lightLevel(state -> state.getValue(ModProperties.HOT) ? 12 : 0);
	}

	private static <T extends Block> T createBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties base) {
		//? if >=1.21.2 {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, UltimateFurnaceMod.id(name));
		return factory.apply(base.setId(key));
		//?} else {
		/*return factory.apply(base);
		*///?}
	}

	private static <T extends Item> T createItem(String name, Function<Item.Properties, T> factory) {
		//? if >=1.21.2 {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, UltimateFurnaceMod.id(name));
		return factory.apply(new Item.Properties().setId(key));
		//?} else if >=1.19.3 {
		/*return factory.apply(new Item.Properties());
		*///?} else {
		/*return factory.apply(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
		*///?}
	}

	//? if fabric {
	public static void registerModBlocks() {
		ULTIMATE_FURNACE = registerBlockWithItem("ultimate_furnace", UltimateFurnaceBlock::new, createUltimateFurnaceSettings());
		COPPER_PLATE = registerBlockWithItem("copper_plate", CopperPlateBlock::new, createCopperPlateSettings());
	}

	public static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
		return Registry.register(
			//? if >=1.19.3 {
			BuiltInRegistries.ITEM,
			//?} else {
			/*Registry.ITEM,
			*///?}
			UltimateFurnaceMod.id(name),
			createItem(name, factory)
		);
	}

	public static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties base) {
		return Registry.register(
			//? if >=1.19.3 {
			BuiltInRegistries.BLOCK,
			//?} else {
			/*Registry.BLOCK,
			*///?}
			UltimateFurnaceMod.id(name),
			createBlock(name, factory, base)
		);
	}

	public static <T extends Block> T registerBlockWithItem(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties base) {
		T block = registerBlock(name, factory, base);
		if ("ultimate_furnace".equals(name)) {
			registerItem(name, settings -> new UltimateFurnaceBlockItem(block, settings));
		} else {
			registerItem(name, settings -> new BlockItem(block, settings));
		}
		return block;
	}
	//?}

	//? if forgeLike {
	/*public static void registerBlocks(RegisterEvent.RegisterHelper<Block> helper) {
		ULTIMATE_FURNACE = createBlock("ultimate_furnace", UltimateFurnaceBlock::new, createUltimateFurnaceSettings());
		COPPER_PLATE = createBlock("copper_plate", CopperPlateBlock::new, createCopperPlateSettings());
		helper.register(UltimateFurnaceMod.id("ultimate_furnace"), ULTIMATE_FURNACE);
		helper.register(UltimateFurnaceMod.id("copper_plate"), COPPER_PLATE);
	}

	public static void registerItems(RegisterEvent.RegisterHelper<Item> helper) {
		helper.register(
			UltimateFurnaceMod.id("ultimate_furnace"),
			createItem("ultimate_furnace", settings -> new UltimateFurnaceBlockItem(ULTIMATE_FURNACE, settings))
		);
		helper.register(
			UltimateFurnaceMod.id("copper_plate"),
			createItem("copper_plate", settings -> new BlockItem(COPPER_PLATE, settings))
		);
	}
	*///?}
}

package com.crepsman.ultimate_furnace.registry;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
//? if fabric {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?}
//?}
//? if forge {
/*import net.minecraftforge.registries.RegisterEvent;
//? if >=1.21.2 {
import java.util.Set;
//?}
*///?}
//? if neoforge {
/*import net.neoforged.neoforge.registries.RegisterEvent;
//? if >=1.21.2 {
import java.util.Set;
//?}
*///?}

public class ModBlockEntities {
	public static BlockEntityType<UltimateFurnaceBlockEntity> ULTIMATE_FURNACE_BLOCK_ENTITY;

	//? if fabric {
	public static void registerModBlockEntities() {
		ULTIMATE_FURNACE_BLOCK_ENTITY = Registry.register(
			//? if >=1.19.3 {
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			//?} else {
			/*Registry.BLOCK_ENTITY_TYPE,
			*///?}
			UltimateFurnaceMod.id("ultimate_furnace"),
			FabricBlockEntityTypeBuilder.create(UltimateFurnaceBlockEntity::new, ModBlocks.ULTIMATE_FURNACE).build()
		);
	}
	//?}

	//? if forgeLike {
	/*public static void registerBlockEntities(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
		//? if >=1.21.2 {
		ULTIMATE_FURNACE_BLOCK_ENTITY = new BlockEntityType<>(
			UltimateFurnaceBlockEntity::new,
			Set.of(ModBlocks.ULTIMATE_FURNACE)
		);
		//?} else {
		/^ULTIMATE_FURNACE_BLOCK_ENTITY = BlockEntityType.Builder.of(
			UltimateFurnaceBlockEntity::new,
			ModBlocks.ULTIMATE_FURNACE
		).build(null);
		^///?}
		helper.register(UltimateFurnaceMod.id("ultimate_furnace"), ULTIMATE_FURNACE_BLOCK_ENTITY);
	}
	*///?}
}

package com.crepsman.ultimate_furnace;

import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.registry.ModBlockEntities;
import com.crepsman.ultimate_furnace.registry.ModBlocks;
import com.crepsman.ultimate_furnace.registry.ModScreenHandlers;
import com.crepsman.ultimate_furnace.util.FurnaceConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
//? if >=1.21.11 {
import net.minecraft.commands.Commands;
//?}
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionCheck;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.server.level.ServerPlayer;
//? if >=1.19.3 {
import net.minecraft.world.item.CreativeModeTabs;
//?}
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//? if >=26.1 {
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
//?} else if >=1.19.3 {
/*import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
*///?}
import net.fabricmc.loader.api.FabricLoader;
//?}
//? if forgeLike && >=1.21.5 {
/*import com.crepsman.ultimate_furnace.gametest.UltimateFurnaceGameTests;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.function.Consumer;
*///?}
//? if (forgeLike && <1.21.5) {
/*import com.crepsman.ultimate_furnace.gametest.UltimateFurnaceForgeLikeGameTests;
*///?}
//? if forge {
/*//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
//?} else {
/^import net.minecraft.core.Registry;
^///?}
import net.minecraftforge.event.RegisterCommandsEvent;
//? if >=1.21.5 {
import net.minecraftforge.gametest.ForgeGameTestHooks;
//?} else {
/^import net.minecraftforge.event.RegisterGameTestsEvent;
^///?}
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.bus.BusGroup;
//?} else {
/^import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
^///?}
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
*///?}
//? if neoforge {
/*import net.minecraft.core.registries.Registries;
//? if >=1.21.5 {
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.ResourceKey;
//? if >=26.3 {
import net.minecraft.world.level.Level;
//?}
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHooks;
//?}
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
*///?}

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

//? if forgeLike {
/*@Mod(UltimateFurnaceMod.MOD_ID)
public class UltimateFurnaceMod {
*///?}
//? if fabric {
public class UltimateFurnaceMod implements ModInitializer {
//?}
	public static final Logger LOGGER = LoggerFactory.getLogger("Ultimate Furnace");
	public static final String MOD_ID = "ultimate_furnace";

	//? if >=1.21.11 {
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	//?} else if >=1.21 {
	/*public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	*///?} else {
	/*public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
	*///?}

	//? if fabric {
	@Override
	public void onInitialize() {
		logStartup();
		FurnaceConfig.load();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerModBlockEntities();
		ModScreenHandlers.registerModScreenHandlers();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
		//? if >=26.1 {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(UltimateFurnaceMod::addItemsToFunctionalItemGroup);
		//?} else if >=1.19.3 {
		/*ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(UltimateFurnaceMod::addItemsToFunctionalItemGroup);
		*///?}
	}

	//? if >=26.1 {
	private static void addItemsToFunctionalItemGroup(FabricCreativeModeTabOutput entries) {
		entries.accept(ModBlocks.ULTIMATE_FURNACE);
		entries.accept(ModBlocks.COPPER_PLATE);
	}
	//?} else if >=1.19.3 {
	/*private static void addItemsToFunctionalItemGroup(FabricItemGroupEntries entries) {
		entries.accept(ModBlocks.ULTIMATE_FURNACE);
		entries.accept(ModBlocks.COPPER_PLATE);
	}
	*///?}
	//?}

	//? if forge {
	/*//? if >=1.21.6 {
	public UltimateFurnaceMod(FMLJavaModLoadingContext context) {
		logStartup();
		FurnaceConfig.load();

		BusGroup modBusGroup = context.getModBusGroup();
		RegisterEvent.getBus(modBusGroup).addListener(UltimateFurnaceMod::onRegister);
		//? if >=1.21.9 {
		BuildCreativeModeTabContentsEvent.BUS.addListener(UltimateFurnaceMod::onBuildCreativeTabContents);
		//?} else {
		/^BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(UltimateFurnaceMod::onBuildCreativeTabContents);
		^///?}
		RegisterCommandsEvent.BUS.addListener(UltimateFurnaceMod::onRegisterCommands);
	}
	//?} else {
	/^//? if >=1.21.1 {
	public UltimateFurnaceMod(FMLJavaModLoadingContext context) {
		logStartup();
		FurnaceConfig.load();

		IEventBus modEventBus = context.getModEventBus();
	//?} else {
	/^public UltimateFurnaceMod() {
		logStartup();
		FurnaceConfig.load();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	^///?}
		modEventBus.addListener(UltimateFurnaceMod::onRegister);
		//? if <1.21.5 {
		/^modEventBus.addListener(UltimateFurnaceMod::onRegisterGameTests);
		^///?}
		//? if >=1.19.3 {
		modEventBus.addListener(UltimateFurnaceMod::onBuildCreativeTabContents);
		//?}
		MinecraftForge.EVENT_BUS.addListener(UltimateFurnaceMod::onRegisterCommands);
	}
	^///?}
	*///?}

	//? if neoforge {
	/*public UltimateFurnaceMod(IEventBus modEventBus) {
		logStartup();
		FurnaceConfig.load();

		modEventBus.addListener(UltimateFurnaceMod::onRegister);
		modEventBus.addListener(UltimateFurnaceMod::onRegisterGameTests);
		modEventBus.addListener(UltimateFurnaceMod::onBuildCreativeTabContents);
		NeoForge.EVENT_BUS.addListener(UltimateFurnaceMod::onRegisterCommands);

		//? if >=1.21.9 {
		boolean isClient = FMLEnvironment.getDist() == Dist.CLIENT;
		//?} else {
		/^boolean isClient = FMLEnvironment.dist == Dist.CLIENT;
		^///?}
		if (isClient) {
			UltimateFurnaceModClient.init(modEventBus);
		}
	}
	*///?}

	//? if forgeLike {
	/*private static void onRegister(RegisterEvent event) {
		//? if >=1.19.3 {
		event.register(Registries.BLOCK, ModBlocks::registerBlocks);
		event.register(Registries.ITEM, ModBlocks::registerItems);
		event.register(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::registerBlockEntities);
		event.register(Registries.MENU, ModScreenHandlers::registerScreenHandlers);
		//? if >=1.21.5 {
		//? if forge {
		/^if (ForgeGameTestHooks.isGametestEnabled()) {
			event.register(Registries.TEST_FUNCTION, UltimateFurnaceMod::registerGameTestFunctions);
		}
		^///?} else if neoforge {
		/^if (GameTestHooks.isGametestEnabled()) {
			event.register(Registries.TEST_FUNCTION, UltimateFurnaceMod::registerGameTestFunctions);
		}
		^///?}
		//?}
		//?} else {
		/^event.register(Registry.BLOCK_REGISTRY, ModBlocks::registerBlocks);
		event.register(Registry.ITEM_REGISTRY, ModBlocks::registerItems);
		event.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, ModBlockEntities::registerBlockEntities);
		event.register(Registry.MENU_REGISTRY, ModScreenHandlers::registerScreenHandlers);
		^///?}
	}

	//? if >=1.21.5 {
	private static void registerGameTestFunctions(RegisterEvent.RegisterHelper<Consumer<GameTestHelper>> helper) {
		helper.register(id("sunlight_and_obstruction"), UltimateFurnaceGameTests::sunlightAndObstruction);
		helper.register(id("battery_and_lit_state"), UltimateFurnaceGameTests::batteryAndLitState);
		helper.register(id("item_persistence"), UltimateFurnaceGameTests::itemPersistence);
		helper.register(id("copper_plate_thermal_and_world_effects"), UltimateFurnaceGameTests::copperPlateThermalAndWorldEffects);
	}
	//?}

	//? if <1.21.5 || neoforge {
	/^private static void onRegisterGameTests(RegisterGameTestsEvent event) {
		//? if neoforge && >=1.21.5 {
		/^var defaultEnv = event.registerEnvironment(id("default"));
		String[] names = {
			"sunlight_and_obstruction",
			"battery_and_lit_state",
			"item_persistence",
			"copper_plate_thermal_and_world_effects"
		};
		int[] maxTicks = {100, 700, 100, 200};
		var emptyStructure = id("empty");
		for (int i = 0; i < names.length; i++) {
			var testId = id(names[i]);
			//? if >=26.3 {
			var testData = new TestData<>(defaultEnv, Level.OVERWORLD, emptyStructure, maxTicks[i], 0, true, Rotation.NONE, false, 1, 1, true, 0);
			//?} else if >=26.1 {
			/^var testData = new TestData<>(defaultEnv, emptyStructure, maxTicks[i], 0, true, Rotation.NONE, false, 1, 1, true, 0);
			^///?} else {
			/^var testData = new TestData<>(defaultEnv, emptyStructure, maxTicks[i], 0, true, Rotation.NONE, false, 1, 1, true);
			^///?}
			event.registerTest(
				testId,
				data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, testId), data),
				testData
			);
		}
		^///?} else {
		event.register(UltimateFurnaceForgeLikeGameTests.class);
		//?}
	}
	^///?}

	//? if >=1.19.3 {
	private static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			//? if neoforge {
			/^event.accept(ModBlocks.ULTIMATE_FURNACE);
			event.accept(ModBlocks.COPPER_PLATE);
			^///?} else {
			event.accept(() -> ModBlocks.ULTIMATE_FURNACE);
			event.accept(() -> ModBlocks.COPPER_PLATE);
			//?}
		}
	}
	//?}

	private static void onRegisterCommands(RegisterCommandsEvent event) {
		registerCommands(event.getDispatcher());
	}
	*///?}

	private static void logStartup() {
		String modVersion = getModVersion();
		//? if >=1.21.6 {
		String mcVersion = SharedConstants.getCurrentVersion().name();
		//?} else {
		/*String mcVersion = SharedConstants.getCurrentVersion().getName();
		*///?}
		LOGGER.info("Initializing Ultimate Furnace v{} on {} (Minecraft {})", modVersion, getLoaderName(), mcVersion);
	}

	public static String getModVersion() {
		//? if fabric {
		return FabricLoader.getInstance()
			.getModContainer(MOD_ID)
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("unknown");
		//?}
		//? if forge {
		/*//? if >=26.1 {
		return ModList.getModContainerById(MOD_ID)
			.map(container -> container.getModInfo().getVersion().toString())
			.orElse("unknown");
		//?} else {
		/^return ModList.get()
			.getModContainerById(MOD_ID)
			.map(container -> container.getModInfo().getVersion().toString())
			.orElse("unknown");
		^///?}
		*///?}
		//? if neoforge {
		/*return ModList.get()
			.getModContainerById(MOD_ID)
			.map(container -> container.getModInfo().getVersion().toString())
			.orElse("unknown");
		*///?}
	}

	public static String getLoaderName() {
		//? if fabric {
		return "Fabric";
		//?}
		//? if forge {
		/*return "Forge";
		*///?}
		//? if neoforge {
		/*return "NeoForge";
		*///?}
	}

	private static void sendFeedback(CommandSourceStack source, Component message, boolean broadcastToOps) {
		//? if >=1.20 {
		source.sendSuccess(() -> message, broadcastToOps);
		//?} else {
		/*source.sendSuccess(message, broadcastToOps);
		*///?}
	}

	private static BlockPos getBlockPos(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		//? if >=1.19.3 {
		return BlockPosArgument.getBlockPos(context, name);
		//?} else {
		/*return BlockPosArgument.getLoadedBlockPos(context, name);
		*///?}
	}

	//? if >=1.21.11 {
	private static boolean hasPermissionLevel(CommandSourceStack source, int level) {
		PermissionCheck check = switch (level) {
			case 1 -> Commands.LEVEL_MODERATORS;
			case 2 -> Commands.LEVEL_GAMEMASTERS;
			case 3 -> Commands.LEVEL_ADMINS;
			case 4 -> Commands.LEVEL_OWNERS;
			default -> Commands.LEVEL_ALL;
		};
		return check.check(source.permissions());
	}
	//?}

	private static int executeInfoLookTarget(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.translatable("commands.ultimate_furnace.info.no_target"));
			return 0;
		}
		Vec3 start = player.getEyePosition();
		Vec3 end = start.add(player.getViewVector(1.0F).scale(8.0));
		BlockHitResult hit = source.getLevel().clip(
			new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)
		);
		if (hit.getType() != HitResult.Type.BLOCK) {
			source.sendFailure(Component.translatable("commands.ultimate_furnace.info.no_target"));
			return 0;
		}
		return executeInfo(source, hit.getBlockPos());
	}

	private static int executeInfo(CommandSourceStack source, BlockPos pos) {
		Block block = source.getLevel().getBlockState(pos).getBlock();
		if (block == ModBlocks.ULTIMATE_FURNACE) {
			UltimateFurnaceBlockEntity entity = source.getLevel().getBlockEntity(pos, ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY).orElse(null);
			if (entity != null) {
				sendFeedback(source, Component.translatable(
					"commands.ultimate_furnace.info",
					entity.getFurnaceLevel(), entity.getSmeltCount(), entity.getStoredPower(),
					UltimateFurnaceBlockEntity.getMaxStoredPower(entity.getFurnaceLevel()),
					FurnaceConfig.getItemsPerLevel(),
					FurnaceConfig.getCookTimeForLevel(entity.getFurnaceLevel()),
					FurnaceConfig.getPowerGainRateForLevel(entity.getFurnaceLevel()),
					FurnaceConfig.getPowerUseRateForLevel(entity.getFurnaceLevel()),
					entity.getSunlightPercent()
				), false);
				return 1;
			}
		}
		source.sendFailure(Component.translatable("commands.ultimate_furnace.fail"));
		return 0;
	}

	private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		// /ultimatefurnace reload
		var reload = literal("reload")
			//? if >=1.21.11 {
			.requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
			//?} else {
			/*.requires(src -> src.hasPermission(4))
			*///?}
			.executes(ctx -> {
				FurnaceConfig.reload();
				sendFeedback(ctx.getSource(), Component.translatable("commands.ultimate_furnace.reload.success"), true);
				return 1;
			});

		// /ultimatefurnace info [pos]
		var info = literal("info")
			//? if >=1.21.11 {
			.requires(src -> hasPermissionLevel(src, FurnaceConfig.getInfoPermissionLevel()))
			//?} else {
			/*.requires(src -> src.hasPermission(FurnaceConfig.getInfoPermissionLevel()))
			*///?}
			.executes(context -> executeInfoLookTarget(context.getSource()))
			.then(argument("position", BlockPosArgument.blockPos())
				.executes(context -> executeInfo(context.getSource(), getBlockPos(context, "position"))));

		// /ultimatefurnace set <sub>
		var set = literal("set")
			//? if >=1.21.11 {
			.requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
			//?} else {
			/*.requires(src -> src.hasPermission(4))
			*///?}
			.then(literal("level")
				.then(argument("level", IntegerArgumentType.integer(1, FurnaceConfig.getMaxLevel()))
					.then(argument("position", BlockPosArgument.blockPos())
						.executes(context -> {
							int level = IntegerArgumentType.getInteger(context, "level");
							BlockPos pos = getBlockPos(context, "position");
							Block block = context.getSource().getLevel().getBlockState(pos).getBlock();
							if (block == ModBlocks.ULTIMATE_FURNACE) {
								UltimateFurnaceBlockEntity entity = context.getSource().getLevel().getBlockEntity(pos, ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY).orElse(null);
								if (entity != null) {
									entity.setLevel(level);
									sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.set.level", entity.getFurnaceLevel()), true);
								} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							return 1;
						}))
				))
			.then(literal("smeltcount")
				.then(argument("smeltcount", IntegerArgumentType.integer(0, 15000))
					.then(argument("position", BlockPosArgument.blockPos())
						.executes(context -> {
							int count = IntegerArgumentType.getInteger(context, "smeltcount");
							BlockPos pos = getBlockPos(context, "position");
							Block block = context.getSource().getLevel().getBlockState(pos).getBlock();
							if (block == ModBlocks.ULTIMATE_FURNACE) {
								UltimateFurnaceBlockEntity entity = context.getSource().getLevel().getBlockEntity(pos, ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY).orElse(null);
								if (entity != null) {
									entity.setSmeltCount(count);
									sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.set.count", entity.getSmeltCount()), true);
								} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							return 1;
						}))
				))
			.then(literal("storepower")
				.then(argument("storedPower", IntegerArgumentType.integer(0, 1000000))
					.then(argument("position", BlockPosArgument.blockPos())
						.executes(context -> {
							int power = IntegerArgumentType.getInteger(context, "storedPower");
							BlockPos pos = getBlockPos(context, "position");
							Block block = context.getSource().getLevel().getBlockState(pos).getBlock();
							if (block == ModBlocks.ULTIMATE_FURNACE) {
								UltimateFurnaceBlockEntity entity = context.getSource().getLevel().getBlockEntity(pos, ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY).orElse(null);
								if (entity != null) {
									entity.setStoredPower(power);
									sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.set.power", entity.getStoredPower()), true);
								} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							} else sendFeedback(context.getSource(), Component.translatable("commands.ultimate_furnace.fail"), false);
							return 1;
						}))
				));

		dispatcher.register(literal("ultimatefurnace")
			.then(reload)
			.then(info)
			.then(set)
		);
	}
}

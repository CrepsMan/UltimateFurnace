package com.crepsman.ultimate_furnace.gametest;

import com.crepsman.ultimate_furnace.blocks.CopperPlateBlock;
import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.registry.ModBlocks;
import com.crepsman.ultimate_furnace.screen.UltimateFurnaceScreenHandler;
//? if <1.20 {
/*import com.mojang.authlib.GameProfile;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
//? if >=1.20.5 {
import net.minecraft.world.level.GameType;
//?}
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Shared loader-agnostic GameTest bodies executed across Fabric, Forge, and NeoForge.
 */
public final class UltimateFurnaceGameTests {
	private UltimateFurnaceGameTests() {}

	public static void sunlightAndObstruction(GameTestHelper helper) {
		setClearDay(helper);

		BlockPos furnacePos = new BlockPos(2, 2, 2);
		helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
		helper.setBlock(furnacePos, ModBlocks.ULTIMATE_FURNACE);
		ensureOpenSkyColumn(helper.getLevel(), helper.absolutePos(furnacePos));

		BlockState persistentLeaf = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

		helper.startSequence()
			.thenIdle(5)
			.thenExecute(() -> {
				ServerLevel level = helper.getLevel();
				BlockPos absPos = helper.absolutePos(furnacePos);

				// Open sky = 100%
				assertEquals(helper, 100, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 100% sunlight under open sky");

				// Glass above = 100%
				helper.setBlock(new BlockPos(2, 4, 2), Blocks.GLASS);
				assertEquals(helper, 100, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 100% sunlight under glass");

				// 1-4 stacked leaves = 75% / 50% / 25% / 0%
				helper.setBlock(new BlockPos(2, 4, 2), persistentLeaf);
				assertEquals(helper, 75, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 75% sunlight under 1 leaf");

				helper.setBlock(new BlockPos(2, 5, 2), persistentLeaf);
				assertEquals(helper, 50, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 50% sunlight under 2 leaves");

				helper.setBlock(new BlockPos(2, 6, 2), persistentLeaf);
				assertEquals(helper, 25, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 25% sunlight under 3 leaves");

				helper.setBlock(new BlockPos(2, 7, 2), persistentLeaf);
				assertEquals(helper, 0, UltimateFurnaceBlockEntity.getSunlightPercent(level, absPos),
					"Expected 0% sunlight under 4 leaves");

				// Clear leaves, place solid stone roof + surrounding torches = 0%
				helper.setBlock(new BlockPos(2, 5, 2), Blocks.AIR);
				helper.setBlock(new BlockPos(2, 6, 2), Blocks.AIR);
				helper.setBlock(new BlockPos(2, 7, 2), Blocks.AIR);
				helper.setBlock(new BlockPos(2, 4, 2), Blocks.STONE);
				helper.setBlock(new BlockPos(1, 1, 2), Blocks.STONE);
				helper.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);
				helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
				helper.setBlock(new BlockPos(2, 1, 3), Blocks.STONE);
				helper.setBlock(new BlockPos(1, 2, 2), Blocks.TORCH);
				helper.setBlock(new BlockPos(3, 2, 2), Blocks.TORCH);
				helper.setBlock(new BlockPos(2, 2, 1), Blocks.TORCH);
				helper.setBlock(new BlockPos(2, 2, 3), Blocks.TORCH);
			})
			.thenIdle(2)
			.thenExecute(() -> {
				int obstructed = UltimateFurnaceBlockEntity.getSunlightPercent(helper.getLevel(), helper.absolutePos(furnacePos));
				assertEquals(helper, 0, obstructed, "Expected 0% sunlight under solid block even with torches");
			})
			.thenSucceed();
	}

	public static void batteryAndLitState(GameTestHelper helper) {
		setClearDay(helper);

		// Level 1 furnace (no battery storage)
		BlockPos lv1Pos = new BlockPos(2, 2, 2);
		helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
		helper.setBlock(lv1Pos, ModBlocks.ULTIMATE_FURNACE);
		ensureOpenSkyColumn(helper.getLevel(), helper.absolutePos(lv1Pos));

		// Level 2 furnace (has storage)
		BlockPos lv2Pos = new BlockPos(6, 2, 2);
		helper.setBlock(new BlockPos(6, 1, 2), Blocks.STONE);
		helper.setBlock(lv2Pos, ModBlocks.ULTIMATE_FURNACE);
		ensureOpenSkyColumn(helper.getLevel(), helper.absolutePos(lv2Pos));

		final int[] powerBeforeDarkIdle = new int[1];
		final UltimateFurnaceScreenHandler[] menus = new UltimateFurnaceScreenHandler[2];

		helper.startSequence()
			.thenIdle(5)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity lv1 = requireFurnace(helper, lv1Pos);
				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				lv2.setLevel(2);
				Inventory inv = createSurvivalPlayer(helper).getInventory();
				menus[0] = (UltimateFurnaceScreenHandler) lv1.createMenu(0, inv);
				menus[1] = (UltimateFurnaceScreenHandler) lv2.createMenu(1, inv);
			})
			// Charge Lv2 in open sunlight for 180 ticks (~360 stored power at +2/tick; Lv2 cookTime = 300 ticks, use = 1/tick)
			.thenIdle(180)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity lv1 = requireFurnace(helper, lv1Pos);
				assertEquals(helper, 0, lv1.getStoredPower(), "Level 1 furnace must have 0 stored power");
				assertFalse(helper, helper.getBlockState(lv1Pos).getValue(AbstractFurnaceBlock.LIT),
					"Idle Level 1 furnace must not be LIT");
				assertFalse(helper, menus[0].isLit(), "Idle Level 1 GUI flame must not be lit");

				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				assertTrue(helper, lv2.getStoredPower() >= 330,
					"Level 2 furnace should charge in sunlight (got " + lv2.getStoredPower() + ")");
				assertFalse(helper, helper.getBlockState(lv2Pos).getValue(AbstractFurnaceBlock.LIT),
					"Idle Level 2 furnace must not be LIT while charging");
				assertFalse(helper, menus[1].isLit(), "Idle Level 2 GUI flame must not be lit while charging");

				// Cover Lv2 with stone to simulate darkness / underground
				powerBeforeDarkIdle[0] = lv2.getStoredPower();
				helper.setBlock(new BlockPos(6, 4, 2), Blocks.STONE);
			})
			// Verify 0 idle drain in darkness for 20 ticks
			.thenIdle(20)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity lv1 = requireFurnace(helper, lv1Pos);
				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				assertEquals(helper, powerBeforeDarkIdle[0], lv2.getStoredPower(),
					"Idle furnace in darkness must have 0 power drain");
				assertFalse(helper, helper.getBlockState(lv2Pos).getValue(AbstractFurnaceBlock.LIT),
					"Idle furnace in darkness must not be LIT");
				assertFalse(helper, menus[1].isLit(), "Idle furnace in darkness must not light GUI flame");

				// Start smelting 1 cobblestone in Lv1 (sunlit) and Lv2 (stored power) while smeltCount == 0
				lv1.setItem(0, new ItemStack(Items.COBBLESTONE, 1));
				lv2.setItem(0, new ItemStack(Items.COBBLESTONE, 1));
			})
			.thenIdle(5)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity lv1 = requireFurnace(helper, lv1Pos);
				assertEquals(helper, 0, lv1.getSmeltCount(), "First smelt should still be in progress on Lv1");
				assertTrue(helper, menus[0].isLit(), "GUI flame must be lit during the first active smelt");

				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				assertEquals(helper, 0, lv2.getSmeltCount(), "First smelt should still be in progress on Lv2");
				assertTrue(helper, helper.getBlockState(lv2Pos).getValue(AbstractFurnaceBlock.LIT),
					"Furnace must be LIT while actively smelting in darkness");
				assertTrue(helper, menus[1].isLit(),
					"GUI flame must be lit while actively smelting first item in darkness");
				assertTrue(helper, lv2.getStoredPower() < powerBeforeDarkIdle[0],
					"Furnace must drain stored power when smelting in darkness");

				// Block sunlight over Lv1 mid-smelt and confirm immediate shutoff
				helper.setBlock(new BlockPos(2, 4, 2), Blocks.STONE);
			})
			.thenIdle(1)
			.thenExecute(() -> {
				assertFalse(helper, helper.getBlockState(lv1Pos).getValue(AbstractFurnaceBlock.LIT),
					"Furnace LIT must turn off immediately when sunlight is blocked mid-smelt");
				assertFalse(helper, menus[0].isLit(),
					"GUI flame must turn off immediately when sunlight is blocked mid-smelt");
			})
			.thenWaitUntil(() -> {
				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				ItemStack output = lv2.getItem(2);
				assertTrue(helper, !output.isEmpty() && output.is(Items.STONE),
					"Waiting for cobblestone smelt to complete");
			})
			.thenIdle(1)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity lv2 = requireFurnace(helper, lv2Pos);
				assertEquals(helper, 1, lv2.getSmeltCount(), "Completed smelt should increment smeltCount to 1");
				assertFalse(helper, helper.getBlockState(lv2Pos).getValue(AbstractFurnaceBlock.LIT),
					"Furnace LIT must return to false after smelting finishes");
				assertFalse(helper, menus[1].isLit(),
					"GUI flame must return to false immediately after smelting finishes");
			})
			.thenSucceed();
	}

	public static void itemPersistence(GameTestHelper helper) {
		BlockPos origPos = new BlockPos(3, 2, 3);
		BlockPos supportPos = new BlockPos(7, 1, 3);
		BlockPos placedPos = new BlockPos(7, 2, 3);

		helper.setBlock(new BlockPos(3, 1, 3), Blocks.STONE);
		helper.setBlock(supportPos, Blocks.STONE);
		helper.setBlock(origPos, ModBlocks.ULTIMATE_FURNACE);

		helper.startSequence()
			.thenIdle(1)
			.thenExecute(() -> {
				UltimateFurnaceBlockEntity furnace = requireFurnace(helper, origPos);
				furnace.setLevel(3);
				furnace.setSmeltCount(42);
				furnace.setStoredPower(1234);

				Player survivalPlayer = createSurvivalPlayer(helper);
				BlockPos absOrig = helper.absolutePos(origPos);
				BlockState state = helper.getLevel().getBlockState(absOrig);
				ModBlocks.ULTIMATE_FURNACE.playerWillDestroy(helper.getLevel(), absOrig, state, survivalPlayer);

				AABB searchBox = new AABB(absOrig).inflate(3.0);
				List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, searchBox);
				ItemStack droppedStack = ItemStack.EMPTY;
				for (ItemEntity drop : drops) {
					if (drop.getItem().is(ModBlocks.ULTIMATE_FURNACE.asItem())) {
						droppedStack = drop.getItem().copy();
						break;
					}
				}
				assertTrue(helper, !droppedStack.isEmpty(), "Breaking furnace as survival player must drop furnace item");

				// Place dropped ItemStack back onto stone support block
				survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, droppedStack);
				BlockPos absSupport = helper.absolutePos(supportPos);
				BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absSupport), Direction.UP, absSupport, false);
				droppedStack.useOn(new UseOnContext(survivalPlayer, InteractionHand.MAIN_HAND, hit));

				UltimateFurnaceBlockEntity restored = requireFurnace(helper, placedPos);
				assertEquals(helper, 3, restored.getFurnaceLevel(), "Persisted level mismatch");
				assertEquals(helper, 42, restored.getSmeltCount(), "Persisted smeltCount mismatch");
				assertEquals(helper, 1234, restored.getStoredPower(), "Persisted storedPower mismatch");
			})
			.thenSucceed();
	}

	public static void copperPlateThermalAndWorldEffects(GameTestHelper helper) {
		// Subtest A: Redstone does not trigger copper plate
		BlockPos redstonePlatePos = new BlockPos(1, 2, 1);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
		helper.setBlock(redstonePlatePos, ModBlocks.COPPER_PLATE);
		helper.setBlock(new BlockPos(2, 2, 1), Blocks.REDSTONE_BLOCK);

		// Subtest B: 6-sided conduction chain (4 -> 3 -> 2 -> 1 -> 0) and comparator output (15/12/8/4/0)
		BlockPos[] chainPos = new BlockPos[]{
			new BlockPos(1, 2, 4),
			new BlockPos(2, 2, 4),
			new BlockPos(3, 2, 4),
			new BlockPos(4, 2, 4),
			new BlockPos(5, 2, 4)
		};
		BlockPos comparatorPos = new BlockPos(1, 2, 5);
		helper.setBlock(comparatorPos.below(), Blocks.STONE);
		helper.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.NORTH));
		for (BlockPos pos : chainPos) {
			helper.setBlock(pos.below(), Blocks.STONE);
			helper.setBlock(pos, ModBlocks.COPPER_PLATE);
		}
		// Place magma last so plate #1 updates and notifies comparator + chain neighbors
		helper.setBlock(chainPos[0].below(), Blocks.MAGMA_BLOCK);

		// Subtest C: Warm + cold opposite sources neutralize to 0
		BlockPos neutralPlatePos = new BlockPos(8, 2, 1);
		helper.setBlock(new BlockPos(8, 1, 1), Blocks.MAGMA_BLOCK);
		helper.setBlock(neutralPlatePos, ModBlocks.COPPER_PLATE);
		helper.setBlock(new BlockPos(9, 2, 1), Blocks.BLUE_ICE);

		// Subtest D: Hot plate evaporates enclosed water and melts enclosed diagonal ice
		BlockPos hotPlatePos = new BlockPos(2, 2, 8);
		BlockPos evapWaterPos = new BlockPos(3, 2, 9);
		BlockPos meltIcePos = new BlockPos(1, 2, 9);
		buildStoneBasin(helper, evapWaterPos);
		buildStoneBasin(helper, meltIcePos);
		helper.setBlock(hotPlatePos.below(), Blocks.MAGMA_BLOCK);
		helper.setBlock(hotPlatePos, ModBlocks.COPPER_PLATE);
		helper.setBlock(evapWaterPos, Blocks.WATER);
		helper.setBlock(meltIcePos, Blocks.ICE);

		// Subtest E: Cold plate freezes enclosed water into Frosted Ice that thaws when cold plate is removed
		BlockPos coldPlatePos = new BlockPos(9, 2, 8);
		BlockPos freezeWaterPos = new BlockPos(10, 2, 9);
		buildStoneBasin(helper, freezeWaterPos);
		helper.setBlock(coldPlatePos.below(), Blocks.BLUE_ICE);
		helper.setBlock(coldPlatePos, ModBlocks.COPPER_PLATE);
		helper.setBlock(freezeWaterPos, Blocks.WATER);

		helper.startSequence()
			.thenIdle(4)
			.thenExecute(() -> {
				// Redstone check
				BlockState redstonePlate = helper.getBlockState(redstonePlatePos);
				assertFalse(helper, redstonePlate.getValue(CopperPlateBlock.HOT), "Redstone must not make plate HOT");
				assertFalse(helper, redstonePlate.getValue(CopperPlateBlock.COLD), "Redstone must not make plate COLD");
				assertEquals(helper, 0, redstonePlate.getValue(CopperPlateBlock.THERMAL_STRENGTH),
					"Redstone must leave THERMAL_STRENGTH at 0");
				assertEquals(helper, 0, getComparatorSignal(helper, redstonePlatePos),
					"Unpowered plate comparator output must be 0");

				// Chain 4 -> 3 -> 2 -> 1 -> 0 and comparator 15/12/8/4/0 check
				int[] expectedStrength = {4, 3, 2, 1, 0};
				int[] expectedSignal = {15, 12, 8, 4, 0};
				for (int i = 0; i < chainPos.length; i++) {
					BlockState pState = helper.getBlockState(chainPos[i]);
					assertEquals(helper, expectedStrength[i], pState.getValue(CopperPlateBlock.THERMAL_STRENGTH),
						"Plate #" + (i + 1) + " thermal strength mismatch");
					assertEquals(helper, expectedSignal[i], getComparatorSignal(helper, chainPos[i]),
						"Plate #" + (i + 1) + " comparator output mismatch");
					if (i < 4) {
						assertTrue(helper, pState.getValue(CopperPlateBlock.HOT), "Plate #" + (i + 1) + " should be HOT");
					} else {
						assertFalse(helper, pState.getValue(CopperPlateBlock.HOT), "Plate #5 should not be HOT");
					}
				}
				assertTrue(helper, helper.getBlockState(comparatorPos).getValue(ComparatorBlock.POWERED),
					"Live comparator next to strength-4 plate must be POWERED");

				// Neutralization check
				BlockState neutralPlate = helper.getBlockState(neutralPlatePos);
				assertFalse(helper, neutralPlate.getValue(CopperPlateBlock.HOT), "Neutralized plate must not be HOT");
				assertFalse(helper, neutralPlate.getValue(CopperPlateBlock.COLD), "Neutralized plate must not be COLD");
				assertEquals(helper, 0, neutralPlate.getValue(CopperPlateBlock.THERMAL_STRENGTH),
					"Neutralized plate strength must be 0");
			})
			// Wait for cold plate freeze tick (at tick 5) and hot plate melt/evap ticks (tick 5, 25, 45)
			.thenWaitUntil(() -> {
				assertTrue(helper, helper.getBlockState(freezeWaterPos).is(Blocks.FROSTED_ICE),
					"Waiting for cold plate to freeze water into frosted ice");
				assertTrue(helper, helper.getBlockState(evapWaterPos).isAir(),
					"Waiting for hot plate to evaporate water");
				assertTrue(helper, helper.getBlockState(meltIcePos).isAir(),
					"Waiting for hot plate to melt ice and evaporate resulting water");
			})
			.thenExecute(() -> {
				// Remove cold plate and verify frosted ice thaws back into water
				helper.setBlock(coldPlatePos, Blocks.AIR);
			})
			.thenIdle(1)
			.thenExecute(() -> {
				assertTrue(helper, helper.getBlockState(freezeWaterPos).is(Blocks.WATER),
					"Frosted ice must thaw back to water after cold plate is removed");
			})
			.thenSucceed();
	}

	private static void ensureOpenSkyColumn(ServerLevel level, BlockPos absPos) {
		//? if >=1.21.2 {
		int maxY = level.getMaxY();
		//?} else {
		/*int maxY = level.getMaxBuildHeight() - 1;
		*///?}
		int topY = Math.min(maxY, level.getHeight(Heightmap.Types.WORLD_SURFACE, absPos.getX(), absPos.getZ()));
		int sealStartY = absPos.getY() + 9;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = topY; y > absPos.getY(); y--) {
			cursor.set(absPos.getX(), y, absPos.getZ());
			BlockState existing = level.getBlockState(cursor);
			if (y >= sealStartY) {
				if (!existing.is(Blocks.GLASS)) {
					level.setBlock(cursor, Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
					level.getChunkSource().getLightEngine().checkBlock(cursor);
				}
			} else if (!existing.isAir()) {
				level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				level.getChunkSource().getLightEngine().checkBlock(cursor);
			}
		}
	}

	private static void buildStoneBasin(GameTestHelper helper, BlockPos centerPos) {
		helper.setBlock(centerPos.below(), Blocks.STONE);
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			helper.setBlock(centerPos.relative(dir), Blocks.STONE);
		}
	}

	private static int getComparatorSignal(GameTestHelper helper, BlockPos relPos) {
		BlockPos absPos = helper.absolutePos(relPos);
		BlockState state = helper.getLevel().getBlockState(absPos);
		CopperPlateBlock plate = (CopperPlateBlock) ModBlocks.COPPER_PLATE;
		//? if >=1.21.9 {
		return plate.getAnalogOutputSignal(state, helper.getLevel(), absPos, Direction.NORTH);
		//?} else {
		/*return plate.getAnalogOutputSignal(state, helper.getLevel(), absPos);
		*///?}
	}

	private static UltimateFurnaceBlockEntity requireFurnace(GameTestHelper helper, BlockPos relPos) {
		BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(relPos));
		if (be instanceof UltimateFurnaceBlockEntity furnace) {
			return furnace;
		}
		fail(helper, "Expected UltimateFurnaceBlockEntity at " + relPos);
		return null;
	}

	private static Player createSurvivalPlayer(GameTestHelper helper) {
		//? if >=1.20.5 {
		return helper.makeMockPlayer(GameType.SURVIVAL);
		//?} else if >=1.20 {
		/*return helper.makeMockSurvivalPlayer();
		*///?} else {
		/*return new Player(helper.getLevel(), helper.absolutePos(BlockPos.ZERO), 0.0F, new GameProfile(UUID.randomUUID(), "test-survival-player"), null) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return false;
			}
		};
		*///?}
	}

	private static void setClearDay(GameTestHelper helper) {
		//? if >=26.1 {
		helper.setTime(1000L);
		helper.getLevel().resetWeatherCycle();
		//?} else {
		/*helper.getLevel().setDayTime(1000L);
		helper.getLevel().setWeatherParameters(6000, 0, false, false);
		*///?}
	}

	private static void fail(GameTestHelper helper, String message) {
		//? if >=1.21.5 {
		helper.fail(Component.literal(message));
		//?} else {
		/*helper.fail(message);
		*///?}
	}

	private static void assertTrue(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			fail(helper, message);
		}
	}

	private static void assertFalse(GameTestHelper helper, boolean condition, String message) {
		if (condition) {
			fail(helper, message);
		}
	}

	private static void assertEquals(GameTestHelper helper, int expected, int actual, String message) {
		if (expected != actual) {
			fail(helper, message + " (expected=" + expected + ", actual=" + actual + ")");
		}
	}
}

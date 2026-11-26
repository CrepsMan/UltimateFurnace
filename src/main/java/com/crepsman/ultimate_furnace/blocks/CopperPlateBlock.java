package com.crepsman.ultimate_furnace.blocks;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.logic.ThermalLogic;
import com.crepsman.ultimate_furnace.util.ModProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
//? if >=1.19.3 {
import net.minecraft.core.registries.Registries;
//?} else {
/*import net.minecraft.core.Registry;
*///?}
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
//? if >=1.21 {
import net.minecraft.tags.EnchantmentTags;
//?}
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
//? if <1.19.4 {
/*import net.minecraft.world.damagesource.DamageSource;
*///?}
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
//? if >=1.21.11 {
import net.minecraft.world.level.gamerules.GameRules;
//?} else {
/*import net.minecraft.world.level.GameRules;
*///?}
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.2 {
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
//?} else {
/*import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
*///?}
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
//? if >=1.20 {
import net.minecraft.world.level.block.IceBlock;
//?}
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CopperPlateBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty HOT = ModProperties.HOT;
	public static final BooleanProperty COLD = ModProperties.COLD;
	public static final IntegerProperty THERMAL_STRENGTH = ModProperties.THERMAL_STRENGTH;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0);
	private static final int PERIODIC_TICK_INTERVAL = 20;
	private static final int MAX_CHANGES_PER_TICK = 2;

	//? if >=1.19.3 {
	public static final TagKey<Block> WARM_BLOCK_TAG = TagKey.create(Registries.BLOCK, UltimateFurnaceMod.id("warm_block"));
	public static final TagKey<Block> COLD_BLOCK_TAG = TagKey.create(Registries.BLOCK, UltimateFurnaceMod.id("cold_block"));
	//?} else {
	/*public static final TagKey<Block> WARM_BLOCK_TAG = TagKey.create(Registry.BLOCK_REGISTRY, UltimateFurnaceMod.id("warm_block"));
	public static final TagKey<Block> COLD_BLOCK_TAG = TagKey.create(Registry.BLOCK_REGISTRY, UltimateFurnaceMod.id("cold_block"));
	*///?}

	public CopperPlateBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(HOT, false)
				.setValue(COLD, false)
				.setValue(THERMAL_STRENGTH, 0)
				.setValue(WATERLOGGED, false)
		);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (state.getValue(HOT)) {
			if (!entity.isSteppingCarefully() && entity instanceof LivingEntity living && !entity.fireImmune() && !living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				//? if >=1.21 {
				boolean frostWalker = EnchantmentHelper.hasTag(living.getItemBySlot(EquipmentSlot.FEET), EnchantmentTags.PREVENTS_ICE_MELTING);
				//?} else {
				/*boolean frostWalker = EnchantmentHelper.hasFrostWalker(living);
				*///?}
				if (!frostWalker) {
					//? if >=1.19.4 {
					entity.hurt(level.damageSources().hotFloor(), 1.5F);
					//?} else {
					/*entity.hurt(DamageSource.HOT_FLOOR, 1.5F);
					*///?}
					if (!level.isClientSide() && entity.getRemainingFireTicks() < 25) {
						entity.setRemainingFireTicks(25);
					}
				}
			}
		} else if (state.getValue(COLD)) {
			if (entity instanceof LivingEntity && !PowderSnowBlock.canEntityWalkOnPowderSnow(entity)) {
				entity.setIsInPowderSnow(true);
			}
		}
		super.stepOn(level, pos, state, entity);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	//? if >=1.21.9 {
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
	//?} else {
	/*public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
	*///?}
		return ThermalLogic.getComparatorOutput(
			state.getValue(HOT),
			state.getValue(COLD),
			state.getValue(THERMAL_STRENGTH)
		);
	}

	@Override
	//? if >=1.21.2 {
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
		super.neighborChanged(state, level, pos, sourceBlock, wireOrientation, notify);
	//?} else {
	/*public void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos fromPos, boolean notify) {
		super.neighborChanged(state, level, pos, sourceBlock, fromPos, notify);
	*///?}
		if (!level.isClientSide()) {
			BlockState updated = updateThermalState(level, pos, state);
			if (updated.getValue(HOT) || updated.getValue(COLD)) {
				for (Direction dir : Direction.values()) {
					FluidState adjacentFluid = level.getBlockState(pos.relative(dir)).getFluidState();
					if (adjacentFluid.getType() == Fluids.WATER || adjacentFluid.getType() == Fluids.FLOWING_WATER || adjacentFluid.getType() == Fluids.LAVA || adjacentFluid.getType() == Fluids.FLOWING_LAVA) {
						level.scheduleTick(pos, this, 5);
						break;
					}
				}
			}
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
		super.onPlace(state, level, pos, oldState, notify);
		if (!level.isClientSide()) {
			BlockState updated = updateThermalState(level, pos, state);
			if (updated.getValue(HOT) || updated.getValue(COLD)) {
				level.scheduleTick(pos, this, 5);
			}
		}
	}

	//? if >=1.21.5 {
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (state.getValue(COLD)) {
			thawFrostedIceAround(level, pos);
		}
	}
	//?} else {
	/*@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onRemove(state, level, pos, newState, isMoving);
		if (!state.is(newState.getBlock()) && state.getValue(COLD) && level instanceof ServerLevel serverLevel) {
			thawFrostedIceAround(serverLevel, pos);
		}
	}
	*///?}

	private BlockState computeThermalState(LevelReader level, BlockPos pos, BlockState baseState) {
		List<ThermalLogic.NeighborState> neighbors = new ArrayList<>(6);
		for (Direction dir : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(dir));
			boolean warmSource = neighbor.is(WARM_BLOCK_TAG);
			boolean coldSource = neighbor.is(COLD_BLOCK_TAG);
			boolean isPlate = neighbor.getBlock() instanceof CopperPlateBlock;
			neighbors.add(new ThermalLogic.NeighborState(
				warmSource,
				coldSource,
				isPlate,
				isPlate && neighbor.getValue(HOT),
				isPlate && neighbor.getValue(COLD),
				isPlate ? neighbor.getValue(THERMAL_STRENGTH) : 0
			));
		}

		ThermalLogic.ThermalResult result = ThermalLogic.computeThermalState(neighbors);
		return baseState
			.setValue(HOT, result.hot)
			.setValue(COLD, result.cold)
			.setValue(THERMAL_STRENGTH, result.strength);
	}

	private BlockState updateThermalState(Level level, BlockPos pos, BlockState currentState) {
		BlockState targetState = computeThermalState(level, pos, currentState);
		if (targetState == currentState) {
			return currentState;
		}
		boolean wasCold = currentState.getValue(COLD);
		boolean nowCold = targetState.getValue(COLD);
		boolean wasActive = currentState.getValue(HOT) || wasCold;
		boolean nowActive = targetState.getValue(HOT) || nowCold;

		level.setBlock(pos, targetState, 3);
		if (wasCold && !nowCold && level instanceof ServerLevel serverLevel) {
			thawFrostedIceAround(serverLevel, pos);
		}
		if (!wasActive && nowActive) {
			level.scheduleTick(pos, this, 5);
		}
		return targetState;
	}

	private static void thawFrostedIceAround(ServerLevel level, BlockPos center) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos targetPos = center.offset(dx, dy, dz);
					BlockState targetState = level.getBlockState(targetPos);
					if (targetState.is(Blocks.FROSTED_ICE) && !isProtectedByColdPlate(level, targetPos)) {
						//? if >=1.20 {
						level.setBlock(targetPos, IceBlock.meltsInto(), 3);
						//?} else {
						/*level.setBlock(targetPos, Blocks.WATER.defaultBlockState(), 3);
						*///?}
					}
				}
			}
		}
	}

	private static boolean isProtectedByColdPlate(Level level, BlockPos icePos) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockState state = level.getBlockState(icePos.offset(dx, dy, dz));
					if (state.getBlock() instanceof CopperPlateBlock && state.getValue(COLD)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState base = this.defaultBlockState()
			.setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
		return computeThermalState(ctx.getLevel(), ctx.getClickedPos(), base);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockState current = updateThermalState(level, pos, state);
		if (!current.is(this)) {
			return;
		}
		boolean isHot = current.getValue(HOT);
		boolean isCold = current.getValue(COLD);
		if (!isHot && !isCold) {
			return;
		}

		level.scheduleTick(pos, this, PERIODIC_TICK_INTERVAL);
		int changesLeft = MAX_CHANGES_PER_TICK;

		if (isHot) {
			if (current.getValue(WATERLOGGED)) {
				level.setBlock(pos, current.setValue(WATERLOGGED, false), 3);
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 2.4F + (random.nextFloat() - random.nextFloat()) * 0.6F);
				spawnEvapParticles(level, pos, random);
				changesLeft--;
			}

			for (int dx = -1; dx <= 1 && changesLeft > 0; dx++) {
				for (int dy = -1; dy <= 1 && changesLeft > 0; dy++) {
					for (int dz = -1; dz <= 1 && changesLeft > 0; dz++) {
						if (dx == 0 && dy == 0 && dz == 0) continue;
						BlockPos targetPos = pos.offset(dx, dy, dz);
						BlockState targetState = level.getBlockState(targetPos);
						FluidState fluidState = targetState.getFluidState();

						if (targetState.is(Blocks.SNOW)) {
							level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
							spawnEvapParticles(level, targetPos, random);
							changesLeft--;
						} else if (targetState.is(Blocks.FROSTED_ICE) || targetState.is(Blocks.ICE) || targetState.is(Blocks.SNOW_BLOCK) || targetState.is(Blocks.POWDER_SNOW)) {
							level.setBlock(targetPos, Blocks.WATER.defaultBlockState(), 3);
							spawnEvapParticles(level, targetPos, random);
							changesLeft--;
						} else if (targetState.is(Blocks.PACKED_ICE)) {
							level.setBlock(targetPos, Blocks.ICE.defaultBlockState(), 3);
							spawnEvapParticles(level, targetPos, random);
							changesLeft--;
						} else if ((fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER)
							&& (!targetState.hasProperty(WATERLOGGED) || !targetState.getValue(WATERLOGGED))) {
							level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
							level.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
							spawnEvapParticles(level, targetPos, random);
							changesLeft--;
						} else if (targetState.hasProperty(WATERLOGGED) && targetState.getValue(WATERLOGGED)) {
							level.setBlock(targetPos, targetState.setValue(WATERLOGGED, false), 3);
							level.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
							spawnEvapParticles(level, targetPos, random);
							changesLeft--;
						}
					}
				}
			}

			// Slow fire ignition around hot plates, respecting doFireTick
			if (changesLeft > 0 && isFireSpreadEnabled(level) && random.nextInt(8) == 0) {
				tryIgniteNearbyFlammable(level, pos, random);
			}
		} else {
			// COLD: keep existing Frosted Ice refreshed so it doesn't melt while the cold plate stays active
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						BlockPos targetPos = pos.offset(dx, dy, dz);
						BlockState targetState = level.getBlockState(targetPos);
						if (targetState.is(Blocks.FROSTED_ICE) && targetState.getValue(FrostedIceBlock.AGE) > 0) {
							level.setBlock(targetPos, targetState.setValue(FrostedIceBlock.AGE, 0), 2);
						}
					}
				}
			}

			for (int dx = -1; dx <= 1 && changesLeft > 0; dx++) {
				for (int dy = -1; dy <= 1 && changesLeft > 0; dy++) {
					for (int dz = -1; dz <= 1 && changesLeft > 0; dz++) {
						if (dx == 0 && dy == 0 && dz == 0) continue;
						BlockPos targetPos = pos.offset(dx, dy, dz);
						BlockState targetState = level.getBlockState(targetPos);
						FluidState fluidState = targetState.getFluidState();

						if (targetState.is(Blocks.FIRE) || targetState.is(Blocks.SOUL_FIRE)) {
							level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
							level.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 2.2F);
							changesLeft--;
						} else if (fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA) {
							BlockState cooled = fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
							level.setBlock(targetPos, cooled, 3);
							level.playSound(null, targetPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 2.4F + (random.nextFloat() - random.nextFloat()) * 0.6F);
							level.sendParticles(ParticleTypes.LARGE_SMOKE, targetPos.getX() + 0.5, targetPos.getY() + 0.8, targetPos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.01);
							changesLeft--;
						} else if (fluidState.getType() == Fluids.WATER && fluidState.isSource() && !targetState.hasProperty(WATERLOGGED)) {
							level.setBlock(targetPos, Blocks.FROSTED_ICE.defaultBlockState(), 3);
							for (int i = 0; i < 4; i++) {
								double x = targetPos.getX() + random.nextDouble();
								double y = targetPos.getY() + 0.9;
								double z = targetPos.getZ() + random.nextDouble();
								level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 1, 0.0, 0.05, 0.0, 0.01);
							}
							changesLeft--;
						}
					}
				}
			}
		}
	}

	private static boolean isFireSpreadEnabled(ServerLevel level) {
		//? if >=1.21.11 {
		return level.getGameRules().get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) > 0;
		//?} else {
		/*return level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK);
		*///?}
	}

	private static boolean isFlammable(BlockState state) {
		//? if >=1.20 {
		return state.ignitedByLava();
		//?} else {
		/*return state.getMaterial().isFlammable();
		*///?}
	}

	private static void tryIgniteNearbyFlammable(ServerLevel level, BlockPos center, RandomSource random) {
		int startOffset = random.nextInt(27);
		for (int i = 0; i < 27; i++) {
			int idx = (startOffset + i) % 27;
			int dx = (idx % 3) - 1;
			int dy = ((idx / 3) % 3) - 1;
			int dz = (idx / 9) - 1;
			if (dx == 0 && dy == 0 && dz == 0) continue;

			BlockPos firePos = center.offset(dx, dy, dz);
			if (!level.isEmptyBlock(firePos)) continue;

			boolean hasFlammableNeighbor = false;
			for (Direction dir : Direction.values()) {
				if (isFlammable(level.getBlockState(firePos.relative(dir)))) {
					hasFlammableNeighbor = true;
					break;
				}
			}
			if (!hasFlammableNeighbor) continue;

			BlockState fireState = BaseFireBlock.getState(level, firePos);
			if (fireState.canSurvive(level, firePos)) {
				level.setBlock(firePos, fireState, 11);
				return;
			}
		}
	}

	private void spawnEvapParticles(ServerLevel level, BlockPos pos, RandomSource random) {
		for (int i = 0; i < 5; i++) {
			double x = pos.getX() + random.nextDouble();
			double y = pos.getY() + 0.5;
			double z = pos.getZ() + random.nextDouble();
			level.sendParticles(ParticleTypes.SMOKE, x, y, z, 2, 0.0, 0.08, 0.0, 0.01);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return BASE_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HOT, COLD, THERMAL_STRENGTH, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	//? if >=1.21.2 {
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (neighborState.getFluidState().getType() == Fluids.WATER ||
			neighborState.getFluidState().getType() == Fluids.FLOWING_WATER ||
			neighborState.getFluidState().getType() == Fluids.LAVA ||
			neighborState.getFluidState().getType() == Fluids.FLOWING_LAVA ||
			(neighborState.hasProperty(WATERLOGGED) && neighborState.getValue(WATERLOGGED)) ||
			neighborState.is(COLD_BLOCK_TAG) ||
			neighborState.is(WARM_BLOCK_TAG) ||
			neighborState.getBlock() instanceof CopperPlateBlock) {
			tickView.scheduleTick(pos, this, 3);
		}
		return super.updateShape(state, level, tickView, pos, direction, neighborPos, neighborState, random);
	}
	//?} else {
	/*public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (neighborState.getFluidState().getType() == Fluids.WATER ||
			neighborState.getFluidState().getType() == Fluids.FLOWING_WATER ||
			neighborState.getFluidState().getType() == Fluids.LAVA ||
			neighborState.getFluidState().getType() == Fluids.FLOWING_LAVA ||
			(neighborState.hasProperty(WATERLOGGED) && neighborState.getValue(WATERLOGGED)) ||
			neighborState.is(COLD_BLOCK_TAG) ||
			neighborState.is(WARM_BLOCK_TAG) ||
			neighborState.getBlock() instanceof CopperPlateBlock) {
			level.scheduleTick(pos, this, 3);
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}
	*///?}
}

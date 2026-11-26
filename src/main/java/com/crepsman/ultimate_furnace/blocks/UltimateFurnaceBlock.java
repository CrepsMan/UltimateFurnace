package com.crepsman.ultimate_furnace.blocks;

import com.crepsman.ultimate_furnace.blocks.entity.UltimateFurnaceBlockEntity;
import com.crepsman.ultimate_furnace.registry.ModBlockEntities;
import com.crepsman.ultimate_furnace.util.ModProperties;
//? if >=1.20.3 && <26.3 {
/*import com.mojang.serialization.MapCodec;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class UltimateFurnaceBlock extends AbstractFurnaceBlock {
	public static final BooleanProperty DAY_MODE;

	public UltimateFurnaceBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(LIT, false)
			.setValue(DAY_MODE, false)
		);
	}

	//? if >=1.20.3 && <26.3 {
	/*@Override
	protected MapCodec<UltimateFurnaceBlock> codec() {
		return simpleCodec(UltimateFurnaceBlock::new);
	}
	*///?}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UltimateFurnaceBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, DAY_MODE, FACING);
	}

	public void onBlockTick(Level level, BlockPos pos, BlockState state, UltimateFurnaceBlockEntity furnaceBlockEntity) {
		UltimateFurnaceBlockEntity.tick(level, pos, state, furnaceBlockEntity);
	}

	@Override
	protected void openContainer(Level level, BlockPos pos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof UltimateFurnaceBlockEntity) {
			player.openMenu((MenuProvider) blockEntity);
			player.awardStat(Stats.INTERACT_WITH_FURNACE);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
		super.onPlace(state, level, pos, oldState, notify);
		if (!level.isClientSide()) {
			updateDayMode(level, state, pos);
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}

		if (type == ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY) {
			return (level1, pos, state1, blockEntity) -> {
				UltimateFurnaceBlockEntity.tick(level1, pos, state1, (UltimateFurnaceBlockEntity) blockEntity);
			};
		}

		return null;
	}

	public void updateDayMode(Level level, BlockState state, BlockPos pos) {
		boolean hasSunlight = UltimateFurnaceBlockEntity.getSunlightPercent(level, pos) > 0;
		if (state.getValue(DAY_MODE) != hasSunlight) {
			level.setBlock(pos, state.setValue(DAY_MODE, hasSunlight), 3);
		}
		scheduleNextTick(level, pos);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		boolean hasSunlight = UltimateFurnaceBlockEntity.getSunlightPercent(level, pos) > 0;
		if (state.getValue(DAY_MODE) != hasSunlight) {
			level.setBlock(pos, state.setValue(DAY_MODE, hasSunlight), 3);
		}
		scheduleNextTick(level, pos);
	}

	private void scheduleNextTick(Level level, BlockPos pos) {
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 200);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(LIT)) {
			double d = pos.getX() + 0.5;
			double e = pos.getY() + 0.0;
			double f = pos.getZ() + 0.5;

			if (random.nextDouble() < 0.1) {
				level.playLocalSound(d, e, f, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}

			Direction direction = state.getValue(FACING);
			Direction.Axis axis = direction.getAxis();
			double g = 0.52;
			double h = random.nextDouble() * 0.6 - 0.3;
			double i = axis == Direction.Axis.X ? direction.getStepX() * g : h;
			double j = random.nextDouble() * 0.6 + 0.2;
			double k = axis == Direction.Axis.Z ? direction.getStepZ() * g : h;

			level.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
			level.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide()) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof UltimateFurnaceBlockEntity furnace) {
				//? if >=1.20.5 {
				CustomData data = stack.get(DataComponents.CUSTOM_DATA);
				CompoundTag tag = data != null ? data.copyTag() : null;
				//?} else {
				/*CompoundTag tag = stack.getTag();
				*///?}
				if (tag != null) {
					//? if >=1.21.5 {
					if (tag.contains("Level")) furnace.setLevel(tag.getIntOr("Level", 1));
					if (tag.contains("SmeltCount")) furnace.setSmeltCount(tag.getIntOr("SmeltCount", 0));
					if (tag.contains("StoredPower")) furnace.setStoredPower(tag.getIntOr("StoredPower", 0));
					//?} else {
					/*if (tag.contains("Level")) furnace.setLevel(tag.getInt("Level"));
					if (tag.contains("SmeltCount")) furnace.setSmeltCount(tag.getInt("SmeltCount"));
					if (tag.contains("StoredPower")) furnace.setStoredPower(tag.getInt("StoredPower"));
					*///?}
				}
			}
		}
	}

	//? if >=1.20.3 {
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
	//?} else {
	/*@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
	*///?}
		if (!level.isClientSide()) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof UltimateFurnaceBlockEntity furnace) {
				Containers.dropContents(level, pos, furnace);
				if (!player.isCreative()) {
					ItemStack stack = new ItemStack(this);
					CompoundTag tag = new CompoundTag();
					tag.putInt("Level", furnace.getFurnaceLevel());
					tag.putInt("SmeltCount", furnace.getSmeltCount());
					tag.putInt("StoredPower", furnace.getStoredPower());
					//? if >=1.20.5 {
					stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
					//?} else {
					/*stack.setTag(tag);
					*///?}
					Block.popResource(level, pos, stack);
				}
			}
			level.removeBlock(pos, false);
			//? if >=1.20.3 {
			return state;
			//?} else {
			/*return;
			*///?}
		}
		//? if >=1.20.3 {
		return super.playerWillDestroy(level, pos, state, player);
		//?} else {
		/*super.playerWillDestroy(level, pos, state, player);
		*///?}
	}

	static {
		DAY_MODE = ModProperties.DAY_MODE;
	}
}

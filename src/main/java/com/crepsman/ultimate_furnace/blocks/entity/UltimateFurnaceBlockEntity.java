package com.crepsman.ultimate_furnace.blocks.entity;

import com.crepsman.ultimate_furnace.blocks.UltimateFurnaceBlock;
import com.crepsman.ultimate_furnace.logic.SolarMath;
import com.crepsman.ultimate_furnace.registry.ModBlockEntities;
import com.crepsman.ultimate_furnace.registry.ModScreenHandlers;
import com.crepsman.ultimate_furnace.screen.UltimateFurnaceScreenHandler;
import com.crepsman.ultimate_furnace.util.FurnaceConfig;
import com.crepsman.ultimate_furnace.util.ModProperties;
//? if >=1.20.5 && <1.21.6 {
/*import net.minecraft.core.HolderLookup;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
//? if >=1.20 && <26.1 {
/*import net.minecraft.core.RegistryAccess;
*///?}
//? if <1.21.6 {
/*import net.minecraft.nbt.CompoundTag;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
//? if <1.21 {
/*import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
*///?}
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
//? if >=1.20.2 {
import net.minecraft.world.item.crafting.RecipeHolder;
//?}
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
//? if >=1.21 {
import net.minecraft.world.item.crafting.SingleRecipeInput;
//?}
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

import java.util.Optional;

public class UltimateFurnaceBlockEntity extends AbstractFurnaceBlockEntity implements WorldlyContainer {
	private int smeltCount = 0;
	private int level = 1;
	private int burnTime;
	private int storedPower = 0;
	private int cookTime;
	private int cookTimeTotal;
	private int sunlightPercent = 0;
	private boolean activelySmelting = false;

	private double cookAccumulator = 0.0;
	private double chargeAccumulator = 0.0;
	private double powerUseAccumulator = 0.0;

	private final ContainerData propertyDelegate;

	public UltimateFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ULTIMATE_FURNACE_BLOCK_ENTITY, pos, state, RecipeType.SMELTING);
		this.propertyDelegate = new ContainerData() {
			@Override
			public int get(int index) {
				switch (index) {
					case 0: return smeltCount;
					case 1: return level;
					case 2: return burnTime;
					case 3: return storedPower & 0xFFFF;
					case 4: return cookTime;
					case 5: return cookTimeTotal;
					case 6: return sunlightPercent;
					case 7: return (storedPower >>> 16) & 0xFFFF;
					default: return 0;
				}
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0: smeltCount = value; break;
					case 1: level = value; break;
					case 2: burnTime = value; break;
					case 3: storedPower = (storedPower & 0xFFFF0000) | (value & 0xFFFF); break;
					case 4: cookTime = value; break;
					case 5: cookTimeTotal = value; break;
					case 6: sunlightPercent = value; break;
					case 7: storedPower = (storedPower & 0x0000FFFF) | ((value & 0xFFFF) << 16); break;
				}
			}

			@Override
			public int getCount() {
				return 8;
			}
		};
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot != 0; // Prevent extraction from the input slot
	}

	public int getSmeltCount() {
		return smeltCount;
	}

	private void levelUp() {
		int maxLevel = FurnaceConfig.getMaxLevel();
		if (level < maxLevel) {
			level++;
			smeltCount = 0;
		}
	}

	public int getFurnaceLevel() {
		return this.level;
	}

	public void setSmeltCount(int count) {
		this.smeltCount = Math.max(0, count);
	}

	public void setLevel(int level) {
		this.level = Math.max(1, Math.min(FurnaceConfig.getMaxLevel(), level));
		int maxPower = getMaxStoredPower(this.level);
		if (this.storedPower > maxPower) {
			this.storedPower = maxPower;
		}
	}

	public int getSunlightPercent() {
		return this.sunlightPercent;
	}

	public boolean isBurning() {
		return this.activelySmelting;
	}

	/**
	 * Computes effective direct sunlight percentage (0..100) reaching the furnace's top face.
	 * - Torches/block light do not count (only sky access & LightLayer.SKY).
	 * - Solid opaque blocks anywhere straight above block sunlight completely (0%).
	 * - Clear glass / non-light-blocking transparent blocks pass 100%.
	 * - Leaves attenuate sunlight by 25% per leaf block above (4+ leaves = 0%).
	 * - Thunderstorms block sunlight completely (0%); rain reduces sunlight to 10%.
	 */
	public static int getSunlightPercent(Level world, BlockPos pos) {
		if (world == null || pos == null) return 0;

		//? if >=26.1 {
		boolean isDay = world.getDefaultClockTime() % 24000 < 12000;
		//?} else {
		/*boolean isDay = world.getDayTime() % 24000 < 12000;
		*///?}
		if (!isDay || world.isThundering()) {
			return 0;
		}

		int skyBrightness = world.getBrightness(LightLayer.SKY, pos.above());
		if (skyBrightness <= 0) {
			return 0;
		}

		//? if >=1.21.2 {
		int maxY = world.getMaxY();
		//?} else {
		/*int maxY = world.getMaxBuildHeight() - 1;
		*///?}
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
		int scanTop = Math.min(maxY, surfaceY - 1);

		int leafCount = 0;
		boolean blockedByOpaque = false;
		BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
		for (int y = pos.getY() + 1; y <= scanTop; y++) {
			checkPos.set(pos.getX(), y, pos.getZ());
			BlockState colState = world.getBlockState(checkPos);
			if (colState.isAir()) continue;
			if (colState.is(BlockTags.LEAVES)) {
				leafCount++;
				if (leafCount >= 4) {
					return 0;
				}
				continue;
			}
			//? if >=26.1 {
			int lightBlock = colState.getLightDampening();
			//?} else if >=1.21.2 {
			/*int lightBlock = colState.getLightBlock();
			*///?} else {
			/*int lightBlock = colState.getLightBlock(world, checkPos);
			*///?}
			if (lightBlock > 0) {
				blockedByOpaque = true;
				break;
			}
		}

		return SolarMath.computeSunlightPercent(
			isDay,
			world.isThundering(),
			world.isRaining(),
			skyBrightness,
			leafCount,
			blockedByOpaque
		);
	}

	//? if >=26.1 {
	private void smeltItem(RecipeHolder<SmeltingRecipe> recipe) {
		if (recipe != null && canAcceptRecipeOutput(recipe, this.items, this.getMaxStackSize())) {
			ItemStack inputStack = this.items.get(0);
			ItemStack outputStack = recipe.value().assemble(new SingleRecipeInput(inputStack));
	//?} else if >=1.20.2 {
	/*private void smeltItem(RegistryAccess registryManager, RecipeHolder<SmeltingRecipe> recipe) {
		if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, this.items, this.getMaxStackSize())) {
			ItemStack inputStack = this.items.get(0);
			//? if >=1.21 {
			ItemStack outputStack = recipe.value().assemble(new SingleRecipeInput(inputStack), registryManager);
			//?} else {
			/^ItemStack outputStack = recipe.value().assemble(new SimpleContainer(inputStack), registryManager);
			^///?}
	*///?} else if >=1.20 {
	/*private void smeltItem(RegistryAccess registryManager, SmeltingRecipe recipe) {
		if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, this.items, this.getMaxStackSize())) {
			ItemStack inputStack = this.items.get(0);
			ItemStack outputStack = recipe.assemble(new SimpleContainer(inputStack), registryManager);
	*///?} else {
	/*private void smeltItem(SmeltingRecipe recipe) {
		if (recipe != null && canAcceptRecipeOutput(recipe, this.items, this.getMaxStackSize())) {
			ItemStack inputStack = this.items.get(0);
			ItemStack outputStack = recipe.assemble(new SimpleContainer(inputStack));
	*///?}
			ItemStack currentOutputStack = this.items.get(2);

			if (currentOutputStack.isEmpty()) {
				this.items.set(2, outputStack.copy());
			//? if >=1.20 {
			} else if (ItemStack.isSameItem(currentOutputStack, outputStack)) {
			//?} else {
			/*} else if (ItemStack.isSame(currentOutputStack, outputStack)) {
			*///?}
				currentOutputStack.grow(outputStack.getCount());
			}

			inputStack.shrink(1);
			smeltCount++;
			this.cookTime = 0;
			this.cookAccumulator = 0.0;
			this.cookTimeTotal = getCookTime();
		} else {
			this.cookTime = 0;
			this.cookAccumulator = 0.0;
		}
	}

	public static void tick(Level world, BlockPos pos, BlockState state, UltimateFurnaceBlockEntity blockEntity) {
		if (world == null || pos == null || state == null || blockEntity == null || world.isClientSide()) {
			return;
		}

		int maxPower = getMaxStoredPower(blockEntity.level);
		if (blockEntity.storedPower > maxPower) {
			blockEntity.storedPower = maxPower;
		}

		int sunlight = getSunlightPercent(world, pos);
		blockEntity.sunlightPercent = sunlight;
		boolean hasSunlight = sunlight > 0;
		boolean wasActivelySmelting = blockEntity.activelySmelting;
		boolean stateChanged = false;

		ItemStack inputStack = blockEntity.getItem(0);
		ItemStack outputStack = blockEntity.getItem(2);
		boolean hasInput = !inputStack.isEmpty();
		boolean hasOutputRoom = outputStack.getCount() < outputStack.getMaxStackSize();

		boolean canSmeltNow = false;
		//? if >=1.20.2 {
		RecipeHolder<SmeltingRecipe> activeRecipe = null;
		//?} else {
		/*SmeltingRecipe activeRecipe = null;
		*///?}
		if (hasInput && hasOutputRoom) {
			//? if >=1.21 {
			Optional<RecipeHolder<SmeltingRecipe>> recipeEntry = blockEntity.getFirstMatch(new SingleRecipeInput(inputStack), world);
			if (recipeEntry.isPresent()) {
				RecipeHolder<SmeltingRecipe> recipe = recipeEntry.get();
			//?} else if >=1.20.2 {
			/*Optional<RecipeHolder<SmeltingRecipe>> recipeEntry = blockEntity.getFirstMatch(new SimpleContainer(inputStack), world);
			if (recipeEntry.isPresent()) {
				RecipeHolder<SmeltingRecipe> recipe = recipeEntry.get();
			*///?} else {
			/*Optional<SmeltingRecipe> recipeEntry = blockEntity.getFirstMatch(new SimpleContainer(inputStack), world);
			if (recipeEntry.isPresent()) {
				SmeltingRecipe recipe = recipeEntry.get();
			*///?}
				//? if >=26.1 {
				boolean recipeFits = canAcceptRecipeOutput(recipe, blockEntity.items, blockEntity.getMaxStackSize());
				//?} else if >=1.20 {
				/*boolean recipeFits = canAcceptRecipeOutput(world.registryAccess(), recipe, blockEntity.items, blockEntity.getMaxStackSize());
				*///?} else {
				/*boolean recipeFits = canAcceptRecipeOutput(recipe, blockEntity.items, blockEntity.getMaxStackSize());
				*///?}
				if (recipeFits && (sunlight > 0 || blockEntity.storedPower > 0)) {
					canSmeltNow = true;
					blockEntity.cookTimeTotal = blockEntity.getCookTime();
					activeRecipe = recipe;
				} else {
					if (blockEntity.cookTime > 0) {
						blockEntity.cookTime = Math.max(0, blockEntity.cookTime - 2);
						blockEntity.cookAccumulator = 0.0;
					}
				}
			} else {
				blockEntity.cookTime = 0;
				blockEntity.cookAccumulator = 0.0;
			}
		} else {
			blockEntity.cookTime = 0;
			blockEntity.cookAccumulator = 0.0;
		}

		SolarMath.StepResult step = SolarMath.stepTick(
			blockEntity.level,
			sunlight,
			blockEntity.storedPower,
			maxPower,
			FurnaceConfig.getPowerUseRateForLevel(blockEntity.level),
			FurnaceConfig.getPowerGainRateForLevel(blockEntity.level),
			canSmeltNow,
			blockEntity.cookAccumulator,
			blockEntity.powerUseAccumulator,
			blockEntity.chargeAccumulator
		);
		blockEntity.storedPower = step.storedPower;
		blockEntity.cookTime += step.cookProgressDelta;
		blockEntity.cookAccumulator = step.cookAccumulator;
		blockEntity.powerUseAccumulator = step.powerUseAccumulator;
		blockEntity.chargeAccumulator = step.chargeAccumulator;
		if (step.powerChanged) {
			stateChanged = true;
		}

		boolean didSmelt = false;
		if (activeRecipe != null && blockEntity.cookTime >= blockEntity.cookTimeTotal) {
			//? if >=26.1 {
			blockEntity.smeltItem(activeRecipe);
			//?} else if >=1.20 {
			/*blockEntity.smeltItem(world.registryAccess(), activeRecipe);
			*///?} else {
			/*blockEntity.smeltItem(activeRecipe);
			*///?}
			didSmelt = true;
			stateChanged = true;
		}

		boolean stillSmelting = canSmeltNow;
		if (stillSmelting && sunlight <= 0 && blockEntity.storedPower <= 0) {
			stillSmelting = false;
		}
		if (stillSmelting && didSmelt) {
			ItemStack remainingInput = blockEntity.getItem(0);
			ItemStack updatedOutput = blockEntity.getItem(2);
			if (remainingInput.isEmpty() || updatedOutput.getCount() >= updatedOutput.getMaxStackSize()) {
				stillSmelting = false;
			} else {
				//? if >=26.1 {
				stillSmelting = canAcceptRecipeOutput(activeRecipe, blockEntity.items, blockEntity.getMaxStackSize());
				//?} else if >=1.20 {
				/*stillSmelting = canAcceptRecipeOutput(world.registryAccess(), activeRecipe, blockEntity.items, blockEntity.getMaxStackSize());
				*///?} else {
				/*stillSmelting = canAcceptRecipeOutput(activeRecipe, blockEntity.items, blockEntity.getMaxStackSize());
				*///?}
			}
		}

		blockEntity.activelySmelting = stillSmelting;
		blockEntity.burnTime = stillSmelting ? 200 : 0;

		boolean currentLit = state.getValue(AbstractFurnaceBlock.LIT);
		boolean currentDayMode = state.getValue(ModProperties.DAY_MODE);
		if (currentLit != stillSmelting || currentDayMode != hasSunlight || wasActivelySmelting != stillSmelting) {
			world.setBlock(
				pos,
				state.setValue(AbstractFurnaceBlock.LIT, stillSmelting)
					.setValue(ModProperties.DAY_MODE, hasSunlight),
				Block.UPDATE_ALL
			);
			stateChanged = true;
		}

		// Leveling threshold from config
		int requiredForNextLevel = FurnaceConfig.getItemsPerLevel() * blockEntity.getFurnaceLevel();
		if (blockEntity.smeltCount >= requiredForNextLevel) {
			blockEntity.levelUp();
			stateChanged = true;
		}

		if (stateChanged) {
			blockEntity.setChanged();
		}
	}

	//? if >=1.21 {
	private Optional<RecipeHolder<SmeltingRecipe>> getFirstMatch(SingleRecipeInput input, Level world) {
	//?} else if >=1.20.2 {
	/*private Optional<RecipeHolder<SmeltingRecipe>> getFirstMatch(Container input, Level world) {
	*///?} else {
	/*private Optional<SmeltingRecipe> getFirstMatch(Container input, Level world) {
	*///?}
		if (world instanceof ServerLevel serverLevel) {
			//? if >=1.21.2 {
			RecipeManager recipeManager = serverLevel.recipeAccess();
			//?} else {
			/*RecipeManager recipeManager = serverLevel.getRecipeManager();
			*///?}
			if (recipeManager == null || input == null) return Optional.empty();
			return recipeManager.getRecipeFor(RecipeType.SMELTING, input, world);
		}
		return Optional.empty();
	}

	//? if >=26.1 {
	private static boolean canAcceptRecipeOutput(RecipeHolder<SmeltingRecipe> recipe, NonNullList<ItemStack> slots, int count) {
		if (!slots.get(0).isEmpty() && recipe != null) {
			ItemStack itemStack = recipe.value().assemble(new SingleRecipeInput(slots.get(0)));
	//?} else if >=1.20.2 {
	/*private static boolean canAcceptRecipeOutput(RegistryAccess registryManager, RecipeHolder<SmeltingRecipe> recipe, NonNullList<ItemStack> slots, int count) {
		if (!slots.get(0).isEmpty() && recipe != null) {
			//? if >=1.21 {
			ItemStack itemStack = recipe.value().assemble(new SingleRecipeInput(slots.get(0)), registryManager);
			//?} else {
			/^ItemStack itemStack = recipe.value().assemble(new SimpleContainer(slots.get(0)), registryManager);
			^///?}
	*///?} else if >=1.20 {
	/*private static boolean canAcceptRecipeOutput(RegistryAccess registryManager, SmeltingRecipe recipe, NonNullList<ItemStack> slots, int count) {
		if (!slots.get(0).isEmpty() && recipe != null) {
			ItemStack itemStack = recipe.assemble(new SimpleContainer(slots.get(0)), registryManager);
	*///?} else {
	/*private static boolean canAcceptRecipeOutput(SmeltingRecipe recipe, NonNullList<ItemStack> slots, int count) {
		if (!slots.get(0).isEmpty() && recipe != null) {
			ItemStack itemStack = recipe.assemble(new SimpleContainer(slots.get(0)));
	*///?}
			if (itemStack.isEmpty()) {
				return false;
			} else {
				ItemStack itemStack2 = slots.get(2);
				if (itemStack2.isEmpty()) {
					return true;
				//? if >=1.20 {
				} else if (!ItemStack.isSameItem(itemStack2, itemStack)) {
				//?} else {
				/*} else if (!ItemStack.isSame(itemStack2, itemStack)) {
				*///?}
					return false;
				} else if (itemStack2.getCount() < count && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
					return true;
				} else {
					return itemStack2.getCount() < itemStack.getMaxStackSize();
				}
			}
		} else {
			return false;
		}
	}

	//? if >=1.21.6 {
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("SmeltCount", this.smeltCount);
		output.putInt("Level", this.level);
		output.putInt("BurnTime", this.burnTime);
		output.putInt("StoredPower", this.storedPower);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.smeltCount = input.getIntOr("SmeltCount", 0);
		this.level = Math.max(1, input.getIntOr("Level", 1));
		this.burnTime = input.getIntOr("BurnTime", 0);
		this.activelySmelting = this.burnTime > 0;
		this.storedPower = Math.max(0, input.getIntOr("StoredPower", 0));
	}
	//?} else if >=1.20.5 {
	/*@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider lookup) {
		super.saveAdditional(nbt, lookup);
		nbt.putInt("SmeltCount", this.smeltCount);
		nbt.putInt("Level", this.level);
		nbt.putInt("BurnTime", this.burnTime);
		nbt.putInt("StoredPower", this.storedPower);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider lookup) {
		super.loadAdditional(nbt, lookup);
		//? if >=1.21.5 {
		this.smeltCount = nbt.getIntOr("SmeltCount", 0);
		this.level = Math.max(1, nbt.getIntOr("Level", 1));
		this.burnTime = nbt.getIntOr("BurnTime", 0);
		this.activelySmelting = this.burnTime > 0;
		this.storedPower = Math.max(0, nbt.getIntOr("StoredPower", 0));
		//?} else {
		/^this.smeltCount = nbt.getInt("SmeltCount");
		this.level = Math.max(1, nbt.getInt("Level"));
		this.burnTime = nbt.getInt("BurnTime");
		this.activelySmelting = this.burnTime > 0;
		this.storedPower = Math.max(0, nbt.getInt("StoredPower"));
		^///?}
	}
	*///?} else {
	/*@Override
	protected void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putInt("SmeltCount", this.smeltCount);
		nbt.putInt("Level", this.level);
		nbt.putInt("BurnTime", this.burnTime);
		nbt.putInt("StoredPower", this.storedPower);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.smeltCount = nbt.getInt("SmeltCount");
		this.level = Math.max(1, nbt.getInt("Level"));
		this.burnTime = nbt.getInt("BurnTime");
		this.activelySmelting = this.burnTime > 0;
		this.storedPower = Math.max(0, nbt.getInt("StoredPower"));
	}
	*///?}

	private int getCookTime() {
		return FurnaceConfig.getCookTimeForLevel(this.level);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0, 2};
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.ultimate_furnace.ultimate_furnace");
	}

	@Override
	public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
		return new UltimateFurnaceScreenHandler(
			ModScreenHandlers.ULTIMATE_FURNACE_SCREEN_HANDLER,
			RecipeType.SMELTING,
			syncId,
			playerInventory,
			this,
			this.propertyDelegate
		);
	}

	public void setStoredPower(int storedPower) {
		this.storedPower = Math.max(0, Math.min(getMaxStoredPower(this.level), storedPower));
	}

	public int getStoredPower() {
		return storedPower;
	}

	public static int getMaxStoredPower(int level) {
		return FurnaceConfig.getMaxStoredPowerForLevel(level);
	}
}

package com.crepsman.ultimate_furnace.util;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ModProperties {
	public static final BooleanProperty DAY_MODE = BooleanProperty.create("day_mode");
	public static final BooleanProperty HOT = BooleanProperty.create("hot");
	public static final BooleanProperty COLD = BooleanProperty.create("cold");
	public static final IntegerProperty THERMAL_STRENGTH = IntegerProperty.create("thermal_strength", 0, 4);
}

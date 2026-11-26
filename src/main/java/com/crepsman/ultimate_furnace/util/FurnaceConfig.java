package com.crepsman.ultimate_furnace.util;

import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import com.crepsman.ultimate_furnace.logic.FurnaceConfigData;
//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?}
//? if forge {
/*import net.minecraftforge.fml.loading.FMLPaths;
*///?}
//? if neoforge {
/*import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * FurnaceConfig supports array-style configuration in ultimate_furnace_config.txt:
 *   max_level=5
 *   items_per_level=3000                     # Base smelts required per level (threshold = level * items_per_level)
 *   cook_times=400,300,200,100,40            # Ticks per smelt at each level (20 ticks = 1 second).
 *   max_stored_power=0,6000,12000,24000,48000 # Capacity of stored daytime power per level.
 *   power_gain_rate=0,2,4,8,15               # Stored power gained per tick at 100% sunlight while idle (halved while smelting).
 *   power_use_rate=1,1,2,4,10                # Stored power spent per smelting tick when sunlight is insufficient.
 *   info_permission_level=0                  # Required permission level to use /ultimatefurnace info (0..4)
 */
public class FurnaceConfig {
	private static final String FILE_NAME = "ultimate_furnace_config.txt";
	private static boolean loaded = false;
	private static FurnaceConfigData data = FurnaceConfigData.defaults();

	public static synchronized void reload() {
		UltimateFurnaceMod.LOGGER.info("Reloading Ultimate Furnace config (array format)...");
		loaded = false;
		load();
	}

	public static void load() {
		if (loaded) return;
		//? if fabric {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		//?}
		//? if forgeLike {
		/*Path configDir = FMLPaths.CONFIGDIR.get();
		*///?}
		Path file = configDir.resolve(FILE_NAME);

		data = FurnaceConfigData.defaults();

		if (Files.notExists(file)) {
			try {
				createDefaultConfig(file);
			} catch (IOException e) {
				UltimateFurnaceMod.LOGGER.error("Failed to create default config file", e);
			}
		} else {
			try {
				List<String> lines = Files.readAllLines(file);
				data = FurnaceConfigData.parseLines(lines);
			} catch (IOException e) {
				UltimateFurnaceMod.LOGGER.error("Failed reading config file", e);
			}
		}
		loaded = true;
		UltimateFurnaceMod.LOGGER.info(
			"Ultimate Furnace config loaded: max_level=" + data.getMaxLevel()
				+ ", arrays: cook_times=" + Arrays.toString(data.getCookTimes())
		);
	}

	private static void createDefaultConfig(Path file) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			writer.write(FurnaceConfigData.formatDefaultConfig());
		}
	}

	// Public API getters -----------------------------------------------------
	public static int getMaxLevel() { return data.getMaxLevel(); }
	public static int getItemsPerLevel() { return data.getItemsPerLevel(); }
	public static int getInfoPermissionLevel() { return data.getInfoPermissionLevel(); }

	public static int getCookTimeForLevel(int level) {
		return data.getCookTimeForLevel(level);
	}

	public static int getMaxStoredPowerForLevel(int level) {
		return data.getMaxStoredPowerForLevel(level);
	}

	public static int getPowerGainRateForLevel(int level) {
		return data.getPowerGainRateForLevel(level);
	}

	public static int getPowerUseRateForLevel(int level) {
		return data.getPowerUseRateForLevel(level);
	}
}

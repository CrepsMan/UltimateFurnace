package com.crepsman.ultimate_furnace.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pure, Minecraft-independent configuration model and parser for Ultimate Furnace.
 */
public final class FurnaceConfigData {
	public static final int DEFAULT_MAX_LEVEL = 5;
	public static final int DEFAULT_ITEMS_PER_LEVEL = 3000;
	public static final int DEFAULT_INFO_PERMISSION_LEVEL = 0;

	public static final int[] DEFAULT_COOK_TIMES = {400, 300, 200, 100, 40};
	public static final int[] DEFAULT_MAX_STORED_POWER = {0, 6000, 12000, 24000, 48000};
	public static final int[] DEFAULT_POWER_GAIN_RATE = {0, 2, 4, 8, 15};
	public static final int[] DEFAULT_POWER_USE_RATE = {1, 1, 2, 4, 10};

	private final int maxLevel;
	private final int itemsPerLevel;
	private final int infoPermissionLevel;
	private final int[] cookTimes;
	private final int[] maxStoredPower;
	private final int[] powerGainRate;
	private final int[] powerUseRate;

	public FurnaceConfigData(
		int maxLevel,
		int itemsPerLevel,
		int infoPermissionLevel,
		int[] cookTimes,
		int[] maxStoredPower,
		int[] powerGainRate,
		int[] powerUseRate
	) {
		this.maxLevel = Math.max(1, maxLevel);
		this.itemsPerLevel = Math.max(1, itemsPerLevel);
		this.infoPermissionLevel = clamp(infoPermissionLevel, 0, 4);
		this.cookTimes = copyOrDefault(cookTimes, DEFAULT_COOK_TIMES);
		this.maxStoredPower = copyOrDefault(maxStoredPower, DEFAULT_MAX_STORED_POWER);
		this.powerGainRate = copyOrDefault(powerGainRate, DEFAULT_POWER_GAIN_RATE);
		this.powerUseRate = copyOrDefault(powerUseRate, DEFAULT_POWER_USE_RATE);
	}

	public static FurnaceConfigData defaults() {
		return new FurnaceConfigData(
			DEFAULT_MAX_LEVEL,
			DEFAULT_ITEMS_PER_LEVEL,
			DEFAULT_INFO_PERMISSION_LEVEL,
			DEFAULT_COOK_TIMES,
			DEFAULT_MAX_STORED_POWER,
			DEFAULT_POWER_GAIN_RATE,
			DEFAULT_POWER_USE_RATE
		);
	}

	public static FurnaceConfigData parseLines(Iterable<String> lines) {
		int maxLevel = DEFAULT_MAX_LEVEL;
		int itemsPerLevel = DEFAULT_ITEMS_PER_LEVEL;
		int infoPermissionLevel = DEFAULT_INFO_PERMISSION_LEVEL;
		int[] cookTimes = Arrays.copyOf(DEFAULT_COOK_TIMES, DEFAULT_COOK_TIMES.length);
		int[] maxStoredPower = Arrays.copyOf(DEFAULT_MAX_STORED_POWER, DEFAULT_MAX_STORED_POWER.length);
		int[] powerGainRate = Arrays.copyOf(DEFAULT_POWER_GAIN_RATE, DEFAULT_POWER_GAIN_RATE.length);
		int[] powerUseRate = Arrays.copyOf(DEFAULT_POWER_USE_RATE, DEFAULT_POWER_USE_RATE.length);

		boolean hasCookTimesKey = false;
		boolean hasMaxStoredKey = false;
		boolean hasGainRateKey = false;
		Map<Integer, Integer> legacyCookTimes = new TreeMap<>();
		Map<Integer, Integer> legacyMaxStored = new TreeMap<>();
		Map<Integer, Integer> legacyGainRate = new TreeMap<>();

		for (String rawLine : lines) {
			if (rawLine == null) continue;
			String line = rawLine.trim();
			if (line.isEmpty() || line.startsWith("#")) continue;
			int idx = line.indexOf('=');
			if (idx <= 0) continue;
			String key = line.substring(0, idx).trim();
			String value = line.substring(idx + 1).trim();

			switch (key) {
				case "max_level" -> maxLevel = parsePositive(value, DEFAULT_MAX_LEVEL);
				case "items_per_level" -> itemsPerLevel = parsePositive(value, DEFAULT_ITEMS_PER_LEVEL);
				case "info_permission_level" -> infoPermissionLevel = clamp(parseNonNegative(value, DEFAULT_INFO_PERMISSION_LEVEL), 0, 4);
				case "cook_times" -> {
					cookTimes = parseIntArray(value, DEFAULT_COOK_TIMES);
					hasCookTimesKey = true;
				}
				case "max_stored_power" -> {
					maxStoredPower = parseIntArray(value, DEFAULT_MAX_STORED_POWER);
					hasMaxStoredKey = true;
				}
				case "power_gain_rate" -> {
					powerGainRate = parseIntArray(value, DEFAULT_POWER_GAIN_RATE);
					hasGainRateKey = true;
				}
				case "power_use_rate" -> powerUseRate = parseIntArray(value, DEFAULT_POWER_USE_RATE);
				default -> {
					if (key.startsWith("cook_time_level_")) {
						int lvl = parsePositive(key.substring("cook_time_level_".length()), -1);
						if (lvl > 0) legacyCookTimes.put(lvl, parsePositive(value, DEFAULT_COOK_TIMES[0]));
					} else if (key.startsWith("max_stored_power_level_")) {
						int lvl = parsePositive(key.substring("max_stored_power_level_".length()), -1);
						if (lvl > 0) legacyMaxStored.put(lvl, parsePositive(value, DEFAULT_MAX_STORED_POWER[0]));
					} else if (key.startsWith("power_gain_rate_level_")) {
						int lvl = parsePositive(key.substring("power_gain_rate_level_".length()), -1);
						if (lvl > 0) legacyGainRate.put(lvl, parsePositive(value, DEFAULT_POWER_GAIN_RATE[0]));
					}
				}
			}
		}

		if (!hasCookTimesKey && !legacyCookTimes.isEmpty()) {
			cookTimes = mapToDenseArray(legacyCookTimes, DEFAULT_COOK_TIMES[DEFAULT_COOK_TIMES.length - 1]);
		}
		if (!hasMaxStoredKey && !legacyMaxStored.isEmpty()) {
			maxStoredPower = mapToDenseArray(legacyMaxStored, DEFAULT_MAX_STORED_POWER[DEFAULT_MAX_STORED_POWER.length - 1]);
		}
		if (!hasGainRateKey && !legacyGainRate.isEmpty()) {
			powerGainRate = mapToDenseArray(legacyGainRate, DEFAULT_POWER_GAIN_RATE[DEFAULT_POWER_GAIN_RATE.length - 1]);
		}

		return new FurnaceConfigData(
			maxLevel,
			itemsPerLevel,
			infoPermissionLevel,
			cookTimes,
			maxStoredPower,
			powerGainRate,
			powerUseRate
		);
	}

	public static String formatDefaultConfig() {
		return "# Ultimate Furnace Configuration (Array Format)\n"
			+ "\n"
			+ "# max_level: Highest attainable furnace level. Must be >= 1.\n"
			+ "max_level=" + DEFAULT_MAX_LEVEL + "\n\n"
			+ "# items_per_level: Base smelts needed per level.\n"
			+ "#   Threshold to reach next level = current_level * items_per_level\n"
			+ "items_per_level=" + DEFAULT_ITEMS_PER_LEVEL + "\n\n"
			+ "# cook_times:\n"
			+ "#   20 ticks = 1 second. Lower = faster smelting. Last value reused if level exceeds list length.*\n"
			+ "cook_times=" + join(DEFAULT_COOK_TIMES) + "\n\n"
			+ "# max_stored_power:\n"
			+ "#   Higher values allow more daytime charge to accumulate and be spent at night or underground.*\n"
			+ "max_stored_power=" + join(DEFAULT_MAX_STORED_POWER) + "\n\n"
			+ "# power_gain_rate:\n"
			+ "#   Power gained per tick at 100% sunlight while idle (halved while smelting). Last value reused if level exceeds list length.*\n"
			+ "power_gain_rate=" + join(DEFAULT_POWER_GAIN_RATE) + "\n\n"
			+ "# power_use_rate:\n"
			+ "#   Stored power spent per smelting tick when direct sunlight is missing or partial.*\n"
			+ "power_use_rate=" + join(DEFAULT_POWER_USE_RATE) + "\n\n"
			+ "# info_permission_level: Minecraft permission level required for /ultimatefurnace info (0..4).\n"
			+ "info_permission_level=" + DEFAULT_INFO_PERMISSION_LEVEL + "\n\n"
			+ "# NOTES:\n"
			+ "# - To extend levels beyond max_level, raise max_level AND append additional values to arrays.\n"
			+ "# - If arrays are shorter than max_level, the last array value is reused for higher levels.\n"
			+ "# - Use /ultimatefurnace reload in-game to apply changes without restarting.\n";
	}

	public int getMaxLevel() { return maxLevel; }
	public int getItemsPerLevel() { return itemsPerLevel; }
	public int getInfoPermissionLevel() { return infoPermissionLevel; }

	public int[] getCookTimes() { return Arrays.copyOf(cookTimes, cookTimes.length); }
	public int[] getMaxStoredPower() { return Arrays.copyOf(maxStoredPower, maxStoredPower.length); }
	public int[] getPowerGainRate() { return Arrays.copyOf(powerGainRate, powerGainRate.length); }
	public int[] getPowerUseRate() { return Arrays.copyOf(powerUseRate, powerUseRate.length); }

	public int getCookTimeForLevel(int level) {
		if (level <= 0) return cookTimes[0];
		return cookTimes[level - 1 < cookTimes.length ? level - 1 : cookTimes.length - 1];
	}

	public int getMaxStoredPowerForLevel(int level) {
		if (level <= 0) return maxStoredPower[0];
		return maxStoredPower[level - 1 < maxStoredPower.length ? level - 1 : maxStoredPower.length - 1];
	}

	public int getPowerGainRateForLevel(int level) {
		if (level <= 0) return powerGainRate[0];
		return powerGainRate[level - 1 < powerGainRate.length ? level - 1 : powerGainRate.length - 1];
	}

	public int getPowerUseRateForLevel(int level) {
		if (level <= 0) return Math.max(1, powerUseRate[0]);
		int val = powerUseRate[level - 1 < powerUseRate.length ? level - 1 : powerUseRate.length - 1];
		return Math.max(1, val);
	}

	private static int[] copyOrDefault(int[] src, int[] def) {
		if (src == null || src.length == 0) {
			return Arrays.copyOf(def, def.length);
		}
		return Arrays.copyOf(src, src.length);
	}

	private static int parsePositive(String s, int def) {
		try {
			int v = Integer.parseInt(s.trim());
			return v > 0 ? v : def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static int parseNonNegative(String s, int def) {
		try {
			int v = Integer.parseInt(s.trim());
			return v >= 0 ? v : def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static int clamp(int v, int min, int max) {
		return Math.min(max, Math.max(min, v));
	}

	private static int[] parseIntArray(String value, int[] def) {
		String[] parts = value.split(",");
		List<Integer> out = new ArrayList<>();
		for (String p : parts) {
			p = p.trim();
			if (p.isEmpty()) continue;
			try {
				out.add(Math.max(0, Integer.parseInt(p)));
			} catch (NumberFormatException ignored) {}
		}
		if (out.isEmpty()) return Arrays.copyOf(def, def.length);
		int[] arr = new int[out.size()];
		for (int i = 0; i < out.size(); i++) arr[i] = out.get(i);
		return arr;
	}

	private static int[] mapToDenseArray(Map<Integer, Integer> map, int fallback) {
		int maxKey = map.keySet().stream().max(Integer::compareTo).orElse(1);
		int[] arr = new int[maxKey];
		for (int i = 1; i <= maxKey; i++) {
			arr[i - 1] = map.getOrDefault(i, fallback);
		}
		return arr;
	}

	private static String join(int[] arr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(arr[i]);
		}
		return sb.toString();
	}
}

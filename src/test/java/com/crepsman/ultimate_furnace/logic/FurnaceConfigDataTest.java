package com.crepsman.ultimate_furnace.logic;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FurnaceConfigDataTest {
	@Test
	void defaultsMatchBalancedLevelProgression() {
		FurnaceConfigData cfg = FurnaceConfigData.defaults();
		assertEquals(5, cfg.getMaxLevel());
		assertEquals(3000, cfg.getItemsPerLevel());
		assertEquals(0, cfg.getInfoPermissionLevel());

		int[] expectedCook = {400, 300, 200, 100, 40};
		int[] expectedMaxPower = {0, 6000, 12000, 24000, 48000};
		int[] expectedGain = {0, 2, 4, 8, 15};
		int[] expectedUse = {1, 1, 2, 4, 10};

		for (int level = 1; level <= 5; level++) {
			assertEquals(expectedCook[level - 1], cfg.getCookTimeForLevel(level));
			assertEquals(expectedMaxPower[level - 1], cfg.getMaxStoredPowerForLevel(level));
			assertEquals(expectedGain[level - 1], cfg.getPowerGainRateForLevel(level));
			assertEquals(expectedUse[level - 1], cfg.getPowerUseRateForLevel(level));
		}
	}

	@Test
	void parsesArraysClampsBoundsAndReusesTailForExtraLevels() {
		FurnaceConfigData cfg = FurnaceConfigData.parseLines(List.of(
			"# Comment line",
			"",
			"max_level = 7",
			"items_per_level = 1500",
			"info_permission_level = 99",
			"cook_times = 200, 100, 50",
			"max_stored_power = 0, 1000, 5000",
			"power_gain_rate = 0, 3, 6",
			"power_use_rate = 0, 2, 5"
		));

		assertEquals(7, cfg.getMaxLevel());
		assertEquals(1500, cfg.getItemsPerLevel());
		assertEquals(4, cfg.getInfoPermissionLevel());

		assertEquals(200, cfg.getCookTimeForLevel(0));
		assertEquals(200, cfg.getCookTimeForLevel(1));
		assertEquals(100, cfg.getCookTimeForLevel(2));
		assertEquals(50, cfg.getCookTimeForLevel(3));
		assertEquals(50, cfg.getCookTimeForLevel(6));

		assertEquals(1, cfg.getPowerUseRateForLevel(1)); // Clamped to >= 1
		assertEquals(2, cfg.getPowerUseRateForLevel(2));
		assertEquals(5, cfg.getPowerUseRateForLevel(7));
	}

	@Test
	void supportsLegacyPerLevelKeysWhenArraysAreAbsent() {
		FurnaceConfigData cfg = FurnaceConfigData.parseLines(List.of(
			"cook_time_level_1=250",
			"cook_time_level_2=125",
			"max_stored_power_level_2=9000",
			"power_gain_rate_level_3=12"
		));

		assertEquals(250, cfg.getCookTimeForLevel(1));
		assertEquals(125, cfg.getCookTimeForLevel(2));
		assertEquals(9000, cfg.getMaxStoredPowerForLevel(2));
		assertEquals(12, cfg.getPowerGainRateForLevel(3));
	}

	@Test
	void defaultConfigFormatRoundTripsCleanly() {
		List<String> lines = Arrays.asList(FurnaceConfigData.formatDefaultConfig().split("\n"));
		FurnaceConfigData parsed = FurnaceConfigData.parseLines(lines);
		FurnaceConfigData defaults = FurnaceConfigData.defaults();

		assertEquals(defaults.getMaxLevel(), parsed.getMaxLevel());
		assertEquals(defaults.getItemsPerLevel(), parsed.getItemsPerLevel());
		assertEquals(defaults.getInfoPermissionLevel(), parsed.getInfoPermissionLevel());
		assertArrayEquals(defaults.getCookTimes(), parsed.getCookTimes());
		assertArrayEquals(defaults.getMaxStoredPower(), parsed.getMaxStoredPower());
		assertArrayEquals(defaults.getPowerGainRate(), parsed.getPowerGainRate());
		assertArrayEquals(defaults.getPowerUseRate(), parsed.getPowerUseRate());
	}
}

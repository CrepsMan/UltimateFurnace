package com.crepsman.ultimate_furnace.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolarMathTest {
	@Test
	void sunlightObstructionAndWeatherFollowSpecification() {
		assertEquals(100, SolarMath.computeSunlightPercent(true, false, false, 15, 0, false));
		assertEquals(75, SolarMath.computeSunlightPercent(true, false, false, 15, 1, false));
		assertEquals(50, SolarMath.computeSunlightPercent(true, false, false, 15, 2, false));
		assertEquals(25, SolarMath.computeSunlightPercent(true, false, false, 15, 3, false));
		assertEquals(0, SolarMath.computeSunlightPercent(true, false, false, 15, 4, false));
		assertEquals(0, SolarMath.computeSunlightPercent(true, false, false, 15, 6, false));

		// Solid block / underground / night / thunderstorm block sunlight completely
		assertEquals(0, SolarMath.computeSunlightPercent(true, false, false, 15, 0, true));
		assertEquals(0, SolarMath.computeSunlightPercent(true, false, false, 0, 0, false));
		assertEquals(0, SolarMath.computeSunlightPercent(false, false, false, 15, 0, false));
		assertEquals(0, SolarMath.computeSunlightPercent(true, true, true, 15, 0, false));

		// Rain cuts clear sunlight to 10% (min 1%)
		assertEquals(10, SolarMath.computeSunlightPercent(true, false, true, 15, 0, false));
		assertEquals(7, SolarMath.computeSunlightPercent(true, false, true, 15, 1, false));
		assertEquals(2, SolarMath.computeSunlightPercent(true, false, true, 15, 3, false));
	}

	@Test
	void levelOneHasZeroBatteryStorageAndIdleHasZeroDarkDrain() {
		FurnaceConfigData cfg = FurnaceConfigData.defaults();

		// Level 1 in full sun charges 0 power (maxStoredPower == 0)
		SolarMath.StepResult lvl1Sun = SolarMath.stepTick(
			1, 100, 0,
			cfg.getMaxStoredPowerForLevel(1),
			cfg.getPowerUseRateForLevel(1),
			cfg.getPowerGainRateForLevel(1),
			false, 0.0, 0.0, 0.0
		);
		assertEquals(0, lvl1Sun.storedPower);
		assertFalse(lvl1Sun.powerChanged);

		// Level 2 idle in darkness never drains stored power
		SolarMath.StepResult lvl2DarkIdle = SolarMath.stepTick(
			2, 0, 1000,
			cfg.getMaxStoredPowerForLevel(2),
			cfg.getPowerUseRateForLevel(2),
			cfg.getPowerGainRateForLevel(2),
			false, 0.0, 0.0, 0.0
		);
		assertEquals(1000, lvl2DarkIdle.storedPower);
		assertEquals(0, lvl2DarkIdle.cookProgressDelta);
		assertFalse(lvl2DarkIdle.powerChanged);
	}

	@Test
	void chargingIsHalvedWhileSmeltingAndDrainsAccordingToLevelUseRate() {
		FurnaceConfigData cfg = FurnaceConfigData.defaults();

		// Level 3: gain=4 idle, gain=2 smelting
		SolarMath.StepResult idleCharge = SolarMath.stepTick(
			3, 100, 100,
			cfg.getMaxStoredPowerForLevel(3),
			cfg.getPowerUseRateForLevel(3),
			cfg.getPowerGainRateForLevel(3),
			false, 0.0, 0.0, 0.0
		);
		assertEquals(104, idleCharge.storedPower);
		assertTrue(idleCharge.powerChanged);

		SolarMath.StepResult smeltingCharge = SolarMath.stepTick(
			3, 100, 100,
			cfg.getMaxStoredPowerForLevel(3),
			cfg.getPowerUseRateForLevel(3),
			cfg.getPowerGainRateForLevel(3),
			true, 0.0, 0.0, 0.0
		);
		assertEquals(102, smeltingCharge.storedPower);
		assertEquals(1, smeltingCharge.cookProgressDelta);

		// Darkness drain scales across levels 2..5 using power_use_rate (1, 2, 4, 10)
		for (int level = 2; level <= 5; level++) {
			int useRate = cfg.getPowerUseRateForLevel(level);
			SolarMath.StepResult darkSmelt = SolarMath.stepTick(
				level, 0, 100,
				cfg.getMaxStoredPowerForLevel(level),
				useRate,
				cfg.getPowerGainRateForLevel(level),
				true, 0.0, 0.0, 0.0
			);
			assertEquals(100 - useRate, darkSmelt.storedPower);
			assertEquals(1, darkSmelt.cookProgressDelta);
			assertTrue(darkSmelt.powerChanged);
		}
	}

	@Test
	void partialSunlightSlowsUnbufferedSmeltingOrTopsUpFromBattery() {
		// No battery at 50% sunlight -> takes 2 ticks per 1 cook progress
		SolarMath.StepResult tick1 = SolarMath.stepTick(2, 50, 0, 6000, 2, 2, true, 0.0, 0.0, 0.0);
		assertEquals(0, tick1.cookProgressDelta);
		SolarMath.StepResult tick2 = SolarMath.stepTick(2, 50, tick1.storedPower, 6000, 2, 2, true, tick1.cookAccumulator, tick1.powerUseAccumulator, tick1.chargeAccumulator);
		assertEquals(1, tick2.cookProgressDelta);

		// With battery at 50% sunlight (useRate=2 -> 1/tick battery deficit, gain=2*0.5*0.5=0.5 charge): full cook speed
		SolarMath.StepResult buffered = SolarMath.stepTick(2, 50, 100, 6000, 2, 2, true, 0.0, 0.0, 0.0);
		assertEquals(1, buffered.cookProgressDelta);
		assertEquals(99, buffered.storedPower);
	}
}

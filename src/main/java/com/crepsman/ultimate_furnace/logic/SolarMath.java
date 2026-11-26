package com.crepsman.ultimate_furnace.logic;

/**
 * Pure sunlight obstruction and power/cook stepping math for Ultimate Furnace.
 */
public final class SolarMath {
	private SolarMath() {}

	/**
	 * Computes effective sunlight percentage (0..100).
	 * - Night, thunderstorm, zero sky brightness, solid opaque block, or 4+ leaves -> 0%.
	 * - Otherwise 100 - 25 * leafCountAbove, reduced to 10% (min 1%) during rain.
	 */
	public static int computeSunlightPercent(
		boolean isDay,
		boolean isThundering,
		boolean isRaining,
		int skyBrightnessAbove,
		int leafCountAbove,
		boolean blockedByOpaque
	) {
		if (!isDay || isThundering || skyBrightnessAbove <= 0 || blockedByOpaque || leafCountAbove >= 4) {
			return 0;
		}
		int clampedLeaves = Math.max(0, leafCountAbove);
		int sunlight = 100 - (clampedLeaves * 25);
		if (isRaining) {
			sunlight = Math.max(1, (sunlight * 10) / 100);
		}
		return sunlight;
	}

	public static final class StepResult {
		public final int storedPower;
		public final int cookProgressDelta;
		public final double cookAccumulator;
		public final double powerUseAccumulator;
		public final double chargeAccumulator;
		public final boolean powerChanged;

		public StepResult(
			int storedPower,
			int cookProgressDelta,
			double cookAccumulator,
			double powerUseAccumulator,
			double chargeAccumulator,
			boolean powerChanged
		) {
			this.storedPower = storedPower;
			this.cookProgressDelta = cookProgressDelta;
			this.cookAccumulator = cookAccumulator;
			this.powerUseAccumulator = powerUseAccumulator;
			this.chargeAccumulator = chargeAccumulator;
			this.powerChanged = powerChanged;
		}
	}

	/**
	 * Advances smelting progress and stored battery power by a single server tick.
	 */
	public static StepResult stepTick(
		int furnaceLevel,
		int sunlightPercent,
		int storedPower,
		int maxStoredPower,
		int powerUseRate,
		int powerGainRate,
		boolean smeltingNow,
		double cookAccumulator,
		double powerUseAccumulator,
		double chargeAccumulator
	) {
		int power = Math.min(Math.max(0, storedPower), Math.max(0, maxStoredPower));
		int initialPower = power;
		int cookDelta = 0;
		double nextCookAcc = cookAccumulator;
		double nextUseAcc = powerUseAccumulator;
		double nextChargeAcc = chargeAccumulator;

		if (smeltingNow) {
			double progressStep;
			if (sunlightPercent >= 100) {
				progressStep = 1.0;
			} else if (sunlightPercent > 0) {
				double solarFraction = sunlightPercent / 100.0;
				double neededBattery = powerUseRate * (1.0 - solarFraction);
				if (power > 0) {
					nextUseAcc += neededBattery;
					int wholeDrain = (int) nextUseAcc;
					if (wholeDrain > 0) {
						int actualDrain = Math.min(power, wholeDrain);
						power -= actualDrain;
						nextUseAcc -= actualDrain;
					}
					progressStep = 1.0;
				} else {
					progressStep = solarFraction;
				}
			} else {
				if (power >= powerUseRate) {
					power -= powerUseRate;
					progressStep = 1.0;
				} else {
					progressStep = powerUseRate > 0 ? power / (double) powerUseRate : 0.0;
					power = 0;
				}
			}

			nextCookAcc += progressStep;
			int wholeCook = (int) nextCookAcc;
			if (wholeCook > 0) {
				cookDelta = wholeCook;
				nextCookAcc -= wholeCook;
			}
		}

		// Charge stored power only in sunlight; charge at half rate while actively smelting.
		if (sunlightPercent > 0 && furnaceLevel > 1 && power < maxStoredPower) {
			double effectiveGain = smeltingNow ? powerGainRate * 0.5 : powerGainRate;
			if (effectiveGain > 0.0) {
				nextChargeAcc += effectiveGain * (sunlightPercent / 100.0);
				int wholeGain = (int) nextChargeAcc;
				if (wholeGain > 0) {
					power = Math.min(maxStoredPower, power + wholeGain);
					nextChargeAcc -= wholeGain;
				}
			}
		} else {
			nextChargeAcc = 0.0;
		}

		return new StepResult(
			power,
			cookDelta,
			nextCookAcc,
			nextUseAcc,
			nextChargeAcc,
			power != initialPower
		);
	}
}

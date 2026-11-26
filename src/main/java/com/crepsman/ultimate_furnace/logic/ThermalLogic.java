package com.crepsman.ultimate_furnace.logic;

/**
 * Pure thermal conduction and comparator signal math for CopperPlateBlock.
 */
public final class ThermalLogic {
	private ThermalLogic() {}

	public static final class NeighborState {
		public final boolean warmSource;
		public final boolean coldSource;
		public final boolean copperPlate;
		public final boolean plateHot;
		public final boolean plateCold;
		public final int plateStrength;

		public NeighborState(
			boolean warmSource,
			boolean coldSource,
			boolean copperPlate,
			boolean plateHot,
			boolean plateCold,
			int plateStrength
		) {
			this.warmSource = warmSource;
			this.coldSource = coldSource;
			this.copperPlate = copperPlate;
			this.plateHot = plateHot;
			this.plateCold = plateCold;
			this.plateStrength = plateStrength;
		}

		public static NeighborState warmBlock() {
			return new NeighborState(true, false, false, false, false, 0);
		}

		public static NeighborState coldBlock() {
			return new NeighborState(false, true, false, false, false, 0);
		}

		public static NeighborState plate(boolean hot, boolean cold, int strength) {
			return new NeighborState(false, false, true, hot, cold, strength);
		}

		public static NeighborState neutral() {
			return new NeighborState(false, false, false, false, false, 0);
		}
	}

	public static final class ThermalResult {
		public final boolean hot;
		public final boolean cold;
		public final int strength;

		public ThermalResult(boolean hot, boolean cold, int strength) {
			this.hot = hot;
			this.cold = cold;
			this.strength = strength;
		}
	}

	public static ThermalResult computeThermalState(Iterable<NeighborState> neighbors) {
		int hotStrength = 0;
		int coldStrength = 0;

		for (NeighborState neighbor : neighbors) {
			if (neighbor == null) continue;
			if (neighbor.warmSource) {
				hotStrength = Math.max(hotStrength, 4);
			}
			if (neighbor.coldSource) {
				coldStrength = Math.max(coldStrength, 4);
			}
			if (neighbor.copperPlate && neighbor.plateStrength > 1) {
				if (neighbor.plateHot) {
					hotStrength = Math.max(hotStrength, neighbor.plateStrength - 1);
				} else if (neighbor.plateCold) {
					coldStrength = Math.max(coldStrength, neighbor.plateStrength - 1);
				}
			}
		}

		// Conflicting hot and cold sources neutralize each other.
		boolean nextHot = hotStrength > 0 && coldStrength == 0;
		boolean nextCold = coldStrength > 0 && hotStrength == 0;
		int nextStrength = nextHot ? hotStrength : (nextCold ? coldStrength : 0);

		return new ThermalResult(nextHot, nextCold, nextStrength);
	}

	public static int getComparatorOutput(boolean hot, boolean cold, int thermalStrength) {
		if (!hot && !cold) {
			return 0;
		}
		return switch (thermalStrength) {
			case 1 -> 4;
			case 2 -> 8;
			case 3 -> 12;
			case 4 -> 15;
			default -> 0;
		};
	}
}

package com.crepsman.ultimate_furnace.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.crepsman.ultimate_furnace.logic.ThermalLogic.NeighborState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThermalLogicTest {
	@Test
	void thermalConductionDecaysFourThreeTwoOneZero() {
		ThermalLogic.ThermalResult step4 = ThermalLogic.computeThermalState(List.of(NeighborState.warmBlock()));
		assertTrue(step4.hot);
		assertFalse(step4.cold);
		assertEquals(4, step4.strength);

		ThermalLogic.ThermalResult step3 = ThermalLogic.computeThermalState(List.of(NeighborState.plate(true, false, step4.strength)));
		assertTrue(step3.hot);
		assertEquals(3, step3.strength);

		ThermalLogic.ThermalResult step2 = ThermalLogic.computeThermalState(List.of(NeighborState.plate(true, false, step3.strength)));
		assertTrue(step2.hot);
		assertEquals(2, step2.strength);

		ThermalLogic.ThermalResult step1 = ThermalLogic.computeThermalState(List.of(NeighborState.plate(true, false, step2.strength)));
		assertTrue(step1.hot);
		assertEquals(1, step1.strength);

		ThermalLogic.ThermalResult step0 = ThermalLogic.computeThermalState(List.of(NeighborState.plate(true, false, step1.strength)));
		assertFalse(step0.hot);
		assertFalse(step0.cold);
		assertEquals(0, step0.strength);
	}

	@Test
	void warmAndColdSourcesCancelToNeutral() {
		ThermalLogic.ThermalResult directConflict = ThermalLogic.computeThermalState(
			List.of(NeighborState.warmBlock(), NeighborState.coldBlock())
		);
		assertFalse(directConflict.hot);
		assertFalse(directConflict.cold);
		assertEquals(0, directConflict.strength);

		ThermalLogic.ThermalResult conductedConflict = ThermalLogic.computeThermalState(
			List.of(NeighborState.plate(true, false, 4), NeighborState.plate(false, true, 2))
		);
		assertFalse(conductedConflict.hot);
		assertFalse(conductedConflict.cold);
		assertEquals(0, conductedConflict.strength);
	}

	@Test
	void comparatorOutputMatchesThermalStrength() {
		assertEquals(0, ThermalLogic.getComparatorOutput(false, false, 4));
		assertEquals(0, ThermalLogic.getComparatorOutput(true, false, 0));
		assertEquals(4, ThermalLogic.getComparatorOutput(true, false, 1));
		assertEquals(8, ThermalLogic.getComparatorOutput(true, false, 2));
		assertEquals(12, ThermalLogic.getComparatorOutput(false, true, 3));
		assertEquals(15, ThermalLogic.getComparatorOutput(false, true, 4));
	}
}

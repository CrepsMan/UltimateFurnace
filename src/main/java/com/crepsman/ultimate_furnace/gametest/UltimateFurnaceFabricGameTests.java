package com.crepsman.ultimate_furnace.gametest;

//? if fabric {
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
*///?}

//? if >=1.21.5 {
public class UltimateFurnaceFabricGameTests {
	@GameTest(structure = "ultimate_furnace:empty", maxTicks = 100, skyAccess = true)
	public void sunlightAndObstruction(GameTestHelper helper) {
		UltimateFurnaceGameTests.sunlightAndObstruction(helper);
	}

	@GameTest(structure = "ultimate_furnace:empty", maxTicks = 700, skyAccess = true)
	public void batteryAndLitState(GameTestHelper helper) {
		UltimateFurnaceGameTests.batteryAndLitState(helper);
	}

	@GameTest(structure = "ultimate_furnace:empty", maxTicks = 100, skyAccess = true)
	public void itemPersistence(GameTestHelper helper) {
		UltimateFurnaceGameTests.itemPersistence(helper);
	}

	@GameTest(structure = "ultimate_furnace:empty", maxTicks = 200, skyAccess = true)
	public void copperPlateThermalAndWorldEffects(GameTestHelper helper) {
		UltimateFurnaceGameTests.copperPlateThermalAndWorldEffects(helper);
	}
}
//?} else {
/*public class UltimateFurnaceFabricGameTests implements FabricGameTest {
	//? if >=1.20.5 {
	@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 100, skyAccess = true)
	//?} else {
	/^@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 100)
	^///?}
	public void sunlightAndObstruction(GameTestHelper helper) {
		UltimateFurnaceGameTests.sunlightAndObstruction(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 700, skyAccess = true)
	//?} else {
	/^@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 700)
	^///?}
	public void batteryAndLitState(GameTestHelper helper) {
		UltimateFurnaceGameTests.batteryAndLitState(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 100, skyAccess = true)
	//?} else {
	/^@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 100)
	^///?}
	public void itemPersistence(GameTestHelper helper) {
		UltimateFurnaceGameTests.itemPersistence(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 200, skyAccess = true)
	//?} else {
	/^@GameTest(template = "ultimate_furnace:empty", timeoutTicks = 200)
	^///?}
	public void copperPlateThermalAndWorldEffects(GameTestHelper helper) {
		UltimateFurnaceGameTests.copperPlateThermalAndWorldEffects(helper);
	}
}
*///?}
//?}

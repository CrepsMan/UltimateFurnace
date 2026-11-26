package com.crepsman.ultimate_furnace.gametest;

//? if forgeLike && <1.21.5 {
/*import com.crepsman.ultimate_furnace.UltimateFurnaceMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
//? if forge && <1.20.2 {
/^import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
^///?} else if forge {
/^import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
^///?} else if neoforge {
/^import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
^///?}

//? if forge && >=1.20.2 {
/^@GameTestHolder(namespace = UltimateFurnaceMod.MOD_ID)
@GameTestDontPrefix
^///?} else {
@GameTestHolder(UltimateFurnaceMod.MOD_ID)
@PrefixGameTestTemplate(false)
//?}
public class UltimateFurnaceForgeLikeGameTests {
	//? if >=1.20.5 {
	@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 100, skyAccess = true)
	//?} else {
	/^@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 100)
	^///?}
	public static void sunlightAndObstruction(GameTestHelper helper) {
		UltimateFurnaceGameTests.sunlightAndObstruction(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 700, skyAccess = true)
	//?} else {
	/^@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 700)
	^///?}
	public static void batteryAndLitState(GameTestHelper helper) {
		UltimateFurnaceGameTests.batteryAndLitState(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 100, skyAccess = true)
	//?} else {
	/^@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 100)
	^///?}
	public static void itemPersistence(GameTestHelper helper) {
		UltimateFurnaceGameTests.itemPersistence(helper);
	}

	//? if >=1.20.5 {
	@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 200, skyAccess = true)
	//?} else {
	/^@GameTest(template = "empty", batch = UltimateFurnaceMod.MOD_ID, timeoutTicks = 200)
	^///?}
	public static void copperPlateThermalAndWorldEffects(GameTestHelper helper) {
		UltimateFurnaceGameTests.copperPlateThermalAndWorldEffects(helper);
	}
}
*///?}

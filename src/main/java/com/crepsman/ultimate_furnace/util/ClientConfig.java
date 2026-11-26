package com.crepsman.ultimate_furnace.util;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
//?}
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
*///?}
//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//? if fabric {
@Environment(EnvType.CLIENT)
//?}
//? if forge {
/*@OnlyIn(Dist.CLIENT)
*///?}
public class ClientConfig {
	private static final String FILE_NAME = "ultimate_furnace_client.properties";
	private static boolean loaded = false;
	private static boolean useUnicodeBar = true;
	private static String barColor = "DARK_GREEN"; // Formatting color name

	public static void load() {
		//? if fabric {
		if (loaded || FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;
		Path configDir = FabricLoader.getInstance().getConfigDir();
		//?}
		//? if forge {
		/*if (loaded || FMLEnvironment.dist != Dist.CLIENT) return;
		Path configDir = FMLPaths.CONFIGDIR.get();
		*///?}
		//? if neoforge {
		/*//? if >=1.21.9 {
		if (loaded || FMLEnvironment.getDist() != Dist.CLIENT) return;
		//?} else {
		/^if (loaded || FMLEnvironment.dist != Dist.CLIENT) return;
		^///?}
		Path configDir = FMLPaths.CONFIGDIR.get();
		*///?}
		Path file = configDir.resolve(FILE_NAME);
		Properties props = new Properties();
		if (Files.notExists(file)) {
			useUnicodeBar = true;
			barColor = "DARK_GREEN";
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				writer.write("useUnicodeBar=true\n");
				writer.write("barColor=DARK_GREEN\n");
			} catch (IOException ignored) {}
		} else {
			try {
				props.load(Files.newBufferedReader(file));
				useUnicodeBar = Boolean.parseBoolean(props.getProperty("useUnicodeBar", "true"));
				barColor = props.getProperty("barColor", "DARK_GREEN");
			} catch (IOException ignored) {}
		}
		loaded = true;
	}

	public static boolean useUnicodeBar() { return useUnicodeBar; }
	public static String barColor() { return barColor; }
}

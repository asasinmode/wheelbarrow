package com.asasinmode.wheelbarrow.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.loader.api.FabricLoader;

public class ModConfig {
	public static File optionsFile;

	public static void registerModConfig() {
		Path configPath = FabricLoader.getInstance().getConfigDir();
		ModConfig.optionsFile = new File(configPath.resolve(Wheelbarrow.MOD_ID + ".txt").toString());

		System.out.println("-------------");
		System.out.println("-------------");
		System.out.println("-------------");
		Wheelbarrow.LOGGER
				.info("creating config file " + ModConfig.optionsFile + " exists " + ModConfig.optionsFile.exists()
						+ " config dir: " + configPath.toString());
		System.out.println("-------------");
		System.out.println("-------------");
		System.out.println("-------------");
	}
}

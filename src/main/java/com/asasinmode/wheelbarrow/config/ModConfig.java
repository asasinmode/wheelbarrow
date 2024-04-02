package com.asasinmode.wheelbarrow.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.loader.api.FabricLoader;

public class ModConfig {
	private static File optionsFile;
	public static final String DEFAULT_VALUE = "maxPassengers:1";
	private static int MAX_PASSENGERS = 1;

	public static void registerModConfig() {
		Path configPath = FabricLoader.getInstance().getConfigDir();
		optionsFile = new File(configPath.resolve(Wheelbarrow.MOD_ID + ".txt").toString());

		if (!optionsFile.exists()) {
			try (final PrintWriter printWriter = new PrintWriter(optionsFile, StandardCharsets.UTF_8);) {
				printWriter.write(DEFAULT_VALUE);
				Wheelbarrow.LOGGER.info("Created the default config file");

				return;
			} catch (IOException e) {
				// TODO find out what the errors should look like
				Wheelbarrow.LOGGER.error("Failed to create the default config file at '" + optionsFile.getPath() + "'!");
				e.printStackTrace();
			}
		}

		boolean foundMaxPassengers = false;

		try (final Scanner scanner = new Scanner(optionsFile)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.isBlank()) {
					continue;
				}

				String[] parts = line.split(":", 2);
				if (parts.length != 2) {
					continue;
				}

				String property = parts[0];
				String value = parts[1];

				if (property.equals("maxPassengers")) {
					foundMaxPassengers = true;
					try {
						MAX_PASSENGERS = Integer.parseInt(value);
					} catch (Exception e) {
						Wheelbarrow.LOGGER
								.warn("Failed to parse the 'maxPassengers' config value '" + value + "'. Should be an integer!");
						MAX_PASSENGERS = 1;
					}
				}
			}
		} catch (FileNotFoundException e) {
			Wheelbarrow.LOGGER.error("Failed to read the config file at '" + optionsFile.getPath() + "'!");
			e.printStackTrace();
		}

		if (!foundMaxPassengers) {
			Wheelbarrow.LOGGER.warn("Max passengers config not found in the config file! Using the default value");
		}
	}

	public static int getMaxPassengers() {
		return MAX_PASSENGERS;
	}
}

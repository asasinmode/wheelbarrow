package com.asasinmode.wheelbarrow;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

// inspired by https://github.com/newhoryzon/farmers-delight-fabric/blob/master/src/main/java/com/nhoryzon/mc/farmersdelight/Configuration.java
public class Config {
	private static File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(),
			Wheelbarrow.MOD_ID + ".json");

	private int maxPassengers = 1;

	public Config() {
	}

	public static Config load() {
		Config config = new Config();
		if (!CONFIG_FILE.exists()) {
			save(config);
		}

		Reader reader;
		try {
			reader = Files.newBufferedReader(CONFIG_FILE.toPath());
			config = (new GsonBuilder().setPrettyPrinting().create()).fromJson(reader, Config.class);
			reader.close();
		} catch (IOException e) {
			Wheelbarrow.LOGGER.error("Failed to load the configuration file. Default configuration used.", e);
		}

		return config;
	}

	public static void save(Config config) {
		System.out.println("saving " + config.getMaxPassengers());
		try {
			Writer writer = Files.newBufferedWriter(CONFIG_FILE.toPath());
			(new GsonBuilder().setPrettyPrinting().create()).toJson(config, writer);
			writer.close();
		} catch (IOException e) {
			Wheelbarrow.LOGGER.error("Failed to save configuration file.", e);
		}
	}

	public int getMaxPassengers() {
		return this.maxPassengers;
	}

	public void setMaxPassengers(int value) {
		this.maxPassengers = Math.min(Integer.MAX_VALUE, Math.max(0, value));
	}
}

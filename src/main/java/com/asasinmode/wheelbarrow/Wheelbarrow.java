package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wheelbarrow implements ModInitializer {
	public static final String MOD_ID = "wheelbarrow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Config CONFIG = new Config();

	@Override
	public void onInitialize() {
		CONFIG = Config.load();
		ModItems.registerModItems();
		ModEntities.registerModEntities();
	}
}

package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.item.ModItems;

import net.fabricmc.api.ModInitializer;

public class Wheelbarrow implements ModInitializer {
	public static final String MOD_ID = "wheelbarrow";

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
	}
}

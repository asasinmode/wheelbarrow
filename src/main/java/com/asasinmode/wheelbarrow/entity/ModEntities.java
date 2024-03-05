package com.asasinmode.wheelbarrow.entity;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
	public static final Item COPPER_WHEELBARROW = registerEntity("copper_wheelbarrow",
			new CopperWheelbarrowItem(new FabricItemSettings().maxCount(1)));

	private static Item registerEntity(String name, Item item) {
		return Registry.register(Registries.ITEM, new Identifier(Wheelbarrow.MOD_ID, name), item);
	}

	public static void registerModEntities() {
	}
}

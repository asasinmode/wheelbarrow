package com.asasinmode.wheelbarrow.item;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.item.custom.*;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
	public static final Item COPPER_WHEELBARROW = registerItem("copper_wheelbarrow",
			new CopperWheelbarrowItem(new FabricItemSettings().maxCount(1)));
	public static final Item EXPOSED_COPPER_WHEELBARROW = registerItem("exposed_copper_wheelbarrow",
			new ExposedCopperWheelbarrowItem(new FabricItemSettings().maxCount(1)));
	public static final Item WEATHERED_COPPER_WHEELBARROW = registerItem("weathered_copper_wheelbarrow",
			new WeatheredCopperWheelbarrowItem(new FabricItemSettings().maxCount(1)));
	public static final Item OXIDIZED_COPPER_WHEELBARROW = registerItem("oxidized_copper_wheelbarrow",
			new OxidizedCopperWheelbarrowItem(new FabricItemSettings().maxCount(1)));

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, new Identifier(Wheelbarrow.MOD_ID, name), item);
	}

	public static void registerModItems() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.addAfter(Items.TNT_MINECART, COPPER_WHEELBARROW);
			content.addAfter(Items.TNT_MINECART, EXPOSED_COPPER_WHEELBARROW);
			content.addAfter(Items.TNT_MINECART, WEATHERED_COPPER_WHEELBARROW);
			content.addAfter(Items.TNT_MINECART, OXIDIZED_COPPER_WHEELBARROW);
		});
	}
}

package com.asasinmode.wheelbarrow.item;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WheelbarrowItem extends Item {
	public WheelbarrowItem(Settings settings) {
		super(settings);
	}

	public static void register() {
		Item WHEELBARROW = Registry.register(
				Registries.ITEM, new Identifier(Wheelbarrow.MOD_ID, "wheelbarrow"),
				new WheelbarrowItem(new FabricItemSettings().maxCount(1)));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.addAfter(Items.TNT_MINECART, WHEELBARROW);
		});
	}
}

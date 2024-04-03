package com.asasinmode.wheelbarrow;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;

public class Keybinds {
	public static final KeyBinding yeet = new KeyBinding(
			"key." + Wheelbarrow.MOD_ID + ".yeet",
			Type.MOUSE,
			1,
			"key.categories." + Wheelbarrow.MOD_ID);

	public static void registerKeybinds() {
		KeyBindingHelper.registerKeyBinding(yeet);
	}
}

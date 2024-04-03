package com.asasinmode.wheelbarrow;

import org.lwjgl.glfw.GLFW;

import com.asasinmode.wheelbarrow.networking.ModMessages;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;

public class Keybinds {
	public static final KeyBinding yeet = new KeyBinding(
			"key." + Wheelbarrow.MOD_ID + ".yeet",
			GLFW.GLFW_KEY_Z,
			"key.categories." + Wheelbarrow.MOD_ID);

	public static void registerKeybinds() {
		KeyBindingHelper.registerKeyBinding(yeet);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (yeet.wasPressed()) {
				ClientPlayNetworking.send(ModMessages.YEET_ID, PacketByteBufs.create());
			}
		});
	}
}

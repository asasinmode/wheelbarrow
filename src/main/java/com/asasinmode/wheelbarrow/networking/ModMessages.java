package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModMessages {
	public static final Identifier INFORM_YEET_KEYBIND_ID = new Identifier(Wheelbarrow.MOD_ID, "inform_yeet_keybind");
	public static final Identifier YEET_ID = new Identifier(Wheelbarrow.MOD_ID, "yeet");

	public static void registerPacketsC2SPackets() {
		ServerPlayNetworking.registerGlobalReceiver(YEET_ID, YeetC2SPacket::receive);
	}

	public static void registerPacketsS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(INFORM_YEET_KEYBIND_ID, InformYeetKeybindS2CPacket::receive);
	}
}

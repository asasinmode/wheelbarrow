package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.networking.ModMessages;
import com.asasinmode.wheelbarrow.render.WheelbarrowEntityRenderer;
import com.asasinmode.wheelbarrow.networking.client.InformYeetKeybindS2CPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class WheelbarrowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.WHEELBARROW, (context) -> {
			return new WheelbarrowEntityRenderer(context);
		});
		Keybinds.registerKeybinds();
		registerPacketsS2CPackets();
	}

	private static void registerPacketsS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(ModMessages.INFORM_YEET_KEYBIND_ID,
				InformYeetKeybindS2CPacket::receive);
	}
}

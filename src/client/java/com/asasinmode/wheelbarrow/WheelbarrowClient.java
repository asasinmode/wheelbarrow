package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.networking.server.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.render.WheelbarrowEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.text.Text;

public class WheelbarrowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.WHEELBARROW, (context) -> {
			return new WheelbarrowEntityRenderer(context);
		});
		Keybinds.registerKeybinds();
		registerS2CPackets();
	}

	private static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(InformYeetKeybindS2CPacket.PACKET_TYPE,
				(packet, player, sender) -> {
					// TODO this gets overriden by dismount message
					player.sendMessage(
							Text.translatable("key." + Wheelbarrow.MOD_ID + ".yeetTooltip", Keybinds.yeet.getBoundKeyLocalizedText()),
							true);
				});
	}
}

package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.networking.InformYeetKeybindS2CPacket;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;
import com.asasinmode.wheelbarrow.render.WheelbarrowEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.text.Text;

public class WheelbarrowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.WHEELBARROW, (context) -> {
			return new WheelbarrowEntityRenderer(context);
		});
		Keybinds.registerKeybinds();
		registerCodecs();
		registerS2CPackets();
	}

	private static void registerCodecs() {
		PayloadTypeRegistry.playS2C().register(InformYeetKeybindS2CPacket.PACKET_ID,
				InformYeetKeybindS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(YeetC2SPacket.PACKET_ID, YeetC2SPacket.PACKET_CODEC);
	}

	private static void registerS2CPackets() {
		ClientPlayNetworking.registerGlobalReceiver(InformYeetKeybindS2CPacket.PACKET_ID,
				(payload, context) -> {
					// TODO this gets overriden by dismount message
					context.player().sendMessage(
							Text.translatable("key." + Wheelbarrow.MOD_ID + ".yeetTooltip", Keybinds.yeet.getBoundKeyLocalizedText()),
							true);
				});
	}
}

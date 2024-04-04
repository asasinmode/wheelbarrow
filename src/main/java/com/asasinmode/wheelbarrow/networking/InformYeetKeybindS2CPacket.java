package com.asasinmode.wheelbarrow.networking;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class InformYeetKeybindS2CPacket {
	public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
			PacketSender responseSender) {
		System.out.println("got inform packet");
		// ((PlayerEntity) controllingPassenger).sendMessage(Text.translatable("key." +
		// Wheelbarrow.MOD_ID + ".yeetTooltip", "Z"), true);
	}
}

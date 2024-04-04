package com.asasinmode.wheelbarrow.networking.client;

import com.asasinmode.wheelbarrow.Keybinds;
import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class InformYeetKeybindS2CPacket {
	public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
			PacketSender responseSender) {
		client.player.sendMessage(Text.translatable("key." +
				Wheelbarrow.MOD_ID + ".yeetTooltip", Keybinds.yeet.getBoundKeyLocalizedText()), true);
	}
}

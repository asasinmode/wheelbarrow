package com.asasinmode.wheelbarrow.networking.server;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class YeetC2SPacket {
	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {
		if (player.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == player) {
			wheelbarrow.yeetLastPassenger();
		}
	}
}

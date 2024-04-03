package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.networking.packets.YeetC2SPacket;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModMessages {
	public static final Identifier YEET_ID = new Identifier(Wheelbarrow.MOD_ID, "yeet");

	public static void registerC2SPackets() {
		ServerPlayNetworking.registerGlobalReceiver(YEET_ID, YeetC2SPacket::receive);
	}
}

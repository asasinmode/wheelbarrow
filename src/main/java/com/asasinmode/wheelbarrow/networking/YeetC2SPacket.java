package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class YeetC2SPacket implements CustomPayload {
	public static final YeetC2SPacket INSTANCE = new YeetC2SPacket();
	public static final CustomPayload.Id<YeetC2SPacket> PACKET_ID = new CustomPayload.Id<>(
			new Identifier(Wheelbarrow.MOD_ID, "yeet"));
	public static final PacketCodec<PacketByteBuf, YeetC2SPacket> PACKET_CODEC = PacketCodec
			.unit(INSTANCE);

	public YeetC2SPacket() {
	}

	public CustomPayload.Id<YeetC2SPacket> getId() {
		return PACKET_ID;
	}
}

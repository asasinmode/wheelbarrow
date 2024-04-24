package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class InformYeetKeybindS2CPacket implements CustomPayload {
	public static final InformYeetKeybindS2CPacket INSTANCE = new InformYeetKeybindS2CPacket();
	public static final CustomPayload.Id<InformYeetKeybindS2CPacket> PACKET_ID = new CustomPayload.Id<>(
			new Identifier(Wheelbarrow.MOD_ID, "inform_yeet_keybind"));
	public static final PacketCodec<PacketByteBuf, InformYeetKeybindS2CPacket> PACKET_CODEC = PacketCodec
			.unit(INSTANCE);

	public InformYeetKeybindS2CPacket() {
	}

	public CustomPayload.Id<InformYeetKeybindS2CPacket> getId() {
		return PACKET_ID;
	}
}

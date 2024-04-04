package com.asasinmode.wheelbarrow.networking.client;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class YeetC2SPacket implements FabricPacket {
	public static final Identifier ID = new Identifier(Wheelbarrow.MOD_ID, "yeet");
	public static final PacketType<YeetC2SPacket> PACKET_TYPE = PacketType.create(ID,
			YeetC2SPacket::new);

	public YeetC2SPacket(PacketByteBuf buf) {
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return PACKET_TYPE;
	}
}

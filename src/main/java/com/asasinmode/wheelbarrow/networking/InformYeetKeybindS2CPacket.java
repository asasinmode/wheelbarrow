package com.asasinmode.wheelbarrow.networking;

import com.asasinmode.wheelbarrow.Wheelbarrow;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class InformYeetKeybindS2CPacket implements FabricPacket {
	public static final Identifier ID = new Identifier(Wheelbarrow.MOD_ID, "inform_yeet_keybind");
	public static final PacketType<InformYeetKeybindS2CPacket> PACKET_TYPE = PacketType.create(ID,
			InformYeetKeybindS2CPacket::new);

	public InformYeetKeybindS2CPacket(PacketByteBuf buf) {
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return PACKET_TYPE;
	}
}

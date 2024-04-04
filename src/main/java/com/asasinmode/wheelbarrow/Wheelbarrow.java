package com.asasinmode.wheelbarrow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.item.ModItems;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class Wheelbarrow implements ModInitializer {
	public static final String MOD_ID = "wheelbarrow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Config CONFIG = new Config();

	@Override
	public void onInitialize() {
		CONFIG = Config.load();
		ModItems.registerModItems();
		ModEntities.registerModEntities();
		registerPacketsC2SPackets();
	}

	private static void registerPacketsC2SPackets() {
		ServerPlayNetworking.registerGlobalReceiver(YeetC2SPacket.PACKET_TYPE, (packet, player, responseSender) -> {
			if (player.getVehicle() instanceof WheelbarrowEntity wheelbarrow
					&& wheelbarrow.getControllingPassenger() == player) {
				wheelbarrow.yeetLastPassenger();
			}
		});
	}
}

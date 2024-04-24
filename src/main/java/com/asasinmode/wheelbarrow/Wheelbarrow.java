package com.asasinmode.wheelbarrow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.item.ModItems;
import com.asasinmode.wheelbarrow.networking.YeetC2SPacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
		registerC2SPackets();
	}

	private static void registerC2SPackets() {
		PayloadTypeRegistry.playC2S().register(YeetC2SPacket.PACKET_ID, YeetC2SPacket.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(YeetC2SPacket.PACKET_ID, (payload, context) -> {
			if (context.player().getVehicle() instanceof WheelbarrowEntity wheelbarrow
					&& wheelbarrow.getControllingPassenger() == context.player()) {
				wheelbarrow.yeetLastPassenger();
			}
		});
	}
}

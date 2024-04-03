package com.asasinmode.wheelbarrow;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.render.WheelbarrowEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class WheelbarrowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.WHEELBARROW, (context) -> {
			return new WheelbarrowEntityRenderer(context);
		});
		Keybinds.registerKeybinds();
	}
}

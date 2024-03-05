package com.asasinmode.wheelbarrow.render;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.layer.ModModelLayers;
import com.asasinmode.wheelbarrow.model.WheelbarrowEntityModel;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class WheelbarrowEntityRenderer extends EntityRenderer<WheelbarrowEntity> {
	private static final Identifier TEXTURE = new Identifier(Wheelbarrow.MOD_ID,
			"textures/entity/wheelbarrow.png");

	public WheelbarrowEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.shadowRadius = 0.5f;
		this.model = new WheelbarrowEntityModel(context)
	}

	@Override
	public Identifier getTexture(WheelbarrowEntity entity) {
		return TEXTURE;
	}
}

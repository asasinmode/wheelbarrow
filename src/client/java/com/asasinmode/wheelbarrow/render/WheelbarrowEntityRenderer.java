package com.asasinmode.wheelbarrow.render;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.model.WheelbarrowEntityModel;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WheelbarrowEntityRenderer extends EntityRenderer<WheelbarrowEntity> {
	private static final Identifier TEXTURE = new Identifier(Wheelbarrow.MOD_ID,
			"textures/entity/wheelbarrow.png");
	private final WheelbarrowEntityModel model;

	public WheelbarrowEntityRenderer(EntityRendererFactory.Context context) {
		super(context);

		this.shadowRadius = 0.7f;
		this.model = new WheelbarrowEntityModel(WheelbarrowEntityModel.getTexturedModelData().createModel());
	}

	@Override
	public Identifier getTexture(WheelbarrowEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(WheelbarrowEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
		matrices.translate(0, -1.5, 0);

		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity))), light,
				OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
}

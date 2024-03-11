package com.asasinmode.wheelbarrow.render;

import org.joml.Quaternionf;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class WheelbarrowEntityRenderer extends EntityRenderer<WheelbarrowEntity> {
	private static final Identifier TEXTURE = new Identifier(Wheelbarrow.MOD_ID,
			"textures/entity/copper_wheelbarrow.png");
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
	public void render(WheelbarrowEntity entity, float yaw, float tickDelta, MatrixStack matrixStack,
			VertexConsumerProvider vertexConsumers, int light) {
		matrixStack.push();

		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));

		float damageWobbleTicks = (float) entity.getDamageWobbleTicks() - tickDelta;
		float damageWobbleStrength = entity.getDamageWobbleStrength() - tickDelta;
		if (damageWobbleStrength < 0.0F) {
			damageWobbleStrength = 0.0F;
		}

		if (damageWobbleTicks > 0.0F) {
			matrixStack.multiply(RotationAxis.POSITIVE_Z
					.rotationDegrees(MathHelper.sin(damageWobbleTicks) * damageWobbleTicks
							* damageWobbleStrength / 15.0F * (float) entity.getDamageWobbleSide()));
		}

		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
		matrixStack.translate(0, -1.5, 0);

		float bubbleWobble = entity.interpolateBubbleWobble(tickDelta);
		if (!MathHelper.approximatelyEquals(bubbleWobble, 0.0F)) {
			matrixStack.multiply(
					(new Quaternionf()).setAngleAxis(entity.interpolateBubbleWobble(tickDelta) * 0.017453292F, 1.0F, 0.0F, 1.0F));
		}

		model.render(matrixStack, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity))), light,
				OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

		matrixStack.pop();
		super.render(entity, yaw, tickDelta, matrixStack, vertexConsumers, light);
	}
}

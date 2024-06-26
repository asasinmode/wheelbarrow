package com.asasinmode.wheelbarrow.render;

import java.util.Map;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.asasinmode.wheelbarrow.model.WheelbarrowEntityModel;
import com.google.common.collect.Maps;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class WheelbarrowEntityRenderer extends EntityRenderer<WheelbarrowEntity> {
	private static final Map<WheelbarrowEntity.Type, Identifier> OXIDATION_LEVEL_TO_TEXTURE = Util
			.make(Maps.newEnumMap(WheelbarrowEntity.Type.class), (map) -> {
				map.put(WheelbarrowEntity.Type.COPPER,
						Identifier.of(Wheelbarrow.MOD_ID, "textures/entity/copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.EXPOSED,
						Identifier.of(Wheelbarrow.MOD_ID, "textures/entity/exposed_copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.WEATHERED,
						Identifier.of(Wheelbarrow.MOD_ID, "textures/entity/weathered_copper_wheelbarrow.png"));
				map.put(WheelbarrowEntity.Type.OXIDIZED,
						Identifier.of(Wheelbarrow.MOD_ID, "textures/entity/oxidized_copper_wheelbarrow.png"));
			});

	private final WheelbarrowEntityModel model;

	public WheelbarrowEntityRenderer(EntityRendererFactory.Context context) {
		super(context);

		this.shadowRadius = 0.7f;
		this.model = new WheelbarrowEntityModel(WheelbarrowEntityModel.getTexturedModelData().createModel());
	}

	@Override
	public Identifier getTexture(WheelbarrowEntity entity) {
		return OXIDATION_LEVEL_TO_TEXTURE.get(entity.getOxidationLevel());
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

		float ageInTicks = this.getAnimationProgress(entity, tickDelta);
		float limbSwing = entity.limbAnimator.getPos(tickDelta);
		float limbSwingAmount = entity.limbAnimator.getSpeed(tickDelta);

		if (limbSwingAmount > 1.0f) {
			limbSwingAmount = 1.0f;
		}

		this.model.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, 0.0f, 0.0f);

		this.model.render(matrixStack, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity))), light,
				OverlayTexture.DEFAULT_UV);

		matrixStack.pop();
		super.render(entity, yaw, tickDelta, matrixStack, vertexConsumers, light);
	}

	protected float getAnimationProgress(WheelbarrowEntity entity, float tickDelta) {
		return (float) entity.age + tickDelta;
	}
}

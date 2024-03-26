package com.asasinmode.wheelbarrow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
		extends EntityRenderer<T>
		implements FeatureRendererContext<T, M> {

	public LivingEntityRendererMixin(EntityRendererFactory.Context ctx, M model, float shadowRadius) {
		super(ctx);
	}

	// todo if wheelbarrow values don't work then use that to modify check and then
	// multiply the values
	// @ModifyExpressionValue(method =
	// "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
	// at = @At(value = "INVOKE", target =
	// "net/minecraft/entity/LivingEntity.hasVehicle()Z", ordinal = 2))
	private boolean modifyRenderLimbAnimationCheck(boolean original, T livingEntity, float yaw, float tickDelta,
			MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		// I want to animate player walking when they are the controlling passenger of
		// the wheelbarrow. Output is negated, hence false
		if (livingEntity instanceof PlayerEntity && livingEntity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == livingEntity) {
			return false;
		}

		return original;
	}

	@ModifyArgs(method = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/model/EntityModel.setAngles(Lnet/minecraft/entity/Entity;FFFFF)V"))
	private void modifyRenderSetAnglesArguments(Args args, T livingEntity, float yaw, float tickDelta,
			MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		if (livingEntity instanceof PlayerEntity && livingEntity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == livingEntity) {
			float limbSwing = wheelbarrow.limbAnimator.getPos(tickDelta);
			float limbSwingAmount = wheelbarrow.limbAnimator.getSpeed(tickDelta);

			if (limbSwingAmount > 1.0f) {
				limbSwingAmount = 1.0f;
			}

			args.set(2, limbSwing);
			args.set(3, limbSwingAmount);
		}
	}
}

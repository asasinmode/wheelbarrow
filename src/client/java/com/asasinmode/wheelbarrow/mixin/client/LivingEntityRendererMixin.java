package com.asasinmode.wheelbarrow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

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

	@ModifyExpressionValue(method = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.hasVehicle()Z", ordinal = 2))
	private boolean modifyRenderLimbAnimationCheck(boolean original, T livingEntity, float yaw, float tickDelta,
			MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		// make the default implementation calculate the player walking values when they
		// are controlling a wheelbarrow. Output is negated, hence false
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
			float limbSwing = args.<Float>get(1) * 8.0f;
			float limbSwingAmount = args.<Float>get(2) * 8.0f;

			if (limbSwingAmount > 1.0f) {
				limbSwingAmount = 1.0f;
			}

			args.set(1, limbSwing);
			args.set(2, limbSwingAmount);
		}
	}
}

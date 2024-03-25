package com.asasinmode.wheelbarrow.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
		extends EntityRenderer<T>
		implements FeatureRendererContext<T, M> {

	public LivingEntityRendererMixin(EntityRendererFactory.Context ctx, M model, float shadowRadius) {
		super(ctx);
	}

	// here get the limbSwing and limbSwingAmount from the wheelbarrow after adding
	// the limbAnimator. See LivingEntity.updateLimbs, should be in tick/velocity
	// vicinity
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
			System.out.println("returning the thingy");
			return false;
		}

		return original;
	}
}

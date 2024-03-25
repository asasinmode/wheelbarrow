package com.asasinmode.wheelbarrow.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity, M extends EntityModel<T>> extends AnimalModel<T>
		implements ModelWithArms, ModelWithHead {
	@NotNull
	@Shadow
	protected abstract Iterable<ModelPart> getHeadParts();

	@NotNull
	@Shadow
	protected abstract Iterable<ModelPart> getBodyParts();

	@NotNull
	@Shadow
	public abstract ModelPart getHead();

	@NotNull
	@Shadow
	public abstract void setArmAngle(Arm arm, MatrixStack matrices);

	@ModifyExpressionValue(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;riding:Z", opcode = Opcodes.GETFIELD))
	private boolean modifySetAnglesRidingCheck(boolean original, T entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		Entity vehicle = entity.getVehicle();

		if (vehicle instanceof WheelbarrowEntity wheelbarrow) {
			Entity controllingPassenger = wheelbarrow.getControllingPassenger();
			return original && entity != controllingPassenger;
		}

		return original;
	}

	// @ModifyVariable(method =
	// "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
	// at = @At("HEAD"), ordinal = 0)
	// private float modifyLimbSwing(float limbSwing, T entity) {
	// return limbSwing;
	// }

	// todo here disable the arms swinging when controlling wheelbarrow
	@Inject(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void updateTravelledDistance(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef limbSwingRef,
			@Local(ordinal = 1) LocalFloatRef limbSwingAmountRef) {
		// if (entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow) {
		// if (wheelbarrow.getControllingPassenger() == entity) {
		// // System.out.println("wheelbarrow velocity" +
		// // wheelbarrow.getVelocity().horizontalLength());

		// limbSwingRef.set((float) wheelbarrow.getLimbSwingValue());
		// limbSwingAmountRef.set(0.5f);
		// }
		// }

		// if (limbSwingRef.get() > 0.0f || limbSwingAmountRef.get() > 0.0f) {
		// System.out.println("limbSwing" + limbSwingRef.get() + " amount " +
		// limbSwingAmountRef.get());
		// }
	}
}

package com.asasinmode.wheelbarrow.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

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

	@Redirect(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;pitch:F", opcode = Opcodes.PUTFIELD, ordinal = 4))
	private void updateRightArmPitch(ModelPart arm, float value, LivingEntity entity) {
		if (entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.0f;
		}
		arm.pitch = value;
	}

	@Redirect(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;pitch:F", opcode = Opcodes.PUTFIELD, ordinal = 5))
	private void updateLeftArmPitch(ModelPart arm, float value, LivingEntity entity) {
		if (entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.0f;
		}
		arm.pitch = value;
	}

	@Redirect(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
	private void updateRightArmRoll(ModelPart arm, float value, LivingEntity entity) {
		if (entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = 0.15f;
		}
		arm.roll = value;
	}

	@Redirect(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F", opcode = Opcodes.PUTFIELD, ordinal = 1))
	private void updateLeftArmRoll(ModelPart arm, float value, LivingEntity entity) {
		if (entity.getVehicle() instanceof WheelbarrowEntity wheelbarrow
				&& wheelbarrow.getControllingPassenger() == entity) {
			value = -0.15f;
		}
		arm.roll = value;
	}
}

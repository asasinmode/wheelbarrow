package com.asasinmode.wheelbarrow.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

	@Inject(method = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, CallbackInfo ci) {
		BipedEntityModel<PlayerEntity> thisObject = (BipedEntityModel<PlayerEntity>) (Object) this;
		Entity vehicle = entity.getVehicle();
		if (vehicle instanceof WheelbarrowEntity wheelbarrow) {
			Entity controllingPassenger = wheelbarrow.getControllingPassenger();
			thisObject.riding = thisObject.riding && entity != controllingPassenger;
		}
	}

	// todo figure out how to target expression/this.riding field access
	// @ModifyExpressionValue(method =
	// "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
	// at = @At(value = "FIELD", target =
	// "Lnet/minecraft/client/render/entity/model/EntityModel;riding:Z", opcode =
	// Opcodes.GETFIELD))
	// private boolean modifyRiding(boolean original) {
	// return original;
	// }
}

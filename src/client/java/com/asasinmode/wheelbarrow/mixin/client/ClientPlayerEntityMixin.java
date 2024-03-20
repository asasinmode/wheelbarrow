package com.asasinmode.wheelbarrow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.stat.StatHandler;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	public ClientPlayerEntityMixin(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler,
			StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
		super(world, networkHandler.getProfile());
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZF)V", shift = At.Shift.AFTER))
	private void wheelbarrowOnInputCall(CallbackInfo ci) {
		Entity riddenEntity = this.getVehicle();
		if (riddenEntity instanceof WheelbarrowEntity wheelbarrow) {
			ClientPlayerEntity thisObject = (ClientPlayerEntity) (Object) this;
			Input input = thisObject.input;
			wheelbarrow.setInputs(input.pressingLeft, input.pressingRight, input.pressingForward, input.pressingBack,
					thisObject.client.options.sprintKey.isPressed(), input.jumping);
		}
	}
}

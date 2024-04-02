package com.asasinmode.wheelbarrow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
	public AbstractClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super((World) world, world.getSpawnPos(), world.getSpawnAngle(), profile);
	}

	@Inject(method = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getFovMultiplier()F", at = @At("RETURN"), cancellable = true)
	private void updateGetFovMultiplier(CallbackInfoReturnable<Float> cir) {
		System.out.println("returning " + cir.getReturnValue());
	}
}

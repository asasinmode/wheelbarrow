package com.asasinmode.wheelbarrow.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.item.BoatItem;

public class WheelbarrowItem extends Item {
	// should be an oxidation wheelbarrow type
	final AbstractMinecartEntity.Type type;

	public WheelbarrowItem(Settings settings) {
		super(settings);
		this.type = AbstractMinecartEntity.Type.RIDEABLE;
	}

	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		ItemStack itemStack = context.getStack();
		Direction side = context.getSide();

		// dont place in center
		// dont place underwater
		if (world instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) world;

			if (side != Direction.UP) {
				return ActionResult.PASS;
			}

			AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(serverWorld,
					(double) blockPos.getX() + 0.5, (double) blockPos.up().getY(), (double) blockPos.getZ() + 0.5,
					this.type, itemStack, context.getPlayer());

			if (!world.isSpaceEmpty(abstractMinecartEntity, abstractMinecartEntity.getBoundingBox())) {
				return ActionResult.FAIL;
			}

			serverWorld.spawnEntity(abstractMinecartEntity);
			serverWorld.emitGameEvent(GameEvent.ENTITY_PLACE, blockPos,
					Emitter.of(context.getPlayer(), serverWorld.getBlockState(blockPos.down())));
		}

		itemStack.decrement(1);
		return ActionResult.success(world.isClient);
	}
}

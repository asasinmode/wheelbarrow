package com.asasinmode.wheelbarrow.item.custom;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;

public class WheelbarrowItem extends Item {
	private final WheelbarrowEntity.Type type;

	public WheelbarrowItem(WheelbarrowEntity.Type type, Settings settings) {
		super(settings);
		this.type = type;
	}

	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		Vec3d hitPos = context.getHitPos();
		ItemStack itemStack = context.getStack();
		Direction side = context.getSide();
		float userYaw = context.getPlayerYaw();

		if (side != Direction.UP) {
			return ActionResult.PASS;
		}

		if (world instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) world;

			WheelbarrowEntity wheelbarrowEntity;

			if (this.type == WheelbarrowEntity.Type.COPPER) {
				wheelbarrowEntity = WheelbarrowEntity.create(this.type, serverWorld,
						(double) hitPos.getX(), (double) hitPos.getY(), (double) hitPos.getZ(),
						itemStack, context.getPlayer(), true);
			} else {
				wheelbarrowEntity = WheelbarrowEntity.create(this.type, serverWorld,
						(double) hitPos.getX(), (double) hitPos.getY(), (double) hitPos.getZ(),
						itemStack, context.getPlayer());
			}

			if (!world.isSpaceEmpty(wheelbarrowEntity, wheelbarrowEntity.getBoundingBox())
					|| !world.getBlockState(blockPos.up()).isAir()) {
				return ActionResult.FAIL;
			}

			wheelbarrowEntity.setYaw(userYaw);
			serverWorld.spawnEntity(wheelbarrowEntity);
			serverWorld.emitGameEvent(GameEvent.ENTITY_PLACE, blockPos,
					Emitter.of(context.getPlayer(), serverWorld.getBlockState(blockPos)));
		}

		itemStack.decrement(1);
		return ActionResult.success(world.isClient);
	}
}

package com.asasinmode.wheelbarrow.entity.custom;

import java.util.Iterator;
import java.util.List;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraft.entity.vehicle.MinecartEntity;

public class WheelbarrowEntity extends Entity {
	private static final TrackedData<Integer> OXIDATION_LEVEL;
	private int lerpTicks;
	private float yawVelocity;
	private float velocityDecay;
	private double boatYaw;
	private double boatPitch;
	private double x;
	private double y;
	private double z;

	public WheelbarrowEntity(EntityType<? extends WheelbarrowEntity> entityType, World world) {
		super(entityType, world);
	}

	protected WheelbarrowEntity(World world, double x, double y, double z) {
		this(ModEntities.WHEELBARROW, world);
		this.setPosition(x, y, z);
		this.prevX = x;
		this.prevY = y;
		this.prevZ = z;
	}

	protected void initDataTracker() {
		this.dataTracker.startTracking(OXIDATION_LEVEL, Type.COPPER.ordinal());
	}

	static enum Type {
		COPPER("copper"),
		EXPOSED("exposed"),
		WEATHERED("weathered"),
		OXIDIZED("oxidized");

		private final String type;

		Type(String type) {
			this.type = type;
		}

		public String asString() {
			return type;
		}

		public static Type getType(String value) {
			for (Type enumConstant : Type.values()) {
				if (enumConstant.asString().equals(value)) {
					return enumConstant;
				}
			}
			throw new IllegalArgumentException("No enum constant with value: " + value);
		}

		public static Type getType(int ordinal) {
			if (ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			throw new IllegalArgumentException("No enum constant with ordinal value: " + ordinal);
		}
	}

	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putString("OxidationLevel", this.getOxidationLevel().asString());
	}

	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.contains("OxidationLevel", 8)) {
			this.setOxidationLevel(Type.getType(nbt.getString("OxidationLevel")));
		}
	}

	public void setOxidationLevel(Type type) {
		this.dataTracker.set(OXIDATION_LEVEL, type.ordinal());
	}

	public Type getOxidationLevel() {
		return Type.getType((Integer) this.dataTracker.get(OXIDATION_LEVEL));
	}

	public static WheelbarrowEntity create(ServerWorld world, double x, double y, double z, ItemStack stack,
			PlayerEntity player) {

		WheelbarrowEntity wheelbarrowEntity = new WheelbarrowEntity(world, x, y, z);

		EntityType.copier(world, stack, player).accept(wheelbarrowEntity);

		return (WheelbarrowEntity) wheelbarrowEntity;
	}

	public Item asItem() {
		Item item;

		switch (this.getOxidationLevel()) {
			case COPPER:
				item = ModItems.COPPER_WHEELBARROW;
				break;
			case EXPOSED:
				item = ModItems.EXPOSED_COPPER_WHEELBARROW;
				break;
			case WEATHERED:
				item = ModItems.WEATHERED_COPPER_WHEELBARROW;
				break;
			case OXIDIZED:
				item = ModItems.OXIDIZED_COPPER_WHEELBARROW;
				break;
			default:
				item = ModItems.COPPER_WHEELBARROW;
		}

		return item;
	}

	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player.shouldCancelInteraction()) {
			return ActionResult.PASS;
		} else if (!this.getWorld().isClient) {
			return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		} else {
			return ActionResult.SUCCESS;
		}
	}

	public boolean collidesWith(Entity other) {
		return canCollide(this, other);
	}

	public static boolean canCollide(Entity entity, Entity other) {
		return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
	}

	public boolean isCollidable() {
		return true;
	}

	public boolean isPushable() {
		return true;
	}

	// not sure what it does yet
	public void pushAwayFrom(Entity entity) {
		if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
			super.pushAwayFrom(entity);
		}
	}

	public boolean canHit() {
		return !this.isRemoved();
	}

	// maybe useful
	// public Direction getMovementDirection() {
	// return this.getHorizontalFacing().rotateYClockwise();
	// }

	protected Text getDefaultName() {
		return Text.translatable(this.asItem().getTranslationKey());
	}

	public ItemStack getPickBlockStack() {
		return new ItemStack(this.asItem());
	}

	protected int getMaxPassengers() {
		return 2;
	}

	public boolean canBeYoinked(Entity entity) {
		return entity.getWidth() < this.getWidth();
	}

	public void tick() {
		super.tick();

		this.updatePositionAndRotation();

		if (this.isLogicalSideForUpdatingMovement()) {
			this.updateVelocity();
			this.move(MovementType.SELF, this.getVelocity());
		} else {
			this.setVelocity(Vec3d.ZERO);
		}

		this.checkBlockCollision();

		// todo adjust expand
		List<Entity> list = this.getWorld().getOtherEntities(this,
				this.getBoundingBox().expand(0.5, -0.01, 0.5),
				EntityPredicates.canBePushedBy(this));

		if (!list.isEmpty()) {
			boolean canYoink = !this.getWorld().isClient && !(this.getControllingPassenger() instanceof PlayerEntity);

			Iterator<Entity> entitiesIterator = list.iterator();
			// check what happens without the loop
			while (true) {
				Entity entity;
				do {
					if (!entitiesIterator.hasNext()) {
						return;
					}

					entity = (Entity) entitiesIterator.next();
				} while (entity.hasPassenger(this));

				if (canYoink && this.getPassengerList().size() < this.getMaxPassengers() && !entity.hasVehicle()
						&& this.canBeYoinked(entity) && entity instanceof LivingEntity
						&& !(entity instanceof PlayerEntity)) {
					entity.startRiding(this);
				} else {
					this.pushAwayFrom(entity);
				}
			}
		}
	}

	private void updatePositionAndRotation() {
		if (this.isLogicalSideForUpdatingMovement()) {
			this.lerpTicks = 0;
			this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpTicks > 0) {
			this.lerpPosAndRotation(this.lerpTicks, this.x, this.y, this.z, this.boatYaw, this.boatPitch);
			--this.lerpTicks;
		}
	}

	private void updateVelocity() {
		this.velocityDecay = 0.9f;

		Vec3d vec3d = this.getVelocity();

		this.setVelocity(vec3d.x * (double) this.velocityDecay, vec3d.y, vec3d.z * (double) this.velocityDecay);
		this.yawVelocity *= this.velocityDecay;
	}

	static {
		OXIDATION_LEVEL = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
	}
}

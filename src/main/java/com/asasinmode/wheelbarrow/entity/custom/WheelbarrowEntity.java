package com.asasinmode.wheelbarrow.entity.custom;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class WheelbarrowEntity extends Entity {
	private static final TrackedData<Integer> OXIDATION_LEVEL;
	private int lerpTicks;
	private float yawVelocity;
	private double fallVelocity;
	private float velocityDecay;
	private double wheelbarrowYaw;
	private double wheelbarrowPitch;
	private double x;
	private double y;
	private double z;
	private double waterLevel;
	private float nearbySlipperiness;
	private Location location;
	private Location lastLocation;

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

	@Override
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

	static enum Location {
		ON_LAND,
		IN_AIR,
		IN_WATER,
		UNDER_WATER,
		UNDER_FLOWING_WATER;
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putString("OxidationLevel", this.getOxidationLevel().asString());
	}

	@Override
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

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (this.getWorld().isClient || this.isRemoved()) {
			return true;
		}

		if (this.isInvulnerableTo(source)) {
			return false;
		}

		this.scheduleVelocityUpdate();
		this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());

		boolean isInCreative = source.getAttacker() instanceof PlayerEntity
				&& ((PlayerEntity) source.getAttacker()).getAbilities().creativeMode;

		if (isInCreative && !this.shouldAlwaysKill(source)) {
			if (isInCreative) {
				this.discard();
			}
		} else {
			this.killAndDropSelf();
		}

		return true;
	}

	boolean shouldAlwaysKill(DamageSource source) {
		return false;
	}

	public void killAndDropSelf() {
		this.kill();
		if (this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(this.asItem());
			if (this.hasCustomName()) {
				itemStack.setCustomName(this.getCustomName());
			}

			this.dropStack(itemStack);
		}
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

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player.shouldCancelInteraction()) {
			return ActionResult.PASS;
		} else if (!this.getWorld().isClient) {
			return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		} else {
			return ActionResult.SUCCESS;
		}
	}

	@Override
	public boolean collidesWith(Entity other) {
		return canCollide(this, other);
	}

	public static boolean canCollide(Entity entity, Entity other) {
		return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public void pushAwayFrom(Entity entity) {
		if (entity.getBoundingBox().minY <= this.getBoundingBox().minY) {
			super.pushAwayFrom(entity);
		}
	}

	@Override
	public boolean canHit() {
		return !this.isRemoved();
	}

	// maybe useful
	// public Direction getMovementDirection() {
	// return this.getHorizontalFacing().rotateYClockwise();
	// }

	@Override
	protected Text getDefaultName() {
		return Text.translatable(this.asItem().getTranslationKey());
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(this.asItem());
	}

	protected int getMaxPassengers() {
		return 2;
	}

	public boolean canBeYoinked(Entity entity) {
		return entity.getWidth() < this.getWidth();
	}

	@Override
	public void tick() {
		this.lastLocation = this.location;
		this.location = this.checkLocation();

		super.tick();

		this.updatePositionAndRotation();

		if (this.isLogicalSideForUpdatingMovement()) {
			this.updateVelocity();
			this.move(MovementType.SELF, this.getVelocity());
		} else {
			this.setVelocity(Vec3d.ZERO);
		}

		this.checkBlockCollision();

		List<Entity> list = this.getWorld().getOtherEntities(this,
				this.getBoundingBox().expand(0.2, 0, 0.2),
				EntityPredicates.canBePushedBy(this));

		if (!list.isEmpty()) {
			Iterator<Entity> entitiesIterator = list.iterator();
			Entity entity;
			do {
				if (!entitiesIterator.hasNext()) {
					return;
				}

				entity = (Entity) entitiesIterator.next();
			} while (entity.hasPassenger(this));

			boolean canYoink = !this.getWorld().isClient && !(this.getControllingPassenger() instanceof PlayerEntity);

			if (canYoink && this.getPassengerList().size() < this.getMaxPassengers() && !entity.hasVehicle()
					&& this.canBeYoinked(entity) && entity instanceof LivingEntity
					&& !(entity instanceof PlayerEntity)) {
				entity.startRiding(this);
			} else {
				this.pushAwayFrom(entity);
			}
		}
	}

	private void updatePositionAndRotation() {
		if (this.isLogicalSideForUpdatingMovement()) {
			this.lerpTicks = 0;
			this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpTicks > 0) {
			this.lerpPosAndRotation(this.lerpTicks, this.x, this.y, this.z, this.wheelbarrowYaw, this.wheelbarrowPitch);
			--this.lerpTicks;
		}
	}

	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch,
			int interpolationSteps) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.wheelbarrowYaw = (double) yaw;
		this.wheelbarrowPitch = (double) pitch;
		this.lerpTicks = 10;
	}

	@Override
	public double getLerpTargetX() {
		return this.lerpTicks > 0 ? this.x : this.getX();
	}

	@Override
	public double getLerpTargetY() {
		return this.lerpTicks > 0 ? this.y : this.getY();
	}

	@Override
	public double getLerpTargetZ() {
		return this.lerpTicks > 0 ? this.z : this.getZ();
	}

	@Override
	public float getLerpTargetPitch() {
		return this.lerpTicks > 0 ? (float) this.wheelbarrowPitch : this.getPitch();
	}

	@Override
	public float getLerpTargetYaw() {
		return this.lerpTicks > 0 ? (float) this.wheelbarrowYaw : this.getYaw();
	}

	private void updateVelocity() {
		double yMod = this.hasNoGravity() ? 0.0 : -0.04;
		double f = 0.0;
		this.velocityDecay = 0.05F;

		if (this.lastLocation == Location.IN_AIR && this.location != Location.IN_AIR && this.location != Location.ON_LAND) {
			this.waterLevel = this.getBodyY(1.0);
			this.setPosition(this.getX(), (double) (this.getWaterHeightBelow() - this.getHeight()) + 0.101, this.getZ());
			this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
			this.fallVelocity = 0.0;
			this.location = Location.IN_WATER;
			return;
		}

		if (this.location == Location.IN_WATER) {
			f = (this.waterLevel - this.getY()) / (double) this.getHeight();
			this.velocityDecay = 0.9F;
		} else if (this.location == Location.UNDER_FLOWING_WATER) {
			yMod = -0.0007;
			this.velocityDecay = 0.9F;
		} else if (this.location == Location.UNDER_WATER) {
			f = 0.01;
			this.velocityDecay = 0.45F;
		} else if (this.location == Location.IN_AIR) {
			this.velocityDecay = 0.9F;
		} else if (this.location == Location.ON_LAND) {
			this.velocityDecay = this.nearbySlipperiness;
			if (this.getControllingPassenger() instanceof PlayerEntity) {
				this.nearbySlipperiness /= 2.0F;
			}
		}

		Vec3d vec3d = this.getVelocity();
		this.setVelocity(vec3d.x * (double) this.velocityDecay, vec3d.y + yMod, vec3d.z * (double) this.velocityDecay);
		this.yawVelocity *= this.velocityDecay;

		if (f > 0.0) {
			Vec3d vec3d2 = this.getVelocity();
			this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.0615) * 0.75, vec3d2.z);
		}
	}

	private Location checkLocation() {
		Location location = this.getUnderWaterLocation();
		if (location != null) {
			this.waterLevel = this.getBoundingBox().maxY;
			return location;
		} else if (this.checkWheelbarrowInWater()) {
			return Location.IN_WATER;
		} else {
			float f = this.getNearbySlipperiness();
			if (f > 0.0F) {
				this.nearbySlipperiness = f;
				return Location.ON_LAND;
			} else {
				return Location.IN_AIR;
			}
		}
	}

	public float getWaterHeightBelow() {
		Box box = this.getBoundingBox();
		int i = MathHelper.floor(box.minX);
		int j = MathHelper.ceil(box.maxX);
		int k = MathHelper.floor(box.maxY);
		int l = MathHelper.ceil(box.maxY - this.fallVelocity);
		int m = MathHelper.floor(box.minZ);
		int n = MathHelper.ceil(box.maxZ);
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		outer: for (int o = k; o < l; ++o) {
			float f = 0.0F;

			for (int p = i; p < j; ++p) {
				for (int q = m; q < n; ++q) {
					mutable.set(p, o, q);
					FluidState fluidState = this.getWorld().getFluidState(mutable);
					if (fluidState.isIn(FluidTags.WATER)) {
						f = Math.max(f, fluidState.getHeight(this.getWorld(), mutable));
					}

					if (f >= 1.0F) {
						continue outer;
					}
				}
			}

			if (f < 1.0F) {
				return (float) mutable.getY() + f;
			}
		}

		return (float) (l + 1);
	}

	public float getNearbySlipperiness() {
		Box box = this.getBoundingBox();
		Box box2 = new Box(box.minX, box.minY - 0.001, box.minZ, box.maxX, box.minY, box.maxZ);
		int i = MathHelper.floor(box2.minX) - 1;
		int j = MathHelper.ceil(box2.maxX) + 1;
		int k = MathHelper.floor(box2.minY) - 1;
		int l = MathHelper.ceil(box2.maxY) + 1;
		int m = MathHelper.floor(box2.minZ) - 1;
		int n = MathHelper.ceil(box2.maxZ) + 1;
		VoxelShape voxelShape = VoxelShapes.cuboid(box2);
		float f = 0.0F;
		int o = 0;
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int p = i; p < j; ++p) {
			for (int q = m; q < n; ++q) {
				int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
				if (r != 2) {
					for (int s = k; s < l; ++s) {
						if (r <= 0 || s != k && s != l - 1) {
							mutable.set(p, s, q);
							BlockState blockState = this.getWorld().getBlockState(mutable);
							if (!(blockState.getBlock() instanceof LilyPadBlock) && VoxelShapes.matchesAnywhere(
									blockState.getCollisionShape(this.getWorld(), mutable).offset((double) p, (double) s, (double) q),
									voxelShape, BooleanBiFunction.AND)) {
								f += blockState.getBlock().getSlipperiness();
								++o;
							}
						}
					}
				}
			}
		}

		return f / (float) o;
	}

	private boolean checkWheelbarrowInWater() {
		Box box = this.getBoundingBox();
		int i = MathHelper.floor(box.minX);
		int j = MathHelper.ceil(box.maxX);
		int k = MathHelper.floor(box.minY);
		int l = MathHelper.ceil(box.minY + 0.001);
		int m = MathHelper.floor(box.minZ);
		int n = MathHelper.ceil(box.maxZ);
		boolean rv = false;
		this.waterLevel = Double.MIN_VALUE;
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int o = i; o < j; ++o) {
			for (int p = k; p < l; ++p) {
				for (int q = m; q < n; ++q) {
					mutable.set(o, p, q);
					FluidState fluidState = this.getWorld().getFluidState(mutable);
					if (fluidState.isIn(FluidTags.WATER)) {
						float f = (float) p + fluidState.getHeight(this.getWorld(), mutable);
						this.waterLevel = Math.max((double) f, this.waterLevel);
						rv |= box.minY < (double) f;
					}
				}
			}
		}

		return rv;
	}

	@Nullable
	private Location getUnderWaterLocation() {
		Box box = this.getBoundingBox();
		double d = box.maxY + 0.001;
		int i = MathHelper.floor(box.minX);
		int j = MathHelper.ceil(box.maxX);
		int k = MathHelper.floor(box.maxY);
		int l = MathHelper.ceil(d);
		int m = MathHelper.floor(box.minZ);
		int n = MathHelper.ceil(box.maxZ);
		boolean isUnderWater = false;
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int o = i; o < j; ++o) {
			for (int p = k; p < l; ++p) {
				for (int q = m; q < n; ++q) {
					mutable.set(o, p, q);
					FluidState fluidState = this.getWorld().getFluidState(mutable);
					if (fluidState.isIn(FluidTags.WATER)
							&& d < (double) ((float) mutable.getY() + fluidState.getHeight(this.getWorld(), mutable))) {
						if (!fluidState.isStill()) {
							return Location.UNDER_FLOWING_WATER;
						}

						isUnderWater = true;
					}
				}
			}
		}

		return isUnderWater ? Location.UNDER_WATER : null;
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
		this.fallVelocity = this.getVelocity().y;

		if (this.hasVehicle()) {
			return;
		}

		if (onGround) {
			this.onLanding();
		} else if (!this.getWorld().getFluidState(this.getBlockPos().down()).isIn(FluidTags.WATER)
				&& heightDifference < 0.0) {
			this.fallDistance -= (float) heightDifference;
		}
	}

	static {
		OXIDATION_LEVEL = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
	}
}

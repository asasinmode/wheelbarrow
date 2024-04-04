package com.asasinmode.wheelbarrow.entity.custom;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;
import com.asasinmode.wheelbarrow.networking.ModMessages;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class WheelbarrowEntity extends VehicleEntity {
	private static final TrackedData<Integer> OXIDATION_LEVEL;
	private static final TrackedData<Boolean> IS_WAXED;
	public final LimbAnimator limbAnimator = new LimbAnimator();
	private int lerpTicks;
	private float velocityDecay;
	private double wheelbarrowYaw;
	private double wheelbarrowPitch;
	private double x;
	private double y;
	private double z;
	private float nearbySlipperiness;
	private Location location;
	private LivingEntity prevControllingPassenger;
	private float prevYawVelocity;
	private float yawVelocity;
	private boolean pressingLeft;
	private boolean pressingRight;
	private boolean pressingForward;
	private boolean pressingBack;
	private boolean sprintingPressed;
	private Entity passengerBeingYeeted = null;

	public WheelbarrowEntity(EntityType<? extends WheelbarrowEntity> entityType, World world) {
		super(entityType, world);
	}

	protected WheelbarrowEntity(World world, double x, double y, double z) {
		this(ModEntities.WHEELBARROW, world);
		this.setPosition(x, y, z);
		this.prevX = x;
		this.prevY = y;
		this.prevZ = z;
		this.setStepHeight(0.5f);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(OXIDATION_LEVEL, Type.COPPER.ordinal());
		this.dataTracker.startTracking(IS_WAXED, false);
	}

	public static enum Type {
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
		UNDER_WATER,
		UNDER_FLOWING_WATER;
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putString("OxidationLevel", this.getOxidationLevel().asString());
		nbt.putBoolean("IsWaxed", this.getIsWaxed());
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.contains("OxidationLevel", NbtElement.STRING_TYPE)) {
			this.setOxidationLevel(Type.getType(nbt.getString("OxidationLevel")));
		}
		if (nbt.contains("IsWaxed")) {
			this.setIsWaxed(nbt.getBoolean("IsWaxed"));
		}
	}

	public void setOxidationLevel(Type type) {
		this.dataTracker.set(OXIDATION_LEVEL, type.ordinal());
	}

	public void setOxidationLevel(int type) {
		this.dataTracker.set(OXIDATION_LEVEL, type);
	}

	public Type getOxidationLevel() {
		return Type.getType((Integer) this.dataTracker.get(OXIDATION_LEVEL));
	}

	public void setIsWaxed(boolean value) {
		this.dataTracker.set(IS_WAXED, value);
	}

	public boolean getIsWaxed() {
		return this.dataTracker.get(IS_WAXED);
	}

	public static WheelbarrowEntity create(Type type, ServerWorld world, double x, double y, double z, ItemStack stack,
			PlayerEntity player) {

		WheelbarrowEntity wheelbarrowEntity = new WheelbarrowEntity(world, x, y, z);
		wheelbarrowEntity.setOxidationLevel(type);

		EntityType.copier(world, stack, player).accept(wheelbarrowEntity);

		return (WheelbarrowEntity) wheelbarrowEntity;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE)) {
			return false;
		}

		return super.damage(source, amount);
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
			ItemStack itemStack = player.getStackInHand(hand);
			Type oxidationLevel = this.getOxidationLevel();
			boolean isWaxed = this.getIsWaxed();

			if (itemStack.isIn(ItemTags.AXES)) {
				if (isWaxed) {
					setIsWaxed(false);
					this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0f,
							1.0f);
					this.spawnParticles(ParticleTypes.WAX_OFF, (ServerWorld) this.getWorld());

					return ActionResult.SUCCESS;
				}
				if (oxidationLevel == Type.COPPER) {
					return ActionResult.CONSUME;
				}

				this.setOxidationLevel(oxidationLevel.ordinal() - 1);
				itemStack.damage(1, player, playerx -> playerx.sendToolBreakStatus(hand));
				this.getWorld().playSound(null, this.getBlockPos(),
						SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.SCRAPE, (ServerWorld) this.getWorld());

				return ActionResult.CONSUME;
			} else if (itemStack.isOf(Items.HONEYCOMB)) {
				if (isWaxed) {
					return ActionResult.CONSUME;
				}
				this.setIsWaxed(true);
				itemStack.decrement(1);
				this.getWorld().playSound(null, this.getBlockPos(),
						SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS,
						1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.WAX_ON, (ServerWorld) this.getWorld());
				return ActionResult.SUCCESS;
			}

			return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
		Type oxidationLevel = this.getOxidationLevel();
		if (!this.getIsWaxed() && oxidationLevel != Type.COPPER) {
			this.setOxidationLevel(oxidationLevel.ordinal() - 1);
		}
	}

	@Override
	public void animateDamage(float yaw) {
		this.setDamageWobbleSide(-this.getDamageWobbleSide());
		this.setDamageWobbleTicks(10);
		this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0f);
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

	@Override
	public Direction getMovementDirection() {
		return this.getHorizontalFacing();
	}

	@Override
	protected Text getDefaultName() {
		return Text.translatable(this.asItem().getTranslationKey());
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(this.asItem());
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengerList().size() < this.getMaxPassengers();
	}

	protected int getMaxPassengers() {
		return Wheelbarrow.CONFIG.getMaxPassengers() + 1;
	}

	@Override
	@Nullable
	public LivingEntity getControllingPassenger() {
		Entity entity = this.getFirstPassenger();
		return entity instanceof LivingEntity ? (LivingEntity) entity : super.getControllingPassenger();
	}

	public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack,
			boolean sprinting, boolean jumping) {
		this.pressingLeft = pressingLeft;
		this.pressingRight = pressingRight;
		this.pressingForward = pressingForward;
		this.pressingBack = pressingBack;
		this.sprintingPressed = sprinting;
		if (!pressingForward) {
			this.setSprinting(false);
		} else if (sprinting) {
			this.setSprinting(true);
		}
	}

	private void steer() {
		if (!(this.getControllingPassenger() instanceof PlayerEntity playerEntity)) {
			return;
		}

		if (this.pressingLeft) {
			this.yawVelocity -= 1.5f;
			if (this.yawVelocity <= -5.0f) {
				this.yawVelocity = -5.0f;
			}
		}
		if (this.pressingRight) {
			this.yawVelocity += 1.5f;
			if (this.yawVelocity >= 5.0f) {
				this.yawVelocity = 5.0f;
			}
		}

		this.setYaw(this.getYaw() + this.yawVelocity);

		float velocity = 0.0f;
		if (this.pressingForward) {
			velocity = 0.07f;
		}
		if (this.pressingBack) {
			velocity = -0.07f;
		}

		// get player base walk speed diff, for example from
		// sprinting/swiftness/slowness
		float playerVelocityMultiplier = ((float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
				- playerEntity.getAbilities().getWalkSpeed()) * 3 + 1.0f;

		if (playerVelocityMultiplier > 1.0f) {
			// when sprinting this is around 0.09f velocity
			playerVelocityMultiplier += 0.195f;
		}

		velocity *= playerVelocityMultiplier;

		this.updateVelocity(1.0f, new Vec3d(0.0f, 0.0f, velocity));
	}

	@Override
	public boolean isSprinting() {
		if (this.getControllingPassenger() instanceof PlayerEntity playerEntity) {
			return playerEntity.isSprinting();
		}
		return super.isSprinting();
	}

	@Override
	public void setSprinting(boolean sprinting) {
		for (Entity entity : this.getPassengerList()) {
			if (entity instanceof PlayerEntity playerEntity) {
				playerEntity.setSprinting(sprinting);
			}
		}
		super.setSprinting(sprinting);
	}

	protected void updateLimbs(float posDelta) {
		// whatever makes wheel turning look good
		float speed = Math.min(posDelta * 2.0f, 1.0f);
		// set speed first so that there is no spin after stop
		this.limbAnimator.setSpeed(speed);
		this.limbAnimator.updateLimbs(speed, 1.0f);
	}

	// up to horse/iron golem/spider
	public boolean canBeYoinked(Entity entity) {
		return entity.getWidth() <= 1.4;
	}

	@Override
	public void tick() {
		boolean isServer = !this.getWorld().isClient;
		this.location = this.checkLocation();

		if (this.getDamageWobbleTicks() > 0) {
			this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
		}

		if (this.getDamageWobbleStrength() > 0.0f) {
			this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
		}

		super.tick();

		this.updatePositionAndRotation();

		if (this.isLogicalSideForUpdatingMovement()) {
			this.updateVelocity();
			if (!isServer) {
				this.steer();
			}
			this.move(MovementType.SELF, this.getVelocity());
		} else {
			this.setVelocity(Vec3d.ZERO);
		}

		double yawRad = Math.toRadians(this.getYaw());
		double yawSin = Math.sin(yawRad);
		double yawCos = Math.cos(yawRad);
		double deltaX = this.getX() - this.prevX;
		double deltaZ = this.getZ() - this.prevZ;
		double movementX = deltaX * yawCos + deltaZ * yawSin;
		double movementZ = -deltaX * yawSin + deltaZ * yawCos;
		int direction = Math.atan2(movementZ, movementX) >= 0.0f ? 1 : -1;
		float posDelta = (float) MathHelper.hypot(deltaX, deltaZ);

		this.updateLimbs(posDelta * direction);

		this.checkBlockCollision();

		LivingEntity controllingPassenger = this.getControllingPassenger();
		this.prevControllingPassenger = controllingPassenger;

		boolean isControlledByPlayer = controllingPassenger instanceof PlayerEntity;

		List<Entity> list = this.getWorld().getOtherEntities(this,
				this.getBoundingBox().expand(0.2, 0.1, 0.2),
				(entity) -> !entity.hasPassenger(this) // wheelbarrow not passenger of
						&& !this.hasPassenger(entity) // entity not passenger of wheelbarrow
						&& EntityPredicates.canBePushedBy(this).test(entity));

		for (Entity entity : list) {
			boolean canYoink = isServer
					&& isControlledByPlayer
					&& this.getPassengerList().size() < this.getMaxPassengers()
					&& !entity.hasVehicle()
					&& entity instanceof LivingEntity
					&& !(entity instanceof PlayerEntity)
					&& this.canBeYoinked(entity);

			if (canYoink) {
				entity.startRiding(this);
			} else {
				this.pushAwayFrom(entity);
			}
		}

		Type oxidationLevel = this.getOxidationLevel();
		if (isServer && oxidationLevel != Type.OXIDIZED && !this.getIsWaxed()) {
			int randomTickSpeed = this.getWorld().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
			Random random = this.getWorld().getRandom();
			// once a day, same as copper blocks + include random tick speed modifier
			// (1 / 24000 / defaultRandomTickSpeed) * randomTickSpeed
			if (random.nextFloat() < 0.0000139f * randomTickSpeed) {
				this.setOxidationLevel(oxidationLevel.ordinal() + 1);
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

	public float getPrevYawVelocity() {
		return this.prevYawVelocity;
	}

	public float getYawVelocity() {
		return this.yawVelocity;
	}

	private void updateVelocity() {
		double yMulitplier = 1.0;
		double yMod = this.hasNoGravity() ? 0.0 : -0.04;
		this.velocityDecay = 0.05f;

		if (this.location == Location.UNDER_FLOWING_WATER) {
			yMod = -0.03;
			this.velocityDecay = 0.3f;
		} else if (this.location == Location.UNDER_WATER) {
			this.velocityDecay = 0.3f;
			yMulitplier = 0.8;
		} else if (this.location == Location.IN_AIR) {
			this.velocityDecay = 0.5f;
		} else if (this.location == Location.ON_LAND) {
			this.velocityDecay = this.nearbySlipperiness;
			if (this.getControllingPassenger() instanceof PlayerEntity) {
				this.nearbySlipperiness /= 2.0f;
			}
		}

		Vec3d velocity = this.getVelocity();
		double y = (velocity.y + yMod) * yMulitplier;

		// terminal velocity so it doesn't accelerate infinitely
		if (y < -1.0) {
			y = -1.0;
		}

		this.prevYawVelocity = this.yawVelocity;
		this.yawVelocity *= 0.6;
		this.setVelocity(velocity.x * (double) this.velocityDecay, y, velocity.z * (double) this.velocityDecay);

		double velocityLength = this.getVelocity().horizontalLength();

		// bumped into something or stopped not sure if thats how you do it
		if (velocityLength <= 0.07) {
			if (this.isSprinting() && !this.sprintingPressed) {
				this.setSprinting(false);
			}
		}
	}

	private Location checkLocation() {
		Location location = this.getUnderWaterLocation();
		if (location != null) {
			return location;
		}

		float slipperiness = this.getNearbySlipperiness();
		if (slipperiness > 0.0f) {
			this.nearbySlipperiness = slipperiness;
			return Location.ON_LAND;
		} else {
			return Location.IN_AIR;
		}
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
		float f = 0.0f;
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

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);

		Entity controllingPassenger = this.getControllingPassenger();
		boolean isControlledByPlayer = controllingPassenger instanceof PlayerEntity;

		this.setStepHeight(isControlledByPlayer ? 1.0f : 0.5f);

		if (!controllingPassenger.getWorld().isClient && isControlledByPlayer && passenger != controllingPassenger
				&& this.getPassengerList().size() == 2) {
			ServerPlayNetworking.send((ServerPlayerEntity) controllingPassenger, ModMessages.INFORM_YEET_KEYBIND_ID,
					PacketByteBufs.empty());
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		this.setStepHeight(this.getControllingPassenger() instanceof PlayerEntity ? 1.0f : 0.5f);
	}

	@Override
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
		float zOffset = -0.8f;
		float yOffset = 0.6f;
		boolean isControllingPassenger = passenger == this.getControllingPassenger();
		boolean isPlayer = passenger instanceof PlayerEntity;

		if ((isControllingPassenger && isPlayer) || (!isPlayer && this.getPassengerList().indexOf(passenger) > 1)) {
			passenger.setPose(EntityPose.STANDING);
		} else {
			passenger.setPose(EntityPose.SITTING);
		}

		if (!isControllingPassenger || !isPlayer) {
			zOffset = 0.2f;
			yOffset = 0.4f;
			zOffset += Math.max((passenger.getWidth() - 1.0f) / 2.0f, 0.0f);
		}

		List<Entity> passengers = this.getPassengerList();
		int index = passengers.indexOf(passenger);
		if (index != 0) {
			int offset = this.getControllingPassenger() instanceof PlayerEntity ? 1 : 0;
			for (Entity entity : passengers.subList(offset, index)) {
				yOffset += entity.getHeight();
				System.out.println(
						"yOffset: " + yOffset + " ridingOffset: " + entity.getRidingOffset(this) + " entity: " + entity.getName());
			}
		}

		return new Vector3f(0.0f, yOffset, zOffset);
	}

	@Override
	protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
		super.updatePassengerPosition(passenger, positionUpdater);

		boolean isControllingPassenger = passenger == this.getControllingPassenger();
		boolean isPlayer = passenger instanceof PlayerEntity;

		if (isControllingPassenger && isPlayer) {
			passenger.setPose(EntityPose.STANDING);
		} else {
			passenger.setPose(EntityPose.SITTING);
		}

		if (passenger.getType().isIn(EntityTypeTags.CAN_TURN_IN_BOATS)) {
			return;
		}

		passenger.setYaw(passenger.getYaw() + this.yawVelocity);
		passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);

		this.clampPassengerYaw(passenger);
	}

	protected void clampPassengerYaw(Entity passenger) {
		if (passenger != this.getControllingPassenger() || !(passenger instanceof PlayerEntity)) {
			return;
		}
		passenger.setBodyYaw(this.getYaw());
		float f = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
		float g = MathHelper.clamp(f, -105.0f, 105.0f);
		passenger.prevYaw += g - f;
		passenger.setYaw(passenger.getYaw() + g - f);
		passenger.setHeadYaw(passenger.getYaw());
	}

	@Override
	public void onPassengerLookAround(Entity passenger) {
		this.clampPassengerYaw(passenger);
	}

	@Override
	public Vec3d updatePassengerForDismount(LivingEntity passenger) {
		// TODO yeeting player is wrong
		if (this.passengerBeingYeeted == passenger) {
			this.passengerBeingYeeted = null;

			float offset = 0.15f + (float) passenger.getWidth() * 0.1f;
			double yawRad = Math.toRadians(this.getYaw());
			double yawSin = -Math.sin(yawRad);
			double yawCos = Math.cos(yawRad);
			double largerSinCos = Math.max(Math.abs(yawSin), Math.abs(yawCos));
			double x = yawSin * offset / largerSinCos;
			double z = yawCos * offset / largerSinCos;

			// TODO add wheelbarrow's velocity
			// System.out
			// .println("yeeting passenger: " + this.getVelocity().length() + " other: " + "
			// thing: " + this.getVelocity());

			passenger.setVelocity(this.getVelocity().add(new Vec3d(x, 0.3, z)));

			return passenger.getPos();
		}

		double x, z, dismountYOffset;
		BlockPos blockPos;

		if (passenger == this.prevControllingPassenger) {
			float offset = -0.6f;
			double yawRad = Math.toRadians(this.getYaw());
			double yawSin = -Math.sin(yawRad);
			double yawCos = Math.cos(yawRad);
			double largerSinCos = Math.max(Math.abs(yawSin), Math.abs(yawCos));
			x = passenger.getX() + yawSin * offset / largerSinCos;
			z = passenger.getZ() + yawCos * offset / largerSinCos;

			blockPos = BlockPos.ofFloored(x, this.getY(), z);
			dismountYOffset = Math.max(0, this.getWorld().getDismountHeight(blockPos));
		} else {
			Vec3d offsetVec = WheelbarrowEntity.getPassengerDismountOffset(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO,
					passenger.getWidth(), passenger.getYaw());

			x = this.getX() + offsetVec.x;
			z = this.getZ() + offsetVec.z;
			blockPos = BlockPos.ofFloored(x, this.getBoundingBox().maxY, z);
			dismountYOffset = this.getWorld().getDismountHeight(blockPos);
		}

		return new Vec3d(x, blockPos.getY() + dismountYOffset, z);
	}

	// overriden because the default implementation makes it hover few blocks above
	// water surface
	@Override
	public void onBubbleColumnSurfaceCollision(boolean drag) {
	}

	private void spawnParticles(DefaultParticleType particleType, ServerWorld world) {
		double yawRad = Math.toRadians(this.getYaw());
		double yawSin = Math.sin(yawRad);
		double yawCos = Math.cos(yawRad);

		// inside
		for (int i = 0; i < 6; i++) {
			double particleX = random.nextDouble() * 0.4 * (double) (random.nextBoolean()
					? 1
					: -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// left
		for (int i = 0; i < 8; i++) {
			double particleX = 0.5;
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.625 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// right
		for (int i = 0; i < 8; i++) {
			double particleX = -0.5;
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + random.nextDouble() * 0.625 * (double) (random.nextBoolean() ? 1 : -1);
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// back
		for (int i = 0; i < 4; i++) {
			double particleX = random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = -0.55;
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}

		// front
		for (int i = 0; i < 4; i++) {
			double particleX = random.nextDouble() * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			double particleY = random.nextDouble() * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.8;
			this.spawnParticle(world, particleType, particleX, particleY, particleZ, yawSin, yawCos);
		}
	}

	private void spawnParticle(ServerWorld world, DefaultParticleType particleType, double x, double y, double z,
			double yawSin, double yawCos) {
		world.spawnParticles(particleType,
				this.getX() + x * yawCos - z * yawSin,
				this.getY() + 0.55 + y,
				this.getZ() + z * yawCos + x * yawSin,
				1,
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
				random.nextDouble() * 0.5);
	}

	public void yeetLastPassenger() {
		List<Entity> passengers = this.getPassengerList();
		if (passengers.size() <= 1) {
			return;
		}

		Entity passenger = passengers.get(passengers.size() - 1);
		this.passengerBeingYeeted = passenger;
		passenger.stopRiding();
	}

	static {
		OXIDATION_LEVEL = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
		IS_WAXED = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}
}

package com.asasinmode.wheelbarrow.entity.custom;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.ModEntities;
import com.asasinmode.wheelbarrow.item.ModItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class WheelbarrowEntity extends Entity {
	private static final TrackedData<Integer> OXIDATION_LEVEL;
	private static final TrackedData<Boolean> IS_WAXED;
	private static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS;
	private static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE;
	private static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH;
	private int lerpTicks;
	private float velocityDecay;
	private double wheelbarrowYaw;
	private double wheelbarrowPitch;
	private double x;
	private double y;
	private double z;
	private float nearbySlipperiness;
	private Location location;

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
		this.dataTracker.startTracking(IS_WAXED, false);
		this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
		this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
		this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);
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
		if (this.getWorld().isClient || this.isRemoved()) {
			return true;
		}

		if (this.isInvulnerableTo(source)
				|| source.isOf(DamageTypes.IN_FIRE)
				|| source.isOf(DamageTypes.ON_FIRE)) {
			return false;
		}

		this.scheduleVelocityUpdate();
		this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());

		this.setDamageWobbleSide(-this.getDamageWobbleSide());
		this.setDamageWobbleTicks(10);
		this.scheduleVelocityUpdate();
		this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
		this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());

		boolean isInCreative = source.getAttacker() instanceof PlayerEntity
				&& ((PlayerEntity) source.getAttacker()).getAbilities().creativeMode;

		if ((isInCreative || !(this.getDamageWobbleStrength() > 40.0F)) && !this.shouldAlwaysKill(source)) {
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
			ItemStack itemStack = player.getStackInHand(hand);
			Type oxidationLevel = this.getOxidationLevel();
			boolean isWaxed = this.getIsWaxed();

			if (itemStack.isIn(ItemTags.AXES)) {
				if (isWaxed) {
					setIsWaxed(false);
					this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0f,
							1.0f);
					this.spawnParticles(ParticleTypes.WAX_OFF, 32, (ServerWorld) this.getWorld());

					return ActionResult.SUCCESS;
				}
				if (oxidationLevel == Type.COPPER) {
					return ActionResult.CONSUME;
				}

				this.setOxidationLevel(oxidationLevel.ordinal() - 1);
				itemStack.damage(1, player, playerx -> playerx.sendToolBreakStatus(hand));
				this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.SCRAPE, 32, (ServerWorld) this.getWorld());

				return ActionResult.CONSUME;
			} else if (itemStack.isOf(Items.HONEYCOMB)) {
				if (isWaxed) {
					return ActionResult.CONSUME;
				}
				this.setIsWaxed(true);
				itemStack.decrement(1);
				this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS,
						1.0f,
						1.0f);
				this.spawnParticles(ParticleTypes.WAX_ON, 32, (ServerWorld) this.getWorld());
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
		this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0F);
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
		this.location = this.checkLocation();

		if (this.getDamageWobbleTicks() > 0) {
			this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
		}

		if (this.getDamageWobbleStrength() > 0.0F) {
			this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
		}

		super.tick();

		this.updatePositionAndRotation();

		if (this.isLogicalSideForUpdatingMovement()) {
			this.updateVelocity();
			this.move(MovementType.SELF, this.getVelocity());
		} else {
			this.setVelocity(Vec3d.ZERO);
		}

		this.checkBlockCollision();

		boolean isServer = !this.getWorld().isClient;
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

			boolean canYoink = isServer && !(this.getControllingPassenger() instanceof PlayerEntity);

			if (canYoink && this.getPassengerList().size() < this.getMaxPassengers() && !entity.hasVehicle()
					&& this.canBeYoinked(entity) && entity instanceof LivingEntity
					&& !(entity instanceof PlayerEntity)) {
				entity.startRiding(this);
			} else {
				this.pushAwayFrom(entity);
			}
		}

		Type oxidationLevel = this.getOxidationLevel();
		if (isServer && oxidationLevel != Type.OXIDIZED && !this.getIsWaxed()) {
			int randomTickSpeed = this.getWorld().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
			Random random = this.getWorld().getRandom();
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

	// todo check minecart's fall speed/decay under water
	private void updateVelocity() {
		double yMulitplier = 1.0;
		double yMod = this.hasNoGravity() ? 0.0 : -0.05;
		this.velocityDecay = 0.05F;

		if (this.location == Location.UNDER_FLOWING_WATER) {
			yMod = -0.03;
			this.velocityDecay = 0.9F;
		} else if (this.location == Location.UNDER_WATER) {
			this.velocityDecay = 0.45F;
			yMulitplier = 0.7;
		} else if (this.location == Location.IN_AIR) {
			this.velocityDecay = 0.9F;
		} else if (this.location == Location.ON_LAND) {
			this.velocityDecay = this.nearbySlipperiness;
			if (this.getControllingPassenger() instanceof PlayerEntity) {
				this.nearbySlipperiness /= 2.0F;
			}
		}

		Vec3d vec3d = this.getVelocity();
		this.setVelocity(vec3d.x * (double) this.velocityDecay, (vec3d.y + yMod) * yMulitplier,
				vec3d.z * (double) this.velocityDecay);
	}

	private Location checkLocation() {
		Location location = this.getUnderWaterLocation();
		if (location != null) {
			return location;
		}

		float slipperiness = this.getNearbySlipperiness();
		if (slipperiness > 0.0F) {
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
	public void onBubbleColumnSurfaceCollision(boolean drag) {
		if (drag) {
			Vec3d vec3d = this.getVelocity();

			this.setVelocity(vec3d.x, Math.max(-0.9, vec3d.y - 0.03), vec3d.z);
		}
	}

	private void spawnParticles(DefaultParticleType particleType, int count, ServerWorld world) {
		double yaw = Math.toRadians(this.getYaw());
		double cosYaw = Math.cos(yaw);
		double sinYaw = Math.sin(yaw);

		// todo make look better
		for (int i = 0; i < count; i++) {
			double xDouble = 1 - (random.nextGaussian() / 3);
			double yDouble = 1 - (random.nextGaussian() / 3);
			double zDouble = 1 - (random.nextGaussian() / 3);
			xDouble = xDouble < 0.5 ? 1 - xDouble : xDouble;
			yDouble = yDouble < 0.5 ? 1 - yDouble : yDouble;
			zDouble = zDouble < 0.5 ? 1 - zDouble : zDouble;

			double particleX = xDouble * 0.5 * (double) (random.nextBoolean() ? 1 : -1);
			double particleY = yDouble * 0.3125 * (double) (random.nextBoolean() ? 1 : -1);
			double particleZ = 0.2 + zDouble * 0.625 * (double) (random.nextBoolean() ? 1 : -1);

			world.spawnParticles(particleType,
					this.getX() + particleX * cosYaw - particleZ * sinYaw,
					this.getY() + 0.5 + particleY,
					this.getZ() + particleZ * cosYaw + particleX * sinYaw,
					1,
					random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
					random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
					random.nextDouble() / 10.0 * (double) (random.nextBoolean() ? 1 : -1),
					random.nextDouble() * 0.3);
		}
	}

	public void setDamageWobbleTicks(int damageWobbleTicks) {
		this.dataTracker.set(DAMAGE_WOBBLE_TICKS, damageWobbleTicks);
	}

	public void setDamageWobbleSide(int damageWobbleSide) {
		this.dataTracker.set(DAMAGE_WOBBLE_SIDE, damageWobbleSide);
	}

	public void setDamageWobbleStrength(float damageWobbleStrength) {
		this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, damageWobbleStrength);
	}

	public float getDamageWobbleStrength() {
		return (Float) this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
	}

	public int getDamageWobbleTicks() {
		return (Integer) this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
	}

	public int getDamageWobbleSide() {
		return (Integer) this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
	}

	static {
		OXIDATION_LEVEL = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
		IS_WAXED = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
		DAMAGE_WOBBLE_TICKS = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
		DAMAGE_WOBBLE_SIDE = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
		DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.FLOAT);
	}
}

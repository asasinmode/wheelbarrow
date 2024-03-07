package com.asasinmode.wheelbarrow.entity.custom;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.ModEntities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class WheelbarrowEntity extends Entity {
	private static final TrackedData<Integer> OXIDATION_LEVEL;

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

	static {
		OXIDATION_LEVEL = DataTracker.registerData(WheelbarrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
	}
}

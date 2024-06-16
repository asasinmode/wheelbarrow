package com.asasinmode.wheelbarrow.entity;

import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.asasinmode.wheelbarrow.entity.custom.*;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
	public static final EntityType<WheelbarrowEntity> WHEELBARROW = Registry.register(Registries.ENTITY_TYPE,
			Identifier.of(Wheelbarrow.MOD_ID, "wheelbarrow"),
			EntityType.Builder
					.create(WheelbarrowEntity::new, SpawnGroup.MISC)
					.dimensions(0.98f, 0.875f)
					.build());

	public static void registerModEntities() {
	}
}

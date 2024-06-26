package com.asasinmode.wheelbarrow.integration;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.asasinmode.wheelbarrow.Config;
import com.asasinmode.wheelbarrow.Wheelbarrow;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

// inspired by https://github.com/newhoryzon/farmers-delight-fabric/blob/master/src/main/java/com/nhoryzon/mc/farmersdelight/integration/modmenu/FarmersDelightModMenu.java
public class WheelbarrowModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return this::buildConfigScreen;
	}

	private Screen buildConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setSavingRunnable(() -> Config.save(Wheelbarrow.CONFIG))
				.setTitle(Text.literal("Wheelbarrow Config"));

		ConfigCategory category = builder.getOrCreateCategory(Text.literal("Default"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		category.addEntry(createEntry(
				entryBuilder,
				"maxPassengers",
				() -> Wheelbarrow.CONFIG.getMaxPassengers(),
				newValue -> Wheelbarrow.CONFIG.setMaxPassengers(newValue),
				1));

		return builder.build();
	}

	private TooltipListEntry<Integer> createEntry(ConfigEntryBuilder builder, String titleTranslationKey,
			Supplier<Integer> current, Consumer<Integer> saver, Integer defaultValue) {
		return builder
				.startIntField(Text.translatable("config." + Wheelbarrow.MOD_ID + "." + titleTranslationKey), current.get())
				.setSaveConsumer(saver)
				.setDefaultValue(defaultValue)
				.build();
	}
}

package com.smanzana.nostrummagica.world.dungeon;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Taken almost exactly from https://github.com/Vazkii/Botania/blob/e38556d265fcf43273c99ea1299a35400bf0c405/src/main/java/vazkii/botania/common/core/loot/LootHandler.java
public final class NostrumLootHandler {

	private static final List<String> TABLES = ImmutableList.of(
			"inject/abandoned_mineshaft", "inject/desert_pyramid",
			"inject/nether_bridge", "inject/igloo_chest",
			"inject/jungle_temple", "inject/simple_dungeon",
			"inject/stronghold_corridor", "inject/end_city_treasure",
			"inject/village_blacksmith"
			);

	public NostrumLootHandler() {
		MinecraftForge.EVENT_BUS.register(this);
		for (String s : TABLES) {
			LootTableList.register(new ResourceLocation(NostrumMagica.MODID, s));
		}
	}

	@SubscribeEvent
	public void lootLoad(LootTableLoadEvent evt) {
		String prefix = "minecraft:chests/";
		String name = evt.getName().toString();

		if (name.startsWith(prefix)) {
			String file = name.substring(name.indexOf(prefix) + prefix.length());
			switch (file) {
			case "abandoned_mineshaft":
			case "desert_pyramid":
			case "nether_bridge":
			case "igloo_chest":
			case "jungle_temple":
			case "simple_dungeon":
			case "stronghold_corridor":
			case "end_city_treasure":
			case "village_blacksmith":
				evt.getTable().addPool(getInjectPool(file));
				break;
			default: break;
			}
		}
	}

	private LootPool getInjectPool(String entryName) {
		return new LootPool(new LootEntry[] { getInjectEntry(entryName, 1) }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "nostrum_inject_pool");
	}

	private LootEntryTable getInjectEntry(String name, int weight) {
		return new LootEntryTable(new ResourceLocation(NostrumMagica.MODID, "inject/" + name), weight, 0, new LootCondition[0], "nostrum_inject_entry");
	}

}

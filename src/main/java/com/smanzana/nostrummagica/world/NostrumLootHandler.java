package com.smanzana.nostrummagica.world;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Taken almost exactly from https://github.com/Vazkii/Botania/blob/e38556d265fcf43273c99ea1299a35400bf0c405/src/main/java/vazkii/botania/common/core/loot/LootHandler.java
public final class NostrumLootHandler {

	public NostrumLootHandler() {
		MinecraftForge.EVENT_BUS.register(this);
//		for (String s : TABLES) {
//			LootTableList.register(new ResourceLocation(NostrumMagica.MODID, s));
//		} // automatically registered?
	}

	@SubscribeEvent
	public void lootLoad(LootTableLoadEvent evt) {
		// Imagine this just looking at the prefix and then seeing if there's an 'inject' in the data folder.
		// Can't find an easy way to check if there's a file there or not though, so leaving hardcoded.
		
		String prefixChest = "chests/";
		String path = evt.getName().getPath();

		if (path.startsWith(prefixChest)) {
			String file = path.substring(path.indexOf(prefixChest) + prefixChest.length());
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
				evt.getTable().addPool(getInjectPool(prefixChest + file));
				break;
			default: break;
			}
		}
		

		String prefixBlock = "blocks/";
		if (path.startsWith(prefixBlock)) {
			String file = path.substring(path.indexOf(prefixBlock) + prefixBlock.length());
			
			// For leaves, we have to be kinda generic.
			// This is clearly not the best
			if (file.toLowerCase().contains("_leaves")) {
				evt.getTable().addPool(getInjectPool(prefixBlock + "leaves"));
			}
		}
	}

	private LootPool getInjectPool(String entryName) {
		return LootPool.builder()
				.addEntry(TableLootEntry.builder(new ResourceLocation(NostrumMagica.MODID, "inject/" + entryName))
						.weight(1))
				.rolls(new RandomValueRange(1))
				.bonusRolls(0, 1)
				.name("nostrum_inject_pool")
		.build();
		//return new LootPool(new LootEntry[] { getInjectEntry(entryName, 1) }, new ILootCondition[0], new ILootFunction[0], new RandomValueRange(1), new RandomValueRange(0, 1), "nostrum_inject_pool");
	}

//	private TableLootEntry getInjectEntry(String name, int weight) {
//		return new TableLootEntry(new ResourceLocation(NostrumMagica.MODID, "inject/" + name), weight, 0, new ILootCondition[0], "nostrum_inject_entry");
//	}

}

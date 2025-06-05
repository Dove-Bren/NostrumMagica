package com.smanzana.nostrummagica.loot;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loot.function.RollEnhancementPageFunction;
import com.smanzana.nostrummagica.loot.function.RollTomeplateFunction;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.core.Registry;

public class NostrumLoot {
	
	public static LootItemFunctionType FUNCTION_ROLL_TOMEPLATE;
	public static LootItemFunctionType FUNCTION_ROLL_ENHANCEMENT;

	//@SubscribeEvent registry<ILootFunction> ....
	public static final void RegisterLootFunctions() {
		Register(RollTomeplateFunction.ID, new LootItemFunctionType(RollTomeplateFunction.SERIALIZER));
		Register(RollEnhancementPageFunction.ID, new LootItemFunctionType(RollEnhancementPageFunction.SERIALIZER));
	}
	
	private static final LootItemFunctionType Register(String id, LootItemFunctionType type) {
		Registry<LootItemFunctionType> registry = Registry.LOOT_FUNCTION_TYPE;
		Registry.register(registry, NostrumMagica.Loc(id), type);
		return type;
	}
	
}

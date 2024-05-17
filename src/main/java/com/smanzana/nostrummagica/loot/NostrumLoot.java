package com.smanzana.nostrummagica.loot;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loot.function.RollTomeplateFunction;

import net.minecraft.loot.LootFunctionType;
import net.minecraft.util.registry.Registry;

public class NostrumLoot {
	
	public static LootFunctionType FUNCTION_ROLL_TOMEPLATE;

	//@SubscribeEvent registry<ILootFunction> ....
	public static final void RegisterLootFunctions() {
		Register(RollTomeplateFunction.ID, new LootFunctionType(RollTomeplateFunction.SERIALIZER));
	}
	
	private static final LootFunctionType Register(String id, LootFunctionType type) {
		Registry<LootFunctionType> registry = Registry.LOOT_FUNCTION_TYPE;
		Registry.register(registry, NostrumMagica.Loc(id), type);
		return type;
	}
	
}

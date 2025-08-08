package com.smanzana.nostrummagica.loot;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loot.condition.BlockTagCondition;
import com.smanzana.nostrummagica.loot.condition.SpiderEntityCondition;
import com.smanzana.nostrummagica.loot.condition.UndeadEntityCondition;
import com.smanzana.nostrummagica.loot.function.RollEnhancementPageFunction;
import com.smanzana.nostrummagica.loot.function.RollTomeplateFunction;
import com.smanzana.nostrummagica.loot.modifier.ReagentMobDropModifier;
import com.smanzana.nostrummagica.loot.modifier.SkyAshModifier;

import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NostrumLoot {
	
	public static LootItemFunctionType FUNCTION_ROLL_TOMEPLATE;
	public static LootItemFunctionType FUNCTION_ROLL_ENHANCEMENT;
	public static LootItemConditionType CONDITION_BLOCK_TAG;
	public static LootItemConditionType CONDITION_UNDEAD_ENTITY;
	public static LootItemConditionType CONDITION_SPIDER_ENTITY;

	//@SubscribeEvent registry<ILootFunction> ....
	public static final void RegisterLootFunctions() {
		FUNCTION_ROLL_TOMEPLATE = Register(RollTomeplateFunction.ID, new LootItemFunctionType(RollTomeplateFunction.SERIALIZER));
		FUNCTION_ROLL_ENHANCEMENT = Register(RollEnhancementPageFunction.ID, new LootItemFunctionType(RollEnhancementPageFunction.SERIALIZER));
	}
	
	public static final void RegisterLootConditions() {
		CONDITION_BLOCK_TAG = Register(BlockTagCondition.ID, new LootItemConditionType(BlockTagCondition.SERIALIZER));
		CONDITION_UNDEAD_ENTITY = Register(UndeadEntityCondition.ID, new LootItemConditionType(UndeadEntityCondition.SERIALIZER));
		CONDITION_SPIDER_ENTITY = Register(SpiderEntityCondition.ID, new LootItemConditionType(SpiderEntityCondition.SERIALIZER));
	}
	
	@SubscribeEvent
	public static final void registerModifiers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
		final var registry = event.getRegistry();
		registry.register(SkyAshModifier.SERIALIZER.setRegistryName(NostrumMagica.Loc(SkyAshModifier.ID)));
		registry.register(ReagentMobDropModifier.SERIALIZER.setRegistryName(NostrumMagica.Loc(ReagentMobDropModifier.ID)));
	}
	
	private static final LootItemFunctionType Register(String id, LootItemFunctionType type) {
		Registry<LootItemFunctionType> registry = Registry.LOOT_FUNCTION_TYPE;
		Registry.register(registry, NostrumMagica.Loc(id), type);
		return type;
	}
	
	private static final LootItemConditionType Register(String id, LootItemConditionType type) {
		Registry<LootItemConditionType> registry = Registry.LOOT_CONDITION_TYPE;
		Registry.register(registry, NostrumMagica.Loc(id), type);
		return type;
	}
	
}

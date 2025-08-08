package com.smanzana.nostrummagica.loot.modifier;

import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class ReagentMobDropModifier extends LootModifier {
	
	public static final String ID = "add_reagent";
	
	private ItemStack stack;
	private float baseChance;
	private float lootingChance;
	private boolean rollPerLooting;

	protected ReagentMobDropModifier(LootItemCondition[] conditionsIn, ItemStack stack, float baseChance, float lootingChance, boolean rollPerLooting) {
		super(conditionsIn);
		this.stack = stack;
		this.baseChance = baseChance;
		this.lootingChance = lootingChance;
		this.rollPerLooting = rollPerLooting;
	}
	
	protected float getChance(LootContext context) {
		return this.baseChance + (context.getLootingModifier() * lootingChance);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		final int rolls = 1 + (this.rollPerLooting ? context.getLootingModifier() : 0);
		for (int i = 0; i < rolls; i++) {
			final Random random = context.getLevel().getRandom();
			if (random.nextFloat() <= getChance(context)) {
				generatedLoot.add(stack.copy());
			}
		}
		
		return generatedLoot;
	}
	
	public static class Serializer extends GlobalLootModifierSerializer<ReagentMobDropModifier> {

		@Override
		public ReagentMobDropModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
			Item item = GsonHelper.getAsItem(object, "item");
			float baseChance = GsonHelper.getAsFloat(object, "base_chance");
			float lootingChance = GsonHelper.getAsFloat(object, "looting_chance");
			boolean rollPerLooting = GsonHelper.getAsBoolean(object, "roll_per_looting");
			return new ReagentMobDropModifier(ailootcondition, new ItemStack(item), baseChance, lootingChance, rollPerLooting);
		}

		@Override
		public JsonObject write(ReagentMobDropModifier instance) {
			JsonObject json = this.makeConditions(instance.conditions);
			json.addProperty("item", instance.stack.getItem().getRegistryName().toString());
			json.addProperty("base_chance", instance.baseChance);
			json.addProperty("looting_chance", instance.lootingChance);
			json.addProperty("roll_per_looting", instance.rollPerLooting);
			return json;
		}
		
	}
	
	public static final Serializer SERIALIZER = new Serializer();

}

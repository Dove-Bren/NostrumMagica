package com.smanzana.nostrummagica.loot.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.loot.NostrumLoot;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RollEnhancementPageFunction extends LootItemConditionalFunction {
	
	public static final String ID = "roll_enhancement";
	
	private final int weightMin;
	private final int weightMax;
	
	protected RollEnhancementPageFunction(LootItemCondition[] conditionsIn, int weightMin, int weightMax) {
		super(conditionsIn);
		this.weightMin = weightMin;
		this.weightMax = weightMax;
	}
	
	@Override
	public LootItemFunctionType getType() {
		return NostrumLoot.FUNCTION_ROLL_ENHANCEMENT;
	}
	
	@Override
	public ItemStack run(ItemStack stack, LootContext context) {
		// +1 to max when calculating range so that (0, 3) becomes {0, 1, 2, 3} instead of {0, 1, 2}.
		// Since we know min <= max, we know range will be 1+ too and avoid an exception in Random#nextInt()
		final int weightRange = (weightMax + 1) - weightMin;
		final int weight = context.getRandom().nextInt(weightRange) + weightMin;
		
		System.out.println("Rolling for enhancement with weight allowance %d".formatted(weight));
		
		// Search for a random enhancement, and then get the highest level of it that fits under weight
		List<SpellTomeEnhancement> all = new ArrayList<>();
		all.addAll(SpellTomeEnhancement.getEnhancements());
		Collections.shuffle(all);
		
		ItemStack pageOut = ItemStack.EMPTY;
		while (!all.isEmpty()) {
			SpellTomeEnhancement candidate = all.get(0);
			int level = -1;
			for (int i = candidate.getMaxLevel(); i >= 0; i--) {
				int req = candidate.getWeight(i);
				if (req <= weight) {
					level = i;
					break;
				}
			}
			
			if (level == -1) {
				// this one didn't work
				all.remove(0);
				continue;
			} else {
				pageOut = SpellTomePage.Create(candidate, level);
				break;
			}
		}
		
		return pageOut;
	}
	
	protected static final class RollEnhancementPageFunctionSerializer extends LootItemConditionalFunction.Serializer<RollEnhancementPageFunction> {

		@Override
		public void serialize(JsonObject object, RollEnhancementPageFunction function, JsonSerializationContext context) {
			object.addProperty("weight_min", function.weightMin);
			object.addProperty("weight_max", function.weightMax);
		}

		@Override
		public RollEnhancementPageFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditionsIn) {
			final int weightMin = GsonHelper.getAsInt(object, "weight_min", 0);
			final int weightMax = GsonHelper.getAsInt(object, "weight_max", 5);
			
			if (weightMin > weightMax) {
				throw new JsonSyntaxException("weight_min(" + weightMin + ") cannot be greater than weight_max(" + weightMax + ")");
			}
			if (weightMin < 0) {
				throw new JsonSyntaxException("weight_min(" + weightMin + ") cannot be less than 0");
			}
			
			return new RollEnhancementPageFunction(conditionsIn, weightMin, weightMax);
		}
		
	}
	
	public static final Serializer<RollEnhancementPageFunction> SERIALIZER = new RollEnhancementPageFunctionSerializer();

}

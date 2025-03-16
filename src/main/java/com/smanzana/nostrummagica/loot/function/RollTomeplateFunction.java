package com.smanzana.nostrummagica.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.loot.NostrumLoot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.util.GsonHelper;

public class RollTomeplateFunction extends LootItemConditionalFunction {
	
	public static final String ID = "roll_tomeplate";
	
	private final int capacityMin;
	private final int capacityMax;
	private final int slotMin;
	private final int slotMax;
	
	protected RollTomeplateFunction(LootItemCondition[] conditionsIn, int capacityMin, int capacityMax, int slotMin, int slotMax) {
		super(conditionsIn);
		this.capacityMax = capacityMax;
		this.capacityMin = capacityMin;
		this.slotMin = slotMin;
		this.slotMax = slotMax;
	}
	
	@Override
	public LootItemFunctionType getType() {
		return NostrumLoot.FUNCTION_ROLL_TOMEPLATE;
	}
	
	@Override
	public ItemStack run(ItemStack stack, LootContext context) {
		ItemStack newStack = stack.copy();
		
		// +1 to max when calculating range so that (0, 3) becomes {0, 1, 2, 3} instead of {0, 1, 2}.
		// Since we know min <= max, we know range will be 1+ too and avoid an exception in Random#nextInt()
		final int capacityRange = (capacityMax + 1) - capacityMin;
		final int capacity = context.getRandom().nextInt(capacityRange) + capacityMin;
		
		final int slotRange = (slotMax + 1) - slotMin;
		final int slots = context.getRandom().nextInt(slotRange) + slotMin;
		
		SpellPlate.setCapacity(newStack, capacity);
		SpellPlate.setSlots(newStack, slots);
		
		return newStack;
	}
	
	protected static final class RollTomeplateFunctionSerializer extends LootItemConditionalFunction.Serializer<RollTomeplateFunction> {

		@Override
		public void serialize(JsonObject object, RollTomeplateFunction function, JsonSerializationContext context) {
			object.addProperty("capacity_min", function.capacityMin);
			object.addProperty("capacity_max", function.capacityMax);
			object.addProperty("slot_min", function.slotMin);
			object.addProperty("slot_max", function.slotMax);
		}

		@Override
		public RollTomeplateFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditionsIn) {
			final int capacityMin = GsonHelper.getAsInt(object, "capacity_min", 0);
			final int capacityMax = GsonHelper.getAsInt(object, "capacity_max", 10);
			final int slotMin = GsonHelper.getAsInt(object, "slot_min", 1);
			final int slotMax = GsonHelper.getAsInt(object, "slot_max", 5);
			
			if (capacityMin > capacityMax) {
				throw new JsonSyntaxException("capacity_min(" + capacityMin + ") cannot be greater than capacity_max(" + capacityMax + ")");
			}
			if (capacityMin < 0) {
				throw new JsonSyntaxException("capacity_min(" + capacityMin + ") cannot be less than 0");
			}
			if (slotMin > slotMax) {
				throw new JsonSyntaxException("slot_min(" + slotMin + ") cannot be greater than slot_max(" + slotMax + ")");
			}
			if (slotMin < 0 || slotMin > 5) {
				throw new JsonSyntaxException("slot_min(" + slotMin + ") cannot be less than 0 or greater than 5");
			}
			if (slotMax < 0 || slotMax > 5) {
				throw new JsonSyntaxException("slot_max(" + slotMax + ") cannot be greater than 5");
			}
			
			return new RollTomeplateFunction(conditionsIn, capacityMin, capacityMax, slotMin, slotMax);
		}
		
	}
	
	public static final Serializer<RollTomeplateFunction> SERIALIZER = new RollTomeplateFunctionSerializer();

}

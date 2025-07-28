package com.smanzana.nostrummagica.loot.modifier;

import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class SkyAshModifier extends LootModifier {
	
	public static final String ID = "add_skyash";
	
	private float decayChance;
	
	private float activeChance;

	protected SkyAshModifier(LootItemCondition[] conditionsIn, float decayChance, float activeChance) {
		super(conditionsIn);
		this.decayChance = decayChance;
		this.activeChance = activeChance;
	}
	
	protected float getChance(LootContext context) {
		if (context.getParamOrNull(LootContextParams.THIS_ENTITY) == null) {
			return this.decayChance;
		}
		return this.activeChance;
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		final Random random = context.getLevel().getRandom();
		if (random.nextFloat() <= getChance(context)) {
			generatedLoot.add(new ItemStack(NostrumItems.reagentSkyAsh));
		}
		
		return generatedLoot;
	}
	
	public static class Serializer extends GlobalLootModifierSerializer<SkyAshModifier> {

		@Override
		public SkyAshModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
			float decayChance = GsonHelper.getAsFloat(object, "decay_chance");
			float activeChance = GsonHelper.getAsFloat(object, "active_chance");
			return new SkyAshModifier(ailootcondition, decayChance, activeChance);
		}

		@Override
		public JsonObject write(SkyAshModifier instance) {
			JsonObject json = this.makeConditions(instance.conditions);
			json.addProperty("decay_chance", instance.decayChance);
			json.addProperty("active_chance", instance.activeChance);
			return json;
		}
		
	}
	
	public static final Serializer SERIALIZER = new Serializer();

}

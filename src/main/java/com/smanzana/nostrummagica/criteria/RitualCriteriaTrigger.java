package com.smanzana.nostrummagica.criteria;

import com.google.gson.JsonObject;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class RitualCriteriaTrigger extends AbstractCriterionTrigger<RitualCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("ritual");
	public static final RitualCriteriaTrigger Instance = new RitualCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public RitualCriteriaTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		final String ritual = json.get("ritual").getAsString();
		return new Instance(entityPredicate, ritual);
	}
	
	public void trigger(ServerPlayerEntity player, String ritual) {
		this.trigger(player, (instance) -> {
			return instance.test(ritual);
		});
	}
	
	public static class Instance extends CriterionInstance {
		
		private final String ritualKey;

		public Instance(AndPredicate playerCondition, String ritualKey) {
			super(RitualCriteriaTrigger.ID, playerCondition);
			this.ritualKey = ritualKey;
		}
		
		public boolean test(String ritualKey) {
			return ritualKey.equalsIgnoreCase(this.ritualKey) || this.ritualKey.isEmpty();
		}
		
		@Override
		public JsonObject serializeToJson(ConditionArraySerializer conditions) {
			JsonObject obj = super.serializeToJson(conditions);
			obj.addProperty("ritual", this.ritualKey);
			return obj;
		}
		
	}
	
}

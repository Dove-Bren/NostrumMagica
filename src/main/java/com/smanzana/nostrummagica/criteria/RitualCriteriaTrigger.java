package com.smanzana.nostrummagica.criteria;

import com.google.gson.JsonObject;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class RitualCriteriaTrigger extends SimpleCriterionTrigger<RitualCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("ritual");
	public static final RitualCriteriaTrigger Instance = new RitualCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public RitualCriteriaTrigger.Instance createInstance(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
		final String ritual = json.get("ritual").getAsString();
		return new Instance(entityPredicate, ritual);
	}
	
	public void trigger(ServerPlayer player, String ritual) {
		this.trigger(player, (instance) -> {
			return instance.test(ritual);
		});
	}
	
	public static class Instance extends AbstractCriterionTriggerInstance {
		
		private final String ritualKey;

		public Instance(Composite playerCondition, String ritualKey) {
			super(RitualCriteriaTrigger.ID, playerCondition);
			this.ritualKey = ritualKey;
		}
		
		public boolean test(String ritualKey) {
			return ritualKey.equalsIgnoreCase(this.ritualKey) || this.ritualKey.isEmpty();
		}
		
		@Override
		public JsonObject serializeToJson(SerializationContext conditions) {
			JsonObject obj = super.serializeToJson(conditions);
			obj.addProperty("ritual", this.ritualKey);
			return obj;
		}
		
	}
	
}

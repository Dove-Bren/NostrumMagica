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

public class CraftSpellCriteriaTrigger extends SimpleCriterionTrigger<CraftSpellCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("craft_spell");
	public static final CraftSpellCriteriaTrigger Instance = new CraftSpellCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public CraftSpellCriteriaTrigger.Instance createInstance(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
		return new Instance(entityPredicate);
	}
	
	public void trigger(ServerPlayer player) {
		this.trigger(player, (instance) -> {
			return true;
		});
	}
	
	public static class Instance extends AbstractCriterionTriggerInstance {
		
		public Instance(Composite playerCondition) {
			super(CraftSpellCriteriaTrigger.ID, playerCondition);
		}
		
		@Override
		public JsonObject serializeToJson(SerializationContext conditions) {
			JsonObject obj = super.serializeToJson(conditions);
			return obj;
		}
		
	}
	
}

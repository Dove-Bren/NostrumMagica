package com.smanzana.nostrummagica.criteria;

import com.google.gson.JsonObject;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class TierCriteriaTrigger extends SimpleCriterionTrigger<TierCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("tier");
	public static final TierCriteriaTrigger Instance = new TierCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public TierCriteriaTrigger.Instance createInstance(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
		final String tierKey = json.get("tier").getAsString();
		final EMagicTier tier = EMagicTier.valueOf(tierKey.toUpperCase());
		return new Instance(entityPredicate, tier);
	}
	
	public void trigger(ServerPlayer player, EMagicTier tier) {
		this.trigger(player, (instance) -> {
			return instance.test(tier);
		});
	}
	
	public static class Instance extends AbstractCriterionTriggerInstance {
		
		private final EMagicTier tier;

		public Instance(Composite playerCondition, EMagicTier tier) {
			super(TierCriteriaTrigger.ID, playerCondition);
			this.tier = tier;
		}
		
		public boolean test(EMagicTier tierIn) {
			return tierIn == this.tier;
		}
		
		@Override
		public JsonObject serializeToJson(SerializationContext conditions) {
			JsonObject obj = super.serializeToJson(conditions);
			obj.addProperty("tier", this.tier.name().toLowerCase());
			return obj;
		}
		
	}
	
}

package com.smanzana.nostrummagica.criteria;

import com.google.gson.JsonObject;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class TierCriteriaTrigger extends AbstractCriterionTrigger<TierCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("tier");
	public static final TierCriteriaTrigger Instance = new TierCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public TierCriteriaTrigger.Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		final String tierKey = json.get("tier").getAsString();
		final EMagicTier tier = EMagicTier.valueOf(tierKey.toUpperCase());
		return new Instance(entityPredicate, tier);
	}
	
	public void trigger(ServerPlayerEntity player, EMagicTier tier) {
		this.triggerListeners(player, (instance) -> {
			return instance.test(tier);
		});
	}
	
	public static class Instance extends CriterionInstance {
		
		private final EMagicTier tier;

		public Instance(AndPredicate playerCondition, EMagicTier tier) {
			super(TierCriteriaTrigger.ID, playerCondition);
			this.tier = tier;
		}
		
		public boolean test(EMagicTier tierIn) {
			return tierIn == this.tier;
		}
		
		@Override
		public JsonObject serialize(ConditionArraySerializer conditions) {
			JsonObject obj = super.serialize(conditions);
			obj.addProperty("tier", this.tier.name().toLowerCase());
			return obj;
		}
		
	}
	
}

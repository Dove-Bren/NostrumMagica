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

public class CastSpellCriteriaTrigger extends AbstractCriterionTrigger<CastSpellCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("cast_spell");
	public static final CastSpellCriteriaTrigger Instance = new CastSpellCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public CastSpellCriteriaTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		return new Instance(entityPredicate);
	}
	
	public void trigger(ServerPlayerEntity player) {
		this.trigger(player, (instance) -> {
			return true;
		});
	}
	
	public static class Instance extends CriterionInstance {
		
		public Instance(AndPredicate playerCondition) {
			super(CastSpellCriteriaTrigger.ID, playerCondition);
		}
		
		@Override
		public JsonObject serializeToJson(ConditionArraySerializer conditions) {
			JsonObject obj = super.serializeToJson(conditions);
			return obj;
		}
		
	}
	
}

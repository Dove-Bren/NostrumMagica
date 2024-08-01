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

public class CraftSpellCriteriaTrigger extends AbstractCriterionTrigger<CraftSpellCriteriaTrigger.Instance> {

	private static final ResourceLocation ID = NostrumMagica.Loc("craft_spell");
	public static final CraftSpellCriteriaTrigger Instance = new CraftSpellCriteriaTrigger();
	
	@Override
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	public CraftSpellCriteriaTrigger.Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
		return new Instance(entityPredicate);
	}
	
	public void trigger(ServerPlayerEntity player) {
		this.triggerListeners(player, (instance) -> {
			return true;
		});
	}
	
	public static class Instance extends CriterionInstance {
		
		public Instance(AndPredicate playerCondition) {
			super(CraftSpellCriteriaTrigger.ID, playerCondition);
		}
		
		@Override
		public JsonObject serialize(ConditionArraySerializer conditions) {
			JsonObject obj = super.serialize(conditions);
			return obj;
		}
		
	}
	
}

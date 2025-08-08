package com.smanzana.nostrummagica.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.smanzana.nostrummagica.loot.NostrumLoot;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class UndeadEntityCondition implements LootItemCondition {
	
	public static final String ID = "entity_undead";
	
	public UndeadEntityCondition() {
		;
	}

	@Override
	public boolean test(LootContext context) {
		Entity ent = context.getParam(LootContextParams.THIS_ENTITY);
		return ent != null && ent instanceof LivingEntity living && living.isInvertedHealAndHarm();
	}

	@Override
	public LootItemConditionType getType() {
		return NostrumLoot.CONDITION_UNDEAD_ENTITY;
	}
	
	private static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<UndeadEntityCondition> {

		@Override
		public void serialize(JsonObject json, UndeadEntityCondition condition, JsonSerializationContext context) {
			;
		}

		@Override
		public UndeadEntityCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			return new UndeadEntityCondition();
		}
		
	}
	
	public static final Serializer SERIALIZER = new Serializer();

}

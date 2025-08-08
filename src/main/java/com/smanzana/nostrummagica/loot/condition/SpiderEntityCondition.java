package com.smanzana.nostrummagica.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.smanzana.nostrummagica.loot.NostrumLoot;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class SpiderEntityCondition implements LootItemCondition {
	
	public static final String ID = "entity_spider";
	
	public SpiderEntityCondition() {
		;
	}

	@Override
	public boolean test(LootContext context) {
		Entity ent = context.getParam(LootContextParams.THIS_ENTITY);
		return ent != null && ent instanceof Spider;
	}

	@Override
	public LootItemConditionType getType() {
		return NostrumLoot.CONDITION_SPIDER_ENTITY;
	}
	
	private static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SpiderEntityCondition> {

		@Override
		public void serialize(JsonObject json, SpiderEntityCondition condition, JsonSerializationContext context) {
			;
		}

		@Override
		public SpiderEntityCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			return new SpiderEntityCondition();
		}
		
	}
	
	public static final Serializer SERIALIZER = new Serializer();

}

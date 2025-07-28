package com.smanzana.nostrummagica.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.smanzana.nostrummagica.loot.NostrumLoot;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Adapted from https://github.com/MysticMods/MysticalWorld/blob/1.18/src/main/java/mysticmods/mysticalworld/loot/conditions/BlockTagCondition.java
 */
public class BlockTagCondition implements LootItemCondition {
	
	public static final String ID = "block_tag";
	
	private final TagKey<Block> tag;
	
	public BlockTagCondition(TagKey<Block> tag) {
		this.tag = tag;
	}

	@Override
	public boolean test(LootContext context) {
		BlockState blockstate = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		return blockstate != null && blockstate.is(tag);
	}

	@Override
	public LootItemConditionType getType() {
		return NostrumLoot.CONDITION_BLOCK_TAG;
	}
	
	private static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BlockTagCondition> {

		@Override
		public void serialize(JsonObject json, BlockTagCondition condition, JsonSerializationContext context) {
			json.addProperty("tag", condition.tag.location().toString());
		}

		@Override
		public BlockTagCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			return new BlockTagCondition(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GsonHelper.getAsString(json, "tag"))));
		}
		
	}
	
	public static final Serializer SERIALIZER = new Serializer();

}

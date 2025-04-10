package com.smanzana.nostrummagica.block;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EssenceOreBlock extends OreBlock {

	public static final String ID = "essore";
	
	public EssenceOreBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(1.7f, 30.0f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				);
		
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		final LootContext context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
		final List<ItemStack> loot = new ArrayList<>();
		final int fortune;
		if (context.hasParam(LootContextParams.TOOL)) {
			fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, context.getParamOrNull(LootContextParams.TOOL));
		} else {
			 fortune = 0;
		}
		
		int count = 3;
		if (fortune > 0) {
			count += 2 * context.getRandom().nextInt(fortune);
		}
		for (int i = 0; i < count; i++) {
			loot.add(EssenceItem.getEssence(EMagicElement.getRandom(context.getRandom()), 1));
		}
		
		return loot;
	}
	
	@Override
	public int getExpDrop(BlockState state, LevelReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? Mth.nextInt(RANDOM, 3, 5) : 0;
	}
}

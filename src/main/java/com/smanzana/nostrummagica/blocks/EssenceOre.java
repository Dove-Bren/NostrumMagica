package com.smanzana.nostrummagica.blocks;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ToolType;

public class EssenceOre extends OreBlock {

	public static final String ID = "essore";
	
	public EssenceOre() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(1.7f, 30.0f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(3)
				);
		
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		final LootContext context = builder.withParameter(LootParameters.BLOCK_STATE, state).build(LootParameterSets.BLOCK);
		final List<ItemStack> loot = new ArrayList<>();
		final int fortune;
		if (context.has(LootParameters.TOOL)) {
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, context.get(LootParameters.TOOL));
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
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? MathHelper.nextInt(RANDOM, 3, 5) : 0;
	}
}

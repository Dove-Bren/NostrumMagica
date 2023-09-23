package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
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
	
//	@Override
//	public int quantityDroppedWithBonus(int fortune, Random random) {
//		int base = 1;
//		while (fortune-- >= 0) {
//			base += random.nextInt(2);
//		}
//		
//		return base;
//	}
//	
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return EssenceItem.instance();
//		
//	}
//	
//	@Override
//	public int damageDropped(BlockState state) {
//		return NostrumMagica.rand.nextInt(
//				EMagicElement.values().length
//				);
//	}
	
	@Override
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? MathHelper.nextInt(RANDOM, 3, 5) : 0;
	}
}

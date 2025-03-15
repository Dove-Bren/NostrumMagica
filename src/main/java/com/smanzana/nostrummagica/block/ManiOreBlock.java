package com.smanzana.nostrummagica.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

public class ManiOreBlock extends OreBlock {

	public static final String ID = "mani_ore";
	
	
	public ManiOreBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(2.0f, 30.f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(2)
				);
	}
	
//	@Override
//	public int quantityDroppedWithBonus(int fortune, Random random) {
//		int count = random.nextInt(2) + 1;
//		if (fortune != 0)
//			count += (fortune) + random.nextInt(fortune);
//		return count;
//	}
//	
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return ReagentItem.instance();
//	}
//	
//	@Override
//	public int damageDropped(BlockState state) {
//		return ReagentItem.ReagentType.MANI_DUST.getMeta();
//	}
	
	@Override
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? RANDOM.nextInt(3) : 0;
	}
}

package com.smanzana.nostrummagica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class ManiOreBlock extends OreBlock {

	public static final String ID = "mani_ore";
	
	
	public ManiOreBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(2.0f, 30.f)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
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
	public int getExpDrop(BlockState state, LevelReader world, BlockPos pos, int fortune, int silktouch) {
		return silktouch == 0 ? RANDOM.nextInt(3) : 0;
	}
}

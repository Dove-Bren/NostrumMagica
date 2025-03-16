package com.smanzana.nostrummagica.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class MagicDirtBlock extends Block {

	public static final String ID = "magic_dirt";
	
	public MagicDirtBlock() {
		super(Block.Properties.of(Material.DIRT)
				.strength(.7f, 1.0f)
				.sound(SoundType.GRAVEL)
				.randomTicks()
				);
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (random.nextBoolean() && random.nextBoolean() && random.nextBoolean()) {
			// Check neighbors. If there are 2+ other blocks, dont' expand. Otherwise, convert neighbors into magic dirt
			int count = 0;
			BlockPos[] neighbors = new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()};
			for (BlockPos neighbor : neighbors) {
				BlockState neighborState = worldIn.getBlockState(neighbor);
				if (neighborState != null && neighborState.getBlock() == this) {
					count++;
				}
			}
			
			if (count < 2) {
				for (BlockPos neighbor : neighbors) {
					BlockState neighborState = worldIn.getBlockState(neighbor);
					if (neighborState != null
							&& neighborState.canOcclude()
							&& neighborState.getMaterial() != Material.AIR
							&& neighborState.getBlock() != this
							&& neighborState.getDestroySpeed(worldIn, neighbor) <= 1) {
						worldIn.setBlockAndUpdate(neighbor, this.defaultBlockState());
					}
				}
			}
		}
	}
}

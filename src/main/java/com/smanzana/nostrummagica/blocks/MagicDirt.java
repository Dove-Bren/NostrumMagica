package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

public class MagicDirt extends Block {

	public static final String ID = "magic_dirt";
	
	public MagicDirt() {
		super(Block.Properties.create(Material.EARTH)
				.hardnessAndResistance(.7f, 1.0f)
				.sound(SoundType.GROUND)
				.harvestTool(ToolType.SHOVEL)
				.harvestLevel(1)
				.tickRandomly()
				);
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
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
							&& neighborState.isSolid()
							&& neighborState.getMaterial() != Material.AIR
							&& neighborState.getBlock() != this
							&& neighborState.getBlockHardness(worldIn, neighbor) <= 1) {
						worldIn.setBlockState(neighbor, this.getDefaultState());
					}
				}
			}
		}
	}
}

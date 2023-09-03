package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagicDirt extends Block {

	public static final String ID = "magic_dirt";
	
	private static MagicDirt instance = null;
	public static MagicDirt instance() {
		if (instance == null)
			instance = new MagicDirt();
		
		return instance;
	}
	
	public MagicDirt() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(0.7f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GROUND);
		this.setHarvestLevel("shovel", 1);
		this.setTickRandomly(true);
	}
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, BlockState state, Random random) {
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
							&& neighborState.isFullBlock()
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

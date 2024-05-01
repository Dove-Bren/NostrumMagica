package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public class ChalkBlock extends Block {

	public static final String ID = "nostrum_chalk_block";
	protected static final VoxelShape CHALK_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16 * 0.03125D, 16.0D);
	
	public ChalkBlock() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(.01f)
				.setLightLevel((state) -> 1)
				.noDrops()
				.notSolid()
				);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return Block.hasSolidSideOnTop(worldIn, pos.down());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return facing == Direction.DOWN && !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
//	@Override
//	public void onNeighborChange(BlockState state, IBlockReader world, BlockPos pos, BlockPos neighbor) {
//		if (world instanceof World && !((World) world).isRemote) {
//			if (!isValidPosition((World) world, pos)) {
//				((World) world).setBlockToAir(pos);
//			}
//		}
//	}
}

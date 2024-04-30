package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

public class MagicWall extends BreakableBlock {

	public static final String ID = "magic_wall";
	private static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 3);
	private static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	
	public MagicWall() {
		super(Block.Properties.create(Material.PLANTS)
				.hardnessAndResistance(.01f, 1.0f)
				.sound(SoundType.GLASS)
				.tickRandomly()
				.noDrops()
				.notSolid()
				);
		//this.setLightOpacity(2);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(DECAY, 0)
				.with(LEVEL, 0));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
//	@Override
//	public boolean isSolid(BlockState state) {
//		return false;
//	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(DECAY, LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public BlockState getState(int level) {
		return getDefaultState().with(DECAY, 0)
				.with(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
			int decay = state.get(DECAY) + 1;
			if (decay >= 1) {
				worldIn.removeBlock(pos, false);
			} else {
				worldIn.setBlockState(pos, state.with(DECAY, decay));
			}
    }
	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockReader worldIn, BlockPos pos, Direction side) {
//		return false;
//    }
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		
		int level = state.get(LEVEL);
		
		if (level <= 0
				|| (level >= 2 && !(context.getEntity() instanceof PlayerEntity))
				|| (level == 1 && !(context.getEntity() instanceof ItemEntity))) {
			return VoxelShapes.fullCube();
		}
		
		return VoxelShapes.empty();
    }
	
//	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//		super.onBlockAdded(worldIn, pos, state);
//	}

}

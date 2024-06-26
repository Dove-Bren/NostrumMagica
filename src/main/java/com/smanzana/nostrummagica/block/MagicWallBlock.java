package com.smanzana.nostrummagica.block;

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
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MagicWallBlock extends BreakableBlock {

	public static final String ID = "magic_wall";
	private static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 3);
	private static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	private static final int DECAY_TICKS = 25;
	
	public MagicWallBlock() {
		super(Block.Properties.create(Material.PLANTS)
				.hardnessAndResistance(.01f, 1.0f)
				.sound(SoundType.GLASS)
				.noDrops()
				.tickRandomly()
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
			if (decay > 3) {
				worldIn.removeBlock(pos, false);
			} else {
				worldIn.setBlockState(pos, state.with(DECAY, decay));
				worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
			}
    }
	
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
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		//worldIn.getPendingBlockTicks().scheduleTick(currentPos, state.getBlock(), DECAY_TICKS);
		return state;
	}

}

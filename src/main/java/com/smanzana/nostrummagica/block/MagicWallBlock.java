package com.smanzana.nostrummagica.block;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class MagicWallBlock extends HalfTransparentBlock {

	public static final String ID = "magic_wall";
	private static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 3);
	private static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	private static final int DECAY_TICKS = 25;
	
	public MagicWallBlock() {
		super(Block.Properties.of(Material.PLANT)
				.strength(.01f, 1.0f)
				.sound(SoundType.GLASS)
				.noDrops()
				.randomTicks()
				.noOcclusion()
				);
		//this.setLightOpacity(2);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(DECAY, 0)
				.setValue(LEVEL, 0));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
	
//	@Override
//	public boolean isSolid(BlockState state) {
//		return false;
//	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DECAY, LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public BlockState getState(int level) {
		return defaultBlockState().setValue(DECAY, 0)
				.setValue(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
			int decay = state.getValue(DECAY) + 1;
			if (decay > 3) {
				worldIn.removeBlock(pos, false);
			} else {
				worldIn.setBlockAndUpdate(pos, state.setValue(DECAY, decay));
				worldIn.getBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
			}
    }
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		int level = state.getValue(LEVEL);
		
		if (level <= 0
				|| (level >= 2 && !(context.getEntity() instanceof Player))
				|| (level == 1 && !(context.getEntity() instanceof ItemEntity))) {
			return Shapes.block();
		}
		
		return Shapes.empty();
    }
	
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		//worldIn.getPendingBlockTicks().scheduleTick(currentPos, state.getBlock(), DECAY_TICKS);
		return state;
	}

}

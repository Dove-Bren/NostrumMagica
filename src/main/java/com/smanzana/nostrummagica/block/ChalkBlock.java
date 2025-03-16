package com.smanzana.nostrummagica.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

public class ChalkBlock extends CrossCollisionBlock {

	public static final String ID = "nostrum_chalk_block";
	protected static final VoxelShape CHALK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16 * 0.03125D, 16.0D);
	
	public ChalkBlock() {
		super(0, 0, 16, 16, 16, Block.Properties.of(Material.CLOTH_DECORATION)
				.strength(.01f)
				.lightLevel((state) -> 1)
				.noDrops()
				.noOcclusion()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(WATERLOGGED, false));;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return Block.canSupportRigidBlock(worldIn, pos.below());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}
		
		// If below changed, possibly break
		if (facing == Direction.DOWN) {
			if (!this.canSurvive(stateIn, worldIn, currentPos)) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		
		if (facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
			return stateIn.setValue(PROPERTY_BY_DIRECTION.get(facing), Boolean.valueOf(this.canConnect(facingState)));
		}
		
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	protected boolean canConnect(BlockState state) {
		return state.getBlock() == this;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final FluidState fluidstate = world.getFluidState(pos);
		return super.getStateForPlacement(context)
				.setValue(NORTH, canConnect(world.getBlockState(pos.north())))
				.setValue(EAST, canConnect(world.getBlockState(pos.east())))
				.setValue(SOUTH, canConnect(world.getBlockState(pos.south())))
				.setValue(WEST, canConnect(world.getBlockState(pos.west())))
				.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER))
				;
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

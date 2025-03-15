package com.smanzana.nostrummagica.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ChalkBlock extends FourWayBlock {

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
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return Block.canSupportRigidBlock(worldIn, pos.below());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		final World world = context.getLevel();
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
